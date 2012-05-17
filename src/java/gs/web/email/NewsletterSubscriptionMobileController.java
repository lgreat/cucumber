package gs.web.email;

import gs.data.community.*;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.web.community.registration.EmailVerificationEmail;
import gs.web.mobile.MobileHelper;
import gs.web.request.RequestInfo;
import gs.web.util.ExactTargetUtil;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 4/18/12
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */

@Controller
@RequestMapping("/email/")
public class NewsletterSubscriptionMobileController implements ReadWriteAnnotationController {
    public static final String SIGNUP_VIEW = "/email/newsletterSubscription";
    public static final String SIGNUP_MOBILE_VIEW = "/email/newsletterSubscription.page";
    private static final String EMAIL_VERIFIED_VIEW = "/email/newslettersEmailVerified.page";
    private static final String EMAIL = "email";

    @Autowired
    @Qualifier("emailVerificationEmail")
    private EmailVerificationEmail _emailVerificationEmail;
    @Autowired
    private IUserDao _userDao;
    @Autowired
    private ISubscriptionDao _subscriptionDao;

    @RequestMapping (value="newsletterSubscription.page", method=RequestMethod.GET)
    public String showForm (HttpServletRequest request) throws IOException {
        RequestInfo requestInfo = RequestInfo.getRequestInfo(request);
        if (!requestInfo.shouldRenderMobileView()) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.NEWSLETTER_MANAGEMENT, null, "");
            return "redirect:" + urlBuilder.asSiteRelative(request);
        }
        return SIGNUP_VIEW;
    }

    @RequestMapping (value="newsletterSubscription.page", method = RequestMethod.POST)
    public void submitForm (HttpServletRequest request,
                           HttpServletResponse response,
                           @RequestParam (value=EMAIL, required=false) String email) throws Exception {
        response.setContentType("application/json");

        if (email == null || !validateEmail(email)) {
            String emailError = "Please enter your email address to sign up.";
            outputJson("error", emailError, response);
            return;
        }

        //Similar to NewsletterSubscriptionController for the article pages hover

        boolean isSubscribedToWeeklyNl = false;
        boolean shouldSendVerificationEmail = false;
        List<Subscription> subscriptions = new ArrayList<Subscription>();

        User user = _userDao.findUserFromEmailIfExists(email);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setHow("mobile_newsletter");
            user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
            _userDao.saveUser(user);

            shouldSendVerificationEmail = true;
        }
        else {
            if(user.getEmailVerified() == null || !user.getEmailVerified()) {
                shouldSendVerificationEmail = true;
            }
            Set<Subscription> userSubscriptions = user.getSubscriptions();

            if (userSubscriptions != null) {
                for (Subscription subscription: userSubscriptions) {
                    if (SubscriptionProduct.PARENT_ADVISOR.equals(subscription.getProduct())) {
                        isSubscribedToWeeklyNl = true;
                    }
                }
            }
        }

        if (!isSubscribedToWeeklyNl) {
            addSubscription(subscriptions, user, SubscriptionProduct.PARENT_ADVISOR);

            _subscriptionDao.addNewsletterSubscriptions(user, subscriptions);
        }

        if(shouldSendVerificationEmail) {
            sendVerificationEmail(request, user, true);
            String message = "Please confirm your subscription by clicking the link in the email we just sent you.";
            outputJson("success", message, response);
        }
        else {
            if (!isSubscribedToWeeklyNl) {
                String message = "You have successfully subscribed to the GreatSchools weekly newsletter.";
                outputJson("success", message, response);
            }
            else {
                String message = "The email address is already signed up.";
                outputJson("error", message, response);
            }
        }
    }

    protected void addSubscription(List<Subscription> subscriptions, User user, SubscriptionProduct subscriptionProduct) {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setProduct(subscriptionProduct);

        subscriptions.add(subscription);
    }

    private void sendVerificationEmail(HttpServletRequest request, User user, boolean addedParentAdvisorSubscription)
            throws IOException, MessagingException, NoSuchAlgorithmException {
        Map<String,String> otherParams = new HashMap<String,String>();
        otherParams.put(ExactTargetUtil.EMAIL_SUB_WELCOME_PARAM,ExactTargetUtil.getEmailSubWelcomeParamValue(addedParentAdvisorSubscription,false,false,false));
        _emailVerificationEmail.sendVerificationEmail(request, user, EMAIL_VERIFIED_VIEW, otherParams);
    }

    protected boolean validateEmail(String email) {
        org.apache.commons.validator.EmailValidator emv = org.apache.commons.validator.EmailValidator.getInstance();
        return emv.isValid(email);
    }

    protected void outputJson(String type, String message, HttpServletResponse response) throws JSONException, IOException {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(type, message);
        jsonResponse.write(response.getWriter());
        response.getWriter().flush();
    }
}