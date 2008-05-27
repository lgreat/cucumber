package gs.web.jsp.link.microsite;

import gs.web.jsp.link.LinkTagHandler;
import gs.web.util.UrlBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: May 27, 2008
 * Time: 9:53:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class SummerReadingTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.SUMMER_READING);
    }
}
