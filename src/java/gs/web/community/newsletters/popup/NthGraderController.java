/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NthGraderController.java,v 1.10 2006/06/14 18:31:23 dlee Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.school.ISchoolDao;
import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionContext;
import gs.web.util.PageHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class NthGraderController extends SimpleFormController {
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private ISchoolDao _schoolDao;
    private List _onLoadValidators;


    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {
        NewsletterCommand nc = (NewsletterCommand) command;
        List validators = getOnLoadValidators();
        for (Iterator iter = validators.iterator(); iter.hasNext();) {
            Validator val = (Validator) iter.next();
            if (val.supports(nc.getClass())) {
                val.validate(nc, errors);
            }
        }

        if (nc.getEmail() == null) {
            ISessionFacade session = SessionContext.getInstance(request);
            User user = session.getUser();
            if (user != null) {
                nc.setEmail(user.getEmail());
            } else {
                nc.setEmail("");
            }
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) {
        NewsletterCommand nc = (NewsletterCommand) command;
        String email = nc.getEmail();
        User user = getUserDao().findUserFromEmailIfExists(email);
        State state = nc.getState();

        if (user == null) {
            user = new User();
            user.setEmail(email);
            getUserDao().saveUser(user);
        }
        PageHelper.setMemberCookie(response, user);

        List subscriptions = new ArrayList();

        if (nc.isMyk()) {
            Subscription sub = new Subscription();
            sub.setUser(user);
            sub.setProduct(SubscriptionProduct.MY_KINDERGARTNER);
            sub.setState(state);
            subscriptions.add(sub);
        }

        if (nc.isMy1()) {
            Subscription sub = new Subscription();
            sub.setUser(user);
            sub.setProduct(SubscriptionProduct.MY_FIRST_GRADER);
            sub.setState(state);
            subscriptions.add(sub);
        }

        if (nc.isMy2()) {
            Subscription sub = new Subscription();
            sub.setUser(user);
            sub.setProduct(SubscriptionProduct.MY_SECOND_GRADER);
            sub.setState(state);
            subscriptions.add(sub);
        }

        if (nc.isMy3()) {
            Subscription sub = new Subscription();
            sub.setUser(user);
            sub.setProduct(SubscriptionProduct.MY_THIRD_GRADER);
            sub.setState(state);
            subscriptions.add(sub);
        }

        if (nc.isMy4()) {
            Subscription sub = new Subscription();
            sub.setUser(user);
            sub.setProduct(SubscriptionProduct.MY_FOURTH_GRADER);
            sub.setState(state);
            subscriptions.add(sub);
        }

        if (nc.isMy5()) {
            Subscription sub = new Subscription();
            sub.setUser(user);
            sub.setProduct(SubscriptionProduct.MY_FIFTH_GRADER);
            sub.setState(state);
            subscriptions.add(sub);
        }

        if (nc.isMyMs()) {
            Subscription sub = new Subscription();
            sub.setUser(user);
            sub.setProduct(SubscriptionProduct.MY_MS);
            sub.setState(state);
            subscriptions.add(sub);
        }

        if (nc.isMyHs()) {
            Subscription sub = new Subscription();
            sub.setUser(user);
            sub.setProduct(SubscriptionProduct.MY_HS);
            sub.setState(state);
            subscriptions.add(sub);
        }

        if (nc.isGn()) {
            Subscription sub = new Subscription();
            sub.setUser(user);
            sub.setProduct(SubscriptionProduct.PARENT_ADVISOR);
            sub.setState(state);
            subscriptions.add(sub);
        }

        getSubscriptionDao().addNewsletterSubscriptions(user, subscriptions);

        ModelAndView mAndV = new ModelAndView();
        mAndV.setViewName(getSuccessView());
        mAndV.getModel().put("state", state);
        mAndV.getModel().put("email", email);
        mAndV.getModel().put("schoolId", String.valueOf(nc.getSchoolId()));

        if (nc.getSchoolId() == 0) {
            mAndV.getModel().put(SubscriptionSummaryController.PARAM_SHOW_FIND_SCHOOL_LINK, "1");
        }
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

    public List getOnLoadValidators() {
        return _onLoadValidators;
    }

    public void setOnLoadValidators(List onLoadValidators) {
        _onLoadValidators = onLoadValidators;
    }

}
