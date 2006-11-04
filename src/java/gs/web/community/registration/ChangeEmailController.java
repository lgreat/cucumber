package gs.web.community.registration;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlBuilder;
import gs.web.util.PageHelper;
import gs.web.util.validator.EmailValidator;
import gs.web.util.context.SessionContextUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;

/**
 * Provides backing for the change email form, that allows a list_member to update their
 * email address.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ChangeEmailController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/changeEmail.page";
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String NOT_MATCHING_ERROR = "Please enter the same email address into both fields";

    private IUserDao _userDao;

    protected Object formBackingObject(HttpServletRequest request) {
        return new ChangeEmailCommand();
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws java.lang.Exception {
        if (SessionContextUtil.getSessionContext(request).getUser() == null) {
            UrlBuilder emailUrl = new UrlBuilder(UrlBuilder.CHANGE_EMAIL, null, null);
            UrlBuilder loginUrl = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null,
                    emailUrl.asFullUrl(request));
            ModelAndView mAndV = new ModelAndView();
            mAndV.setViewName("redirect:" + loginUrl.asFullUrl(request));
            return mAndV;
        }
        return super.handleRequestInternal(request, response);
    }

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object objCommand,
                                     BindException errors) throws NoSuchAlgorithmException {
        ChangeEmailCommand command = (ChangeEmailCommand) objCommand;

        if (!command.getConfirmNewEmail().equals(command.getNewEmail())) {
            errors.rejectValue("newEmail", null, NOT_MATCHING_ERROR);
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object objCommand,
                                 BindException errors) throws NoSuchAlgorithmException {
        ChangeEmailCommand command = (ChangeEmailCommand) objCommand;

        User user = SessionContextUtil.getSessionContext(request).getUser();
        user.setEmail(command.getNewEmail());
        _userDao.updateUser(user);
        PageHelper.setMemberAuthorized(request, response, user);

        ModelAndView mAndV = new ModelAndView();

        mAndV.setViewName(getSuccessView());
        mAndV.getModel().put("message", "Your email has been updated to " + user.getEmail());
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
