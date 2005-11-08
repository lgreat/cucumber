/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ArticleLinkTagHandler.java,v 1.7 2005/11/08 19:21:04 apeterson Exp $
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

    private Article _article;
    private String _windowName;
    private UrlUtil _urlUtil = new UrlUtil();
    private boolean _featured;


    public void doTag() throws IOException {

        StringBuffer b = new StringBuffer();

        ISessionFacade sc = getSessionContext();

        State s = sc.getStateOrDefault();

        if (_article.isNew()) {
            String img = _article.isSpanish() ? "/res/img/content/nuevo.jpg" : "/res/img/content/icon_newarticle.gif";
            img = _urlUtil.buildHref(img, false, null);
            b.append("<img src=\"" + img + "\" alt=\"new\" class=\"newarticle\">&nbsp;");
        }

        if (s.isSubscriptionState() && _article.isInsider()) {
            b.append("<img src=\"/res/img/st_icon.gif\" border=\"0\" />");
        }

        b.append("<a href=\"");

        // Calculate page to use
        String page;
        if (s.isSubscriptionState() && _article.isInsider()) {
            page = "showpartarticle";
        } else {
            if (_featured) {
                page = "showarticlefeature";
            } else {
                page = "showarticle";
            }
        }

        // Calculate link
        String link = "/cgi-bin/" +
                page +
                "/" +
                s.getAbbreviationLowerCase() +
                "/" +
                _article.getId();
        link = _urlUtil.buildHref(link, false, null);
        b.append(link);

        b.append("\" ");
        if (StringUtils.isNotEmpty(_windowName)) {
            b.append(" target=\"")
                    .append(_windowName)
                    .append("\"");
        }
        b.append(">");


        getJspContext().getOut().print(b);


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

    public String getWindowName() {
        return _windowName;
    }

    public void setWindowName(String windowName) {
        _windowName = windowName;
    }

    public boolean isFeatured() {
        return _featured;
    }

    public void setFeatured(boolean featured) {
        _featured = featured;
    }
}
