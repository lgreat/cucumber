/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.jsp.link;

import gs.web.content.ArticleLinkTagHandler;
import gs.data.content.Article;

import org.apache.commons.lang.StringUtils;

/**
 * Provides a link to the specified article's forum.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ArticleForumLinkTagHandler extends ArticleLinkTagHandler {

    protected Article getAndValidateArticle() {
        Article article = super.getAndValidateArticle();
        if (article == null) {
            return null;
        } else if (StringUtils.isEmpty(article.getForumUrl())) {
            _log.warn("Missing forum_url for article with id " + article.getId());
            return null;
        }

        return article;
    }

    public String getHref(Article article) {
        return article.getForumUrl().replaceAll("&", "&amp;");
    }
}
