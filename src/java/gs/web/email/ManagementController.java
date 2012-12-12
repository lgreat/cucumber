package gs.web.email;

import gs.web.community.registration.AccountInformationCommand;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import gs.data.integration.exacttarget.ExactTargetAPI;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.lang.reflect.Method;

/**
 * Controller backing the email management page.
 */
public class ManagementController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String BEAN_ID = "/email/management.page";

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private StateManager _stateManager;
    private IUserContentDao _userContentDao;
    private IDiscussionDao _discussionDao;

    public static final int MAX_MSS_SUBSCRIPTIONS = 4;

    public IDiscussionDao getDiscussionDao() {
        return _discussionDao;
    }

    public void setDiscussionDao(IDiscussionDao discussionDao) {
        _discussionDao = discussionDao;
    }

    public IUserContentDao getUserContentDao() {
        return _userContentDao;
    }

    public void setUserContentDao(IUserContentDao userContentDao) {
        _userContentDao = userContentDao;
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

        //then try from userId parameter
        if(user == null && request.getParameter("ref") != null){
            Integer userId = new Integer(request.getParameter("ref"));
            if(userId.intValue() > 0){
                user = getUserDao().findUserFromId(userId.intValue());
            }
        }

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
        command.setEmail(user.getEmail());
        command.setFirstName(user.getFirstName());

        if ("unsubscribe".equals(request.getParameter("action"))) {
            String ei = request.getParameter("ei");
            if (ei == null && user.getNotifyAboutReplies()) {
                _log.info("User " + user.getId() + " unsubscribed from all replies to community posts");
                user.setNotifyAboutReplies(false);
                _userDao.saveUser(user);
                command.setUnsubCommunityNotificationsForAllPosts(true);
            } else if (ei != null && StringUtils.isNumeric(ei)) {
                try {
                    int entityId = Integer.parseInt(ei);
                    Discussion discussion = _discussionDao.findById(entityId);
                    if (discussion != null && discussion.isNotifyAuthorAboutReplies() && discussion.getAuthorId().equals(user.getId())) {
                        _log.info("User " + user.getId() + " unsubscribed from replies to community posts about user content id " + discussion.getId());
                        _userContentDao.doNotNotifyAuthorAboutReplies(user.getId(), discussion.getId());
                        command.setUnsubPostTitle(discussion.getTitle());
                    }
                } catch (Exception e) {
                    // no need to do anything, not important
                }
            }
        }

        command.setRepliesToCommunityPosts(user.getNotifyAboutReplies());

        // your location
        State userState;
        if(user.getUserProfile() != null){
            userState = user.getUserProfile().getState();
            command.setUserCity(user.getUserProfile().getCity());
        }else{
            userState = user.getState();
            command.setUserCity(user.getTown());
        }
        command.setUserState(userState);
        List<City> userCities = _geoDao.findAllCitiesByState(userState);
        City userCity = new City();
        userCity.setName("My city is not listed");
        userCities.add(0, userCity);
        command.setUserCityList(userCities);

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
                }else if(s.getProduct().equals(SubscriptionProduct.DAILY_TIP)){
                    command.setDailytip(true);
                    command.setDailytipId(s.getId());
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
                }else if(s.getProduct().equals(SubscriptionProduct.PLEDGE)){
                    command.setPledge(true);
                    command.setPledgeId(s.getId());
                }else if (s.getProduct().equals(SubscriptionProduct.BTSTIP_E)) {
                    command.setBtsTip(true);
                    command.setBtsTipVersion("e");
                    command.setBtsTip_eId(s.getId());
                }else if (s.getProduct().equals(SubscriptionProduct.BTSTIP_M)) {
                    command.setBtsTip(true);
                    command.setBtsTipVersion("m");
                    command.setBtsTip_mId(s.getId());
                }else if (s.getProduct().equals(SubscriptionProduct.BTSTIP_H)) {
                    command.setBtsTip(true);
                    command.setBtsTipVersion("h");
                    command.setBtsTip_hId(s.getId());
                }
            }
        }

        // set default version of BTS tip to elementary
        if (command.getBtsTipVersion() == null) {
            command.setBtsTipVersion("e");
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
                if (myStats != null && myStats.size() >= MAX_MSS_SUBSCRIPTIONS) {
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

        for (int myStatNum = 1; myStatNum <= myStats.size() && myStatNum <= MAX_MSS_SUBSCRIPTIONS; myStatNum++) {
            Subscription myStat = myStats.get(myStatNum-1);
            try{
                School school;
                school = _schoolDao.getSchoolById(myStat.getState(), myStat.getSchoolId());
                schools.add(school);
                myStatIds.add(myStat.getId());
                Class dummyClass = Class.forName("gs.web.email.ManagementCommand");

                Method meth = dummyClass.getMethod("setSchool" + myStatNum,Boolean.TYPE);
                Method meth2 = dummyClass.getMethod("setName" + myStatNum,String.class);
                meth.invoke(command,true);
                meth2.invoke(command,school.getName());
            }catch(ObjectRetrievalFailureException orfe){
                    // ain't doin' nathan cos i don't care about schools that don't exist
            }
        }
        command.setMyStatIds(myStatIds);
        command.setCurrentMySchoolStats(schools);
        if(!(command.getSchool() > 0)){
            command.setSchool(0);
        }

        List<City> cities = new ArrayList<City>();
        if (command.getUserState() != null) {
            cities = _geoDao.findAllCitiesByState(command.getUserState());
        }
        City city = new City();
        city.setName("My city is not listed");  //e
        cities.add(0, city);
        command.setCityList(cities);
        command.setStateAdd(command.getUserState());
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

//        user.setFirstName(command.getFirstName());

        // replies to community posts
        boolean newNotifyAboutReplies = command.isRepliesToCommunityPosts();
        boolean oldNotifyAboutReplies = user.getNotifyAboutReplies();
        user.setNotifyAboutReplies(newNotifyAboutReplies);
        if (!oldNotifyAboutReplies && newNotifyAboutReplies) {
            // turn notifications back on for all discussions authored by this user
            _userContentDao.resetNotifyAuthorAboutReplies(user.getId());
        }

        // save first name and notifyAboutReplies for user
        _userDao.saveUser(user);

        List<Subscription> subscriptions = new ArrayList<Subscription>();
        State state = user.getState();
        if(user.getUserProfile() != null){
            // your location
            user.getUserProfile().setState(command.getUserState());
            user.getUserProfile().setCity(command.getUserCity());
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

        // mystats
        doMySchoolStats(user, command, subscriptions, request);

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

        if((!(command.getDailytipId() >0)) && command.isDailytip()){
            Subscription s = new Subscription(user,SubscriptionProduct.DAILY_TIP, state);
            subscriptions.add(s);
        }
        if(command.getDailytipId() >0 && !command.isDailytip()){
            _subscriptionDao.removeSubscription(command.getDailytipId());
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

        if(command.isBtsTip() && (!(command.getBtsTip_eId() >0)) && "e".equals(command.getBtsTipVersion())){
            Subscription s = new Subscription(user,SubscriptionProduct.BTSTIP_E, state);
            _subscriptionDao.saveSubscription(s);
        }
        if(command.getBtsTip_eId() >0 && (!command.isBtsTip() || !"e".equals(command.getBtsTipVersion()))){
            _subscriptionDao.removeSubscription(command.getBtsTip_eId());
        }

        if(command.isBtsTip() && (!(command.getBtsTip_mId() >0)) && "m".equals(command.getBtsTipVersion())){
            Subscription s = new Subscription(user,SubscriptionProduct.BTSTIP_M, state);
            _subscriptionDao.saveSubscription(s);
        }
        if(command.getBtsTip_mId() >0 && (!command.isBtsTip() || !"m".equals(command.getBtsTipVersion()))){
            _subscriptionDao.removeSubscription(command.getBtsTip_mId());
        }

        if(command.isBtsTip() && (!(command.getBtsTip_hId() >0)) && "h".equals(command.getBtsTipVersion())){
            Subscription s = new Subscription(user,SubscriptionProduct.BTSTIP_H, state);
            _subscriptionDao.saveSubscription(s);
        }
        if(command.getBtsTip_hId() >0 && (!command.isBtsTip() || !"h".equals(command.getBtsTipVersion()))){
            _subscriptionDao.removeSubscription(command.getBtsTip_hId());
        }

        if((!(command.getSponsorId() >0)) && command.isSponsor()){
            Subscription s = new Subscription(user,SubscriptionProduct.SPONSOR_OPT_IN, state);
            subscriptions.add(s);
        }
        if(command.getSponsorId() >0 && !command.isSponsor()){
            _subscriptionDao.removeSubscription(command.getSponsorId());
        }

        if((!(command.getPledgeId() >0)) && command.isPledge()){
            Subscription s = new Subscription(user,SubscriptionProduct.PLEDGE, state);
            subscriptions.add(s);
        }
        if(command.getPledgeId() >0 && !command.isPledge()){
            _subscriptionDao.removeSubscription(command.getPledgeId());
        }

        List<String> messages = new ArrayList<String>();
        updateMessages(command, messages);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(getCommandName(), command);
        model.put("messages",messages);

        _subscriptionDao.addNewsletterSubscriptions(user,subscriptions);

        // DELETE USER FROM EXACT TARGET IF NO SUBSCRIPTIONS
        if(_subscriptionDao.getUserSubscriptions(user) == null){
            ExactTargetAPI _etAPI = (ExactTargetAPI)getApplicationContext().getBean(ExactTargetAPI.BEAN_ID);
            _etAPI.deleteSubscriber(user.getEmail());
        }

        if (user.getUserProfile() != null && PageHelper.isMemberAuthorized(request)) {
            return new ModelAndView(getSuccessView(), model);
        } else {
            return new ModelAndView(new RedirectView(BEAN_ID + "?ref=" + user.getId()));
        }
    }

    protected void doMySchoolStats(User user, ManagementCommand command, List<Subscription> newSubscriptions,
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
                Subscription s = new Subscription(user,SubscriptionProduct.MYSTAT, stateToAdd);
                s.setSchoolId(schoolIdToAdd);
                newSubscriptions.add(s);
            }
            counter++;
        }
    }

    public void addMyNth(SubscriptionProduct sp,User user,State state){
        Student student = new Student();
        student.setSchoolId(-1);
        student.setGrade(sp.getGrade());
        student.setState(state);
        user.addStudent(student);
        _userDao.saveUser(user);

    }

    public void removeMyNth(SubscriptionProduct myNth,User user){
        Set<Student> students = user.getStudents();
        Student studentToDelete = null;
        for (Student student: students) {
            if (student.getGrade() != null && student.getGrade().equals(myNth.getGrade())) {
                studentToDelete = student;
                break;
            }
        }
        if (studentToDelete != null) {
            students.remove(studentToDelete);
            user.setStudents(students);
            _userDao.saveUser(user);
        }
    }

    protected void updateMessages(ManagementCommand command, List<String> messages) {
        messages.clear();
        if (command.getGreatnews()) {
            messages.add("Weekly Newsletter");
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
        if (command.isSeasonal()) {
            messages.add("Summer Tips and Activities to Prevent Brain Drain");
        }
        if (command.isLearning_dis()) {
            messages.add("Helping Kids With Learning Disabilities");
        }
        if (command.isDailytip()) {
            messages.add("Daily Newsletter");
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
        if (command.isPledge()) {
            messages.add("Updates about The GreatSchools Parents Pledge");
        }
        if (command.isBtsTip()) {
            messages.add("Back-to-School Tip of the Day");
        }
    }

    public void submitMyNthList(User user,
                                ManagementCommand command,
                                State state){
        //foreach possible subscription, check if it needs to be added or deleted
        // first delete
        for(SubscriptionProduct myNth : SubscriptionProduct.MY_NTH_GRADER){
            if(command.checkedBox(myNth) && !(command.getMyNthId(myNth) > 0)){
                addMyNth(myNth,user,state);
            }
            if((!command.checkedBox(myNth)) && command.getMyNthId(myNth) > 0){
                removeMyNth(myNth, user);
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
