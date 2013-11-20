package gs.web.content.cms;

import gs.data.community.*;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.json.JSONObject;
import gs.web.community.registration.EmailVerificationEmail;
import gs.web.util.ExactTargetUtil;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class NewsletterSubscriptionController extends SimpleFormController implements ReadWriteController {
    private IUserDao _userDao;
    protected final Log _log = LogFactory.getLog(getClass());
    private ISubscriptionDao _subscriptionDao;
    private EmailVerificationEmail _emailVerificationEmail;

    private ExactTargetAPI _exactTargetAPI;

    public static final String EXACT_TARGET_HOME_PAGE_PITCH_KEY = "offer_download_trigger";

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {

        ModelAndView mAndV = new ModelAndView();
        NewsletterSubscriptionCommand nlSubCmd = (NewsletterSubscriptionCommand) command;

        String email = StringEscapeUtils.escapeHtml(nlSubCmd.getEmail());
        User user = null;
        if (StringUtils.isNotBlank(email)) {
            user = getUserDao().findUserFromEmailIfExists(email);

            boolean isSubscribedToParentAdvisor = false;
            boolean isSubscribedToSponsorOptIn = false;
            boolean shouldSendVerificationEmail = false;
            boolean addedParentAdvisorSubscription = false;
            boolean addedSponsorOptInSubscription = false;
            List subscriptions = new ArrayList();

            if (user != null) {
                Set<Subscription> userSubs = user.getSubscriptions();

                if (userSubs != null) {
                    for (Subscription s : userSubs) {
                        if (SubscriptionProduct.PARENT_ADVISOR.equals(s.getProduct())) {
                            isSubscribedToParentAdvisor = true;
                        } else if (SubscriptionProduct.SPONSOR_OPT_IN.equals(s.getProduct())) {
                            isSubscribedToSponsorOptIn = true;
                        }
                    }
                }

                if (!isSubscribedToParentAdvisor || (!isSubscribedToSponsorOptIn && nlSubCmd.isPartnerNewsletter())) {

                    if (!isSubscribedToParentAdvisor) {
                        addSubscription(subscriptions, user, SubscriptionProduct.PARENT_ADVISOR);
                        addedParentAdvisorSubscription = true;
                    }
                    if (nlSubCmd.isPartnerNewsletter() && !isSubscribedToSponsorOptIn) {
                        addSubscription(subscriptions, user, SubscriptionProduct.SPONSOR_OPT_IN);
                        addedSponsorOptInSubscription = true;

                    }

                }
            } else if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                if(nlSubCmd.isNlSignUpFromHomePage()){
                    user.setEmailVerified(true);
                    user.setHow("hover_offerdownload");
                    shouldSendVerificationEmail = false;
                } else {
                    user.setHow("hover_article");
                    shouldSendVerificationEmail = true;
                }
                _userDao.saveUser(user);

                addSubscription(subscriptions, user, SubscriptionProduct.PARENT_ADVISOR);
                addedParentAdvisorSubscription = true;

                if (nlSubCmd.isPartnerNewsletter()) {
                    addSubscription(subscriptions, user, SubscriptionProduct.SPONSOR_OPT_IN);
                    addedSponsorOptInSubscription = true;
                }
            }

            if (subscriptions != null && subscriptions.size() > 0) {
                getSubscriptionDao().addNewsletterSubscriptions(user, subscriptions);
            }

            String thankYouMsg = "You have successfully subscribed to the GreatSchools weekly newsletter.";

            if (shouldSendVerificationEmail) {
                sendVerificationEmail(request, user, addedParentAdvisorSubscription, addedSponsorOptInSubscription);
                thankYouMsg = "Please confirm your subscription(s) by clicking the link in the email we just sent you.";
            }

            if (nlSubCmd.isNlSignUpFromHomePage()){
                _exactTargetAPI.sendTriggeredEmail(EXACT_TARGET_HOME_PAGE_PITCH_KEY, user);
            }

            if (nlSubCmd.isAjaxRequest()) {
                JSONObject rval = new JSONObject();
                rval.put("userAlreadySubscribed", isSubscribedToParentAdvisor);
                rval.put("thankYouMsg", thankYouMsg);
                response.getWriter().print(rval.toString());
                return null;
            }
        }
        return mAndV;
    }

    private void sendVerificationEmail(HttpServletRequest request, User user, boolean addedParentAdvisorSubscription, boolean addedSponsorOptInSubscription)
            throws IOException, MessagingException, NoSuchAlgorithmException {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.HOME);
        urlBuilder.addParameter("showSubscriptionThankYouHover","true");
        String redirectUrl = urlBuilder.asFullUrl(request);
        Map<String,String> otherParams = new HashMap<String,String>();
        otherParams.put(ExactTargetUtil.EMAIL_SUB_WELCOME_PARAM,ExactTargetUtil.getEmailSubWelcomeParamValue(addedParentAdvisorSubscription,false,false,addedSponsorOptInSubscription));
        getEmailVerificationEmail().sendVerificationEmail(request, user, redirectUrl, otherParams);
    }

    private void addSubscription(List subscriptions, User user, SubscriptionProduct product) {
        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setProduct(product);
        subscriptions.add(sub);
    }

    public EmailVerificationEmail getEmailVerificationEmail() {
        return _emailVerificationEmail;
    }

    public void setEmailVerificationEmail(EmailVerificationEmail emailVerificationEmail) {
        _emailVerificationEmail = emailVerificationEmail;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
    }
}