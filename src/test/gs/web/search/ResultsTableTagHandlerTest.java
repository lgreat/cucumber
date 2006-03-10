package gs.web.search;

import gs.web.BaseTestCase;
import org.springframework.mock.web.MockPageContext;

import java.util.List;
import java.util.ArrayList;

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
        assertEquals("", _rtth.getQueryString());

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

    }
}
