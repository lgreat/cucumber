package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import java.security.NoSuchAlgorithmException;

/**
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jun 27, 2006
 * Time: 1:56:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegistrationConfirmControllerTest extends BaseControllerTestCase {
    private RegistrationConfirmController _controller;

    private IUserDao _userDao;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (RegistrationConfirmController) appContext.getBean(RegistrationConfirmController.BEAN_ID);

        _userDao = (IUserDao)appContext.getBean(IUserDao.BEAN_ID);
    }

    public void testRegistrationConfirm() {
        // 1) create user record with non-validated password
        User user = new User();
        user.setEmail("testRegistrationConfirm@greatschools.net");
        _userDao.saveUser(user);
        try {
            user.setPlaintextPassword("foobar");
            user.setEmailProvisional();
            _userDao.saveUser(user);

            // 2) generate hash for user from email/id, add to request
            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            String id = hash + user.getId();
            getRequest().addParameter("id", id);

            // 3) call handleRequestInternal
            ModelAndView mAndV =_controller.handleRequestInternal(getRequest(), getResponse());
            // 4) verify no errors
            assertFalse(mAndV.getViewName().startsWith("redirect:"));

            // 5) verify that password has become validated
            assertTrue(_userDao.findUserFromId(user.getId().intValue()).isEmailValidated());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            // 6) remove user record (finally block)
            _userDao.removeUser(user.getId());
        }
     }

    public void testInvalidId() throws NoSuchAlgorithmException {
        String email = "noSuchEmail@address.com";
        // choose an id I know doesn't exist
        Integer id = new Integer(99999);
        String hash = DigestUtil.hashStringInt(email, id);
        getRequest().addParameter("id", hash + id);

        assertNull("Fake user " + email + " already exists??", _userDao.findUserFromEmailIfExists(email));

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());

        assertTrue("Validation not catching bad id", mAndV.getViewName().startsWith("redirect:"));
    }

    public void testInvalidHash() throws NoSuchAlgorithmException {
        String email = "noSuchEmail@address.com";
        // choose an id I know exists, but have the wrong email (which will screw up the hash)
        Integer id = new Integer(1);
        String hash = DigestUtil.hashStringInt(email, id);
        getRequest().addParameter("id", hash + id);

        assertNull("Fake user " + email + " already exists??", _userDao.findUserFromEmailIfExists(email));

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());

        assertTrue("Validation not catching bad hash", mAndV.getViewName().startsWith("redirect:"));
    }

}
