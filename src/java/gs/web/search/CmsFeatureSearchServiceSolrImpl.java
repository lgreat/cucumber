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
import java.util.Map;

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

    public SearchResultsPage<ICmsFeatureSearchResult> search(List<CmsCategory> topics, List<CmsCategory> grades, List<CmsCategory> subjects, Map<String, Object> model, int page, boolean strict, ContentKey excludeContentKey, String language) {

        String searchStr = "";
        for (CmsCategory category : topics) {
//            searchStr = "+((" + CmsFeatureDocumentBuilder.FIELD_CMS_PRIMARY_CATEGORY_ID + ":" + category.getId() + ")^0.9";
            if (!strict) {
                 searchStr += buildEitherOrIncludeQuery(CmsFeatureDocumentBuilder.FIELD_CMS_PRIMARY_CATEGORY_ID,String.valueOf(category.getId()),new Float(0.9),CmsFeatureDocumentBuilder.FIELD_CMS_TOPIC_ID,String.valueOf(category.getId()),null);
//                searchStr += " OR " + CmsFeatureDocumentBuilder.FIELD_CMS_TOPIC_ID + ":" + category.getId();
            }else{
                 searchStr += buildMustIncludeQuery(CmsFeatureDocumentBuilder.FIELD_CMS_PRIMARY_CATEGORY_ID,String.valueOf(category.getId()),new Float(0.9));
            }
//            searchStr = searchStr + ")";
        }

        for (CmsCategory category : grades) {
            searchStr += buildMustIncludeQuery(CmsFeatureDocumentBuilder.FIELD_CMS_GRADE_ID,String.valueOf(category.getId()),null);
//            searchStr += " +(" + CmsFeatureDocumentBuilder.FIELD_CMS_GRADE_ID + ":" + category.getId() + ")";
        }

        for (CmsCategory category : subjects) {
            searchStr += buildMustIncludeQuery(CmsFeatureDocumentBuilder.FIELD_CMS_SUBJECT_ID,String.valueOf(category.getId()),null);
//            searchStr += " +(" + CmsFeatureDocumentBuilder.FIELD_CMS_SUBJECT_ID + ":" + category.getId() + ")";
        }

        if (excludeContentKey != null) {
            searchStr += buildMustExcludeQuery(CmsFeatureDocumentBuilder.FIELD_CONTENT_ID,excludeContentKey.toString());
//            searchStr += " -(" + CmsFeatureDocumentBuilder.FIELD_CONTENT_ID + ":" + excludeContentKey.toString() + ")";
        }

        if (language != null) {
            searchStr += buildMustIncludeQuery(CmsFeatureDocumentBuilder.FIELD_LANGUAGE,language,null);
//            searchStr += " +(" + CmsFeatureDocumentBuilder.FIELD_LANGUAGE + ":" + language + ")";
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
//                searchStr += " OR (" + CmsFeatureDocumentBuilder.FIELD_TITLE + ":" + typeDisplay + ")^0.6";
            }
        }

        try {
            SearchResultsPage<ICmsFeatureSearchResult> searchResultsPage = search(searchStr);

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