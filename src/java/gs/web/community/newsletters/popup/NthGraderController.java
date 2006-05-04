/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NthGraderController.java,v 1.3 2006/05/04 18:03:36 dlee Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.school.ISchoolDao;
import gs.data.state.State;
import gs.web.util.validator.EmailValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
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
public class NthGraderController extends SimpleFormController {
    public static final String BEAN_ID = "/community/newsletters/popup/mss/page2.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private ISchoolDao _schoolDao;

    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {
        NewsletterCommand nc = (NewsletterCommand) command;
        Validator val = new EmailValidator();
        val.validate(nc, errors);
    }

    public ModelAndView onSubmit(Object command) {
        NewsletterCommand nc = (NewsletterCommand) command;
        String email = nc.getEmail();
        User user = _userDao.getUserFromEmailIfExists(email);
        State state = nc.getState();

        if (user == null) {
            user = new User();
            user.setEmail(email);
            _userDao.saveUser(user);
        }

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

        _subscriptionDao.addNewsletterSubscriptions(user, subscriptions);

        ModelAndView mAndV = new ModelAndView();
        mAndV.setViewName(getSuccessView());
        mAndV.getModel().put("email", email);
        mAndV.getModel().put("state", state);
        mAndV.getModel().put("schoolId", String.valueOf(nc.getSchoolId()));

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
