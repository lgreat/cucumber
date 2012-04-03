package gs.web.community;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class DeactivateContentCommand {
    private long _contentId;
    public enum ContentType {reply, discussion, schoolReview, schoolMedia}
    private ContentType _contentType;
    private boolean _reactivate;

    public long getContentId() {
        return _contentId;
    }

    public void setContentId(long contentId) {
        _contentId = contentId;
    }

    public ContentType getContentType() {
        return _contentType;
    }

    public void setContentType(ContentType contentType) {
        _contentType = contentType;
    }

    public boolean isReactivate() {
        return _reactivate;
    }

    public void setReactivate(boolean reactivate) {
        _reactivate = reactivate;
    }
}
