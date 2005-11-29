/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: FeaturedArticlesController.java,v 1.6 2005/11/29 01:37:38 apeterson Exp $
 */
package gs.web.content;

import gs.data.content.Article;
import gs.data.content.IArticleDao;
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
import java.util.Map;

/**
 * Controller to display all specified articles.
 *
 * @author Andrew Peterson <mailto:apeterson@greatschools.net>
 */
public class FeaturedArticlesController extends AbstractController {

    private static final Log _log = LogFactory.getLog(FeaturedArticlesController.class);

    private String _viewName;

    private IArticleDao _articleDao;
    public static final String POSITION_PARAM = "position";
    private static final String ABSTRACT_PARAM = "abstract";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

        ISessionFacade sessionFacade = SessionFacade.getInstance(request);

        String posStr = request.getParameter(POSITION_PARAM);
        if (StringUtils.isEmpty(posStr)) {
            posStr = IArticleDao.HOT_TOPIC; // bad default
            _log.warn("Default position being used. Please fix.");
        }

        boolean showAbstract = StringUtils.isNotEmpty(request.getParameter(ABSTRACT_PARAM));

        Article article = _articleDao.getFeaturedArticle(sessionFacade.getStateOrDefault(), posStr);

        // Allow param override
        final String paramHeading = request.getParameter("heading");
        String heading = "Today&#8217s Feature";
        if (StringUtils.isNotEmpty(paramHeading)) {
            heading = paramHeading;
        } else {
            if (StringUtils.equals(posStr, IArticleDao.FOCUS_ON_CHOICE)) {
                heading = "Focus on Choice";
            } else if (StringUtils.equals(posStr, IArticleDao.HOT_TOPIC)) {
                heading = ""; // no heading
            } else {
                heading = "Today&#8217s Feature";
            }
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
