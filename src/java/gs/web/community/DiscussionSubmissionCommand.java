package gs.web.community;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class DiscussionSubmissionCommand {
    // Fields for DiscussionReply
    private Integer _discussionId;
    // Fields for Discussion
    private Integer _discussionBoardId;
    private String _title;
    // Fields for both Discussion and DiscussionReply
    private String _body;
    private String _redirect;

    public Integer getDiscussionId() {
        return _discussionId;
    }

    public void setDiscussionId(Integer discussionId) {
        _discussionId = discussionId;
    }

    public Integer getDiscussionBoardId() {
        return _discussionBoardId;
    }

    public void setDiscussionBoardId(Integer discussionBoardId) {
        _discussionBoardId = discussionBoardId;
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
    }

    public String getBody() {
        return _body;
    }

    public void setBody(String body) {
        _body = body;
    }

    public String getRedirect() {
        return _redirect;
    }

    public void setRedirect(String redirect) {
        _redirect = redirect;
    }
}
