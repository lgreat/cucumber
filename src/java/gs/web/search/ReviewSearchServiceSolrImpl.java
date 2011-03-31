package gs.web.search;

import gs.data.search.BaseSingleFieldSolrSearchService;
import gs.data.search.GsSolrQuery;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import gs.data.search.fields.ReviewFields;
import gs.data.search.fields.SolrField;
import gs.data.search.indexers.documentBuilders.ReviewDocumentBuilder;
import gs.data.state.State;
import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewSearchServiceSolrImpl extends BaseSingleFieldSolrSearchService<IReviewResult> {

    public static final Logger _log = Logger.getLogger(ReviewSearchServiceSolrImpl.class);

    public void addDocumentTypeFilter(SolrQuery solrQuery) {
        solrQuery.addFilterQuery(ReviewDocumentBuilder.DOCUMENT_TYPE + ":" + ReviewFields.DOCUMENT_TYPE_REVIEW.getName());
    }

    public List<IReviewResult> getResultBeans(QueryResponse response) {
        List<SolrReviewSearchResult> r = response.getBeans(SolrReviewSearchResult.class);
        return ListUtils.typedList(r, SolrReviewSearchResult.class);
    }

    public SearchResultsPage<IReviewResult> findBySchool(Integer schoolId, State schoolDatabaseState, int offset, int count) throws SearchException {
        GsSolrQuery query = findBySchoolQuery(schoolId, schoolDatabaseState);

        query.page(offset, count);

        return search(query);
    }

    public GsSolrQuery findBySchoolQuery(Integer schoolId, State schoolDatabaseState) {

        GsSolrQuery query = new GsSolrQuery();

        Map<SolrField,String> fieldValues = new HashMap<SolrField,String>();
        fieldValues.put(ReviewFields.REVIEW_SCHOOL_ID, String.valueOf(schoolId));
        fieldValues.put(ReviewFields.REVIEW_SCHOOL_DATABASE_STATE, schoolDatabaseState.getAbbreviationLowerCase());

        return query;
    }

    public SearchResultsPage<IReviewResult> search(GsSolrQuery query) throws SearchException {
        return search(query.getSolrQuery());
    }

}
