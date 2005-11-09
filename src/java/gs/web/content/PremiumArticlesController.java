/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PremiumArticlesController.java,v 1.1 2005/11/09 00:53:09 dlee Exp $
 */
package gs.web.content;

import gs.data.content.IArticleDao;
import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionFacade;
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
 * Controller to display all premium articles
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class PremiumArticlesController extends AbstractController {

    private static final Log _log = LogFactory.getLog(FeaturedArticlesController.class);

    private String _viewName;

    private IArticleDao _articleDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

        ISessionFacade sessionFacade = SessionFacade.getInstance(request);

        State state = sessionFacade.getStateOrDefault();
        Map model = new HashMap();

        if (state.isSubscriptionState()) {
            List articles = _articleDao.getPremiumArticles(state);
            model.put("articles", articles);
        }

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
