/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: MssPaController.java,v 1.3 2006/05/04 06:37:04 dlee Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.school.ISchoolDao;
import gs.data.state.State;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
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

    public MssPaController() {
        setValidateOnBinding(true);
        setBindOnNewForm(true);
    }

    protected void onBindOnNewForm(HttpServletRequest request,
                                 Object command,
                                 BindException errors) {
        NewsletterCommand nc = (NewsletterCommand) command;
        if (nc.getSchoolId() == 0 || nc.getState() == null) {

            errors.reject("bindOnEntry","schoolId or state not passed in");
            _log.error("No state or school id provided");
        }
    }
    /*
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) {
        NewsletterCommand nc = (NewsletterCommand) command;
        State state = nc.getState();
        School s = _schoolDao.getSchoolById(state,Integer.valueOf(nc.getSchoolId()));
        nc.setSchoolName(s.getName());
    }
    */

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
