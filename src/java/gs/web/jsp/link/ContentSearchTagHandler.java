/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: ContentSearchTagHandler.java,v 1.2 2009/10/23 23:24:18 yfan Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.community.User;

/**
 * Generates link to a content search results page.
 *
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class ContentSearchTagHandler extends LinkTagHandler {
    private String _query;
    private Integer _page;
    private String _type;
    private String _sample;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.CONTENT_SEARCH, getQuery(), getPage(), getType(), getSample());
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

    public String getSample() {
        return _sample;
    }

    public void setSample(String sample) {
        _sample = sample;
    }
}