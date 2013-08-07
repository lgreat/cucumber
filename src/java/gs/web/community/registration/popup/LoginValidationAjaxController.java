package gs.web.community.registration.popup;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.json.JSONObject;
import gs.web.community.registration.AuthenticationManager;
import gs.web.community.registration.LoginCommand;
import gs.web.community.registration.LoginController;
import gs.web.community.registration.LoginValidatorHelper;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.validator.EmailValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.mvc.BaseCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Samson Sprouse <mailto:ssprouse@greatschools.org>
 */
public class LoginValidationAjaxController extends AbstractCommandController {

    protected final Log _log = LogFactory.getLog(getClass());

    public static final String BEAN_ID = "/community/registration/popup/loginValidationAjax.page";
    private IUserDao _userDao;
    private AuthenticationManager _authenticationManager;
    private boolean _requireEmailValidation = true;

    public Map<Object,Object> validateLoginForm(HttpServletRequest request,
                                  LoginHoverCommand loginCommand) throws Exception {

       Map<Object,Object> errors = new HashMap<Object,Object>();

       org.apache.commons.validator.EmailValidator emv = org.apache.commons.validator.EmailValidator.getInstance();

        if (!emv.isValid(loginCommand.getEmail())) {
            errors.put("email", "Please enter a valid email address.");
            return errors;
        }

        User user = getUserDao().findUserFromEmailIfExists(loginCommand.getEmail());
        boolean isMslSubscriber = false;
        if (user != null) {
            isMslSubscriber = (user.getFavoriteSchools() != null && !user.getFavoriteSchools().isEmpty());
        }

       if (LoginValidatorHelper.noSuchUser(user, _requireEmailValidation)) {
            errors.put("noSuchUser",
                    "There is no account associated with that email address.");
            _log.info("Community login: user " + loginCommand.getEmail() + " is not in database");
       } else if (user != null && user.isFacebookUser()) {
           errors.put("facebookUser", "This email is linked to a Facebook account. Please log in using Facebook.");
        } else if (LoginValidatorHelper.userNotValidated(user, _requireEmailValidation)) {
            errors.put("userNotValidated","Your email address has not been validated");
            _log.info("Community login: user " + loginCommand.getEmail() + " has not validated their email.");
        } else if (LoginValidatorHelper.userNoPassword(user)) {
          
            errors.put("userNoPassword","Hi, " + user.getEmail().split("@")[0] +
                    "! You've already subscribed to My School List, but still need to create an account with GreatSchools.");

            _log.info("Community login: non-community user " + loginCommand.getEmail() + " MSL subscriber? " + isMslSubscriber);
        } else if (LoginValidatorHelper.userDeactivated(user)) {

            String errmsg = "The account associated with that email address has been disabled. " +
                    "Please <a href=\"http://" +
                    SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                    "/report/email-moderator\">contact us</a> for more information.";
             
            errors.put("userDeactivated", errmsg);
            _log.info("Community login: disabled community user " + loginCommand.getEmail() + " MSL subscriber? " + isMslSubscriber);
        } else if (LoginValidatorHelper.passwordMismatch(user, loginCommand.getPassword())) {
            errors.put("passwordMismatch", "The password you entered is incorrect.");
        } else {
            _log.info("Community login: community user " + loginCommand.getEmail() + " MSL subscriber? " + isMslSubscriber);
        }

        return errors;
    }

    public ModelAndView handle(HttpServletRequest request,
                                 HttpServletResponse response, Object command, BindException errors
                                 ) throws Exception {

        LoginHoverCommand loginCommand = (LoginHoverCommand) command;

        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("application/json");

        Map<Object,Object> mapErrors = validateLoginForm(request,loginCommand);

        String jsonString = new JSONObject(mapErrors).toString();

        _log.info("Writing JSON response -" + jsonString);

        response.getWriter().write(jsonString);

        response.getWriter().flush();
        /*
        if (request.getParameter("joinForm") != null) {
            mAndV.setViewName("redirect:/community/registration/popup/registrationHover.page?email=" +
                    URLEncoder.encode(email, "UTF-8") + "&how=" + loginCommand.getHow());
            return mAndV;
        }
        */

        /*
        User user = getUserDao().findUserFromEmail(email);

        String redirectUrl;
        if (user.isPasswordEmpty()) {
            // Log the user in to MSL
            PageHelper.setMemberCookie(request, response, user);
            // But they don't have a community password, so send them to the registration page
            redirectUrl = "/community/registration/popup/registrationHover.page?email=" +
                    URLEncoder.encode(email, "UTF-8") + "&msl=1&how=" + loginCommand.getHow();
        } else {
            // The password has validated, so set the cookies and send them onward
            PageHelper.setMemberAuthorized(request, response, user, loginCommand.isRememberMe());

            redirectUrl = "/community/registration/popup/sendToDestination.page";
        }

        mAndV.setViewName("redirect:" + redirectUrl);
        return mAndV;
        */
        return null;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        _authenticationManager = authenticationManager;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public AuthenticationManager getAuthenticationManager() {
        return _authenticationManager;
    }

    public boolean isRequireEmailValidation() {
        return _requireEmailValidation;
    }

    public void setRequireEmailValidation(boolean requireEmailValidation) {
        _requireEmailValidation = requireEmailValidation;
    }
}