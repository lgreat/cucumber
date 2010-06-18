package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class ThirdGradeTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.THIRD_GRADE);
        return builder;
    }

}

