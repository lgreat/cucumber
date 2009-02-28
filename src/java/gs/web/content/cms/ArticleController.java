package gs.web.content.cms;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class ArticleController extends AbstractController {
    private static final Logger _log = Logger.getLogger(ArticleController.class);

    /** Spring Bean ID */
    public static final String BEAN_ID = "/content/cms/article.page";
    public static final String VIEW_NAME = "content/cms/article";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();

        Map<String, Object> model = new HashMap<String, Object>();
        return new ModelAndView(VIEW_NAME, model);
    }
}
