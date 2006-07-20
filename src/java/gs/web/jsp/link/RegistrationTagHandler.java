package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jul 3, 2006
 * Time: 3:15:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegistrationTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, getState());
        return builder;
    }
}
