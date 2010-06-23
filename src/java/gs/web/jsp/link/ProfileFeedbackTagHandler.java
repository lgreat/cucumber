package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ProfileFeedbackTagHandler extends LinkTagHandler {
    @Override
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.PROFILE_FEEDBACK, getState(), null);
    }
}
