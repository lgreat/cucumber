/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: DiscussionTagHandler.java,v 1.1 2009/09/29 16:17:20 droy Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.community.Discussion;
import org.apache.commons.lang.StringUtils;

/**
 * Generates link to a discussion.
 *
 * @author Andrew Peterson <mailto:apeterson@greatschools.net>
 */
public class DiscussionTagHandler extends LinkTagHandler {
    private Discussion _discussion;
    private Integer _discussionId;

    protected UrlBuilder createUrlBuilder() {
        int id;
        UrlBuilder builder = null;
        if (_discussion != null) {
            builder = new UrlBuilder(_discussion);
        } else if (_discussionId != null) {
            builder = new UrlBuilder(UrlBuilder.COMMUNITY_DISCUSSION);
            builder.setParameter("content", _discussionId.toString());
        } else {
            throw new RuntimeException("DiscussionTagHandler requires a discussion or a discussion id");
        }

        return builder;
    }

    public Discussion getDiscussion() {
        return _discussion;
    }

    public void setDiscussion(Discussion discussion) {
        _discussion = discussion;
    }

    public Integer getDiscussionId() {
        return _discussionId;
    }

    public void setDiscussionId(Integer discussionId) {
        _discussionId = discussionId;
    }
}