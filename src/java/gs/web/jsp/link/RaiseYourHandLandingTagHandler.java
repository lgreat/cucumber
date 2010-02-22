package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class RaiseYourHandLandingTagHandler  extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.RAISE_YOUR_HAND_LANDING);
        return builder;
    }
}
