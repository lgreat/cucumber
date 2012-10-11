package gs.web.test;

import gs.web.BaseControllerTestCase;
import gs.web.util.list.Anchor;
import gs.web.util.UrlBuilder;
import gs.data.util.google.GoogleSpreadsheetDaoFactory;
import gs.data.state.State;
import gs.data.util.table.ITableDao;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author chriskimm@greatschools.org
 */
public class TestLandingControllerTest extends BaseControllerTestCase {

    private TestLandingController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (TestLandingController)getApplicationContext().getBean(TestLandingController.BEAN_ID);
        GoogleSpreadsheetDaoFactory factory = new GoogleSpreadsheetDaoFactory();
        factory.setGoogleKey("pYwV1uQwaOCJGhxtFDPHjTg");
        factory.setVisibility("public");
        factory.setProjection("values");
        factory.setWorksheetName("od6");
        ITableDao tableDao = factory.getTableDao();
        _controller.setTableDao(tableDao);
    }



    public void testGetCityBrowseUrlBuilder() {
        UrlBuilder urlBuilder = TestLandingController.getCityBrowseUrlBuilder(State.CA, "San Francisco", "e");
        assertEquals("/california/san-francisco/schools/?gradeLevels=e&st=public&st=charter", urlBuilder.asSiteRelative(getRequest()));
        urlBuilder = TestLandingController.getCityBrowseUrlBuilder(State.CA, "San Francisco", "m");
        assertEquals("/california/san-francisco/schools/?gradeLevels=m&st=public&st=charter", urlBuilder.asSiteRelative(getRequest()));
        urlBuilder = TestLandingController.getCityBrowseUrlBuilder(State.CA, "San Francisco", "h");
        assertEquals("/california/san-francisco/schools/?gradeLevels=h&st=public&st=charter", urlBuilder.asSiteRelative(getRequest()));

        boolean threwException = false;
        try {
            TestLandingController.getCityBrowseUrlBuilder(State.CA, "San Francisco", null);
        } catch (IllegalArgumentException e) {
            threwException = true;
        }
        assertTrue(threwException);

        threwException = false;
        try {
            TestLandingController.getCityBrowseUrlBuilder(State.CA, null, "e");
        } catch (IllegalArgumentException e) {
            threwException = true;
        }
        assertTrue(threwException);

        threwException = false;
        try {
            TestLandingController.getCityBrowseUrlBuilder(null, "San Francisco", "e");
        } catch (IllegalArgumentException e) {
            threwException = true;
        }
        assertTrue(threwException);
    }

    public void xtestHandleRequestWithNoParams() throws Exception {
        getRequest().setMethod("GET");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertEquals("State and tid parameters are required", "/test/landing", mAndV.getViewName());
    }

    public void xtestRequestForm() throws Exception {
        getRequest().setMethod("GET");
        getRequest().setParameter("state", "FL");
        getRequest().setParameter("tid", "1");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertEquals("/test/landing", mAndV.getViewName());
        assertTrue(mAndV.getModel().get("displayname").toString().contains("Florida"));
        assertTrue(mAndV.getModel().get("info").toString().contains("FCAT"));
        List<Anchor> anchors = (List<Anchor>)mAndV.getModel().get("links");
        Anchor a1 = anchors.get(0);
        assertNotNull(a1.getContents());
        assertNotNull(a1.getHref());
    }

    public void xtestRequestFormWithLowercaseState() throws Exception {
        getRequest().setMethod("GET");
        getRequest().setParameter("state", "fl");
        getRequest().setParameter("tid", "1");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertEquals("/test/landing", mAndV.getViewName());
        assertTrue(mAndV.getModel().get("displayname").toString().contains("Florida"));
        assertTrue(mAndV.getModel().get("info").toString().contains("FCAT"));
        List<Anchor> anchors = (List<Anchor>)mAndV.getModel().get("links");
        Anchor a1 = anchors.get(0);
        assertNotNull(a1.getContents());
        assertNotNull(a1.getHref());
    }

    public void testParseAnchorList() throws Exception {
        List<Anchor> aList = _controller.parseAnchorList(null);
        assertEquals(0, aList.size());
        aList = _controller.parseAnchorList("");
        assertEquals(0, aList.size());

        String data = "text,http://www.href.com\ntext2,http://www.href2.com";
        aList = _controller.parseAnchorList(data);
        assertEquals(2, aList.size());
        assertEquals("text", aList.get(0).getContents());
        assertEquals("http://www.href.com", aList.get(0).getHref());
        assertEquals("text2", aList.get(1).getContents());
        assertEquals("http://www.href2.com", aList.get(1).getHref());

        data = "text,href\ntext2,.,x,\ntext2;href2\ntext3,href3";
        aList = _controller.parseAnchorList(data);
        assertEquals(2, aList.size());
        assertEquals("text3", aList.get(1).getContents());
        assertEquals("href3", aList.get(1).getHref());

        data = "aid:21\ntest,foofoo\nbreak\naid:101\nbreak\nfoo,bar";
        aList = _controller.parseAnchorList(data);
        assertEquals(6, aList.size());
        assertEquals("What Does the School Secretary Do?", aList.get(0).getContents());
        assertEquals("/cgi-bin/showarticle/21", aList.get(0).getHref());
        assertEquals("test", aList.get(1).getContents());
        assertEquals("foofoo", aList.get(1).getHref());
        assertNull(aList.get(2).getHref());
        assertNull(aList.get(2).getContents());
        assertEquals("Alabama Schools: Key Facts and Resources", aList.get(3).getContents());
        assertEquals("/cgi-bin/showarticle/101", aList.get(3).getHref());
        assertNull(aList.get(4).getHref());
        assertNull(aList.get(4).getContents());
        assertEquals("foo", aList.get(5).getContents());
        assertEquals("bar", aList.get(5).getHref());        
    }

    public void xtestLoadCacheMinimal() throws Exception {
        TestLandingController controller = (TestLandingController)getApplicationContext().getBean(TestLandingController.BEAN_ID);
        Map<String, Map> data = new HashMap<String, Map>();
        controller.loadCache(data);
        assertFalse(data.isEmpty());
    }
}
