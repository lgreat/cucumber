package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jul 18, 2006
 * Time: 10:02:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class ForgotPasswordTagHandler  extends LinkTagHandler {

    private String _email;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.FORGOT_PASSWORD, getState(), getEmail());
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }
}
