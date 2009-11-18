package gs.web.community;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ReportContentCommand {
    public enum ReportType {reply, discussion, member}
    private ReportType _type;
    private int _contentId;
    private String _reason;
    private int _reporterId;

    public ReportType getType() {
        return _type;
    }

    public void setType(ReportType type) {
        _type = type;
    }

    public int getContentId() {
        return _contentId;
    }

    public void setContentId(int contentId) {
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
}
