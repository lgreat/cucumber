package gs.web.backToSchool;

import gs.data.community.*;
import gs.web.BaseTestCase;

import java.util.HashSet;
import java.util.Set;

import static org.easymock.EasyMock.*;

public class BackToSchoolChecklistTest extends BaseTestCase {

    IUserDao _userDao;
    
    public void setUp() {
        _userDao = createStrictMock(IUserDao.class);
    }

    public void testHasCompletedEmailSeries(){
        User user = new User();
        user.setId(99999);
        user.setEmail("test@greatschools.org");

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        user.setUserProfile(profile);

        assertFalse(BackToSchoolChecklist.hasCompletedEmailSeries(user));

        //profile.setBackToSchoolChecklist((int)BackToSchoolChecklist.BackToSchoolChecklistItem.EMAIL_SERIES.byteValue());
        Set<Subscription> subscriptions = new HashSet(1);
        Subscription subscription = new Subscription();
        subscription.setProduct(SubscriptionProduct.BTSTIP_E);
        subscription.setUser(user);
        subscriptions.add(subscription);
        user.setSubscriptions(subscriptions);
        assertTrue(BackToSchoolChecklist.hasCompletedEmailSeries(user));
    }
    
    public void testCompletedItems() {
        User user = new User();
        user.setId(99999);
        user.setEmail("test@greatschools.org");

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        user.setUserProfile(profile);

        assertFalse(BackToSchoolChecklist.getCompletedItems(user).contains(BackToSchoolChecklist.BackToSchoolChecklistItem.ARTICLE1));

        profile.setBackToSchoolChecklist((int)BackToSchoolChecklist.BackToSchoolChecklistItem.ARTICLE1.byteValue());
        
        assertTrue(BackToSchoolChecklist.getCompletedItems(user).contains(BackToSchoolChecklist.BackToSchoolChecklistItem.ARTICLE1));
    }

    public void testHasCompletedChecklist() {
        User user = new User();
        user.setId(99999);
        user.setEmail("test@greatschools.org");

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        user.setUserProfile(profile);

        assertFalse(BackToSchoolChecklist.hasCompletedChecklist(user));

        BackToSchoolChecklist checklist = (BackToSchoolChecklist) getApplicationContext().getBean("backToSchoolChecklist");
        checklist.setUserDao(_userDao);

        _userDao.updateUser(isA(User.class));
        _userDao.updateUser(isA(User.class));
        _userDao.updateUser(isA(User.class));
        replay(_userDao);

        for (BackToSchoolChecklist.BackToSchoolChecklistItem item : BackToSchoolChecklist.solutionSet) {
            checklist.addChecklistItem(item.name(), user);
        }

        Set<Subscription> subscriptions = new HashSet(1);
        Subscription subscription = new Subscription();
        subscription.setProduct(SubscriptionProduct.BTSTIP_E);
        subscription.setUser(user);
        subscriptions.add(subscription);
        user.setSubscriptions(subscriptions);

        assertTrue(BackToSchoolChecklist.hasCompletedChecklist(user));

        verify(_userDao);
    }

}
