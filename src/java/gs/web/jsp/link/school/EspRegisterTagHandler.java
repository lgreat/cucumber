package gs.web.jsp.link.school;

import gs.web.jsp.link.LinkTagHandler;
import gs.web.util.UrlBuilder;

/**
 * @author aroy@greatschools.org
 */
public class EspRegisterTagHandler extends LinkTagHandler {

    @Override
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.ESP_REGISTRATION);
    }
}
