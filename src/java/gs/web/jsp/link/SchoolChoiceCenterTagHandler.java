package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class SchoolChoiceCenterTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.SCHOOL_CHOICE_CENTER, getState());
    }
}
