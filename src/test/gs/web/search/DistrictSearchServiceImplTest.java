package gs.web.search;

import gs.data.state.State;
import junit.framework.TestCase;
import org.apache.lucene.search.Query;

public class DistrictSearchServiceImplTest extends TestCase {

    DistrictSearchServiceImpl _districtSearchService;

    public void setUp() throws Exception {
        _districtSearchService = new DistrictSearchServiceImpl();
    }

    public void testBuildQueryWithSpecialCharacterOnlySearchString() throws Exception {
        String searchString = "?";

        Query query = _districtSearchService.buildQuery(searchString, null);

        assertNull("meaningless search string should return null query, for now",query);
    }

    public void testBuildQueryWithSpecialCharacterOnlySearchString2() throws Exception {
        String searchString = "!@#";

        Query query = _districtSearchService.buildQuery(searchString, null);

        assertNull("meaningless search string should return null query, for now",query);
    }

    public void testBuildQueryWithSpecialCharacterOnlySearchString3() throws Exception {
        String searchString = "!@#";

        Query query = _districtSearchService.buildQuery(searchString, State.CA);

        assertNull("meaningless search string + field constraints should return null query, for now",query);
    }

    public void testBuildQueryWithNoMeaningfulInput() throws Exception {
        String searchString = "";

        try {
            Query query = _districtSearchService.buildQuery(searchString, null);
            fail("meaningless search string + field constraints should return null query, for now");
        } catch (IllegalArgumentException e) {

        }
    }
}
