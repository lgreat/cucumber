/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.jsp.link;

import gs.web.jsp.MockPageContext;
import gs.web.jsp.MockJspWriter;
import gs.web.community.registration.AuthenticationManager;
import gs.web.util.context.SessionContext;
import gs.web.util.MockSessionContext;
import gs.web.BaseTestCase;
import gs.data.community.User;

import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

/**
 * Provides testing for the ToCommunityTagHandler class.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ToCommunityTagHandlerTest extends BaseTestCase {

    private ToCommunityTagHandler _tagHandler;
    private AuthenticationManager _authManager;
    private MockSessionContext _sessionContext;
    private MockJspWriter _out;

    public void setUp() throws Exception {
        _tagHandler = new ToCommunityTagHandler();
        MockPageContext pageContext = new MockPageContext();
        _tagHandler.setJspContext(pageContext);
        _out = (MockJspWriter) pageContext.getOut();
        _authManager = new AuthenticationManager();
        _tagHandler.setAuthenticationManager(_authManager);

        pageContext.setAttribute(PageContext.PAGECONTEXT, pageContext);
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        _sessionContext = new MockSessionContext();
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);
    }

    public void testDoTagWithUser() throws IOException {
        User user = new User();
        user.setId(new Integer(99));
        user.setEmail("aroy@greatschools.net");
        _sessionContext.setUser(user);

        _tagHandler.setStyleClass("class");
        _tagHandler.setTarget("target");
        _tagHandler.setTargetLink("/loc.12345678");

        _tagHandler.doTag();

        assertEquals("class", _tagHandler.getStyleClass());
        assertEquals("target", _tagHandler.getTarget());
        assertEquals("/loc.12345678", _tagHandler.getTargetLink());

        String output = _out.getOutputBuffer().toString();
        assertTrue(StringUtils.isNotEmpty(output));
        assertTrue(output.indexOf("loc.12345678") > -1);
        assertTrue(output.indexOf(AuthenticationManager.WEBCROSSING_FORWARD_URL) > -1);
    }

    public void testDoTagWithoutUser() throws IOException {
        _tagHandler.setStyleClass("class");
        _tagHandler.setTarget("target");
        _tagHandler.setTargetLink("/loc.12345678");

        _tagHandler.doTag();

        assertEquals("class", _tagHandler.getStyleClass());
        assertEquals("target", _tagHandler.getTarget());
        assertEquals("/loc.12345678", _tagHandler.getTargetLink());

        String output = _out.getOutputBuffer().toString();
        assertTrue(StringUtils.isNotEmpty(output));
        assertTrue(output + " should contain \"loc.12345678\"", output.indexOf("loc.12345678") > -1);
        assertTrue(output.indexOf("loginOrRegister.page") > -1);
    }

    public void testConstructUrl() throws NoSuchAlgorithmException {
        User user = new User();
        user.setId(new Integer(99));
        user.setEmail("aroy@greatschools.net");
        String url = ToCommunityTagHandler.constructUrl("/loc.12345678", user, _authManager);
        assertTrue(url.indexOf(AuthenticationManager.WEBCROSSING_FORWARD_URL) > -1);
        assertTrue(url.indexOf("/loc.12345678") > -1);

        User user2 = new User();
        user2.setId(new Integer(98));
        user2.setEmail("aroy2@greatschools.net");
        String url2 = ToCommunityTagHandler.constructUrl("/loc.12345678", user2, _authManager);
        assertFalse(url.equals(url2));
    }
}
