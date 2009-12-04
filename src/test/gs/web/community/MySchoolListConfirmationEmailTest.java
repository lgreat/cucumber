package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.data.util.email.MockJavaMailSender;
import gs.data.util.email.EmailHelperFactory;
import gs.data.community.User;

import javax.mail.MessagingException;
import javax.mail.Message;
import java.io.IOException;
import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class MySchoolListConfirmationEmailTest extends BaseControllerTestCase {
    private static final String FROM_EMAIL = "aroy@greatschools.org";
    private static final String FROM_NAME = "Anthony";
    private static final String SUBJECT = "Testing";
    private MySchoolListConfirmationEmail _email;
    private MockJavaMailSender _mailSender;

    public void setUp() throws Exception {
        super.setUp();
        _email = new MySchoolListConfirmationEmail();
        _email.setFromEmail(FROM_EMAIL);
        _email.setFromName(FROM_NAME);
        _email.setSubject(SUBJECT);
        _mailSender = new MockJavaMailSender();
        _mailSender.setHost("greatschools.org");
        EmailHelperFactory _factory = new EmailHelperFactory();
        _factory.setMailSender(_mailSender);
        _email.setEmailHelperFactory(_factory);
    }

    public void testSend() throws MessagingException, IOException {
        // verify init
        assertEquals(FROM_EMAIL, _email.getFromEmail());
        assertEquals(SUBJECT, _email.getSubject());
        assertEquals(FROM_NAME, _email.getFromName());
        // setup
        User user = new User();
        user.setEmail("aroy+1@greatschools.org");
        // call
        _email.sendToUser(user, getRequest());
        // verify
        List msgs = _mailSender.getSentMessages();
        assertNotNull(msgs);
        assertEquals(1, msgs.size());
        Message msg = (Message) msgs.get(0);
        assertEquals(SUBJECT, msg.getSubject());
        assertTrue(msg.getFrom()[0].toString().indexOf(FROM_NAME) > -1);
        assertTrue(msg.getFrom()[0].toString().indexOf(FROM_EMAIL) > -1);
        assertNotNull(msg.getContent());
    }

    public void testSendToUndeliverableUser() throws Exception {
        User user = new User();
        user.setEmail("foobar@greatschools.org");
        user.setUndeliverable(true);
        // call
        _email.sendToUser(user, getRequest());
        // verify
        List msgs = _mailSender.getSentMessages();
        assertNull(msgs);
    }

    /**
     * Remove x to trigger a real email send to the user's email address.
     */
    public void xtestRealSend() throws Exception {
        MySchoolListConfirmationEmail email = (MySchoolListConfirmationEmail)
                getApplicationContext().getBean(MySchoolListConfirmationEmail.BEAN_ID);
        User user = new User();
        user.setEmail("aroy@greatschools.org");

        email.sendToUser(user, getRequest());
    }
}
