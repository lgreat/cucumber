/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: FeaturedArticlesController.java,v 1.26 2007/01/02 20:09:17 cpickslay Exp $
 */
package gs.web.content;

import gs.data.content.Article;
import gs.data.content.IArticleDao;
import gs.data.state.State;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.Anchor;
import gs.web.util.list.AnchorListModel;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Extracts one or more featured articles, based on
 * parameters passed in, and forwards them to the view.
 * There are two unique views supported:
 * <li>single article</li>
 * <li>multiple article</li>
 * The single article view receives the article in the "article" model property.
 * The multiple article view receives the UnorderedList model.
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
     * The "article position" string. Corresonds with values in the
     * FEATURED column of the database and the article editor's constants.
     */
    public static final String PARAM_POSITION = "position";
    /**
     * Index into the sub feature position. The default is 0.
     */
    public static final String PARAM_SUB_POSITION = "sub";
    /**
     * Number of articles to display. The default is 1. If
     * fewer articles are available, only those articles are displayed.
     */
    private static final String PARAM_COUNT = "count";

    /**
     * A string to be displayed as a heading above the articles.
     */
    public static final String PARAM_HEADING = "heading";
    private static final String PARAM_VALUE_NO_HEADING = "none";

    /*
        Model property names.
    */
    // Single article display
    public static final String MODEL_ARTICLE = "article";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {


        String posStr = request.getParameter(PARAM_POSITION);
        if (StringUtils.isEmpty(posStr)) {
            posStr = IArticleDao.HOT_TOPIC; // bad default
            _log.warn("Default position being used. Please fix.");
        }

        String countStr = request.getParameter(PARAM_COUNT);
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
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);

        int subPosition = 0;
        if (StringUtils.isNumeric(request.getParameter(PARAM_SUB_POSITION))) {
            subPosition = Integer.parseInt(request.getParameter(PARAM_SUB_POSITION));
        }

        Article article = _articleDao.getFeaturedArticle(sessionContext.getStateOrDefault(), posStr, subPosition);

        if (article == null) {
            return null;
        }

        // Allow param override
        final String heading = calcHeading(request, posStr);

        Map model = new HashMap(2);
        model.put("article", article);
        model.put(PARAM_HEADING, heading);

        return new ModelAndView(_singleArticleViewName, model);
    }

    private ModelAndView handleMultipleArticles(HttpServletRequest request, String posStr, int count) {

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        final State state = sessionContext.getStateOrDefault();

        List items = new ArrayList(count);
        Set articles = new HashSet(count);
        for (int i = 0; i < count; i++) {
            try {
                Article article = _articleDao.getFeaturedArticle(state, posStr, i);

                if (article != null &&
                        !articles.contains(article)) {
                    UrlBuilder builder = new UrlBuilder(article, state, false);
                    String articleLink = builder.toString();
                    final Anchor anchor;
                    if (article.isNew()) {
                        String imageUrl = _urlUtil.buildUrl("/res/img/content/icon_newarticle.gif", request);
                        anchor = new Anchor(articleLink, article.getTitle(), "new", imageUrl);
                    } else {
                        anchor = new Anchor(articleLink, article.getTitle());
                    }
                    items.add(anchor);
                    articles.add(article);
                }
            } catch (IllegalArgumentException e) {
                _log.warn("Problem finding a featured article.", e);
            }
        }

        if (StringUtils.equals(IArticleDao.FOCUS_ON_CHOICE, posStr)) {
            Anchor anchor = new Anchor(_urlUtil.buildUrl("/content/schoolChoiceCenter.page?state=$STATE", request),
                    "Browse all school choice resources", "viewall");
            items.add(anchor);
        } else if (StringUtils.equals(IArticleDao.HOT_TOPIC, posStr)) {
            /*Anchor anchor = new Anchor(_urlUtil.buildUrl("vpage:content.seasonal", request),
                    "Browse the latest topics", "viewall");
            items.add(anchor);*/
        }

        UrlBuilder builder = new UrlBuilder(UrlBuilder.ARTICLE_LIBRARY, state);
        Anchor anchor = builder.asAnchor(request, "Browse all topics", "viewall");
        items.add(anchor);

        final String heading = calcHeading(request, posStr);

        Map model = new HashMap(2);
        model.put(AnchorListModel.RESULTS, items);
        model.put(AnchorListModel.HEADING, heading);

        return new ModelAndView(_multipleArticlesViewName, model);
    }

    private String calcHeading(HttpServletRequest request, String posStr) {
        // Allow param override
        final String paramHeading = request.getParameter(PARAM_HEADING);
        String heading;
        heading = "Today&#8217s Feature";
        if (StringUtils.equals(paramHeading, PARAM_VALUE_NO_HEADING)) {
            heading = "";
        } else if (StringUtils.isNotEmpty(paramHeading)) {
            heading = paramHeading;
        } else {
            if (StringUtils.equals(posStr, IArticleDao.FOCUS_ON_CHOICE)) {
                heading = "School Choosers Guide";
            } else if (StringUtils.equals(posStr, IArticleDao.HOT_TOPIC)) {
                heading = ""; // no heading
            } else {
                heading = "Featured Topics"; // "Today&#8217s Feature";
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
