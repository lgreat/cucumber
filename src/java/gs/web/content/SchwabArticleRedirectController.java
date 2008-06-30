package gs.web.content;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.context.SessionContext;

public class SchwabArticleRedirectController implements Controller {
    private Log _log = LogFactory.getLog(SchwabArticleRedirectController.class);

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionContext context = (SessionContext) request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);
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
