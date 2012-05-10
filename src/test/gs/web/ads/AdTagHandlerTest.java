/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: AdTagHandlerTest.java,v 1.24 2012/05/10 22:25:47 cauer Exp $
 */
package gs.web.ads;

import gs.data.admin.IPropertyDao;
import gs.web.BaseTestCase;
import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
import gs.web.util.MockSessionContext;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import java.io.IOException;
import java.io.Writer;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createStrictMock;

/**
 * Test AdTagHandler
 *
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public class AdTagHandlerTest extends BaseTestCase {
    AdTagHandler _tag;
    MockSessionContext _sessionContext;
    HttpServletRequest _request;
    MockJspWriter _writer;
    private IPropertyDao _propertyDao;

    public void setUp() throws Exception {
        super.setUp();
        _tag = new AdTagHandler();
        _request = new MockHttpServletRequest();
        _sessionContext = new MockSessionContext();
        _propertyDao = createStrictMock(IPropertyDao.class);
        _sessionContext.setPropertyDao(_propertyDao);
        _writer = new MockJspWriter();
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void replayAll() {
        super.replayMocks(_propertyDao);
    }

    public void verifyAll() {
        super.verifyMocks(_propertyDao);
    }

    public void resetAll() {
        super.resetMocks(_propertyDao);
    }

    public void testAdFreeCobrands() throws Exception {
        _sessionContext.setCobrand("framed");
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _tag.setJspContext(jspContext);
        _tag.setPosition("x22");

        resetAll();
        expect(_propertyDao.getProperty(IPropertyDao.GPT_ENABLED_KEY,"false")).andReturn("false");
        replayAll();

        assertEquals("", _tag.getContent());

        verifyAll();
    }

    public void testAdFreeCobrandsWithAlwaysShowFramed() throws Exception {
        _sessionContext.setCobrand("framed");
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _tag.setJspContext(jspContext);
        _tag.setPosition("YouTube_381x311");
        _tag.setAlwaysShow(true);

        resetAll();
        expect(_propertyDao.getProperty(IPropertyDao.GPT_ENABLED_KEY,"false")).andReturn("false");
        replayAll();

        assertEquals("Expect ad tag even though ad free cobrand",
                "<div id=\"adYouTube_381x311\" class=\"adYouTube_381x311 ad noprint\"><script type=\"text/javascript\">GA_googleFillSlot('YouTube_381x311');</script></div>", _tag.getContent());
        _tag.setAlwaysShow(false);

        verifyAll();
    }

    public void testAdFreeCobrandsWithAlwaysShowAdvertisingOffline() throws Exception {
        _sessionContext.setCobrand("framed");
        _sessionContext.setAdvertisingOnline(false);
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _tag.setJspContext(jspContext);
        _tag.setPosition("YouTube_381x311");
        _tag.setAlwaysShow(true);

        assertEquals("Expect advertising offline to hide ad",
                "", _tag.getContent());
        _tag.setAlwaysShow(false);
        _sessionContext.setAdvertisingOnline(true);
    }

    public void testAdFreeCobrandsWithAlwaysShowCrawler() throws Exception {
        _sessionContext.setCobrand("framed");
        _sessionContext.setCrawler(true);
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _tag.setJspContext(jspContext);
        _tag.setPosition("YouTube_381x311");
        _tag.setAlwaysShow(true);

        assertEquals("Expect crawler to hide ad",
                "", _tag.getContent());
        _tag.setAlwaysShow(false);
        _sessionContext.setCrawler(false);
    }

    public void testBasicDeferWorks() throws Exception {
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _tag.setJspContext(jspContext);
        _tag.setPosition("x22");

        String output = _tag.getContent();
        //_log.debug(output);

        assertEquals("<div id=\"adx22\" class=\"adx22 ad noprint\"><script type=\"text/javascript\">OAS_AD('x22');</script></div>", output);
        assertTrue(pageHelper.getAdPositions().contains(AdPosition.X_22));

        //try to set the same ad position
        try {
            _tag.setPosition("x22");
            resetAll();
            expect(_propertyDao.getProperty(IPropertyDao.GPT_ENABLED_KEY,"false")).andReturn("false");
            replayAll();

            _tag.getContent();
            verifyAll();
            fail("x22 already set so we shouldn't allow it to be set again");
        } catch (IllegalArgumentException e){}
    }

    public void testBasicGAMDeferWorks() throws Exception {
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _tag.setJspContext(jspContext);
        _tag.setPosition("Top_300x137");

        String output = _tag.getContent();
        //_log.debug(output);

        assertEquals("<div id=\"adTop_300x137\" class=\"adTop_300x137 ad noprint\"><script type=\"text/javascript\">GA_googleFillSlot('Top_300x137');</script></div>", output);
        assertTrue(pageHelper.getAdPositions().contains(AdPosition.Top_300x137));

        //try to set the same ad position
        try {
            _tag.setPosition("Top_300x137");
            resetAll();
            expect(_propertyDao.getProperty(IPropertyDao.GPT_ENABLED_KEY,"false")).andReturn("false");
            replayAll();

            _tag.getContent();

            verifyAll();
            fail("Top_300x137 already set so we shouldn't allow it to be set again");
        } catch (IllegalArgumentException e){}        
    }

    public void testPrefixSlotNameGAMDeferWorks() throws Exception {
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _tag.setJspContext(jspContext);
        _tag.setPosition("Top_300x137");
        _request.setAttribute(AdTagHandler.REQUEST_ATTRIBUTE_SLOT_PREFIX_NAME, "SchoolProfile_");

        String output = _tag.getContent();
        assertEquals("<div id=\"adTop_300x137\" class=\"adTop_300x137 ad noprint\"><script type=\"text/javascript\">GA_googleFillSlot('SchoolProfile_Top_300x137');</script></div>", output);
        assertTrue(pageHelper.getAdPositions().contains(AdPosition.Top_300x137));

        //try to set the same ad position
        try {
            _tag.setPosition("Top_300x137");
            resetAll();
            expect(_propertyDao.getProperty(IPropertyDao.GPT_ENABLED_KEY,"false")).andReturn("false");
            replayAll();

            _tag.getContent();

            verifyAll();
            fail("Top_300x137 already set so we shouldn't allow it to be set again");
        } catch (IllegalArgumentException e){}
    }

    public void testCobrandServedAd() throws Exception {
        _sessionContext.setCobrand("yahoo");
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _tag.setJspContext(jspContext);
        _tag.setPosition("AboveFold_300x250");

        String output = _tag.getContent();
        String expectedOutput = "<div id=\"adAboveFold_300x250\" class=\"adAboveFold_300x250 ad noprint\">\n" +
                "            <center>\n" +
                "            <script type=\"text/javascript\" src=\"http://us.adserver.yahoo.com/a?f=96345362&p=ed&l=LREC&c=r\"></script>\n" +
                "            <noscript>\n" +
                "            <iframe src=\"http://us.adserver.yahoo.com/a?f=96345362&p=ed&l=LREC&c=sh&bg=white\" align=\"center\" width=\"300\" height=\"265\" frameborder=\"no\" border=\"0\" marginwidth=\"0\" marginheight=\"0\" scrolling=\"no\"></iframe>\n" +
                "            </noscript>\n" +
                "            </center>" +
                "        </div>";
         

        XMLAssert.assertXMLEqual(encode(expectedOutput), encode(output));
    }

    public void testCobrandServedAdWithAlwaysShow() throws Exception {
        _sessionContext.setCobrand("yahoo");
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _tag.setJspContext(jspContext);
        _tag.setPosition("YouTube_381x311");
        _tag.setAlwaysShow(true);

        String output = _tag.getContent();
        String expectedOutput = "<div id=\"adYouTube_381x311\" class=\"adYouTube_381x311 ad noprint\"><script type=\"text/javascript\">GA_googleFillSlot('YouTube_381x311');</script></div>";

        XMLAssert.assertEquals(encode(expectedOutput), encode(output));
        _tag.setAlwaysShow(false);
    }

    public void testCustomCobrandWithNoAdPositionDefinedReturnsEmptyString() throws Exception {
        _sessionContext.setCobrand("yahoo");
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _tag.setJspContext(jspContext);
        _tag.setPosition("x67");

        String output = _tag.getContent();
        assertEquals("", output);
    }

    /**
     * If body content is not empty, then parse in the ad code
     * @throws Exception
     */
    public void testBodyWritten() throws Exception {
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        final JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _tag.setJspContext(jspContext);
        _tag.setJspBody( new JspFragment() {
            public void invoke(Writer writer) throws JspException, IOException {
                writer.write("<div>AD</div>$AD");
            }

            public JspContext getJspContext() {
                return jspContext;
            }
        });
        _tag.setPosition("x40");        
        final String expectedOutput = "<div id=\"adx40\" class=\"adx40 ad noprint\">" +
                "<div>AD</div>" +
                "<script type=\"text/javascript\">OAS_AD('x40');</script>" +
                "</div>";

        XMLAssert.assertXMLEqual(expectedOutput, _tag.getContent());
    }

    public void testIsMobileAdAndOffline() throws Exception {
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
        final JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);
        _tag.setJspContext(jspContext);

        // get a mobile specific position
        _tag.setPosition("Pos1_320x50");

        // answer that mobile advertising is online
        _sessionContext.setAdvertisingOnline(false);
        _sessionContext.setAdvertisingOnMobileOnline(true);
        assertFalse(_tag.isMobileAdAndOffline(_sessionContext));

        // answer that mobile advertising is offline
        _sessionContext.setAdvertisingOnMobileOnline(false);
        assertTrue(_tag.isMobileAdAndOffline(_sessionContext));

        // use a non mobile ad slot
        _tag.setPosition("Sponsor_610x30");
        _sessionContext.setAdvertisingOnline(true);

        // should answer false for both mobile advertising online and offline
        _sessionContext.setAdvertisingOnMobileOnline(true);
        assertFalse(_tag.isMobileAdAndOffline(_sessionContext));
        _sessionContext.setAdvertisingOnMobileOnline(false);
        assertFalse(_tag.isMobileAdAndOffline(_sessionContext));
    }

    public void testIsAsyncMode() throws Exception {
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
        final JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);
        _tag.setJspContext(jspContext);

        // get a mobile specific position
        _tag.setPosition("Pos1_320x50");

        // test mobile async mode
        resetAll();
        expect(_propertyDao.getProperty(IPropertyDao.GPT_ASYNCHRONOUS_MODE_ON_MOBILE_ENABLED_KEY, "true")).andReturn("true");
        replayAll();
        assertTrue(_tag.isAsyncMode(_sessionContext));

        // test mobile sync mode
        resetAll();
        expect(_propertyDao.getProperty(IPropertyDao.GPT_ASYNCHRONOUS_MODE_ON_MOBILE_ENABLED_KEY, "true")).andReturn("false").atLeastOnce();
        replayAll();
        assertFalse(_tag.isAsyncMode(_sessionContext));
        verifyAll();

        // get a desktop specific position
        _tag.setPosition("Sponsor_610x30");

        // test desktop async mode
        resetAll();
        expect(_propertyDao.getProperty(IPropertyDao.GPT_ENABLED_KEY, "false")).andReturn("true").atLeastOnce();
        expect(_propertyDao.getProperty(IPropertyDao.GPT_ASYNCHRONOUS_MODE_ENABLED_KEY, "false")).andReturn("true").atLeastOnce();
        replayAll();
        assertTrue(_tag.isAsyncMode(_sessionContext));
        verifyAll();

        // test desktop sync mode
        resetAll();
        expect(_propertyDao.getProperty(IPropertyDao.GPT_ENABLED_KEY, "false")).andReturn("true").atLeastOnce();
        expect(_propertyDao.getProperty(IPropertyDao.GPT_ASYNCHRONOUS_MODE_ENABLED_KEY, "false")).andReturn("false").atLeastOnce();
        replayAll();
        assertFalse(_tag.isAsyncMode(_sessionContext));
        verifyAll();

        // test desktop GPT off
        resetAll();
        expect(_propertyDao.getProperty(IPropertyDao.GPT_ENABLED_KEY, "false")).andReturn("false").atLeastOnce();
        replayAll();
        assertFalse(_tag.isAsyncMode(_sessionContext));
        verifyAll();


    }

    /**
     * even if body contains values, cobrand served ads will not use body content
     * @throws Exception
     */
    public void testCobrandServedAdDoesNotUseBodyContent() throws Exception {
        _sessionContext.setCobrand("yahoo");
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        final JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _tag.setJspContext(jspContext);
        _tag.setJspBody( new JspFragment() {
            public void invoke(Writer writer) throws JspException, IOException {
                writer.write("<div>AD</div>$AD");
            }

            public JspContext getJspContext() {
                return jspContext;
            }
        });
        _tag.setPosition("AboveFold_300x250");

        String expectedOutput = "<div id=\"adAboveFold_300x250\" class=\"adAboveFold_300x250 ad noprint\">\n" +
                "            <center>\n" +
                "            <script type=\"text/javascript\" src=\"http://us.adserver.yahoo.com/a?f=96345362&p=ed&l=LREC&c=r\"></script>\n" +
                "            <noscript>\n" +
                "            <iframe src=\"http://us.adserver.yahoo.com/a?f=96345362&p=ed&l=LREC&c=sh&bg=white\" align=\"center\" width=\"300\" height=\"265\" frameborder=\"no\" border=\"0\" marginwidth=\"0\" marginheight=\"0\" scrolling=\"no\"></iframe>\n" +
                "            </noscript>\n" +
                "            </center>" +
                "        </div>";

        XMLAssert.assertXMLEqual(encode(expectedOutput), encode(_tag.getContent()));
    }

    public void testExceptionThrownIfBodyContentDoesNotContainAdKeyword() {
        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        final JspContext jspContext = new MockPageContext(new MockServletContext(), _request);
        jspContext.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _tag.setJspContext(jspContext);
        _tag.setJspBody( new JspFragment() {
            public void invoke(Writer writer) throws JspException, IOException {
                writer.write("<div>AD</div>");
            }

            public JspContext getJspContext() {
                return jspContext;
            }
        });
        _tag.setPosition("x40");
        try {
            _tag.getContent();
            fail("body does not contain $AD keyword");
        } catch (IllegalStateException e) {} catch (IOException e) {} catch (JspException e) {}
    }

    private String encode(String s) {
        s = s.replaceAll("&", "&amp;");
        return s;
    }
}
