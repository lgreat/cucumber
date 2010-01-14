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
            user.setEmail(command.getNewEmail());
            // save user
            user.setUpdated(new Date());
            _userDao.updateUser(user);
            PageHelper.setMemberAuthorized(request, response, user);
            message = "4F3C-46E1-82EF-126A";
            mAndV.getModel().put("msg", message);
        }

        mAndV.setViewName("redirect:/account/");
        return mAndV;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
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
