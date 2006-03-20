package gs.web.search;

import gs.web.BaseControllerTestCase;
import gs.web.MockHttpServletRequest;
import gs.data.search.Searcher;
import gs.data.search.IndexDir;
import gs.data.search.Indexer;
import gs.data.search.GSAnalyzer;
import gs.data.state.State;
import gs.data.school.district.District;
import gs.data.util.Address;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexWriter;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.ArrayList;

/**
 * Tests gs.web.search.CityDistrictController
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CityDistrictControllerTest extends BaseControllerTestCase {

    private Directory _testDir;
    private CityDistrictController _controller;

    protected void setUp() throws Exception {
        super.setUp();
        Indexer indexer = (Indexer) getApplicationContext().getBean(Indexer.BEAN_ID);
        _testDir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(_testDir, new GSAnalyzer(), true);
        indexer.indexCities(State.AK, writer);
        indexer.indexDistricts(getDistricts(), writer);
        _controller =
                (CityDistrictController)getApplicationContext().
                        getBean(CityDistrictController.BEAN_ID);
    }

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


    /**
     * @todo CK fix so that there are asserts
     * @throws Exception
     */
    public void testCities() throws Exception {

        IndexDir testIndexDir = new IndexDir(_testDir, new RAMDirectory());
        Searcher searcher = new Searcher(testIndexDir);
        CityDistrictController controller = (CityDistrictController)getApplicationContext().getBean(CityDistrictController.BEAN_ID);
        ModelAndView mv = controller.handleRequestInternal(getRequest(), null);
    }

    private List getDistricts() {
        List districts = new ArrayList();

        districts.add(createDistrict("District A"));

        return districts;
    }

    private District createDistrict(String name) {
        District district = new District();
        district.setName(name);
        Address address = new Address();
        address.setStreet("1234 Foo Lane");
        address.setCity("Fooville");
        address.setState(State.CA);
        address.setZip("12345");
        district.setPhysicalAddress(address);
        return district;
    }
}
