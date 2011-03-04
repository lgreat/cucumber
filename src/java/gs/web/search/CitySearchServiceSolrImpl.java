package gs.web.search;

import gs.data.search.indexers.documentBuilders.CityDocumentBuilder;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.List;

public class CitySearchServiceSolrImpl extends BaseSingleFieldSolrSearchService<ICitySearchResult> implements CitySearchService {

    private Logger _log = Logger.getLogger(CitySearchServiceImpl.class);

    public void addDocumentTypeFilter(SolrQuery solrQuery) {
        solrQuery.addFilterQuery(CityDocumentBuilder.DOCUMENT_TYPE + ":" + CityDocumentBuilder.DOCUMENT_TYPE_CITY);
    }

    @Override
    public void setQueryType(SolrQuery query) {
        query.add("df","city_name");
    }

    @Override
    public String buildQuery(String searchString) {
        String defaultQuery = "*:*";
        if (!StringUtils.isBlank(searchString)) {
            searchString = cleanseSearchString(searchString);
            if (searchString == null) {
                return defaultQuery; //Provided search string was garbage, early exit
            }
        } else {
            return defaultQuery;
        }

        return searchString;
    }

    public List<ICitySearchResult> getResultBeans(QueryResponse response) {
        List<CitySearchResult> r = response.getBeans(CitySearchResult.class);
        return ListUtils.typedList(r, ICitySearchResult.class);
    }

}
