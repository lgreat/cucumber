package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Nov 12, 2009
 * Time: 5:11:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class AboutUsTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.ABOUT_US);
        return builder;
    }
}
