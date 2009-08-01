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

public class BackToSchoolPollResultsController extends AbstractController {
    private IArticleDao _legacyArticleDao;
    public String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        List<ArticleComment> comments = _legacyArticleDao.getArticleComments(10000L);
        model.put("comments", comments);

        return new ModelAndView(_viewName, model);
    }

    public void setArticleDao(IArticleDao legacyArticleDao) {
        _legacyArticleDao = legacyArticleDao;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
