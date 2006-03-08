package gs.web.search;

import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SearchResultTest extends TestCase {

    public void testGetFields() {
        Document doc = new Document();
        doc.add(Field.Text("name", "pablo"));
        doc.add(Field.Text("id", "1234"));

        SearchResult sr = new SearchResult(doc);
        assertEquals("pablo", sr.getName());
        assertEquals("1234", sr.getId());
    }

    public void testGetType() {
        Document doc = new Document();
        doc.add(Field.Text("type", "school"));
        SearchResult sr = new SearchResult(doc);
        assertEquals(SearchResult.SCHOOL, sr.getType());
    }
}
