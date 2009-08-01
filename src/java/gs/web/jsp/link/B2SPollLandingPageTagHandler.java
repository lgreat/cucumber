package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class B2SPollLandingPageTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.B2S_POLL_LANDING_PAGE);
        return builder;
    }

}

