/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SubscriptionSummaryController.java,v 1.18 2007/12/17 18:01:22 aroy Exp $
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
    public static final String MODEL_SET_NTH_MS_HS = "setNthMsHs";
    public static final String MODEL_EMAIL = "email";
    public static final String MODEL_HAS_FIND_SCHOOL_LINK = "hasFindSchoolLink";
    public static final String PARAM_SHOW_FIND_SCHOOL_LINK = "showFindSchool";
    public static final String MODEL_FIRST_LIST = "firstSetSubs";
    public static final String MODEL_FIRST_LIST_SIZE = "firstSetSize";
    public static final String MODEL_SECOND_LIST = "secondSetSubs";
    public static final String MODEL_SECOND_LIST_SIZE = "secondSetSize";

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

    /**
     * Splits newsletterNames into two equal sized lists
     * @param newsletterNames
     * @param model
     */
    protected void splitSet(Set<String> newsletterNames, Map<String, Object> model) {
        if (newsletterNames == null || newsletterNames.size() == 0) {
            return;
        }

        List<String> firstListNames = new ArrayList<String>();
        List<String> secondListNames = new ArrayList<String>();

        int size = newsletterNames.size();

        int firstListSize = (size / 2) + (size % 2); // odd number goes to first list

        Iterator<String> iter = newsletterNames.iterator();

        for (int x=0; x < firstListSize; x++) {
            firstListNames.add(iter.next());
        }

        for (int x=firstListSize; x < size; x++) {
            secondListNames.add(iter.next());
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

    protected Set<String> getOrderedNewsletterSet() {
        final Map<String, Integer> orderMap = new HashMap<String, Integer> ();
        orderMap.put(SubscriptionProduct.MY_KINDERGARTNER.getLongName(), 0);
        orderMap.put(SubscriptionProduct.MY_FIRST_GRADER.getLongName(), 1);
        orderMap.put(SubscriptionProduct.MY_SECOND_GRADER.getLongName(), 2);
        orderMap.put(SubscriptionProduct.MY_THIRD_GRADER.getLongName(), 3);
        orderMap.put(SubscriptionProduct.MY_FOURTH_GRADER.getLongName(), 4);
        orderMap.put(SubscriptionProduct.MY_FIFTH_GRADER.getLongName(), 5);
        orderMap.put(SubscriptionProduct.MY_MS.getLongName(), 6);
        orderMap.put(SubscriptionProduct.MY_HS.getLongName(), 7);
        return new TreeSet<String> (
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
                        } else if (orderOne.equals(orderTwo)) {
                            return 0;
                        } else {
                            return 1;
                        }
                    }
                });
    }

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) {
        Map<String, Object> model = new HashMap<String, Object>();

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

                Set<String> setNthMsHs = getOrderedNewsletterSet();

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
                            setNthMsHs.add(sp.getLongName());
                        } else if (sp == SubscriptionProduct.CITY_COMMUNITY
                                || sp == SubscriptionProduct.SCHOOL_COMMUNITY) {
                            // ignore
                        } else {
                            setNthMsHs.add(sp.getLongName());
                        }
                    }
                }

                splitSet(setNthMsHs, model);

                model.put(MODEL_SET_NTH_MS_HS, setNthMsHs);
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
