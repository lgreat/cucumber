package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class FourthGradeTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.FOURTH_GRADE);
        return builder;
    }

}

