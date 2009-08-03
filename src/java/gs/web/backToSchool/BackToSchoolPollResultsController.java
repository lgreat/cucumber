package gs.web.backToSchool;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import gs.data.content.ArticleComment;
import gs.data.content.IArticleDao;
import gs.web.util.UrlBuilder;

public class BackToSchoolPollResultsController extends AbstractController {
    private IArticleDao _legacyArticleDao;
    public String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        long legacyArticleId = 10000L;
        List<ArticleComment> comments = _legacyArticleDao.getArticleComments(legacyArticleId);
        model.put("comments", comments);

        UrlBuilder builder = new UrlBuilder(UrlBuilder.B2S_POLL_LANDING_PAGE);
        model.put("contentUrl", builder.asFullUrl(request));
        model.put("legacyArticleId", String.valueOf(legacyArticleId));

        return new ModelAndView(_viewName, model);
    }

    public void setArticleDao(IArticleDao legacyArticleDao) {
        _legacyArticleDao = legacyArticleDao;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
