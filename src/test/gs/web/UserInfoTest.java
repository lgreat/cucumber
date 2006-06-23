/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: UserInfoTest.java,v 1.1 2006/06/23 01:00:37 apeterson Exp $
 */

package gs.web;

import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.community.FavoriteSchool;
import gs.data.state.State;
import gs.web.community.UserInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class UserInfoTest extends BaseTestCase {
    //private IUserDao _userDao;

    protected void setUp() throws Exception {
        super.setUp();

        //_userDao = (IUserDao) getApplicationContext().getBean(IUserDao.BEAN_ID);
    }

    public void testCalculateNickname() {

        User user = new User();
        user.setEmail("blahblahblah@whatever.org");
        user.setId(Integer.valueOf(5));

        UserInfo userInfo = new UserInfo(user);

        assertEquals("blahblahblah@whatever.org", userInfo.getEmail());
        assertEquals("blahblahblah", userInfo.getNickname());

        user.setEmail("wbeck@whatever.org");
        userInfo = new UserInfo(user);
        assertEquals("wbeck@whatever.org", userInfo.getEmail());
        assertEquals("wbeck", userInfo.getNickname());
    }

    public void testFindsMsses() {
        User user = new User();
        user.setEmail("wbeck@gs.net");

        // Manually add some subscriptions
        Set subscriptions = new HashSet();

        Subscription sub = new Subscription();
        sub.setProduct(SubscriptionProduct.MYSTAT);
        sub.setState(State.GA);
        sub.setSchoolId(1234);
        subscriptions.add(sub);

        Subscription sub2 = new Subscription();
        sub2.setProduct(SubscriptionProduct.MYSTAT);
        sub2.setState(State.CA);
        sub2.setSchoolId(1234);
        subscriptions.add(sub2);

        user.setSubscriptions(subscriptions);

        UserInfo userInfo = new UserInfo(user);
        assertEquals(2, userInfo.getMssCount());
        assertTrue(userInfo.hasMssSubscription(State.CA, 1234));
        assertTrue(userInfo.hasMssSubscription(State.GA, 1234));
        assertFalse(userInfo.hasMssSubscription(State.CA, 1235));

        // Cool, let's make sure it doesn't find non-MSS subscriptions and count them.
        Subscription sub3 = new Subscription();
        sub3.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        sub3.setState(State.CA);
        subscriptions.add(sub3);

        Subscription sub4 = new Subscription();
        sub4.setProduct(SubscriptionProduct.MY_FIRST_GRADER);
        sub4.setState(State.CA);
        subscriptions.add(sub4);

        user.setSubscriptions(subscriptions);

        userInfo = new UserInfo(user);
        assertEquals(2, userInfo.getMssCount());
        assertTrue(userInfo.hasMssSubscription(State.CA, 1234));
        assertTrue(userInfo.hasMssSubscription(State.GA, 1234));
        assertFalse(userInfo.hasMssSubscription(State.CA, 1235));


    }

    public void testFindsMsls() {
        User user = new User();
        user.setEmail("wbeck@gs.net");

        // Manually add some schools
        Set favoriteSchools = new HashSet();

        FavoriteSchool school = new FavoriteSchool();
        school.setState(State.CA);
        school.setSchoolId(Integer.valueOf(1));
        favoriteSchools.add(school);

        FavoriteSchool school2 = new FavoriteSchool();
        school2.setState(State.CA);
        school2.setSchoolId(Integer.valueOf(5));
        favoriteSchools.add(school2);

        user.setFavoriteSchools(favoriteSchools);

        UserInfo userInfo = new UserInfo(user);
        assertEquals(2, userInfo.getMslCount());

    }

    public void testFindsNonMssSubscriptions() {
        User user = new User();
        user.setEmail("wbeck@gs.net");

        UserInfo userInfo = new UserInfo(user);
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.MY_FIRST_GRADER));
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.BETA_GROUP));
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.MY_SECOND_GRADER));
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.MYSTAT));

        // Manually add some subscriptions
        Set subscriptions = new HashSet();

        Subscription sub = new Subscription();
        sub.setProduct(SubscriptionProduct.MYSTAT);
        sub.setState(State.GA);
        sub.setSchoolId(1234);
        subscriptions.add(sub);

        Subscription sub2 = new Subscription();
        sub2.setProduct(SubscriptionProduct.MYSTAT);
        sub2.setState(State.CA);
        sub2.setSchoolId(1234);
        subscriptions.add(sub2);

        user.setSubscriptions(subscriptions);

        userInfo = new UserInfo(user);
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.MY_FIRST_GRADER));
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.BETA_GROUP));
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.MY_SECOND_GRADER));
        assertTrue(userInfo.hasSubscription(SubscriptionProduct.MYSTAT));

        // Cool, let's make sure it doesn't find non-MSS subscriptions and count them.
        Subscription sub3 = new Subscription();
        sub3.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        sub3.setState(State.CA);
        subscriptions.add(sub3);

        Subscription sub4 = new Subscription();
        sub4.setProduct(SubscriptionProduct.MY_FIRST_GRADER);
        sub4.setState(State.CA);
        subscriptions.add(sub4);

        user.setSubscriptions(subscriptions);

        userInfo = new UserInfo(user);
        assertTrue(userInfo.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));
        assertTrue(userInfo.hasSubscription(SubscriptionProduct.MY_FIRST_GRADER));
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.BETA_GROUP));
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.MY_SECOND_GRADER));
        assertTrue(userInfo.hasSubscription(SubscriptionProduct.MYSTAT));

    }


    public void testSetFromCookies() {
        UserInfo userInfo = new UserInfo("ndp@mac.com", "ndp", 8, "CA10", "greatnews");
        assertEquals("ndp@mac.com", userInfo.getEmail());
        assertEquals("ndp", userInfo.getNickname());
        assertEquals(8, userInfo.getMslCount());
        assertTrue(userInfo.hasMssSubscription(State.CA, 10));
        assertFalse(userInfo.hasMssSubscription(State.CA, 1));
        assertFalse(userInfo.hasMssSubscription(State.GA, 10));

        assertTrue(userInfo.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));

    }

    public void testCalculatesCookies() {
        User user = new User();
        user.setEmail("wbeck@gs.net");

        UserInfo userInfo = new UserInfo(user);
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.MY_FIRST_GRADER));
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.BETA_GROUP));
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.MY_SECOND_GRADER));
        assertFalse(userInfo.hasSubscription(SubscriptionProduct.MYSTAT));

        // Manually add some subscriptions
        Set subscriptions = new HashSet();

        Subscription sub = new Subscription();
        sub.setProduct(SubscriptionProduct.MYSTAT);
        sub.setState(State.GA);
        sub.setSchoolId(1234);
        subscriptions.add(sub);

        Subscription sub2 = new Subscription();
        sub2.setProduct(SubscriptionProduct.MYSTAT);
        sub2.setState(State.CA);
        sub2.setSchoolId(1234);
        subscriptions.add(sub2);

        Subscription sub3 = new Subscription();
        sub3.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        sub3.setState(State.CA);
        subscriptions.add(sub3);

        Subscription sub4 = new Subscription();
        sub4.setProduct(SubscriptionProduct.MY_FIRST_GRADER);
        sub4.setState(State.CA);
        subscriptions.add(sub4);

        user.setSubscriptions(subscriptions);

        userInfo = new UserInfo(user);
        assertTrue("GA1234,CA1234".equals(userInfo.getMssCookie()) || "CA1234,GA1234".equals(userInfo.getMssCookie()));
        assertTrue("my1,greatnews".equals(userInfo.getNonMssCookie()) || "greatnews,my1".equals(userInfo.getNonMssCookie()));
    }

}
