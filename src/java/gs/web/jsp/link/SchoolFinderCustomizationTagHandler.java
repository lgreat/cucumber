package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Dec 23, 2008
 * Time: 9:55:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class SchoolFinderCustomizationTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.SCHOOL_FINDER_CUSTOMIZATION);
    }
}
