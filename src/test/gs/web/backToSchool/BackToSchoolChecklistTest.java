package gs.web.backToSchool;

import gs.data.community.*;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.web.BaseTestCase;

import java.util.HashSet;
import java.util.Set;

import static org.easymock.classextension.EasyMock.*;

public class BackToSchoolChecklistTest extends BaseTestCase {

    IUserDao _userDao;

    ExactTargetAPI _exactTargetAPI;
    
    public void setUp() {
        _userDao = createStrictMock(IUserDao.class);
        _exactTargetAPI = createStrictMock(ExactTargetAPI.class);
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

    public void testAddChecklistItemSendsEmailsAppropriately() {
        User user = new User();
        user.setId(99999);
        user.setEmail("test@greatschools.org");

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        user.setUserProfile(profile);

        BackToSchoolChecklist checklist = new BackToSchoolChecklist();
        checklist.setUserDao(_userDao);
        checklist.setExactTargetAPI(_exactTargetAPI);

        Set<Subscription> subscriptions = new HashSet(1);
        Subscription subscription = new Subscription();
        subscription.setProduct(SubscriptionProduct.BTSTIP_E);
        subscription.setUser(user);
        subscriptions.add(subscription);
        user.setSubscriptions(subscriptions);

        BackToSchoolChecklist.BackToSchoolChecklistItem[] solutionSet = BackToSchoolChecklist.solutionSet;

        for (int i = 0; i < solutionSet.length-1; i++) {
            _userDao.updateUser(isA(User.class));
        }

        replay(_userDao);

        for (int i = 0; i < solutionSet.length-1; i++) {
            checklist.addChecklistItem(solutionSet[i].name(), user);
        }

        verify(_userDao);
        reset(_userDao);
        
        _userDao.updateUser(isA(User.class));
        _exactTargetAPI.sendTriggeredEmail(eq(BackToSchoolChecklist.EXACT_TARGET_EMAIL_KEY), isA(User.class));

        replay(_userDao);
        replay(_exactTargetAPI);

        checklist.addChecklistItem(solutionSet[solutionSet.length-1].name(), user);

        verify(_userDao);
        verify(_exactTargetAPI);
    }


    public void testAddChecklistItemSendsEmailsOnlyOnce() {
        User user = new User();
        user.setId(99999);
        user.setEmail("ssprouse+test2@greatschools.org");

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        user.setUserProfile(profile);

        BackToSchoolChecklist checklist = new BackToSchoolChecklist();
        checklist.setUserDao(_userDao);
        checklist.setExactTargetAPI(_exactTargetAPI);

        Set<Subscription> subscriptions = new HashSet(1);
        Subscription subscription = new Subscription();
        subscription.setProduct(SubscriptionProduct.BTSTIP_E);
        subscription.setUser(user);
        subscriptions.add(subscription);
        user.setSubscriptions(subscriptions);

        BackToSchoolChecklist.BackToSchoolChecklistItem[] solutionSet = BackToSchoolChecklist.solutionSet;

        for (int i = 0; i < solutionSet.length-1; i++) {
            _userDao.updateUser(isA(User.class));
        }

        replay(_userDao);

        for (int i = 0; i < solutionSet.length-1; i++) {
            checklist.addChecklistItem(solutionSet[i].name(), user);
        }

        verify(_userDao);
        reset(_userDao);

        _userDao.updateUser(isA(User.class));
        _exactTargetAPI.sendTriggeredEmail(eq(BackToSchoolChecklist.EXACT_TARGET_EMAIL_KEY), isA(User.class));

        replay(_userDao);
        replay(_exactTargetAPI);

        checklist.addChecklistItem(solutionSet[solutionSet.length-1].name(), user);

        verify(_userDao);
        verify(_exactTargetAPI);

        reset(_userDao);
        reset(_exactTargetAPI);

        replay(_userDao);
        replay(_exactTargetAPI);
        checklist.addChecklistItem(solutionSet[solutionSet.length-1].name(), user);
        verify(_userDao);
        verify(_exactTargetAPI);
    }

}
