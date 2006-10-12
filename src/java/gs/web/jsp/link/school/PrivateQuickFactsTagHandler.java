package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * School Profile Private School Quick Facts page
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class PrivateQuickFactsTagHandler extends BaseSchoolTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE_PRIVATE_QUICK_FACTS);
    }
}
