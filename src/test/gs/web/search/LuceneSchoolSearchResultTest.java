package gs.web.search;

import gs.data.geo.LatLon;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.search.ISchoolSearchResult;
import gs.data.search.IndexField;
import gs.data.search.Indexer;
import gs.data.search.LuceneSchoolSearchResult;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.BaseTestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class LuceneSchoolSearchResultTest extends BaseTestCase {

    LuceneSchoolSearchResult _result;

    private Document getTestDocument() {
        Document document = new Document();
        document.add(new Field(Indexer.ID, "1", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.SORTABLE_NAME, "Alameda High School", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.SCHOOL_NAME, "Alameda High School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.STREET, "1234 Main St", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.CITY, "alameda", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.CITY_KEYWORD, "alameda", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.STATE, State.CA.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.ZIP, "94501", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.GRADE_LEVEL, LevelCode.HIGH.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.SCHOOL_TYPE, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Alameda High School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Alameda", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, State.CA.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, LevelCode.HIGH.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.SCHOOL_PHONE, "415-555-5555", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.LATITUDE, "37.00", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.LONGITUDE, "-122.00", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.OVERALL_RATING, "5", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.PARENT_RATINGS_AVG_QUALITY, "8", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.PARENT_RATINGS_COUNT, "3", Field.Store.YES, Field.Index.UN_TOKENIZED));
        return document;
    }

    private Document getTestDocument1() {
        Document document = getTestDocument();
        document.add(new Field(Indexer.COMMUNITY_RATING_SORTED_ASC, "99", Field.Store.YES, Field.Index.UN_TOKENIZED));
        return document;
    }

    private Document getTestDocument2() {
        Document document = getTestDocument();
        document.add(new Field(Indexer.COMMUNITY_RATING_SORTED_ASC, "9", Field.Store.YES, Field.Index.UN_TOKENIZED));
        return document;
    }


    public void setUp() throws Exception {
        super.setUp();

        Document document = getTestDocument1();

        _result = new LuceneSchoolSearchResult(document);
    }

    public void testGetId() throws Exception {
        Integer actualId = _result.getId();
        assertEquals("ID should match document", new Integer(1), actualId);
    }

    public void testGetDatabaseState() throws Exception {
        State actualDatabaseState = _result.getDatabaseState();
        assertEquals("State should match document", State.CA, actualDatabaseState);
    }

    public void testGetName() throws Exception {
        String actualName = _result.getName();
        assertEquals("Name should match document", "Alameda High School", actualName);
    }

    public void testGetAddress() throws Exception {
        Address address = _result.getAddress();
        assertEquals("Street should match document",  "1234 Main St", address.getStreet());
        assertEquals("City should match document", "alameda", address.getCity());
        assertEquals("Zip should match document", "94501", address.getZip());
    }

    public void testGetPhone() throws Exception {
        String actualPhone = _result.getPhone();
        assertEquals("Phone should match document", "415-555-5555", actualPhone);
    }

    public void testGetLatLon() throws Exception {
        LatLon actualLatLon = _result.getLatLon();
        assertEquals("LatLon should match document", 37.00f, actualLatLon.getLat());
        assertEquals("LatLon should match document", -122.00f, actualLatLon.getLon());
    }

    public void testGetLevelCode() throws Exception {
        String actualLevelCode = _result.getLevelCode();
        assertEquals("LevelCode should match document", LevelCode.HIGH.toString(), actualLevelCode);
    }

    public void testGetSchoolType() throws Exception {
        String actualSchoolType = _result.getSchoolType();
        assertEquals("School type should match document", SchoolType.PUBLIC.getSchoolTypeName(), actualSchoolType);
    }

    public void testGetGreatSchoolsRating() throws Exception {
        Integer actualRating = _result.getGreatSchoolsRating();
        assertEquals("GS rating should match document", new Integer(5), actualRating);
    }

    public void testGetParentRatingNullWhen99() throws Exception {
        Integer actualRating = _result.getParentRating();
        assertNull("Parent rating should be null", actualRating);
    }

    public void testGetParentRatingNull() throws Exception {
        ISchoolSearchResult result2 = new LuceneSchoolSearchResult(getTestDocument2());
        Integer actualRating = result2.getParentRating();
        assertEquals("Parent rating should be 9", new Integer(9), actualRating);
    }
}
