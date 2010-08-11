package gs.web.community;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import gs.data.community.DiscussionReply;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.jsp.Util;

public class RaiseYourHandSubmissionController extends DiscussionSubmissionController {
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors) throws Exception {
        DiscussionSubmissionCommand command = (DiscussionSubmissionCommand) commandObj;
        DiscussionReply reply = null;
        if (command.getDiscussionId() != null) {
            reply = handleRaiseYourHandSubmission(request, response, command);
        }

        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        out.println("{");
        if (reply != null) {
            out.println("\"status\":\"ok\",");
            out.println("\"replyId\":\"" + reply.getId() + "\",");
            out.println("\"body\":\"" + StringEscapeUtils.escapeJavaScript(reply.getBody()) + "\"");
        } else {
            out.println("\"status\":\"error\"");
        }
        out.println("}");

        return null;
    }

    protected DiscussionReply handleRaiseYourHandSubmission
            (HttpServletRequest request, HttpServletResponse response, DiscussionSubmissionCommand command)
            throws IllegalStateException {
        DiscussionReply reply = handleDiscussionReplySubmissionHelper(request, response, command, true);

        // omniture success event only if new raise your hand reply
        if (command.getDiscussionReplyId() == null) {
            OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
            ot.addSuccessEvent(CookieBasedOmnitureTracking.SuccessEvent.CommunityRaiseYourHand);
        }

        return reply;
    }
}
