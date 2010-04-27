package gs.web.community;

import gs.data.community.Discussion;
import gs.data.community.DiscussionReply;
import gs.data.community.User;
import gs.data.content.cms.CmsDiscussionBoard;

import java.util.List;
import java.util.Date;
import java.util.Collections;

/**
 * Read-only facade over a Discussion and a list of replies.
 * 
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class DiscussionFacade {
    private Discussion _discussion;
    private List<DiscussionReply> _replies;
    private int _totalReplies = 0;
    private Date _mostRecentReplyDateCreated;
    private CmsDiscussionBoard _discussionBoard;

    public DiscussionFacade(Discussion parent, List<DiscussionReply> replies) {
        _discussion = parent;
        _replies = replies;
        if (replies != null) {
            Collections.reverse(_replies); // GS-9080
        }
    }

    public String getTitle() {
        return _discussion.getTitle();
    }

    public String getBody() {
        return _discussion.getBody();
    }

    public Date getDateCreated() {
        return _discussion.getDateCreated();
    }

    public User getUser() {
        return _discussion.getUser();
    }

    public String getFullUri() {
        return _discussion.getDiscussionBoard().getFullUri();
    }

    public List<DiscussionReply> getReplies() {
        return _replies;
    }

    public int getTotalReplies() {
        return _totalReplies;
    }

    public void setTotalReplies(int totalReplies) {
        _totalReplies = totalReplies;
    }

    public Integer getId() {
        return _discussion.getId();
    }

    public boolean isAnonymous() {
        return _discussion.isAnonymous();
    }

    public boolean isActive() {
        return _discussion.isActive();
    }

    public Date getMostRecentReplyDateCreated() {
        return _mostRecentReplyDateCreated;
    }

    public void setMostRecentReplyDateCreated(Date mostRecentReplyDateCreated) {
        _mostRecentReplyDateCreated = mostRecentReplyDateCreated;
    }

    public CmsDiscussionBoard getDiscussionBoard() {
        return _discussionBoard;
    }

    public void setDiscussionBoard(CmsDiscussionBoard discussionBoard) {
        _discussionBoard = discussionBoard;
    }
}
