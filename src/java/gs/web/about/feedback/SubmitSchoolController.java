package gs.web.about.feedback;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import java.util.Map;
import java.util.HashMap;

import gs.web.content.Election2008EmailCommand;
import gs.web.util.validator.UserCommandValidator;
import gs.web.util.validator.IRequestAwareValidator;
import gs.web.util.validator.SubmitPreschoolCommandValidator;
import gs.web.util.validator.SubmitPrivateSchoolCommandValidator;

/**
 * @author 
 */
public class SubmitSchoolController extends SimpleFormController {
    // TODO-6868 What BEAN_ID since reused by two different beans
    protected final Log _log = LogFactory.getLog(getClass());

    public static String TYPE_PRIVATE_SCHOOL = "private school";
    public static String TYPE_PRESCHOOL = "preschool";

    private JavaMailSender _mailSender;
    private String _type;

    @Override
    protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {
        super.onBindAndValidate(request, command, errors);

        if (TYPE_PRESCHOOL.equals(getType())) {
            SubmitPreschoolCommandValidator validator = new SubmitPreschoolCommandValidator();
            validator.validate(request, command, errors);
        } else if (TYPE_PRIVATE_SCHOOL.equals(getType())) {
            SubmitPrivateSchoolCommandValidator validator = new SubmitPrivateSchoolCommandValidator();
            validator.validate(request, command, errors);
        }
    }

    @Override
    protected ModelAndView onSubmit(Object o) throws ServletException {
        return new ModelAndView(getSuccessView());
    }

    protected boolean sendEmail(String text, String to, String subject, String from) {
        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setText(text);
        smm.setTo(to);
        smm.setSubject(subject);
        smm.setFrom(from);

        try {
            _mailSender.send(smm);
        } catch (MailException ex) {
            _log.error(ex.getMessage());
            return false;
        }
        return true;
    }

    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        SubmitSchoolCommand command = (SubmitSchoolCommand)o;
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("type", getType());
        return map;
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }
}
