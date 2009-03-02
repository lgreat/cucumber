package gs.web.content.cms;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import gs.data.content.cms.ICmsArticleDao;
import gs.data.content.cms.CmsArticle;
import gs.data.content.Article;

public class CmsArticleController extends AbstractController {
    private static final Logger _log = Logger.getLogger(CmsArticleController.class);

    /** Spring Bean ID */
    public static final String BEAN_ID = "/content/cms/article.page";
    public static final String VIEW_NAME = "content/cms/article";

    private ICmsArticleDao _articleDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();

        Map<String, Object> model = new HashMap<String, Object>();

        CmsArticle article = _articleDao.get(uri);

        model.put("article", article);
        return new ModelAndView(VIEW_NAME, model);
    }

    public void setCmsArticleDao(ICmsArticleDao articleDao) {
        _articleDao = articleDao;
    }
}
