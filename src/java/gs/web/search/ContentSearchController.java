package gs.web.search;

import gs.data.community.CommunityConstants;
import gs.data.content.cms.CmsConstants;
import gs.data.content.cms.ContentKey;
import gs.data.search.ContentSearchResult;
import gs.data.search.SolrService;
import gs.data.util.CommunityUtil;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public static final String MODEL_PAGE = "page";
    public static final String MODEL_TOTAL_PAGES = "totalPages";
    public static final String MODEL_PAGE_SIZE = "pageSize";
    public static final String MODEL_PAGE_TITLE_PREFIX = "pageTitlePrefix";
    public static final String MODEL_TYPE = "type";
    public static final String MODEL_ARTICLE_RESULTS = "articleResults";
    public static final String MODEL_COMMUNITY_RESULTS = "communityResults";
    public static final String MODEL_VIDEO_RESULTS = "videoResults";
    public static final String MODEL_URL = "url";
    public static final String MODEL_URL_WITHOUT_PAGE_NUM = "urlWithoutPageNum";
    public static final String MODEL_CURRENT_DATE = "currentDate";

    public static final String PARAM_SEARCH_QUERY = "q";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_TYPE = "type";

    public static final String TYPE_COMMUNITY = "community";
    public static final String TYPE_ARTICLES = "articles";
    public static final String TYPE_VIDEO = "videos";

    public static final String MODEL_SAMPLE = "sample";
    public static final String PARAM_SAMPLE = "sample";
    public static final String SAMPLE_NO_RESULTS = "noResults";
    public static final String SAMPLE_NO_ARTICLES = "noArticles";
    public static final String SAMPLE_NO_DISCUSSIONS = "noDiscussions";
    public static final String SAMPLE_ALL_RESULTS = "allResults";

    public static final int PAGE_SIZE = 25;
    public static final int ALL_RESULTS_PAGE_SIZE = 5;

    private String _viewName;
    private SolrService _solrService;

    //=========================================================================
    // business logic
    //=========================================================================

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();

        String sample = request.getParameter(PARAM_SAMPLE);
        boolean isSample = StringUtils.isNotBlank(sample);
        int page = getPageNumber(request);
        String type = getType(request);

        String searchQuery = request.getParameter(PARAM_SEARCH_QUERY);
        if (searchQuery != null) {
            searchQuery = searchQuery.trim();
            // TODO-8876 do this a better way or refactor
            // special chars: + - && || ! ( ) { } [ ] ^ " ~ * ? : \
            // escape by preceding with \ .... how does that work with && or ||
            searchQuery = searchQuery.replace(":","").replace("(","").replace(")","");
        }

        if (isSample) {
            searchQuery = "choosing a school";
            populateModelForSample(model, page, type, sample);
        } else {
            populateModelForQuery(model, page, type, searchQuery);
        }

        model.put(MODEL_SEARCH_QUERY, searchQuery);
        model.put(MODEL_PAGE, page);
        model.put(MODEL_PAGE_SIZE, PAGE_SIZE);
        model.put(MODEL_SAMPLE, sample);

        long numArticles = (Long)model.get(MODEL_NUM_ARTICLES);
        if (TYPE_ARTICLES.equals(type) && numArticles == 0) {
            type = null;
        }
        long numDiscussions = (Long)model.get(MODEL_NUM_DISCUSSIONS);
        if (TYPE_COMMUNITY.equals(type) && numDiscussions == 0) {
            type = null;
        }
        if (TYPE_COMMUNITY.equals(type)) {
            // Google Ad Manager ad keywords
            PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
            pageHelper.addAdKeyword(CommunityUtil.COMMUNITY_GAM_AD_ATTRIBUTE_KEY, String.valueOf(true));
        }
        long numVideos = (Long)model.get(MODEL_NUM_VIDEOS);
        if (TYPE_VIDEO.equals(type) && numVideos == 0) {
            type = null;
        }

        model.put(MODEL_TYPE, type);

        if (isSample) {
            populateModelResultsForSample(model, numArticles, numDiscussions, type);
        }

        model.put(MODEL_URL, getUrl(request, searchQuery, page, type, sample));
        model.put(MODEL_URL_WITHOUT_PAGE_NUM, getUrlWithoutPageNumber(request, searchQuery, type, sample));

        model.put(MODEL_TOTAL_PAGES, getTotalPages(numArticles, numDiscussions, numVideos, type));
        model.put(MODEL_CURRENT_DATE, new Date());

        return new ModelAndView(_viewName, model);
    }

    /*
     * TODO-8876 Use Field Collapsing to reduce the number of queries
     * @see http://www.lucidimagination.com/search/document/cd9f61be2de88fdd/showing_few_results_for_each_category_facet
     * @see http://wiki.apache.org/solr/FieldCollapsing
     * @see https://issues.apache.org/jira/browse/SOLR-236
     * @see http://osdir.com/ml/solr-dev.lucene.apache.org/2009-08/msg00303.html
     *
     */
    protected void populateModelForQuery(Map<String, Object> model, int page, String type, String searchQuery) {
        long numResults = 0;
        long numArticles = 0;
        long numDiscussions = 0;
        long numVideos = 0;
        String pageTitlePrefix;

        if (StringUtils.isNotBlank(searchQuery)) {
        try {
            // TODO-8876 maybe don't expose so much of the solr internals; instead, move to SolrService?
            SolrServer solr = _solrService.getReadOnlySolrServer();

            // first query for facets
            SolrQuery query = _solrService.getFacetCountQuery(searchQuery);

            QueryResponse rsp = solr.query(query);

            FacetField contentTypeFacet = rsp.getFacetField(SolrService.FIELD_CONTENT_TYPE);
            for (FacetField.Count count : contentTypeFacet.getValues()) {
                if (CmsConstants.ARTICLE_CONTENT_TYPE.equals(count.getName()) ||
                    CmsConstants.ARTICLE_SLIDESHOW_CONTENT_TYPE.equals(count.getName()) ||
                    CmsConstants.ASK_THE_EXPERTS_CONTENT_TYPE.equals(count.getName())) {
                    numArticles += count.getCount();
                } else if (CommunityConstants.DISCUSSION_CONTENT_TYPE.equals(count.getName())) {
                    numDiscussions = count.getCount();
                } else if (CmsConstants.VIDEO_CONTENT_TYPE.equals(count.getName())) {
                    numVideos = count.getCount();
                }
            }

            numResults = numArticles + numDiscussions + numVideos;

            if (numResults == 0) {
                SpellCheckResponse spell = rsp.getSpellCheckResponse();
                String suggestedQuery = searchQuery;
                for (SpellCheckResponse.Suggestion suggestion : spell.getSuggestions()) {
                    suggestedQuery =
                            suggestedQuery.replaceAll(suggestion.getToken(), suggestion.getAlternatives().get(0));
                }
                model.put(MODEL_SUGGESTED_SEARCH_QUERY, suggestedQuery);
            }


            List<ContentSearchResult> articleResults = new ArrayList<ContentSearchResult>();
            List<ContentSearchResult> communityResults = new ArrayList<ContentSearchResult>();
            List<ContentSearchResult> videoResults = new ArrayList<ContentSearchResult>();

            // subsequent queries for page results (2 queries if all results; 1 query if filtered by type)
            // these use the same query and just set rows and filterQueries, so we can take advantage of caching

            if (TYPE_ARTICLES.equals(type) || (numArticles > 0 && numDiscussions == 0 && numVideos == 0)) {
                rsp = _solrService.getResultsForType(solr, query, page, PAGE_SIZE, TYPE_ARTICLES);
                articleResults = rsp.getBeans(ContentSearchResult.class);
            } else if (TYPE_COMMUNITY.equals(type) || (numDiscussions > 0 && numArticles == 0 && numVideos == 0)) {
                rsp = _solrService.getResultsForType(solr, query, page, PAGE_SIZE, TYPE_COMMUNITY);
                communityResults = rsp.getBeans(ContentSearchResult.class);
            } else if (TYPE_VIDEO.equals(type) || numVideos > 0 && numDiscussions == 0 && numArticles == 0) {
                rsp = _solrService.getResultsForType(solr, query, page, PAGE_SIZE, CmsConstants.VIDEO_CONTENT_TYPE);
                videoResults = rsp.getBeans(ContentSearchResult.class);
            } else {
                rsp = _solrService.getResultsForType(solr, query, 1, ALL_RESULTS_PAGE_SIZE, TYPE_ARTICLES);
                articleResults = rsp.getBeans(ContentSearchResult.class);
                rsp = _solrService.getResultsForType(solr, query, 1, ALL_RESULTS_PAGE_SIZE, TYPE_COMMUNITY);
                communityResults = rsp.getBeans(ContentSearchResult.class);
                rsp = _solrService.getResultsForType(solr, query, 1, ALL_RESULTS_PAGE_SIZE, CmsConstants.VIDEO_CONTENT_TYPE);
                videoResults = rsp.getBeans(ContentSearchResult.class);
            }

            model.put(MODEL_ARTICLE_RESULTS, articleResults);
            model.put(MODEL_COMMUNITY_RESULTS, communityResults);
            model.put(MODEL_VIDEO_RESULTS, videoResults);
        } catch (Exception e) {
            _log.error("Error querying solr for query: " + searchQuery, e);
        }
        } else{
            numResults = 0;

        }

        pageTitlePrefix = getPageTitlePrefix(numArticles, numDiscussions, numVideos, type);

        model.put(MODEL_NUM_RESULTS, numResults);
        model.put(MODEL_NUM_ARTICLES, numArticles);
        model.put(MODEL_NUM_DISCUSSIONS, numDiscussions);
        model.put(MODEL_NUM_VIDEOS, numVideos);
        model.put(MODEL_PAGE_TITLE_PREFIX, pageTitlePrefix);
    }

    //=========================================================================
    // helper methods
    //=========================================================================

    protected String getPageTitlePrefix(long numArticles, long numDiscussions, long numVideos, String type) {
        long numResults = numArticles + numDiscussions;
        if (numResults == 0) {
            return null;
        } else if (numResults == numArticles || numResults == numDiscussions || numResults == numVideos) {
            return "Results for";
        } else if (TYPE_ARTICLES.equals(type)) {
            return "Article results for";
        } else if (TYPE_COMMUNITY.equals(type)) {
            return "Community results for";
        } else if (TYPE_VIDEO.equals(type)) {
            return "Video results for";
        } else {
            return "Results for";
        }
    }

    protected static long getTotalPages(long numArticles, long numDiscussions, long numVideos, String type) {
        long totalPages;
        if (numArticles > 0 && numDiscussions > 0 && numVideos > 0) {
            if (TYPE_ARTICLES.equals(type)) {
                totalPages = numArticles / PAGE_SIZE;
                if (numArticles % PAGE_SIZE > 0) {
                    totalPages++;
                }
            } else if (TYPE_COMMUNITY.equals(type)) {
                totalPages = numDiscussions / PAGE_SIZE;
                if (numDiscussions % PAGE_SIZE > 0) {
                    totalPages++;
                }
            } else if (TYPE_VIDEO.equals(type)) {
                totalPages = numVideos / PAGE_SIZE;
                if (numVideos % PAGE_SIZE > 0) {
                    totalPages++;
                }
            } else {
                totalPages = 1;
            }
        } else if (numArticles > 0) {
            totalPages = numArticles / PAGE_SIZE;
            if (numArticles % PAGE_SIZE > 0) {
                totalPages++;
            }
        } else if (numDiscussions > 0) {
            totalPages = numDiscussions / PAGE_SIZE;
            if (numDiscussions % PAGE_SIZE > 0) {
                totalPages++;
            }
        } else if (numVideos > 0) {
            totalPages = numVideos / PAGE_SIZE;
            if (numVideos % PAGE_SIZE > 0) {
                totalPages++;
            }
        } else {
            totalPages = 1;
        }
        return totalPages;
    }

    protected static String getUrlWithoutPageNumber(HttpServletRequest request, String searchQuery, String type, String sample) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.CONTENT_SEARCH, searchQuery, (Integer)null, type, sample);
        return builder.asSiteRelative(request);
    }

    protected static String getUrl(HttpServletRequest request, String searchQuery, int page, String type, String sample) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.CONTENT_SEARCH, searchQuery, page, type, sample);
        return builder.asSiteRelative(request);
    }

    /**
     * Extract the page number from the request. Defaults to 1.
     */
    protected static int getPageNumber(HttpServletRequest request) {
        int page = 1;
        String pageParam = request.getParameter(PARAM_PAGE);
        if (pageParam != null) {
            try {
                page = Integer.valueOf(pageParam);
            } catch (NumberFormatException nfe) {
                // nothing
            }
        }
        return page;
    }

    /**
     * Extract the content type filter from the request. Defaults to null (no type filter).
     */
    protected static String getType(HttpServletRequest request) {
        String type;
        String typeParam = request.getParameter(PARAM_TYPE);
        if (TYPE_COMMUNITY.equals(typeParam)) {
            type = typeParam;
        } else if (TYPE_ARTICLES.equals(typeParam)) {
            type = typeParam;
        } else if (TYPE_VIDEO.equals(typeParam)) {
            type = typeParam;
        } else {
            type = null;
        }
        return type;
    }

    //=========================================================================
    // sample search results helper methods
    //=========================================================================

    protected void populateModelForSample(Map<String, Object> model, int page, String type, String sample) {
        long numResults;
        long numArticles;
        long numDiscussions;
        long numVideos = 0; //TODO: Figure out sample results as they pertain to videos
        String pageTitlePrefix;
        if (SAMPLE_NO_ARTICLES.equals(sample)) {
            numArticles = 0;
            numDiscussions = 173;
        } else if (SAMPLE_NO_DISCUSSIONS.equals(sample)) {
            numArticles = 68;
            numDiscussions = 0;
        } else if (SAMPLE_ALL_RESULTS.equals(sample)) {
            numArticles = 68;
            numDiscussions = 173;
        } else {
            // SAMPLE_NO_RESULTS
            numArticles = 0;
            numDiscussions = 0;
            model.put(MODEL_SUGGESTED_SEARCH_QUERY, "friendship");
        }

        numResults = numArticles + numDiscussions;
        pageTitlePrefix = getPageTitlePrefix(numArticles, numDiscussions, numVideos, type);

        List<ContentSearchResult> articleResults = new ArrayList<ContentSearchResult>();
        if (!TYPE_COMMUNITY.equals(type)) {
            articleResults = getResultsForPage(getSampleArticles(numArticles), page);
        }
        List<ContentSearchResult> communityResults = new ArrayList<ContentSearchResult>();
        if (!TYPE_ARTICLES.equals(type)) {
            communityResults = getResultsForPage(getSampleDiscussions(numDiscussions), page);
        }
        List<ContentSearchResult> videoResults = new ArrayList<ContentSearchResult>();
        if (!TYPE_VIDEO.equals(type)) {
            videoResults = getResultsForPage(getSampleDiscussions(numDiscussions), page);
        }

        model.put(MODEL_ARTICLE_RESULTS, articleResults);
        model.put(MODEL_COMMUNITY_RESULTS, communityResults);
        model.put(MODEL_VIDEO_RESULTS, videoResults);

        model.put(MODEL_NUM_RESULTS, numResults);
        model.put(MODEL_NUM_ARTICLES, numArticles);
        model.put(MODEL_NUM_DISCUSSIONS, numDiscussions);
        model.put(MODEL_NUM_VIDEOS, numVideos);
        model.put(MODEL_PAGE_TITLE_PREFIX, pageTitlePrefix);
    }

    /**
     * This can only be called after numArticles and numDiscussions have been determined
     * and type has been reset to null if either numArticles or numDiscussions are zero
     * @param model
     * @param numArticles
     * @param numDiscussions
     * @param type
     */
    protected void populateModelResultsForSample(Map<String, Object> model, long numArticles, long numDiscussions, String type) {
        if (numArticles > 0 && numDiscussions > 0 && type == null) {
            List<ContentSearchResult> articleResults = (List<ContentSearchResult>) model.get(MODEL_ARTICLE_RESULTS);
            articleResults = articleResults.subList(0, (int) Math.min(ALL_RESULTS_PAGE_SIZE, numArticles));
            model.put(MODEL_ARTICLE_RESULTS, articleResults);

            List<ContentSearchResult> communityResults = (List<ContentSearchResult>) model.get(MODEL_COMMUNITY_RESULTS);
            communityResults = communityResults.subList(0, (int) Math.min(ALL_RESULTS_PAGE_SIZE, numDiscussions));
            model.put(MODEL_COMMUNITY_RESULTS, communityResults);
        }
    }

    // only used by sample
    protected static List<ContentSearchResult> getResultsForPage(List<ContentSearchResult> results, int page) {
        int fromIndex = (page-1) * PAGE_SIZE;
        int toIndex = page * PAGE_SIZE;
        return results.subList(fromIndex, Math.min(toIndex, results.size()));
    }

    protected static List<ContentSearchResult> getSampleArticles(long numArticles) {
        List<ContentSearchResult> articles = new ArrayList<ContentSearchResult>();
        for (long i = 1; i <= numArticles; i++) {
            ContentSearchResult result = new ContentSearchResult();
            result.setContentKey(new ContentKey("Article",(long)i));
            result.setResultSummary("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam urna diam, consectetur a volutpat sed, convallis in sem. Nam vitae tortor ultrices felis sodales ultricies. Aliquam erat volutpat. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Phasellus placerat, nibh vitae aliquam vestibulum, leo felis auctor nisl, vulputate condimentum tellus lacus nec orci. Aliquam mollis, lacus sit amet cursus posuere, diam tortor placerat lectus, ac placerat massa mi et mi. Nulla at nibh erat, ut rutrum magna. Integer rhoncus enim non ipsum lobortis eu tristique quam fermentum. Integer tempus consectetur fringilla. Donec mattis vehicula mollis.");
            result.setTitle("This is the title of article " + i);
            result.setFullUri("article-full-uri");
            articles.add(result);
        }
        return articles;
    }

    protected static List<ContentSearchResult> getSampleDiscussions(long numDiscussions) {
        List<ContentSearchResult> discussions = new ArrayList<ContentSearchResult>();
        for (long i = 1; i <= numDiscussions; i++) {
            ContentSearchResult result = new ContentSearchResult();
            result.setContentKey(new ContentKey("Discussion",(long)i));
            result.setResultSummary("Vivamus ullamcorper accumsan convallis. Aliquam adipiscing, arcu eu adipiscing viverra, nulla tellus tempor dui, sit amet accumsan nibh ligula non nibh. Nam ornare congue rhoncus. In aliquet eleifend lectus vitae malesuada. Donec mattis nibh ac augue dapibus condimentum. Cras velit elit, dignissim id tempus et, imperdiet quis enim. Proin vitae velit risus. Etiam eget elit ut nibh adipiscing tincidunt. Cras augue lorem, egestas ut vestibulum a, suscipit quis turpis. Aliquam pretium mollis commodo. Morbi id turpis metus, in porttitor metus.");
            result.setTitle("This is the title of discussion " + i);
            result.setUsername("chriskimm");
            result.setDiscussionBoardId(14L);
            result.setDiscussionBoardTitle("Discussion Board Name");
            result.setFullUri("discussion-full-uri");
            Calendar date = Calendar.getInstance();
            // 15 days ago
            date.add(Calendar.DATE, -15);
            result.setDateCreated(date.getTime());
            discussions.add(result);
        }
        return discussions;
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
}
