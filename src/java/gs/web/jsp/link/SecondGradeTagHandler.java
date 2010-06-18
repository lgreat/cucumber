package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class SecondGradeTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.SECOND_GRADE);
        return builder;
    }

}

