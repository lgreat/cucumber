/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: FeaturedArticlesController.java,v 1.9 2005/12/01 01:56:24 apeterson Exp $
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
import java.util.*;

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
    /**
     * Number of articles to display. The default is 1.
     */
    private static final String COUNT_PARAM = "count";

    public static final String HEAD_PARAM = "heading";

    /*
        Model property names.
    */
    // Single article display
    public static final String MODEL_ARTICLE = "article";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {


        String posStr = request.getParameter(POSITION_PARAM);
        if (StringUtils.isEmpty(posStr)) {
            posStr = IArticleDao.HOT_TOPIC; // bad default
            _log.warn("Default position being used. Please fix.");
        }

        String countStr = request.getParameter(COUNT_PARAM);
        int count = 1;
        if (StringUtils.isNumeric(countStr)) {
            try {
                count = Integer.valueOf(countStr).intValue();
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (count > 1) {
            return handleMultipleArticles(request, posStr, count);
        } else {
            return handleSingleArticle(request, posStr);
        }
    }

    private ModelAndView handleSingleArticle(HttpServletRequest request, String posStr) {
        ISessionFacade sessionFacade = SessionFacade.getInstance(request);
        Article article = _articleDao.getFeaturedArticle(sessionFacade.getStateOrDefault(), posStr, 0);

        // Allow param override
        final String heading = calcHeading(request, posStr);

        Map model = new HashMap(2);
        model.put("article", article);
        model.put(HEAD_PARAM, heading);

        return new ModelAndView(_singleArticleViewName, model);
    }

    private ModelAndView handleMultipleArticles(HttpServletRequest request, String posStr, int count) {

        ISessionFacade sessionFacade = SessionFacade.getInstance(request);

        List items = new ArrayList(count);
        Set articles = new HashSet(count);
        for (int i = 0; i < count; i++) {
            Article article = _articleDao.getFeaturedArticle(sessionFacade.getStateOrDefault(), posStr, i);

            if (article != null &&
                    !articles.contains(article)) {
                Anchor anchor = new Anchor(_urlUtil.getArticleLink(sessionFacade.getStateOrDefault(), article, false),
                        article.getTitle());
                items.add(anchor);
                articles.add(article);
            }
        }

        Anchor anchor = new Anchor("#",
                "View all articles", "viewall");
        items.add(anchor);


        final String heading = calcHeading(request, posStr);

        Map model = new HashMap(2);
        model.put("results", items);
        model.put("header", heading);

        return new ModelAndView(_multipleArticlesViewName, model);
    }

    private String calcHeading(HttpServletRequest request, String posStr) {
        // Allow param override
        final String paramHeading = request.getParameter(HEAD_PARAM);
        String heading;
        heading = "Today&#8217s Feature";
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
        return heading;
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
