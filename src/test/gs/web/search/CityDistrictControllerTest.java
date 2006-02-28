package gs.web.search;

import gs.web.BaseControllerTestCase;
import gs.web.MockHttpServletRequest;
import gs.data.search.Searcher;
import gs.data.search.IndexDir;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/**
 * Tests gs.web.search.CityDistrictController
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CityDistrictControllerTest extends BaseControllerTestCase {

    public void testNullSearcher() throws Exception {
        try {
            CityDistrictController controller = new CityDistrictController(null);
            MockHttpServletRequest request = getRequest();
            request.setParameter("q", "foo");
            controller.handleRequestInternal(request, getResponse());
            fail("CityDistrictController must not accept a null Searcher");
        } catch (RuntimeException expected) {
            assertTrue(true);
        }
    }

    public void testCities() throws Exception {

        Directory mainDir = new RAMDirectory();
        Directory spellDir = new RAMDirectory();

        IndexDir testIndexDir = new IndexDir(mainDir, spellDir);
        Searcher searcher = new Searcher(testIndexDir);
        CityDistrictController controller =
                new CityDistrictController(searcher);
        //ModelAndView mv = controller.handleRequestInternal(null, null);
    }
}
