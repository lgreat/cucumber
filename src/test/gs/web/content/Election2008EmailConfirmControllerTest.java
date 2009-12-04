package gs.web.content;

import gs.web.BaseControllerTestCase;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Young Fan <mailto:yfan@greatschools.org>
 */
public class Election2008EmailConfirmControllerTest extends BaseControllerTestCase {
    private Election2008EmailConfirmController _controller;
    private Election2008EmailCommand _command;

    private JavaMailSender _mailSender;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (Election2008EmailConfirmController) getApplicationContext().getBean(Election2008EmailConfirmController.BEAN_ID);
        _command = new Election2008EmailCommand();
        _mailSender = createMock(JavaMailSender.class);
        _controller.setMailSender(_mailSender);
    }

    public void testBasics() {
        assertSame(_controller.getMailSender(), _mailSender);
    }

    public void testOnBindAndValidate() {
        // TODO
    }

    public void testSendEmail() {
        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setText(_command.getMessage());
        smm.setTo(_command.getFriendEmail());
        smm.setSubject(_command.getSubject());
        smm.setFrom(_command.getUserEmail());

        _mailSender.send(smm);

        assertTrue(_controller.sendEmail(_command));
    }

    public void testOnSubmit() {
        // TODO
    }
}
