/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.util.email;

import gs.web.BaseTestCase;
import gs.web.util.MockJavaMailSender;

/**
 * Provides testing for the EmailHelperFactory class.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class EmailHelperFactoryTest extends BaseTestCase {
    private EmailHelperFactory _factory;

    public void setUp() throws Exception {
        super.setUp();
        _factory = new EmailHelperFactory();
    }

    public void testThreadSafe() {
        EmailHelper helper1 = _factory.getEmailHelper();
        EmailHelper helper2 = _factory.getEmailHelper();

        assertTrue(helper1 != helper2);
        helper1.setToEmail("aroy@greatschools.net");
        helper2.setToEmail("aroy+1@greatschools.net");
        assertFalse(helper1.getToEmail().equals(helper2.getToEmail()));
    }

    public void testInjection() {
        EmailHelper helper = _factory.getEmailHelper();
        // no mail sender since the factory was instantiated outside of Spring
        assertNull(helper.getMailSender());

        MockJavaMailSender _mailSender = new MockJavaMailSender();
        // set the mail sender
        _factory.setMailSender(_mailSender);
        assertEquals(_mailSender, _factory.getMailSender());

        // now we should see the mail sender passed on to the helper
        helper = _factory.getEmailHelper();
        assertNotNull(helper.getMailSender());
        assertEquals(_mailSender, helper.getMailSender());
    }
}
