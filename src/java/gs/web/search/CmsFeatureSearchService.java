package gs.web.search;

import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.ContentKey;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.List;

public interface CmsFeatureSearchService {
    public SearchResultsPage<ICmsFeatureSearchResult> getCmsFeatures(List<CmsCategory> topics,
                                                                     List<CmsCategory> grades,
                                                                     List<CmsCategory> subjects,
                                                                     List<CmsCategory> locations,
                                                                     List<CmsCategory> outcomes,
                                                                     boolean strict, ContentKey excludeContentKey,
                                                                     String language, int pageSize, int offset);
    public SearchResultsPage<ICmsFeatureSearchResult> getCmsFeaturesByType(List<CmsCategory> primaryTopics, String contentType,
                                                                     int pageSize, int offset);
    public SearchResultsPage<ICmsFeatureSearchResult> search(SolrQuery query) throws SearchException;
}