package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.util.email.MockJavaMailSender;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.easymock.MockControl;

import java.security.NoSuchAlgorithmException;

/**
 * Provides testing for RegistrationConfirmController
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationConfirmControllerTest extends BaseControllerTestCase {
    private RegistrationConfirmController _controller;

    private IUserDao _userDao;
    private MockControl _userControl;
    private MockJavaMailSender _mailSender;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new RegistrationConfirmController();

        _userControl = MockControl.createControl(IUserDao.class);
        _userDao = (IUserDao)_userControl.getMock();
        _controller.setUserDao(_userDao);
        _controller.setViewName("/room/with/a/view");

        _mailSender = new MockJavaMailSender();
        _mailSender.setHost("greatschools.net");
        RegistrationConfirmationEmail email = (RegistrationConfirmationEmail)
                getApplicationContext().getBean(RegistrationConfirmationEmail.BEAN_ID);
        email.getEmailHelperFactory().setMailSender(_mailSender);
        _controller.setRegistrationConfirmationEmail(email);
    }

    public void testRegistrationConfirm() throws NoSuchAlgorithmException {
        // 1) create user record with non-validated password
        User user = new User();
        user.setEmail("testRegistrationConfirm@greatschools.net");
        user.setId(new Integer(234));
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional();
        assertTrue(user.isEmailProvisional());
        assertFalse(user.isEmailValidated());

        _userControl.expectAndReturn(_userDao.findUserFromId(234), user);
        _userDao.saveUser(user);
        _userControl.replay();

        // 2) generate hash for user from email/id, add to request
        String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String id = hash + user.getId();
        getRequest().addParameter("id", id);

        // 3) call handleRequestInternal
        ModelAndView mAndV =_controller.handleRequestInternal(getRequest(), getResponse());
        // 4) verify no errors
        _userControl.verify();
        assertFalse(mAndV.getViewName().startsWith("redirect:"));

        // 5) verify that password has become validated
        assertFalse(user.isEmailProvisional());
        assertTrue(user.isEmailValidated());
        assertEquals(1, _mailSender.getSentMessages().size());
     }

    public void testInvalidId() throws NoSuchAlgorithmException {
        String email = "noSuchEmail@address.com";
        Integer id = new Integer(99999);
        String hash = DigestUtil.hashStringInt(email, id);
        getRequest().addParameter("id", hash + id);

        _userControl.expectAndThrow(_userDao.findUserFromId(99999),
                new ObjectRetrievalFailureException("Can not find user with id 99999", null));
        _userControl.replay();

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        _userControl.verify();

        assertTrue("Validation not catching bad id", mAndV.getViewName().startsWith("redirect:"));
    }

    public void testInvalidHash() throws NoSuchAlgorithmException {
        String email = "noSuchEmail@address.com";
        Integer id = new Integer(3344);
        String hash = DigestUtil.hashStringInt(email, id);
        getRequest().addParameter("id", hash + id);

        User user = new User();
        user.setId(new Integer(3344));
        user.setEmail("anotherEmail@address.com");
        _userControl.expectAndReturn(_userDao.findUserFromId(3344), user);
        _userControl.replay();

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        _userControl.verify();

        assertTrue("Validation not catching bad hash", mAndV.getViewName().startsWith("redirect:"));
    }

}
