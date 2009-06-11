package gs.web.email;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: eddie
 * Date: May 22, 2009
 * Time: 12:53:27 PM
 * To change this template use File | Settings | File Templates.
 */


public class ManagementController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao userDao;
    private ISubscriptionDao subscriptionDao;
    private IGeoDao geoDao;
    private ISchoolDao schoolDao;

    public ISubscriptionDao getSubscriptionDao() {
        return subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        this.subscriptionDao = subscriptionDao;
    }

    public IUserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(IUserDao userDao) {
        this.userDao = userDao;
    }

    public ISchoolDao getSchoolDao() {
        return schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        this.schoolDao = schoolDao;
    }

    public IGeoDao getGeoDao() {
        return geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        this.geoDao = geoDao;
    }

    //    @Override
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
        List<Subscription> subscriptions = subscriptionDao.getUserSubscriptions(user);

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

        List<School> schools = new ArrayList();
        List<Subscription> myStats = new ArrayList<Subscription>(subscriptionDao.findMssSubscriptionsByUser(user));
        List myStatIds = new ArrayList();


        int myStatNum = 1;
        for (Subscription myStat: myStats ) {
            School school = schoolDao.getSchoolById(myStat.getState(), myStat.getSchoolId());
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



        List<City> cities = geoDao.findAllCitiesByState(sc.getStateOrDefault());
        City city = new City();
        city.setName("My city is not listed");
        cities.add(0, city);
        command.setCityList(cities);
        _log.warn("doing bindform");


    }

    protected ModelAndView showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            BindException be)
            throws Exception {
        ManagementCommand command =  (ManagementCommand) be.getTarget();
        if(command.getUserId() == 0){
            return new ModelAndView("redirect:/community/loginOrRegister.page");
        }else{
            _log.warn("doing showform");
            //List<Error> errors = new ArrayList<Error>(be.getAllErrors());
            //for(Error e:errors){
            _log.warn(be.getMessage());
            //}
            Map<String, Object> model = new HashMap<String, Object>();
            model.put(getCommandName(), command);
            User user = new User();
            user.setId(command.getUserId());
            PageHelper.setMemberCookie(request, response, user);
            return new ModelAndView("email/management",model);
        }
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,Object o, BindException be) throws Exception {
        _log.warn("doing submit");
        ManagementCommand command = (ManagementCommand)o;
        User user = getUserDao().findUserFromId(command.getUserId());
        List<Subscription> subscriptions = new ArrayList();
        List<String> messages = new ArrayList();
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
            messages.add("added " + s.getProduct().getLongName());
        }
        if((!(command.getGreatnews())) && command.getGreatnewsId() > 0){
            subscriptionDao.removeSubscription(command.getGreatnewsId());
            messages.add("removed parent advisor");
        }

        //my nth grader
        submitMyNthList(user,command,messages,subscriptions,state);

        //see if we need to delete any my school stats
        if(command.getId1() >0 && !command.isSchool1()){
            messages.add(command.getName1() + " has been removed from your school list.");
            subscriptionDao.removeSubscription(command.getId1());
        }
        if(command.getId2() >0 && !command.isSchool2()){
            messages.add(command.getName2() + " has been removed from your school list.");
            subscriptionDao.removeSubscription(command.getId2());
        }
        if(command.getId3() >0 && !command.isSchool3()){
            messages.add(command.getName3() + " has been removed from your school list.");
            subscriptionDao.removeSubscription(command.getId3());
        }
        if(command.getId4() >0 && !command.isSchool4()){
            messages.add(command.getName4() + " has been removed from your school list.");
            subscriptionDao.removeSubscription(command.getId4());
        }

        if(command.getSchool() >0){
            if(command.getSchool() == command.getId1()
                    || command.getSchool() == command.getId2()
                    || command.getSchool() == command.getId3()
                    ){
                messages.add("Did not add school because it is already in your list." );
            }else{
                Subscription s = new Subscription(user,SubscriptionProduct.MYSTAT, command.getStateAdd());
                s.setSchoolId(command.getSchool());
                subscriptions.add(s);
                School school = schoolDao.getSchoolById(command.getStateAdd(),command.getSchool());
                messages.add("added " + s.getProduct().getLongName() + " for " + school.getName());
            }

        }
        _log.warn("doing something else");

        if(command.isSeasonal()){
            if(command.getSeasonalId() > 0){
                subscriptionDao.removeSubscription(command.getSeasonalId());
            }
            subscriptionDao.addSeasonal(command.getStartweek(),user,state);
            messages.add("added summer seasonal newsletter");
        }
        if(command.getSeasonalId() >0 && !command.isSeasonal()){
            messages.add("removed summer seasonal newsletter");
            subscriptionDao.removeSubscription(command.getSeasonalId());
        }

        if((!(command.getLearning_disId() >0)) && command.isLearning_dis()){
            Subscription s = new Subscription(user,SubscriptionProduct.LEARNING_DIFFERENCES, state);
            subscriptions.add(s);
            messages.add("added " + s.getProduct().getLongName());
        }
        if(command.getLearning_disId() >0 && !command.isLearning_dis()){
            messages.add("removed " + SubscriptionProduct.LEARNING_DIFFERENCES.getLongName());
            subscriptionDao.removeSubscription(command.getLearning_disId());
        }


        if((!(command.getChooserpack_pId() >0)) && command.isChooserpack_p()){
            Subscription s = new Subscription(user,SubscriptionProduct.SCHOOL_CHOOSER_PACK_PRESCHOOL, state);
            subscriptionDao.saveSubscription(s);
            messages.add("added " + s.getProduct().getLongName());
        }
        if(command.getChooserpack_pId() >0 && !command.isChooserpack_p()){
            messages.add("removed " + SubscriptionProduct.SCHOOL_CHOOSER_PACK_PRESCHOOL.getLongName());
            subscriptionDao.removeSubscription(command.getChooserpack_pId());
        }

        if((!(command.getChooserpack_eId() >0)) && command.isChooserpack_e()){
            Subscription s = new Subscription(user,SubscriptionProduct.SCHOOL_CHOOSER_PACK_ELEMENTARY, state);
            subscriptionDao.saveSubscription(s);
            messages.add("added " + s.getProduct().getLongName());
        }
        if(command.getChooserpack_eId() >0 && !command.isChooserpack_e()){
            messages.add("removed " + SubscriptionProduct.SCHOOL_CHOOSER_PACK_ELEMENTARY.getLongName());
            subscriptionDao.removeSubscription(command.getChooserpack_eId());
        }

        if((!(command.getChooserpack_mId() >0)) && command.isChooserpack_m()){
            Subscription s = new Subscription(user,SubscriptionProduct.SCHOOL_CHOOSER_PACK_MIDDLE, state);
            subscriptionDao.saveSubscription(s);
            messages.add("added " + s.getProduct().getLongName());
        }
        if(command.getChooserpack_mId() >0 && !command.isChooserpack_m()){
            messages.add("removed " + SubscriptionProduct.SCHOOL_CHOOSER_PACK_MIDDLE.getLongName());
            subscriptionDao.removeSubscription(command.getChooserpack_mId());
        }

        if((!(command.getChooserpack_hId() >0)) && command.isChooserpack_h()){
            Subscription s = new Subscription(user,SubscriptionProduct.SCHOOL_CHOOSER_PACK_HIGH, state);
            subscriptionDao.saveSubscription(s);
            messages.add("added " + s.getProduct().getLongName());
        }
        if(command.getChooserpack_hId() >0 && !command.isChooserpack_h()){
            messages.add("removed " + SubscriptionProduct.SCHOOL_CHOOSER_PACK_HIGH.getLongName());
            subscriptionDao.removeSubscription(command.getChooserpack_hId());
        }


        if((!(command.getSponsorId() >0)) && command.isSponsor()){
            Subscription s = new Subscription(user,SubscriptionProduct.SPONSOR_OPT_IN, state);
            subscriptions.add(s);
            messages.add("added " + s.getProduct().getLongName());
        }
        if(command.getSponsorId() >0 && !command.isSponsor()){
            messages.add("removed " + SubscriptionProduct.SPONSOR_OPT_IN.getLongName());
            subscriptionDao.removeSubscription(command.getSponsorId());
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(getCommandName(), command);
        model.put("messages",messages);

        subscriptionDao.addNewsletterSubscriptions(user,subscriptions);
        _log.warn("doing yet something else");

        return new ModelAndView(getSuccessView(), model);
    }

    public void addMyNth(SubscriptionProduct sp,User user,State state){
        Student student = new Student();
        student.setSchoolId(-1);
        student.setGrade(sp.getGrade());
        student.setState(state);
        user.addStudent(student);
        userDao.saveUser(user);

    }

    public void submitMyNthList(User user,
                                ManagementCommand command,
                                List<String> messages,
                                List<Subscription> subscriptions,
                                State state){
        //foreach possible subscription, check if it needs to be added or deleted
        for(SubscriptionProduct myNth : SubscriptionProduct.MY_NTH_GRADER){
            if(command.checkedBox(myNth) && !(command.getMyNthId(myNth) > 0)){
                addMyNth(myNth,user,state);
                messages.add("added " + myNth.getLongName());
            }
            if((!command.checkedBox(myNth)) && command.getMyNthId(myNth) > 0){
                subscriptionDao.removeStudent(command.getMyNthId(myNth));
                messages.add("removed " + myNth.getLongName());
            }

        }
    }

    public void doMyNthList(User user,ManagementCommand command){
        //store existing My Nth Grade subscriptions in myNthMap
        List<Student> mynthSubscriptions = subscriptionDao.findMynthSubscriptionsByUser(user);
        Map myNthMap = new HashMap();
        for(Student myNth : mynthSubscriptions){
            myNthMap.put(myNth.getMyNth(),myNth.getId());
        }

        //set checkboxes and hidden ids to correct values
        for(SubscriptionProduct myNth : SubscriptionProduct.MY_NTH_GRADER){
            if(myNthMap.get(myNth) == null){continue;}
            Integer mnId = (Integer) myNthMap.get(myNth);
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


}
