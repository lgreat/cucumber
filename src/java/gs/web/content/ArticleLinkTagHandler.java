/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ArticleLinkTagHandler.java,v 1.11 2005/11/29 23:38:25 apeterson Exp $
 */
package gs.web.content;

import gs.data.content.Article;
import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.jsp.BaseTagHandler;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.JspException;
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


    public void doTag() throws IOException {

        StringBuffer b = new StringBuffer();

        ISessionFacade sc = getSessionContext();

        State s = sc.getStateOrDefault();

        if (_flaggedIfNew && _article.isNew()) {
            String img = _article.isSpanish() ? "/res/img/content/nuevo.jpg" : "/res/img/content/icon_newarticle.gif";
            img = _urlUtil.buildHref(img, false, null, null);
            b.append("<img src=\"" + img + "\" alt=\"new\" class=\"newarticle\">&nbsp;");
        }

        if (s.isSubscriptionState() && _article.isInsider()) {
            b.append("<img src=\"/res/img/st_icon.gif\" border=\"0\" />");
        }

        b.append("<a href=\"");

        String link = _urlUtil.getArticleLink(s, _article, _featured);
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
                String title = _article.getTitle().replaceAll("\\$LONGSTATE", s.getLongName());
                getJspContext().getOut().print(title);
            }
        } else {
            String title = _article.getTitle().replaceAll("\\$LONGSTATE", s.getLongName());
            getJspContext().getOut().print(title);
        }

        getJspContext().getOut().print("</a>");
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
}
