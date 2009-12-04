package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Webby award thank you page
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 */
public class WebbyAwardThanksTagHandler extends LinkTagHandler {

    public WebbyAwardThanksTagHandler() {
        super();
        setRel("nofollow");
    }

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.WEBBY_AWARD_THANKS, getState());
    }
}
