package gs.web.email;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
 
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;
import gs.data.school.School;
import gs.data.school.ISchoolDao;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.lang.reflect.Method;

/**
 * Controller backing the email management page.
 */
public class ManagementController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private StateManager _stateManager;

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

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    @Override
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
        ManagementCommand command = (ManagementCommand) o;
        command.setUserId(user.getId());
        List<Subscription> subscriptions = _subscriptionDao.getUserSubscriptions(user);
        if(subscriptions != null){
            for (Object subscription : subscriptions) {
                Subscription s = (Subscription) subscription;
                if(s.getProduct().equals(SubscriptionProduct.PARENT_ADVISOR)){
                    command.setGreatnewsId(s.getId());
                    command.setGreatnews(true);
                }else if(s.getProduct().getName().startsWith("summer")){
                    Calendar cal = Calendar.getInstance();
                    if(s.getExpires().after(cal.getTime())){
                        command.setStartweek(s.getProduct().getName());
                        command.setSeasonalId(s.getId());
                        command.setSeasonal(true);
                    }
                }else if(s.getProduct().equals(SubscriptionProduct.LEARNING_DIFFERENCES)){
                    command.setLearning_dis(true);
                    command.setLearning_disId(s.getId());
                }else if(s.getProduct().equals(SubscriptionProduct.SCHOOL_CHOOSER_PACK_PRESCHOOL)){
                    command.setChooser(true);
                    command.setChooserpack_p(true);
                    command.setChooserpack_pId(s.getId());
                }else if(s.getProduct().equals(SubscriptionProduct.SCHOOL_CHOOSER_PACK_ELEMENTARY)){
                    command.setChooser(true);
                    command.setChooserpack_e(true);
                    command.setChooserpack_eId(s.getId());
                }else if(s.getProduct().equals(SubscriptionProduct.SCHOOL_CHOOSER_PACK_MIDDLE)){
                    command.setChooser(true);
                    command.setChooserpack_m(true);
                    command.setChooserpack_mId(s.getId());
                }else if(s.getProduct().equals(SubscriptionProduct.SCHOOL_CHOOSER_PACK_HIGH)){
                    command.setChooser(true);
                    command.setChooserpack_h(true);
                    command.setChooserpack_hId(s.getId());
                }else if(s.getProduct().equals(SubscriptionProduct.SPONSOR_OPT_IN)){
                    command.setSponsor(true);
                    command.setSponsorId(s.getId());
                }
            }
        }

        doMyNthList(user,command);


        checkRequestParams(request, command);

        if (request.getParameter("schoolId") != null && request.getParameter("schoolState") != null) {
            Integer id = Integer.valueOf(request.getParameter("schoolId"));
            State state = State.fromString(request.getParameter("schoolState"));
            School school = null;
            try {
                school = _schoolDao.getSchoolById(state, id);
            } catch (ObjectRetrievalFailureException orfe) {
                // nothing
            }
            // verify that this school exists
            if (school != null) {
                List<Subscription> myStats = _subscriptionDao.findMssSubscriptionsByUser(user);
                if (myStats != null && myStats.size() >= 4) {
                    // error!
                    command.setTooManySchoolsError(true);
                } else {
                    List<Subscription> newMssSub = new ArrayList<Subscription>(1);
                    Subscription s = new Subscription(user,SubscriptionProduct.MYSTAT, state);
                    s.setSchoolId(id);
                    newMssSub.add(s);
                    _subscriptionDao.addNewsletterSubscriptions(user, newMssSub);
                    ThreadLocalTransactionManager.commitOrRollback();
                }
            }
        }

        List<School> schools = new ArrayList<School>();
        List<Subscription> myStats = new ArrayList<Subscription>(_subscriptionDao.findMssSubscriptionsByUser(user));
        List<Integer> myStatIds = new ArrayList<Integer>();

        int myStatNum = 1;
        for (Subscription myStat: myStats ) {
            School school = _schoolDao.getSchoolById(myStat.getState(), myStat.getSchoolId());
            schools.add(school);
            myStatIds.add(myStat.getId());
            Class dummyClass = Class.forName("gs.web.email.ManagementCommand");

            Method meth = dummyClass.getMethod("setSchool" + myStatNum,Boolean.TYPE);
            Method meth2 = dummyClass.getMethod("setName" + myStatNum,String.class);
            meth.invoke(command,true);
            meth2.invoke(command,school.getName());
            myStatNum++;
        }
        command.setMyStatIds(myStatIds);
        command.setCurrentMySchoolStats(schools);
        if(!(command.getSchool() > 0)){
            command.setSchool(0);
        }

        List<City> cities = _geoDao.findAllCitiesByState(sc.getStateOrDefault());
        City city = new City();
        city.setName("My city is not listed");
        cities.add(0, city);
        command.setCityList(cities);
    }

    @Override
    protected ModelAndView showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            BindException be)
            throws Exception {
        ManagementCommand command =  (ManagementCommand) be.getTarget();
        if(command.getUserId() == 0){
            return new ModelAndView("redirect:/community/loginOrRegister.page?redirect=/email/management.page");
        }else{
            Map<String, Object> model = new HashMap<String, Object>();
            model.put(getCommandName(), command);
            User user = new User();
            user.setId(command.getUserId());
            PageHelper.setMemberCookie(request, response, user);
            return new ModelAndView("email/management",model);
        }
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException be) throws Exception {
        ManagementCommand command = (ManagementCommand)o;
        User user = getUserDao().findUserFromId(command.getUserId());
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        State state = user.getState();
        if(user.getUserProfile() != null){
            state = user.getUserProfile().getState();
        }
        if (state == null) {
            state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        }
        if(command.getGreatnews() && !(command.getGreatnewsId() > 0)){
            Subscription s = new Subscription(user,SubscriptionProduct.PARENT_ADVISOR, state);
            subscriptions.add(s);
        }
        if((!(command.getGreatnews())) && command.getGreatnewsId() > 0){
            _subscriptionDao.removeSubscription(command.getGreatnewsId());
        }

        //my nth grader
        submitMyNthList(user,command,state);

        //see if we need to delete any my school stats
        if(command.getId1() >0 && !command.isSchool1()){
            _subscriptionDao.removeSubscription(command.getId1());
        }
        if(command.getId2() >0 && !command.isSchool2()){
            _subscriptionDao.removeSubscription(command.getId2());
        }
        if(command.getId3() >0 && !command.isSchool3()){
            _subscriptionDao.removeSubscription(command.getId3());
        }
        if(command.getId4() >0 && !command.isSchool4()){
            _subscriptionDao.removeSubscription(command.getId4());
        }

        if(command.getSchool() >0){
            if(command.getSchool() == command.getId1()
                    || command.getSchool() == command.getId2()
                    || command.getSchool() == command.getId3()
                    ){
            }else{
                Subscription s = new Subscription(user,SubscriptionProduct.MYSTAT, command.getStateAdd());
                s.setSchoolId(command.getSchool());
                subscriptions.add(s);
                School school = _schoolDao.getSchoolById(command.getStateAdd(),command.getSchool());
                command.setSchoolName(school.getName());
            }

        }

        if(command.isSeasonal()){
            if(command.getSeasonalId() > 0){
                if(_subscriptionDao.findSummerSubscriptions(user).size() != 0 &&!_subscriptionDao.findSummerSubscriptions(user).get(0).getProduct().equals(SubscriptionProduct.getSubscriptionProduct(command.getStartweek()))){
                     _subscriptionDao.updateSeasonal(command.getStartweek(),user);
                }
            }
            else if(_subscriptionDao.findSummerSubscriptions(user).size() == 0){
                     _subscriptionDao.addSeasonal(command.getStartweek(),user,state);
                }
        }
        
        if(command.getSeasonalId() >0 && !command.isSeasonal()){
            _subscriptionDao.removeSubscription(command.getSeasonalId());
        }

        if((!(command.getLearning_disId() >0)) && command.isLearning_dis()){
            Subscription s = new Subscription(user,SubscriptionProduct.LEARNING_DIFFERENCES, state);
            subscriptions.add(s);
        }
        if(command.getLearning_disId() >0 && !command.isLearning_dis()){
            _subscriptionDao.removeSubscription(command.getLearning_disId());
        }


        if((!(command.getChooserpack_pId() >0)) && command.isChooserpack_p()){
            Subscription s = new Subscription(user,SubscriptionProduct.SCHOOL_CHOOSER_PACK_PRESCHOOL, state);
            _subscriptionDao.saveSubscription(s);
        }
        if(command.getChooserpack_pId() >0 && !command.isChooserpack_p()){
            _subscriptionDao.removeSubscription(command.getChooserpack_pId());
        }

        if((!(command.getChooserpack_eId() >0)) && command.isChooserpack_e()){
            Subscription s = new Subscription(user,SubscriptionProduct.SCHOOL_CHOOSER_PACK_ELEMENTARY, state);
            _subscriptionDao.saveSubscription(s);
        }
        if(command.getChooserpack_eId() >0 && !command.isChooserpack_e()){
            _subscriptionDao.removeSubscription(command.getChooserpack_eId());
        }

        if((!(command.getChooserpack_mId() >0)) && command.isChooserpack_m()){
            Subscription s = new Subscription(user,SubscriptionProduct.SCHOOL_CHOOSER_PACK_MIDDLE, state);
            _subscriptionDao.saveSubscription(s);
        }
        if(command.getChooserpack_mId() >0 && !command.isChooserpack_m()){
            _subscriptionDao.removeSubscription(command.getChooserpack_mId());
        }

        if((!(command.getChooserpack_hId() >0)) && command.isChooserpack_h()){
            Subscription s = new Subscription(user,SubscriptionProduct.SCHOOL_CHOOSER_PACK_HIGH, state);
            _subscriptionDao.saveSubscription(s);
        }
        if(command.getChooserpack_hId() >0 && !command.isChooserpack_h()){
            _subscriptionDao.removeSubscription(command.getChooserpack_hId());
        }

        if((!(command.getSponsorId() >0)) && command.isSponsor()){
            Subscription s = new Subscription(user,SubscriptionProduct.SPONSOR_OPT_IN, state);
            subscriptions.add(s);
        }
        if(command.getSponsorId() >0 && !command.isSponsor()){
            _subscriptionDao.removeSubscription(command.getSponsorId());
        }

        List<String> messages = new ArrayList<String>();
        updateMessages(command, messages);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(getCommandName(), command);
        model.put("messages",messages);

        _subscriptionDao.addNewsletterSubscriptions(user,subscriptions);

        return new ModelAndView(getSuccessView(), model);
    }

    public void addMyNth(SubscriptionProduct sp,User user,State state){
        Student student = new Student();
        student.setSchoolId(-1);
        student.setGrade(sp.getGrade());
        student.setState(state);
        user.addStudent(student);
        _userDao.saveUser(user);

    }

    protected void updateMessages(ManagementCommand command, List<String> messages) {
        messages.clear();
        if (command.getGreatnews()) {
            messages.add("GreatSchools Weekly");
        }
        if (command.isSchool1()) {
            messages.add("School updates for " + command.getName1());
        }
        if (command.isSchool2()) {
            messages.add("School updates for " + command.getName2());
        }
        if (command.isSchool3()) {
            messages.add("School updates for " + command.getName3());
        }
        if (command.isSchool4()) {
            messages.add("School updates for " + command.getName4());
        }
        if (command.isSchool4()) {
            messages.add("School updates for " + command.getName4());
        }
        if (!StringUtils.isBlank(command.getSchoolName())) {
            messages.add("School updates for " + command.getSchoolName());
        }
        if (command.isSeasonal()) {
            messages.add("Summer Tips and Activities to Prevent Brain Drain");
        }
        if (command.isLearning_dis()) {
            messages.add("Helping Kids With Learning Disabilities");
        }
        if (command.isChooserpack_p()) {
            messages.add("Preschool Tip Sheet");
        }
        if (command.isChooserpack_e()) {
            messages.add("Elementary Tip Sheet");
        }
        if (command.isChooserpack_m()) {
            messages.add("Middle School Tip Sheet");
        }
        if (command.isChooserpack_h()) {
            messages.add("High School Tip Sheet");
        }
        if (command.isSponsor()) {
            messages.add("Valuable offers and information from GreatSchools' partners");
        }
    }

    public void submitMyNthList(User user,
                                ManagementCommand command,
                                State state){
        //foreach possible subscription, check if it needs to be added or deleted
        for(SubscriptionProduct myNth : SubscriptionProduct.MY_NTH_GRADER){
            if(command.checkedBox(myNth) && !(command.getMyNthId(myNth) > 0)){
                addMyNth(myNth,user,state);
            }
            if((!command.checkedBox(myNth)) && command.getMyNthId(myNth) > 0){
                _subscriptionDao.removeStudent(command.getMyNthId(myNth));
            }
        }
    }

    public void doMyNthList(User user,ManagementCommand command){
        //store existing My Nth Grade subscriptions in myNthMap
        List<Student> mynthSubscriptions = _subscriptionDao.findMynthSubscriptionsByUser(user);
        Map<SubscriptionProduct, Integer> myNthMap = new HashMap<SubscriptionProduct, Integer>();
        for(Student myNth : mynthSubscriptions){
            myNthMap.put(myNth.getMyNth(),myNth.getId());
        }

        //set checkboxes and hidden ids to correct values
        for(SubscriptionProduct myNth : SubscriptionProduct.MY_NTH_GRADER){
            if(myNthMap.get(myNth) == null){continue;}
            Integer mnId = myNthMap.get(myNth);
            if(myNth.equals(SubscriptionProduct.MY_PRESCHOOLER)){
                command.setMypk(true);
                command.setMypkId(mnId);
            }
            else if(myNth.equals(SubscriptionProduct.MY_KINDERGARTNER)){
                command.setMyk(true);
                command.setMykId(mnId);
            }
            else if(myNth.equals(SubscriptionProduct.MY_FIRST_GRADER)){
                command.setMy1(true);
                command.setMy1Id(mnId);
            }
            else if(myNth.equals(SubscriptionProduct.MY_SECOND_GRADER)){
                command.setMy2(true);
                command.setMy2Id(mnId);
            }
            else if(myNth.equals(SubscriptionProduct.MY_THIRD_GRADER)){
                command.setMy3(true);
                command.setMy3Id(mnId);
            }
            else if(myNth.equals(SubscriptionProduct.MY_FOURTH_GRADER)){
                command.setMy4(true);
                command.setMy4Id(mnId);
            }
            else if(myNth.equals(SubscriptionProduct.MY_FIFTH_GRADER)){
                command.setMy5(true);
                command.setMy5Id(mnId);
            }
            else if(myNth.equals(SubscriptionProduct.MY_MS)){
                command.setMyms(true);
                command.setMymsId(mnId);
            }
            else if(myNth.equals(SubscriptionProduct.MY_HS)){
                command.setMyhs(true);
                command.setMyhsId(mnId);
            }
        }
    }

    protected void checkRequestParams(HttpServletRequest request, ManagementCommand command) {
        for(SubscriptionProduct myNth : SubscriptionProduct.MY_NTH_GRADER){
            if (request.getParameter("set" + myNth.getName()) == null) {
                continue;
            }
             // this ensures that if a request param sets an nth newsletter to checked, the
            // greatnews will also be checked
            command.setGreatnews(true);
            if(myNth.equals(SubscriptionProduct.MY_PRESCHOOLER)){
                command.setMypk(true);
            }
            else if(myNth.equals(SubscriptionProduct.MY_KINDERGARTNER)){
                command.setMyk(true);
            }
            else if(myNth.equals(SubscriptionProduct.MY_FIRST_GRADER)){
                command.setMy1(true);
            }
            else if(myNth.equals(SubscriptionProduct.MY_SECOND_GRADER)){
                command.setMy2(true);
            }
            else if(myNth.equals(SubscriptionProduct.MY_THIRD_GRADER)){
                command.setMy3(true);
            }
            else if(myNth.equals(SubscriptionProduct.MY_FOURTH_GRADER)){
                command.setMy4(true);
            }
            else if(myNth.equals(SubscriptionProduct.MY_FIFTH_GRADER)){
                command.setMy5(true);
            }
            else if(myNth.equals(SubscriptionProduct.MY_MS)){
                command.setMyms(true);
            }
            else if(myNth.equals(SubscriptionProduct.MY_HS)){
                command.setMyhs(true);
            }
        }
    }
}
