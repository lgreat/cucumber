package gs.web.school;

import gs.data.community.User;
import gs.data.util.email.EmailHelper;
import gs.web.util.AbstractSendEmailBean;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.IOException;

/**
 * @author aroy@greatschools.org
 */
@Component("espRejectionEmail")
public class EspRejectionEmail extends AbstractSendEmailBean {
    public void sendRejectionEmail(User user)
            throws IOException, MessagingException {

        EmailHelper emailHelper = getEmailHelper();
        emailHelper.setSubject("Unable to verify school affiliation");
        emailHelper.setToEmail(user.getEmail());
        emailHelper.setFromEmail("gs-batch@greatschools.org");
        emailHelper.setFromName("GreatSchools");
        emailHelper.readHtmlFromResource("/gs/web/school/espRejectionEmail.txt");

        emailHelper.addInlineReplacement("FIRST_NAME", user.getFirstName());

        emailHelper.send();
    }
}
