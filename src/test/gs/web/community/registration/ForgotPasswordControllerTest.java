package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.web.util.MockJavaMailSender;
import gs.data.community.IUserDao;
import gs.data.community.User;
import org.springframework.validation.BindException;
import org.easymock.MockControl;

import javax.mail.internet.MimeMessage;
import javax.mail.Multipart;
import javax.mail.Address;
import javax.mail.MessagingException;
import java.util.List;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jul 18, 2006
 * Time: 10:07:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class ForgotPasswordControllerTest extends BaseControllerTestCase {
    private ForgotPasswordController _controller;

    private IUserDao _userDao;
    private MockControl _userControl;
    private MockJavaMailSender _mailSender;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new ForgotPasswordController();
        _mailSender = new MockJavaMailSender();
        // have to set host else the mock mail sender will throw an exception
        // actual value is irrelevant
        _mailSender.setHost("greatschools.net");
        _controller.setMailSender(_mailSender);
        _userControl = MockControl.createControl(IUserDao.class);
        _userDao = (IUserDao) _userControl.getMock();
        _controller.setUserDao(_userDao);
    }

    public void testOnSubmit() throws Exception {
        String email = "forgotPasswordTest@greatschools.net";
        User user = new User();
        user.setEmail(email);
        user.setId(new Integer(123));
        user.setPlaintextPassword("foobar");

        _userDao.findUserFromEmailIfExists(email);
        _userControl.setReturnValue(user);
        _userControl.replay();

        UserCommand command = new UserCommand();
        command.setEmail(email);
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());

        _userControl.reset();
        _userDao.findUserFromEmailIfExists(email);
        _userControl.setReturnValue(user);
        _userControl.replay();

        _controller.onSubmit(getRequest(), getResponse(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
        verifyEmail(_mailSender.getSentMessages(), email,
                _controller.getEmailPlainTextUserExists(getRequest(), email, new Integer(123)),
                _controller.getEmailHTMLUserExists(getRequest(), email, new Integer(123)));
    }

    public void testOnSubmitUserNotExist() throws Exception {
        String email = "forgotPasswordTest@greatschools.net";

        _userDao.findUserFromEmailIfExists(email);
        _userControl.setReturnValue(null);
        _userControl.replay();

        UserCommand command = new UserCommand();
        command.setEmail(email);
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());

        _userControl.reset();
        _userDao.findUserFromEmailIfExists(email);
        _userControl.setReturnValue(null);
        _userControl.replay();

        _controller.onSubmit(getRequest(), getResponse(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
        verifyEmail(_mailSender.getSentMessages(), email,
                _controller.getEmailPlainTextUserNotExist(getRequest(), email),
                _controller.getEmailHTMLUserNotExist(getRequest(), email));
    }

    private void verifyEmail(List messages, String email, String plainTextBody, String HTMLBody) throws MessagingException, IOException {
        assertNotNull("No messages sent.", messages);
        assertEquals("More than 1 message sent", 1, messages.size());
        MimeMessage msg = (MimeMessage) messages.get(0);
        Address[] to = msg.getRecipients(MimeMessage.RecipientType.TO);
        assertNotNull("Empty to field", to);
        assertEquals("More than 1 to address", 1, to.length);
        assertEquals("To field does not equal user's email", email, to[0].toString());
        Multipart mp = (Multipart) msg.getContent();
        assertEquals("Email contains more than 2 content types", 2, mp.getCount());
        for (int x=0; x < mp.getCount(); x++) {
            if (mp.getBodyPart(x).getContentType().indexOf("text/plain") > -1) {
                assertEquals(plainTextBody, String.valueOf(mp.getBodyPart(0).getContent()));
            } else {
                assertEquals(HTMLBody, String.valueOf(mp.getBodyPart(1).getContent()));
            }
        }
    }

    public void testNoPasswordUser() throws Exception {
        User user = new User();
        user.setEmail("forgotPasswordTest@greatschools.net");
        user.setId(new Integer(124));

        _userDao.findUserFromEmailIfExists("forgotPasswordTest@greatschools.net");
        _userControl.setReturnValue(user);
        _userControl.replay();

        UserCommand command = new UserCommand();
        command.setEmail("forgotPasswordTest@greatschools.net");
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertTrue("Controller missing expected errors on validation", errors.hasErrors());
    }

    public void testEmailProvisionalUser() throws Exception {
        User user = new User();
        user.setEmail("forgotPasswordTest@greatschools.net");
        user.setId(new Integer(125));
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional();

        _userDao.findUserFromEmailIfExists("forgotPasswordTest@greatschools.net");
        _userControl.setReturnValue(user);
        _userControl.replay();

        UserCommand command = new UserCommand();
        command.setEmail(user.getEmail());
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertTrue("Controller missing expected errors on validation", errors.hasErrors());
    }
}
