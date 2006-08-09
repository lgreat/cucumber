/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NewsletterCommandSaTest.java,v 1.1 2006/08/09 00:33:01 dlee Exp $
 */
package gs.web.community.newsletters;

import gs.web.community.newsletters.popup.NewsletterCommand;
import junit.framework.TestCase;

/**
 * Test newsletter command object.
 *
 * Created after the fact...NewsletterCommand is tested thoroughly in the following controller tests
 *
 * @see gs.web.community.newsletters.popup.MssPaControllerTest
 * @see gs.web.community.newsletters.popup.NthGraderControllerTest
 * @see gs.web.community.newsletters.popup.SubscriptionSummaryTest
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class NewsletterCommandSaTest extends TestCase {
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testNewsletterCommand() {
        NewsletterCommand command = new NewsletterCommand();
        //if referrer is not set, it should always return an empty string, tracking variable "referredBy" relies on this
        assertEquals("", command.getReferrer());
        command.setReferrer("some page");
        assertEquals("some page", command.getReferrer());

    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

}
