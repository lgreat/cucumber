/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: ContentSearchTagHandler.java,v 1.5 2011/10/08 03:35:24 ssprouse Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.community.User;

/**
 * Generates link to a content search results page.
 *
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class ContentSearchTagHandler extends LinkTagHandler {
    private String _query;
    private Integer _page;
    private String _type;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.CONTENT_SEARCH, getQuery(), getPage(), getType());
    }

    public String getQuery() {
        return _query;
    }

    public void setQuery(String query) {
        _query = query;
    }

    public Integer getPage() {
        return _page;
    }

    public void setPage(Integer page) {
        _page = page;
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }
}