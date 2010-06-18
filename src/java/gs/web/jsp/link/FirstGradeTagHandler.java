package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class FirstGradeTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.FIRST_GRADE);
        return builder;
    }

}

