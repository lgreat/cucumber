package gs.web.promo;

import gs.data.community.*;
import gs.web.community.registration.EmailVerificationEmail;
import gs.web.tracking.JsonBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Young Fan <mailto:yfan@greatschools.org>
 */
@org.springframework.stereotype.Controller
public class EmailSignUpAjaxController implements ReadWriteAnnotationController {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String STATUS_ERROR = "Error";
    public static final String STATUS_SUCCESS_EMAIL_SENT = "OK-emailSent";
    public static final String STATUS_SUCCESS_NO_EMAIL_SENT = "OK-noEmailSent";

private EmailVerificationEmail _emailVerificationEmail;

    private ISubscriptionDao _subscriptionDao;
    private IUserDao _userDao;
    private static final SubscriptionProduct PARENT_ADVISOR = SubscriptionProduct.PARENT_ADVISOR;
    private static final SubscriptionProduct PARENT_ADVISOR_NOT_VERIFIED = SubscriptionProduct.PARENT_ADVISOR_NOT_VERIFIED;

    @RequestMapping(value = "/promo/emailSignUpAjax.page", method = RequestMethod.POST)
    public void handleEmailSignUp(@ModelAttribute("command") EmailSignUpCommand command,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {
        // user/subscription info needed for validation and business logic
        User user = (StringUtils.isNotBlank(command.getEmail()) ? _userDao.findUserFromEmailIfExists(command.getEmail()) : null);
        boolean subscribedToProd = (user != null) && _subscriptionDao.isUserSubscribed(user, PARENT_ADVISOR, null);
        boolean subscribedToProdNotVerified = (user != null) && _subscriptionDao.isUserSubscribed(user, PARENT_ADVISOR_NOT_VERIFIED, null);

        // variables needed for json response
        String errors = validate(command, (user != null), subscribedToProd);
        String status;
        JsonBasedOmnitureTracking omnitureTracking = null;

        if (StringUtils.isBlank(errors)) {
            boolean isNewMember = false;
            boolean sendVerificationEmail = false;

            // If the user does not yet exist, add to list_member
            if (user == null) {
                user = new User();
                user.setEmail(command.getEmail());
                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                _userDao.saveUser(user);
                isNewMember = true;
                sendVerificationEmail = true;
            }

            // add subscription - use PARENT_ADVISOR_NOT_VERIFIED instead of PARENT_ADVISOR
            // if we just created the user. this will let us keep track of that fact that they
            // want to be subscribed to PARENT_ADVISOR but not actually subscribe them until
            // they've verified their email
            if (!subscribedToProd && !subscribedToProdNotVerified) {
                // save new subscription
                Subscription sub = new Subscription();
                if (isNewMember) {
                    sub.setProduct(PARENT_ADVISOR_NOT_VERIFIED);
                    sendVerificationEmail = true;
                } else {
                    sub.setProduct(PARENT_ADVISOR);

                    // add success event to track having signed up for emails
                    omnitureTracking = new JsonBasedOmnitureTracking();
                    omnitureTracking.addSuccessEvent(OmnitureTracking.SuccessEvent.EmailModuleSignup);
                }
                sub.setUser(user);
                _subscriptionDao.saveSubscription(sub);
            } else if (subscribedToProdNotVerified) {
                // user already tried to sign up, but haven't verified email yet;
                // send another verification email to make it easy for them to confirm their email
                sendVerificationEmail = true;
            }

            if (sendVerificationEmail) {
                // send email verification email
                sendVerificationEmail(request, user);
                status = STATUS_SUCCESS_EMAIL_SENT;
            } else {
                status = STATUS_SUCCESS_NO_EMAIL_SENT;
            }
        } else {
            status = STATUS_ERROR;
        }

        sendJsonResponse(response, errors, status, omnitureTracking);
    }

    private void sendJsonResponse(HttpServletResponse response, String errors, String status, JsonBasedOmnitureTracking omnitureTracking) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println("{");
        out.println("\"status\":\"" + status + "\"");
        if (omnitureTracking != null) {
            out.println("," + "\"omnitureTracking\":" + omnitureTracking.toJsonObject());
        }
        if (StringUtils.isNotBlank(errors)) {
            out.println("," + "\"errors\":\"" + errors + "\"");
        }
        out.println("}");
    }

    private void sendVerificationEmail(HttpServletRequest request, User user) throws IOException, MessagingException, NoSuchAlgorithmException {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.HOME);
        String redirectUrl = urlBuilder.asFullUrl(request);
        getEmailVerificationEmail().sendVerificationEmail(request, user, redirectUrl);
    }

    /**
     * Returns empty string if the command seems valid; otherwise, comma-separated list of fields with errors
     */
    protected String validate(EmailSignUpCommand command, boolean userExists, boolean subscribedToProd) {
        List<String> errorList = new ArrayList<String>();

        // validate not null email
        if (StringUtils.isBlank(command.getEmail())) {
            errorList.add("emailInvalid");
        } else {
            // validate format email
            EmailValidator emailValidator = EmailValidator.getInstance();
            if (!emailValidator.isValid(command.getEmail())) {
                errorList.add("emailInvalid");
            } else {
                if (userExists && subscribedToProd) {
                    errorList.add("emailAlreadySignedUp");
                }
            }
        }

        return StringUtils.join(errorList, ',');
    }

    public EmailVerificationEmail getEmailVerificationEmail() {
        return _emailVerificationEmail;
    }

    public void setEmailVerificationEmail(EmailVerificationEmail emailVerificationEmail) {
        _emailVerificationEmail = emailVerificationEmail;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}

