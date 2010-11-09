package gs.web.search;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.search.Indexer;
import gs.data.search.SchoolComparatorFactory;
import gs.data.state.State;
import gs.data.state.StateManager;
import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import static org.easymock.classextension.EasyMock.*;

import java.io.IOException;
import java.util.List;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class ResultsPagerTest extends TestCase {
    private static Hits _hits;
    private ISchoolDao _schoolDao;
    private StateManager _stateManager;
    private static final int NUMBER_OF_HITS = 50;
    private static final String SEARCH_STATE = "CA";

    protected void setUp() throws Exception {
        _schoolDao = createMock(ISchoolDao.class);
        _stateManager = createMock(StateManager.class);
        getHits();
    }

    public void testNullArguments() {
        ResultsPager pager = new ResultsPager(null, ResultsPager.ResultType.school);
        // Since schoolDao is static, backup and restore to avoid side effects in other tests
        ISchoolDao oldSchoolDao = pager.getSchoolDao();
        pager.setSchoolDao(_schoolDao);
        assertNotNull(pager.getResults(1, 1));
        pager.setSchoolDao(oldSchoolDao);
    }

    public void testPageSizes() {
        ResultsPager pager = new ResultsPager(_hits, ResultsPager.ResultType.topic);
        List results = pager.getResults(1, 0);
        assertEquals("All hits should be returned if page size is 0", NUMBER_OF_HITS, results.size());

        results = pager.getResults(1, NUMBER_OF_HITS);
        assertEquals("All hits should be returned if page size is same as result size", NUMBER_OF_HITS, results.size());

        results = pager.getResults(1, 10);
        assertEquals("Expected (page size) results on first page", 10, results.size());

        results = pager.getResults(2, 10);
        assertEquals("Expected (page size) results on middle page", 10, results.size());

        results = pager.getResults(5, 10);
        assertEquals("Expected (page size) results on last page", 10, results.size());

        results = pager.getResults(6, 10);
        assertNull("No hits should be returned if page * page size > # of hits", results);

        results = pager.getResults(7, 10);
        assertNull("No hits should be returned if page * page size > # of hits", results);

        results = pager.getResults(6, 9);
        assertEquals("Expected total hits - (page * page size)", 5, results.size());
    }

    public void testGetPageOfSchoolResults() {
        ResultsPager pager = new ResultsPager(_hits, ResultsPager.ResultType.school);
        // Since schoolDao is static, backup and restore to avoid side effects in other tests
        ISchoolDao oldSchoolDao = pager.getSchoolDao();
        pager.setSchoolDao(_schoolDao);
        // State manager is static so we need to back it up and restore it
        StateManager oldStateManager = pager.getStateManager();
        pager.setStateManager(_stateManager);
        expect(_stateManager.getState(SEARCH_STATE))
                .andReturn(State.CA)
                .anyTimes();
        for (int i = 0; i < 11; i++) {
            School school = new School();
            school.setId(i);
            expect(_schoolDao.getSchoolById(State.CA, i))
                    .andReturn(school)
                    .anyTimes();
        }
        replay(_stateManager);
        replay(_schoolDao);

        List results = pager.getResults(1, 10);
        for (Object result : results) {
            assertTrue("Each result should be a SchoolSearchResult object", result instanceof SchoolSearchResult);
            SchoolSearchResult schoolResult = (SchoolSearchResult) result;
            assertNotNull("School should be set in result object", schoolResult.getSchool());
            assertNotNull("GreatSchools rating should not be null", schoolResult.getGreatSchoolsRating());
            assertEquals("Based on test data setup, rating should equal school ID",
                    schoolResult.getGreatSchoolsRating(), schoolResult.getSchool().getId().toString());
        }
        pager.setStateManager(oldStateManager);
        pager.setSchoolDao(oldSchoolDao);
    }


    public void testSortedGSRatingDescendingResults() throws Exception {

        _hits = null;
        getSortedHits();
        ResultsPager pager = new ResultsPager(_hits, ResultsPager.ResultType.school,
            SchoolComparatorFactory.createComparator("ratingsHeader", "desc"));
        // Since schoolDao is static, backup and restore to avoid side effects in other tests
        ISchoolDao oldSchoolDao = pager.getSchoolDao();
        pager.setSchoolDao(_schoolDao);
        // State manager is static so we need to back it up and restore it
        StateManager oldStateManager = pager.getStateManager();
        pager.setStateManager(_stateManager);
        expect(_stateManager.getState(SEARCH_STATE))
                .andReturn(State.CA)
                .anyTimes();
        for (int i = 50; i > 0; --i) {
            School school = new School();
            school.setId(i);
            expect(_schoolDao.getSchoolById(State.CA, i))
                    .andReturn(school)
                    .anyTimes();
        }
        replay(_stateManager);
        replay(_schoolDao);

        List results = pager.getResults(1, 10);
        for (int i = 0; i < 10; i++) {
            Integer expected = 49 - i;
            SchoolSearchResult schoolResult = (SchoolSearchResult) results.get(i);
            assertEquals("Each schoolResult should have a greatSchollsRating from 49 - i", expected.toString(), schoolResult.getGreatSchoolsRating());
        }
        pager.setStateManager(oldStateManager);
        pager.setSchoolDao(oldSchoolDao);
    }


    private static void getSortedHits() {

        if (_hits == null) {
            try {
                Directory directory = new RAMDirectory();
                Analyzer analyzer = new SimpleAnalyzer();
                IndexWriter writer = new IndexWriter(directory, analyzer, true);

                for (int j = 0; j < NUMBER_OF_HITS; j++) {
                    Document d = new Document();

                    d.add(new Field(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_SCHOOL, Field.Store.YES, Field.Index.TOKENIZED));
                    d.add(new Field(Indexer.ID, String.valueOf(j), Field.Store.YES, Field.Index.TOKENIZED));
                    d.add(new Field(Indexer.OVERALL_RATING, String.valueOf(j), Field.Store.YES, Field.Index.TOKENIZED));
                    d.add(new Field(Indexer.PARENT_RATINGS_AVG_P_OVERALL, String.valueOf(j * 10), Field.Store.YES, Field.Index.TOKENIZED));
                    d.add(new Field(Indexer.PARENT_RATINGS_COUNT, String.valueOf(5), Field.Store.YES, Field.Index.TOKENIZED));
                    d.add(new Field(Indexer.PARENT_RATINGS_AVG_QUALITY, String.valueOf(j * 2), Field.Store.YES, Field.Index.TOKENIZED));

                    d.add(new Field("test", "x", Field.Store.YES, Field.Index.TOKENIZED));
                    d.add(new Field(Indexer.STATE, SEARCH_STATE, Field.Store.YES, Field.Index.TOKENIZED));
                    writer.addDocument(d);
                }
                writer.close();
                Searcher searcher = new IndexSearcher(directory);
                _hits = searcher.search(new TermQuery(new Term("test", "x")));
            } catch (IOException ioe) {
                fail("Could not build test Hits object");
            }

        }
    }

    private static void getHits() {
        if (_hits == null) {
            try {
                Directory directory = new RAMDirectory();
                Analyzer analyzer = new SimpleAnalyzer();
                IndexWriter writer = new IndexWriter(directory, analyzer, true);

                for (int j = 0; j < NUMBER_OF_HITS; j++) {
                    Document d = new Document();
                    if (j > 0 && j < 11) {
                        d.add(new Field(Indexer.DOCUMENT_TYPE, Indexer.DOCUMENT_TYPE_SCHOOL, Field.Store.YES, Field.Index.TOKENIZED));
                        d.add(new Field(Indexer.ID, String.valueOf(j), Field.Store.YES, Field.Index.TOKENIZED));
                        d.add(new Field(Indexer.OVERALL_RATING, String.valueOf(j), Field.Store.YES, Field.Index.TOKENIZED));
                    } else {
                        d.add(new Field(Indexer.DOCUMENT_TYPE, "blah", Field.Store.YES, Field.Index.TOKENIZED));
                    }
                    d.add(new Field("test", "x", Field.Store.YES, Field.Index.TOKENIZED));
                    d.add(new Field(Indexer.STATE, SEARCH_STATE, Field.Store.YES, Field.Index.TOKENIZED));
                    writer.addDocument(d);
                }
                writer.close();
                Searcher searcher = new IndexSearcher(directory);
                _hits = searcher.search(new TermQuery(new Term("test", "x")));
            } catch (IOException ioe) {
                fail("Could not build test Hits object");
            }

        }
    }
}