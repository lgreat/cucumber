package gs.web.about.feedback;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.mail.javamail.JavaMailSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.mail.MessagingException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;

import gs.web.util.validator.SubmitPreschoolCommandValidator;
import gs.web.util.validator.SubmitPrivateSchoolCommandValidator;
import gs.web.util.context.SessionContextUtil;
import gs.data.state.State;
import gs.data.geo.IGeoDao;
import gs.data.geo.ICounty;
import gs.data.util.email.EmailHelperFactory;
import gs.data.util.email.EmailHelper;

/**
 * @author Young Fan 
 */
public class SubmitSchoolController extends SimpleFormController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static String TYPE_PRIVATE_SCHOOL = "private school";
    public static String TYPE_PRESCHOOL = "preschool";

    public static String PRESCHOOL_THANK_YOU_EMAIL_PATH = "gs/web/about/feedback/preschoolConfirmationEmail.txt";
    public static String PRIVATE_SCHOOL_THANK_YOU_EMAIL_PATH = "gs/web/about/feedback/privateSchoolConfirmationEmail.txt";

    public static String THANK_YOU_EMAIL_SUBJECT = "Thanks for your feedback";

    private IGeoDao _geoDao;
    private String _type;
    private String _fromEmail;
    private EmailHelperFactory _emailHelperFactory;

    // SPRING MVC METHODS

    @Override
    protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {
        super.onBindAndValidate(request, command, errors);

        if (TYPE_PRESCHOOL.equals(_type)) {
            SubmitPreschoolCommandValidator validator = new SubmitPreschoolCommandValidator();
            validator.validate(request, command, errors);
        } else if (TYPE_PRIVATE_SCHOOL.equals(_type)) {
            SubmitPrivateSchoolCommandValidator validator = new SubmitPrivateSchoolCommandValidator();
            validator.validate(request, command, errors);
        }
    }

    @Override
    protected ModelAndView onSubmit(Object o) throws ServletException {
        SubmitSchoolCommand command = (SubmitSchoolCommand)o;

        String subject = null;
        if (TYPE_PRESCHOOL.equals(_type)) {
            subject = "Add a preschool";
        } else if (TYPE_PRIVATE_SCHOOL.equals(_type)) {
            subject = "Add a private school";
        }

        String thankYouEmailPath = null;
        if (TYPE_PRESCHOOL.equals(_type)) {
            thankYouEmailPath = PRESCHOOL_THANK_YOU_EMAIL_PATH;
        } else if (TYPE_PRIVATE_SCHOOL.equals(_type)) {
            thankYouEmailPath = PRIVATE_SCHOOL_THANK_YOU_EMAIL_PATH;
        }

        try {

            // send submission to data team queue
            sendEmail(_fromEmail, _fromEmail, subject, createSubmissionBodyText(command), false, false);

            // send thank you email to submitter
            sendEmail(command.getSubmitterEmail(), _fromEmail, THANK_YOU_EMAIL_SUBJECT, thankYouEmailPath, true, true);
            
        } catch (IOException e) {
            throw new ServletException(e);
        } catch (MessagingException e) {
            throw new ServletException(e);
        }

        Map<String,String> model = new HashMap<String,String>();
        model.put("type", _type);

        return new ModelAndView(getSuccessView(), model);
    }

    @Override
    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        SubmitSchoolCommand command = (SubmitSchoolCommand)o;
        Map<String,Object> map = new HashMap<String,Object>();

        map.put("type", _type);

        State state = command.getState();
        if (state == null) {
            state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        }
        List<ICounty> counties = _geoDao.findCounties(state);
        map.put("countyList", counties);

        return map;
    }

    // BUSINESS LOGIC

    protected String createSubmissionBodyText(SubmitSchoolCommand command) {
        StringBuilder sb = new StringBuilder();
        sb.append("SUBMITTER INFORMATION:\n");
        sb.append("\n");
        sb.append("<b>Your name</b>: ").append(command.getSubmitterName()).append("\n");
        sb.append("<b>Your email address</b>: ").append(command.getSubmitterEmail()).append("\n");
        sb.append("<b>Your connection to the school</b>: ").append(command.getSubmitterConnectionToSchool()).append("\n");
        sb.append("\n");
        sb.append("SCHOOL INFORMATION:\n");
        sb.append("\n");
        sb.append("<b>School name</b>: ").append(command.getSchoolName()).append("\n");
        sb.append("<b>Physical street address</b>: ").append(command.getStreetAddress()).append("\n");
        sb.append("<b>City</b>: ").append(command.getCity()).append("\n");
        sb.append("<b>State</b>: ").append(command.getState().getAbbreviation()).append("\n");
        sb.append("<b>Zip code</b>: ").append(command.getZipCode()).append("\n");
        sb.append("<b>County</b>: ").append(command.getCounty()).append("\n");
        sb.append("<b>Number of students enrolled</b>: ").append(command.getNumStudentsEnrolled()).append("\n");
        sb.append("<b>Phone number</b>: ").append(command.getPhoneNumber()).append("\n");
        sb.append("<b>Fax number</b>: ").append(!StringUtils.isBlank(command.getFaxNumber()) ? command.getFaxNumber() : "").append("\n");
        sb.append("<b>School web site</b>: ").append(!StringUtils.isBlank(command.getSchoolWebSite()) ? command.getSchoolWebSite() : "").append("\n");
        sb.append("<b>Religious affiliation or denomination</b>: ").append(!StringUtils.isBlank(command.getReligion()) ? command.getReligion() : "").append("\n");
        sb.append("<b>Association memberships</b>: ").append(!StringUtils.isBlank(command.getAssociationMemberships()) ? command.getAssociationMemberships() : "").append("\n");

        if (TYPE_PRESCHOOL.equals(_type)) {
            SubmitPreschoolCommand cmd = (SubmitPreschoolCommand)command;
            sb.append("\n");
            sb.append("PRESCHOOL-SPECIFIC INFORMATION:\n");
            sb.append("\n");
            sb.append("<b>Lowest age served</b>: ").append(cmd.getLowestAge()).append("\n");
            sb.append("<b>Highest age served</b>: ").append(cmd.getHighestAge()).append("\n");
            sb.append("<b>Bilingual education offered</b>: ").append(cmd.getBilingualEd()).append("\n");
            sb.append("<b>Special education programs offered</b>: ").append(cmd.getSpecialEd()).append("\n");
            sb.append("<b>Computers present in the classroom</b>: ").append(cmd.getComputersPresent()).append("\n");
            sb.append("<b>Extended care available</b>: ").append(cmd.getExtendedCare()).append("\n");
            sb.append("<b>Type of preschool</b>: ").append(cmd.getPreschoolSubtype()).append("\n");
        } else if (TYPE_PRIVATE_SCHOOL.equals(_type)) {
            SubmitPrivateSchoolCommand cmd = (SubmitPrivateSchoolCommand)command;
            sb.append("\n");
            sb.append("PRIVATE-SCHOOL-SPECIFIC INFORMATION:\n");
            sb.append("\n");
            sb.append("<b>Lowest grade offered</b>: ").append(cmd.getLowestGrade()).append("\n");
            sb.append("<b>Highest grade offered</b>: ").append(cmd.getHighestGrade()).append("\n");
            sb.append("<b>Gender(s)</b>: ").append(cmd.getGender()).append("\n");
        }

        sb.append("\n");
        sb.append("\n");
        sb.append("XML FOR DATA LOADING:\n");
        sb.append("\n");

        sb.append("<agency stateAbbrev=\"").append(command.getState().getAbbreviation()).append("\">\n");
        sb.append("<name>").append(StringEscapeUtils.escapeXml(command.getSchoolName())).append("</name>\n");
        sb.append("<type>private</type>\n");

        if (TYPE_PRESCHOOL.equals(_type)) {
            SubmitPreschoolCommand cmd = (SubmitPreschoolCommand)command;
            sb.append("<subtypes><subtype>").append(cmd.getPreschoolSubtype()).append("</subtype></subtypes>\n");
        } else if (TYPE_PRIVATE_SCHOOL.equals(_type)) {
            SubmitPrivateSchoolCommand cmd = (SubmitPrivateSchoolCommand)command;
            sb.append("<subtypes><subtype>").append(cmd.getGender()).append("</subtype></subtypes>\n");
        }

        sb.append("<contact contactType=\"").append(StringEscapeUtils.escapeXml(command.getSubmitterConnectionToSchool())).append("\">\n");
        sb.append("    <name>").append(StringEscapeUtils.escapeXml(command.getSubmitterName())).append("</name>\n");
        sb.append("    <email>").append(StringEscapeUtils.escapeXml(command.getSubmitterEmail())).append("</email>\n");
        sb.append("</contact>\n");
        if (!StringUtils.isBlank(command.getSchoolWebSite())) {
            sb.append("<url>").append(StringEscapeUtils.escapeXml(command.getSchoolWebSite())).append("</url>\n");
        }
        sb.append("<phoneNumber>").append(command.getPhoneNumber()).append("</phoneNumber>\n");

        if (!StringUtils.isBlank(command.getFaxNumber())) {
            sb.append("<faxNumber>").append(command.getFaxNumber()).append("</faxNumber>\n");
        }
        sb.append("<affiliations>").append(StringEscapeUtils.escapeXml(command.getReligion())).append("</affiliations>\n");
        sb.append("<associations>").append(StringEscapeUtils.escapeXml(command.getAssociationMemberships())).append("</associations>\n");

        ICounty county = _geoDao.findCountyByName(command.getCounty(), command.getState());
        if (county != null) {
            sb.append("<fipsCountyCode>").append(county.getCountyFips()).append("</fipsCountyCode>\n");
        }

        sb.append("<countyName>").append(StringEscapeUtils.escapeXml(command.getCounty())).append("</countyName>\n");
        sb.append("<address type=\"LOC\">\n");
        sb.append("    <line1>").append(StringEscapeUtils.escapeXml(command.getStreetAddress())).append("</line1>\n");
        sb.append("    <city>").append(StringEscapeUtils.escapeXml(command.getCity())).append("</city>\n");
        sb.append("    <stateAbbrev>").append(command.getState().getAbbreviation()).append("</stateAbbrev>\n");
        sb.append("    <zipCode>").append(command.getZipCode()).append("</zipCode>\n");
        sb.append("</address>\n");

        if (TYPE_PRESCHOOL.equals(_type)) {
            SubmitPreschoolCommand cmd = (SubmitPreschoolCommand)command;
            if (!StringUtils.isBlank(cmd.getBilingualEd())) {
                sb.append("<pk_bilingual>").append(cmd.getBilingualEd().substring(0,1).toUpperCase()).append("</pk_bilingual>\n");
            }
            if (!StringUtils.isBlank(cmd.getSpecialEd())) {
                sb.append("<pk_specialEd>").append(cmd.getSpecialEd().substring(0,1).toUpperCase()).append("</pk_specialEd>\n");
            }
            if (!StringUtils.isBlank(cmd.getComputersPresent())) {
                sb.append("<pk_computers>").append(cmd.getComputersPresent().substring(0,1).toUpperCase()).append("</pk_computers>\n");
            }
            if (!StringUtils.isBlank(cmd.getExtendedCare())) {
                sb.append("<pk_beforeAfterCare>").append(cmd.getExtendedCare().substring(0,1).toUpperCase()).append("</pk_beforeAfterCare>\n");
            }
            sb.append("<pk_lowAge>").append(cmd.getLowestAge().toUpperCase()).append("</pk_lowAge>\n");
            sb.append("<pk_highAge>").append(cmd.getHighestAge().toUpperCase()).append("</pk_highAge>\n");
            sb.append("<pk_capacity>").append(command.getNumStudentsEnrolled()).append("</pk_capacity>\n");
        } else if (TYPE_PRIVATE_SCHOOL.equals(_type)) {
            SubmitPrivateSchoolCommand cmd = (SubmitPrivateSchoolCommand)command;
            sb.append("<grades>\n");
            sb.append("    <grade>").append(cmd.getLowestGrade()).append("</grade>\n");
            sb.append("    <grade>").append(cmd.getHighestGrade()).append("</grade>\n");
            sb.append("</grades>\n");
            sb.append("<membership>\n");
            sb.append("<amount>").append(command.getNumStudentsEnrolled()).append("</amount>\n");
            sb.append("</membership>\n");
        }

        sb.append("</agency>\n");

        return sb.toString();
    }

    protected void sendEmail(String to, String from, String subject, String body, boolean isHtml, boolean isResource) throws MessagingException, IOException {
        EmailHelper emailHelper = getEmailHelperFactory().getEmailHelper();
        emailHelper.setSubject(subject);
        emailHelper.setFromEmail(from);
        emailHelper.setToEmail(to);

        // prevent "This message was sent to GreatSchools subscriber: [To email address]"
        // from appearing by setting non-null empty string
        // (must be called before readHtmlFromResource or setHtmlBody)
        emailHelper.setSentToCustomMessage("");

        if (isResource) {
            if (isHtml) {
                emailHelper.readHtmlFromResource(body);
            } else {
                emailHelper.readPlainTextFromResource(body);
            }
        } else {
            if (isHtml) {
                emailHelper.setHtmlBody(body);
            } else {
                emailHelper.setTextBody(body);
            }
        }
        emailHelper.send();
    }

    // PROPERTIES FROM pages-servlet.xml

    public void setType(String type) {
        _type = type;
    }

    public void setFromEmail(String fromEmail) {
        _fromEmail = fromEmail;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public EmailHelperFactory getEmailHelperFactory() {
        return _emailHelperFactory;
    }

    public void setEmailHelperFactory(EmailHelperFactory emailHelperFactory) {
        _emailHelperFactory = emailHelperFactory;
    }
}
