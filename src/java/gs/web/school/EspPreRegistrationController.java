package gs.web.school;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.school.EspMembership;
import gs.data.school.EspMembershipStatus;
import gs.data.school.IEspMembershipDao;
import gs.data.security.IRoleDao;
import gs.data.security.Role;
import gs.data.util.DigestUtil;
import gs.web.community.registration.UserCommand;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;
import gs.web.util.validator.UserCommandValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

/**
 * @author aroy@greatschools.org
 */
@Controller
@RequestMapping("/official-school-profile/")
public class EspPreRegistrationController implements ReadWriteAnnotationController {
    public static final String PATH_TO_FORM = "/official-school-profile/preRegister.page"; // used by UrlBuilder
    private static final Log _log = LogFactory.getLog(EspPreRegistrationController.class);

    public static final String VIEW = "school/espPreRegistration";

    public static final String PARAM_ID = "id";
    public static final String MODEL_MEMBERSHIP = "membership";
    public static final String MODEL_USER = "user";

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private IUserDao _userDao;

    @Autowired
    protected IRoleDao _roleDao;

    @RequestMapping(value = "preRegister.page", method = RequestMethod.GET)
    public String showForm(ModelMap modelMap, HttpServletRequest request) {
        String hashPlusId = request.getParameter(PARAM_ID);
        if (StringUtils.isBlank(hashPlusId)) {
            _log.error("No id parameter");
            return redirectToRegistration(request);
        }

        // validate hashPlusId (see EmailVerificationLinkValidator)
        // this should also give us a User object
        // Note we're ignoring cookies here, we only care about who the user encoded in the link is
        User user = getValidUserFromHash(hashPlusId);
        if (user == null) {
            // error logging handled by getValidUserFromHash method
            return redirectToRegistration(request);
        }
        // fetch memberships for user
        List<EspMembership> memberships = _espMembershipDao.findEspMembershipsByUserId(user.getId(), false);
        // error check
        if (memberships == null || memberships.size() == 0) {
            _log.error("No memberships found for user " + user);
            return redirectToRegistration(request);
        }
        // pull the membership in status PRE_APPROVED
        boolean hasActiveMembership = false;
        EspMembership membershipToProcess = null;
        for (EspMembership membership : memberships) {
            if (membership.getStatus() == EspMembershipStatus.PRE_APPROVED) {
                membershipToProcess = membership;
            } else if (membership.getActive()) {
                hasActiveMembership = true;
            }
        }
        if (membershipToProcess == null) {
            _log.error("No pre_approved memberships found for user " + user);
            return redirectToRegistration(request);
        } else if (hasActiveMembership) {
            // What to do here? For now we only allow one active membership per user, so error
            _log.error("Already found active membership for user " + user);
            return redirectToRegistration(request);
        }

        // ok member passed authentication and has a pre-approved membership, let's show them the form!
        modelMap.put(MODEL_MEMBERSHIP, membershipToProcess);
        modelMap.put(MODEL_USER, user);
        EspRegistrationCommand command = new EspRegistrationCommand();
        modelMap.addAttribute("espRegistrationCommand", command);

        return VIEW;
    }

    @RequestMapping(value = "preRegister.page", method = RequestMethod.POST)
    public String processForm(@ModelAttribute("espRegistrationCommand") EspRegistrationCommand command,
                              BindingResult result,
                              HttpServletRequest request,
                              HttpServletResponse response) throws Exception {
        String hashPlusId = request.getParameter(PARAM_ID);
        User user = getValidUserFromHash(hashPlusId);
        if (user == null) {
            // error logging handled by getValidUserFromHash method
            return redirectToRegistration(request);
        }

        //server side validation for the fields.
        validate(command, result, user);
        if (result.hasErrors()) {
            return VIEW;
        }

        setUserInfo(command, user);
        _userDao.updateUser(user);
        ThreadLocalTransactionManager.commitOrRollback();

        //Set the user's password.
        setUsersPassword(command, user);

        //add the esp_member role to the user
        Role role = _roleDao.findRoleByKey(Role.ESP_MEMBER);
        user.addRole(role);

        _userDao.updateUser(user);
        ThreadLocalTransactionManager.commitOrRollback();
        user = getValidUserFromHash(hashPlusId);

        //Set the user's profile and save the user.
        updateUserProfile(command, user);
        _userDao.updateUser(user);

        //update ESP membership for user.
        updateEspMembership(command, user);

        //Sign the user in and re-direct to the dashboard
        PageHelper.setMemberAuthorized(request, response, user, true);
        return redirectToEspDashboard(request);
    }

    protected User getValidUserFromHash(String hashPlusUserId) {
        User user = null;
        Integer userId = getUserId(hashPlusUserId);
        if (hashPlusUserId == null) {
            _log.error("Cannot verify email with null hashPlusUserId");
        } else if (hashPlusUserId.length() <= DigestUtil.MD5_HASH_LENGTH || userId == null) {
            _log.warn("Email verification request with badly formed hashPlusUserId: " + hashPlusUserId +
                    "Expecting hash of length " + DigestUtil.MD5_HASH_LENGTH + " followed by userId.");
        } else {
            String hash = getHash(hashPlusUserId);
            try {
                user = _userDao.findUserFromId(userId);
                if (!verificationHashMatchesUser(user, hash)) {
                    _log.error("Verification link hash " + hashPlusUserId + " does not match user: " + user);
                }
            } catch (ObjectRetrievalFailureException orfe) {
                _log.warn("Community registration request for unknown user id: " + userId);
            }
        }

        return user;
    }

    public boolean verificationHashMatchesUser(User user, String hash) {
        boolean validHash = false;
        String actualHash = null;
        try {
            if (user.getId() != null && user.getEmail() != null) {
                actualHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            }
            validHash = (hash != null && actualHash != null && hash.equals(actualHash));
            if (!validHash) {
                _log.warn("OSP Pre-registration request has invalid hash: " + hash + " for user " + user.getEmail());
//                _log.error("TEMPORARILY RETURNING TRUE DURING DEVELOPMENT. DO NOT CHECK IN");
//                return true;
            }
        } catch (NoSuchAlgorithmException e) {
            _log.warn("Failed to hash string: " + e, e);
            //Nothing can be done
        }
        return validHash;
    }

    public String getHash(String hashPlusUserId) {
        if (hashPlusUserId == null || hashPlusUserId.length() <= DigestUtil.MD5_HASH_LENGTH) {
            return null;
        }

        return hashPlusUserId.substring(0, DigestUtil.MD5_HASH_LENGTH);
    }

    public Integer getUserId(String hashPlusUserId) {
        if (hashPlusUserId == null || hashPlusUserId.length() <= DigestUtil.MD5_HASH_LENGTH) {
            return null;
        }

        try {
            return Integer.parseInt(hashPlusUserId.substring(DigestUtil.MD5_HASH_LENGTH));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected void validate(EspRegistrationCommand command, BindingResult result, User user) {
        UserCommandValidator validator = new UserCommandValidator();
        validator.setUserDao(_userDao);
        UserCommand userCommand = new UserCommand();

        userCommand.setFirstName(command.getFirstName());
        userCommand.setLastName(command.getLastName());
        userCommand.setPassword(command.getPassword());
        userCommand.setConfirmPassword(command.getConfirmPassword());
        userCommand.setScreenName(command.getScreenName());

        validator.validateFirstName(userCommand, result);
        validator.validateLastName(userCommand, result);
        validator.validatePassword(userCommand, result);
        validator.validateUsername(userCommand, user, result);

        String email = command.getEmail();
        if (StringUtils.isNotBlank(email)) {
            email = email.trim();
            if (!user.getEmail().equals(email)) {
                result.rejectValue("email", "Email address in the hash does not match the email param in the POST.");
            } else if (!validateEmail(email.trim())) {
                result.rejectValue("email", "invalid_email");
            }
        } else {
            result.rejectValue("email", "invalid_email");
        }

        String jobTitle = command.getJobTitle();
        if (StringUtils.isBlank(jobTitle)) {
            result.rejectValue("jobTitle", null, "Job Title cannot be empty.");
        }
    }

    //TODO move this to a util
    protected boolean validateEmail(String email) {
        org.apache.commons.validator.EmailValidator emv = org.apache.commons.validator.EmailValidator.getInstance();
        return emv.isValid(email);
    }

    protected void setUserInfo(EspRegistrationCommand command, User user) {
        if (user != null) {
            if (StringUtils.isNotBlank(command.getFirstName())) {
                user.setFirstName(command.getFirstName().trim());
            }
            if (StringUtils.isNotBlank(command.getLastName())) {
                user.setLastName(command.getLastName().trim());
            }
            //default gender.
            if (StringUtils.isBlank(user.getGender())) {
                user.setGender("u");
            }
        }
    }

    protected void setUsersPassword(EspRegistrationCommand command, User user) throws Exception {
        //NOTE :We accept just spaces as password.Therefore do NOT use : isBlank, use : isEmpty and do NOT trim().
        try {
            if (StringUtils.isNotEmpty(command.getPassword()) && !user.isEmailValidated()) {
                user.setPlaintextPassword(command.getPassword());
                user.setEmailVerified(true);
            }
        } catch (Exception e) {
            _log.warn("Error setting password: " + e.getMessage(), e);
            throw e;
        }
    }

    protected void updateUserProfile(EspRegistrationCommand command, User user) {
        UserProfile userProfile;

        if (user.getUserProfile() != null && user.getUserProfile().getId() != null) {
            userProfile = user.getUserProfile();
            setUserProfileFieldsFromCommand(command, userProfile);

        } else {
            userProfile = new UserProfile();
            setUserProfileFieldsFromCommand(command, userProfile);
            userProfile.setUser(user);
            user.setUserProfile(userProfile);
        }
    }

    protected void setUserProfileFieldsFromCommand(EspRegistrationCommand command, UserProfile userProfile) {
        if (userProfile != null) {
            if (StringUtils.isNotBlank(command.getScreenName())) {
                userProfile.setScreenName(command.getScreenName().trim());
            }
            //TODO update the state and city?

            if (StringUtils.isBlank(userProfile.getHow())) {
                userProfile.setHow("esp");
            }
            userProfile.setUpdated(new Date());
        }
    }

    protected void updateEspMembership(EspRegistrationCommand command, User user) {

        List<EspMembership> espMemberships = _espMembershipDao.findEspMembershipsByUserId(user.getId(), false);
        //TODO for all the pre-approved?
        for (EspMembership espMembership : espMemberships) {
            if (espMembership.getStatus().equals(EspMembershipStatus.PRE_APPROVED)) {
                if (StringUtils.isNotBlank(command.getJobTitle())) {
                    espMembership.setJobTitle(command.getJobTitle());
                }
                espMembership.setStatus(EspMembershipStatus.APPROVED);
                espMembership.setActive(true);
                espMembership.setUpdated(new Date());
                _espMembershipDao.saveEspMembership(espMembership);
            }
        }
    }

    protected String redirectToRegistration(HttpServletRequest request) {
        UrlBuilder ospReg = new UrlBuilder(UrlBuilder.ESP_REGISTRATION);
        return "redirect:" + ospReg.asSiteRelative(request);
    }

    protected String redirectToEspDashboard(HttpServletRequest request) {
        UrlBuilder ospReg = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
        return "redirect:" + ospReg.asSiteRelative(request);
    }
}
