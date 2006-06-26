/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: ClientSideSessionCacheTest.java,v 1.2 2006/06/26 21:28:11 apeterson Exp $
 */

package gs.web.community;

import gs.data.community.FavoriteSchool;
import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.state.State;
import junit.framework.TestCase;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests ClientSideSessionCache class.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 * @noinspection FeatureEnvy,LocalVariableNamingConvention,OverlyLongMethod,MagicNumber
 */
public class ClientSideSessionCacheTest extends TestCase {

    public void testCalculateNickname() {

        User user = new User();
        user.setEmail("blahblahblah@whatever.org");

        ClientSideSessionCache clientSideSessionCache = new ClientSideSessionCache(user);

        assertEquals("blahblahblah@whatever.org", clientSideSessionCache.getEmail());
        assertEquals("blahblahblah", clientSideSessionCache.getNickname());

        user.setEmail("wbeck@whatever.org");
        clientSideSessionCache = new ClientSideSessionCache(user);
        assertEquals("wbeck@whatever.org", clientSideSessionCache.getEmail());
        assertEquals("wbeck", clientSideSessionCache.getNickname());
    }

    public void testFindsMsses() {
        User user = new User();
        user.setEmail("wbeck@gs.net");

        // Manually add some subscriptions
        Set subscriptions = new HashSet();

        addMssSubscriptions(subscriptions);

        user.setSubscriptions(subscriptions);

        ClientSideSessionCache clientSideSessionCache = new ClientSideSessionCache(user);
        assertEquals(2, clientSideSessionCache.getMssCount());
        assertTrue(clientSideSessionCache.hasMssSubscription(State.CA, 1234));
        assertTrue(clientSideSessionCache.hasMssSubscription(State.GA, 1234));
        assertFalse(clientSideSessionCache.hasMssSubscription(State.CA, 1235));

        // Cool, let's make sure it doesn't find non-MSS subscriptions and count them.
        addPaAndMyFirstSubscriptions(subscriptions);

        user.setSubscriptions(subscriptions);

        clientSideSessionCache = new ClientSideSessionCache(user);
        assertEquals(2, clientSideSessionCache.getMssCount());
        assertTrue(clientSideSessionCache.hasMssSubscription(State.CA, 1234));
        assertTrue(clientSideSessionCache.hasMssSubscription(State.GA, 1234));
        assertFalse(clientSideSessionCache.hasMssSubscription(State.CA, 1235));


    }

    public void testFindsMsls() {
        User user = new User();
        user.setEmail("wbeck@gs.net");

        // Manually add some schools
        Set favoriteSchools = new HashSet();

        FavoriteSchool school = new FavoriteSchool();
        school.setState(State.CA);
        school.setSchoolId(new Integer(1));
        favoriteSchools.add(school);

        FavoriteSchool school2 = new FavoriteSchool();
        school2.setState(State.CA);
        school2.setSchoolId(new Integer(5));
        favoriteSchools.add(school2);

        user.setFavoriteSchools(favoriteSchools);

        ClientSideSessionCache clientSideSessionCache = new ClientSideSessionCache(user);
        assertEquals(2, clientSideSessionCache.getMslCount());

    }

    public void testFindsNonMssSubscriptions() {
        User user = new User();
        user.setEmail("wbeck@gs.net");

        ClientSideSessionCache clientSideSessionCache = new ClientSideSessionCache(user);
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.MY_FIRST_GRADER));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.BETA_GROUP));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.MY_SECOND_GRADER));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.MYSTAT));

        // Manually add some subscriptions
        Set subscriptions = new HashSet();

        addMssSubscriptions(subscriptions);
        user.setSubscriptions(subscriptions);

        clientSideSessionCache = new ClientSideSessionCache(user);
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.MY_FIRST_GRADER));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.BETA_GROUP));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.MY_SECOND_GRADER));
        assertTrue(clientSideSessionCache.hasSubscription(SubscriptionProduct.MYSTAT));

        // Cool, let's make sure it doesn't find non-MSS subscriptions and count them.
        addPaAndMyFirstSubscriptions(subscriptions);
        user.setSubscriptions(subscriptions);

        clientSideSessionCache = new ClientSideSessionCache(user);
        assertTrue(clientSideSessionCache.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));
        assertTrue(clientSideSessionCache.hasSubscription(SubscriptionProduct.MY_FIRST_GRADER));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.BETA_GROUP));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.MY_SECOND_GRADER));
        assertTrue(clientSideSessionCache.hasSubscription(SubscriptionProduct.MYSTAT));

    }


    public void testSetFromCookies() {
        User user = new User();
        user.setEmail("wbeck@gs.net");

        ClientSideSessionCache clientSideSessionCache = new ClientSideSessionCache(user);
        clientSideSessionCache.setMssCookie("CA10");
        assertTrue(clientSideSessionCache.hasMssSubscription(State.CA, 10));
        assertFalse(clientSideSessionCache.hasMssSubscription(State.CA, 1));
        assertFalse(clientSideSessionCache.hasMssSubscription(State.GA, 10));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));
        assertEquals(0, clientSideSessionCache.getMslCount());

        clientSideSessionCache.setMssCookie("CA10,GA10");
        assertTrue(clientSideSessionCache.hasMssSubscription(State.CA, 10));
        assertFalse(clientSideSessionCache.hasMssSubscription(State.CA, 1));
        assertTrue(clientSideSessionCache.hasMssSubscription(State.GA, 10));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));

        clientSideSessionCache.setNonMssCookie("greatnews");
        assertTrue(clientSideSessionCache.hasMssSubscription(State.CA, 10));
        assertFalse(clientSideSessionCache.hasMssSubscription(State.CA, 1));
        assertTrue(clientSideSessionCache.hasMssSubscription(State.GA, 10));
        assertTrue(clientSideSessionCache.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));

        clientSideSessionCache.setNonMssCookie("greatnews,my1");
        assertTrue(clientSideSessionCache.hasMssSubscription(State.CA, 10));
        assertFalse(clientSideSessionCache.hasMssSubscription(State.CA, 1));
        assertTrue(clientSideSessionCache.hasMssSubscription(State.GA, 10));
        assertTrue(clientSideSessionCache.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));
        assertTrue(clientSideSessionCache.hasSubscription(SubscriptionProduct.MY_FIRST_GRADER));
    }

    public void testCalculatesCookies() {
        User user = new User();
        user.setEmail("wbeck@gs.net");

        ClientSideSessionCache clientSideSessionCache = new ClientSideSessionCache(user);
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.MY_FIRST_GRADER));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.BETA_GROUP));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.MY_SECOND_GRADER));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.MYSTAT));

        // Manually add some subscriptions
        Set subscriptions = new HashSet();

        addMssSubscriptions(subscriptions);
        addPaAndMyFirstSubscriptions(subscriptions);
        user.setSubscriptions(subscriptions);
        clientSideSessionCache = new ClientSideSessionCache(user);
        assertTrue("GA1234,CA1234".equals(clientSideSessionCache.getMssCookie()) || "CA1234,GA1234".equals(clientSideSessionCache.getMssCookie()));
        assertTrue("my1,greatnews".equals(clientSideSessionCache.getNonMssCookie()) || "greatnews,my1".equals(clientSideSessionCache.getNonMssCookie()));
    }

    public void testCanExternalize() throws IOException, ClassNotFoundException {
        // Create a simple one, and make sure it comes back
        User user = new User();
        user.setEmail("apeterson@gs.net");
        ClientSideSessionCache clientSideSessionCache = new ClientSideSessionCache(user);
        assertEquals(0, clientSideSessionCache.getMslCount());
        assertEquals(0, clientSideSessionCache.getMssCount());
        ClientSideSessionCache clientSideSessionCacheCopy = externalizeAndBringBack(clientSideSessionCache);
        assertEquals("apeterson@gs.net", clientSideSessionCacheCopy.getEmail());
        assertEquals("apeterson", clientSideSessionCacheCopy.getNickname());
        assertEquals(0, clientSideSessionCacheCopy.getMslCount());
        assertEquals(0, clientSideSessionCacheCopy.getMssCount());

        // Create a rich user info object, with everything
        Set subscriptions = new HashSet();
        addMssSubscriptions(subscriptions);
        addPaAndMyFirstSubscriptions(subscriptions);
        user.setSubscriptions(subscriptions);

        clientSideSessionCache = new ClientSideSessionCache(user);

        // Make sure it's all here
        assertTrue(clientSideSessionCache.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));
        assertTrue(clientSideSessionCache.hasSubscription(SubscriptionProduct.MY_FIRST_GRADER));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.BETA_GROUP));
        assertFalse(clientSideSessionCache.hasSubscription(SubscriptionProduct.MY_SECOND_GRADER));
        assertTrue(clientSideSessionCache.hasSubscription(SubscriptionProduct.MYSTAT));

        // Bring it back
        clientSideSessionCacheCopy = externalizeAndBringBack(clientSideSessionCache);

        // Make sure it came back
        assertTrue(clientSideSessionCacheCopy.hasSubscription(SubscriptionProduct.PARENT_ADVISOR));
        assertTrue(clientSideSessionCacheCopy.hasSubscription(SubscriptionProduct.MY_FIRST_GRADER));
        assertFalse(clientSideSessionCacheCopy.hasSubscription(SubscriptionProduct.BETA_GROUP));
        assertFalse(clientSideSessionCacheCopy.hasSubscription(SubscriptionProduct.MY_SECOND_GRADER));
        assertTrue(clientSideSessionCacheCopy.hasSubscription(SubscriptionProduct.MYSTAT));

    }

    private ClientSideSessionCache externalizeAndBringBack(ClientSideSessionCache clientSideSessionCache) throws IOException, ClassNotFoundException {
        String cookie = clientSideSessionCache.getCookieRepresentation();
        return ClientSideSessionCache.createClientSideSessionCache(cookie);
    }

    private void addPaAndMyFirstSubscriptions(Set subscriptions) {
        Subscription sub3 = new Subscription();
        sub3.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        sub3.setState(State.CA);
        subscriptions.add(sub3);

        Subscription sub4 = new Subscription();
        sub4.setProduct(SubscriptionProduct.MY_FIRST_GRADER);
        sub4.setState(State.CA);
        subscriptions.add(sub4);
    }


    private void addMssSubscriptions(Set subscriptions) {
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
    }

}
