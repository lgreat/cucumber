package gs.web.jsp.link.microsite;

import gs.web.jsp.link.LinkTagHandler;
import gs.web.util.UrlBuilder;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class MediaChoicesTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.MEDIA_CHOICES);
    }
}
