/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ArticleLinkTagHandler.java,v 1.35 2007/05/02 22:08:55 cpickslay Exp $
 */
package gs.web.content;

import gs.data.content.Article;
import gs.data.state.State;
import gs.data.util.HtmlUtil;
import gs.web.jsp.BaseTagHandler;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
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
     * CSS class to apply to anchor.
     */
    private String _styleClass;

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

        SessionContext sc = getSessionContext();
        State s = sc.getStateOrDefault();

        Article article = getAndValidateArticle();

        if (article == null) {
            return; // NOTE: Early exit!
        }

        StringBuffer b = new StringBuffer();

        if (StringUtils.isNotEmpty(_wrappingElement)) {
            b.append("<").append(_wrappingElement).append(">");
        }

        if (_flaggedIfNew && article.isNew()) {
            PageContext pageContext = (PageContext) getJspContext().findAttribute(PageContext.PAGECONTEXT);
            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

            String img = article.isSpanish() ? "/res/img/content/nuevo.gif" : "/res/img/content/icon_newarticle.gif";
            img = _urlUtil.buildUrl(img, request);
            b.append("<img src=\"").append(img).append("\" alt=\"new\" class=\"newarticle\"/>&nbsp;");
        }

        b.append("<a href=\"");

        String link = getHref(article);
        b.append(link);

        b.append("\"");
        if (StringUtils.isNotEmpty(_target)) {
            b.append(" target=\"")
                    .append(_target)
                    .append("\"");
        }
        if (StringUtils.isNotEmpty(_styleClass)) {
            b.append(" class=\"").append(_styleClass).append("\"");
        }
        b.append(">");


        getJspContext().getOut().print(b);

        // Output the body, if any. Otherwise, output the article title.
        JspFragment jspBody = getJspBody();
        if (jspBody != null && StringUtils.isNotEmpty(jspBody.toString())) {
            try {
                jspBody.invoke(getJspContext().getOut());
            } catch (JspException e) {
                getJspContext().getOut().print(formatArticleTitle(article, s));
            }
        } else {
            getJspContext().getOut().print(formatArticleTitle(article, s));
        }

        getJspContext().getOut().print("</a>");

        if (StringUtils.isNotEmpty(_wrappingElement)) {
            getJspContext().getOut().print("</" + _wrappingElement + ">");
        }
    }

    /**
     * Do a substitution on $LONGSTATE with the State's name, and escape ampersands.
     * @param article Article to get title from
     * @param s Current State
     * @return Formatted article title
     */
    protected String formatArticleTitle(Article article, State s) {
        String title = article.getTitle().replaceAll("\\$LONGSTATE", s.getLongName());

        // match ampersands and entities. second capture group holds entity content, if any
        return HtmlUtil.escapeAmpersands(title);
    }

    /**
     * Returns article if it is valid for this tag, null otherwise.
     * @return valid article or null
     */
    protected Article getAndValidateArticle() {
        SessionContext sc = getSessionContext();
        State s = sc.getStateOrDefault();
        Article article = _article;

        // If no article object, use the Dao to retrieve the article based on the article id.
        if (article == null) {
            article = getArticleDao().getArticleFromId(_articleId);
        }

        if (article == null) {
            _log.warn("Cannot find article with id " + _articleId);
            return null;
        } else if (!article.isActive()) {
            _log.warn("Inactive article being called: " + article.getId());
            return null;
        } else if (!article.isArticleAvailableInState(s)) {
            _log.warn("Article not available in State " + s.getLongName() + ": " + article.getId());
            return null;
        }

        return article;
    }

    /**
     * returns a url to the article to be used as the href
     * @param article
     * @return url for href
     */
    protected String getHref(Article article) {
        SessionContext sc = getSessionContext();
        State s = sc.getStateOrDefault();
        UrlBuilder builder = new UrlBuilder(article, s, _featured);
        return builder.toString();
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

    public String getStyleClass() {
        return _styleClass;
    }

    public void setStyleClass(String styleClass) {
        _styleClass = styleClass;
    }
}
