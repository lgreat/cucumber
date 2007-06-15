package gs.web.search;

import junit.framework.TestCase;

import java.util.List;
import java.io.IOException;

import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import static org.easymock.EasyMock.*;
import gs.data.school.ISchoolDao;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ResultsPagerTest extends TestCase {
    private static Hits _hits;
    private ISchoolDao _schoolDao;
    private static final int NUMBER_OF_HITS = 50;

    protected void setUp() throws Exception {
        _schoolDao = createMock(ISchoolDao.class);
        getHits();
    }

    public void testNullArguments() {
        ResultsPager pager = new ResultsPager(null, ResultsPager.ResultType.SCHOOLS);
        pager.setSchoolDao(_schoolDao);
        assertNotNull(pager.getResults(1, 1));
    }

    public void testPageSizes() {
        ResultsPager rp = new ResultsPager(_hits, ResultsPager.ResultType.ARTICLES);
        List results = rp.getResults(1, 0);
        assertEquals("All hits should be returned if page size is 0", NUMBER_OF_HITS, results.size());

        results = rp.getResults(1, NUMBER_OF_HITS);
        assertEquals("All hits should be returned if page size is same as result size", NUMBER_OF_HITS, results.size());

        results = rp.getResults(1, 10);
        assertEquals("Expected (page size) results on first page", 10, results.size());

        results = rp.getResults(2, 10);
        assertEquals("Expected (page size) results on middle page", 10, results.size());

        results = rp.getResults(5, 10);
        assertEquals("Expected (page size) results on last page", 10, results.size());

        results = rp.getResults(6, 10);
        assertEquals("No hits should be returned if pages * page size > # of hits", 0, results.size());

        results = rp.getResults(6, 9);
        assertEquals("Expected total hits - (page * page size)", 5, results.size());
    }

    public void testGetPageOfSchoolResults() {
        ResultsPager pager = new ResultsPager(_hits, ResultsPager.ResultType.SCHOOLS);
        pager.setSchoolDao(_schoolDao);
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
                        d.add(Field.Text("type", "school"));
                        d.add(Field.Text("id", String.valueOf(j)));
                    } else {
                        d.add(Field.Text("type", "blah"));
                    }
                    d.add(Field.Text("test", "x"));
                    d.add(Field.Text("state", "CA"));
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