package gs.web.content;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.content.IArticleDao;
import gs.data.content.Article;

import java.util.Map;
import java.util.HashMap;

/**
 * This is the controller for the article page.  A single article and any
 * associated tools are displayed on article.page given an article id.
 *  
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ArticleController extends AbstractController {

    /** Spring Bean ID */
    public static final String BEAN_ID = "/content/article.page";

    /** Log4j logger */
    private static final Logger _log = Logger.getLogger(ArticleController.class);

    /** Provides access to database articles */
    private IArticleDao _articleDao;

    /** Article id GET request parameter */
    public static final String PARAM_AID = "aid";
    /** Whether this is a new style or old style article */
    public static final String MODEL_NEW_ARTICLE = "newArticle";
    /** Article itself */
    public static final String MODEL_ARTICLE = "article";

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) {
        String articleId = request.getParameter(PARAM_AID);
        Map<String, Object> model = new HashMap<String, Object>();

        try {
            Article article = _articleDao.getArticleFromId(Integer.valueOf(articleId));
            model.put(MODEL_NEW_ARTICLE, isArticleNewStyle(article));
            model.put(MODEL_ARTICLE, article);
        } catch (NumberFormatException nfe) {
            _log.warn("Bad article id: " + articleId);
        }
        return new ModelAndView("content/article", model);
    }

    protected boolean isArticleNewStyle(Article article) {
        return StringUtils.contains(article.getArticleText(),
                "<div id=\"article-main\">");
    }

    public void setArticleDao(IArticleDao articleDao) {
        _articleDao = articleDao;
    }
}
