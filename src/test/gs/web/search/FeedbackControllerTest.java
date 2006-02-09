package gs.web.search;

import junit.framework.TestCase;
import gs.web.BaseControllerTestCase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class FeedbackControllerTest extends BaseControllerTestCase {
    public void testOnSubmit() throws Exception {
        FeedbackController feedbackController = new FeedbackController();
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        ModelAndView modelAndView =
                feedbackController.onSubmit(request, response, null, null);
        assertNotNull(modelAndView.getView().toString());
    }
}
