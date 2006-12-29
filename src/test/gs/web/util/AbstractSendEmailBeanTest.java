/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.util;

import gs.web.BaseControllerTestCase;
import gs.data.util.email.EmailHelper;
import gs.data.util.email.EmailHelperFactory;

import java.util.Map;

/**
 * Provides testing for the AbstractSendEmailBean class.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class AbstractSendEmailBeanTest extends BaseControllerTestCase {
    AbstractSendEmailBean _bean;
    EmailHelperFactory _factory;

    public void setUp() throws Exception {
        super.setUp();
        _bean = new AbstractSendEmailBean() {};
        _factory = new EmailHelperFactory();
        _bean.setEmailHelperFactory(_factory);
    }

    public void testGetEmailHelper() {
        _bean.setFromEmail("a@b.c");
        _bean.setSubject("subject");
        EmailHelper helper = _bean.getEmailHelper();
        assertNotNull(helper);
        assertEquals("a@b.c", helper.getFromEmail());
        assertEquals("subject", helper.getSubject());
        assertNull(helper.getFromName());
        _bean.setFromName("me");
        helper = _bean.getEmailHelper();
        assertNotNull(helper);
        assertEquals("a@b.c", helper.getFromEmail());
        assertEquals("subject", helper.getSubject());
        assertEquals("me", helper.getFromName());
    }

    public void testAddLinkReplacement() {
        EmailHelper helper = _bean.getEmailHelper();
        // call
        _bean.addLinkReplacement(helper, getRequest(), UrlBuilder.COMMUNITY_LANDING, "KEY", "click here");
        // verify
        Map replacements = helper.getInlineReplacements();
        assertNotNull(replacements);
        assertEquals(1, replacements.size());
        String value = (String) replacements.get("KEY");
        assertNotNull(value);
        assertTrue(value.indexOf(">click here</a>") > -1);
    }

    public void testAddLinkReplacementOmniture() {
        EmailHelper helper = _bean.getEmailHelper();
        // call
        _bean.addLinkReplacement(helper, getRequest(), UrlBuilder.COMMUNITY_LANDING, "KEY",
                "click here", "welcomereg");
        // verify
        Map replacements = helper.getInlineReplacements();
        assertNotNull(replacements);
        assertEquals(1, replacements.size());
        String value = (String) replacements.get("KEY");
        assertNotNull(value);
        assertTrue(value.indexOf(">click here</a>") > -1);
        assertTrue(value.indexOf("cpn=welcomereg") > -1);
    }
}
