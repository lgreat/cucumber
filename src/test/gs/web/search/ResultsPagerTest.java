package gs.web.search;

import junit.framework.TestCase;

import java.util.List;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ResultsPagerTest extends TestCase {

    public void testNullLoad() {
        ResultsPager pager = new ResultsPager();
        pager.load(null, null);
        assertNotNull(pager.getResults(1,1));
    }

    public void testGetSchools() {
        ResultsPager pager = new ResultsPager();
        assertNotNull(pager.getSchools(1,1));
        
    }

    public void testGetPage() {
        ResultsPager pager = new ResultsPager();
        List list = pager.getPage(null, 1,1);
        assertNotNull(list);
    }
}
