package gs.web.community;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ReportContentCommand {
    public enum ReportType {reply, discussion}
    private ReportType _type;
    private long _contentId;
    private String _reason;
    private int _reporterId;
    private String _redirect;

    public ReportType getType() {
        return _type;
    }

    public void setType(ReportType type) {
        _type = type;
    }

    public long getContentId() {
        return _contentId;
    }

    public void setContentId(long contentId) {
        _contentId = contentId;
    }

    public String getReason() {
        return _reason;
    }

    public void setReason(String reason) {
        _reason = reason;
    }

    public int getReporterId() {
        return _reporterId;
    }

    public void setReporterId(int reporterId) {
        _reporterId = reporterId;
    }

    public String getRedirect() {
        return _redirect;
    }

    public void setRedirect(String redirect) {
        _redirect = redirect;
    }
}
