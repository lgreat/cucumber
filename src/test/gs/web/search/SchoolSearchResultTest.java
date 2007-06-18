package gs.web.search;

import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import gs.data.search.Indexer;
import gs.data.school.School;

public class SchoolSearchResultTest extends TestCase {
    public void testGreatSchoolsRatingComesFromIndex() {
        Document doc = new Document();
        doc.add(Field.Text(Indexer.OVERALL_RATING, "4"));
        SchoolSearchResult result = new SchoolSearchResult(doc);

        assertEquals("Unexpected greatschools rating", "4", result.getGreatSchoolsRating());
    }

    public void testSchoolIsInjected() {
        SchoolSearchResult result = new SchoolSearchResult(new Document());

        assertNull("School should be null by default", result.getSchool());

        School expectedSchool = new School();
        result.setSchool(expectedSchool);

        assertEquals("Unexpected school", expectedSchool, result.getSchool());
    }
}
