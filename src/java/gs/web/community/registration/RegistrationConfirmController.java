package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.WelcomeMessageStatus;
import gs.data.util.DigestUtil;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Final stage in the confirmation process when using email validation.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RegistrationConfirmController extends AbstractController implements ReadWriteController {
    public static final String BEAN_ID = "/community/registrationConfirm.page";
    public static final Long EXPIRE_DURATION_IN_MILLIS = 5L * 24L * 60L * 60L * 1000L; // 5 days in milliseconds
    protected final Log _log = LogFactory.getLog(getClass());

    private String _viewName;
    private IUserDao _userDao;

    protected ModelAndView redirectToRegistration(HttpServletRequest request) {
        return redirectToRegistration(request, null);
    }

    protected ModelAndView redirectToRegistration(HttpServletRequest request, String email) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, email);
        String redirectUrl = builder.asFullUrl(request);
        ModelAndView mAndV = new ModelAndView();
        mAndV.setViewName("redirect:" + redirectUrl);
        return mAndV;
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws
                                                                                                           NoSuchAlgorithmException,
                                                                                                           UnsupportedEncodingException {
        String idString = request.getParameter("id");
        if (idString == null) {
            _log.warn("Community registration request with no id, redirecting to registration");
            return redirectToRegistration(request);
        }
        if (idString.length() <= DigestUtil.MD5_HASH_LENGTH) {
            _log.warn("Community registration request with badly formed idString: " + idString + 
                    ", redirecting to registration. Expecting hash of length " +
                    DigestUtil.MD5_HASH_LENGTH + " followed by id.");
            return redirectToRegistration(request);
        }
        String hash = idString.substring(0, DigestUtil.MD5_HASH_LENGTH);
        int id = Integer.parseInt(idString.substring(DigestUtil.MD5_HASH_LENGTH));

        User user;
        try {
            user = getUserDao().findUserFromId(id);
        } catch (ObjectRetrievalFailureException orfe) {
            _log.warn("Community registration request for unknown user id: " + id + ", redirecting to registration");
            return redirectToRegistration(request);
        }
        // now confirm hash
        String actualHash = DigestUtil.hashStringInt(user.getEmail(), id);
        String dateSentAsString = request.getParameter("date");
        actualHash = DigestUtil.hashString(actualHash + dateSentAsString);
        if (!hash.equals(actualHash)) {
            _log.warn("Community registration request has invalid hash: " + hash + " for user " +
                    user.getEmail() + ", redirecting to registration");
            return redirectToRegistration(request, user.getEmail());
        }

        // make sure dateSent is less than X days ago (GS-8865)
        Date now = new Date();
        if (now.getTime() - Long.valueOf(dateSentAsString) > EXPIRE_DURATION_IN_MILLIS) {
            // If a user clicks on an expired link, they should be taken to the GS homepage
            // with the [verification link expired hover].
            _log.info("Validation link expired for " + user.getEmail());
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.HOME, null);
            String redirect = urlBuilder.asSiteRelative(request);
            if (UrlUtil.isDeveloperWorkstation(request.getServerName())) {
                redirect += "index.page";
            }

            String email = URLEncoder.encode(user.getEmail(), "UTF-8");
            email = email.replace("+", "%2B");
            return new ModelAndView("redirect:" + redirect +
                    "?showValidationLinkExpiredHover=true&email=" + email);
        }

        if (user.isPasswordEmpty()) {
            // request for new password has expired, redirect to registration
            _log.warn("Community registration request has expired for user " +
                    user.getEmail() + ", redirecting to registration");
            return redirectToRegistration(request, user.getEmail());
        }
        // authenticate user
        if (!user.isEmailValidated()) {
            user.setEmailValidated();
            // per GS-8290 All users who complete registration should get a welcome message
            // but only users who haven't already been sent one
            if (user.getWelcomeMessageStatus().equals(WelcomeMessageStatus.DO_NOT_SEND)) {
                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEED_TO_SEND);
            }
            _userDao.saveUser(user);
            PageHelper.setMemberAuthorized(request, response, user); // auto-log in to community

            String target;
            if (request.getParameter("edit") != null) {
                // when editing their email address, always send them back to the change email page
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.CHANGE_EMAIL, null, "");
                urlBuilder.addParameter("showEditEmailValidatedHover", "true");
                target = "redirect:" + urlBuilder.asSiteRelative(request);
            } else {
                // when registering, send them to where they were before, or to the /account/ page
                if (StringUtils.isNotBlank(request.getParameter("redirect"))) {
                    target = "redirect:" + UrlUtil.addParameter(request.getParameter("redirect"), "showEmailValidatedHover=true");
                } else {
                    target = "redirect:/account/?showEmailValidatedHover=true";
                }

            }
            OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
            ot.addSuccessEvent(OmnitureTracking.SuccessEvent.EmailVerified);
            _log.info("Email confirmed, forwarding user to " + target);
            return new ModelAndView(target);
        }

        return redirectToRegistration(request, user.getEmail());
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}