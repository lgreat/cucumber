package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.community.IUserDao;
import gs.data.community.User;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RequestEmailValidationControllerTest extends BaseControllerTestCase {
    private RequestEmailValidationController _controller;

    private IUserDao _userDao;
    private EmailVerificationEmail _emailVerificationEmail;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new RequestEmailValidationController();
        _userDao = createStrictMock(IUserDao.class);
        _emailVerificationEmail = createStrictMock(EmailVerificationEmail.class);
        _controller.setUserDao(_userDao);
        _controller.setEmailVerificationEmail(_emailVerificationEmail);
        _controller.setViewName("/oh/what/a/beautiful/morning");
    }

    public void testBasics() {
        assertSame(_userDao, _controller.getUserDao());
        assertSame(_emailVerificationEmail, _controller.getEmailVerificationEmail());
    }

    private void replayAllMocks() {
        replayMocks(_userDao, _emailVerificationEmail);
    }

    private void verifyAllMocks() {
        verifyMocks(_userDao, _emailVerificationEmail);
    }

    public void testRequestEmailValidation() throws Exception {
        getRequest().setParameter("email", "aroy@greatschools.org");

        User user = new User();
        user.setEmail("aroy@greatschools.org");
        user.setId(1);
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional("foobar");

        expect(_userDao.findUserFromEmailIfExists("aroy@greatschools.org")).andReturn(user);

        _emailVerificationEmail.sendVerificationEmail(getRequest(), user, "/account/");
        replayAllMocks();
        _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
    }
}
