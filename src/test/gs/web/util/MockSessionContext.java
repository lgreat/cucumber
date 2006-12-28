/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: MockSessionContext.java,v 1.7 2006/12/28 21:52:26 cpickslay Exp $
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

    public boolean isAdvertisingOnline() {
        return _advertisingOnline;
    }

    public void setAdvertisingOnline(boolean online) {
        _advertisingOnline = online;
    }

    public ApplicationContext getApplicationContext() {
        return SpringUtil.getApplicationContext();
    }

}
