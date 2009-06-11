package gs.web.email;

import gs.web.BaseControllerTestCase;
import gs.data.community.*;
import gs.data.school.School;
import gs.data.school.ISchoolDao;
import gs.data.school.Grade;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: eddie
 * Date: May 26, 2009
 * Time: 4:51:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class ManagementControllerTest extends BaseControllerTestCase {

    private ISubscriptionDao subscriptionDao;
    public ISchoolDao schoolDao;
    public IUserDao userDao;
    public ManagementCommand command;
    User user;

    public void testFormSubmit() throws Exception {
        ManagementCommand command = new ManagementCommand();
        command.setGreatnews(true);
        assertTrue(command.getGreatnews());
    }

    public void testMynth(){
        Student student = new Student();
        student.setSchoolId(-1);
        student.setGrade(Grade.G_1);
        /*
        student.setState(user.getUserProfile().getState());
        user.addStudent(student);
        userDao.saveUser(user);
        */
    }

    protected void setUp() throws Exception {
        super.setUp();
        command = new ManagementCommand();
        subscriptionDao = (ISubscriptionDao) getApplicationContext().getBean(ISubscriptionDao.BEAN_ID);
        schoolDao = (ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID);
        userDao = (IUserDao) getApplicationContext().getBean(IUserDao.BEAN_ID);
        //user = userDao.findUserFromId(736);
        user = new User();
        user.setId(736);
    }
    public void xtestCreateNthList(){
        //create mock dao at some point
        ManagementController mc = new ManagementController();
        mc.setSubscriptionDao(subscriptionDao);
        mc.doMyNthList(user,command);
        //assertTrue(command.isHasMy1());
        //assertTrue(command.isHasMy2());
        //assertFalse(command.isHasMy3());
        System.out.println(command.getMy1Id());
        System.out.println(command.getMy2Id());
        System.out.println(command.getMy5Id());
    }

    public void xtestMyStat(){
        List<School> schools = new ArrayList();
        List<Subscription> myStats = new ArrayList<Subscription>(subscriptionDao.findMssSubscriptionsByUser(user));

        int myStatNum = 1;
        for (Subscription myStat: myStats ) {
            School school = schoolDao.getSchoolById(myStat.getState(), myStat.getSchoolId());
            schools.add(school);
            try{
                Class dummyClass = Class.forName("gs.web.email.ManagementCommand");

                Method meth = dummyClass.getMethod("setSchool" + myStatNum,Boolean.TYPE);
                Method meth2 = dummyClass.getMethod("setName" + myStatNum,String.class);
                Method meth3 = dummyClass.getMethod("setId" + myStatNum,Integer.TYPE);
                Object[] arglist = new Object[]{Boolean.TYPE};
                meth.invoke(command,true);
                meth2.invoke(command,school.getName());
                meth3.invoke(command,myStat.getId());
            }catch(Exception e){

            }
            myStatNum++;
        }
        System.out.println(command.getId1());

    }

    public void xtestSummer(){
        List<Subscription> subscriptions = subscriptionDao.getUserSubscriptions(user);

        boolean hasGreatNews = false;

        for (Object subscription : subscriptions) {
            Subscription s = (Subscription) subscription;
            Calendar cal = Calendar.getInstance();
            if(s.getProduct().getName().startsWith("summer")
            && s.getExpires() != null && s.getExpires().after(cal.getTime())){
                System.out.println(s.getProduct().getName());
            }

        }

    }

}
