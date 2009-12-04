/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: ArticleLinkTagHandler.java,v 1.43 2009/12/04 22:27:11 chriskimm Exp $
 */
package gs.web.content;

import gs.data.cms.IPublicationDao;
import gs.data.content.Article;
import gs.data.content.cms.CmsFeature;
import gs.data.content.cms.CmsContent;
import gs.data.content.cms.CmsConstants;
import gs.data.state.State;
import gs.data.util.CmsUtil;
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
 * @author David Lee <mailto:dlee@greatschools.org>
 * @author Andrew Peterson <mailto:apeterson@greatschools.org>
 */
public class ArticleLinkTagHandler extends BaseTagHandler {

    private static UrlUtil _urlUtil = new UrlUtil();

    /**
     * CSS class to apply to anchor.
     */
    private String _styleClass;

    /**
     * Element id to apply to anchor.
     */
    private String _styleId;

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

    /**
     * Specify an anchor to point to
     */
    private String _anchorName;
    private static IPublicationDao _publicationDao;

    protected IPublicationDao getPublicationDao() {
        if (_publicationDao == null) {
            _publicationDao = (IPublicationDao) getApplicationContext().getBean("publicationDao");
        }
        return _publicationDao;
    }

    public static class LinkableContent {
        boolean isNew;
        boolean isSpanish;
        String title;
        String link;

        public LinkableContent(Article article) {
            isNew = article.isNew();
            isSpanish = article.isSpanish();
            title = article.getTitle();
        }

        public LinkableContent(CmsContent content) {
            isNew = false;
            isSpanish = "ES".equals(content.getLanguage());
            title = content.getLinkText();
            link = new UrlBuilder(content.getContentKey(), content.getFullUri()).toString();
        }
    }

    public void doTag() throws IOException {
        SessionContext sc = getSessionContext();
        State s = sc.getStateOrDefault();

        LinkableContent linkableContent = getLinkableContent();

        if (linkableContent == null) {
            return;
        }

        StringBuffer b = new StringBuffer();

        if (StringUtils.isNotEmpty(_wrappingElement)) {
            b.append("<").append(_wrappingElement).append(">");
        }

        if (_flaggedIfNew && linkableContent.isNew) {
            PageContext pageContext = (PageContext) getJspContext().findAttribute(PageContext.PAGECONTEXT);
            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

            String img = linkableContent.isSpanish ? "/res/img/content/nuevo.gif" : "/res/img/content/icon_newarticle.gif";
            img = _urlUtil.buildUrl(img, request);
            b.append("<img src=\"").append(img).append("\" alt=\"new\" class=\"newarticle\"/>&#160;");
        }

        b.append("<a href=\"");

        b.append(linkableContent.link);

        if (StringUtils.isNotEmpty(_anchorName)) {
            b.append('#').append(_anchorName);
        }

        b.append("\"");
        if (StringUtils.isNotEmpty(_target)) {
            b.append(" target=\"")
                    .append(_target)
                    .append("\"");
        }
        if (StringUtils.isNotEmpty(_styleClass)) {
            b.append(" class=\"").append(_styleClass).append("\"");
        }
        if (StringUtils.isNotEmpty(_styleId)) {
            b.append(" id=\"").append(_styleId).append("\"");
        }

        b.append(">");


        getJspContext().getOut().print(b);

        // Output the body, if any. Otherwise, output the article title.
        JspFragment jspBody = getJspBody();
        if (jspBody != null && StringUtils.isNotEmpty(jspBody.toString())) {
            try {
                jspBody.invoke(getJspContext().getOut());
            } catch (JspException e) {
                getJspContext().getOut().print(formatArticleTitle(linkableContent, s));
            }
        } else {
            getJspContext().getOut().print(formatArticleTitle(linkableContent, s));
        }

        getJspContext().getOut().print("</a>");

        if (StringUtils.isNotEmpty(_wrappingElement)) {
            getJspContext().getOut().print("</" + _wrappingElement + ">");
        }

    }

    protected LinkableContent getLinkableContent() {
        LinkableContent linkableContent = null;

        if (CmsUtil.isCmsEnabled() && !CmsConstants.isArticleServedByLegacyCms(_articleId)) {
            CmsFeature content = getPublicationDao().populateByLegacyId(Long.valueOf(_articleId), new CmsFeature());

            if (content != null) {
                linkableContent = new LinkableContent(content);
            }
        }

        if (linkableContent == null) {
            Article article = getAndValidateArticle();

            if (article != null) {
                linkableContent = new LinkableContent(article);
                linkableContent.link = getUrlForArticle(article);
            }
        }

        return linkableContent;
    }

    public String getUrlForArticle(Article article) {
        return new UrlBuilder(article.getId(), _featured).toString();
    }

    /**
     * Do a substitution on $LONGSTATE with the State's name, and escape ampersands.
     *
     * @param linkableContent Article to get title from
     * @param s Current State
     * @return Formatted article title
     */
    protected String formatArticleTitle(LinkableContent linkableContent, State s) {
        String title = linkableContent.title.replaceAll("\\$LONGSTATE", s.getLongName());

        // match ampersands and entities. second capture group holds entity content, if any
        return HtmlUtil.escapeAmpersands(title);
    }

    /**
     * Returns article if it is valid for this tag, null otherwise.
     *
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

    public Article getArticle() {
        return _article;
    }

    public void setArticle(Article article) {
        _article = article;
        if(article !=null){
            setArticleId(article.getId());
        }
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

    public String getAnchorName() {
        return _anchorName;
    }

    public void setAnchorName(String _anchorName) {
        this._anchorName = _anchorName;
    }

    public String getStyleId() {
        return _styleId;
    }

    public void setStyleId(String styleId) {
        _styleId = styleId;
    }
}