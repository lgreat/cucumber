package gs.web.community.registration;

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
import gs.data.soap.SoapRequestException;
import gs.web.soap.ChangeEmailRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Provides backing for the change email form, that allows a list_member to update their
 * email address.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ChangeEmailController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/changeEmail.page";
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String NOT_MATCHING_ERROR = "The two emails don't match.";
    public static final String EMAIL_TOO_LONG_ERROR = "Your email must be less than 128 characters long.";
    public static final String EMAIL_IN_USE_ERROR =
            "The email address you entered has already been registered with GreatSchools.";

    private IUserDao _userDao;
    private ChangeEmailRequest _soapRequest;

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
        ModelAndView mAndV = new ModelAndView();

        if (request.getParameter("submit") != null || request.getParameter("submit.x") != null) {
            ChangeEmailCommand command = (ChangeEmailCommand) objCommand;
            String message;

            User user = SessionContextUtil.getSessionContext(request).getUser();
            String oldEmail = user.getEmail();
            user.setEmail(command.getNewEmail());
            if (notifyCommunity(user)) {
                // success
                // save user
                user.setUpdated(new Date());
                _userDao.updateUser(user);
                PageHelper.setMemberAuthorized(request, response, user);
                message = "Your email has been updated to: " + user.getEmail();
            } else {
                // failure
                user.setEmail(oldEmail);
                message = "We're sorry! There was an error updating your email. " +
                        "Please try again in a few minutes.";
            }
            mAndV.getModel().put("message", message);
        }

        mAndV.setViewName(getSuccessView());
        return mAndV;
    }

    /**
     * Fires off a SOAP request to community updating the email address. If there is an error,
     * this method returns FALSE, otherwise TRUE.
     * @param user User with updated email address
     * @return TRUE if successful, FALSE Otherwise
     */
    protected boolean notifyCommunity(User user) {
        ChangeEmailRequest soapRequest = getSoapRequest();
        try {
            soapRequest.changeEmailRequest(user);
        } catch (SoapRequestException couure) {
            _log.error("SOAP error - " + couure.getErrorCode() + ": " + couure.getErrorMessage());
            // send to error page
            return false;
        }

        return true;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    /**
     * Encapsulate into method so the testing class can mock it
     */
    public ChangeEmailRequest getSoapRequest() {
        if (_soapRequest == null) {
            _soapRequest = new ChangeEmailRequest();
        }
        return _soapRequest;
    }

    public void setSoapRequest(ChangeEmailRequest soapRequest) {
        _soapRequest = soapRequest;
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
