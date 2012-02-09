package gs.web.jsp.link.school;

import gs.data.state.State;
import gs.web.jsp.link.LinkTagHandler;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * @author aroy@greatschools.org
 */
public class EspDashboardTagHandler extends BaseSchoolTagHandler {
    @Override
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
        
        if (getSchool() != null) {
            urlBuilder.addParameter("schoolId", String.valueOf(getSchool().getId()));
            urlBuilder.addParameter("state", getSchool().getDatabaseState().getAbbreviationLowerCase());
        }
        
        return urlBuilder;
    }
}
