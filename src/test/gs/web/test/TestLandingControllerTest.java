package gs.web.test;

import gs.web.BaseControllerTestCase;
import gs.web.util.list.Anchor;
import gs.web.util.UrlBuilder;
import gs.data.school.School;
import gs.data.state.State;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author chriskimm@greatschools.net
 */
public class TestLandingControllerTest extends BaseControllerTestCase {

    private TestLandingController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (TestLandingController)getApplicationContext().getBean(TestLandingController.BEAN_ID);
    }

    public void testHandleRequestWithNoParams() throws Exception {
        getRequest().setMethod("GET");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertEquals("State and tid parameters are required", "/test/landing", mAndV.getViewName());
    }

    public void testSubmitAchievementForm() throws Exception {

        School school = new School();
        school.setDatabaseState(State.CA);
        school.setId(1);

        getRequest().setMethod("POST");
        getRequest().setParameter("type", "achievement");
        getRequest().setParameter("sid", String.valueOf(school.getId()));
        getRequest().setParameter("state", school.getDatabaseState().getAbbreviationLowerCase());
        
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        RedirectView view = (RedirectView)mAndV.getView();
        UrlBuilder builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_TEST_SCORE);
        assertEquals (builder.asSiteRelative(getRequest()), view.getUrl());

        getRequest().setParameter("type", "compare");
        mAndV = _controller.handleRequest(getRequest(), getResponse());
        view = (RedirectView)mAndV.getView();
        assertEquals ("/cgi-bin/cs_compare/ca?area=m&city=null&level=null&sortby=distance&tab=over", view.getUrl());
    }


    public void testRequestForm() throws Exception {
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

    public void testLoadCacheMinimal() throws Exception {
        TestLandingController controller = (TestLandingController)getApplicationContext().getBean(TestLandingController.BEAN_ID);
        Map<String, Map> data = new HashMap<String, Map>();
        controller.loadCache(data);
        assertFalse(data.isEmpty());
    }
}
