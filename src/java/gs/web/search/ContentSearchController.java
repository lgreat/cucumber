package gs.web.search;

import gs.data.community.CommunityConstants;
import gs.data.content.cms.CmsConstants;
import gs.data.pagination.DefaultPaginationConfig;
import gs.web.pagination.Pagination;
import gs.data.pagination.PaginationConfig;
import gs.data.search.*;
import gs.data.search.fields.CmsFeatureFields;
import gs.data.search.fields.DocumentType;
import gs.data.util.CommunityUtil;
import gs.web.pagination.RequestedPage;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.springframework.beans.BeansException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.util.*;

/**
 * GS-8876
 * Community and article search controller
 * @author Young Fan <mailto:yfan@greatschools.org>
 */
public class ContentSearchController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String MODEL_SEARCH_QUERY = "searchQuery";
    public static final String MODEL_SUGGESTED_SEARCH_QUERY = "suggestedSearchQuery";

    public static final String MODEL_NUM_RESULTS = "numResults";
    public static final String MODEL_NUM_ARTICLES = "numArticles";
    public static final String MODEL_NUM_DISCUSSIONS = "numDiscussions";
    public static final String MODEL_NUM_VIDEOS = "numVideos";
    public static final String MODEL_NUM_WORKSHEETS= "numWorksheets";
    public static final String MODEL_NUM_RESULT_TYPES = "numResultTypes";
    
    public static final String MODEL_PAGE = "page";
    public static final String MODEL_TOTAL_PAGES = "totalPages";
    public static final String MODEL_PAGE_SIZE = "pageSize";
    public static final String MODEL_PAGE_TITLE_PREFIX = "pageTitlePrefix";
    public static final String MODEL_TYPE = "type";

    public static final String MODEL_ARTICLE_RESULTS = "articleResults";
    public static final String MODEL_COMMUNITY_RESULTS = "communityResults";
    public static final String MODEL_VIDEO_RESULTS = "videoResults";
    public static final String MODEL_WORKSHEET_RESULTS = "worksheetResults";

    public static final String MODEL_URL = "url";
    public static final String MODEL_URL_WITHOUT_PAGE_NUM = "urlWithoutPageNum";
    public static final String MODEL_CURRENT_DATE = "currentDate";

    public static final String PARAM_SEARCH_QUERY = "q";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_TYPE = "type";

    public static final String TYPE_COMMUNITY = "community";
    public static final String TYPE_ARTICLES = "articles";
    public static final String TYPE_VIDEOS = "videos";
    public static final String TYPE_WORKSHEETS = "worksheets";

    public static final int PAGE_SIZE = 25;
    public static final int ALL_RESULTS_PAGE_SIZE = 5;

    private String _viewName;
    private SolrService _solrService;
    private GsSolrSearcher _gsSolrSearcher;

    protected static PaginationConfig CONTENT_SEARCH_PAGINATION_CONFIG;

    static {
        CONTENT_SEARCH_PAGINATION_CONFIG = new PaginationConfig (
                DefaultPaginationConfig.DEFAULT_PAGE_SIZE_PARAM,
                PARAM_PAGE,
                DefaultPaginationConfig.DEFAULT_OFFSET_PARAM,
                PAGE_SIZE,
                PAGE_SIZE,
                DefaultPaginationConfig.ZERO_BASED_OFFSET,
                DefaultPaginationConfig.ZERO_BASED_PAGES
        );
    }
    //=========================================================================
    // business logic
    //=========================================================================

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();

        // get the content type from the request (worksheets, videos, etc)
        ContentSearchType requestedType = ContentSearchType.getByValue(request.getParameter(PARAM_TYPE));

        // get the RequestedPage from the request
        RequestedPage requestedPage = getRequestedPage(request);

        // get the user's requested search string
        String searchQuery = request.getParameter(PARAM_SEARCH_QUERY);

        // clean up the user's search string
        searchQuery = cleanSearchString(searchQuery);

        if (StringUtils.isNotBlank(searchQuery)) {
            try {
                GsSolrQuery facetQuery = getFacetCountQuery(searchQuery);
                QueryResponse rsp = _gsSolrSearcher.search(facetQuery);

                // Perform a facet query and get aggregated facet info
                ContentTypeFacetInfo contentTypeFacetInfo = getFacetInfo(rsp);

                // get counts for each content type
                Map<ContentSearchType, Long> contentSearchTypeCounts = contentTypeFacetInfo.contentSearchTypeCounts;

                // If a single content type is requested, and results exist it,  we want to search only for that type
                // Otherwise we'll search with all of the content types that have results
                List<ContentSearchType> contentTypesToSearch = new ArrayList<ContentSearchType>();
                if (contentTypeFacetInfo.typesWithResults.contains(requestedType)) {
                    contentTypesToSearch.add(requestedType);
                } else {
                    contentTypesToSearch.addAll(contentTypeFacetInfo.typesWithResults);
                }

                // If view will only display results for one type, then effectiveType will contain that type
                ContentSearchType onlyTypeWithResults = null;
                if (contentTypesToSearch.size() == 1) {
                    onlyTypeWithResults = contentTypesToSearch.get(0);
                }

                // look for spellcheck suggestions
                if (contentTypeFacetInfo.sumFacetCounts == 0) {
                    String suggestedQuery = getSpellcheckQuery(searchQuery, rsp);
                    model.put(MODEL_SUGGESTED_SEARCH_QUERY, suggestedQuery);
                } else {
                    // If we have a specific contentSearchType to search with, use that. Otherwise get results for all
                    // Known content search types that will yield results
                    ContentSearchResultsInfo resultsInfo = getContentSearchTypeResults(facetQuery.getSolrQuery().getQuery(), contentTypesToSearch);
                    Map<ContentSearchType, List<ContentSearchResult>> contentSearchTypeResults = resultsInfo.contentSearchResults;

                    addResultCountsToModel(model, contentTypeFacetInfo);
                    addResultsToModel(model, contentSearchTypeResults);

                    addGAMAttributes(request, requestedType, contentSearchTypeCounts);

                    if (onlyTypeWithResults != null) {
                        addPagingDataToModel(model, requestedPage, resultsInfo.totalFound);
                        model.put(MODEL_TYPE, onlyTypeWithResults);
                    }
                }

                model.put(MODEL_PAGE_TITLE_PREFIX, getPageTitlePrefix(onlyTypeWithResults));
                model.put(MODEL_SEARCH_QUERY, searchQuery);
                String typeString = requestedType==null? "":requestedType.toString();
                model.put(MODEL_URL, getUrl(request, searchQuery, requestedPage.pageNumber, typeString));
                model.put(MODEL_URL_WITHOUT_PAGE_NUM, getUrlWithoutPageNumber(request, searchQuery, typeString));
                model.put(MODEL_CURRENT_DATE, new Date());

            } catch (Exception e) {
                _log.error("Error querying solr for query: " + searchQuery, e);
            }
        }

        return new ModelAndView(_viewName, model);
    }

    protected void addGAMAttributes(HttpServletRequest request, ContentSearchType requestedType, Map<ContentSearchType, Long> contentSearchTypeCounts) {
        if (requestedType != null && contentSearchTypeCounts.get(requestedType) > 0) {
           if (ContentSearchType.DISCUSSIONS.equals(requestedType)) {
               // if type is community and we found community results, set up GAM keywords
               PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
               pageHelper.addAdKeyword(CommunityUtil.COMMUNITY_GAM_AD_ATTRIBUTE_KEY, String.valueOf(true));
           }
        }
    }

    public void addPagingDataToModel(Map<String,Object> model, RequestedPage requestedPage, long totalResults) {
        int totalPages = (int) Math.ceil(totalResults / ((float) requestedPage.pageSize));
        model.put(MODEL_PAGE, requestedPage.pageNumber);
        model.put(MODEL_PAGE_SIZE, PAGE_SIZE);
        model.put(MODEL_TOTAL_PAGES, totalPages);
    }

    protected String cleanSearchString(String searchQuery) {
        if (searchQuery != null) {
            searchQuery = searchQuery.trim();
            // TODO-8876 do this a better way or refactor
            // special chars: + - && || ! ( ) { } [ ] ^ " ~ * ? : \
            // escape by preceding with \ .... how does that work with && or ||
            searchQuery = searchQuery.replace(":","").replace("(","").replace(")","");
        }
        return searchQuery;
    }

    /**
     * A ContentSearch-localized group of all available content types.
     */
    enum ContentSearchType {
        ARTICLES(
                TYPE_ARTICLES,
                CmsConstants.ARTICLE_CONTENT_TYPE,
                CmsConstants.ARTICLE_SLIDESHOW_CONTENT_TYPE,
                CmsConstants.ASK_THE_EXPERTS_CONTENT_TYPE
        ),
        DISCUSSIONS(
                TYPE_COMMUNITY,
                CommunityConstants.DISCUSSION_CONTENT_TYPE
        ),
        VIDEOS(TYPE_VIDEOS,
                CmsConstants.VIDEO_CONTENT_TYPE
        ),
        WORKSHEETS(TYPE_WORKSHEETS,
                CmsConstants.WORKSHEET_CONTENT_TYPE
        );

        // the string representation for this ContentSearchType
        private String _value;

        // the content types that this ContentSearchType will search/return
        private String[] _contentTypes;

        ContentSearchType(String value, String... contentTypes) {
            this._value = value;
            this._contentTypes = contentTypes;
        }

        public static ContentSearchType findByContentType(String contentType) {
            ContentSearchType match = null;
            for (ContentSearchType type : EnumSet.allOf(ContentSearchType.class)) {
                if (ArrayUtils.contains(type.getContentTypes(), contentType)) {
                    match = type;
                    break;
                }
            }
            return match;
        }

        public static ContentSearchType getByValue(String value){
           ContentSearchType match = null;
           for (ContentSearchType type : EnumSet.allOf(ContentSearchType.class)) {
               if (type.toString().equals(value)) {
                   match = type;
                   break;
               }
           }
           return match;
        }

        public String toString() {return _value;}
        public String[] getContentTypes() {return _contentTypes;}
    }

    public GsSolrQuery getQueryForContentType(ContentSearchType type) {
        if (type == null) {
            throw new IllegalArgumentException("Can't build query with null ContentSearchType");
        }
        GsSolrQuery query = new GsSolrQuery();

        // Limit searches to CMS features and appropriate content types
        query.filter(DocumentType.CMS_FEATURE);
        query.filter(CmsFeatureFields.FIELD_CONTENT_TYPE, Arrays.asList(type.getContentTypes()));

        return query;
    }

    public void addResultCountsToModel(Map<String,Object> model, ContentTypeFacetInfo facetInfo) {
        //TODO: Use naming conventions to make this code dynamic?
        long numArticles = facetInfo.contentSearchTypeCounts.get(ContentSearchType.ARTICLES);
        long numDiscussions = facetInfo.contentSearchTypeCounts.get(ContentSearchType.DISCUSSIONS);
        long numVideos = facetInfo.contentSearchTypeCounts.get(ContentSearchType.VIDEOS);
        long numWorksheets = facetInfo.contentSearchTypeCounts.get(ContentSearchType.WORKSHEETS);

        model.put(MODEL_NUM_ARTICLES, numArticles);
        model.put(MODEL_NUM_DISCUSSIONS, numDiscussions);
        model.put(MODEL_NUM_VIDEOS, numVideos);
        model.put(MODEL_NUM_WORKSHEETS, numWorksheets);

        model.put(MODEL_NUM_RESULT_TYPES, facetInfo.numberOfTypesWithResults);
        model.put(MODEL_NUM_RESULTS, facetInfo.sumFacetCounts);
    }


    public void addResultsToModel(Map<String,Object> model, Map<ContentSearchType,List<ContentSearchResult>> contentSearchTypeResults) {
        //TODO: Use naming conventions to make this code dynamic?
        List<ContentSearchResult> articleResults = contentSearchTypeResults.get(ContentSearchType.ARTICLES);
        List<ContentSearchResult> communityResults = contentSearchTypeResults.get(ContentSearchType.DISCUSSIONS);
        List<ContentSearchResult> videoResults = contentSearchTypeResults.get(ContentSearchType.VIDEOS);
        List<ContentSearchResult> worksheetResults = contentSearchTypeResults.get(ContentSearchType.WORKSHEETS);

        model.put(MODEL_ARTICLE_RESULTS, articleResults);
        model.put(MODEL_COMMUNITY_RESULTS, communityResults);
        model.put(MODEL_VIDEO_RESULTS, videoResults);
        model.put(MODEL_WORKSHEET_RESULTS, worksheetResults);
    }

    class ContentSearchResultsInfo {
        public final Map<ContentSearchType, List<ContentSearchResult>> contentSearchResults;
        public final int totalFound;

        ContentSearchResultsInfo(Map<ContentSearchType, List<ContentSearchResult>> contentSearchResults, int totalFound) {
            this.contentSearchResults = contentSearchResults;
            this.totalFound = totalFound;
        }
    }

    private ContentSearchResultsInfo getContentSearchTypeResults(String searchQuery, List<ContentSearchType> contentTypesToSearch) throws SearchException {
        if (contentTypesToSearch == null || contentTypesToSearch.size() == 0) {
            throw new IllegalArgumentException("No content types were provided, cannot search");
        }

        int pageSize = contentTypesToSearch.size()==1? PAGE_SIZE : ALL_RESULTS_PAGE_SIZE;
        int totalFound = 0;
        GsSolrQuery gsSolrQuery;

        Map<ContentSearchType, List<ContentSearchResult>>contentSearchTypeResults = new HashMap<ContentSearchType, List<ContentSearchResult>>();
        for (ContentSearchType type : EnumSet.allOf(ContentSearchType.class)) {
            if (contentTypesToSearch.contains(type)) {
                gsSolrQuery = getQueryForContentType(type);
                gsSolrQuery.query(searchQuery);
                gsSolrQuery.page(0, pageSize);
                SearchResultsPage<ContentSearchResult> searchResultsPage = _gsSolrSearcher.search(gsSolrQuery, ContentSearchResult.class);
                List<ContentSearchResult> results = searchResultsPage.getSearchResults();
                contentSearchTypeResults.put(type, results);
                totalFound += searchResultsPage.getTotalResults();
            } else {
                contentSearchTypeResults.put(type, new ArrayList<ContentSearchResult>());
            }
        }

        return new ContentSearchResultsInfo(contentSearchTypeResults, totalFound);
    }

    final class ContentTypeFacetInfo {
        private final Map<ContentSearchType, Long> contentSearchTypeCounts;
        private final long sumFacetCounts;
        private final long numberOfTypesWithResults;
        private final Set<ContentSearchType> typesWithResults;

        ContentTypeFacetInfo(Map<ContentSearchType, Long> contentSearchTypeCounts, long sumFacetCounts, Set<ContentSearchType> typesWithResults) {
            this.contentSearchTypeCounts = contentSearchTypeCounts;
            this.sumFacetCounts = sumFacetCounts;
            this.numberOfTypesWithResults = typesWithResults.size();
            this.typesWithResults = typesWithResults;
        }
    }

    /**
     * Given a QueryResponse builds a map with each of the ContentSearchTypes and the associated facet counts
     *
     * @param response
     * @return
     * @throws SolrServerException
     * @throws MalformedURLException
     * @throws SearchException
     */
    protected ContentTypeFacetInfo getFacetInfo(QueryResponse response) throws SolrServerException, MalformedURLException, SearchException {

        FacetField contentTypeFacet = response.getFacetField(CmsFeatureFields.FIELD_CONTENT_TYPE.getName());

        Map<ContentSearchType, Long> contentSearchTypeCounts = new HashMap<ContentSearchType, Long>();
        // Pre-populate map so that entries with a count of 0 are included
        for (ContentSearchType type: EnumSet.allOf(ContentSearchType.class)) {
            contentSearchTypeCounts.put(type, 0l);
        }

        Set<ContentSearchType> typesWithResults = new HashSet<ContentSearchType>();

        // If there's data indexed for a content type that's not included within the content types we're searching
        // On, then the sum of all the counts within this map might be less than the total number of results
        // solr returns. Therefore we need to calculate it rather than use queryResponse.getResults().getNumFound()
        // We also need to know how many of the content types actually had results
        long sumFacetCounts = 0;

        List<FacetField.Count> facetFields = contentTypeFacet.getValues();
        Iterator<FacetField.Count> iterator = facetFields.iterator();

        // Iterate through the facet counts received from Solr
        while (iterator.hasNext()) {
            FacetField.Count facetCount = iterator.next();
            // Find the ContentSearchType that matches the facet field's name
            ContentSearchType matchingContentSearchType = ContentSearchType.findByContentType(facetCount.getName());
            if (matchingContentSearchType != null) {
                Long count = facetCount.getCount();
                sumFacetCounts += count;
                if (count > 0) {
                    typesWithResults.add(matchingContentSearchType);
                }
                Long typeResultCount = contentSearchTypeCounts.get(matchingContentSearchType);
                contentSearchTypeCounts.put(matchingContentSearchType, typeResultCount + facetCount.getCount());
            }
        }

        ContentTypeFacetInfo facetInfo = new ContentTypeFacetInfo(
                contentSearchTypeCounts,
                sumFacetCounts,
                typesWithResults
        );

        return facetInfo;
    }

    public GsSolrQuery getFacetCountQuery(String searchQuery) throws SolrServerException {
        GsSolrQuery query = new GsSolrQuery();
        query.facet(CmsFeatureFields.FIELD_CONTENT_TYPE);
        query.page(0,0);

        StringBuilder q = new StringBuilder();
        q.append("(");
        q.append("(title:" + searchQuery.toLowerCase() + ")^10");
        q.append(" OR (text:" + searchQuery.toLowerCase() + ")");
        q.append(" OR (cmsGradesText:" + searchQuery.toLowerCase() + ")");
        q.append(")");
        query.query(q.toString());
        return query;
    }

    protected String getSpellcheckQuery(String searchQuery, QueryResponse rsp) {
        SpellCheckResponse spell = rsp.getSpellCheckResponse();
        String suggestedQuery = searchQuery;
        for (SpellCheckResponse.Suggestion suggestion : spell.getSuggestions()) {
            suggestedQuery =
                    suggestedQuery.replaceAll(suggestion.getToken(), suggestion.getAlternatives().get(0));
        }
        if (suggestedQuery.equals(searchQuery)) {
            suggestedQuery = null;
        }
        return suggestedQuery;
    }

    //=========================================================================
    // helper methods
    //=========================================================================

    protected String getPageTitlePrefix(ContentSearchType requestedType) {
        if (ContentSearchType.ARTICLES.equals(requestedType)) {
            return "Article results for";
        } else if (ContentSearchType.DISCUSSIONS.equals(requestedType)) {
            return "Community results for";
        } else if (ContentSearchType.VIDEOS.equals(requestedType)) {
            return "Video results for";
        } else if (ContentSearchType.WORKSHEETS.equals(requestedType)) {
            return "Worksheet results for";
        } else {
            return "Results for";
        }
    }

    protected static String getUrlWithoutPageNumber(HttpServletRequest request, String searchQuery, String type) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.CONTENT_SEARCH, searchQuery, (Integer)null, type);
        return builder.asSiteRelative(request);
    }

    protected static String getUrl(HttpServletRequest request, String searchQuery, int page, String type) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.CONTENT_SEARCH, searchQuery, page, type);
        return builder.asSiteRelative(request);
    }

    /**
     * Extract the page number from the request and uses it to create a RequestedPage
     */
    protected RequestedPage getRequestedPage(HttpServletRequest request) {
        RequestedPage requestedPage;

        int pageNumber = 1;
        String pageParam = request.getParameter(PARAM_PAGE);
        if (pageParam != null) {
            try {
                pageNumber = Integer.valueOf(pageParam);
            } catch (NumberFormatException nfe) {
                // nothing
            }
        }
        // Paging isn't enabled on the "all results" version of the view, so if a page number is set we can
        // Safely set page size to the PAGE_SIZE
        requestedPage = Pagination.getRequestedPage(PAGE_SIZE, null, pageNumber, ContentSearchController.CONTENT_SEARCH_PAGINATION_CONFIG);
        return requestedPage;
    }

    //=========================================================================
    // setters/getters for spring injection
    //=========================================================================

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public SolrService getSolrService() {
        return _solrService;
    }

    public void setSolrService(SolrService solrService) {
        _solrService = solrService;
    }

    public void setGsSolrSearcher(GsSolrSearcher gsSolrSearcher) {
        _gsSolrSearcher = gsSolrSearcher;
    }
}
