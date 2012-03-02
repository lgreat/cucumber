package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.json.JSONObject;
import gs.data.state.State;
import gs.web.util.UrlBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ForgotPasswordValidatorAjaxController implements Controller {
    private IUserDao _userDao;

    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        Map<Object, Object> returnMap = validate(request.getParameter("email"), request);
        JSONObject rval = new JSONObject(returnMap);
        response.getWriter().print(rval.toString());
        return null;
    }

    protected Map<Object, Object> validate(String email, HttpServletRequest request) {
        Map<Object, Object> rval = new HashMap<Object, Object>();
        if (ForgotPasswordValidatorHelper.emailInvalid(email)) {
            rval.put("errorMsg", "Email invalid.");
            rval.put("errorCode", "INVALID_EMAIL");
            return rval;
        }
        User user = _userDao.findUserFromEmailIfExists(email);
        if (ForgotPasswordValidatorHelper.noSuchUser(user)) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, email);
            String href = builder.asSiteRelative(request);
            String errorMsg = "There is no account associated with that email address. " +
                    "Would you like to <a href=\"" + href +
                    "\" onclick=\"GSType.hover.forgotPassword.showJoin();return false;\">join GreatSchools</a>?";
            rval.put("errorMsg", errorMsg);
            rval.put("errorCode", "NO_SUCH_ACCOUNT");
        } else if (ForgotPasswordValidatorHelper.userNoPassword(user)) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, email);
            String href = builder.asSiteRelative(request);
            String joinLink = builder.asAHref(request, "Join now <span class=\"alertDoubleArrow\">&raquo;</span>");
            String errorMsg = "Hi, " + email.split("@")[0] +
                    "! You have an email address on file, " +
                    "but still need to create a free account with GreatSchools. <a href=\"" + href +
                    "\" onclick=\"GSType.hover.forgotPassword.showJoin();return false;\"></a>" + joinLink;
            rval.put("errorMsg", errorMsg);
            rval.put("errorCode", "EMAIL_ONLY_ACCOUNT");
        } else if (ForgotPasswordValidatorHelper.userDeactivated(user)) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.CONTACT_US, State.CA, null);
            String href = builder.asAnchor(request, "contact us").asATag();
            String errorMsg = "The account associated with that email address has been disabled. " +
                    "Please " + href + " for more information.";
            rval.put("errorMsg", errorMsg);
            rval.put("errorCode","DISABLED_ACCOUNT");
        }

        if (rval.containsKey("errorMsg") || rval.containsKey("errorCode")) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.ESP_REGISTRATION);
            String href = builder.asSiteRelative(request);
            rval.put("ESP_REGISTRATION_URL", href);
        }

        return rval;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
