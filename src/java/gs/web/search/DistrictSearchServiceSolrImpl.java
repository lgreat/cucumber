package gs.web.search;

import gs.data.search.indexers.documentBuilders.DistrictDocumentBuilder;
import gs.data.search.parsing.IGsQueryParser;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.List;

public class DistrictSearchServiceSolrImpl extends BaseSingleFieldSolrSearchService<IDistrictSearchResult> implements DistrictSearchService {

    private Logger _log = Logger.getLogger(DistrictSearchServiceImpl.class);

    private IGsQueryParser _queryParser;

    @Override
    public void setQueryType(SolrQuery query) {
        query.add("df","district_name");
    }

    public void addDocumentTypeFilter(SolrQuery solrQuery) {
        solrQuery.addFilterQuery(DistrictDocumentBuilder.DOCUMENT_TYPE + ":" + DistrictDocumentBuilder.DOCUMENT_TYPE_DISTRICT);
    }

    public String buildQuery(String searchString) {
        String defaultQuery = "*:*";
        if (!StringUtils.isBlank(searchString)) {
            searchString = cleanseSearchString(searchString);
            if (searchString == null) {
                return defaultQuery; //Provided search string was garbage, early exit regardless of field constraints
            }
        }
        
        String q = defaultQuery;
        if (!StringUtils.isBlank(searchString)) {
            if (_queryParser != null) {
                try {
                    q = getQueryParser().parse(searchString).toString();
                } catch (ParseException e) {
                    _log.debug("Could not parse query, default query will be used.");
                    q = defaultQuery;
                }
            } else {
                q = searchString;
            }
        }

        return q;
    }

    public List<IDistrictSearchResult> getResultBeans(QueryResponse response) {
        List<DistrictSearchResult> r = response.getBeans(DistrictSearchResult.class);
        return ListUtils.typedList(r, IDistrictSearchResult.class);
    }

    public IGsQueryParser getQueryParser() {
        return _queryParser;
    }

    public void setQueryParser(IGsQueryParser queryParser) {
        _queryParser = queryParser;
    }

}