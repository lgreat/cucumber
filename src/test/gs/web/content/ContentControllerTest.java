/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ContentControllerTest.java,v 1.4 2005/11/09 01:54:06 apeterson Exp $
 */
package gs.web.content;

import gs.data.content.Article;
import gs.data.content.ArticleManager;
import gs.data.content.IArticleDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * The purpose is to test the AllArticlesController
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class ContentControllerTest extends BaseControllerTestCase {

    public void testGetCategories() throws Exception {
        AllArticlesController c = new AllArticlesController();
        c.setApplicationContext(getApplicationContext());
        c.setArticleDao((IArticleDao) getApplicationContext().getBean(IArticleDao.BEAN_ID));
        c.setArticleManager((ArticleManager) getApplicationContext().getBean(ArticleManager.BEAN_ID));

        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        Map catMap = (Map) modelAndView.getModel().get("categories");
        assertTrue(catMap.size() > 0);
        Map sccMap = (Map) modelAndView.getModel().get("scc_categories");
        assertTrue(catMap.size() > 0);
        List insiderArticles = (List) modelAndView.getModel().get("insider_articles");
        assertTrue(insiderArticles.size() > 0);
        String numberOfCatgegories = String.valueOf(catMap.size() + sccMap.size());
        assertEquals(numberOfCatgegories, (String) modelAndView.getModel().get("num_categories"));
        assertEquals(new Integer(0), (Integer) modelAndView.getModel().get("index"));

    }


    public void testFeaturedArticlesController() throws Exception {
        FeaturedArticlesController c = new FeaturedArticlesController();
        c.setApplicationContext(getApplicationContext());
        c.setArticleDao((IArticleDao) getApplicationContext().getBean(IArticleDao.BEAN_ID));

        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        Object article = modelAndView.getModel().get("article");
        assertTrue(article instanceof Article);
    }

    public void testPremiumArticlesController() throws Exception {
        PremiumArticlesController c = new PremiumArticlesController();
        c.setApplicationContext(getApplicationContext());
        c.setArticleDao((IArticleDao) getApplicationContext().getBean(IArticleDao.BEAN_ID));

        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        List articles = (List) modelAndView.getModel().get("articles");
        //ca has premium articles
        assertTrue(articles.size() > 0);
        assertTrue(articles.get(0) instanceof Article);

        //de does not have premium articles
        assertFalse(State.DE.isSubscriptionState());
        getSessionContext().setState(State.DE);
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        articles = (List) modelAndView.getModel().get("articles");
        assertNull(articles);
    }
}
