package gs.web.jsp.link.microsite;

import gs.web.util.UrlBuilder;
import gs.web.jsp.link.LinkTagHandler;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Jul 8, 2008
 * Time: 6:23:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class BackToSchoolTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.BACK_TO_SCHOOL);
    }
}
