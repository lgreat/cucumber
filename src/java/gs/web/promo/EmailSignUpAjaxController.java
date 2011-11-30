package gs.web.promo;

import gs.data.community.*;
import gs.web.community.registration.EmailVerificationEmail;
import gs.web.tracking.JsonBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.ExactTargetUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final SubscriptionProduct WEEKLY_PROD = SubscriptionProduct.PARENT_ADVISOR;
    private static final SubscriptionProduct WEEKLY_PROD_NOT_VERIFIED = SubscriptionProduct.PARENT_ADVISOR_NOT_VERIFIED;
    private static final SubscriptionProduct DAILY_PROD = SubscriptionProduct.DAILY_TIP;
    private static final SubscriptionProduct DAILY_PROD_NOT_VERIFIED = SubscriptionProduct.DAILY_TIP_NOT_VERIFIED;

    @RequestMapping(value = "/promo/emailSignUpAjax.page", method = RequestMethod.POST)
    public void handleEmailSignUp(@ModelAttribute("command") EmailSignUpCommand command,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {
        // user/subscription info needed for validation and business logic
        User user = (StringUtils.isNotBlank(command.getEmail()) ?
                _userDao.findUserFromEmailIfExists(command.getEmail()) : null);
        boolean subscribedToVerifiedWeeklyProd =
                (user != null) && _subscriptionDao.isUserSubscribed(user, WEEKLY_PROD, null);
        boolean subscribedToNotVerifiedWeeklyProd =
                (user != null) && _subscriptionDao.isUserSubscribed(user, WEEKLY_PROD_NOT_VERIFIED, null);
        boolean subscribedToVerifiedDailyProd =
                (user != null) && _subscriptionDao.isUserSubscribed(user, DAILY_PROD, null);
        boolean subscribedToNotVerifiedDailyProd =
                (user != null) && _subscriptionDao.isUserSubscribed(user, DAILY_PROD_NOT_VERIFIED, null);

        // variables needed for json response
        String errors = validate(command, (user != null),
                subscribedToVerifiedWeeklyProd, subscribedToVerifiedDailyProd);
        String status;
        JsonBasedOmnitureTracking omnitureTracking = null;

        if (StringUtils.isBlank(errors)) {
            boolean isNewMember = false;

            // If the user does not yet exist, add to list_member
            if (user == null) {
                user = new User();
                user.setEmail(command.getEmail());
                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                _userDao.saveUser(user);
                isNewMember = true;
            }

            // add subscription - use not-verified subscription product instead of the regular subscription product
            // if we just created the user. this will let us keep track of that fact that they want to be subscribed
            // to the regular subscription product but not actually subscribe them until they've verified their email

            // weekly email (greatnews)
            EmailSignUpState weeklyState = processSubscription(user, isNewMember,
                    WEEKLY_PROD_NOT_VERIFIED, WEEKLY_PROD,
                    subscribedToNotVerifiedWeeklyProd, subscribedToVerifiedWeeklyProd);

            // daily email (dailytip)
            EmailSignUpState dailyState = processSubscription(user, isNewMember,
                    DAILY_PROD_NOT_VERIFIED, DAILY_PROD,
                    subscribedToNotVerifiedDailyProd, subscribedToVerifiedDailyProd);

            if (weeklyState.isAddSuccessEvent() || dailyState.isAddSuccessEvent()) {
                // add success event to track having signed up for emails
                omnitureTracking = new JsonBasedOmnitureTracking();
                omnitureTracking.addSuccessEvent(OmnitureTracking.SuccessEvent.EmailModuleSignup);
            }

            if (isNewMember || weeklyState.isSendVerificationEmail() || dailyState.isSendVerificationEmail()) {
                // send email verification email
                sendVerificationEmail(request, user, weeklyState.isSendVerificationEmail(), dailyState.isSendVerificationEmail());
                status = STATUS_SUCCESS_EMAIL_SENT;
            } else {
                status = STATUS_SUCCESS_NO_EMAIL_SENT;
            }
        } else {
            status = STATUS_ERROR;
        }

        sendJsonResponse(response, errors, status, omnitureTracking);
    }

    private EmailSignUpState processSubscription(User user,
                                                 boolean isNewMember,
                                                 SubscriptionProduct notVerifiedProduct,
                                                 SubscriptionProduct verifiedProduct,
                                                 boolean subscribedToNotVerifiedProd,
                                                 boolean subscribedToVerifiedProd) {
        boolean sendVerificationEmail = false;
        boolean addSuccessEvent = false;
        if (!subscribedToVerifiedProd && !subscribedToNotVerifiedProd) {
            // save new subscription
            Subscription sub = new Subscription();
            if (isNewMember) {
                sub.setProduct(notVerifiedProduct);
                sendVerificationEmail = true;
            } else {
                sub.setProduct(verifiedProduct);
                addSuccessEvent = true;
            }
            sub.setUser(user);
            _subscriptionDao.saveSubscription(sub);
        } else if (subscribedToNotVerifiedProd) {
            // user already tried to sign up, but haven't verified email yet;
            // send another verification email to make it easy for them to confirm their email
            sendVerificationEmail = true;
        }
        return new EmailSignUpState(sendVerificationEmail, addSuccessEvent);
    }

    private void sendJsonResponse(HttpServletResponse response, String errors, String status,
                                  JsonBasedOmnitureTracking omnitureTracking) throws IOException {
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

    private void sendVerificationEmail(HttpServletRequest request, User user, boolean addedWeeklySubscription, boolean addedDailySubscription)
            throws IOException, MessagingException, NoSuchAlgorithmException {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.HOME);
        String redirectUrl = urlBuilder.asFullUrl(request);
        Map<String,String> otherParams = new HashMap<String,String>();
        otherParams.put(ExactTargetUtil.EMAIL_SUB_WELCOME_PARAM,
                ExactTargetUtil.getEmailSubWelcomeParamValue(addedWeeklySubscription,addedDailySubscription,false,false));
        getEmailVerificationEmail().sendVerificationEmail(request, user, redirectUrl, otherParams);
    }

    /**
     * Returns empty string if the command seems valid; otherwise, comma-separated list of fields with errors
     */
    protected String validate(EmailSignUpCommand command, boolean userExists,
                              boolean subscribedToWeeklyProd, boolean subscribedToDailyProd) {
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
                if (userExists && (subscribedToWeeklyProd && subscribedToDailyProd)) {
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

    private class EmailSignUpState {
        private boolean _sendVerificationEmail;
        private boolean _addSuccessEvent;

        public EmailSignUpState(boolean sendVerificationEmail, boolean addSuccessEvent) {
            _sendVerificationEmail = sendVerificationEmail;
            _addSuccessEvent = addSuccessEvent;
        }

        public boolean isSendVerificationEmail() {
            return _sendVerificationEmail;
        }

        public boolean isAddSuccessEvent() {
            return _addSuccessEvent;
        }
    }
}

