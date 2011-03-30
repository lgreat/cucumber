package gs.web.search;

import gs.data.content.cms.CmsCategory;
import gs.data.search.indexers.documentBuilders.CmsCategoryDocumentBuilder;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.ArrayList;
import java.util.List;

public class CmsCategorySearchServiceSolrImpl extends BaseSingleFieldSolrSearchService<ICmsCategorySearchResult> implements CmsCategorySearchService {
    public static final String BEAN_ID = "solrCategorySearchService";

    public void init() {
    }

    public void addDocumentTypeFilter(SolrQuery solrQuery) {
        solrQuery.addFilterQuery(CmsCategoryDocumentBuilder.DOCUMENT_TYPE + ":" + CmsCategoryDocumentBuilder.DOCUMENT_TYPE_CMS_CATEGORY);
    }

    public List<ICmsCategorySearchResult> getResultBeans(QueryResponse response) {
        List<SolrCmsCategorySearchResult> result = response.getBeans(SolrCmsCategorySearchResult.class);
        return ListUtils.typedList(result, SolrCmsCategorySearchResult.class);
    }

    public CmsCategory getCategoryFromURI(String requestURI) {
        if (StringUtils.isNotBlank(requestURI)) {
            String categoryUri = requestURI.replaceAll("/gs-web", "").replaceAll("/articles/", "");
            if (StringUtils.isNotBlank(categoryUri)) {
                try {
                    SearchResultsPage<ICmsCategorySearchResult> searchResultsPage = search(CmsCategoryDocumentBuilder.FIELD_FULL_URI + ":" + categoryUri);
                    if (searchResultsPage.getSearchResults() != null && searchResultsPage.getSearchResults().size() == 1) {
                        ICmsCategorySearchResult result = searchResultsPage.getSearchResults().get(0);
                        CmsCategory category = buildCmsCategory(result);
                        return category;
                    }

                } catch (SearchException ex) {
                    _log.debug("Search Exception in CmsCategorySearch.", ex);
                }
            }
        }

        return null;
    }

    public List<CmsCategory> getCategoriesFromIds(String ids) {
        List<CmsCategory> categories = new ArrayList<CmsCategory>();
        if (StringUtils.isNotBlank(ids)) {
            for (String id : ids.split(",")) {
                CmsCategory category = getCmsCategoryFromId(Long.parseLong(id.trim()));
                if (category != null) {
                    categories.add(category);
                }
            }
        }

        return categories;
    }

    public CmsCategory getCmsCategoryFromId(long categoryId) {
        try {
            SearchResultsPage<ICmsCategorySearchResult> searchResultsPage = search(CmsCategoryDocumentBuilder.FIELD_CONTENT_ID + ":" + categoryId);
            if (searchResultsPage.getSearchResults() != null && searchResultsPage.getSearchResults().size() == 1) {
                ICmsCategorySearchResult result = searchResultsPage.getSearchResults().get(0);
                CmsCategory category = buildCmsCategory(result);
                return category;
            }

        } catch (SearchException ex) {
            _log.debug("Search Exception in CmsCategorySearch.", ex);
        }
        return null;
    }

    private CmsCategory buildCmsCategory(ICmsCategorySearchResult result) {
        CmsCategory category = new CmsCategory();
        category.setId(result.getCategoryId());
        category.setName(result.getCategoryName());
        category.setFullUri(result.getCategoryFullUri());
        category.setType(result.getCategoryType());
        return category;
    }

    @Override
    public String buildQuery(String searchString) {
        String defaultQuery = "";
        if (StringUtils.isBlank(searchString)) {
            return defaultQuery;
        }
        return searchString;
    }

}