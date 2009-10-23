package gs.web.search;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

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
    public static final String MODEL_TYPE = "type";

    public static final String PARAM_SEARCH_QUERY = "q";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_TYPE = "type";

    public static final String TYPE_COMMUNITY = "community";
    public static final String TYPE_ARTICLES = "articles";
    public static final String TYPE_ALL = "all";

    public static final int PAGE_SIZE = 25;

    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {

        Map<String, Object> model = new HashMap<String, Object>();

        String searchQuery = request.getParameter(PARAM_SEARCH_QUERY);
        int page = getPageNumber(request);
        String type = getType(request);

        model.put(MODEL_SEARCH_QUERY, searchQuery);
        model.put(MODEL_SUGGESTED_SEARCH_QUERY, "friendship");
        model.put(MODEL_NUM_RESULTS, 241);
        model.put(MODEL_NUM_ARTICLES, 68);
        model.put(MODEL_NUM_DISCUSSIONS, 173);
        model.put(MODEL_PAGE, page);
        model.put(MODEL_TOTAL_PAGES, 2);
        model.put(MODEL_PAGE_SIZE, PAGE_SIZE);
        model.put(MODEL_TYPE, type);


        return new ModelAndView(_viewName, model);
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
     * Extract the content type from the request. Defaults to TYPE_ALL.
     */
    protected String getType(HttpServletRequest request) {
        String type;
        String typeParam = request.getParameter(PARAM_TYPE);
        if (TYPE_COMMUNITY.equals(typeParam)) {
            type = typeParam;
        } else if (TYPE_ARTICLES.equals(typeParam)) {
            type = typeParam;
        } else {
            type = TYPE_ALL;
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
