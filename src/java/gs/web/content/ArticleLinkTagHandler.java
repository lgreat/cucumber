/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ArticleLinkTagHandler.java,v 1.4 2005/09/28 23:53:10 wbeck Exp $
 */
package gs.web.content;

import gs.data.content.Article;
import gs.data.state.State;
import gs.web.SessionContext;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * Write out an article link
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class ArticleLinkTagHandler extends SimpleTagSupport {

    private Article _article;
    private String _windowName = "";

    public void doTag() throws IOException {

        JspContext jspContext = getJspContext();
        StringBuffer articleLink = new StringBuffer();

        if (jspContext != null) {
            SessionContext sc = (SessionContext) jspContext.getAttribute(SessionContext.SESSION_ATTRIBUTE_NAME, PageContext.SESSION_SCOPE);

            if (sc != null) {
                State s = sc.getStateOrDefault();

                if (_article.isSpanish() && _article.isNew()) {
                    articleLink.append("<img src=\"/res/img/content/nuevo.jpg\">&nbsp;");
                }
                else if (_article.isNew()) {
                    articleLink.append("<img src=\"/res/img/content/new.jpg\">&nbsp;");

                }

                if (s.isSubscriptionState() && _article.isInsider()) {
                    articleLink.append("<img src=\"/res/img/st_icon.gif\" border=\"0\" /><a href=\"/cgi-bin/showpartarticle/");
                } else {
                    articleLink.append("<a href=\"/cgi-bin/showarticle/");
                }



                String title = _article.getTitle();
                title = title.replaceAll("\\$LONGSTATE",s.getLongName());

                articleLink.append(s.getAbbreviationLowerCase()).append("/")
                            .append(_article.getId().toString())
                            .append("\" target=\"")
                            .append(_windowName)
                            .append("\">")
                            .append(title).append("</a>").toString();

            }
        }

        JspWriter out = getJspContext().getOut();
        out.print(articleLink);
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

}
