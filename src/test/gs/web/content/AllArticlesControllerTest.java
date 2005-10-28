/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AllArticlesControllerTest.java,v 1.2 2005/10/28 21:25:41 dlee Exp $
 */
package gs.web.content;

import gs.data.content.ArticleManager;
import gs.data.content.IArticleDao;
import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * The purpose is to test the AllArticlesController
 *
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class AllArticlesControllerTest extends BaseControllerTestCase {

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
}
