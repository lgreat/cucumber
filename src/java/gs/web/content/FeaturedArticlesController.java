/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: FeaturedArticlesController.java,v 1.8 2005/11/30 00:09:58 apeterson Exp $
 */
package gs.web.content;

import gs.data.content.Article;
import gs.data.content.IArticleDao;
import gs.web.ISessionFacade;
import gs.web.SessionFacade;
import gs.web.util.Anchor;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller to display all specified articles.
 *
 * @author Andrew Peterson <mailto:apeterson@greatschools.net>
 */
public class FeaturedArticlesController extends AbstractController {

    private static final Log _log = LogFactory.getLog(FeaturedArticlesController.class);

    private String _singleArticleViewName;
    private String _multipleArticlesViewName;

    private UrlUtil _urlUtil = new UrlUtil();

    private IArticleDao _articleDao;

    /**
     * One or more "article position" strings, comma separated.
     */
    public static final String POSITION_PARAM = "position";

    // Single article display
    public static final String MODEL_ARTICLE = "article";

    // Multiple article display
    // model contains "header", a string; and "results", a list of Anchor objects

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {


        String posStr = request.getParameter(POSITION_PARAM);
        if (StringUtils.isEmpty(posStr)) {
            posStr = IArticleDao.HOT_TOPIC; // bad default
            _log.warn("Default position being used. Please fix.");
        }

        if (posStr.indexOf(',') != -1) {
            return handleMultipleArticles(request, posStr);
        } else {
            return handleSingleArticle(request, posStr);
        }
    }

    private ModelAndView handleSingleArticle(HttpServletRequest request, String posStr) {
        ISessionFacade sessionFacade = SessionFacade.getInstance(request);
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

        return new ModelAndView(_singleArticleViewName, model);
    }

    private ModelAndView handleMultipleArticles(HttpServletRequest request, String posStr) {

        ISessionFacade sessionFacade = SessionFacade.getInstance(request);


        String[] posStrs = StringUtils.split(posStr, ',');


        List items = new ArrayList(posStrs.length);
        for (int i = 0; i < posStrs.length; i++ ) {
            Article article = _articleDao.getFeaturedArticle(sessionFacade.getStateOrDefault(), posStrs[i]);

            Anchor anchor = new Anchor(_urlUtil.getArticleLink(sessionFacade.getStateOrDefault(), article, false),
                    article.getTitle());
            items.add(anchor);
        }

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
        model.put("results", items);
        model.put("heading", heading);

        return new ModelAndView(_multipleArticlesViewName, model);
    }

    public String getSingleArticleViewName() {
        return _singleArticleViewName;
    }

    public void setSingleArticleViewName(String singleArticleViewName) {
        this._singleArticleViewName = singleArticleViewName;
    }


    public String getMultipleArticlesViewName() {
        return _multipleArticlesViewName;
    }

    public void setMultipleArticlesViewName(String multipleArticlesViewName) {
        _multipleArticlesViewName = multipleArticlesViewName;
    }

    public IArticleDao getArticleDao() {
        return _articleDao;
    }

    public void setArticleDao(IArticleDao articleDao) {
        _articleDao = articleDao;
    }

}
