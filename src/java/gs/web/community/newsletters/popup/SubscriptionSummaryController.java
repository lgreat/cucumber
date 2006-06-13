/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SubscriptionSummaryController.java,v 1.8 2006/06/13 22:11:23 dlee Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.IUserDao;
import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class SubscriptionSummaryController extends SimpleFormController {
    public static final String BEAN_ID = "/community/newsletters/popup/mss/page3.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private ISchoolDao _schoolDao;
    private List _onLoadValidators;

    public static final String MODEL_SCHOOL_NAME = "schoolName";
    public static final String MODEL_PARENT_ADVISOR = "parentAdvisor";
    public static final String MODEL_SET_NTH_GRADER = "setNth";
    public static final String MODEL_SET_MS_HS = "setMsHs";
    public static final String MODEL_EMAIL = "email";
    public static final String MODEL_HAS_FIND_SCHOOL_LINK = "hasFindSchoolLink";
    public static final String PARAM_SHOW_FIND_SCHOOL_LINK = "showFindSchool";

    private String _viewName;

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
    }

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) {
        Map model = new HashMap();

        if (!errors.hasErrors()) {
            NewsletterCommand nc = (NewsletterCommand) command;
            String email = nc.getEmail();
            User user = getUserDao().findUserFromEmailIfExists(email);

            if (user == null) {
                errors.reject("nokey", "User with email " + email + " does not exist");
            } else {
                Set subcriptions = user.getSubscriptions();
                State state = nc.getState();

                Set setNth = new HashSet();
                Set setMsHs = new HashSet();

                for (Iterator iter = subcriptions.iterator(); iter.hasNext();) {
                    Subscription sub = (Subscription) iter.next();
                    SubscriptionProduct sp = sub.getProduct();

                    if (sp.isNewsletter()) {
                        if (sp == SubscriptionProduct.MYSTAT ) {
                            if (sub.getSchoolId() == nc.getSchoolId()
                                && sub.getState() == state) {
                                int schoolId = nc.getSchoolId();
                                School s = getSchoolDao().getSchoolById(state, new Integer(schoolId));
                                model.put(MODEL_SCHOOL_NAME, s.getName());
                            }

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
                model.put(MODEL_EMAIL, email);

                if (!StringUtils.isEmpty(request.getParameter(PARAM_SHOW_FIND_SCHOOL_LINK))) {
                    model.put(MODEL_HAS_FIND_SCHOOL_LINK, Boolean.valueOf(true));
                } else {
                    model.put(MODEL_HAS_FIND_SCHOOL_LINK, Boolean.valueOf(false));
                }
            }
        }
        model.put(getCommandName(),command);
        return model;
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

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public List getOnLoadValidators() {
        return _onLoadValidators;
    }

    public void setOnLoadValidators(List onLoadValidators) {
        _onLoadValidators = onLoadValidators;
    }
}
