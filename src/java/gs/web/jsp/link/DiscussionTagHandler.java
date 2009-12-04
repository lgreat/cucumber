/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: DiscussionTagHandler.java,v 1.9 2009/12/04 22:27:02 chriskimm Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.community.Discussion;

/**
 * Generates link to a discussion detail page.
 *
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class DiscussionTagHandler extends LinkTagHandler {
    private Discussion _discussion;
    private Integer _discussionId;
    private String _fullUri;
    private Integer _discussionReplyId; // optionally specify the discussion reply to link to

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder;
        if (_discussion != null) {
            builder = new UrlBuilder(UrlBuilder.COMMUNITY_DISCUSSION, _fullUri, Long.valueOf(_discussion.getId()));
        } else if (_discussionId != null) {
            builder = new UrlBuilder(UrlBuilder.COMMUNITY_DISCUSSION, _fullUri, Long.valueOf(_discussionId));
        } else {
            throw new RuntimeException("DiscussionTagHandler requires a discussion or (a discussion id and fullUri)");
        }

        if (_discussionReplyId != null) {
            builder.setParameter("discussionReplyId", String.valueOf(_discussionReplyId));
            setAnchor("#reply_" + _discussionReplyId);
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

    public String getFullUri() {
        return _fullUri;
    }

    public void setFullUri(String fullUri) {
        _fullUri = fullUri;
    }

    public Integer getDiscussionReplyId() {
        return _discussionReplyId;
    }

    public void setDiscussionReplyId(Integer discussionReplyId) {
        if (discussionReplyId != null && discussionReplyId > 0) {
            _discussionReplyId = discussionReplyId;
        } else {
            _discussionReplyId = null;
        }
    }
}