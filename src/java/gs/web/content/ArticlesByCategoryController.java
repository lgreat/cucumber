package gs.web.content;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.*;
import org.apache.lucene.index.Term;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import gs.data.content.IArticleCategoryDao;
import gs.data.content.ArticleCategory;
import gs.data.search.Searcher;
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

    public static final String PARAM_PAGE = "p";
    public static final int PAGE_SIZE = 10;

    private Searcher _searcher;
    private IArticleCategoryDao _articleCategoryDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        // Look in the URL for parameters
        List<ArticleCategory> categories = getCategoriesFromURI(request.getRequestURI());
        int page = 1;
        String p = request.getParameter(PARAM_PAGE);
        if (p != null) {
            try {
                page = Integer.parseInt(p);
            } catch (Exception e) {
                // ignore this and just assume the page is 1.
            }
        }
        model.put(MODEL_PAGE, page);

        if (categories != null) {
            model.put(MODEL_SUBCATEGORY, categories.get(categories.size()-1));

            ResultsPager resultsPager = getResultsForCategory(categories.get(0), model);
            if (resultsPager != null) {
                model.put(MODEL_PAGE_SIZE, PAGE_SIZE);
                model.put(MODEL_RESULTS, resultsPager.getResults(page, PAGE_SIZE));
            }
        }

        return new ModelAndView("content/articleCategory", model);
    }

    protected ResultsPager getResultsForCategory(ArticleCategory category, Map<String, Object> model) {
        TermQuery termQuery = new TermQuery(new Term("category", category.getType()));
        Filter typeFilter =
                new CachingWrapperFilter(new QueryFilter(new TermQuery(new Term("type", "article"))));

        Hits hits = _searcher.search(termQuery, null, null, typeFilter);

        if (hits != null && hits.length() > 0) {
            model.put(MODEL_TOTAL_HITS, hits.length());
            return new ResultsPager(hits, ResultsPager.ResultType.topic);
        }

        return null;
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
        categories.add(category);

        ArticleCategory parent = _articleCategoryDao.getArticleCategoryByType(category.getParentType());
        while (parent != null) {
            categories.add(parent);
            if (StringUtils.equals(parent.getType(), parent.getParentType())) {
                _log.warn("Category's type \"" + parent.getType() +
                        "\" equals parent \"" + parent.getParentType() + "\"");
                break;
            } else if (parent.getParentType() == null) {
                break;
            }
            parent = _articleCategoryDao.getArticleCategoryByType(parent.getParentType());
        }
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
}
