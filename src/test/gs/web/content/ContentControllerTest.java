/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ContentControllerTest.java,v 1.14 2006/05/04 18:53:10 chriskimm Exp $
 */
package gs.web.content;

import gs.data.content.Article;
import gs.data.content.ArticleManager;
import gs.data.content.IArticleDao;
import gs.data.state.State;
import gs.data.admin.IPropertyDao;
import gs.web.BaseControllerTestCase;
import gs.web.util.Anchor;
import gs.web.util.ListModel;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The purpose is to test the various controllers in the content package.
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
        c.setSingleArticleViewName("/promo/featuredArticles");

        getRequest().setParameter("position", IArticleDao.HOT_TOPIC);

        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        assertEquals("/promo/featuredArticles", modelAndView.getViewName());

        Object article = modelAndView.getModel().get("article");
        assertTrue(article instanceof Article);
        assertEquals("", modelAndView.getModel().get("heading"));


        getRequest().setParameter("position", IArticleDao.FOCUS_ON_CHOICE);
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        article = modelAndView.getModel().get("article");
        assertTrue(article instanceof Article);
        assertEquals("School Choosers Guide", modelAndView.getModel().get("heading"));

        getRequest().setParameter("position", IArticleDao.PATH1_FEATURE);
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        article = modelAndView.getModel().get("article");
        assertTrue(article instanceof Article);
        assertEquals("Featured Topics", modelAndView.getModel().get("heading"));
    }

    public void testFeaturedArticlesControllerMultiple() throws Exception {
        FeaturedArticlesController c = (FeaturedArticlesController) getApplicationContext().getBean("/promo/featuredArticles.module");

        getRequest().addParameter("position", IArticleDao.FOCUS_ON_CHOICE);
        getRequest().addParameter("count", "3");
        getRequest().addParameter("heading", "Grateful");

        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        Map model = modelAndView.getModel();
        assertEquals("/unorderedList", modelAndView.getViewName());
        assertNull(model.get("article"));

        List articles = (List) model.get(ListModel.RESULTS);
        assertTrue(articles instanceof Collection);
        assertEquals(5, articles.size());
        assertEquals(Anchor.class, articles.get(0).getClass());
        assertEquals(Anchor.class, articles.get(1).getClass());
        assertEquals(Anchor.class, articles.get(2).getClass());
        assertEquals("Grateful", model.get(ListModel.HEADING)); // Grateful Heading

        // Ask for 4, but only get 3
        getRequest().setParameter("count", "4");
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        model = modelAndView.getModel();
        articles = (List) model.get(ListModel.RESULTS);
        assertTrue(articles instanceof Collection);
        assertEquals(5, articles.size());

        // Ask for 2, and get 2
        getRequest().setParameter("count", "2");
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        model = modelAndView.getModel();
        assertEquals("/unorderedList", modelAndView.getViewName());
        assertNull(model.get("article"));

        articles = (List) model.get(ListModel.RESULTS);
        assertTrue(articles instanceof Collection);
        assertEquals(4, articles.size());
        assertEquals(Anchor.class, articles.get(0).getClass());
        assertEquals(Anchor.class, articles.get(1).getClass());

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

    public void testParentPollController() throws Exception {
        ParentPollController controller = new ParentPollController();
        controller.setPropertyDao(new IPropertyDao() {
            public String getProperty(String key) {
                return "8888";
            }

            public void setProperty(String key, String value) {
            }
            
            public String getProperty(String key, String defaultValue) {
                return "8888";
            }
        });
        controller.setViewName("someView");

        ModelAndView modelAndView = controller.handleRequestInternal(getRequest(), getResponse());

        assertEquals("8888", modelAndView.getModel().get(ParentPollController.MODEL_PARENT_POLL_ID));

    }
}
