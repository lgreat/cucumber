package gs.web.community;

public class RaiseYourHandCommand {
    private Integer _discussionId;
    private Long _topicCenterId;
    private String _action;

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

    public String getAction() {
        return _action;
    }

    public void setAction(String action) {
        _action = action;
    }
}
