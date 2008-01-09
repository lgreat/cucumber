package gs.web.content;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.content.IArticleDao;
import gs.data.content.Article;

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

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        String schoolId = request.getParameter(PARAM_AID);
        ModelAndView mAndV = new ModelAndView("content/article");

        try {
            Article article = _articleDao.getArticleFromId(Integer.valueOf(schoolId));
            mAndV.getModel().put("article", article);
        } catch (NumberFormatException nfe) {
            _log.warn("Bad article id: " + schoolId);
        }
        return mAndV; 
    }

    public void setArticleDao(IArticleDao articleDao) {
        _articleDao = articleDao;
    }
}
