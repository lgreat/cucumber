package gs.web.search;

import gs.data.search.indexers.documentBuilders.SchoolDocumentBuilder;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchoolSearchServiceSolrImpl extends BaseSingleFieldSolrSearchService<ISchoolSearchResult> implements SchoolSearchService, ResourceLoaderAware {
    private ResourceLoader resourceLoader;

    private static String optionalWordsFile = "classpath:gs/web/search/school-search-optional-words.txt"; 

    private Map<String, Float> _optionalTerms = new HashMap<String, Float>();

    public static final Logger _log = Logger.getLogger(SchoolSearchServiceSolrImpl.class);

    public static final String BEAN_ID = "solrSchoolSearchService";
    
    public void init() {
        this.setOptionalTerms();
    }

    public void addDocumentTypeFilter(SolrQuery solrQuery) {
        solrQuery.addFilterQuery(SchoolDocumentBuilder.DOCUMENT_TYPE + ":" + SchoolDocumentBuilder.DOCUMENT_TYPE_SCHOOL);
    }

    public List<ISchoolSearchResult> getResultBeans(QueryResponse response) {
        List<SolrSchoolSearchResult> r = response.getBeans(SolrSchoolSearchResult.class);
        return ListUtils.typedList(r, SolrSchoolSearchResult.class);
    }

    @Override
    public void setQueryType(SolrQuery query) {
        query.setQueryType("school-search");
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

        return requireNonOptionalWords(searchString);
    }

    public void setOptionalTerms() {
        Resource resource = resourceLoader.getResource(optionalWordsFile);

        _optionalTerms = new HashMap<String,Float>();

        try {
            InputStream is = resource.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = br.readLine()) != null) {
                _optionalTerms.put(line, 0.0f);
            }
        } catch (Exception e) {
            _log.debug("Could not load optional words file: " + optionalWordsFile);
        }
    }

    public String requireNonOptionalWords(String queryString) {
        if (_optionalTerms.size() == 0) {
            setOptionalTerms();
        }
        
        String[] tokens = StringUtils.splitPreserveAllTokens(queryString);

        for (int i = 0; i < tokens.length; i++) {
            String lctoken = tokens[i].toLowerCase();
            if (lctoken.length() > 1 && !_optionalTerms.containsKey(lctoken) && !lctoken.matches((PUNCTUATION_AND_WHITESPACE_PATTERN))) {
                tokens[i] = "+" + tokens[i];
            }
        }

        return StringUtils.join(tokens, " ");
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}

