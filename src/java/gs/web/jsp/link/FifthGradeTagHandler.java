package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class FifthGradeTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.FIFTH_GRADE);
        return builder;
    }

}


