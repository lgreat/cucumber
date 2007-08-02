package gs.web.community;

import gs.web.util.context.SessionContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
