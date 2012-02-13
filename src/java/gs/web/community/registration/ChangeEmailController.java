package gs.web.community.registration;

import gs.data.school.EspMembership;
import gs.data.school.IEspMembershipDao;
import gs.data.security.IRoleDao;
import gs.data.security.Role;
import gs.web.util.SitePrefCookie;
import gs.web.util.UrlBuilder;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.validator.EmailValidator;
import gs.web.util.context.SessionContextUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Provides backing for the change email form, that allows a list_member to update their
 * email address.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class ChangeEmailController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/changeEmail.page";
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String NOT_MATCHING_ERROR = "The two emails don't match.";
    public static final String EMAIL_TOO_LONG_ERROR = "Your email must be less than 128 characters long.";
    public static final String EMAIL_IN_USE_ERROR =
            "The email address you entered has already been registered with GreatSchools.";

    private IUserDao _userDao;
    private IRoleDao _roleDao;
    private IEspMembershipDao _espMembershipDao;
    private EmailVerificationEmail _emailVerificationEmail;
    private boolean _requireEmailValidation;

    protected Object formBackingObject(HttpServletRequest request) {
        return new ChangeEmailCommand();
    }

    protected boolean suppressValidation(HttpServletRequest request, Object obj) {
        return isCancel(request) || super.suppressValidation(request, obj);
    }

    protected boolean isCancel(HttpServletRequest request) {
        return request.getParameter("cancel.x") != null || request.getParameter("cancel") != null;
    }

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object objCommand,
                                     BindException errors) throws NoSuchAlgorithmException {
        if (suppressValidation(request, objCommand) || errors.hasErrors()) {
            return;
        }
        ChangeEmailCommand command = (ChangeEmailCommand) objCommand;

        if (command.getNewEmail().length() > 127) {
            errors.rejectValue("newEmail", null, EMAIL_TOO_LONG_ERROR);
            return; // other errors are irrelevant
        }

        User user = _userDao.findUserFromEmailIfExists(command.getNewEmail());

        if (user != null) {
            errors.rejectValue("newEmail", null, EMAIL_IN_USE_ERROR);
            return; // other errors are irrelevant
        }

        if (!command.getConfirmNewEmail().equals(command.getNewEmail())) {
            errors.rejectValue("newEmail", null, NOT_MATCHING_ERROR);
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object objCommand,
                                 BindException errors) throws NoSuchAlgorithmException {
        ModelAndView mAndV = new ModelAndView("redirect:/account/");

        if (request.getParameter("submit") != null || request.getParameter("submit.x") != null) {
            ChangeEmailCommand command = (ChangeEmailCommand) objCommand;

            User user = SessionContextUtil.getSessionContext(request).getUser();
            user.setEmail(command.getNewEmail());
            // save user
            user.setUpdated(new Date());

            if (_requireEmailValidation) {
                user.setEmailProvisional("changeEmail");
            }

            //If user is an ESP member then remove the role and set all esp memberships to inactive.
            boolean isUserEspMember =  user.hasRole(Role.ESP_MEMBER);
            if(isUserEspMember){
                Role role = _roleDao.findRoleByKey(Role.ESP_MEMBER);
                user.removeRole(role);
            }
            _userDao.updateUser(user);

            if (isUserEspMember) {
                _espMembershipDao.deactivateAllEspMembershipsForUser(user.getId());
            }

            if (_requireEmailValidation) {
                // GS-8865 If a user changes their email address and clicks save, the new email address should
                // replace the old one in the database and we should send them the [verification email
                // (changed email)]. They should also be logged out and taken to the inline sign in page
                // with the [verify change email hover] open. The user should be put back into the provisional
                // state in the db.
                PageHelper.logout(request, response);
                try {
                    getEmailVerificationEmail().sendChangedEmailAddress(request, user);
                } catch (Exception e) {
                    _log.error("Error sending email verification email to " + user.getEmail() + ": " + e, e);
                }
                SitePrefCookie cookie = new SitePrefCookie(request, response);
                cookie.setProperty("showHover", "validateEditEmail");
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null);
                mAndV.setViewName("redirect:" + urlBuilder.asSiteRelative(request));
            } else {
                PageHelper.setMemberAuthorized(request, response, user);
                String message = "4F3C-46E1-82EF-126A";
                mAndV.getModel().put("msg", message);
            }
        }
        return mAndV;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public EmailVerificationEmail getEmailVerificationEmail() {
        return _emailVerificationEmail;
    }

    public void setEmailVerificationEmail(EmailVerificationEmail emailVerificationEmail) {
        _emailVerificationEmail = emailVerificationEmail;
    }

    public boolean isRequireEmailValidation() {
        return _requireEmailValidation;
    }

    public void setRequireEmailValidation(boolean requireEmailValidation) {
        _requireEmailValidation = requireEmailValidation;
    }

    public IRoleDao getRoleDao() {
        return _roleDao;
    }

    public void setRoleDao(IRoleDao roleDao) {
        _roleDao = roleDao;
    }

    public IEspMembershipDao getEspMembershipDao() {
        return _espMembershipDao;
    }

    public void setEspMembershipDao(IEspMembershipDao espMembershipDao) {
        _espMembershipDao = espMembershipDao;
    }

    public static class ChangeEmailCommand implements EmailValidator.IEmail {
        private String _newEmail;
        private String _confirmNewEmail;

        public String getNewEmail() {
            return _newEmail;
        }

        public void setNewEmail(String newEmail) {
            _newEmail = newEmail;
        }

        public String getConfirmNewEmail() {
            return _confirmNewEmail;
        }

        public void setConfirmNewEmail(String confirmNewEmail) {
            _confirmNewEmail = confirmNewEmail;
        }

        public String getEmail() {
            return getNewEmail();
        }
    }
}
