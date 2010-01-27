package gs.web.community;

import gs.data.community.ReportedEntity;
import gs.data.community.User;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public interface IReportContentService {
    public void reportContent(User reporter, 
                              User reportee,
                              HttpServletRequest request,
                              int contentId,
                              ReportedEntity.ReportedEntityType type,
                              String reason);

    public String getModerationEmail();

    public void setModerationEmail(String moderationEmail);
}
