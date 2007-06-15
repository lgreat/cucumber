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

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ResultsPagerTest extends TestCase {
    private static Hits _hits;

    protected void setUp() throws Exception {
        getHits();
    }

    public void testNullArguments() {
        ResultsPager pager2 = new ResultsPager(null, ResultsPager.ResultType.SCHOOLS);
        assertNotNull(pager2.getResults(1, 1));
    }

    public void testPagerConstraint() {
        ResultsPager rp = new ResultsPager(_hits, ResultsPager.ResultType.ARTICLES);
        List results = rp.getResults(1, 10);
        assertEquals(10, results.size());
    }

    public void testPageSizes() {
        ResultsPager rp = new ResultsPager(_hits, ResultsPager.ResultType.ARTICLES);
        List results = rp.getResults(1, 0);
        assertEquals("All hits should be returned if page size is 0", _hits.length(), results.size());

        List r2 = rp.getResults(1, 50);
        assertEquals(50, r2.size());

        //TODO: test page size variations and school results
    }

    public void testGetPageOfSchoolResults() {
        ResultsPager rp = new ResultsPager(_hits, ResultsPager.ResultType.SCHOOLS);
    }

    private static void getHits() {
        if (_hits == null) {
            try {
                Directory directory = new RAMDirectory();
                Analyzer analyzer = new SimpleAnalyzer();
                IndexWriter writer = new IndexWriter(directory, analyzer, true);

                for (int j = 0; j < 50; j++) {
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