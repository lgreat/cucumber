package gs.web.search;

import gs.web.BaseControllerTestCase;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.mail.Message;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class FeedbackControllerTest extends BaseControllerTestCase {

    public void testOnSubmit() throws Exception {

        FeedbackController feedbackController = new FeedbackController();

        FeedbackCommand command = new FeedbackCommand();
        command.test = true;
        command.setComment("This is a comment");
        ModelAndView modelAndView = feedbackController.onSubmit(getRequest(),
                getResponse(), command, null);
        RedirectView view = (RedirectView)modelAndView.getView();
        assertEquals("/search/feedbackSubmit.page?state=ca", view.getUrl());

        ModelAndView mv2 = feedbackController.onSubmit(getRequest(),
                getResponse(), null, null);
        RedirectView view2 = (RedirectView)mv2.getView();
        assertEquals("/search/feedbackSubmit.page?state=ca", view2.getUrl());

        FeedbackCommand akCommand = new FeedbackCommand();
        akCommand.test = true;
        akCommand.setComment("This is a comment");
        akCommand.setState("AK");
        ModelAndView akMV = feedbackController.onSubmit(getRequest(),
                getResponse(), akCommand, null);
        RedirectView akView = (RedirectView)akMV.getView();
        assertEquals("/search/feedbackSubmit.page?state=ak", akView.getUrl());

    }

    public void testBuildMessage() throws Exception {
        FeedbackCommand fc = new FeedbackCommand();
        fc.test = true;

        fc.setComment("This is a comment");
        fc.setQuery("foo");

        Message message = FeedbackController.buildMessage(fc);
        assertEquals("Search Feedback: foo", message.getSubject());
    }
}
