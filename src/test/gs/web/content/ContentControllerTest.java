/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ContentControllerTest.java,v 1.18 2006/06/14 22:16:02 apeterson Exp $
 */
package gs.web.content;

import gs.data.admin.IPropertyDao;
import gs.data.content.Article;
import gs.data.content.ArticleManager;
import gs.data.content.IArticleDao;
import gs.web.BaseControllerTestCase;
import gs.web.util.Anchor;
import gs.web.util.AnchorListModel;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * The purpose is to test the various controllers in the content package.
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 * @noinspection FeatureEnvy,ProhibitedExceptionDeclared,HardcodedFileSeparator
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
        String numberOfCatgegories = String.valueOf(catMap.size() + sccMap.size());
        assertEquals(numberOfCatgegories, (String) modelAndView.getModel().get("num_categories"));
        assertEquals(new Integer(0), modelAndView.getModel().get("index"));

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

    /** @noinspection OverlyLongMethod*/
    public void testFeaturedArticlesControllerMultiple() throws Exception {
        FeaturedArticlesController c = (FeaturedArticlesController) getApplicationContext().getBean("/promo/featuredArticles.module");

        getRequest().addParameter("position", IArticleDao.FOCUS_ON_CHOICE);
        getRequest().addParameter("count", "3");
        getRequest().addParameter("heading", "Grateful");

        ModelAndView modelAndView = c.handleRequestInternal(getRequest(), getResponse());

        Map model = modelAndView.getModel();
        assertEquals("/unorderedList", modelAndView.getViewName());
        assertNull(model.get("article"));

        List articles = (List) model.get(AnchorListModel.RESULTS);
        assertTrue(articles instanceof Collection);
        assertEquals(5, articles.size());
        assertEquals(Anchor.class, articles.get(0).getClass());
        assertEquals(Anchor.class, articles.get(1).getClass());
        assertEquals(Anchor.class, articles.get(2).getClass());
        assertEquals("Grateful", model.get(AnchorListModel.HEADING)); // Grateful Heading

        // Ask for 4, but only get 3
        getRequest().setParameter("count", "4");
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        model = modelAndView.getModel();
        articles = (List) model.get(AnchorListModel.RESULTS);
        assertTrue(articles instanceof Collection);
        assertEquals(5, articles.size());

        // Ask for 2, and get 2
        getRequest().setParameter("count", "2");
        modelAndView = c.handleRequestInternal(getRequest(), getResponse());
        model = modelAndView.getModel();
        assertEquals("/unorderedList", modelAndView.getViewName());
        assertNull(model.get("article"));

        articles = (List) model.get(AnchorListModel.RESULTS);
        assertTrue(articles instanceof Collection);
        assertEquals(4, articles.size());
        assertEquals(Anchor.class, articles.get(0).getClass());
        assertEquals(Anchor.class, articles.get(1).getClass());

    }

    public void testParentPollController() throws Exception {
        ParentPollController controller = new ParentPollController();
        controller.setPropertyDao(new IPropertyDao() {
            public String getProperty(String key) {
                return "8888";
            }

            public void setProperty(String key, String value) {
            }

            public void removeProperty(String key) {
            }

            public String getProperty(String key, String defaultValue) {
                return "8888";
            }

            public Date getPropertyAsDate(String key) {
                return null;
            }

            public void setPropertyAsDate(String key, Date date) {
            }
        });
        controller.setViewName("someView");

        ModelAndView modelAndView = controller.handleRequestInternal(getRequest(), getResponse());

        assertEquals("8888", modelAndView.getModel().get(ParentPollController.MODEL_PARENT_POLL_ID));

    }
}
