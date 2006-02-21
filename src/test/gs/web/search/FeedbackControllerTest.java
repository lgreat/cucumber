package gs.web.search;

import gs.web.BaseControllerTestCase;

import org.springframework.web.servlet.ModelAndView;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class FeedbackControllerTest extends BaseControllerTestCase {
    public void testOnSubmit() throws Exception {
        FeedbackController feedbackController = new FeedbackController();
        FeedbackCommand command = new FeedbackCommand();
        command.setComment("This is a comment");
        ModelAndView modelAndView =
                feedbackController.onSubmit(command);
        assertNotNull(modelAndView.getView().toString());
    }

    public void testSendFeedbackToHelpdesk() throws Exception {
        FeedbackCommand fc = new FeedbackCommand();
        fc.setComment("This is a comment");
    }
}
