package gs.web.cbi;

import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.community.*;
import gs.data.state.State;
import gs.web.util.ReadWriteController;
import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.util.*;

public class CBIIntegrationController implements ReadWriteController {

    /** Spring BEAN id */
    public static final String BEAN_ID = "/cbi/integration.page";

    private static final String KEY_PARAM = "key";
    private static final String ACTION_PARAM = "action";
    private static final String SECRET_KEY = "cbounder";
    /** Keys to the actions supported by this controller */
    public static final String SEND_TRIGGERED_EMAIL = "send_triggered_email";
    public static final String SUBSCRIBE_CB_NL = "subscribe_cb_nl";
    public static final String UNSUBSCRIBE_CB_NL = "unsubscribe_cb_nl";
    public static final String SUBSCRIBE_CB_COURSE_COMPLETE = "subscribe_cb_coursecomplete";
    public static final String UNSUBSCRIBE_CB_COURSE_COMPLETE = "unsubscribe_cb_coursecomplete";
    private ExactTargetAPI _exactTargetAPI;
    protected IUserDao _userDao;


    private ISubscriptionDao _subscriptionDao;

    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        PrintWriter out = response.getWriter();

        String secretKey = request.getParameter(KEY_PARAM);
        if (SECRET_KEY.equals(secretKey)) {
            String action = request.getParameter(ACTION_PARAM);
            if (SEND_TRIGGERED_EMAIL.equals(action)) {
                out.print(sendExactTargetTriggeredEmail(request));
            }else if(UNSUBSCRIBE_CB_NL.equals(action)){
                out.print(unsubscribeUser(request.getParameter("email"),SubscriptionProduct.CB_NEWSLETTER));
            }else if(UNSUBSCRIBE_CB_COURSE_COMPLETE.equals(action)){
                out.print(unsubscribeUser(request.getParameter("email"),SubscriptionProduct.CB_COURSE_COMPLETE));
            }else if(SUBSCRIBE_CB_NL.equals(action)){
                out.print(subscribeUser(request.getParameter("email"),request.getParameter("state"),SubscriptionProduct.CB_NEWSLETTER));
            }else if(SUBSCRIBE_CB_COURSE_COMPLETE.equals(action)){
                out.print(subscribeUser(request.getParameter("email"),request.getParameter("state"),SubscriptionProduct.CB_COURSE_COMPLETE));
            }
        }
        response.setContentType("text/plain");
        out.flush();
        return null;
    }

    protected String sendExactTargetTriggeredEmail(HttpServletRequest request) {
        String exactTargetKey = request.getParameter("etkey");
        String memberid = request.getParameter("memberid");
        boolean isCourseCompletionEmail = new Boolean(request.getParameter("iscoursecompletion"));
        StringBuilder response = new StringBuilder();
        if(exactTargetKey != null && memberid != null){
            User user = _userDao.findUserFromId(Integer.parseInt(memberid));
            if(user != null){
                Map attributesMap = constructETAttributesMap(request,user);
                if(!attributesMap.isEmpty()){
                    //if course completion email
                    if(isCourseCompletionEmail){
                        //check if the user is subscribed to course completion emails
                        if(isUserSubscribedToCourseCompletion(user)){
                            _exactTargetAPI.sendTriggeredEmail(exactTargetKey,user,attributesMap);
                        }
                    }else{
                        _exactTargetAPI.sendTriggeredEmail(exactTargetKey,user,attributesMap);
                    }
                }
                response.append("success");
            }            
        }
        return response.toString();
    }
    
     public String subscribeUser(String email,String state,SubscriptionProduct subProduct) {
        StringBuilder response = new StringBuilder();
        User user = _userDao.findUserFromEmailIfExists(email);
        if(user != null){
            Subscription sub = new Subscription();
            setSubscriptionData(sub,user, state);
            sub.setProduct(subProduct);
            _subscriptionDao.saveSubscription(sub);
            response.append("success");
        }
        return response.toString();
     }

    public String unsubscribeUser(String email,SubscriptionProduct sub){
        StringBuilder response = new StringBuilder();
        User user = _userDao.findUserFromEmailIfExists(email);
        if(user != null){
            Set<Subscription> subs = user.getSubscriptions();
            for (Subscription sub1 : subs) {
                if (sub1.getProduct().getLongName().equals(sub.getLongName())) {
                    _subscriptionDao.removeSubscription(sub1.getId());
                    response.append("success");
                }
            }
            if(_subscriptionDao.getUserSubscriptions(user) == null){
                _exactTargetAPI.deleteSubscriber(email);
            }
        }
         return response.toString();
    }

    public void setSubscriptionData(Subscription sub,User user,String state){
        sub.setUser(user);
        if(StringUtils.isNotBlank(state)){
        sub.setState(State.fromString(state));
        }else{
        sub.setState(State.CA);
        }
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI _exactTargetAPI) {
        this._exactTargetAPI = _exactTargetAPI;
    }

    public Map constructETAttributesMap(HttpServletRequest request,User user){
        Map<String, String> attributes = new HashMap<String, String>();
        String groupName = request.getParameter("groupName");
        String startDate = request.getParameter("startdate");
        String courseIndex = request.getParameter("courseindex");
        String sponsorId = request.getParameter("sponsorId");
        attributes.put("cbFirstName",user.getFirstName());
        attributes.put("cbUserName",user.getUserProfile().getScreenName());
        if(StringUtils.isNotBlank(groupName)){
            attributes.put("cbGroupName",groupName);
        }
        if(StringUtils.isNotBlank(startDate)){
            attributes.put("cbStartDate",startDate);
        }
        if(StringUtils.isNotBlank(courseIndex)){
            attributes.put("cbCourseIndex",courseIndex);
        }
        if(StringUtils.isNotBlank(sponsorId)){
            attributes.put("cbPartnerId",sponsorId);
        }
        
        return attributes;
    }
    
    public boolean isUserSubscribedToCourseCompletion(User user){
        Set<Subscription> subs = user.getSubscriptions();
        Iterator it = subs.iterator();
        while(it.hasNext()){
            Subscription s = (Subscription)it.next();
            if(s.getProduct().getLongName().equals(SubscriptionProduct.CB_COURSE_COMPLETE.getLongName())){
              return true;
            }
        }
        return false;
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

}
