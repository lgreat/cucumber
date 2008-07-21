package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Generates link to the My School List login page.
 */
public class MySchoolListLoginTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST_LOGIN);
    }
}
