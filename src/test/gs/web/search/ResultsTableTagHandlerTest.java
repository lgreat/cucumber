package gs.web.search;

import gs.web.BaseTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.school.SchoolsController;
import gs.web.util.UrlBuilder;
import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
//import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ResultsTableTagHandlerTest extends BaseTestCase {

    private ResultsTableTagHandler _rtth;

    protected void setUp() throws Exception {
        super.setUp();
        _rtth = new ResultsTableTagHandler() {
            public String getConstraint() {
                return "school";
            }
        };
    }

    public void testGetPage() {

        assertEquals(1, _rtth.getPage());

        MockPageContext context = new MockPageContext();
        context.setAttribute("p", "2");
        _rtth.setJspContext(context);
        assertEquals(2, _rtth.getPage());

        context.setAttribute("p", "");
        assertEquals(1, _rtth.getPage());

        context.setAttribute("p", null);
        assertEquals(1, _rtth.getPage());

        context.setAttribute("p", "-1");
        assertEquals(1, _rtth.getPage());
    }

    public void testReverse() {
        assertFalse(_rtth.sortReverse());

        _rtth.setReverse("t");
        assertTrue(_rtth.sortReverse());

        _rtth.setReverse("f");
        assertFalse(_rtth.sortReverse());

        _rtth.setReverse(null);
        assertFalse(_rtth.sortReverse());

        _rtth.setReverse("");
        assertFalse(_rtth.sortReverse());
    }

    public void testQueryString() {
        try {
            _rtth.getQueryString();
            fail("TagHandler should not work without a JspContext object");
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        MockPageContext context = new MockPageContext();
        _rtth.setJspContext(context);

        context.setAttribute("q", null);
        assertEquals("", _rtth.getQueryString());

        context.setAttribute("q", "  ");
        assertEquals("", _rtth.getQueryString());

        context.setAttribute("q", "This is a query");
        assertEquals("This is a query", _rtth.getQueryString());
    }

    public void testGettersAndSetters() {

        List testList = new ArrayList();
        testList.add("foo");

        _rtth.setResults(testList);
        List resultList = _rtth.getResults();
        assertEquals("foo", (String)resultList.get(0));

        assertNull(_rtth.getSortColumn());
        _rtth.setSortColumn("boo");
        assertEquals("boo", _rtth.getSortColumn());

        assertFalse(_rtth._debug);
        _rtth.setDebug(true);
        assertTrue(_rtth._debug);
    }

    public void testWritePageNumbers_minimal() throws IOException {
        GsMockHttpServletRequest mockRequest = new GsMockHttpServletRequest();
        UrlBuilder builder = new UrlBuilder(mockRequest, "/foo/bar");
        MockPageContext context = new MockPageContext();
        _rtth.setJspContext(context);
        _rtth.setTotal(30);
        _rtth.writePageNumbers(builder);

        MockJspWriter mockWriter = (MockJspWriter)context.getOut();

        StringBuffer buffer = new StringBuffer();
        buffer.append("<span class=\"active pad\">1</span><a class=\"pad\" href=\"/foo/bar?q=&state=ca&amp;p=2\">2</a>\n");
        buffer.append("<a class=\"pad\" href=\"/foo/bar?q=&state=ca&amp;p=3\">3</a>\n");
        buffer.append("<a class=\"pad\" href=\"/foo/bar?q=&state=ca&amp;p=2\">Next &#160;&gt;</a>");
        assertEquals(buffer.toString(), mockWriter.getOutputBuffer().toString().trim());
    }

    public void testWritePageNumbers_city() throws IOException {
        GsMockHttpServletRequest mockRequest = new GsMockHttpServletRequest();
        mockRequest.setAttribute(SchoolsController.MODEL_LEVEL_CODE, new String[] {"elementary"});
        UrlBuilder builder = new UrlBuilder(mockRequest, "/foo/bar");
        MockPageContext context = new MockPageContext();
        context.setAttribute("city", "alameda");
        context.setAttribute("p", "3");
        _rtth.setJspContext(context);
        _rtth.setTotal(100);
        _rtth.writePageNumbers(builder);
        MockJspWriter mockWriter = (MockJspWriter)context.getOut();

        StringBuffer buffer = new StringBuffer();
        buffer.append("<a class=\"pad\" href=\"/foo/bar?q=&city=alameda&state=ca&amp;p=2\">&lt;&#160;Previous</a>\n");
        buffer.append("<a class=\"pad\" href=\"/foo/bar?q=&city=alameda&state=ca&amp;p=1\">1</a>\n");
        buffer.append("<a class=\"pad\" href=\"/foo/bar?q=&city=alameda&state=ca&amp;p=2\">2</a>\n");
        buffer.append("<span class=\"active pad\">3</span><a class=\"pad\" href=\"/foo/bar?q=&city=alameda&state=ca&amp;p=4\">4</a>\n");
        buffer.append("<a class=\"pad\" href=\"/foo/bar?q=&city=alameda&state=ca&amp;p=5\">5</a>\n");
        buffer.append("<a class=\"pad\" href=\"/foo/bar?q=&city=alameda&state=ca&amp;p=6\">6</a>\n");
        buffer.append("<a class=\"pad\" href=\"/foo/bar?q=&city=alameda&state=ca&amp;p=7\">7</a>\n");
        buffer.append("<a class=\"pad\" href=\"/foo/bar?q=&city=alameda&state=ca&amp;p=8\">8</a>\n");
        buffer.append("<a class=\"pad\" href=\"/foo/bar?q=&city=alameda&state=ca&amp;p=9\">9</a>\n");
        buffer.append("<a class=\"pad\" href=\"/foo/bar?q=&city=alameda&state=ca&amp;p=10\">10</a>\n");
        buffer.append("<a class=\"pad\" href=\"/foo/bar?q=&city=alameda&state=ca&amp;p=4\">Next &#160;&gt;</a>\n");
        assertEquals(buffer.toString().trim(), mockWriter.getOutputBuffer().toString().trim());
    }
}
