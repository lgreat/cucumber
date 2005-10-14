/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AllArticlesController.java,v 1.3 2005/10/14 23:21:26 apeterson Exp $
 */
package gs.web.content;

import gs.data.content.Article;
import gs.data.content.ArticleCategory;
import gs.data.content.ArticleManager;
import gs.data.content.IArticleDao;
import gs.web.ISessionFacade;
import gs.web.SessionFacade;
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

    public static final String BEAN_ID = "/content/allArticles.page";

    private static final Log _log = LogFactory.getLog(AllArticlesController.class);

    private String _viewName;

    private IArticleDao _articleDao;

    private ArticleManager _articleManager;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {


        ISessionFacade sessionFacade = SessionFacade.getInstance(request);

        List articles = _articleDao.getArticlesForState(sessionFacade.getStateOrDefault());

        Article article = null;
        ArticleCategory articleCategory = null;
        Map catMap = new LinkedHashMap();
        Map sccMap = new LinkedHashMap();
        List insiderArticles = new ArrayList();
        Map model = new HashMap();


        //initialize the map that holds all the categories
        List categories = _articleManager.getAllCategories();
        for (int i = 0; i < categories.size(); i++) {
            articleCategory = (ArticleCategory) categories.get(i);
            catMap.put(articleCategory, null);
            sccMap.put(articleCategory, null);
        }

        //iterate through all articles
        for (int i = 0; i < articles.size(); i++) {
            article = (Article) articles.get(i);
            categories = _articleManager.getCategories(article.getCategory());

            if (article.isInsider()) {
                insiderArticles.add(article);
            }

            for (int j = 0; j < categories.size(); j++) {
                articleCategory = (ArticleCategory) categories.get(j);

                if (_articleManager.isSchoolChoiceCenterCategory(articleCategory)) {
                    addArticleToMap(sccMap, articleCategory, article);
                } else {
                    if (articleCategory == ArticleCategory.SPANISH) {
                        _log.debug(articleCategory.getCategoryName());
                    }
                    addArticleToMap(catMap, articleCategory, article);
                }
            }
        }

        model.put("categories", catMap);
        model.put("scc_categories", sccMap);
        model.put("insider_articles", insiderArticles);
        model.put("num_categories", String.valueOf(catMap.size() + sccMap.size()));
        model.put("index", new Integer(0));

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
