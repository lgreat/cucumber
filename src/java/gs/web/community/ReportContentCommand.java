package gs.web.community;

import gs.data.community.ReportedEntity;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class ReportContentCommand {
    private ReportedEntity.ReportedEntityType _type;
    private int _contentId;
    private String _reason;
    private int _reporterId;

    public ReportedEntity.ReportedEntityType getType() {
        return _type;
    }

    public void setType(ReportedEntity.ReportedEntityType type) {
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
