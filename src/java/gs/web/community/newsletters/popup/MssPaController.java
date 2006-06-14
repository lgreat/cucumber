/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: MssPaController.java,v 1.11 2006/06/14 17:43:32 dlee Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
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
 * Form that allows user to sign up for MSS and Parent Advisor
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class MssPaController extends SimpleFormController {
    public static final String BEAN_ID = "/community/newsletters/popup/mss/page1.page";
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

        for (Iterator iter=validators.iterator(); iter.hasNext();) {
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

        if (!errors.hasErrors()) {
            State state = nc.getState();
            School s = getSchoolDao().getSchoolById(state, new Integer(nc.getSchoolId()));
            if (s != null) {
                nc.setSchoolName(s.getName());
            } else {
                errors.reject("nokey","Invalid school");
                _log.info("Invalid school passed" + state.getAbbreviation() + nc.getSchoolId());
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
            PageHelper.setMemberCookie(response, user);
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

        getSubscriptionDao().addNewsletterSubscriptions(user, subscriptions);

        ModelAndView mAndV = new ModelAndView();

        mAndV.getModel().put("state", nc.getState());
        mAndV.getModel().put("email", nc.getEmail());
        mAndV.getModel().put("schoolId", String.valueOf(nc.getSchoolId()));

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

    public List getOnLoadValidators() {
        return _onLoadValidators;
    }

    public void setOnLoadValidators(List onLoadValidators) {
        _onLoadValidators = onLoadValidators;
    }

}
