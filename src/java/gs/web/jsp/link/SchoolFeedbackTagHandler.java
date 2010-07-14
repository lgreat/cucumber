package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolFeedbackTagHandler extends ContactUsTagHandler {
    public SchoolFeedbackTagHandler() {
        super();
        setFeedbackType("incorrectSchoolDistrictInfo_incorrectSchool");
    }
}
