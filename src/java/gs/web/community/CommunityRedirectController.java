package gs.web.community;

import gs.web.util.context.SessionContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * GS-3687: Create a URI that cobrands can use to direct users to community pages with the
 * cobrand headers. This can't be removed with new community since some cobrands may still use it.
 *
 * To understand this, realize that old community does not have cobrand subdomains, so the only
 * way it knew how to show cobrand headers was through a cookie. This redirect ensures that the user
 * gets a cobrand cookie (from this controller) before sending them on to old community.
 */
public class CommunityRedirectController implements Controller {
    public static final String PAGE_PARAM = "page";
    
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestedPage = request.getParameter(PAGE_PARAM);
        if (!requestedPage.startsWith("/")) {
            requestedPage = "/" + requestedPage;
        }

        SessionContext context = (SessionContext) request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);
        String communityHostname = context.getSessionContextUtil().getCommunityHost(request);
        String redirectURL = "http://" + communityHostname + requestedPage;

        response.setStatus(301);
        response.setHeader("Location", redirectURL);
        response.setHeader("Connection", "close");
        return null;
    }
}
