package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 *
 */
public class ContactUsTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.CONTACT_US, getState());
        return builder;
    }
}
