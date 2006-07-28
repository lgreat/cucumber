/*
 * Copyright (c) 2005 NDP Software. All Rights Reserved.
 * $Id: NewsItemTagHandlerTest.java,v 1.1 2006/07/28 15:37:27 apeterson Exp $
 */

package gs.web.content;

import junit.framework.TestCase;
import gs.data.state.State;

/**
 * Provides...
 *
 * @author <a href="mailto:ndp@mac.com">Andrew J. Peterson</a>
 */
public class NewsItemTagHandlerTest extends TestCase {
    private NewsItemTagHandler _handler;

    protected void setUp() throws Exception {
        super.setUp();
        _handler = new NewsItemTagHandler();
    }

    public void testBasics() {
        _handler.setCategory("TEST CAT");
        _handler.setState(State.VA);

    }
}
