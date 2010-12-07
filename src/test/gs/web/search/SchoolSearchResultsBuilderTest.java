package gs.web.search;

import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.search.*;
import gs.data.state.State;
import gs.web.BaseTestCase;
import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchoolSearchResultsBuilderTest extends BaseTestCase {
    SchoolSearchResultsBuilder _builder;
    SchoolSearchServiceImpl _schoolSearchService;
    Searcher _searcher;

    public void setUp() throws Exception {
        _builder = new SchoolSearchResultsBuilder();

        _schoolSearchService = (SchoolSearchServiceImpl) getApplicationContext().getBean("luceneSchoolSearchService");
        _searcher = getRamDirectorySearcher();
        _schoolSearchService.setSearcher(_searcher);
        _schoolSearchService.setQueryParser(new GSQueryParser());
        _schoolSearchService.setResultsBuilder(new SchoolSearchResultsBuilder());
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

    public Hits getTestHits() throws Exception{
        String searchString = "Lowell";
        Map<FieldConstraint,String> fieldConstraints = new HashMap<FieldConstraint,String>();
        fieldConstraints.put(FieldConstraint.STATE, "ca");
        Hits hits = _schoolSearchService.searchLucene(searchString, fieldConstraints, null, null);
        assertEquals("hits should contain correct results", 4, hits.length());
        return hits;
    }

    public void testBuildWhenOffsetIsOutOfBounds() throws Exception {
        Hits hits = getTestHits();

        try {
            List<ISchoolSearchResult> results = _builder.build(hits, 999999);
            fail("should have thrown exception");
        } catch (IllegalArgumentException e) {

        }
    }

    public void testBuildWhenCountZero() throws Exception {
        Hits hits = getTestHits();

        List<ISchoolSearchResult> results = _builder.build(hits, 0, 0);

        assertEquals("Results should contain correct number of results", 4, results.size());
    }

    public void testBuildWhenCountEqualToTotalResults() throws Exception {
        Hits hits = getTestHits();

        List<ISchoolSearchResult> results = _builder.build(hits, 0, 4);

        assertEquals("Results should contain correct number of results", 4, results.size());
    }

    public void testBuildWhenOffsetOneLessThanTotalResults() throws Exception {
        Hits hits = getTestHits();

        List<ISchoolSearchResult> results = _builder.build(hits, 3);

        assertEquals("Results should contain correct number of results", 1, results.size());
    }

    public void testBuild() throws Exception {
        Hits hits = getTestHits();

        List<ISchoolSearchResult> results = _builder.build(hits);

        assertEquals("Results should contain correct number of results", 4, results.size());
    }

}
