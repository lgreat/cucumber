/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ArticleLinkTagHandler.java,v 1.6 2005/11/02 01:10:34 apeterson Exp $
 */
package gs.web.content;

import gs.data.content.Article;
import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.jsp.BaseTagHandler;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.JspFragment;
import java.io.IOException;

/**
 * Write out an article link
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class ArticleLinkTagHandler extends BaseTagHandler {

    private Article _article;
    private String _windowName = "";
    private UrlUtil _urlUtil = new UrlUtil();


    public void doTag() throws IOException {

        JspContext jspContext = getJspContext();
        StringBuffer articleLink = new StringBuffer();

        if (jspContext != null) {
            ISessionFacade sc = getSessionContext();

            if (sc != null) {
                State s = sc.getStateOrDefault();

                if (_article.isNew()) {
                    String img = _article.isSpanish() ? "/res/img/content/nuevo.jpg" : "/res/img/content/icon_newarticle.gif";
                    img = _urlUtil.buildHref(img, false, null);
                    articleLink.append("<img src=\"" + img + "\" alt=\"new\" class=\"newarticle\">&nbsp;");
                }

                if (s.isSubscriptionState() && _article.isInsider()) {
                    articleLink.append("<img src=\"/res/img/st_icon.gif\" border=\"0\" /><a href=\"/cgi-bin/showpartarticle/");
                } else {
                    articleLink.append("<a href=\"/cgi-bin/showarticle/");
                }


                String title = _article.getTitle();
                title = title.replaceAll("\\$LONGSTATE", s.getLongName());

                JspFragment jspBody = getJspBody();

                articleLink.append(s.getAbbreviationLowerCase()).append("/")
                        .append(_article.getId().toString())
                        .append("\" ");
                if (StringUtils.isNotEmpty(_windowName)) {
                    articleLink.append(" target=\"")
                            .append(_windowName)
                            .append("\"");
                }
                articleLink.append(">")
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
