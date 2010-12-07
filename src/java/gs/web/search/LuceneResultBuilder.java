package gs.web.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;

import java.io.IOException;
import java.util.List;


public interface LuceneResultBuilder<SR extends ISearchResult> {
    public List<SR> build(Hits hits) throws IOException;

    public SR build(Document document);
}