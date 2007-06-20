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
        doc.add(new Field("name", "pablo", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("id", "1234", Field.Store.YES, Field.Index.TOKENIZED));

        SearchResult sr = new SearchResult(doc);
        assertEquals("pablo", sr.getName());
        assertEquals("1234", sr.getId());
    }

    public void testGetHeadline() {
        // note that the order the fields are added is important.
        Document doc = new Document();
        doc.add(new Field("city", "San Francisco", Field.Store.YES, Field.Index.TOKENIZED));
        SearchResult result = new SearchResult(doc);
        assertEquals("San Francisco", result.getHeadline());

        doc.add(new Field("term", "SAT", Field.Store.YES, Field.Index.TOKENIZED));
        result = new SearchResult(doc);
        assertEquals("SAT", result.getHeadline());

        doc.add(new Field("title", "How my cat ate my mother.", Field.Store.YES, Field.Index.TOKENIZED));
        result = new SearchResult(doc);
        assertEquals("How my cat ate my mother.", result.getHeadline());

        doc.add(new Field("name", "West High School", Field.Store.YES, Field.Index.TOKENIZED));
        result = new SearchResult(doc);
        assertEquals("West High School", result.getHeadline());
    }

    public void testGetContext() {
        Document doc = new Document();
        SearchResult result = new SearchResult(doc);
        assertNull(result.getContext());

        doc.add(new Field("type", "district", Field.Store.YES, Field.Index.TOKENIZED));
        result = new SearchResult(doc);
        assertNull(result.getContext());

        doc = new Document();
        doc.add(new Field("type", "term", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("definition", "to dig a hole, with gusto",
                Field.Store.YES, Field.Index.TOKENIZED));
        result = new SearchResult(doc);
        assertEquals(SearchResult.TERM, result.getType());
        assertEquals("to dig a hole, with gusto", result.getContext());

        doc = new Document();
        doc.add(new Field("type", "article", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("definition", "to dig a hole, with gusto",
                Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("abstract", "jasper johns was a silly penguin",
                Field.Store.YES, Field.Index.TOKENIZED));
        result = new SearchResult(doc);
        assertEquals(SearchResult.ARTICLE, result.getType());
        assertEquals("jasper johns was a silly penguin", result.getContext());

        doc = new Document();
        doc.add(new Field("type", "school", Field.Store.YES, Field.Index.TOKENIZED));
        result = new SearchResult(doc);
        assertEquals(SearchResult.SCHOOL, result.getType());
        assertTrue(StringUtils.isBlank(result.getContext()));
        //assertEquals("jasper johns was a silly penguin", result.getContext());
    }

    public void testGetType() {
        Document doc = new Document();
        doc.add(new Field("type", "school", Field.Store.YES, Field.Index.TOKENIZED));
        SearchResult sr = new SearchResult(doc);
        assertEquals(SearchResult.SCHOOL, sr.getType());

        doc = new Document();
        doc.add(new Field("type", "city", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("city", "New Orleans", Field.Store.YES, Field.Index.TOKENIZED));
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
        doc.add(new Field(IndexField.NUMBER_OF_SCHOOLS, "1234",
                Field.Store.YES, Field.Index.TOKENIZED));
        result = new SearchResult(doc);
        assertEquals(1234, result.getSchools());
    }

    public void testGetAddress() {
        Document doc = new Document();
        SearchResult result = new SearchResult(doc);
        assertTrue(StringUtils.isBlank(result.getAddress()));

        doc.add(new Field("street", "222 2nd St.",
                Field.Store.YES, Field.Index.TOKENIZED));
        result = new SearchResult(doc);
        assertEquals("222 2nd St.", result.getAddress());

        doc.add(new Field("city", "Tahachepe",
                Field.Store.YES, Field.Index.TOKENIZED));
        result = new SearchResult(doc);
        assertEquals("222 2nd St.,  Tahachepe", result.getAddress());

        doc.add(new Field("state", "ca", Field.Store.YES, Field.Index.TOKENIZED));
        result = new SearchResult(doc);
        assertEquals("222 2nd St.,  Tahachepe, CA", result.getAddress());

        doc.add(new Field("zip", "12345", Field.Store.YES, Field.Index.TOKENIZED));
        result = new SearchResult(doc);
        assertEquals("222 2nd St.,  Tahachepe, CA 12345", result.getAddress());
        // this repleated assert tests a conditional in getAddress();
        assertEquals("222 2nd St.,  Tahachepe, CA 12345", result.getAddress());
    }

    public void testGetStreetAddress() {
        Document doc = new Document();
        doc.add(new Field("street", "222 2nd St.", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("city", "Tahachepe", Field.Store.YES, Field.Index.TOKENIZED));
        SearchResult result = new SearchResult(doc);

        assertEquals("Street address should not include city", "222 2nd St.", result.getStreetAddress());
    }

    public void testGetCityStateZip() {
        Document doc = new Document();
        doc.add(new Field("city", "Tahachepe", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("state", "ca", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("zip", "12345", Field.Store.YES, Field.Index.TOKENIZED));
        SearchResult result = new SearchResult(doc);

        assertEquals("Unexpected city state zip", "Tahachepe, CA 12345", result.getCityStateZip());
    }

    public void testGetSchoolType() {
        Document doc = new Document();
        SearchResult result = new SearchResult(doc);
        assertNull(result.getSchoolType());

        doc.add(new Field("schooltype", "public", Field.Store.YES, Field.Index.TOKENIZED));
        result = new SearchResult(doc);
        assertEquals("public", result.getSchoolType());
    }
}
