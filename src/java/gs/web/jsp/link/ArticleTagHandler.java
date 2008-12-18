/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: ArticleTagHandler.java,v 1.7 2008/12/18 17:36:54 aroy Exp $
 */

package gs.web.jsp.link;

import gs.data.content.Article;
import gs.data.content.IArticleDao;
import gs.web.util.context.SessionContext;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import org.apache.commons.lang.StringUtils;

/**
 * Generates link to an article.
 *
 * @author Andrew Peterson <mailto:apeterson@greatschools.net>
 */
public class ArticleTagHandler extends LinkTagHandler {

    private Article _article;
    private Integer _articleId;
    private boolean _featured;
    private String _campaignId;

    protected UrlBuilder createUrlBuilder() {
        Article a= getArticle();
        UrlBuilder builder = new UrlBuilder(a, getState(), _featured);
        if (StringUtils.isNotBlank(_campaignId)) {
            builder.addParameter("s_cid", _campaignId);
        }
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

    public void setCampaignId(String campaignId) {
        _campaignId = campaignId;
    }
}
