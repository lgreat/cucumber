package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * @author greatschools.org>
 */
public class ChangeEmailTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.CHANGE_EMAIL, null);
    }
}
