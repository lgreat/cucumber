package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class SubmitPreschoolTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.SUBMIT_PRESCHOOL, getState());
    }
}
