package gs.web.test;

import gs.web.BaseControllerTestCase;
import gs.web.util.list.Anchor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.net.URL;

/**
 * @author chriskimm@greatschools.net
 */
public class TestTestLandingController extends BaseControllerTestCase {

    private TestLandingController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (TestLandingController)getApplicationContext().getBean(TestLandingController.BEAN_ID);
        URL testUrl = new URL("http://spreadsheets.google.com/feeds/worksheets/o02465749437938730339.5684652189673031494/private/full/od6");
        _controller.setWorksheetUrl(testUrl);
    }

    public void testRequestForm() throws Exception {
        getRequest().setMethod("GET");
        getRequest().setParameter("state", "FL");
        getRequest().setParameter("tid", "1");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertEquals("/test/landing", mAndV.getViewName());
        assertEquals("Florida Camping and Testing", mAndV.getModel().get("displayname"));
        assertEquals("Here is some FL FCAT text.", mAndV.getModel().get("info"));
        List<Anchor> anchors = (List<Anchor>)mAndV.getModel().get("links");
        Anchor a1 = anchors.get(0);
        assertEquals("link text 1", a1.getContents());
        assertEquals("http://www.google.com", a1.getHref());
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

        data = "aid:21\ntest,foofoo\naid:101";
        aList = _controller.parseAnchorList(data);
        assertEquals(3, aList.size());
        assertEquals("What Does the School Secretary Do?", aList.get(0).getContents());
        assertEquals("/cgi-bin/showarticle/21", aList.get(0).getHref());
        assertEquals("test", aList.get(1).getContents());
        assertEquals("foofoo", aList.get(1).getHref());
        assertEquals("Alabama Schools: Key Facts and Resources", aList.get(2).getContents());
        assertEquals("/cgi-bin/showarticle/101", aList.get(2).getHref());
    }
}
