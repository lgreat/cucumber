package gs.web.school;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.WelcomeMessageStatus;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.json.JSONObject;
import gs.data.school.EspMembershipStatus;
import gs.data.school.IEspMembershipDao;
import gs.data.school.EspMembership;
import gs.data.state.State;
import gs.web.community.registration.UserCommand;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.validator.UserCommandValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
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
public class EspMembershipController implements ReadWriteAnnotationController {
    private static final Log _log = LogFactory.getLog(EspMembershipController.class);

    public static final String FORM_VIEW = "school/espMembershipForm";
    public static final String SUCCESS_VIEW = "school/espMembershipSuccess";

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private IUserDao _userDao;

    @RequestMapping(value = "form.page", method = RequestMethod.GET)
    public String showForm(ModelMap modelMap, HttpServletRequest request) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();
        EspMembershipCommand command = new EspMembershipCommand();

        if (user != null && user.getId() != null) {
            // User already exists in the session.Therefore pre-fill in form fields.

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

            modelMap.put("showRegPanel", true);
        }

        modelMap.addAttribute("schoolEspCommand", command);
        return FORM_VIEW;
    }

    @RequestMapping(value = "form.page", method = RequestMethod.POST)
    public String createEspMembership(@ModelAttribute("schoolEspCommand") EspMembershipCommand command,
                                      BindingResult result,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();

        String email = command.getEmail();
        if (StringUtils.isNotBlank(email)) {
            email = email.trim();
        }
        String registeredPassword = command.getRegisteredPassword();

        if (user == null && StringUtils.isNotBlank(email) && StringUtils.isNotEmpty(registeredPassword)) {
            //There is no user cookie.The command has the registered password filled in.
            //Therefore the user already exists, just match the password, log in the user and re-direct to the logged in view.

            user = getUserDao().findUserFromEmailIfExists(email);
            if (user != null && user.isEmailValidated()) {
                boolean matchesPassword = user.matchesPassword(registeredPassword);
                if (matchesPassword) {
                    PageHelper.setMemberAuthorized(request, response, user, true);
                }
            }
            return "redirect:" + "/school/esp/form.page";
        } else {

            //If there was no user cookie, get the user from the database.
            if (user == null && StringUtils.isNotBlank(email)) {
                user = getUserDao().findUserFromEmailIfExists(email);
            }

            //TODO: cookie based omniture?
//            OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);

            //Server side validation.
            validate(command, result, user);
            if (result.hasErrors()) {
                return FORM_VIEW;
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
            //Set the users password and save the user.
            setUsersPassword(command, user);
            getUserDao().updateUser(user);

            //Set the users profile and save the user.
            updateUserProfile(command, user);
            getUserDao().updateUser(user);

            // TODO: do some cookie logic.
            //Save ESP membership for user.
            saveEspMembership(command, user);
            return SUCCESS_VIEW;
        }
    }

    @RequestMapping(value = "checkEspUser.page", method = RequestMethod.GET)
    public void checkIfUserExists(HttpServletRequest request, HttpServletResponse response, EspMembershipCommand command) {
        String email = command.getEmail();
        String fieldsToCollect = "";
        boolean isUserESPMember = false;
        boolean isUserMember = false;
        boolean isEmailValid = true;
        boolean isUserEmailValidated = false;

        if (StringUtils.isBlank(email)) {
            isEmailValid = false;
        } else if (!StringUtils.isBlank(email)) {

            isEmailValid = validateEmail(email.trim());

            if (isEmailValid) {
                User user = getUserDao().findUserFromEmailIfExists(email.trim());
                //Found a user
                if (user != null && user.getId() != null) {
                    isUserMember = true;
                    //Check if the user is already an ESP member.TODO maybe just check the role?
                    List<EspMembership> membership = getEspMembershipDao().findEspMembershipsByUserId(new Long(user.getId()), false);

                    if (user.isEmailValidated()) {
                        isUserEmailValidated = true;
                    }

                    //User already an ESP member.Therefore he will have all the required fields.
                    if (membership != null && membership.size() > 0) {
                        isUserESPMember = true;
                    } else {
                        //User not a ESP member.He might be missing some of the required fields.Therefore collect them.
                        if (StringUtils.isBlank(user.getFirstName())) {
                            fieldsToCollect += "firstName";
                        }
                        if (StringUtils.isBlank(user.getLastName())) {
                            fieldsToCollect += fieldsToCollect.length() > 0 ? ",lastName" : "lastName";
                        }
                        if (user.getUserProfile() == null || (user.getUserProfile() != null && StringUtils.isBlank(user.getUserProfile().getScreenName()))) {
                            fieldsToCollect += fieldsToCollect.length() > 0 ? ",screenName" : "screenName";
                        }
                        if (StringUtils.isBlank(user.getPasswordMd5())) {
                            fieldsToCollect += fieldsToCollect.length() > 0 ? ",password" : "password";
                            fieldsToCollect += ",confirmPassword";
                        }

                    }
                }
            }
        }

        try {
            JSONObject rval;
            Map data = new HashMap();
            if (!isEmailValid) {
                data.put("invalidEmail", "Please enter a valid email address.");
            } else if (isUserEmailValidated) {
                data.put("isUserEmailValidated", true);
            } else if (isUserMember) {
                data.put("isUserMember", true);
                if (fieldsToCollect.length() > 0) {
                    data.put("fieldsToCollect", fieldsToCollect);
                }
                if (isUserESPMember) {
                    data.put("isUserESPMember", true);
                }
            } else {
                data.put("userNotFound", true);
            }

            rval = new JSONObject(data);
            response.setContentType("application/json");
            response.getWriter().print(rval.toString());
            response.getWriter().flush();
        } catch (Exception exp) {
            _log.error("Error " + exp, exp);
            //TODO return an json error .response code to 500.
        }
    }

    @RequestMapping(value = "checkUserPassword.page", method = RequestMethod.GET)
    public void checkUserPassword(HttpServletRequest request, HttpServletResponse response, EspMembershipCommand command) throws Exception {
        String email = command.getEmail();
        String registeredPassword = command.getRegisteredPassword();

        boolean matchesPassword = false;
        if (StringUtils.isNotBlank(email) && StringUtils.isNotEmpty(registeredPassword)) {
            email = email.trim();
            User user = getUserDao().findUserFromEmailIfExists(email);
            if (user != null) {
                matchesPassword = user.matchesPassword(registeredPassword);
            }
        }
        Map data = new HashMap();
        data.put("matchesPassword", matchesPassword);

        JSONObject rval = new JSONObject(data);
        response.setContentType("application/json");
        response.getWriter().print(rval.toString());
        response.getWriter().flush();
    }


    protected void setFieldsOnUserUsingCommand(EspMembershipCommand espMembershipCommand, User user) {
        if (user != null) {
            if (StringUtils.isNotBlank(espMembershipCommand.getFirstName()) && StringUtils.isBlank(user.getFirstName())) {
                user.setFirstName(espMembershipCommand.getFirstName().trim());
            }
            if (StringUtils.isNotBlank(espMembershipCommand.getLastName()) && StringUtils.isBlank(user.getLastName())) {
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

    protected void setUsersPassword(EspMembershipCommand espMembershipCommand, User user) throws Exception {
        //We accept just spaces as password.Therefore do NOT use : isBlank, use : isEmpty and do NOT trim().
        try {
            if (StringUtils.isNotEmpty(espMembershipCommand.getPassword()) && user.isPasswordEmpty()) {
                user.setPlaintextPassword(espMembershipCommand.getPassword());
                user.setEmailProvisional(espMembershipCommand.getPassword());
            }
        } catch (Exception e) {
            _log.warn("Error setting password: " + e.getMessage(), e);
            throw e;
        }
    }

    protected void updateUserProfile(EspMembershipCommand espMembershipCommand, User user) {
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

        //TODO set omniture success events here?
    }

    protected void setUserProfileFieldsFromCommand(EspMembershipCommand espMembershipCommand, UserProfile userProfile) {
        if (userProfile != null) {
            if (StringUtils.isNotBlank(espMembershipCommand.getScreenName()) && StringUtils.isBlank(userProfile.getScreenName())) {
                userProfile.setScreenName(espMembershipCommand.getScreenName().trim());
            }

            if (StringUtils.isNotBlank(espMembershipCommand.getCity()) && StringUtils.isBlank(userProfile.getCity())) {
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


    protected void saveEspMembership(EspMembershipCommand command, User user) {
        State state = command.getState();
        Long schoolId = command.getSchoolId();
        EspMembership espMembership = null;

        if (state != null && schoolId != null && user != null && user.getId() != null) {

            espMembership = getEspMembershipDao().findEspMembershipByStateSchoolIdUserId(state, schoolId, new Long(user.getId()));

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

            }
        }
    }


    @RequestMapping(value = "checkStateSchoolUserUnique.page", method = RequestMethod.GET)
    protected void checkStateSchoolUserUnique(HttpServletRequest request, HttpServletResponse response, EspMembershipCommand command) throws Exception {
        State state = command.getState();
        Long schoolId = command.getSchoolId();
        String email = command.getEmail();
        boolean isUnique = true;
        boolean isActive = false;

        if (state != null && schoolId != null & StringUtils.isNotBlank(email)) {
            User user = getUserDao().findUserFromEmailIfExists(email.trim());
            if (user != null && user.getId() != null) {
                EspMembership espMembership = getEspMembershipDao().findEspMembershipByStateSchoolIdUserId(state, schoolId, new Long(user.getId()));

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

    protected void validate(EspMembershipCommand espMembershipCommand, BindingResult result, User user)  {
        UserCommandValidator validator = new UserCommandValidator();
        validator.setUserDao(getUserDao());
        UserCommand userCommand = new UserCommand();

        userCommand.setFirstName(espMembershipCommand.getFirstName());
        userCommand.setLastName(espMembershipCommand.getLastName());
        userCommand.setPassword(espMembershipCommand.getPassword());
        userCommand.setConfirmPassword(espMembershipCommand.getConfirmPassword());
        userCommand.setScreenName(espMembershipCommand.getScreenName());

        //First name, last name, password and screen name are not always visible on the form.
        //Therefore check the command and validate them.
        if(StringUtils.isNotBlank(espMembershipCommand.getFirstName())){
            validator.validateFirstName(userCommand, result);
        }

        if(StringUtils.isNotBlank(espMembershipCommand.getLastName())){
            validator.validateLastName(userCommand, result);
        }

        if(StringUtils.isNotEmpty(espMembershipCommand.getPassword())){
            validator.validatePassword(userCommand, result);
        }

        if(StringUtils.isNotBlank(espMembershipCommand.getScreenName())){
            validator.validateUsername(userCommand, user, result);
        }

        //Email, state, school, job title are always visible on the form.Therefore validate them.
        String email = espMembershipCommand.getEmail();
        State state = espMembershipCommand.getState();
        Long schoolId = espMembershipCommand.getSchoolId();
        String jobTitle = espMembershipCommand.getJobTitle();

        if (StringUtils.isNotBlank(email)) {
            email = email.trim();
            if (!validateEmail(email.trim())) {
                result.rejectValue("email", "invalid_email");
            }
        } else {
            result.rejectValue("email", "invalid_email");
        }

        if (state == null) {
            result.rejectValue("state", null, "State cannot be null.");
        }

        if (schoolId == null || schoolId == 0 || schoolId == -1) {
            result.rejectValue("schoolId", null, "School cannot be null.");
        }

        if (StringUtils.isBlank(jobTitle)) {
            result.rejectValue("jobTitle", null, "Job Title cannot be empty.");
        }

    }

    protected boolean validateEmail(String email) {
        org.apache.commons.validator.EmailValidator emv = org.apache.commons.validator.EmailValidator.getInstance();
        return emv.isValid(email);
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
}