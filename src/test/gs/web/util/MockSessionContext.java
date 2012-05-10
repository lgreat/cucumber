/*
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: MockSessionContext.java,v 1.11 2012/05/10 22:25:47 cauer Exp $
 */

package gs.web.util;

import gs.data.util.SpringUtil;
import gs.web.util.context.SessionContext;
import org.springframework.context.ApplicationContext;

/**
 * A stub to allow SessionContext to be used in tests without having to worry about dependencies on DAOs
 *
 */
public class MockSessionContext extends SessionContext {
    private boolean _advertisingOnline = true;
    private boolean _advertisingOnMobileOnline = true;

    public boolean isAdvertisingOnline() {
        return _advertisingOnline;
    }

    public void setAdvertisingOnline(boolean online) {
        _advertisingOnline = online;
    }

    public boolean isAdvertisingOnMobileOnline() {
        return _advertisingOnMobileOnline;
    }

    public void setAdvertisingOnMobileOnline(boolean advertisingOnMobileOnline) {
        _advertisingOnMobileOnline = advertisingOnMobileOnline;
    }

    public ApplicationContext getApplicationContext() {
        return SpringUtil.getApplicationContext();
    }
}
