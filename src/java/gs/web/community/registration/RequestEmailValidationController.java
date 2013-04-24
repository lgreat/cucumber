package gs.web.community.registration;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import gs.web.util.UrlBuilder;
import gs.data.community.User;
import gs.data.community.IUserDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RequestEmailValidationController extends AbstractController {
    public static final String BEAN_ID = "/community/requestEmailValidation.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private String _viewName;
    private IUserDao _userDao;
    private EmailVerificationEmail _emailVerificationEmail;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> model = new HashMap<String, String>();
        String email = request.getParameter("email");
        String emailRedirectParam = request.getParameter("emailRedirect");
        User user = _userDao.findUserFromEmailIfExists(email);
        UserCommand userCommand = new UserCommand();
        userCommand.setUser(user);

        if (user == null || user.isPasswordEmpty()) {
            // get registration form to auto fill in email
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, email);
            String href = builder.asAnchor(request, "Register here").asATag();

            model.put("message", "You are not a member yet. " + href + ".");
            _log.warn("Can't find user with email " + email);
        } else if (user.isEmailProvisional()) {
            // determine where to send the user when they click on the link in their email
            UrlBuilder builder = new UrlBuilder(UrlBuilder.USER_ACCOUNT, null);
            String emailRedirect = builder.asSiteRelative(request);

            if(StringUtils.isNotBlank(emailRedirectParam)){
                emailRedirect = emailRedirectParam;
            }
            // send email
            getEmailVerificationEmail().sendVerificationEmail(request, user, emailRedirect);
            // determine where to send the user now
            String redirect = request.getParameter("redirect");
            if (StringUtils.isBlank(redirect)) {
                // no redirect? Oh well, they probably came from the login page
                builder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, email);
                redirect = builder.asSiteRelative(request);
            }
            return new ModelAndView("redirect:" + redirect);
        } else if (user.isEmailValidated()) {
            model.put("message", "Your account has already been validated.");
        }

        return new ModelAndView(_viewName, model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
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
}