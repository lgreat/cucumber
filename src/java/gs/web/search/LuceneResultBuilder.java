package gs.web.search;

import org.apache.lucene.search.Hits;

import java.io.IOException;
import java.util.List;


public interface LuceneResultBuilder {
    public List<? extends ISearchResult> build(Hits hits) throws IOException;
}