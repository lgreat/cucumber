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
import java.security.NoSuchAlgorithmException;
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

    @RequestMapping(method = RequestMethod.POST)
    public String createEspMembership(@ModelAttribute("schoolEspCommand") EspMembershipCommand command,
                                      BindingResult result,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        if (StringUtils.isNotBlank(command.getEmail())) {
            try {
                User user = getUserDao().findUserFromEmailIfExists(command.getEmail().trim());
                //TODO: cookie based omniture?
//            OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
                boolean userExists = false;

                if (user != null) {
                    userExists = true;
                    setFieldsOnUserUsingCommand(command, user);

                } else {
                    user = new User();
                    user.setEmail(command.getEmail());
                    //TODO .Is this right?
                    user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                    setFieldsOnUserUsingCommand(command, user);
                    getUserDao().saveUser(user);
                    //TODO :?
                    ThreadLocalTransactionManager.commitOrRollback();
                }

                setUsersPassword(command, user);
                //TODO :?
                ThreadLocalTransactionManager.commitOrRollback();
                getUserDao().updateUser(user);
                updateUserProfile(command, user);
                //TODO is this needed twice?
                getUserDao().updateUser(user);

                // TODO: do some cookie  and email logic

                //TODO:Should I defensively check the ESP_membership just in case?
                //TODO: What if there is an error saving/updating the user?
                saveEspMembership(command, user);

            } catch (Exception exception) {
                _log.debug(exception);
                return FORM_VIEW;
            }
            return SUCCESS_VIEW;
        }


        return FORM_VIEW;
    }

    //TODO write unit tests for this.
    @RequestMapping(value = "checkEspUser.page", method = RequestMethod.GET)
    public void checkIfUserExists(HttpServletRequest request, HttpServletResponse response, EspMembershipCommand command) {
        String email = command.getEmail().trim();
        if (!StringUtils.isBlank(email)) {
            User user = getUserDao().findUserFromEmailIfExists(email);
            String fieldsToCollect = "";
            boolean userAlreadyESPMember = false;
            if (user != null) {
                //Check if user is already an ESP member.TODO maybe just check the role?
                EspMembership membership = getEspMembershipDao().findEspMembershipByUserId(new Long(user.getId()));

                if (membership != null) {
                    //TODO If already an ESP member maybe check if all the required espmembership fields are present?
                    userAlreadyESPMember = true;
                } else {
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
                    }
                }
            }

            try {
                JSONObject rval;
                Map data = new HashMap();
                if (fieldsToCollect.length() > 0) {
                    data.put("fieldsToCollect", fieldsToCollect);
                } else if (userAlreadyESPMember) {
                    data.put("userAlreadyESPMember", true);
                } else {
                    data.put("userNotFound", true);
                }
                rval = new JSONObject(data);
                response.setContentType("application/json");
                response.getWriter().print(rval.toString());
                response.getWriter().flush();
            } catch (Exception exp) {
                _log.error("Error " + exp, exp);
                //TODO return an error page?
            }
        }
    }


    protected void setFieldsOnUserUsingCommand(EspMembershipCommand espMembershipCommand, User user) {
        //TODO validate before setting?
        //TODO also should I check if the fields are set in the DB.It may not happen but in case it does, do not want to overwrite.
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
        //Only set  "how" if it is not already set.
        if (StringUtils.isBlank(user.getHow())) {
            user.setHow("esp");
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
            //TODO : uncomment the code below?
//            if (!userExists) {
//                // for new users, cancel the account on error
//                _userDao.removeUser(user.getId());
//            }
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
        //TODO there is a "how" in user profile also?
        //TODO set numschools to 0?
        user.getUserProfile().setUpdated(new Date());
    }

    protected void setUserProfileFieldsFromCommand(EspMembershipCommand espMembershipCommand, UserProfile userProfile) {
        //TODO check in the database before overwriting them just in case?

        if (StringUtils.isNotBlank(espMembershipCommand.getUserName())) {
            userProfile.setScreenName(espMembershipCommand.getUserName().trim());
        }

        if (StringUtils.isNotBlank(espMembershipCommand.getCity())) {
            userProfile.setCity(espMembershipCommand.getCity().trim());
        }

        if (espMembershipCommand.getState() != null) {
            userProfile.setState(espMembershipCommand.getState());
        }
    }

    //TODO write unit tests for this.
    protected void saveEspMembership(EspMembershipCommand command, User user) {
        EspMembership esp = new EspMembership();
        esp.setIsActive(false);
        esp.setJobTitle(command.getJobTitle());
        esp.setState(command.getState());
        esp.setSchoolId(command.getSchoolId());
        esp.setStatus(EspMembershipStatus.PROCESSING);
        esp.setUser(user);
        esp.setWebUrl(command.getWebPageUrl());
        getEspMembershipDao().saveEspMembership(esp);
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