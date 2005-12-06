package gs.web.search;

import gs.web.BaseTestCase;
import gs.data.search.Searcher;
import gs.data.search.IndexDir;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CityDistrictControllerTest extends BaseTestCase {

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
