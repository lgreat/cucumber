/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: AllArticlesController.java,v 1.20 2009/12/04 22:15:14 npatury Exp $
 */
package gs.web.content;

import gs.data.content.Article;
import gs.data.content.ArticleCategoryEnum;
import gs.data.content.ArticleManager;
import gs.data.content.IArticleDao;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.RedirectView301;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Controller to display all articles
 *
 * @author greatschools.org>
 */
public class AllArticlesController extends AbstractController {

    private static final Log _log = LogFactory.getLog(AllArticlesController.class);

    private String _viewName;

    private IArticleDao _articleDao;

    private ArticleManager _articleManager;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

        // GS-7301 - standard requests for /content/allArticles.page are redirected
        // to /education-topics/
        if ("301".equals(getViewName())) {
            return new ModelAndView(new RedirectView301("/education-topics/"));
        }
        
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);

        List<Article> articles = _articleDao.getArticlesForState(sessionContext.getStateOrDefault());

        Map<ArticleCategoryEnum, List> catMap = new LinkedHashMap<ArticleCategoryEnum, List>();
        Map<ArticleCategoryEnum, List> sccMap = new LinkedHashMap<ArticleCategoryEnum, List>();
        Map<String, Object> model = new HashMap<String, Object>();

        //initialize the map that holds all the categories
        List<ArticleCategoryEnum> categories = _articleManager.getAllCategories();
        for (ArticleCategoryEnum articleCategory : categories) {
            catMap.put(articleCategory, null);
            sccMap.put(articleCategory, null);
        }

        for (Article article : articles) {
            categories = _articleManager.getCategories(article.getCategory());
            for (ArticleCategoryEnum articleCategory : categories) {
                if (articleCategory.isSchoolChoiceCenterCategory()) {
                    addArticleToMap(sccMap, articleCategory, article);
                } else {
                    addArticleToMap(catMap, articleCategory, article);
                }
            }
        }

        model.put("categories", catMap);
        model.put("scc_categories", sccMap);
        model.put("num_categories", String.valueOf(catMap.size() + sccMap.size()));
        model.put("index", 0);
        model.put("articles", articles);

        return new ModelAndView(_viewName, model);
    }

    private void addArticleToMap(Map<ArticleCategoryEnum, List> map, ArticleCategoryEnum category, Article a) {
        if (map.containsKey(category)) {
            List articlesInCategory = map.get(category);

            if (articlesInCategory == null) {
                articlesInCategory = new ArrayList();
            }
            articlesInCategory.add(a);
            map.put(category, articlesInCategory);
        }
    }


    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        this._viewName = viewName;
    }

    public IArticleDao getArticleDao() {
        return _articleDao;
    }

    public void setArticleDao(IArticleDao articleDao) {
        _articleDao = articleDao;
    }

    public ArticleManager getArticleManager() {
        return _articleManager;
    }

    public void setArticleManager(ArticleManager articleManager) {
        _articleManager = articleManager;
    }
}
