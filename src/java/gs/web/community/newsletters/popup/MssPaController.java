/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: MssPaController.java,v 1.2 2006/05/04 00:25:45 dlee Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionFacade;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class MssPaController extends SimpleFormController {
    public static final String BEAN_ID = "/community/newsletters/popup/mss/page1.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private ISchoolDao _schoolDao;

    private static String PARAM_SCHOOL_ID = "schoolId";

    protected Object formBackingObject(HttpServletRequest request) {
        NewsletterCommand command = new NewsletterCommand();
        ISessionFacade context = SessionFacade.getInstance(request);
        State state = context.getState();
        String schoolId = request.getParameter(PARAM_SCHOOL_ID);
        School s = _schoolDao.getSchoolById(state,Integer.valueOf(schoolId));
        command.setSchoolId(s.getId().intValue());
        command.setSchoolName(s.getName());
        return command;
    }

    public ModelAndView onSubmit(Object command) {
        NewsletterCommand nc = (NewsletterCommand)command;
        String email = nc.getEmail();
        User user = _userDao.getUserFromEmailIfExists(email);
        State state = nc.getState();

        if (user == null) {
            user = new User();
            user.setEmail(email);
             _userDao.saveUser(user);
        }

        List subscriptions = new ArrayList();

        if (nc.isGn()) {
            Subscription sub = new Subscription();
            sub.setUser(user);
            sub.setProduct(SubscriptionProduct.PARENT_ADVISOR);
            sub.setState(state);
            subscriptions.add(sub);
        }

        if (nc.isMystat()) {
            Subscription sub = new Subscription();
            sub.setUser(user);
            sub.setProduct(SubscriptionProduct.MYSTAT);
            sub.setSchoolId(nc.getSchoolId());
            sub.setState(state);
            subscriptions.add(sub);
        }

        _subscriptionDao.addNewsletterSubscriptions(user, subscriptions);

        ModelAndView mAndV = new ModelAndView();
        mAndV.getModel().put("email", email);
        mAndV.getModel().put("state", state);
        mAndV.getModel().put("schoolId", String.valueOf(nc.getSchoolId()));
        mAndV.getModel().put("newsCmd", nc);
        mAndV.setViewName(getSuccessView());
        
        return mAndV;
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

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

}
