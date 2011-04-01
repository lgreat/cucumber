package gs.web.search;

import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.ContentKey;
import gs.data.search.BaseSingleFieldSolrSearchService;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import gs.data.search.indexers.documentBuilders.CmsFeatureDocumentBuilder;

import java.util.ArrayList;
import java.util.List;

public class CmsFeatureSearchServiceSolrImpl extends BaseSingleFieldSolrSearchService<ICmsFeatureSearchResult> implements CmsFeatureSearchService {
    public void init() {
    }

    public void addDocumentTypeFilter(SolrQuery solrQuery) {
        solrQuery.addFilterQuery(CmsFeatureDocumentBuilder.DOCUMENT_TYPE + ":" + CmsFeatureDocumentBuilder.DOCUMENT_TYPE_CMS_FEATURE);
    }

    public List<ICmsFeatureSearchResult> getResultBeans(QueryResponse response) {
        List<SolrCmsFeatureSearchResult> r = response.getBeans(SolrCmsFeatureSearchResult.class);
        return ListUtils.typedList(r, SolrCmsFeatureSearchResult.class);
    }

    @Override
    public String buildQuery(String searchString) {
        String defaultQuery = "";
        if (StringUtils.isBlank(searchString)) {
            return defaultQuery;
        }
        return searchString;
    }

    public SearchResultsPage<ICmsFeatureSearchResult> getCmsFeatures(List<CmsCategory> topics, List<CmsCategory> grades,
                                                             List<CmsCategory> subjects,
                                                             boolean strict,
                                                             ContentKey excludeContentKey, String language,int pageSize,int offset) {

        String searchStr = "";
        for (CmsCategory category : topics) {

            if (!strict) {
                 searchStr += buildEitherOrIncludeQuery(CmsFeatureDocumentBuilder.FIELD_CMS_PRIMARY_CATEGORY_ID,String.valueOf(category.getId()),new Float(0.9),CmsFeatureDocumentBuilder.FIELD_CMS_TOPIC_ID,String.valueOf(category.getId()),null);
            }else{
                 searchStr += buildMustIncludeQuery(CmsFeatureDocumentBuilder.FIELD_CMS_PRIMARY_CATEGORY_ID,String.valueOf(category.getId()),new Float(0.9));
            }
        }

        for (CmsCategory category : grades) {
            searchStr += buildMustIncludeQuery(CmsFeatureDocumentBuilder.FIELD_CMS_GRADE_ID,String.valueOf(category.getId()),null);
        }

        for (CmsCategory category : subjects) {
            searchStr += buildMustIncludeQuery(CmsFeatureDocumentBuilder.FIELD_CMS_SUBJECT_ID,String.valueOf(category.getId()),null);
        }

        if (excludeContentKey != null) {
            searchStr += buildMustExcludeQuery(CmsFeatureDocumentBuilder.FIELD_CONTENT_KEY,excludeContentKey.toString());
        }

        if (language != null) {
            searchStr += buildMustIncludeQuery(CmsFeatureDocumentBuilder.FIELD_LANGUAGE,language,null);
        }

        List<CmsCategory> categories = new ArrayList<CmsCategory>();
        if (topics != null) {
            categories.addAll(topics);
        }
        if (grades != null) {
            categories.addAll(grades);
        }
        if (subjects != null) {
            categories.addAll(subjects);
        }
        for (CmsCategory category : categories) {
            String typeDisplay = category.getName();
            if (StringUtils.isNotBlank(typeDisplay)) {
                searchStr += buildOrIncludeQuery(CmsFeatureDocumentBuilder.FIELD_TITLE,typeDisplay,new Float(0.6));
            }
        }

        if(offset > 1 && pageSize >0 ){
            offset = (offset * pageSize) - pageSize;
        }

        try {
            SearchResultsPage<ICmsFeatureSearchResult> searchResultsPage = search(searchStr,offset,pageSize);

            return searchResultsPage;

        } catch (SearchException ex) {
            _log.debug("Search Exception in CmsFeatureSearch.", ex);
        }

        return null;
    }

    private String buildMustIncludeQuery(String field,String value,Float boost){
      String searchStr = "";
        if(boost != null && boost > 0.0){
            searchStr += " +(" + field + ":" + value + ")^"+boost;
        }else{
            searchStr += " +(" + field + ":" + value + ")";
        }

      return searchStr;
    }

    private String buildOrIncludeQuery(String field,String value,Float boost){
      String searchStr = "";
        if(boost != null && boost > 0.0){
            searchStr += " OR (" + field + ":" + value + ")^"+boost;
        }else{
            searchStr += " OR (" + field + ":" + value + ")";
        }

      return searchStr;
    }

    private String buildEitherOrIncludeQuery(String field1, String value1, Float boost1,
                                             String field2,String value2,Float boost2) {
        String searchStr = "";
        searchStr = "+((" + field1 + ":" + value1 + ")^"+boost1;
            searchStr += buildOrIncludeQuery(field2,value2,boost2);
        searchStr = searchStr + ")";

        return searchStr;
    }

    private String buildMustExcludeQuery(String field, String value) {
        String searchStr = "";
        searchStr += " -(" + field + ":" + value + ")";
        return searchStr;
    }

}