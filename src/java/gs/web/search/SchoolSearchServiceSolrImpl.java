package gs.web.search;

import gs.data.search.indexers.documentBuilders.SchoolDocumentBuilder;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
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
        String defaultQuery = "";
        if (!StringUtils.isBlank(searchString)) {
            searchString = cleanseSearchString(searchString);
            if (searchString == null) {
                return defaultQuery; //Provided search string was garbage, early exit
            }
            searchString = requireNonOptionalWords(searchString);
        } else {
            return defaultQuery;
        }

        return searchString;
    }

    public List<String> suggest(String queryString, String state, int offset, int count) throws SearchException {

        SolrQuery query = buildAutosuggestQuery(queryString, state, offset, count);

        SearchResultsPage<ISchoolSearchResult> searchResults = search(query);

        return searchResults.getSuggestionsForFirstFacetField();
    }

    SolrQuery buildAutosuggestQuery(String queryString, String state, int offset, int count) {
        SolrQuery query = new SolrQuery();
        addDocumentTypeFilter(query);
        setQueryType(query);
        query.setStart(offset);
        query.setFacet(true);
        query.addFacetField("school_autosuggest");
        query.setFacetMinCount(1);
        if (count > 0) {
           query.setFacetLimit(count);
        }

        if (state != null && state.length() > 0) {
            query.addFilterQuery(SchoolDocumentBuilder.ADDRESS_STATE + ":" + state);
        }

        String[] tokens = StringUtils.split(queryString);

        //extract the last word from the string
        String partialWord = tokens[tokens.length-1];

        tokens = (String[]) ArrayUtils.remove(tokens, tokens.length - 1);

        //join all the completed words from beginning of the string
        String completedPhrase = StringUtils.join(tokens);

        String q = "";
        if (completedPhrase != null && completedPhrase.length() > 0) {
            q = buildQuery(completedPhrase);
        }

        if (q != null && q.length() > 0) {
            query.setQuery(q);
        }

        //since we're faceting on a non-tokenized field, set the facet prefix to the entire query string instead
        //of just the last (incomplete) word in the query string
        //query.setFacetPrefix(partialWord);
        query.setFacetPrefix(queryString);

        return query;
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

