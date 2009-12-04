package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * This tag handler implements the new <link:...> architecture.
 *
 * @author greatschools.org>
 */
public class BetaUnsubscribeTagHandler extends BetaSignupTagHandler {

    /**
     * @return a <code>UrlBuilder</code> object.
     */
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.BETA_UNSUBSCRIBE, getState(), getEmail());
    }
}
