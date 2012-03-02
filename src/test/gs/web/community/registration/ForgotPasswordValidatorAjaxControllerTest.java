package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.web.BaseControllerTestCase;

import static org.easymock.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ForgotPasswordValidatorAjaxControllerTest extends BaseControllerTestCase {
    private ForgotPasswordValidatorAjaxController _controller;
    private IUserDao _userDao;
    private String _email = "aroy@greatschools.org";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new ForgotPasswordValidatorAjaxController();

        _userDao = createStrictMock(IUserDao.class);
        _controller.setUserDao(_userDao);
        getRequest().setParameter("email", _email);        
    }

    public void testBasics() {
        assertSame(_userDao, _controller.getUserDao());
    }

    public void testNoEmail() throws Exception {
        getRequest().setParameter("email", (String)null);
        replayMocks(_userDao);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks(_userDao);
        assertTrue(getResponse().getContentAsString().contains("\"errorMsg\":"));
        assertTrue(getResponse().getContentAsString().contains("\"errorCode\":"));
    }

    public void testInvalidEmail() throws Exception {
        getRequest().setParameter("email", "foobar");
        replayMocks(_userDao);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks(_userDao);
        assertTrue(getResponse().getContentAsString().contains("\"errorMsg\":"));
        assertTrue(getResponse().getContentAsString().contains("\"errorCode\":"));
    }

    public void testNoUser() throws Exception {
        expect(_userDao.findUserFromEmailIfExists(_email)).andReturn(null);
        replayMocks(_userDao);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks(_userDao);
        assertTrue(getResponse().getContentAsString().contains("\"errorMsg\":"));
        assertTrue(getResponse().getContentAsString().contains("\"errorCode\":"));
    }

    public void testProvisionalUser() throws Exception {
        User user = new User();
        user.setId(1);
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional("foobar");
        user.setUserProfile(new UserProfile());
        expect(_userDao.findUserFromEmailIfExists(_email)).andReturn(user);
        replayMocks(_userDao);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks(_userDao);
        assertTrue(getResponse().getContentAsString().contains("\"errorMsg\":"));
        assertTrue(getResponse().getContentAsString().contains("\"errorCode\":"));
    }

    public void testSuccess() throws Exception {
        User user = new User();
        user.setId(1);
        user.setPlaintextPassword("foobar");
        user.setUserProfile(new UserProfile());
        expect(_userDao.findUserFromEmailIfExists(_email)).andReturn(user);
        replayMocks(_userDao);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks(_userDao);
        assertEquals("{}", getResponse().getContentAsString());
    }
}
