/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ArticleLinkTagHandler.java,v 1.21 2006/06/26 21:26:00 apeterson Exp $
 */
package gs.web.content;

import gs.data.content.Article;
import gs.data.content.IArticleDao;
import gs.data.state.State;
import gs.web.ISessionContext;
import gs.web.jsp.BaseTagHandler;
import gs.web.util.UrlUtil;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import java.io.IOException;

/**
 * Write out an article link.
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 * @author Andrew Peterson <mailto:apeterson@greatschools.net>
 */
public class ArticleLinkTagHandler extends BaseTagHandler {

    private static UrlUtil _urlUtil = new UrlUtil();

    /**
     * The article id to link to. Set either this or the article.
     */
    private Integer _articleId;

    /**
     * The article to link to.
     */
    private Article _article;

    /**
     * The target window name, if any.
     */
    private String _target;

    /**
     * Link to the "featured" version of the article, instead of the regular one.
     */
    private boolean _featured;

    /**
     * Draw a visual indication that the article is new.
     */
    private boolean _flaggedIfNew;

    /**
     * Specify an element to wrap the article in, if the article appears.
     */
    private String _wrappingElement;


    public void doTag() throws IOException {

        ISessionContext sc = getSessionContext();
        State s = sc.getStateOrDefault();

        Article article = _article;

        // If no article object, use the Dao to retrieve the article based on the article id.
        if (article == null) {
            IArticleDao articleDao = getArticleDao();
            article = articleDao.getArticleFromId(_articleId);
            if (article == null) {
                _log.warn("Cannot find article with id " + _articleId);
            }
        }

        if (article == null) {
            return; // NOTE: Early exit!
        }

        if (!article.isArticleAvailableInState(s)) {
            return; // NOTE: Early exit!
        }

        StringBuffer b = new StringBuffer();

        if (StringUtils.isNotEmpty(_wrappingElement)) {
            b.append("<" + _wrappingElement + ">");
        }

        PageContext pageContext = (PageContext) getJspContext().findAttribute(PageContext.PAGECONTEXT);
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        if (_flaggedIfNew && article.isNew()) {
            String img = article.isSpanish() ? "/res/img/content/nuevo.jpg" : "/res/img/content/icon_newarticle.gif";
            img = _urlUtil.buildUrl(img, request);
            b.append("<img src=\"" + img + "\" alt=\"new\" class=\"newarticle\">&nbsp;");
        }

        b.append("<a href=\"");

        UrlBuilder builder = new UrlBuilder(article, s, _featured);
        String link = builder.toString();
        b.append(link);

        b.append("\" ");
        if (StringUtils.isNotEmpty(_target)) {
            b.append(" target=\"")
                    .append(_target)
                    .append("\"");
        }
        b.append(">");


        getJspContext().getOut().print(b);

        // Output the body, if any. Otherwise, output the article title.
        JspFragment jspBody = getJspBody();
        if (jspBody != null && StringUtils.isNotEmpty(jspBody.toString())) {
            try {
                jspBody.invoke(getJspContext().getOut());
            } catch (JspException e) {
                String title = article.getTitle().replaceAll("\\$LONGSTATE", s.getLongName());
                getJspContext().getOut().print(title);
            }
        } else {
            String title = article.getTitle().replaceAll("\\$LONGSTATE", s.getLongName());
            getJspContext().getOut().print(title);
        }

        getJspContext().getOut().print("</a>");

        if (StringUtils.isNotEmpty(_wrappingElement)) {
            getJspContext().getOut().print("</" + _wrappingElement + ">");
        }
    }

    public Article getArticle() {
        return _article;
    }

    public void setArticle(Article article) {
        _article = article;
    }

    public String getTarget() {
        return _target;
    }

    public void setTarget(String target) {
        _target = target;
    }

    public boolean isFeatured() {
        return _featured;
    }

    public void setFeatured(boolean featured) {
        _featured = featured;
    }

    public boolean isFlaggedIfNew() {
        return _flaggedIfNew;
    }

    public void setFlaggedIfNew(boolean flaggedIfNew) {
        _flaggedIfNew = flaggedIfNew;
    }

    public String getWrappingElement() {
        return _wrappingElement;
    }

    public void setWrappingElement(String wrappingElement) {
        _wrappingElement = wrappingElement;
    }

    public Integer getArticleId() {
        return _articleId;
    }

    public void setArticleId(Integer articleId) {
        _articleId = articleId;
    }
}
