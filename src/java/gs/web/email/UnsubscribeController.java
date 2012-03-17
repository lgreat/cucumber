package gs.web.email;

import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;
import gs.data.state.State;
import gs.data.integration.exacttarget.ExactTargetAPI;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Controller backing the email unsubscribe page.
 */
@Controller
@RequestMapping("/email/")
public class UnsubscribeController implements ReadWriteAnnotationController {
    protected final Log _log = LogFactory.getLog(getClass());
    private static final String UNSUBSCRIBE_FORM_VIEW = "/email/unsubscribe";
    private static final String LOGIN_PAGE_VIEW = "/community/loginOrRegister.page?redirect=/email/management.page";
    private static final String UNSUBSCRIBE_SURVEY_VIEW = "/email/unsubscribeSurvey";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_REF = "ref";

    @Autowired
    private IUserDao _userDao;
    @Autowired
    private ISubscriptionDao _subscriptionDao;
    @Autowired
    @Qualifier("exactTargetAPI") /*http://forum.springsource.org/showthread.php?60659-Problem-with-Autowired-(requires-Qualifier)*/
    private ExactTargetAPI _exactTargetAPI;
    @Autowired
    private IUnsubscribeDao _unsubscribeDao;
    @Autowired
    private IUnsubscribedProductsDao _unsubscribedProductsDao;

    @RequestMapping(value="unsubscribe.page", method=RequestMethod.GET)
    public String showForm(@ModelAttribute("emailCmd") UnsubscribeCommand command, 
                           ModelMap modelMap, 
                           HttpServletRequest request,
                           HttpServletResponse response,
                           @RequestParam(value=PARAM_EMAIL, required=false) String email,
                           @RequestParam(value=PARAM_REF, required=false) String ref){
        User user = getValidUser(request, email);
        if(user == null){
            return "redirect:" + LOGIN_PAGE_VIEW;
        }
        else {
            command.setUserId(user.getId());
            command.setEmail(user.getEmail());

            /* Preselect the check boxes if parameters are set to 1 */
            command.setWeeklyNl(request.getParameter("weekly"));
            command.setDailyNl(request.getParameter("daily"));
            command.setMss(request.getParameter("mss"));
            command.setPartnerOffers(request.getParameter("partner"));

            int userIdParam = command.getUserId();
            if(ref != null) {
                try{
                    userIdParam = Integer.parseInt(ref);
                } catch(NumberFormatException e) {
                  /* on exception userIdParam will have the value of command object's userId */
                }
            }

            /* if the user's member id and the user id param do not belong to the same user, redirect to the login page */
            if(command.getUserId() == 0 || command.getUserId() != userIdParam) {
                return "redirect:" + LOGIN_PAGE_VIEW;
            }
            /* display the unsubscribe form */
            else {
                modelMap.put("emailCmd", command);
                return UNSUBSCRIBE_FORM_VIEW;
            }
        }
    }

    @RequestMapping(value="unsubscribe.page", method=RequestMethod.POST)
    protected String onSubmit(@ModelAttribute("emailCmd") UnsubscribeCommand command,
                              BindingResult bindingResult,
                              ModelMap modelMap,
                              HttpServletRequest request) throws Exception {
        /* if no checkbox were selected and the associated submit button was clicked, display the form with error field set */
        if(WebUtils.hasSubmitParameter(request, "Unsubscribe") && command.isAllUnchecked()){
            bindingResult.reject("unsubscribe_error");
            modelMap.put("hasErrors",true);
            return UNSUBSCRIBE_FORM_VIEW;
        }

        User user = _userDao.findUserFromId(command.getUserId());
        State state = user.getState();
        if(user.getUserProfile() != null){
            state = user.getUserProfile().getState();
        }
        if (state == null) {
            state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        }

        boolean unsubscribeAll =  false;
        List<String> messages = new ArrayList<String>();
        updateMessages(command, messages);

        modelMap.put("emailCmd", command);
        modelMap.put("messages",messages);

        Unsubscribe unsubscribe = null;
        Set<UnsubscribedProducts> unsubscribedProducts = new HashSet<UnsubscribedProducts>();
        Set<String> subscriberAttributes = new HashSet<String>();

        /*get all subscriptions in single call, then filter them*/
        List<Subscription> allSubscriptions = _subscriptionDao.getUserSubscriptions(user);
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        
        /* if "Unsubscribe" submit button was used, add the subscriptions that were checked. create an unsubscribe object with the
        user and unsubscribe method */
        if(WebUtils.hasSubmitParameter(request,"Unsubscribe") && allSubscriptions != null ){
            unsubscribe = new Unsubscribe(user, "checkbox");
            for(Subscription subscription : allSubscriptions) {
                if(command.getWeeklyNl() && SubscriptionProduct.PARENT_ADVISOR.getName().equals(subscription.getProduct().getName())) {
                    subscriptions.add(subscription);
                }
                if(command.getDailyNl() && SubscriptionProduct.DAILY_TIP.getName().equals(subscription.getProduct().getName())){
                    subscriptions.add(subscription);
                }
                if(command.getMss() && SubscriptionProduct.MYSTAT.getName().equals(subscription.getProduct().getName())){
                    subscriptions.add(subscription);
                }
                if(command.getPartnerOffers() && SubscriptionProduct.SPONSOR_OPT_IN.getName().equals(subscription.getProduct().getName())){
                    subscriptions.add(subscription);
                }
            }
        }

        /* if "Unsubscribe All" submit button was used */
        else if(WebUtils.hasSubmitParameter(request, "UnsubscribeAll") && allSubscriptions != null ){
            subscriptions.addAll(allSubscriptions);
            unsubscribe = new Unsubscribe(user, "all");
            unsubscribeAll = true;
        }

        /* if there are any subscriptions to be removed, create unique objects for each of the subscription products,
        and save the unsubscribe
        String passed to the exact target API method is the name that is the attribute name used on exact target*/
        if(subscriptions.size() > 0){
            Iterator<Subscription> subscriptionIterator = subscriptions.iterator();
            while (subscriptionIterator.hasNext()) {
                Subscription s = subscriptionIterator.next();
                if(SubscriptionProduct.PARENT_ADVISOR.getName().equals(s.getProduct().getName())){
                    unsubscribedProducts.add(new UnsubscribedProducts("weekly", unsubscribe));
                    subscriberAttributes.add("GreatNews");
                }
                if(SubscriptionProduct.DAILY_TIP.getName().equals(s.getProduct().getName())){
                    unsubscribedProducts.add(new UnsubscribedProducts("daily", unsubscribe));
                    subscriberAttributes.add("Summer Brain Drain");
                }
                if(SubscriptionProduct.MYSTAT.getName().equals(s.getProduct().getName())){
                    unsubscribedProducts.add(new UnsubscribedProducts("mss", unsubscribe));
                    subscriberAttributes.add("Mystats");
                }
                if(SubscriptionProduct.SPONSOR_OPT_IN.getName().equals(s.getProduct().getName())){
                    unsubscribedProducts.add(new UnsubscribedProducts("partner", unsubscribe));
                    subscriberAttributes.add("Sponsor");
                }
                _subscriptionDao.removeSubscription(s.getId());
            }
            if(unsubscribeAll) {
                _exactTargetAPI.unsubscribeAll(user.getEmail());
            }
            else {
                _exactTargetAPI.unsubscribeProduct(user.getEmail(), subscriberAttributes);
            }
            _unsubscribeDao.saveUnsubscribe(unsubscribe);
            command.setUnsubscribeId(unsubscribe.getId());
            command.setUnsubscribeDateTime(unsubscribe.getUnsubcribeDateTime());
        }

        /* save unsubscribed products if any */
        if(unsubscribedProducts != null){
            for(Object unsubscribedProduct : unsubscribedProducts){
                UnsubscribedProducts unsubProd = (UnsubscribedProducts) unsubscribedProduct;
                _unsubscribedProductsDao.saveUnsubscribedProducts(unsubProd);
            }
        }

        List<Student> previousMyNth = _subscriptionDao.findMynthSubscriptionsByUser(user);
        for (Student student: previousMyNth) {
            _subscriptionDao.removeStudent(student.getId());
        }

        /* make unsubscribe survey page available */
        command.setUnsubscribedSuccess(true);

        return UNSUBSCRIBE_SURVEY_VIEW;
    }

    @RequestMapping(value="unsubscribeSurvey.page", method=RequestMethod.POST)
    protected String onSubmitSurveyForm(@ModelAttribute("emailCmd") UnsubscribeCommand command,
                                  ModelMap modelMap,
                                  HttpServletRequest request){
        if(!command.getUnsubscribedSuccess()){
            return "redirect:" + LOGIN_PAGE_VIEW;
        }

        Unsubscribe unsubscribed = _unsubscribeDao.findUnsubscribeById(command.getUnsubscribeId());

        if(unsubscribed != null){
            _unsubscribeDao.updateUnsubscribedReason(unsubscribed, command.getUnsubscribeReason(), command.getOtherReasonsText(), command.getUnsubscribeDateTime());
        }

        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.HOME);
        return "redirect:" + urlBuilder.asFullUrl(request);
    }

    protected User getValidUser(HttpServletRequest request, String email) {
        SessionContext sc = SessionContextUtil.getSessionContext(request);

        //first try to get user from email in url parameter
        User user = _userDao.findUserFromEmailIfExists(email);

        //if email not in parameter, check if the user is signed in
        if(user == null){
            if(PageHelper.isMemberAuthorized(request)){
                user = sc.getUser();
            }
        }
        return user;
    }

    protected void updateMessages(UnsubscribeCommand command, List<String> messages) {
        messages.clear();
    }

    //Setters for DAO to be used by UnsubscribeControllerTest

    public void setUnsubscribeDao(IUnsubscribeDao unsubscribeDao) {
        _unsubscribeDao = unsubscribeDao;
    }

    public void setUnsubscribedProductsDao(IUnsubscribedProductsDao unsubscribedProductsDao) {
        _unsubscribedProductsDao = unsubscribedProductsDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public void setUserDao(IUserDao userDao){
        _userDao = userDao;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI){
        _exactTargetAPI = exactTargetAPI;
    }
}
