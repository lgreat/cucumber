package gs.web.community;

import gs.data.community.User;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public interface IReportContentService {
    public enum ReportType {reply, discussion, member};
    
    public void reportContent(User reporter, User reportee, HttpServletRequest request, int contentId, ReportContentService.ReportType type, String reason);

    public void reportContent(User reporter, User reportee, String urlToContent, ReportContentService.ReportType type, String reason);

    public String getModerationEmail();

    public void setModerationEmail(String moderationEmail);
}
