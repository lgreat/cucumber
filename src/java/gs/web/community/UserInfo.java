/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: UserInfo.java,v 1.1 2006/06/23 01:00:28 apeterson Exp $
 */

package gs.web.community;

import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Provides summary information about the user. Can be initialized either from
 * a user object or from a set of values. Normal usage is to create a set of
 * session cookies when the user first logs in, and then persist cookies
 * as session cookies. When a new request comes in, a UserInfo will created
 * from the cookies.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class UserInfo {
    private final String _nickname;
    private final String _email;
    private final int _mslCount;
    /**
     * of {@link #createMssHash(gs.data.state.State, int)} values
     */
    private final Set _mss;

    /**
     * of {@link SubscriptionProduct#getName() values}
     */
    private final Set _nonMss;
    private static final String COOKIE_COMPONENT_DELIMETER = ",";

    public UserInfo(String email,
                    String nickname,
                    int mslCount,
                    String mssSubscriptionsCookie,
                    String nonMssSubscriptionsCookie) {
        _email = email;
        _nickname = nickname;
        _mslCount = mslCount;
        _mss = new HashSet();
        String[] p = mssSubscriptionsCookie.split(COOKIE_COMPONENT_DELIMETER);
        for (int i = 0; i < p.length; i++) {
            String s = p[i];
            if (s.length() > 2) {
                _mss.add(s);
            }
        }

        _nonMss = new HashSet();
        p = nonMssSubscriptionsCookie.split(COOKIE_COMPONENT_DELIMETER);
        for (int i = 0; i < p.length; i++) {
            String s = p[i];
            SubscriptionProduct prod = SubscriptionProduct.getSubscriptionProduct(s);
            if (prod != null) {
                _nonMss.add(prod.getName());
            }
        }


    }

    public UserInfo(User user) {
        _email = user.getEmail();
        final String[] p = _email.split("@");
        _nickname = p[0];

        // Count their subscriptions
        final Set subscriptions = user.getSubscriptions();
        _mss = new HashSet();
        _nonMss = new HashSet();
        if (subscriptions != null) {
            for (Iterator iter = subscriptions.iterator(); iter.hasNext();) {
                Subscription sub = (Subscription) iter.next();
                if (sub.getProduct().equals(SubscriptionProduct.MYSTAT)) {
                    _mss.add(createMssHash(sub.getState(), sub.getSchoolId()));
                } else {
                    _nonMss.add(sub.getProduct().getName());
                }
            }
        }

        _mslCount = user.getFavoriteSchools() != null ? user.getFavoriteSchools().size() : 0;
    }

    private String createMssHash(State state, int schoolId) {
        return state.getAbbreviation() + schoolId;
    }

    public int getMslCount() {
        return _mslCount;
    }

    public String getEmail() {
        return _email;
    }

    public String getNickname() {
        return _nickname;
    }

    public boolean hasMssSubscription(State state, int schoolId) {
        String mssHash = createMssHash(state, schoolId);
        return _mss.contains(mssHash);
    }

    public boolean hasSubscription(SubscriptionProduct product) {
        if (SubscriptionProduct.MYSTAT.equals(product)) {
            return !_mss.isEmpty();
        } else {
            return _nonMss.contains(product.getName());
        }
    }

    public int getMssCount() {
        return _mss.size();
    }

    public String getMssCookie() {
        return StringUtils.join(_mss.iterator(), COOKIE_COMPONENT_DELIMETER);
    }

    public String getNonMssCookie() {
        return StringUtils.join(_nonMss.iterator(), COOKIE_COMPONENT_DELIMETER);
    }
}
