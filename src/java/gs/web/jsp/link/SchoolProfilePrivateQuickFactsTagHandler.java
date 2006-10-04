package gs.web.jsp.link;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */

import gs.web.util.UrlBuilder;
import gs.data.school.School;
import gs.data.school.SchoolType;

/**
 * School Profile Census
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class SchoolProfilePrivateQuickFactsTagHandler extends BaseSchoolProfileTagHandler{
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE_PRIVATE_QUICK_FACTS);
    }
}
