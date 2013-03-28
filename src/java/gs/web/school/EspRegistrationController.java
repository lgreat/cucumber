package gs.web.school;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.WelcomeMessageStatus;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.json.JSONObject;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.State;
import gs.web.community.registration.EmailVerificationEmail;
import gs.web.community.registration.UserCommand;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.HttpCacheInterceptor;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.SitePrefCookie;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.validator.UserCommandValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping("/official-school-profile/")
public class EspRegistrationController implements ReadWriteAnnotationController {
    private static final Log _log = LogFactory.getLog(EspRegistrationController.class);

    public static final String VIEW = "school/espRegistration";

    HttpCacheInterceptor _cacheInterceptor = new HttpCacheInterceptor();

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private IUserDao _userDao;

    @Autowired
    private ISchoolDao _schoolDao;

    private ExactTargetAPI _exactTargetAPI;

    @Autowired
    @Qualifier("emailVerificationEmail")
    private EmailVerificationEmail _emailVerificationEmail;

    @RequestMapping(value = "register.page", method = RequestMethod.GET)
    public String showForm(ModelMap modelMap, HttpServletRequest request,
                           @RequestParam(value = "state", required = false) String state,
                           @RequestParam(value = "city", required = false) String city,
                           @RequestParam(value = "schoolId", required = false) String schoolId) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();
        EspRegistrationCommand command = new EspRegistrationCommand();

        if (user != null && user.getId() != null) {
            //If the user isa super user, then redirect to dashboard.
            if (user.hasRole(Role.ESP_SUPERUSER)) {
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
                return "redirect:" + urlBuilder.asFullUrl(request);
            }

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
            // provisional users are treated as approved
            for (EspMembership membership : memberships) {
                if (membership.getActive() && membership.getStatus().equals(EspMembershipStatus.APPROVED)) {
                    modelMap.clear();
                    UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
                    return "redirect:" + urlBuilder.asFullUrl(request);
                } else if (membership.getStatus().equals(EspMembershipStatus.PROCESSING)) {
                    modelMap.addAttribute("isUserAwaitingESPMembership", true);
                } else if (membership.getStatus().equals(EspMembershipStatus.PROVISIONAL)) {
                    modelMap.clear();
                    UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
                    return "redirect:" + urlBuilder.asFullUrl(request);
                }
            }
        } else if (request.getParameter("email") != null) {
            command.setEmail(request.getParameter("email"));
        }

        String userState = request.getParameter("us");
        if (StringUtils.equals("0.a", userState)) {
            modelMap.addAttribute("newUserFromSignIn", true);
        }

        //set preselectSchool to false initially
        modelMap.put("preselectSchool", false);

        //Pre-select form fields
        if(StringUtils.isNotBlank(state) && StringUtils.isNotBlank(city) && StringUtils.isNotBlank(schoolId)) {
            try {
                modelMap.put("state", State.fromString(state));
                modelMap.put("preselectSchool", true);
            }
            catch (IllegalArgumentException ex) {
                _log.debug("EspRegistrationController: Invalid state abbreviation passed.");
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
            user.setHow("esp");
            setFieldsOnUserUsingCommand(command, user);
            getUserDao().saveUser(user);
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

        if (command.getSchoolId() != null && command.getState() != null) {
            School school = getSchoolDao().getSchoolById(command.getState(), command.getSchoolId());
            if (!user.isEmailValidated() && school != null) {
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
                _emailVerificationEmail.sendOSPVerificationEmail(request, user, urlBuilder.asSiteRelative(request), school);
            }
        }
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
        String schoolName = "";
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
                    userState.setNewUser(false);
                    if (!user.isPasswordEmpty() && user.isEmailValidated()) {
                        userState.setUserEmailValidated(true);
                    }

                    //Check is user is already approved or if pending, disabled or rejected.
                    if (user.hasRole(Role.ESP_MEMBER) || user.hasRole(Role.ESP_SUPERUSER)) {
                        userState.setUserApprovedESPMember(true);
                    } else {
                        List<EspMembership> memberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), false);
                        for (EspMembership membership : memberships) {
                            if (membership.getStatus().equals(EspMembershipStatus.PROVISIONAL)) {
                                userState.setUserApprovedESPMember(true);
                            } else if (membership.getStatus().equals(EspMembershipStatus.PROCESSING)) {
                                userState.setUserAwaitingESPMembership(true);
                            } else if (membership.getStatus().equals(EspMembershipStatus.DISABLED) && !membership.getActive()) {
                                userState.setUserESPDisabled(true);
                            } else if (membership.getStatus().equals(EspMembershipStatus.REJECTED) && !membership.getActive()) {
                                userState.setUserESPRejected(true);
                            }else if(membership.getStatus().equals(EspMembershipStatus.PRE_APPROVED) && !membership.getActive()){
                                userState.setUserESPPreApproved(true);
                                schoolName = getSchoolNameForEspMembership(membership);
                            }
                        }
                    }
                }
            }
        }

        try {
            JSONObject rval;
            // Note this must not use generics or else JSONObject's constructor dies
            Map data = userState.getUserState();
            data.put("schoolName", schoolName);
            rval = new JSONObject(data);
            _cacheInterceptor.setNoCacheHeaders(response);
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
            userProfile.setHow("esp");
            user.setUserProfile(userProfile);
        }
    }
    
    protected void sendRequestReceivedEmail(User user, School school) {
        String triggerKey = "ESP-request-confirm";
        Map<String,String> emailAttributes = new HashMap<String,String>();
        emailAttributes.put("ESP_schoolname", school.getName());
        emailAttributes.put("first_name", user.getFirstName());

        getExactTargetAPI().sendTriggeredEmail(triggerKey, user, emailAttributes);
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
            } else if (espMembership.getStatus().equals(EspMembershipStatus.DISABLED) && !espMembership.getActive()) {
                espMembership.setStatus(EspMembershipStatus.PROCESSING);
                getEspMembershipDao().saveEspMembership(espMembership);
            }
        }
    }


    @RequestMapping(value = "checkStateSchoolUserUnique.page", method = RequestMethod.GET)
    protected void checkStateSchoolUserUnique(HttpServletRequest request, HttpServletResponse response, EspRegistrationCommand command) throws Exception {
        State state = command.getState();
        Integer schoolId = command.getSchoolId();
        String email = command.getEmail();
        boolean isUnique = true;
        boolean isDisabled = false;
        boolean isRejected = false;
        boolean isProcessing = false;

        if (state != null && schoolId != null & StringUtils.isNotBlank(email)) {
            User user = getUserDao().findUserFromEmailIfExists(email.trim());
            if (user != null && user.getId() != null) {
                EspMembership espMembership = getEspMembershipDao().findEspMembershipByStateSchoolIdUserId(state, schoolId, user.getId(), false);

                if (espMembership != null) {
                    isUnique = false;
                    isProcessing = !espMembership.getActive() && espMembership.getStatus().equals(EspMembershipStatus.PROCESSING);
                    isDisabled = !espMembership.getActive() && espMembership.getStatus().equals(EspMembershipStatus.DISABLED);
                    isRejected = !espMembership.getActive() && espMembership.getStatus().equals(EspMembershipStatus.REJECTED);
                }
            }
        }

        Map data = new HashMap();
        data.put("isUnique", isUnique);
        data.put("isProcessing", isProcessing);
        data.put("isDisabled", isDisabled);
        data.put("isRejected", isRejected);

        JSONObject rval = new JSONObject(data);
        _cacheInterceptor.setNoCacheHeaders(response);
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

    protected String getSchoolNameForEspMembership(EspMembership espMembership) {
        String schoolName = "";
        if (espMembership != null) {
            School school = _schoolDao.getSchoolById(espMembership.getState(), espMembership.getSchoolId());
            if (school != null) {
                schoolName = school.getName();
            }
        }
        return schoolName;
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
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