package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * This tag handler implements the new <link:...> architecture.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BetaSignupTagHandler extends LinkTagHandler {

    /** a user's email address : optional */
    private String _email;

    /**
     * @return a <code>UrlBuilder</code> object.
     */
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.BETA_SIGNUP, getState(), getEmail());
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }
}
