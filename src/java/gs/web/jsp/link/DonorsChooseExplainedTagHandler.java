package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Oct 24, 2008
 * Time: 12:14:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class DonorsChooseExplainedTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.DONORS_CHOOSE_EXPLAINED);
    }
}
