/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SubscriptionSummaryController.java,v 1.1 2006/05/04 00:25:45 dlee Exp $
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
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class SubscriptionSummaryController extends AbstractCommandController {
    public static final String BEAN_ID = "/community/newsletters/popup/mss/page3.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private ISchoolDao _schoolDao;

    private static final String MODEL_SCHOOL_NAME = "schoolName";
    private static final String MODEL_PARENT_ADVISOR = "parentAdvisor";
    private static final String MODEL_SET_NTH_GRADER = "setNth";
    private static final String MODEL_SET_MS_HS = "setMsHs";
    private static final String MODEL_EMAIL = "email";


    private String _viewName;

    public SubscriptionSummaryController()
    {
        setCommandClass(NewsletterCommand.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException bindException) throws Exception {
        Map model = new HashMap();
        Set setNth = new HashSet();
        Set setMsHs = new HashSet();


        NewsletterCommand nc = (NewsletterCommand) command;
        ISessionFacade context = SessionFacade.getInstance(request);
        State state = context.getState();
        String email = nc.getEmail();
        List subs = null;

        User user = _userDao.getUserFromEmail(email);
        if (user == null) {

        }

        Set subcriptions = user.getSubscriptions();
        for (Iterator iter = subcriptions.iterator(); iter.hasNext();) {
            Subscription sub = (Subscription) iter.next();
            SubscriptionProduct sp = sub.getProduct();
            if (sp.isNewsletter()) {

                if (sp == SubscriptionProduct.MYSTAT
                        && sub.getSchoolId() == nc.getSchoolId()
                        && sub.getState() == state) {
                    int schoolId = nc.getSchoolId();
                    School s = _schoolDao.getSchoolById(state, new Integer(schoolId));
                    model.put(MODEL_SCHOOL_NAME, s.getName());

                } else if (sp == SubscriptionProduct.PARENT_ADVISOR) {
                    model.put(MODEL_PARENT_ADVISOR, sp.getLongName());

                } else if (sp == SubscriptionProduct.MY_MS
                        || sp == SubscriptionProduct.MY_HS) {
                    setMsHs.add(sp.getLongName());
                } else {
                    setNth.add(sp.getLongName());
                }
            }
        }
        model.put(MODEL_SET_MS_HS, setMsHs);
        model.put(MODEL_SET_NTH_GRADER, setNth);
        model.put(MODEL_EMAIL,email);

        return new ModelAndView(_viewName, model);
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

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
