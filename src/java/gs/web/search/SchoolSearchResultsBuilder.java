package gs.web.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SchoolSearchResultsBuilder {

    public List<ISchoolSearchResult> build(Hits hits) throws IOException {
        return build(hits, 0, hits.length());
    }


    public List<ISchoolSearchResult> build(Hits hits, int offset) throws IOException {
        return build(hits, offset, hits.length() - offset);
    }

    public List<ISchoolSearchResult> build(Hits hits, int offset, int count) throws IOException {
        int length = hits.length();
        List<ISchoolSearchResult> searchResults = new ArrayList<ISchoolSearchResult>();

        for (int i = offset; (i < length && i < offset + count); i++ ) {
            Document document = hits.doc(i);
            LuceneSchoolSearchResult result = new LuceneSchoolSearchResult(document);
            searchResults.add(result);
        }

        return searchResults;
    }

    protected ISchoolSearchResult build(Document document) {
        return new LuceneSchoolSearchResult(document);
    }
}
