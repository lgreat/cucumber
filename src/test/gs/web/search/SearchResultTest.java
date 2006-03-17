package gs.web.search;

import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Explanation;
import org.apache.commons.lang.StringUtils;
import gs.data.search.IndexField;

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

    public void testGetHeadline() {
        // note that the order the fields are added is important.
        Document doc = new Document();
        doc.add(Field.Text("city", "San Francisco"));
        SearchResult result = new SearchResult(doc);
        assertEquals("San Francisco", result.getHeadline());

        doc.add(Field.Text("term", "SAT"));
        result = new SearchResult(doc);
        assertEquals("SAT", result.getHeadline());

        doc.add(Field.Text("title", "How my cat ate my mother."));
        result = new SearchResult(doc);
        assertEquals("How my cat ate my mother.", result.getHeadline());

        doc.add(Field.Text("name", "West High School"));
        result = new SearchResult(doc);
        assertEquals("West High School", result.getHeadline());
    }

    public void testGetContext() {
        Document doc = new Document();
        SearchResult result = new SearchResult(doc);
        assertNull(result.getContext());

        doc.add(Field.Text("type", "district"));
        result = new SearchResult(doc);
        assertNull(result.getContext());

        doc = new Document();
        doc.add(Field.Text("type", "term"));
        doc.add(Field.Text("definition", "to dig a hole, with gusto"));
        result = new SearchResult(doc);
        assertEquals(SearchResult.TERM, result.getType());
        assertEquals("to dig a hole, with gusto", result.getContext());

        doc = new Document();
        doc.add(Field.Text("type", "article"));
        doc.add(Field.Text("definition", "to dig a hole, with gusto"));
        doc.add(Field.Text("abstract", "jasper johns was a silly penguin"));
        result = new SearchResult(doc);
        assertEquals(SearchResult.ARTICLE, result.getType());
        assertEquals("jasper johns was a silly penguin", result.getContext());

        doc = new Document();
        doc.add(Field.Text("type", "school"));
        result = new SearchResult(doc);
        assertEquals(SearchResult.SCHOOL, result.getType());
        assertTrue(StringUtils.isBlank(result.getContext()));
        //assertEquals("jasper johns was a silly penguin", result.getContext());
    }

    public void testGetType() {
        Document doc = new Document();
        doc.add(Field.Text("type", "school"));
        SearchResult sr = new SearchResult(doc);
        assertEquals(SearchResult.SCHOOL, sr.getType());

        doc = new Document();
        doc.add(Field.Text("type", "city"));
        doc.add(Field.Text("city", "New Orleans"));
        sr = new SearchResult(doc);
        assertEquals(SearchResult.CITY, sr.getType());
        assertEquals("New Orleans", sr.getCity());
    }

    public void testExplanation() {
        SearchResult result = new SearchResult(null);
        assertEquals("", result.getExplanation());

        Explanation ex = new Explanation();
        ex.setValue(2.2f);
        ex.setDescription("Oh me oh my");
        result.setExplanation(ex);
        assertEquals("2.2 = Oh me oh my", result.getExplanation());
    }

    public void testGetSchools() {
        Document doc = new Document();
        SearchResult result = new SearchResult(doc);
        assertEquals(0, result.getSchools());

        doc = new Document();
        doc.add(Field.Text(IndexField.SCHOOLS, "1234"));
        result = new SearchResult(doc);
        assertEquals(1234, result.getSchools());
    }

    public void testGetAddress() {
        Document doc = new Document();
        SearchResult result = new SearchResult(doc);
        assertTrue(StringUtils.isBlank(result.getAddress()));

        doc.add(Field.Text("street", "222 2nd St."));
        result = new SearchResult(doc);
        assertEquals("222 2nd St.", result.getAddress());

        doc.add(Field.Text("city", "Tahachepe"));
        result = new SearchResult(doc);
        assertEquals("222 2nd St.,  Tahachepe", result.getAddress());

        doc.add(Field.Text("state", "ca"));
        result = new SearchResult(doc);
        assertEquals("222 2nd St.,  Tahachepe, CA", result.getAddress());

        doc.add(Field.Text("zip", "12345"));
        result = new SearchResult(doc);
        assertEquals("222 2nd St.,  Tahachepe, CA 12345", result.getAddress());
        // this repleated assert tests a conditional in getAddress();
        assertEquals("222 2nd St.,  Tahachepe, CA 12345", result.getAddress());
    }

    public void testGetInsider() {
        Document doc = new Document();
        SearchResult result = new SearchResult(doc);
        assertFalse(result.isInsider());

        doc.add(Field.Text("insider", "true"));
        result = new SearchResult(doc);
        assertTrue(result.isInsider());
    }

    public void testGetSchoolType() {
        Document doc = new Document();
        SearchResult result = new SearchResult(doc);
        assertNull(result.getSchoolType());

        doc.add(Field.Text("schooltype", "public"));
        result = new SearchResult(doc);
        assertEquals("public", result.getSchoolType());
    }
}
