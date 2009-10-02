package gs.web.community;

import gs.data.community.Discussion;
import gs.data.community.DiscussionReply;
import gs.data.community.User;

import java.util.List;
import java.util.Date;

/**
 * Read-only facade over a Discussion and a list of replies.
 * 
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class DiscussionFacade {
    private Discussion _discussion;
    private List<DiscussionReply> _replies;
    
    public DiscussionFacade(Discussion parent, List<DiscussionReply> replies) {
        _discussion = parent;
        _replies = replies;
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

    public Integer getId() {
        return _discussion.getId();
    }
}
