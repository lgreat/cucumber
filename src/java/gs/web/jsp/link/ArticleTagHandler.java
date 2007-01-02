/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: ArticleTagHandler.java,v 1.6 2007/01/02 20:09:16 cpickslay Exp $
 */

package gs.web.jsp.link;

import gs.data.content.Article;
import gs.data.content.IArticleDao;
import gs.web.util.context.SessionContext;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;

/**
 * Generates link to an article.
 *
 * @author Andrew Peterson <mailto:apeterson@greatschools.net>
 */
public class ArticleTagHandler extends LinkTagHandler {

    private Article _article;
    private Integer _articleId;
    private boolean _featured;


    protected UrlBuilder createUrlBuilder() {
        Article a= getArticle();
        UrlBuilder builder = new UrlBuilder(a, getState(), _featured);
        return builder;
    }

    public Article getArticle() {
        if (_article != null) {
            return _article;
        } else {
            SessionContext context = getSessionContext();
            IArticleDao articleDao = (IArticleDao) context.getApplicationContext().getBean(IArticleDao.BEAN_ID);
            Article a = articleDao.getArticleFromId(_articleId);
            return a;
        }
    }

    public void setArticle(Article article) {
        _article = article;
    }

    public void setArticleId(Integer articleId) {
        _articleId = articleId;
    }

    public void setFeatured(Boolean featured) {
        _featured = featured.booleanValue();
    }

}
