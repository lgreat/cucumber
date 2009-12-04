package gs.web.community;

import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Redirect requests for schwablearning.org forum posts to the static archive
 */
public class SchwabForumRedirectController implements Controller {
    private Log _log = LogFactory.getLog(SchwabForumRedirectController.class);

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String threadId = request.getParameter("thread");

        String hostPrefix = SessionContextUtil.getServerName(request);
        String wwwSiteHostname = "http://" + hostPrefix + ".greatschools.org";
        String hostInfix = ("www".equals(hostPrefix)) ? "" : "." + hostPrefix;
        String forumArchiveHostname = "http://schwablearningforumarchive" + hostInfix + ".greatschools.org";

        // default to /content/specialNeeds.page
        String redirectURL = wwwSiteHostname + "/content/specialNeeds.page?fromSchwab=1";

        try {
            int articleIdAsInt = Integer.parseInt(threadId);
            redirectURL = forumArchiveHostname + "/thread/" + threadId + ".html";
        } catch (NumberFormatException e) {
            _log.warn("Bad thread ID passed to SchwabForumRedirectController: " + threadId, e);
        }

        response.setStatus(301);
        response.setHeader("Location", redirectURL);
        response.setHeader("Connection", "close");
        return null;
    }
}
