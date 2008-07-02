package gs.web.content;

import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Redirect requests for schwablearning.org articles to the corresponding article on the greatschools site
 */
public class SchwabArticleRedirectController implements Controller {
    private Log _log = LogFactory.getLog(SchwabArticleRedirectController.class);
    protected static Map<String, Integer> staticRedirects = new HashMap<String, Integer>() {{
        put("315",1213);
        put("366", 1164);
        put("370", 982);
        put("405", 993);
        put("516", 1050);
        put("532", 998);
        put("625", 989);
        put("878", 1160);
        put("1091", 999);
        put("1130", 1187);
    }};

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String articleId = request.getParameter("r");

        String hostPrefix = SessionContextUtil.getServerName(request);
        String wwwSiteHostname = "http://" + hostPrefix + ".greatschools.net";

        // default to /content/specialNeeds.page
        String redirectURL = wwwSiteHostname + "/content/specialNeeds.page";

        Integer staticArticleId = staticRedirects.get(articleId);
        if (staticArticleId != null) {
            redirectURL = wwwSiteHostname + "/cgi-bin/showarticle/" + staticArticleId;
        } else {
            try {
                int articleIdAsInt = Integer.parseInt(articleId);
                if (articleIdAsInt  < 1501) {
                    int newArticleId = articleIdAsInt + 2000;
                    redirectURL = wwwSiteHostname + "/cgi-bin/showarticle/" + newArticleId;
                }
            } catch (NumberFormatException e) {
                _log.error("Bad article ID passed to SchwabArticleRedirectController: " + articleId, e);
            }
        }

        response.setStatus(301);
        response.setHeader("Location", redirectURL);
        response.setHeader("Connection", "close");
        return null;
    }
}
