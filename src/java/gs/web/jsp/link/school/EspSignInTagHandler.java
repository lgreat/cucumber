package gs.web.jsp.link.school;

import gs.web.jsp.link.LinkTagHandler;
import gs.web.util.UrlBuilder;

/**
 * @author aroy@greatschools.org
 */
public class EspSignInTagHandler extends LinkTagHandler {

    @Override
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.ESP_SIGN_IN);
    }
}
