package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class SubmitPrivateSchoolTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.SUBMIT_PRIVATE_SCHOOL, getState());
    }
}
