package gs.web.email;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;
import gs.data.school.ISchoolDao;
import gs.data.geo.IGeoDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.integration.exacttarget.ExactTargetAPI;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Controller backing the email unsubscribe page.
 */
public class UnsubscribeController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private StateManager _stateManager;

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

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    @Override
    protected void onBindOnNewForm(HttpServletRequest request, Object o) throws Exception {
        SessionContext sc = SessionContextUtil.getSessionContext(request);

        //first try to get user from email in url parameter
        User user = getUserDao().findUserFromEmailIfExists(request.getParameter("email"));
        String verified = request.getParameter("verified");

        //if email not in parameter, check if the user is signed in
        if(user == null){
            if(PageHelper.isMemberAuthorized(request)){
                user = sc.getUser();
            }
        }
        if(user == null){
            return;
        }
        UnsubscribeCommand command = (UnsubscribeCommand) o;
        command.setUserId(user.getId());
        command.setEmail(user.getEmail());
        command.setVerified(verified);
    }

    @Override
    protected ModelAndView showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            BindException be)
            throws Exception {
        UnsubscribeCommand command =  (UnsubscribeCommand) be.getTarget();
        if(command.getUserId() == 0){
            return new ModelAndView("redirect:/community/loginOrRegister.page?redirect=/email/unsubscribe.page");
        }else{
            Map<String, Object> model = new HashMap<String, Object>();
            model.put(getCommandName(), command);
            User user = new User();
            user.setId(command.getUserId());
            PageHelper.setMemberCookie(request, response, user);
            return new ModelAndView("email/unsubscribe",model);
        }
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException be) throws Exception {
        UnsubscribeCommand command = (UnsubscribeCommand)o;
        User user = getUserDao().findUserFromId(command.getUserId());
        State state = user.getState();
        if(user.getUserProfile() != null){
            state = user.getUserProfile().getState();
        }
        if (state == null) {
            state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        }
        List<String> messages = new ArrayList<String>();
        updateMessages(command, messages);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(getCommandName(), command);
        model.put("messages",messages);

        List<Subscription> subscriptions = _subscriptionDao.getUserSubscriptions(user);
        if(subscriptions != null){
            for (Object subscription : subscriptions) {
                Subscription s = (Subscription) subscription;
                _subscriptionDao.removeSubscription(s.getId());
            }
        }



        List<Student> previousMyNth = _subscriptionDao.findMynthSubscriptionsByUser(user);
        for (Student student: previousMyNth) {
            _subscriptionDao.removeStudent(student.getId());
        }
        // DELETE USER FROM EXACT TARGET IF NO SUBSCRIPTIONS
        ExactTargetAPI _etAPI = (ExactTargetAPI)getApplicationContext().getBean(ExactTargetAPI.BEAN_ID);
        _etAPI.deleteSubscriber(user.getEmail());
        return new ModelAndView(getSuccessView(), model);
    }


    protected void updateMessages(UnsubscribeCommand command, List<String> messages) {
        messages.clear();
    }

    protected void checkRequestParams(HttpServletRequest request, UnsubscribeCommand command) {
    }
}
