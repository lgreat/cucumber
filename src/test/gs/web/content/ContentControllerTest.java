/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ContentControllerTest.java,v 1.21 2008/10/14 22:21:52 chriskimm Exp $
 */
package gs.web.content;

import gs.data.admin.IPropertyDao;
import gs.data.content.Article;
import gs.data.content.ArticleManager;
import gs.data.content.IArticleDao;
import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.Map;

/**
 * The purpose is to test the various controllers in the content package.
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 * @noinspection FeatureEnvy,ProhibitedExceptionDeclared,HardcodedFileSeparator
 */
public class ContentControllerTest extends BaseControllerTestCase {

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
