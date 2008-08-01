/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NthGraderController.java,v 1.24 2008/08/01 22:33:58 jnorton Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.school.ISchoolDao;
import gs.data.state.State;
import gs.data.admin.IPropertyDao;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteController;
import gs.web.util.NewSubscriberDetector;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows user to subscribe to parent advisor and nth grader newsletters
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class NthGraderController extends SimpleFormController implements ReadWriteController {

    public static final String PARAM_AUTOCHECK = "autocheck";

    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private ISchoolDao _schoolDao;
    private IPropertyDao _propertyDao;
    private List<Validator> _onLoadValidators;


    protected ModelAndView showForm(
            HttpServletRequest request, HttpServletResponse response, BindException errors)
            throws Exception {
        request.setAttribute("academicYear",
                getPropertyDao().getProperty(IPropertyDao.CURRENT_ACADEMIC_YEAR));
        return super.showForm(request, response, errors, null);
    }

    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {
        String autocheck = request.getParameter(PARAM_AUTOCHECK);

        NewsletterCommand nc = (NewsletterCommand) command;

        List<Validator> validators = getOnLoadValidators();
        // removed the onload state validator, so add a null check here for safety
        if (validators != null) {
            for (Validator val : validators) {
                if (val.supports(nc.getClass())) {
                    val.validate(nc, errors);
                }
            }
        }

        if (StringUtils.isNotBlank(autocheck)) {
            if ("mypk".equalsIgnoreCase(autocheck)) { nc.setMyPk(true); }
            if ("myk".equalsIgnoreCase(autocheck)) { nc.setMyk(true); }
            if ("my1".equalsIgnoreCase(autocheck)) { nc.setMy1(true); }
            if ("my2".equalsIgnoreCase(autocheck)) { nc.setMy2(true); }
            if ("my3".equalsIgnoreCase(autocheck)) { nc.setMy3(true); }
            if ("my4".equalsIgnoreCase(autocheck)) { nc.setMy4(true); }
            if ("my5".equalsIgnoreCase(autocheck)) { nc.setMy5(true); }
            if ("myms".equalsIgnoreCase(autocheck)) { nc.setMyMs(true); }
            if ("myhs".equalsIgnoreCase(autocheck)) { nc.setMyHs(true); }
        }

        if (nc.getEmail() == null) {
            SessionContext session = SessionContextUtil.getSessionContext(request);
            String email = session.getEmail();
            if (StringUtils.isNotEmpty(email)) {
                nc.setEmail(email);
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
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        if (null == state) {
            // use a default state for setting subscriptions
            state = sessionContext.getStateOrDefault();
        }

        ModelAndView mAndV = new ModelAndView();

        if (user == null) {
            user = new User();
            user.setEmail(email);
            getUserDao().saveUser(user);
            mAndV.getModel().put("newuser", "true");
        }
        PageHelper.setMemberCookie(request, response, user);

        List<Subscription> subscriptions = new ArrayList<Subscription>();

        if (nc.isMyPk()) { addSub(user, state, SubscriptionProduct.MY_PRESCHOOLER, subscriptions); }
        if (nc.isMyk()) { addSub(user, state, SubscriptionProduct.MY_KINDERGARTNER, subscriptions); }
        if (nc.isMy1()) { addSub(user, state, SubscriptionProduct.MY_FIRST_GRADER, subscriptions); }
        if (nc.isMy2()) { addSub(user, state, SubscriptionProduct.MY_SECOND_GRADER, subscriptions); }
        if (nc.isMy3()) { addSub(user, state, SubscriptionProduct.MY_THIRD_GRADER, subscriptions); }
        if (nc.isMy4()) { addSub(user, state, SubscriptionProduct.MY_FOURTH_GRADER, subscriptions); }
        if (nc.isMy5()) { addSub(user, state, SubscriptionProduct.MY_FIFTH_GRADER, subscriptions); }
        if (nc.isMyMs()) { addSub(user, state, SubscriptionProduct.MY_MS, subscriptions); }
        if (nc.isMyHs()) { addSub(user, state, SubscriptionProduct.MY_HS, subscriptions); }
        if (nc.isGn()) { addSub(user, state, SubscriptionProduct.PARENT_ADVISOR, subscriptions); }
        if (nc.isSponsor()) { addSub(user, state, SubscriptionProduct.SPONSOR_OPT_IN, subscriptions); }

        NewSubscriberDetector.notifyOmnitureWhenNewNewsLetterSubscriber(user, request, response);
        getSubscriptionDao().addNewsletterSubscriptions(user, subscriptions);

        mAndV.setViewName(getSuccessView());
        if (null != nc.getState()) {
            // do not add state to model (which sets the cookie) if the state
            // was not there before.
            mAndV.getModel().put("state", nc.getState());
        }
        mAndV.getModel().put("email", email);
        mAndV.getModel().put("schoolId", String.valueOf(nc.getSchoolId()));

        if (nc.getSchoolId() == 0) {
            mAndV.getModel().put(SubscriptionSummaryController.PARAM_SHOW_FIND_SCHOOL_LINK, "1");
        }
        return mAndV;
    }

    private void addSub(User u, State state, SubscriptionProduct sp, List<Subscription> subscriptions) {
        Subscription sub = new Subscription();
        sub.setUser(u);
        sub.setProduct(sp);
        sub.setState(state);
        subscriptions.add(sub);
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

    public List<Validator> getOnLoadValidators() {
        return _onLoadValidators;
    }

    public void setOnLoadValidators(List<Validator> onLoadValidators) {
        _onLoadValidators = onLoadValidators;
    }

    public IPropertyDao getPropertyDao() {
        return _propertyDao;
    }

    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
    }
}
