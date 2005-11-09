/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: FeaturedArticlesController.java,v 1.3 2005/11/09 19:52:52 apeterson Exp $
 */
package gs.web.content;

import gs.data.content.IArticleDao;
import gs.data.content.Article;
import gs.web.ISessionFacade;
import gs.web.SessionFacade;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller to display all articles
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class FeaturedArticlesController extends AbstractController {

    private static final Log _log = LogFactory.getLog(FeaturedArticlesController.class);

    private String _viewName;

    private IArticleDao _articleDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

        ISessionFacade sessionFacade = SessionFacade.getInstance(request);

        String heading = "Today&#8217s Feature";
        Article article = null;
        String posStr = request.getParameter("pos");
        if (StringUtils.isNumeric(posStr)) {
            int pos = Integer.valueOf(posStr).intValue();
            article = _articleDao.getFeaturedArticle(sessionFacade.getStateOrDefault(), pos);

            if (pos==2) {
                heading = "What&#8217s New?";
            }
        }

        if (article== null) {
            List articles = _articleDao.getFeaturedArticles(sessionFacade.getStateOrDefault());
            article = (Article) articles.get(0);
        }


        Map model = new HashMap(2);
        model.put("article", article);
        model.put("heading", heading);        

        return new ModelAndView(_viewName, model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        this._viewName = viewName;
    }

    public IArticleDao getArticleDao() {
        return _articleDao;
    }

    public void setArticleDao(IArticleDao articleDao) {
        _articleDao = articleDao;
    }

}
