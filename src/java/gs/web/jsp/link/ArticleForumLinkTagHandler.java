/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.jsp.link;

import gs.web.jsp.BaseTagHandler;
import gs.data.content.Article;
import gs.data.content.IArticleDao;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

/**
 * Provides a link to the specified article's forum.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ArticleForumLinkTagHandler extends BaseTagHandler {
    private Integer _articleId;
    private String _styleClass;
    private String _target;

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

    public String getTarget() {
        return _target;
    }

    public void setTarget(String target) {
        _target = target;
    }

    public void doTag() throws IOException {
        if (_articleId == null) {
            _log.warn("Called with no article id");
            return; // early exit
        }
        Article article = getArticleDao().getArticleFromId(_articleId);

        if (article == null || StringUtils.isEmpty(article.getForumUrl())) {
            _log.warn("Cannot find article with id=" + _articleId);
            return; // early exit
        }

        JspWriter out = getJspContext().getOut();
        out.print("<a");

        if (StringUtils.isNotEmpty(_styleClass)) {
            out.print(" class=\"" + _styleClass + "\"");
        }

        if (StringUtils.isNotEmpty(_target)) {
            out.print(" target=\"" + _target + "\"");
        }

        String href=article.getForumUrl();

        out.print(" href=\"");
        out.print(href);
        out.print("\"");
        out.print(">");

        JspFragment jspBody = getJspBody();
        if (jspBody != null && StringUtils.isNotEmpty(jspBody.toString())) {
            try {
                jspBody.invoke(out);
            } catch (JspException e) {
                _log.error(e);
            }
        } else {
            out.print(article.getTitle());
        }

        out.print("</a>");
    }

    /** For testing class */
    protected IArticleDao getArticleDao() {
        return super.getArticleDao();
    }
}
