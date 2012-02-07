package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * @author aroy@greatschools.org
 */
public class PrincipalReviewTagHandler extends BaseSchoolTagHandler {
    @Override
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PRINCIPAL_REVIEW);
    }
}
