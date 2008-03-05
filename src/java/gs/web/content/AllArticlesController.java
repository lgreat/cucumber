/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AllArticlesController.java,v 1.12 2008/03/05 18:11:24 cpickslay Exp $
 */
package gs.web.content;

import gs.data.content.Article;
import gs.data.content.ArticleCategory;
import gs.data.content.ArticleManager;
import gs.data.content.IArticleDao;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
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
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class AllArticlesController extends AbstractController {

    private static final Log _log = LogFactory.getLog(AllArticlesController.class);

    private String _viewName;

    private IArticleDao _articleDao;

    private ArticleManager _articleManager;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);

        List articles = _articleDao.getArticlesForState(sessionContext.getStateOrDefault());

        Map catMap = new LinkedHashMap();
        Map sccMap = new LinkedHashMap();
        Map model = new HashMap();

        //initialize the map that holds all the categories
        List categories = _articleManager.getAllCategories();
        for (int i = 0; i < categories.size(); i++) {
            ArticleCategory articleCategory = (ArticleCategory) categories.get(i);
            catMap.put(articleCategory, null);
            sccMap.put(articleCategory, null);
        }

        for (int i = 0; i < articles.size(); i++) {
            Article article = (Article) articles.get(i);
            categories = _articleManager.getCategories(article.getCategory());

            for (int j = 0; j < categories.size(); j++) {
                ArticleCategory articleCategory = (ArticleCategory) categories.get(j);

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
        model.put("index", new Integer(0));
        model.put("articles", articles);

        return new ModelAndView(_viewName, model);
    }

    private void addArticleToMap(Map map, ArticleCategory category, Article a) {
        if (map.containsKey(category)) {
            List articlesInCategory = (List) map.get(category);

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
