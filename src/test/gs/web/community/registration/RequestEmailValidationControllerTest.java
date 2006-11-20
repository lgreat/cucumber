package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.util.email.MockJavaMailSender;
import gs.data.community.IUserDao;
import gs.data.community.User;
import org.springframework.web.servlet.ModelAndView;
import org.easymock.MockControl;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RequestEmailValidationControllerTest extends BaseControllerTestCase {
    private RequestEmailValidationController _controller;

    private IUserDao _userDao;
    private MockControl _userControl;
    private MockJavaMailSender _mailSender;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new RequestEmailValidationController();
        _mailSender = new MockJavaMailSender();
        // have to set host else the mock mail sender will throw an exception
        // actual value is irrelevant
        _mailSender.setHost("greatschools.net");
        _controller.setMailSender(_mailSender);
        _userControl = MockControl.createControl(IUserDao.class);
        _userDao = (IUserDao) _userControl.getMock();
        _controller.setUserDao(_userDao);
        _controller.setViewName("/oh/what/a/beautiful/morning");
    }

    public void testRequestEmailValidation() throws Exception {
        // 1) create user record with non-validated password
        String email = "testRequestEmailValidation@greatschools.net";
        User user = new User();
        user.setEmail(email);
        user.setId(new Integer(246));
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional();

        getRequest().addParameter("email", email);

        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), user);
        _userControl.replay();
        // 3) call handleRequestInternal
        ModelAndView mAndV =_controller.handleRequestInternal(getRequest(), getResponse());
        // 4) verify no errors
        _userControl.verify();
        assertFalse(mAndV.getViewName().startsWith("redirect:"));
        assertNotNull(_mailSender.getSentMessages());
        assertTrue(_mailSender.getSentMessages().size() == 1);
    }
}
