package gs.web.community.registration;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlrpc.client.TimingOutCallback;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcClient;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.validator.EmailValidator;
import gs.web.util.context.SessionContextUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Provides backing for the change email form, that allows a list_member to update their
 * email address.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ChangeEmailController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/changeEmail.page";
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String NOT_MATCHING_ERROR = "Please enter the same email address into both fields.";

    private IUserDao _userDao;
    private String _rpcServerUrl;
    private int _timeOutMs;

    protected Object formBackingObject(HttpServletRequest request) {
        return new ChangeEmailCommand();
    }

    protected boolean suppressValidation(HttpServletRequest request) {
        return isCancel(request);
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
        if (suppressValidation(request) || errors.hasErrors()) {
            return;
        }
        ChangeEmailCommand command = (ChangeEmailCommand) objCommand;

        if (command.getNewEmail().length() > 127) {
            errors.rejectValue("newEmail", null,
                    "We're sorry, your email must be less than 128 characters long.");
            return; // other errors are irrelevant
        }

        User user = _userDao.findUserFromEmailIfExists(command.getNewEmail());

        if (user != null) {
            errors.rejectValue("newEmail", null,
                    "We're sorry, that email address is already in use.");
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

            User user = SessionContextUtil.getSessionContext(request).getUser();
            user.setEmail(command.getNewEmail());
            _userDao.updateUser(user);
            PageHelper.setMemberAuthorized(request, response, user);
            notifyCommunity(user);
            mAndV.getModel().put("message", "Your email has been updated to " + user.getEmail());
        }

        mAndV.setViewName(getSuccessView());
        return mAndV;
    }

    protected String notifyCommunity(User user) {
        String email = null;
        //times out after _timeOutMs milliseconds
        TimingOutCallback callback = new TimingOutCallback(_timeOutMs);
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try {
            config.setServerURL(new URL(getRpcServerUrl()));
            config.setEnabledForExtensions(false);
        } catch (MalformedURLException e) {
            _log.error("Error notifying community of changed email address for user " + user +
                    ", error=" + e);
        }

        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);

        Object[] params = new Object[]{
                user.getId(), // id to identify user
                user.getEmail() // new email address to update user with
        };

        String result;

        try {
            client.executeAsync("gsUpdateEmail", params, callback);
            result = (String) callback.waitForResponse();
            _log.info("gsUpdateEmail result: " + result);
            if (StringUtils.isBlank(result)) {
                _log.error("Error notifying community of changed email address for user " + user +
                        ", error=empty response");
            } else if (result.startsWith("error") && !result.startsWith("error: user not found")) {
                _log.error("Error notifying community of changed email address for user " + user +
                        ", error=" + result);
            } else if (result.startsWith("error: user not found")) {
                // we don't care if the user exists on their end or not
                email = user.getEmail(); // fake a success here
            } else {
                email = result;
            }
        } catch (Throwable t) {
            _log.error("Error notifying community of changed email address for user " + user +
                    ", error=" + t);
        }
        return email;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public String getRpcServerUrl() {
        return _rpcServerUrl;
    }

    public void setRpcServerUrl(String rpcServerUrl) {
        _rpcServerUrl = rpcServerUrl;
    }

    public int getTimeOutMs() {
        return _timeOutMs;
    }

    public void setTimeOutMs(int timeOutMs) {
        _timeOutMs = timeOutMs;
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
