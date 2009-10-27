package gs.web.search;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import gs.web.util.UrlBuilder;
import gs.data.search.ContentSearchResult;
import gs.data.search.SolrService;
import gs.data.content.cms.ContentKey;

/**
 * GS-8876
 * Community and article search controller
 * @author Young Fan <mailto:yfan@greatschools.net>
 */
public class ContentSearchController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String MODEL_SEARCH_QUERY = "searchQuery";
    public static final String MODEL_SUGGESTED_SEARCH_QUERY = "suggestedSearchQuery";
    public static final String MODEL_NUM_RESULTS = "numResults";
    public static final String MODEL_NUM_ARTICLES = "numArticles";
    public static final String MODEL_NUM_DISCUSSIONS = "numDiscussions";
    public static final String MODEL_PAGE = "page";
    public static final String MODEL_TOTAL_PAGES = "totalPages";
    public static final String MODEL_PAGE_SIZE = "pageSize";
    public static final String MODEL_PAGE_TITLE_PREFIX = "pageTitlePrefix";
    public static final String MODEL_TYPE = "type";
    public static final String MODEL_ARTICLE_RESULTS = "articleResults";
    public static final String MODEL_COMMUNITY_RESULTS = "communityResults";
    public static final String MODEL_URL = "url";
    public static final String MODEL_URL_WITHOUT_PAGE_NUM = "urlWithoutPageNum";
    public static final String MODEL_CURRENT_DATE = "currentDate";

    public static final String PARAM_SEARCH_QUERY = "q";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_TYPE = "type";

    public static final String TYPE_COMMUNITY = "community";
    public static final String TYPE_ARTICLES = "articles";

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

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {

        Map<String, Object> model = new HashMap<String, Object>();

        String sample = request.getParameter(PARAM_SAMPLE);
        boolean isSample = StringUtils.isNotBlank(sample);

        String searchQuery = request.getParameter(PARAM_SEARCH_QUERY);
        int page = getPageNumber(request);
        String type = getType(request);

        if (isSample) {
            searchQuery = "friendship";
            populateModelForSample(model, page, type, sample);
        } else {
            populateModelForQuery(model, page, type, searchQuery);
        }

        model.put(MODEL_SEARCH_QUERY, searchQuery);
        model.put(MODEL_PAGE, page);
        model.put(MODEL_PAGE_SIZE, PAGE_SIZE);
        model.put(MODEL_SAMPLE, sample);

        int numArticles = (Integer)model.get(MODEL_NUM_ARTICLES);
        if (TYPE_ARTICLES.equals(type) && numArticles == 0) {
            type = null;
        }
        int numDiscussions = (Integer)model.get(MODEL_NUM_DISCUSSIONS);
        if (TYPE_COMMUNITY.equals(type) && numDiscussions == 0) {
            type = null;
        }
        model.put(MODEL_TYPE, type);

        // TODO-8876 see if we can get only ALL_RESULTS_PAGE_SIZE results instead of getting
        // PAGE_SIZE results and then truncating
        // also, this is a weird way of getting the right number of results
        if (numArticles > 0 && numDiscussions > 0 && type == null) {
            List<ContentSearchResult> articleResults = (List<ContentSearchResult>)model.get(MODEL_ARTICLE_RESULTS);
            articleResults = articleResults.subList(0, Math.min(ALL_RESULTS_PAGE_SIZE, numArticles));
            model.put(MODEL_ARTICLE_RESULTS, articleResults);

            List<ContentSearchResult> communityResults = (List<ContentSearchResult>)model.get(MODEL_COMMUNITY_RESULTS);
            communityResults = communityResults.subList(0, Math.min(ALL_RESULTS_PAGE_SIZE, numDiscussions));
            model.put(MODEL_COMMUNITY_RESULTS, communityResults);
        }
        
        model.put(MODEL_URL, getUrl(request, searchQuery, page, type, sample));
        model.put(MODEL_URL_WITHOUT_PAGE_NUM, getUrlWithoutPageNumber(request, searchQuery, type, sample));

        model.put(MODEL_TOTAL_PAGES, getTotalPages(numArticles, numDiscussions, type));
        model.put(MODEL_CURRENT_DATE, new Date());

        return new ModelAndView(_viewName, model);
    }

    protected void populateModelForQuery(Map<String, Object> model, int page, String type, String searchQuery) {
        int numResults;
        int numArticles;
        int numDiscussions;
        String pageTitlePrefix;
        try {
            // TODO-8876 use Solr to search
            List<ContentSearchResult> results = _solrService.getResults(searchQuery);
            List<ContentSearchResult> articleResults = new ArrayList<ContentSearchResult>();
            List<ContentSearchResult> communityResults = new ArrayList<ContentSearchResult>();
            for (ContentSearchResult result : results) {
                if (result.getContentKey().getType().equals("Discussion")) {
                    communityResults.add(result);
                } else {
                    articleResults.add(result);
                }
            }
            numArticles = articleResults.size();
            numDiscussions = communityResults.size();
            numResults = numArticles + numDiscussions;

            // TODO-8876 remove me
            //System.out.println("====== numArticles = " + numArticles + ", numDiscussions = " + numDiscussions + ", numResults = " + numResults);

            if (numResults == 0) {
                // TODO-8876 use Solr to search
                model.put(MODEL_SUGGESTED_SEARCH_QUERY, "friendship");
            }

            pageTitlePrefix = getPageTitlePrefix(numArticles, numDiscussions, type);

            model.put(MODEL_ARTICLE_RESULTS, articleResults);
            model.put(MODEL_COMMUNITY_RESULTS, communityResults);

            model.put(MODEL_NUM_RESULTS, numResults);
            model.put(MODEL_NUM_ARTICLES, numArticles);
            model.put(MODEL_NUM_DISCUSSIONS, numDiscussions);

            model.put(MODEL_PAGE_TITLE_PREFIX, pageTitlePrefix);
        } catch (Exception e) {
            _log.error("Error querying solr for query: " + searchQuery, e);
        }
    }

    protected String getPageTitlePrefix(int numArticles, int numDiscussions, String type) {
        int numResults = numArticles + numDiscussions;
        if (numResults == 0) {
            return null;
        } else if (numResults == numArticles || numResults == numDiscussions) {
            return "Results for";
        } else if (TYPE_ARTICLES.equals(type)) {
            return "Article results for";
        } else if (TYPE_COMMUNITY.equals(type)) {
            return "Community results for";
        } else {
            return "Results for";
        }
    }

    protected void populateModelForSample(Map<String, Object> model, int page, String type, String sample) {
        int numResults;
        int numArticles;
        int numDiscussions;
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
        pageTitlePrefix = getPageTitlePrefix(numArticles, numDiscussions, type);

        List<ContentSearchResult> articleResults = getResultsForPage(getSampleArticles(numArticles), page);
        List<ContentSearchResult> communityResults = getResultsForPage(getSampleDiscussions(numDiscussions), page);

        model.put(MODEL_ARTICLE_RESULTS, articleResults);
        model.put(MODEL_COMMUNITY_RESULTS, communityResults);

        model.put(MODEL_NUM_RESULTS, numResults);
        model.put(MODEL_NUM_ARTICLES, numArticles);
        model.put(MODEL_NUM_DISCUSSIONS, numDiscussions);
        model.put(MODEL_PAGE_TITLE_PREFIX, pageTitlePrefix);
    }

    protected static List<ContentSearchResult> getResultsForPage(List<ContentSearchResult> results, int page) {
        int fromIndex = (page-1) * PAGE_SIZE;
        int toIndex = page * PAGE_SIZE;
        return results.subList(fromIndex, Math.min(toIndex, results.size()));
    }

    protected static List<ContentSearchResult> getSampleArticles(int numArticles) {
        List<ContentSearchResult> articles = new ArrayList<ContentSearchResult>();
        for (int i = 1; i <= numArticles; i++) {
            ContentSearchResult result = new ContentSearchResult();
            result.setContentKey(new ContentKey("Article",(long)i));
            result.setResultSummary("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam urna diam, consectetur a volutpat sed, convallis in sem. Nam vitae tortor ultrices felis sodales ultricies. Aliquam erat volutpat. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Phasellus placerat, nibh vitae aliquam vestibulum, leo felis auctor nisl, vulputate condimentum tellus lacus nec orci. Aliquam mollis, lacus sit amet cursus posuere, diam tortor placerat lectus, ac placerat massa mi et mi. Nulla at nibh erat, ut rutrum magna. Integer rhoncus enim non ipsum lobortis eu tristique quam fermentum. Integer tempus consectetur fringilla. Donec mattis vehicula mollis.");
            result.setTitle("This is the title of article " + i);
            result.setFullUri("article-full-uri");
            articles.add(result);
        }
        return articles;
    }

    protected static List<ContentSearchResult> getSampleDiscussions(int numDiscussions) {
        List<ContentSearchResult> discussions = new ArrayList<ContentSearchResult>();
        for (int i = 1; i <= numDiscussions; i++) {
            ContentSearchResult result = new ContentSearchResult();
            result.setContentKey(new ContentKey("Discussion",(long)i));
            result.setResultSummary("Vivamus ullamcorper accumsan convallis. Aliquam adipiscing, arcu eu adipiscing viverra, nulla tellus tempor dui, sit amet accumsan nibh ligula non nibh. Nam ornare congue rhoncus. In aliquet eleifend lectus vitae malesuada. Donec mattis nibh ac augue dapibus condimentum. Cras velit elit, dignissim id tempus et, imperdiet quis enim. Proin vitae velit risus. Etiam eget elit ut nibh adipiscing tincidunt. Cras augue lorem, egestas ut vestibulum a, suscipit quis turpis. Aliquam pretium mollis commodo. Morbi id turpis metus, in porttitor metus.");
            result.setTitle("This is the title of discussion " + i);
            result.setUsername("chriskimm");
            result.setDiscussionBoardId(14L);
            result.setDiscussionBoardTitle("Discussion Board Name");
            result.setFullUri("discussion-full-uri");
            result.setNumReplies((i * 7) % 19);
            Calendar date = Calendar.getInstance();
            // 15 days ago
            date.add(Calendar.DATE, -15);
            result.setDateCreated(date.getTime());
            discussions.add(result);
        }
        return discussions;
    }

    protected static int getTotalPages(int numArticles, int numDiscussions, String type) {
        int totalPages;
        if (numArticles > 0 && numDiscussions > 0) {
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
        } else {
            type = null;
        }
        return type;
    }

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
