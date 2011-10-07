package gs.web.email;

import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.geo.City;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteController;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class NewslettersSignUpController extends SimpleFormController implements ReadWriteController {
    private String _viewName;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private ISchoolDao _schoolDao;
    protected final Log _log = LogFactory.getLog(getClass());


    protected void onBindOnNewForm(HttpServletRequest request, Object o) throws Exception {
        SessionContext sc = SessionContextUtil.getSessionContext(request);
        //first try to get user from email in url parameter
        User user = getUserDao().findUserFromEmailIfExists(request.getParameter("email"));

        //if email not in parameter, check if the user is signed in
        if(user == null){
            if(PageHelper.isMemberAuthorized(request)){
                user = sc.getUser();
            }
        }
        if(user == null){
            return;
        }
        NewslettersSignUpCommand command = (NewslettersSignUpCommand) o;
        command.setUserId(user.getId());
        command.setEmail(user.getEmail());
        City city = new City();
        city.setName("My city is not listed");
        List<City> cities = new ArrayList<City>();
        cities.add(0, city);
        command.setCityList(cities);
    }

    protected ModelAndView showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            BindException be)
            throws Exception {
        NewslettersSignUpCommand command =  (NewslettersSignUpCommand) be.getTarget();

            Map<String, Object> model = new HashMap<String, Object>();
            model.put(getCommandName(), command);

            return new ModelAndView(_viewName,model);

    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException be) throws Exception {
        NewslettersSignUpCommand command = (NewslettersSignUpCommand)o;
        User user = getUserDao().findUserFromId(command.getUserId());
        List<Subscription> subscriptions = new ArrayList<Subscription>();
             doMySchoolStats(user, command, subscriptions, request);
         Map<String, Object> model = new HashMap<String, Object>();
        model.put(getCommandName(), command);
          ThreadLocalTransactionManager.commitOrRollback();
        _subscriptionDao.addNewsletterSubscriptions(user,subscriptions);
        return new ModelAndView(_viewName,model);
    }

    protected void doMySchoolStats(User user, NewslettersSignUpCommand command, List<Subscription> newSubscriptions,
                                   HttpServletRequest request) {
        List<Subscription> previousMyStatsSubs = _subscriptionDao.findMssSubscriptionsByUser(user);
        Set<String> stateIdStringSetFromDB = new HashSet<String>(previousMyStatsSubs.size());
        Map<String, Subscription> stringToSubscriptionMap =
                new HashMap<String, Subscription>(previousMyStatsSubs.size());
        for (Subscription sub: previousMyStatsSubs) {
            String key = sub.getState().getAbbreviation() + sub.getSchoolId();
            stateIdStringSetFromDB.add(key);
            stringToSubscriptionMap.put(key, sub);
        }

        // pull out the schools represented on the page
        String[] stateIdStringsFromPage = request.getParameterValues("uniqueStateId");
        Set<String> stateIdStringSetFromPage = new HashSet<String>();
        if (stateIdStringsFromPage != null) {
            stateIdStringSetFromPage.addAll(Arrays.asList(stateIdStringsFromPage));
            // get rid of entry from template
            stateIdStringSetFromPage.remove("REPLACE-ME");
        }

        // any schools in the existing subscription set that is not represented, remove it
        for (String stateIdStringFromDB: stateIdStringSetFromDB) {
            if (!stateIdStringSetFromPage.contains(stateIdStringFromDB)) {
                _subscriptionDao.removeSubscription(stringToSubscriptionMap.get(stateIdStringFromDB).getId());
            }
        }
        // any school on the page that isn't in the existing set, add it
        int counter = 1;
        // reset messages
        command.setSchool1(false);
        command.setSchool2(false);
        command.setSchool3(false);
        command.setSchool4(false);
        for (String stateIdStringFromPage: stateIdStringSetFromPage) {
            State stateToAdd;
            Integer schoolIdToAdd;
            try {
                stateToAdd = State.fromString(stateIdStringFromPage.substring(0, 2));
                schoolIdToAdd = new Integer(stateIdStringFromPage.substring(2));
                if (schoolIdToAdd < 1) {
                    _log.warn("Invalid school id from page: " + schoolIdToAdd);
                    continue;
                }
            } catch (Exception e) {
                _log.warn("Failed to convert mss from page: " + e, e);
                continue;
            }
            School newSchool = _schoolDao.getSchoolById(stateToAdd, schoolIdToAdd);
            if (newSchool == null) {
                _log.warn("Failed to find school from page: " + stateToAdd + ":" + schoolIdToAdd);
                continue;
            }
            // update command for messages
            if (counter == 1) {
                command.setSchool1(true);
                command.setName1(newSchool.getName());
            } else if (counter == 2) {
                command.setSchool2(true);
                command.setName2(newSchool.getName());
            } else if (counter == 3) {
                command.setSchool3(true);
                command.setName3(newSchool.getName());
            } else if (counter == 4) {
                command.setSchool4(true);
                command.setName4(newSchool.getName());
            } else {
                _log.warn("Too many schools on page.");
                break;
            }
            if (!stateIdStringSetFromDB.contains(stateIdStringFromPage)) {
                Subscription s = new Subscription(user, SubscriptionProduct.MYSTAT, stateToAdd);
                s.setSchoolId(schoolIdToAdd);
                newSubscriptions.add(s);
            }
            counter++;
        }
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
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

}
