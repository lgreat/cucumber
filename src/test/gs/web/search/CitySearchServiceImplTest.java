package gs.web.search;

import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.search.IndexDir;
import gs.data.search.IndexField;
import gs.data.search.Indexer;
import gs.data.state.State;
import gs.web.BaseTestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class CitySearchServiceImplTest extends BaseTestCase {

    CitySearchServiceImpl _citySearchService;
    gs.data.search.Searcher _searcher;

    public void setUp() throws Exception {
        _citySearchService = new CitySearchServiceImpl();
        _searcher = getRamDirectorySearcher();
        _citySearchService.setSearcher(_searcher);
    }

    private gs.data.search.Searcher getRamDirectorySearcher() throws Exception {
        Directory directory = new RAMDirectory();
        Analyzer analyzer = new SimpleAnalyzer();
        IndexWriter writer = new IndexWriter(directory, analyzer, true);

        Document document = new Document();
        document.add(new Field(Indexer.SCHOOL_NAME, "Alameda High School", Field.Store.YES, Field.Index.TOKENIZED));
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
        document.add(new Field(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_SCHOOL, Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(document);

        document = new Document();
        document.add(new Field(Indexer.SCHOOL_NAME, "Lowell High School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.CITY_KEYWORD, "san francisco", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.STATE, State.CA.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.ZIP, "94132", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.GRADE_LEVEL, LevelCode.HIGH.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.SCHOOL_TYPE, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Lowel High School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, "San Francisco", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, State.CA.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, LevelCode.HIGH.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_SCHOOL, Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(document);

        document = new Document();
        document.add(new Field(Indexer.SCHOOL_NAME, "Lowell Elementary School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.CITY_KEYWORD, "san Jose", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.STATE, State.CA.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.ZIP, "95112", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.GRADE_LEVEL, LevelCode.ELEMENTARY.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.SCHOOL_TYPE, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Lowel Elementary School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, "San Jose", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, State.CA.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, LevelCode.ELEMENTARY.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_SCHOOL, Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(document);

        document = new Document();
        document.add(new Field(Indexer.SCHOOL_NAME, "Lowell Elementary School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.CITY_KEYWORD, "santa Ana", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.STATE, State.CA.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.ZIP, "92703", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.GRADE_LEVEL, LevelCode.ELEMENTARY.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.SCHOOL_TYPE, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Lowel Elementary School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Santa Ana", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, State.CA.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, LevelCode.ELEMENTARY.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_SCHOOL, Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(document);

        document = new Document();
        document.add(new Field(Indexer.SCHOOL_NAME, "Lowell Pre-School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.CITY_KEYWORD, "fresno", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.STATE, State.CA.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.ZIP, "93721", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.GRADE_LEVEL, LevelCode.PRESCHOOL.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.SCHOOL_TYPE, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Lowel Pre-School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Fresno", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, State.CA.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, LevelCode.PRESCHOOL.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_SCHOOL, Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(document);

        document = new Document();
        document.add(new Field(Indexer.SCHOOL_NAME, "Lowell Junior/Senior High School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.CITY_KEYWORD, "lowell", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.STATE, State.OR.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.ZIP, "97452", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.GRADE_LEVEL, LevelCode.MIDDLE.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.GRADE_LEVEL, LevelCode.HIGH.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.SCHOOL_TYPE, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Lowel Junior/Senior High School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Lowel", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, State.OR.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, LevelCode.MIDDLE.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, LevelCode.HIGH.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_SCHOOL, Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(document);

        document = new Document();
        document.add(new Field(Indexer.SCHOOL_NAME, "Lundy Elementary School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.CITY_KEYWORD, "lowell", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.STATE, State.OR.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.ZIP, "97452", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.GRADE_LEVEL, LevelCode.ELEMENTARY.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.SCHOOL_TYPE, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Lundy Elementary School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Lowel", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, State.OR.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, LevelCode.ELEMENTARY.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_SCHOOL, Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(document);

        document = new Document();
        document.add(new Field(Indexer.SCHOOL_NAME, "Southwest Charter School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.CITY_KEYWORD, "portland", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.STATE, State.OR.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.ZIP, "97239", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.GRADE_LEVEL, LevelCode.ELEMENTARY.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.SCHOOL_TYPE, SchoolType.CHARTER.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Southwest Charter School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Portland", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, State.OR.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, LevelCode.ELEMENTARY.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, SchoolType.CHARTER.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_SCHOOL, Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(document);

        document = new Document();
        document.add(new Field(Indexer.SCHOOL_NAME, "Portland Adventist Academy", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.CITY_KEYWORD, "portland", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.STATE, State.OR.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.ZIP, "97216", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.GRADE_LEVEL, LevelCode.HIGH.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.SCHOOL_TYPE, SchoolType.PRIVATE.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Portland Adventist Academy", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Portland", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, State.OR.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, LevelCode.HIGH.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, SchoolType.PRIVATE.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_SCHOOL, Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(document);

        document = new Document();
        document.add(new Field(Indexer.SCHOOL_NAME, "Lowell Elementary School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.CITY_KEYWORD, "lowell", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.STATE, State.AR.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.ZIP, "72745", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.GRADE_LEVEL, LevelCode.ELEMENTARY.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.SCHOOL_TYPE, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Lowel Elementary School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Lowel", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, State.AR.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, LevelCode.ELEMENTARY.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_SCHOOL, Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(document);

        document = new Document();
        document.add(new Field(Indexer.SCHOOL_NAME, "Hummingbird Day Care", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.CITY_KEYWORD, "lowell", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.STATE, State.AR.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.ZIP, "72745", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.GRADE_LEVEL, LevelCode.PRESCHOOL.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.SCHOOL_TYPE, SchoolType.PRIVATE.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Hummingbird Day Care", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Lowel", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, State.AR.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.GRADE_LEVEL, LevelCode.PRESCHOOL.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.SCHOOL_TYPE, SchoolType.PRIVATE.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_SCHOOL, Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(document);

        document = new Document();
        document.add(new Field(Indexer.SCHOOL_NAME, "Portland Elementary School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.CITY_KEYWORD, "portland", Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.STATE, State.AR.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(Indexer.ZIP, "71663", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.GRADE_LEVEL, LevelCode.ELEMENTARY.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.SCHOOL_TYPE, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Portland Elementary School", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, "Portland", Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, State.AR.getAbbreviationLowerCase(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, LevelCode.ELEMENTARY.toString(), Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(IndexField.TEXT, SchoolType.PUBLIC.getSchoolTypeName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        document.add(new Field(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_SCHOOL, Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(document);

        writer.close();

        gs.data.search.Searcher searcher = new gs.data.search.Searcher(new IndexDir(directory, new RAMDirectory()));
        return searcher;
    }

    public void testBuildQueryWithSpecialCharacterOnlySearchString() throws Exception {
        String searchString = "?";

        Query query = _citySearchService.buildQuery(searchString, null);

        assertNull("meaningless search string should return null query, for now",query);
    }

    public void testBuildQueryWithSpecialCharacterOnlySearchString2() throws Exception {
        String searchString = "!@#";

        Query query = _citySearchService.buildQuery(searchString, null);

        assertNull("meaningless search string should return null query, for now",query);
    }

    public void testBuildQueryWithSpecialCharacterOnlySearchString3() throws Exception {
        String searchString = "!@#";

        Query query = _citySearchService.buildQuery(searchString, State.CA);

        assertNull("meaningless search string + field constraints should return null query, for now",query);
    }

    public void testBuildQueryWithNoMeaningfulInput() throws Exception {
        String searchString = "";

        try {
            Query query = _citySearchService.buildQuery(searchString, null);
            fail("meaningless search string + field constraints should return null query, for now");
        } catch (IllegalArgumentException e) {

        }
    }

}
