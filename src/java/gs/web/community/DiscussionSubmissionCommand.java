package gs.web.community;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class DiscussionSubmissionCommand {
    // Fields for DiscussionReply
    private Integer _discussionId;
    // Fields for Discussion
    private Long _topicCenterId;
    private Long _discussionBoardId;
    private String _title;
    // Fields for both Discussion and DiscussionReply
    private String _body;
    private String _redirect;
    private Integer _discussionReplyId;
    private String _type;

    public Integer getDiscussionId() {
        return _discussionId;
    }

    public void setDiscussionId(Integer discussionId) {
        _discussionId = discussionId;
    }

    public Long getTopicCenterId() {
        return _topicCenterId;
    }

    public void setTopicCenterId(Long topicCenterId) {
        _topicCenterId = topicCenterId;
    }

    public Long getDiscussionBoardId() {
        return _discussionBoardId;
    }

    public void setDiscussionBoardId(Long discussionBoardId) {
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

    public Integer getDiscussionReplyId() {
        return _discussionReplyId;
    }

    public void setDiscussionReplyId(Integer discussionReplyId) {
        _discussionReplyId = discussionReplyId;
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }
}
