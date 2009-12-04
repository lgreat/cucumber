/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: ArticleTagHandler.java,v 1.9 2009/12/04 20:54:11 npatury Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * Generates link to an article.
 *
 * @author Andrew Peterson <mailto:apeterson@greatschools.net>
 */
public class ArticleTagHandler extends LinkTagHandler {

    private Integer _articleId;
    private boolean _featured;
    private String _campaignId;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(_articleId, _featured);
        if (StringUtils.isNotBlank(_campaignId)) {
            builder.addParameter("s_cid", _campaignId);
        }
        return builder;
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
