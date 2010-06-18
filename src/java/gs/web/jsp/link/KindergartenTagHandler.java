package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class KindergartenTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.KINDERGARTEN);
        return builder;
    }

}
