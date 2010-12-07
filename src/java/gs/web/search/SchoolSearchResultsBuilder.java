package gs.web.search;

import org.apache.lucene.document.Document;

public class SchoolSearchResultsBuilder extends AbstractLuceneResultBuilder implements LuceneResultBuilder {

    public ISchoolSearchResult build(Document document) {
        return new LuceneSchoolSearchResult(document);
    }
}
