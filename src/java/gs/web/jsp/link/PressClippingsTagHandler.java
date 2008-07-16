package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * @author Young Fan <mailto:yfan@greatschools.net>
 */
public class PressClippingsTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.PRESS_CLIPPINGS, getState());
    }
}
