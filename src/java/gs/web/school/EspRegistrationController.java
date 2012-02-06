package gs.web.school;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.WelcomeMessageStatus;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.json.JSONObject;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.State;
import gs.web.community.registration.UserCommand;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.SitePrefCookie;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.validator.UserCommandValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping("/school/esp/")
public class EspRegistrationController implements ReadWriteAnnotationController {
    private static final Log _log = LogFactory.getLog(EspRegistrationController.class);

    public static final String VIEW = "school/espRegistration";

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private IUserDao _userDao;

    @Autowired
    private ISchoolDao _schoolDao;

    @RequestMapping(value = "register.page", method = RequestMethod.GET)
    public String showForm(ModelMap modelMap, HttpServletRequest request) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();
        EspRegistrationCommand command = new EspRegistrationCommand();

        if (user != null && user.getId() != null) {
            // User already exists in the session .Therefore pre-fill in form fields.
            command.setEmail(user.getEmail());

            if (!StringUtils.isBlank(user.getFirstName())) {
                command.setFirstName(user.getFirstName());
            }
            if (!StringUtils.isBlank(user.getLastName())) {
                command.setLastName(user.getLastName());
            }
            if (user.getUserProfile() != null && !StringUtils.isBlank(user.getUserProfile().getScreenName())) {
                command.setScreenName(user.getUserProfile().getScreenName());
            }

            //Check if user is awaiting ESP access.
            List<EspMembership> memberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), false);
            //If there is a "accepted" status then redirect the user to dashboard.Else if there are pending memberships
            //display a message to the user.We do not care about rejected or inactive users yet.
            for (EspMembership membership : memberships) {
                if (membership.getActive() && membership.getStatus().equals(EspMembershipStatus.APPROVED)) {
                    UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
                    return "redirect:" + urlBuilder.asFullUrl(request);
                } else if (membership.getStatus().equals(EspMembershipStatus.PROCESSING)) {
                    modelMap.addAttribute("isUserAwaitingESPMembership", true);
                }
            }
        }

        modelMap.addAttribute("espRegistrationCommand", command);
        return VIEW;
    }

    @RequestMapping(value = "register.page", method = RequestMethod.POST)
    public String createEspMembership(@ModelAttribute("espRegistrationCommand") EspRegistrationCommand command,
                                      BindingResult result,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();

        String email = command.getEmail();
        if (StringUtils.isNotBlank(email)) {
            email = email.trim();
        }

        //If there was no user cookie, get the user from the database.
        if (user == null && StringUtils.isNotBlank(email)) {
            user = getUserDao().findUserFromEmailIfExists(email);
        }

        //Server side validation.
        validate(command, result, user);
        if (result.hasErrors()) {
            return VIEW;
        }

        //If user already exists.
        if (user != null && user.getId() != null) {
            setFieldsOnUserUsingCommand(command, user);
        } else {
            //If no user exists so create a new user.
            user = new User();
            user.setEmail(email);
            user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
            setFieldsOnUserUsingCommand(command, user);
            getUserDao().saveUser(user);
            ThreadLocalTransactionManager.commitOrRollback();
        }

        //Set the user's password.
        setUsersPassword(command, user);

        //Set the user's profile and save the user.
        updateUserProfile(command, user);
        getUserDao().updateUser(user);

        //Save ESP membership for user.
        saveEspMembership(command, user);

        OmnitureTracking omnitureTracking = new CookieBasedOmnitureTracking(request, response);
        omnitureTracking.addSuccessEvent(OmnitureTracking.SuccessEvent.EspRegistration);

        return "redirect:" + getSchoolOverview(request, response, command);
    }

    /**
     * Checks the for various states of the user and sets them on the userStateStruct.
     * @param request
     * @param response
     * @param command
     */
    @RequestMapping(value = "checkUserState.page", method = RequestMethod.GET)
    public void checkUserState(HttpServletRequest request, HttpServletResponse response, EspRegistrationCommand command) {

        String email = command.getEmail();
        EspUserStateStruct userState = new EspUserStateStruct();
        User user = null;

        //check if user cookie exists
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        if (sessionContext != null) {
            user = sessionContext.getUser();
            if (user != null && user.getId() != null) {
                userState.setUserCookieSet(true);
            }
        }

        if (StringUtils.isBlank(email)) {
            userState.setEmailValid(false);
        } else if (!StringUtils.isBlank(email)) {
            email = email.trim();
            userState.setEmailValid(validateEmail(email));

            if (userState.isEmailValid()) {

                //In case we have the user object from the cookie, validate that the email on the form is the same as the one in the cookie.
                if (!validateUserCookie(user, email)) {
                   userState.setCookieMatched(false);
                }

                //No user cookie ,therefore check if user exists in the DB.
                if (user == null) {
                    user = getUserDao().findUserFromEmailIfExists(email);
                }

                //Found a user
                if (user != null && user.getId() != null) {
                    if (!user.isPasswordEmpty() && user.isEmailValidated()) {
                        userState.setUserEmailValidated(true);
                    }

                    //Check is user is already approved or if the decision is pending.
                    if (user.hasRole(Role.ESP_MEMBER)) {
                        userState.setUserApprovedESPMember(true);
                    } else {
                        List<EspMembership> memberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), false);
                        for (EspMembership membership : memberships) {
                            if (membership.getStatus().equals(EspMembershipStatus.PROCESSING)) {
                                userState.setUserAwaitingESPMembership(true);
                            }
                        }
                    }
                }
            }
        }

        try {
            JSONObject rval;
            Map data = userState.getUserState();
            rval = new JSONObject(data);
            response.setContentType("application/json");
            response.getWriter().print(rval.toString());
            response.getWriter().flush();
        } catch (Exception exp) {
            _log.error("Error " + exp, exp);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected void setFieldsOnUserUsingCommand(EspRegistrationCommand espMembershipCommand, User user) {
        if (user != null) {
            if (StringUtils.isNotBlank(espMembershipCommand.getFirstName())) {
                user.setFirstName(espMembershipCommand.getFirstName().trim());
            }
            if (StringUtils.isNotBlank(espMembershipCommand.getLastName())) {
                user.setLastName(espMembershipCommand.getLastName().trim());
            }
            //default gender.
            if (StringUtils.isBlank(user.getGender())) {
                user.setGender("u");
            }
            //Only set "how" if it is not already set.
            if (StringUtils.isBlank(user.getHow())) {
                user.setHow("esp");
            }
        }
    }

    protected void setUsersPassword(EspRegistrationCommand espMembershipCommand, User user) throws Exception {
        //If the password is set in the command that means that the password field is visible on the form.
        // It is overwritten each time a request is submitted.
        //NOTE :We accept just spaces as password.Therefore do NOT use : isBlank, use : isEmpty and do NOT trim().
        try {
            if (StringUtils.isNotEmpty(espMembershipCommand.getPassword()) && !user.isEmailValidated()) {
                user.setPlaintextPassword(espMembershipCommand.getPassword());
                user.setEmailProvisional(espMembershipCommand.getPassword());
            }
        } catch (Exception e) {
            _log.warn("Error setting password: " + e.getMessage(), e);
            throw e;
        }
    }

    protected void updateUserProfile(EspRegistrationCommand espMembershipCommand, User user) {
        UserProfile userProfile;

        if (user.getUserProfile() != null && user.getUserProfile().getId() != null) {
            userProfile = user.getUserProfile();
            setUserProfileFieldsFromCommand(espMembershipCommand, userProfile);

        } else {
            userProfile = new UserProfile();
            setUserProfileFieldsFromCommand(espMembershipCommand, userProfile);
            userProfile.setUser(user);
            user.setUserProfile(userProfile);
        }
    }

    protected void setUserProfileFieldsFromCommand(EspRegistrationCommand espMembershipCommand, UserProfile userProfile) {
        if (userProfile != null) {
            if (StringUtils.isNotBlank(espMembershipCommand.getScreenName())) {
                userProfile.setScreenName(espMembershipCommand.getScreenName().trim());
            }

            if (StringUtils.isNotBlank(espMembershipCommand.getCity()) && userProfile.getCity() == null) {
                userProfile.setCity(espMembershipCommand.getCity().trim());
            }

            if (espMembershipCommand.getState() != null && userProfile.getState() == null) {
                userProfile.setState(espMembershipCommand.getState());
            }

            if (StringUtils.isBlank(userProfile.getHow())) {
                userProfile.setHow("esp");
            }

            userProfile.setUpdated(new Date());
        }
    }


    protected void saveEspMembership(EspRegistrationCommand command, User user) {
        State state = command.getState();
        Integer schoolId = command.getSchoolId();

        if (state != null && schoolId != null && schoolId > 0 && user != null && user.getId() != null) {

            EspMembership espMembership = getEspMembershipDao().findEspMembershipByStateSchoolIdUserId(state, schoolId, user.getId(), false);

            if (espMembership == null) {
                EspMembership esp = new EspMembership();
                esp.setActive(false);
                esp.setJobTitle(command.getJobTitle());
                esp.setState(command.getState());
                esp.setSchoolId(command.getSchoolId());
                esp.setStatus(EspMembershipStatus.PROCESSING);
                esp.setUser(user);
                esp.setWebUrl(command.getWebPageUrl());
                getEspMembershipDao().saveEspMembership(esp);
            }else{
                // We do not allow multiple submission for now.
            }
        }
    }


    @RequestMapping(value = "checkStateSchoolUserUnique.page", method = RequestMethod.GET)
    protected void checkStateSchoolUserUnique(HttpServletRequest request, HttpServletResponse response, EspRegistrationCommand command) throws Exception {
        State state = command.getState();
        Integer schoolId = command.getSchoolId();
        String email = command.getEmail();
        boolean isUnique = true;
        boolean isActive = false;

        if (state != null && schoolId != null & StringUtils.isNotBlank(email)) {
            User user = getUserDao().findUserFromEmailIfExists(email.trim());
            if (user != null && user.getId() != null) {
                EspMembership espMembership = getEspMembershipDao().findEspMembershipByStateSchoolIdUserId(state, schoolId, user.getId(), false);

                if (espMembership != null) {
                    isUnique = false;
                    isActive = espMembership.getActive();
                }
            }
        }

        Map data = new HashMap();
        data.put("isUnique", isUnique);
        data.put("isActive", isActive);

        JSONObject rval = new JSONObject(data);
        response.setContentType("application/json");
        response.getWriter().print(rval.toString());
        response.getWriter().flush();

    }

    protected void validate(EspRegistrationCommand espMembershipCommand, BindingResult result, User user) {
        UserCommandValidator validator = new UserCommandValidator();
        validator.setUserDao(getUserDao());
        UserCommand userCommand = new UserCommand();

        userCommand.setFirstName(espMembershipCommand.getFirstName());
        userCommand.setLastName(espMembershipCommand.getLastName());
        userCommand.setPassword(espMembershipCommand.getPassword());
        userCommand.setConfirmPassword(espMembershipCommand.getConfirmPassword());
        userCommand.setScreenName(espMembershipCommand.getScreenName());

        validator.validateFirstName(userCommand, result);
        validator.validateLastName(userCommand, result);

        //Password is not always visible on the form.Therefore check the command to see if its available.
        if (StringUtils.isNotEmpty(espMembershipCommand.getPassword())) {
            validator.validatePassword(userCommand, result);
        }
        validator.validateUsername(userCommand, user, result);

        String email = espMembershipCommand.getEmail();
        if (StringUtils.isNotBlank(email)) {
            email = email.trim();
            if (!validateEmail(email.trim())) {
                result.rejectValue("email", "invalid_email");
            }
        } else {
            result.rejectValue("email", "invalid_email");
        }
        //In case we have the user object from the cookie, validate that the email on the form is the same as the one in the cookie.
        if (!validateUserCookie(user, email)) {
            result.rejectValue("email", "Email in the cookie does not match the one on the form.");
        }

        State state = espMembershipCommand.getState();
        if (state == null) {
            result.rejectValue("state", null, "State cannot be null.");
        }

        Integer schoolId = espMembershipCommand.getSchoolId();
        if (schoolId == null || schoolId == 0 || schoolId == -1) {
            result.rejectValue("schoolId", null, "School cannot be null.");
        }

        String jobTitle = espMembershipCommand.getJobTitle();
        if (StringUtils.isBlank(jobTitle)) {
            result.rejectValue("jobTitle", null, "Job Title cannot be empty.");
        }
    }

    protected boolean validateUserCookie(User user, String email) {
        //Protect against js and cookie manipulation.
        return ((user != null && StringUtils.isNotBlank(email) && !user.getEmail().equals(email.trim())) ? false : true);
    }

    protected boolean validateEmail(String email) {
        org.apache.commons.validator.EmailValidator emv = org.apache.commons.validator.EmailValidator.getInstance();
        return emv.isValid(email);
    }

    protected String getSchoolOverview(HttpServletRequest request, HttpServletResponse response, EspRegistrationCommand command) {
        State state = command.getState();
        int schoolId = command.getSchoolId();
        if (state != null && schoolId > 0) {
            School school = getSchoolDao().getSchoolById(state, schoolId);
            if (school != null) {
                UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
                SitePrefCookie sitePrefCookie = new SitePrefCookie(request, response);
                sitePrefCookie.setProperty("showHover", "schoolEspThankYou");
                return urlBuilder.asFullUrl(request);
            }
        }
        return VIEW;
    }

    public IEspMembershipDao getEspMembershipDao() {
        return _espMembershipDao;
    }

    public void setEspMembershipDao(IEspMembershipDao espMembershipDao) {
        _espMembershipDao = espMembershipDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}