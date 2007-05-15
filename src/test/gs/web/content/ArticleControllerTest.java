package gs.web.content;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.data.content.Article;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ArticleControllerTest extends BaseControllerTestCase {

    private ArticleController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller =
                (ArticleController)getApplicationContext().
                        getBean(ArticleController.BEAN_ID);
    }
    
    public void testHandleRequestInternal() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.setParameter(ArticleController.PARAM_AID, "2");
        ModelAndView mAndV =
                _controller.handleRequestInternal(request, getResponse());
        Article article =
                (Article)mAndV.getModel().get(ArticleController.MODEL_ARTICLE);
        assertEquals("Article title does not match.",
                "Who Makes School Discipline Decisions?", article.getTitle());
    }

    
}
