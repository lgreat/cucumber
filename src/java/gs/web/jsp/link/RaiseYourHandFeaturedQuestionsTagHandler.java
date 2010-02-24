package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class RaiseYourHandFeaturedQuestionsTagHandler  extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.RAISE_YOUR_HAND_FEATURED_QUESTIONS);
        return builder;
    }
}
