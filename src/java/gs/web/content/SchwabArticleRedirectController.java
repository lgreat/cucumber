package gs.web.content;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Redirect requests for schwablearning.org articles to the corresponding article on the greatschools site
 */
public class SchwabArticleRedirectController implements Controller {
    private Log _log = LogFactory.getLog(SchwabArticleRedirectController.class);

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String articleId = request.getParameter("r");

        // default to /content/specialNeeds.page
        String redirectURL = "/content/specialNeeds.page";

        try {
            int articleIdAsInt = Integer.parseInt(articleId);
            if (articleIdAsInt  < 1501) {
                int newArticleId = articleIdAsInt + 3000;
                redirectURL = "/cgi-bin/showarticle/" + newArticleId;
            }
        } catch (NumberFormatException e) {
            _log.error("Bad article ID passed to SchwabArticleRedirectController: " + articleId, e);
        }

        response.setStatus(301);
        response.setHeader("Location", redirectURL);
        response.setHeader("Connection", "close");
        return null;
    }
}
