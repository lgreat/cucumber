package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.web.util.MockJavaMailSender;
import gs.data.community.IUserDao;
import gs.data.community.User;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RequestEmailValidationControllerTest extends BaseControllerTestCase {
    private RequestEmailValidationController _controller;

    private IUserDao _userDao;
    private MockJavaMailSender _mailSender;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (RequestEmailValidationController) appContext.getBean(RequestEmailValidationController.BEAN_ID);
        _mailSender = new MockJavaMailSender();
        // have to set host else the mock mail sender will throw an exception
        // actual value is irrelevant
        _mailSender.setHost("greatschools.net");
        _controller.setMailSender(_mailSender);
        _userDao = (IUserDao)appContext.getBean(IUserDao.BEAN_ID);
    }

    public void testRequestEmailValidation() {
        // 1) create user record with non-validated password
        User user = new User();
        user.setEmail("testRequestEmailValidation@greatschools.net");
        _userDao.saveUser(user);
        try {
            user.setPlaintextPassword("foobar");
            user.setEmailProvisional();
            _userDao.saveUser(user);

            getRequest().addParameter("email", "testRequestEmailValidation@greatschools.net");
            // 3) call handleRequestInternal
            ModelAndView mAndV =_controller.handleRequestInternal(getRequest(), getResponse());
            // 4) verify no errors
            assertFalse(mAndV.getViewName().startsWith("redirect:"));
            assertNotNull(_mailSender.getSentMessages());
            assertTrue(_mailSender.getSentMessages().size() == 1);

        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            // 6) remove user record (finally block)
            _userDao.removeUser(user.getId());
        }

    }
}
