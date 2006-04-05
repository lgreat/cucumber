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

    public void testNullArguments() {
        ResultsPager pager = new ResultsPager(null, null);
        assertNotNull(pager.getResults(1, 1));

        ResultsPager pager2 = new ResultsPager(null, "school");
        assertNotNull(pager2.getResults(1, 1));
    }

    public void testPagerNullConstraint() {
        Hits h = getHits();
        System.out.println ("hits: " + h.length());
        ResultsPager rp = new ResultsPager(h, null);
        List results = rp.getResults(1, 10);
        assertEquals(10, results.size());

        List r2 = rp.getResults(0, 10);
        assertEquals(10, r2.size());
    }

    public void testGetSchoolsTotal() {
        ResultsPager rp1 = new ResultsPager(getHits(), "foo");
        assertEquals(0, rp1.getSchoolsTotal());

        ResultsPager rp2 = new ResultsPager(getHits(), "school");
        assertEquals(50, rp2.getSchoolsTotal());
    }

    public void testPagerConstraint() {
        ResultsPager rp = new ResultsPager(getHits(), "foo");
        List results = rp.getResults(1, 10);
        assertEquals(10, results.size());
    }

    public void testPageSizes() {
        ResultsPager rp = new ResultsPager(getHits(), "school");
        List results = rp.getResults(1, 0);
        assertEquals(50, results.size());

        List r2 = rp.getResults(1, 50);
        assertEquals(50, r2.size());
    }

    public void testGetSchools() {
        ResultsPager rp = new ResultsPager(getHits(), "foo");
        List shouldBeEmpty = rp.getSchools(1, 10);
        assertEquals(0, shouldBeEmpty.size());

        ResultsPager rp2 = new ResultsPager(getHits(), "school");
        List shouldHave9 = rp2.getSchools(1, 10);
        assertEquals(9, shouldHave9.size());
    }

    private static Hits getHits() {
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
        return _hits;
    }
}