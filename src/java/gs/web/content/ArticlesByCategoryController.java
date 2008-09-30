package gs.web.content;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import gs.data.content.IArticleCategoryDao;
import gs.data.content.ArticleCategory;
import gs.data.search.Searcher;
import gs.data.search.Indexer;
import gs.data.search.GSAnalyzer;
import gs.web.search.ResultsPager;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ArticlesByCategoryController extends AbstractController {
    private static final Log _log = LogFactory.getLog(ArticlesByCategoryController.class);

    protected static final String MODEL_SUBCATEGORY = "subcategory";
    protected static final String MODEL_PAGE = "p";
    protected static final String MODEL_PAGE_SIZE = "pageSize";
    protected static final String MODEL_RESULTS = "mainResults";
    protected static final String MODEL_TOTAL_HITS = "total";

    /** Page number */
    public static final String PARAM_PAGE = "p";
    /** Allow override of category id */
    public static final String PARAM_ID = "id";
    /** Results per page */
    public static final int PAGE_SIZE = 10;

    private Searcher _searcher;
    private IArticleCategoryDao _articleCategoryDao;
    /** Whether to look up the subcategory's parent categories */
    private boolean _getParents = false;

    // GS-7210: Used to boost articles by relevance.
    private QueryParser _titleParser;

    public ArticlesByCategoryController() {
        _titleParser = new QueryParser(Indexer.ARTICLE_TITLE, new GSAnalyzer());
        _titleParser.setDefaultOperator(QueryParser.Operator.OR);
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();
        // Look in the URL for parameters describing the category
        List<ArticleCategory> categories;
        if (request.getParameter(PARAM_ID) == null) {
            categories = getCategoriesFromURI(request.getRequestURI());
        } else {
            // allow request parameter to override URI
            categories = getCategoriesFromId(request.getParameter(PARAM_ID));
        }
        int page = 1;
        // check for page number
        String p = request.getParameter(PARAM_PAGE);
        if (p != null) {
            try {
                page = Integer.parseInt(p);
            } catch (Exception e) {
                // ignore this and just assume the page is 1.
            }
        }
        model.put(MODEL_PAGE, page);
        model.put(MODEL_PAGE_SIZE, PAGE_SIZE); // results per page

        // if we found a category, ask the searcher for results and put them in the model
        if (categories != null && categories.size() > 0) {
            model.put(MODEL_SUBCATEGORY, categories.get(0));
            storeResultsForCategory(categories.get(0), model, page);
        }

        return new ModelAndView("content/articleCategory", model);
    }

    /**
     * Queries the search indexes for any articles tagged with this category and stores the results
     * in the model
     *
     * @param category Category to search for
     * @param model Model to place results
     * @param page the page of results
     */
    protected void storeResultsForCategory(ArticleCategory category, Map<String, Object> model, int page) {
        BooleanQuery bq = new BooleanQuery();
        TermQuery termQuery = new TermQuery(new Term("category", category.getType()));
        bq.add(termQuery, BooleanClause.Occur.MUST);

        // Begin: GS-7210
        String typeDisplay = category.getTypeDisplay();
        if (StringUtils.isNotBlank(typeDisplay)) {
            try {
                Query titleQuery = _titleParser.parse(typeDisplay);
                bq.add(titleQuery, BooleanClause.Occur.SHOULD);
            } catch (ParseException pe) {
                _log.warn("Couldn't parse article category.", pe);
            }
        }
        // End: GS-7210

        Filter typeFilter =
                new CachingWrapperFilter(new QueryFilter(new TermQuery(new Term("type", "article"))));

        Hits hits = _searcher.search(bq, null, null, typeFilter);

        if (hits != null && hits.length() > 0) {
            model.put(MODEL_TOTAL_HITS, hits.length());
            ResultsPager resultsPager = new ResultsPager(hits, ResultsPager.ResultType.topic);
            model.put(MODEL_RESULTS, resultsPager.getResults(page, PAGE_SIZE));
        }
    }

    protected List<ArticleCategory> getCategoriesFromURI(String requestURI) {
        String categoryId = null;
        String requestUri = requestURI.replaceAll("/gs-web", "");
        requestUri = requestUri.replaceAll("/articles/", "");
        String[] rs = StringUtils.split(requestUri, "/");
        if (rs.length >= 1) {
            categoryId = rs[0];
        }
        if (categoryId != null && StringUtils.isNumeric(categoryId)) {
            return getCategoriesFromId(categoryId);
        } else {
            _log.error("Can't interpret first parameter as integer id: " + requestURI);
        }

        return null;
    }

    protected List<ArticleCategory> getCategoriesFromId(String categoryId) {
        List<ArticleCategory> categories = new ArrayList<ArticleCategory>();
        ArticleCategory category = _articleCategoryDao.getArticleCategory(Integer.valueOf(categoryId));
        if (category != null) {
            categories.add(category);

            // only proceed if the parent type looks valid
            // it doesn't seem this page needs to know what the parent categories are
            // I've disabled this loop for now, but can re-enable it by setting getParents to true
            if (_getParents && category.getParentType() != null &&
                    !StringUtils.equals(category.getType(), category.getParentType())) {
                // grab parent
                ArticleCategory parent = _articleCategoryDao.getArticleCategoryByType(category.getParentType());
                while (parent != null) {
                    // add parent to the list
                    categories.add(parent);
                    // if the parent's parent looks invalid, break out of the loop
                    if (StringUtils.equals(parent.getType(), parent.getParentType())) {
                        _log.warn("Category's type \"" + parent.getType() +
                                "\" equals parent \"" + parent.getParentType() + "\"");
                        break;
                    } else if (parent.getParentType() == null) {
                        break;
                    }
                    // otherwise grab the parent's parent
                    parent = _articleCategoryDao.getArticleCategoryByType(parent.getParentType());
                } // end while loop
            } // end if parentType != null
        } // end if category != null
        return categories;
    }

    public IArticleCategoryDao getArticleCategoryDao() {
        return _articleCategoryDao;
    }

    public void setArticleCategoryDao(IArticleCategoryDao articleCategoryDao) {
        _articleCategoryDao = articleCategoryDao;
    }

    public Searcher getSearcher() {
        return _searcher;
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }

    public boolean isGetParents() {
        return _getParents;
    }

    public void setGetParents(boolean getParents) {
        _getParents = getParents;
    }
}
