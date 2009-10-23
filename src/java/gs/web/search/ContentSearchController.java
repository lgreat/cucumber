package gs.web.search;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import gs.web.util.UrlBuilder;

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

    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {

        Map<String, Object> model = new HashMap<String, Object>();

        String sample = request.getParameter(PARAM_SAMPLE);
        boolean isSample = StringUtils.isNotBlank(sample);

        String searchQuery = request.getParameter(PARAM_SEARCH_QUERY);
        int page = getPageNumber(request);
        String type = getType(request);

        if (isSample) {
            searchQuery = "friendship";
            populateModelWithSampleResults(model, searchQuery, page, type, sample);
        } else {
            // TODO-8876 use Solr to search
            model.put(MODEL_SUGGESTED_SEARCH_QUERY, "friendship");
            /*
            model.put(MODEL_NUM_RESULTS, 0);
            model.put(MODEL_NUM_ARTICLES, 0);
            model.put(MODEL_NUM_DISCUSSIONS, 0);
            model.put(MODEL_TOTAL_PAGES, 0);
            */
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
        
        model.put(MODEL_URL, getUrl(request, searchQuery, page, type, sample));
        model.put(MODEL_URL_WITHOUT_PAGE_NUM, getUrlWithoutPageNumber(request, searchQuery, type, sample));

        model.put(MODEL_TOTAL_PAGES, getTotalPages(numArticles, numDiscussions, type));

        // TODO-8876 fix me!
        model.put(MODEL_ARTICLE_RESULTS, new ArrayList());
        model.put(MODEL_COMMUNITY_RESULTS, new ArrayList());

        return new ModelAndView(_viewName, model);
    }

    protected void populateModelWithSampleResults(Map<String, Object> model, String searchQuery, int page, String filter, String sample) {
        int numArticles;
        int numDiscussions;
        int totalPages = 0;
        String pageTitlePrefix;
        if (SAMPLE_NO_ARTICLES.equals(sample)) {
            numArticles = 0;
            numDiscussions = 173;
            pageTitlePrefix = "Results for";
        } else if (SAMPLE_NO_DISCUSSIONS.equals(sample)) {
            numArticles = 68;
            numDiscussions = 0;
            pageTitlePrefix = "Results for";
        } else if (SAMPLE_ALL_RESULTS.equals(sample)) {
            numArticles = 68;
            numDiscussions = 173;
            if (TYPE_ARTICLES.equals(filter)) {
                pageTitlePrefix = "Article results for";
            } else if (TYPE_COMMUNITY.equals(filter)) {
                pageTitlePrefix = "Community results for";
            } else {
                pageTitlePrefix = "Results for";
            }
        } else {
            // SAMPLE_NO_RESULTS
            numArticles = 0;
            numDiscussions = 0;
            totalPages = 0;
            pageTitlePrefix = null;
            model.put(MODEL_SUGGESTED_SEARCH_QUERY, "friendship");
            model.put(MODEL_TOTAL_PAGES, 2);
        }

        model.put(MODEL_NUM_RESULTS, String.valueOf(numArticles + numDiscussions));
        model.put(MODEL_NUM_ARTICLES, numArticles);
        model.put(MODEL_NUM_DISCUSSIONS, numDiscussions);
        model.put(MODEL_PAGE_TITLE_PREFIX, pageTitlePrefix);
    }

    protected int getTotalPages(int numArticles, int numDiscussions, String type) {
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

    protected String getUrlWithoutPageNumber(HttpServletRequest request, String searchQuery, String type, String sample) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.CONTENT_SEARCH, searchQuery, (Integer)null, type, sample);
        return builder.asSiteRelative(request);
    }

    protected String getUrl(HttpServletRequest request, String searchQuery, int page, String type, String sample) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.CONTENT_SEARCH, searchQuery, page, type, sample);
        return builder.asSiteRelative(request);
    }

    /**
     * Extract the page number from the request. Defaults to 1.
     */
    protected int getPageNumber(HttpServletRequest request) {
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
    protected String getType(HttpServletRequest request) {
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
}
