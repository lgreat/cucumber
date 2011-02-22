package gs.web.search;

import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

public class BaseLuceneSearchServiceTest extends TestCase {

    BaseLuceneSearchService abc = new BaseLuceneSearchService<SolrSchoolSearchResult>() {

        @Override
        public SearchResultsPage<SolrSchoolSearchResult> search(String queryString, Map<? extends IFieldConstraint, String> fieldConstraints, List<FilterGroup> filters, FieldSort fieldSort, int offset, int count) throws SearchException {
            return null; 
        }
    };

    public void testCleanseSearchString() throws Exception {

        String test = "!";

        String cleansed = abc.cleanseSearchString(test);

        assertNull("search string should have been cleansed to null", cleansed);
    }

    public void testCleanseSearchString2() throws Exception {
        String test = "?";

        String cleansed = abc.cleanseSearchString(test);

        assertNull("search string should have been cleansed to null", cleansed);
    }

    public void testCleanseSearchString3() throws Exception {
        String test = "!?";

        String cleansed = abc.cleanseSearchString(test);

        assertNull("search string should have been cleansed to null", cleansed);
    }

    public void testCleanseSearchString4() throws Exception {
        String test = "!?#$(*&(";

        String cleansed = abc.cleanseSearchString(test);

        assertNull("search string should have been cleansed to null", cleansed);
    }

    public void testCleanseSearchString5() throws Exception {
        String test = "George's preschool:";

        String cleansed = abc.cleanseSearchString(test);

        assertEquals("search string should have been escaped", "george's preschool\\:", cleansed);
    }
}
