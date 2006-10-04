package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * School Profile Private School Quick Facts page
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolProfilePrivateQuickFactsTagHandler extends BaseSchoolProfileTagHandler{
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE_PRIVATE_QUICK_FACTS);
    }
}
