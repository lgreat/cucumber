package gs.web.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLuceneResultBuilder<SR extends ISearchResult> implements LuceneResultBuilder<SR> {

    /**
     * Builds a list of <code>ISchoolSearchResult</code>. Will start at building at hit element equal to offset. Will
     * build a number of results no greater than specified count.
     * @param hits Lucene class containing matched documents
     * @param offset Zero-based value to start at
     * @param count Maximum number of results to build
     *
     * @return
     * @throws IOException
     */
    public List<SR> build(Hits hits, int offset, int count) throws IOException {
        int length = hits.length();
        if (offset >= hits.length()) {
            throw new IllegalArgumentException("Cannot access element at offset " + offset + ", which is greater than total number of results, which is " + hits.length());
        }
        if (count == 0) {
            count = hits.length();
        }
        List<SR> searchResults = new ArrayList<SR>();

        for (int i = offset; (i < length && i < offset + count); i++ ) {
            Document document = hits.doc(i);
            searchResults.add(build(document));
        }

        return searchResults;
    }

    public List<SR> build(Hits hits) throws IOException {
        return build(hits, 0, hits.length());
    }

    public List<SR> build(Hits hits, int offset) throws IOException {
        return build(hits, offset, hits.length() - offset);
    }

}
