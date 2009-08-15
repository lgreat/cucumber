package gs.web.cbi;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.community.*;
import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.util.*;

public class CBIIntegrationController implements Controller {

    /** Spring BEAN id */
    public static final String BEAN_ID = "/cbi/integration.page";

    private static final String KEY_PARAM = "key";
    private static final String ACTION_PARAM = "action";
    private static final String SECRET_KEY = "cbounder";
    /** Keys to the actions supported by this controller */
    public static final String SEND_TRIGGERED_EMAIL = "send_triggered_email";
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
            }
        }
        response.setContentType("text/plain");
        out.flush();
        return null;
    }

//    protected String sendTargetTriggeredEmail(HttpServletRequest request) {
//        String exactTargetKey = request.getParameter("etkey");
//        String emailAddress = request.getParameter("email");
//        StringBuilder response = new StringBuilder();
//        if(exactTargetKey != null && emailAddress != null){
//             User user = new User();
//             user.setEmail(emailAddress);
//             Map attributesMap = constructETAttributesMap(request);
//            if(!attributesMap.isEmpty()){
//                _exactTargetAPI.sendTriggeredEmail(exactTargetKey,user,attributesMap);
//            }else{
//                _exactTargetAPI.sendTriggeredEmail(exactTargetKey,user);
//            }
//            response.append("success");
//        }
//        return response.toString();
//    }


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
                    if(isCourseCompletionEmail){
                        //check
                    }
                    _exactTargetAPI.sendTriggeredEmail(exactTargetKey,user,attributesMap);
                }else{
                    _exactTargetAPI.sendTriggeredEmail(exactTargetKey,user);
                }
                response.append("success");
            }            
        }
        return response.toString();
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
        return attributes;
    }

    public void removeCourseCompleteSubscription(User user){
        Set<Subscription> subs = user.getSubscriptions();
        Iterator it = subs.iterator();
        while(it.hasNext()){
            Subscription s = (Subscription)it.next();
            if(s.getProduct().getLongName().equals(SubscriptionProduct.CB_COURSE_COMPLETE.getLongName())){
                _subscriptionDao.removeSubscription(s.getId());
            }
        }
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
