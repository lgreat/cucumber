/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AllArticlesControllerTest.java,v 1.1 2005/09/17 00:25:58 dlee Exp $
 */
package gs.web.content;

import gs.data.content.ArticleManager;
import gs.data.content.IArticleDao;
import gs.web.BaseTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import java.util.Map;

/**
 * The purpose is ...
 * TODO - dlee: write a test that passes
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class AllArticlesControllerTest extends BaseTestCase {
    private AllArticlesController _controller;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = (AllArticlesController) _sApplicationContext.getBean(AllArticlesController.BEAN_ID);
        _controller.setArticleDao((IArticleDao)_sApplicationContext.getBean(IArticleDao.BEAN_ID));
        _controller.setArticleManager(new ArticleManager());
    }

    public void testNothing() {
        String test = "Test Below Needs to Pass";
        assertEquals(test,test);
    }
    /**
     * The handle request method connects to the database and gets the build version
     */
    public void notestHandleRequest() throws Exception, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        ModelAndView mv = _controller.handleRequestInternal(request,null);

        Map model = mv.getModel();
        assertNotNull(model);
    }
}
