/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SubscriptionSummaryController.java,v 1.16 2007/09/25 17:24:52 aroy Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.IUserDao;
import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.context.SessionContextUtil;
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
 * Backs newsletter subscription hover confirmation page.
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class SubscriptionSummaryController extends SimpleFormController {
    public static final String BEAN_ID = "/community/newsletters/popup/mss/page3.page";
    public static final String COREG_VIEW = "/community/newsletters/popup/mss/coreg";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private ISchoolDao _schoolDao;
    private List<Validator> _onLoadValidators;

    public static final String MODEL_SCHOOL_NAME = "schoolName";
    public static final String MODEL_PARENT_ADVISOR = "parentAdvisor";
    public static final String MODEL_COMMUNITY = "community";
    public static final String MODEL_SPONSOR = "sponsor";
    public static final String MODEL_SET_NTH_GRADER = "setNth";
    public static final String MODEL_SET_MS_HS = "setMsHs";
    public static final String MODEL_EMAIL = "email";
    public static final String MODEL_HAS_FIND_SCHOOL_LINK = "hasFindSchoolLink";
    public static final String PARAM_SHOW_FIND_SCHOOL_LINK = "showFindSchool";
    public static final String MODEL_FIRST_LIST = "firstListSubs";
    public static final String MODEL_FIRST_LIST_SIZE = "firstListSize";
    public static final String MODEL_SECOND_LIST = "secondListSubs";
    public static final String MODEL_SECOND_LIST_SIZE = "secondListSize";

    private String _viewName;

    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {
        NewsletterCommand nc = (NewsletterCommand) command;
        List<Validator> validators = getOnLoadValidators();

        if (validators != null) {
            for (Validator val : validators) {
                if (val.supports(nc.getClass())) {
                    val.validate(nc, errors);
                }
            }
        }
    }

    protected Map coregReferenceData(HttpServletRequest request, Object command, Errors errors) {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put(MODEL_FIRST_LIST_SIZE, 0);
        model.put(MODEL_SECOND_LIST_SIZE, 0);

        User user = SessionContextUtil.getSessionContext(request).getUser();

        if (user == null) {
            _log.warn("Cannot determine user. SessionContext.memberId=" + 
                    SessionContextUtil.getSessionContext(request).getMemberId());
            return model;
        }

        model.put(MODEL_EMAIL, user.getEmail());


        Set<Subscription> subscriptions = user.getSubscriptions();

        if (subscriptions == null) {
            _log.warn("No subscriptions found for user " + user);
            return model;
        }

        List<Subscription> newsletterSubs = new ArrayList<Subscription>();
        for (Subscription sub: subscriptions) {
            if (sub.getProduct().isNewsletter() && !sub.getProduct().equals(SubscriptionProduct.BETA_GROUP)) {
                newsletterSubs.add(sub);
            }
        }

        if (newsletterSubs.size() == 0) {
            _log.warn("No newsletter subscriptions found for user " + user);
            return model;
        }

        // sort the subscriptions
        sortNewsletterSubs(newsletterSubs);
        // split the subscriptions into two equal length lists
        splitList(newsletterSubs, model);

        return model;
    }

    protected void splitList(List<Subscription> newsletterSubs, Map<String, Object> model) {
        if (newsletterSubs == null || newsletterSubs.size() == 0) {
            return;
        }

        List<String> firstListNames = new ArrayList<String>();
        List<String> secondListNames = new ArrayList<String>();

        int size = newsletterSubs.size();

        int firstListSize = (size / 2) + (size % 2); // odd number goes to first list

        for (int x=0; x < firstListSize; x++) {
            Subscription sub = newsletterSubs.get(x);
            if (sub.getProduct().equals(SubscriptionProduct.MYSTAT)) {
                School school = getSchoolDao().getSchoolById(sub.getState(), sub.getSchoolId());
                if (school != null) {
                    firstListNames.add("Monthly Stats about " + school.getName());
                }
            } else {
                firstListNames.add(sub.getProduct().getLongName());
            }
        }

        for (int x=firstListSize; x < size; x++) {
            Subscription sub = newsletterSubs.get(x);
            if (sub.getProduct().equals(SubscriptionProduct.MYSTAT)) {
                School school = getSchoolDao().getSchoolById(sub.getState(), sub.getSchoolId());
                if (school != null) {
                    secondListNames.add("Monthly Stats about " + school.getName());
                }
            } else {
                secondListNames.add(sub.getProduct().getLongName());
            }
        }

        model.put(MODEL_FIRST_LIST, firstListNames);
        model.put(MODEL_FIRST_LIST_SIZE, firstListNames.size());
        model.put(MODEL_SECOND_LIST, secondListNames);
        model.put(MODEL_SECOND_LIST_SIZE, secondListNames.size());
    }

    protected void sortNewsletterSubs(List<Subscription> newsletterSubs) {
        // Map of SubscriptionProduct names to their proper ordering
        final Map<SubscriptionProduct, Integer> orderMap = new HashMap<SubscriptionProduct, Integer>();
        int compareOrder = 0;
        orderMap.put(SubscriptionProduct.MYSTAT, compareOrder++);
        orderMap.put(SubscriptionProduct.PARENT_ADVISOR, compareOrder++);
        orderMap.put(SubscriptionProduct.COMMUNITY, compareOrder++);
        orderMap.put(SubscriptionProduct.SPONSOR_OPT_IN, compareOrder++);
        orderMap.put(SubscriptionProduct.MY_KINDERGARTNER, compareOrder++);
        orderMap.put(SubscriptionProduct.MY_FIRST_GRADER, compareOrder++);
        orderMap.put(SubscriptionProduct.MY_SECOND_GRADER, compareOrder++);
        orderMap.put(SubscriptionProduct.MY_THIRD_GRADER, compareOrder++);
        orderMap.put(SubscriptionProduct.MY_FOURTH_GRADER, compareOrder++);
        orderMap.put(SubscriptionProduct.MY_FIFTH_GRADER, compareOrder++);
        orderMap.put(SubscriptionProduct.MY_MS, compareOrder++);
        orderMap.put(SubscriptionProduct.MY_HS, compareOrder++);

        Collections.sort(newsletterSubs, new SubscriptionProductComparator(orderMap));
    }

    public static class SubscriptionProductComparator implements Comparator<gs.data.community.Subscription> {
        private Map<SubscriptionProduct, Integer> _orderMap;

        public SubscriptionProductComparator(Map<SubscriptionProduct, Integer> orderMap) {
            _orderMap = orderMap;
        }
        public int compare(Subscription sub1, Subscription sub2) {
            SubscriptionProduct product1 = sub1.getProduct();
            SubscriptionProduct product2 = sub2.getProduct();
            Integer orderOne = _orderMap.get(product1);
            if (orderOne == null) {
                orderOne = -1;
            }
            Integer orderTwo = _orderMap.get(product2);
            if (orderTwo == null) {
                orderTwo = -1;
            }
            if (orderOne < orderTwo) {
                return -1;
            } else if (orderOne == orderTwo) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) {
        Map<String, Object> model = new HashMap<String, Object>();
        if (COREG_VIEW.equals(getSuccessView())) {
            return coregReferenceData(request, command, errors);
        }

        if (!errors.hasErrors()) {
            NewsletterCommand nc = (NewsletterCommand) command;
            String email = nc.getEmail();
            User user = getUserDao().findUserFromEmailIfExists(email);

            if (user == null) {
                errors.reject("nokey", "User with email " + email + " does not exist");
            } else {
                Set<Subscription> subscriptions = user.getSubscriptions();
                State state = nc.getState();
                if (null == state) {
                    // set a default state for the query below
                    state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
                }

                // Map of SubscriptionProduct names to their proper ordering
                final Map<String, Integer> orderMap = new HashMap<String, Integer> ();
                orderMap.put(SubscriptionProduct.MY_KINDERGARTNER.getLongName(), 0);
                orderMap.put(SubscriptionProduct.MY_FIRST_GRADER.getLongName(), 1);
                orderMap.put(SubscriptionProduct.MY_SECOND_GRADER.getLongName(), 2);
                orderMap.put(SubscriptionProduct.MY_THIRD_GRADER.getLongName(), 3);
                orderMap.put(SubscriptionProduct.MY_FOURTH_GRADER.getLongName(), 4);
                orderMap.put(SubscriptionProduct.MY_FIFTH_GRADER.getLongName(), 5);
                orderMap.put(SubscriptionProduct.MY_MS.getLongName(), 6);
                orderMap.put(SubscriptionProduct.MY_HS.getLongName(), 7);

                Set<String> setNth = new TreeSet<String> (
                        new Comparator<String>() {
                            public int compare(String spOne, String spTwo) {
                                Integer orderOne = orderMap.get(spOne);
                                if (orderOne == null) {
                                    orderOne = -1;
                                }
                                Integer orderTwo = orderMap.get(spTwo);
                                if (orderTwo == null) {
                                    orderTwo = -1;
                                }

                                if (orderOne < orderTwo) {
                                    return -1;
                                } else if (orderOne == orderTwo) {
                                    return 0;
                                } else {
                                    return 1;
                                }
                            }
                        });
                Set<String> setMsHs = new HashSet<String>();

                for (Subscription sub : subscriptions) {
                    SubscriptionProduct sp = sub.getProduct();

                    if (sp.isNewsletter()) {
                        if (sp == SubscriptionProduct.MYSTAT) {
                            if (sub.getSchoolId() == nc.getSchoolId()
                                    && sub.getState() == state) {
                                int schoolId = nc.getSchoolId();
                                School s = getSchoolDao().getSchoolById(state, schoolId);
                                model.put(MODEL_SCHOOL_NAME, s.getName());
                            }

                        } else if (sp == SubscriptionProduct.PARENT_ADVISOR) {
                            model.put(MODEL_PARENT_ADVISOR, sp.getLongName());
                        } else if (sp == SubscriptionProduct.COMMUNITY) {
                            model.put(MODEL_COMMUNITY, sp.getLongName());
                        } else if (sp == SubscriptionProduct.SPONSOR_OPT_IN) {
                            model.put(MODEL_SPONSOR, sp.getLongName());
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
                    model.put(MODEL_HAS_FIND_SCHOOL_LINK, true);
                } else {
                    model.put(MODEL_HAS_FIND_SCHOOL_LINK, false);
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

    public List<Validator> getOnLoadValidators() {
        return _onLoadValidators;
    }

    public void setOnLoadValidators(List<Validator> onLoadValidators) {
        _onLoadValidators = onLoadValidators;
    }
}
