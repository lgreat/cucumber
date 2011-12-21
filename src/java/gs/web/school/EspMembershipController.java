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
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.ReadWriteAnnotationController;
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
public class EspMembershipController implements ReadWriteAnnotationController {
    private static final Log _log = LogFactory.getLog(EspMembershipController.class);

    public static final String FORM_VIEW = "school/espMembershipForm";
    public static final String SUCCESS_VIEW = "school/espMembershipSuccess";

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private IUserDao _userDao;

    @RequestMapping(value = "form.page", method = RequestMethod.GET)
    public String showForm(ModelMap modelMap) {
        EspMembershipCommand command = new EspMembershipCommand();
        modelMap.addAttribute("schoolEspCommand", command);
        return FORM_VIEW;
    }

    @RequestMapping(value = "form.page", method = RequestMethod.POST)
    public String createEspMembership(@ModelAttribute("schoolEspCommand") EspMembershipCommand command,
                                      BindingResult result,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        if (StringUtils.isNotBlank(command.getEmail())) {
            try {
                String email = command.getEmail().trim();
                User user = getUserDao().findUserFromEmailIfExists(email);
                //TODO: cookie based omniture?
//                OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
                boolean userExists = false;

                //If user already exists.
                if (user != null) {
                    userExists = true;
                    setFieldsOnUserUsingCommand(command, user);

                } else {
                    //If no user already exists create a new one.
                    user = new User();
                    user.setEmail(email);
                    //TODO .Is this right?
                    user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                    setFieldsOnUserUsingCommand(command, user);
                    getUserDao().saveUser(user);
                    //TODO :?
                    ThreadLocalTransactionManager.commitOrRollback();
                }

                //Set the users password and save the user.
                setUsersPassword(command, user);
                getUserDao().updateUser(user);
//                ThreadLocalTransactionManager.commitOrRollback();

                //Set the users profile and save the user.
                updateUserProfile(command, user);
                getUserDao().updateUser(user);

                // TODO: do some cookie  and welcome/verification email send.

                //Save ESP membership for user.
                saveEspMembership(command, user);

            } catch (Exception exception) {
                _log.debug(exception);
                throw exception;
            }
            return SUCCESS_VIEW;
        }
        return FORM_VIEW;
    }

    //TODO write unit tests for this. and this should validate email also.
    @RequestMapping(value = "checkEspUser.page", method = RequestMethod.GET)
    public void checkIfUserExists(HttpServletRequest request, HttpServletResponse response, EspMembershipCommand command) {
        String email = command.getEmail();
        String fieldsToCollect = "";
        boolean isUserESPMember = false;
        boolean isUserMember = false;
        boolean isEmailValid = true;

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
                            fieldsToCollect += fieldsToCollect.length() > 0 ? ",userName" : "userName";
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


    protected void setFieldsOnUserUsingCommand(EspMembershipCommand espMembershipCommand, User user) {
        //TODO validate before setting?
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
            //Only set  "how" if it is not already set.
            if (StringUtils.isBlank(user.getHow())) {
                user.setHow("esp");
            }
        }
    }

    protected void setUsersPassword(EspMembershipCommand espMembershipCommand, User user) throws Exception {
        //We accept just spaces as password.Therefore do NOT use : isBlank, use : isEmpty and do NOT trim().
        try {
            if (StringUtils.isNotEmpty(espMembershipCommand.getPassword())) {
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
            if (StringUtils.isNotBlank(espMembershipCommand.getUserName()) && StringUtils.isBlank(userProfile.getScreenName())) {
                userProfile.setScreenName(espMembershipCommand.getUserName().trim());
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

    //TODO write unit tests for this.
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