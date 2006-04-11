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
            _rtth.getSrcQuery();
            fail("TagHandler should not work without a JspContext object");
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        MockPageContext context = new MockPageContext();
        _rtth.setJspContext(context);

        context.setAttribute("q", null);
        assertEquals("", _rtth.getSrcQuery());

        context.setAttribute("q", "  ");
        assertEquals("", _rtth.getSrcQuery());

        context.setAttribute("q", "This is a query");
        assertEquals("This is a query", _rtth.getSrcQuery());
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

    public void testWritePageNumbersMinimal() throws IOException {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();

        MockPageContext context = new MockPageContext();
        _rtth.setJspContext(context);
        _rtth.setTotal(30);

        UrlBuilder builder = new UrlBuilder(request, "/foo/bar");
        builder.setParameter("state", "CA");
        builder.setParameter("q", "");
        _rtth.writePageNumbers(1, request, builder, 30);

        MockJspWriter mockWriter = (MockJspWriter)context.getOut();

        StringBuffer buffer = new StringBuffer();
        buffer.append("<span class=\"active pad\">1</span><a href=\"/foo/bar?p=2&amp;q=&amp;state=CA\" class=\"pad\">2</a>\n");
        buffer.append("<a href=\"/foo/bar?p=3&amp;q=&amp;state=CA\" class=\"pad\">3</a>\n");
        buffer.append("<a href=\"/foo/bar?p=2&amp;q=&amp;state=CA\" class=\"pad\">Next &#160;&gt;</a>");

        assertEquals(buffer.toString(), mockWriter.getOutputBuffer().toString().trim());
    }

    public void testWritePageNumbersCity() throws IOException {
        GsMockHttpServletRequest mockRequest = new GsMockHttpServletRequest();
        mockRequest.setAttribute(SchoolsController.MODEL_LEVEL_CODE, new String[] {"elementary"});

        MockPageContext context = new MockPageContext();
        context.setAttribute("city", "alameda");
        context.setAttribute("p", "3");
        mockRequest.setParameter("city", "alameda");
        mockRequest.setParameter("p", "3");
        _rtth.setJspContext(context);
        _rtth.setTotal(100);

        UrlBuilder builder = new UrlBuilder(mockRequest, "/foo/bar");
        builder.setParameter("state", "CA");
        builder.setParameter("city", "alameda");
        builder.setParameter("q", "");

        _rtth.writePageNumbers(3, mockRequest, builder, 100);


        MockJspWriter mockWriter = (MockJspWriter)context.getOut();

        StringBuffer buffer = new StringBuffer();
        buffer.append("<a href=\"/foo/bar?city=alameda&amp;p=2&amp;q=&amp;state=CA\" class=\"pad\">&lt;&#160;Previous</a>\n");
        buffer.append("<a href=\"/foo/bar?city=alameda&amp;p=1&amp;q=&amp;state=CA\" class=\"pad\">1</a>\n");
        buffer.append("<a href=\"/foo/bar?city=alameda&amp;p=2&amp;q=&amp;state=CA\" class=\"pad\">2</a>\n");
        buffer.append("<span class=\"active pad\">3</span>");
        buffer.append("<a href=\"/foo/bar?city=alameda&amp;p=4&amp;q=&amp;state=CA\" class=\"pad\">4</a>\n");
        buffer.append("<a href=\"/foo/bar?city=alameda&amp;p=5&amp;q=&amp;state=CA\" class=\"pad\">5</a>\n");
        buffer.append("<a href=\"/foo/bar?city=alameda&amp;p=6&amp;q=&amp;state=CA\" class=\"pad\">6</a>\n");
        buffer.append("<a href=\"/foo/bar?city=alameda&amp;p=7&amp;q=&amp;state=CA\" class=\"pad\">7</a>\n");
        buffer.append("<a href=\"/foo/bar?city=alameda&amp;p=8&amp;q=&amp;state=CA\" class=\"pad\">8</a>\n");
        buffer.append("<a href=\"/foo/bar?city=alameda&amp;p=9&amp;q=&amp;state=CA\" class=\"pad\">9</a>\n");
        buffer.append("<a href=\"/foo/bar?city=alameda&amp;p=10&amp;q=&amp;state=CA\" class=\"pad\">10</a>\n");
        buffer.append("<a href=\"/foo/bar?city=alameda&amp;p=4&amp;q=&amp;state=CA\" class=\"pad\">Next &#160;&gt;</a>\n");
        assertEquals(buffer.toString().trim(), mockWriter.getOutputBuffer().toString().trim());
    }
}
