package gs.web.promo;

import gs.web.util.SitePrefCookie;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;
import java.net.URLEncoder;

import gs.data.community.*;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.JsonBasedOmnitureTracking;

import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;

/**
 * Created by chriskimm@greatschools.org
 */
public class SchoolChoicePackPromoController extends AbstractController implements ReadWriteController {

    public static final String BEAN_ID = "/promo/schoolChoicePackPromo.page";
    public static final String EMAIL_PARAM = "email";
    public static final String LEVELS_PARAM = "levels";
    public static final String PAGE_NAME = "pageName";
    public static final String SCHOOL_CHOICE_PACK_TRIGGER_KEY = "chooser_pack_trigger";
    public static final String CHOOSER_SERIES_TRIGGER_KEY = "chooser_series_trigger";
    public static final String REDIRECT_FOR_CONFIRM = "redirectForConfirm";

    private ISubscriptionDao _subscriptionDao;
    private IUserDao _userDao;
    private ExactTargetAPI _exactTargetAPI;
    private static final Logger _log = Logger.getLogger(SchoolChoicePackPromoController.class);

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        SessionContext context = SessionContextUtil.getSessionContext(request);
        User loggedInUser = context.getUser();
        boolean isLoggedIn = (loggedInUser != null);

        String email = request.getParameter(EMAIL_PARAM);
        String level_val= request.getParameter(LEVELS_PARAM);
        String pageName = request.getParameter(PAGE_NAME);
        String redirectForConfirm = request.getParameter(REDIRECT_FOR_CONFIRM);
        
        if (level_val != null) {

            String[] levels = level_val.split(",");

            User user = _userDao.findUserFromEmailIfExists(email);

            // If the user does not yet exist, add her to list_member
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                _userDao.saveUser(user);
            }


            JsonBasedOmnitureTracking omnitureTracking = new JsonBasedOmnitureTracking();

            omnitureTracking.addSuccessEvent(OmnitureTracking.SuccessEvent.ChoicePackRequest);
            omnitureTracking.addEvar(new OmnitureTracking.Evar(OmnitureTracking.EvarNumber.CrossPromotion, "Chooser_pack_" + pageName));



            // add each promo level as a new subscription
            for (String level : levels) {
                SubscriptionProduct prod = SubscriptionProduct.getSubscriptionProduct("chooserpack_" + level);
                if (prod != null) {
                    if (!_subscriptionDao.isUserSubscribed(user, prod, null)) {
                        Subscription sub = new Subscription();
                        sub.setProduct(prod);
                        sub.setUser(user);
                        _subscriptionDao.saveSubscription(sub);
                    } else {
                        StringBuilder sb = new StringBuilder("User is already subscribed: ");
                        sb.append(user.toString());
                        sb.append(" prod: ");
                        sb.append(prod.getName());
                        _log.info(sb.toString());
                    }
                } else {
                    _log.warn ("Could not find subscription product for: " + level);
                }
            }

            // don't set memid cookie here per GS-8301
            // if there is no logged in user, then sign in the user using the email entered
//            if (!isLoggedIn) {
//                PageHelper.setMemberCookie(request, response, user);
//            }
            triggerPromoPackEmail(user, levels);
            String emailEncoded = URLEncoder.encode(email, "UTF-8");
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.println("{");
            out.println("\"memid\":\"" + String.valueOf(user.getId()) + "\",");
            out.println("\"redirectEncoded\":\"" + URLEncoder.encode(redirectForConfirm, "UTF-8") + "\",");

            // if the email entered is not already registered with community
            if (!user.isCommunityMember()) {
                // if there is no logged in user, or the logged in user is not a community member already
                // forward them to registration
                if (!isLoggedIn || (!PageHelper.isCommunityCookieSet(request))) {
                    out.println("\"showRegistration\":\"" + "y" + "\",");
                }
            }
            // if there is no logged in user, then tell the JS to sign in the user using the email entered.
            // We have to do this here because the cookie has to be set IMMEDIATELY on the page so that
            // any AJAX calls to other pages will reflect the signed in status. The PageHelper call above
            // may not set the cookie until the next request.
            if(!isLoggedIn){
                out.println("\"createMemberCookie\":\"" + "y" + "\",");
            }
            out.println("\"emailEncoded\":\"" + emailEncoded + "\",");
            out.println("\"omnitureTracking\":" + omnitureTracking.toJsonObject());
            out.println("}");

            SitePrefCookie cookie = new SitePrefCookie(request, response);
            cookie.setProperty("schoolChoicePackAlreadySubmitted", "true");
        }
        return null;
    }

    /**
     * This method triggers an ExactTarget SOAP api call that sends out the
     * promo pack email.
     * @param user - a valid user
     * @param levels - the grade levels - {p,e,m,h} 
     */
    void triggerPromoPackEmail(User user, String[] levels) {
        Map<String, String> attributes = new HashMap<String, String>();
        for (String level : levels) {
            attributes.put(level,  "1");
        }
        _exactTargetAPI.sendTriggeredEmail(SCHOOL_CHOICE_PACK_TRIGGER_KEY, user, attributes);
    }

    void triggerChooserSeries(User user){

        _exactTargetAPI.sendTriggeredEmail(CHOOSER_SERIES_TRIGGER_KEY,user);
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public void setExactTargetAPI(ExactTargetAPI etAPI) {
        _exactTargetAPI = etAPI;
    }
}
