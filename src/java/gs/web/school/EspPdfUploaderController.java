package gs.web.school;

import gs.data.community.User;
import gs.data.school.EspResponse;
import gs.data.school.IEspResponseDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Controller
@RequestMapping("/osp/pdfUploader")
public class EspPdfUploaderController implements ReadWriteAnnotationController {

    public static final String JSON = "application/json";

    public static final String UNAUTHORIZED_ERROR = "Unauthorized"; // value referenced in JS
    public static final String REQUEST_TOO_LARGE_ERROR = "Request too large"; // value referenced in JS
    public static final String INVALID_CONTENT_TYPE_ERROR = "File type not supported"; // value referenced in JS
    public static final String UNKNOWN_ERROR = "Unknown error";
    public static final String INCORRECT_REQUEST_TYPE_ERROR = "Not a multi-part MIME request";
    public static final String[] VALID_CONTENT_TYPES = {"application/pdf", "application/octet-stream"};
    public static final int MAX_FILE_BYTES = 1024 * 1024 * 20;

    protected static final Log _log = LogFactory.getLog(EspPdfUploaderController.class);

    @Autowired
    private EspFormValidationHelper _espFormValidationHelper;
    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private IEspResponseDao _espResponseDao;

    private class StateEditor extends PropertyEditorSupport {
        public void setAsText(String stateAbbreviation) throws IllegalArgumentException {
            State state = State.fromString(stateAbbreviation);
            super.setValue(state);
        }
    }
    
    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(State.class, null, new StateEditor());
    }

    @RequestMapping(method=RequestMethod.GET, value="/test")
    public String handleGet(
            @RequestParam(value="schoolId", required=false, defaultValue="1") Integer schoolId,
            @RequestParam(value="schoolDatabaseState", required=false, defaultValue="CA") State schoolDatabaseState,
            ModelMap modelMap) {

        modelMap.put("schoolId",schoolId);
        modelMap.put("schoolDatabaseState",schoolDatabaseState.getAbbreviation());

        return "espPdfUploaderTest";
    }
    
    public void setErrorOnModel(ModelMap model, String errorMessage) {
        model.put("errorMessage", errorMessage);
    }
    
    @RequestMapping(method=RequestMethod.POST)
    public ModelMap handlePost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();
        Integer schoolId;
        State schoolDatabaseState;
        Map<String,String> formFields = new HashMap<String,String>();
        ModelMap model = new ModelMap();
        School school = null;
        String type = null; //ACT or SAT

        // handle user not logged in
        if (user == null) {
            setErrorOnModel(model, UNAUTHORIZED_ERROR);
            return model;
        }

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (isMultipart) {
            // not meant to be accurate. Error when content length (which mostly contains an image) is overly large
            int wiggleRoom = 100000; // actual content will be a little larger than the image
            if (request.getContentLength() > EspPdfUploaderController.MAX_FILE_BYTES + wiggleRoom) {
                setErrorOnModel(model, REQUEST_TOO_LARGE_ERROR);
                return model;
            }
            
            ServletFileUpload upload = new ServletFileUpload();

            try {
                FileItemIterator iter = upload.getItemIterator(request);
                while (iter.hasNext()) {
                    FileItemStream fileStream = iter.next();
                    if (!fileStream.isFormField()) {
                        if (!ArrayUtils.contains(VALID_CONTENT_TYPES, fileStream.getContentType())) {
                            setErrorOnModel(model, INVALID_CONTENT_TYPE_ERROR);
                            return model;
                        }

                        // Handle a multi-part MIME encoded file.
                        if (formFields.containsKey("schoolId") && formFields.containsKey("schoolDatabaseState") && formFields.containsKey("type") && fileStream != null) {
                            try {
                                schoolId = Integer.valueOf(formFields.get("schoolId"));
                                schoolDatabaseState = State.fromString(formFields.get("schoolDatabaseState"));
                                school = getActiveNonPkOnlySchool(schoolDatabaseState, schoolId);
                                
                                if (school == null) {
                                    setErrorOnModel(model, UNKNOWN_ERROR);
                                    return model;
                                }
                                if (!_espFormValidationHelper.checkUserHasAccess(user, schoolDatabaseState, schoolId)) {
                                    setErrorOnModel(model, UNAUTHORIZED_ERROR);
                                    return model;
                                }
                            } catch (Exception e) {
                                _log.debug("Problem converting request param:", e);
                                return model;
                            }
                            
                            type = formFields.get("type");

                            try {
                                MimeMessage message = createEmail(user, school, type, fileStream);
                                Transport.send(message);
                            } catch (MessagingException e) {
                                _log.debug("Problem created mail message: ", e);
                                setErrorOnModel(model, UNKNOWN_ERROR);
                                return model;
                            } catch (IOException e ) {
                                _log.debug("Problem created mail message: ", e);
                                setErrorOnModel(model, UNKNOWN_ERROR);
                                return model;
                            }

                            // record that this OSP has a PDF by inserting an EspResponse
                            String answerKey = "has_" + StringUtils.lowerCase(type) + "_pdf";
                            EspResponse espResponse = createEspResponse(user, school, new Date(), answerKey, true, "yes");
                            List<EspResponse> espResponseList = new ArrayList<EspResponse>();
                            espResponseList.add(espResponse);

                            // Deactivate existing data first, then save
                            HashSet pdfResponseKey = new HashSet();
                            pdfResponseKey.add(answerKey);
                            _espResponseDao.deactivateResponsesByKeys(school, pdfResponseKey);
                            _espResponseDao.saveResponses(school, espResponseList);
                        }
                    } else {
                        // put multi-part form fields into a map for later use
                        formFields.put(fileStream.getFieldName(), IOUtils.toString(fileStream.openStream(), "UTF-8"));
                    }
                }

            } catch (FileUploadException e) {
                _log.debug("Error while trying to upload file:", e);
                setErrorOnModel(model, UNKNOWN_ERROR);
                return model;
            } catch (IOException e) {
                _log.debug("Error while trying to upload file:", e);
                setErrorOnModel(model, UNKNOWN_ERROR);
                return model;
            }

        } else {
            // Not a multi-part MIME request.
            setErrorOnModel(model, INCORRECT_REQUEST_TYPE_ERROR);
            return model;
        }

        return model;
    }

    protected EspResponse createEspResponse(User user, School school, Date now, String key, boolean active, String responseValue) {
        if (StringUtils.isBlank(responseValue)) {
            return null;
        }
        EspResponse espResponse = new EspResponse();
        espResponse.setKey(key);
        espResponse.setValue(StringUtils.left(responseValue, EspFormController.MAX_RESPONSE_VALUE_LENGTH));
        espResponse.setSchool(school);
        espResponse.setMemberId(user.getId());
        espResponse.setCreated(now);
        espResponse.setActive(active);
        return espResponse;
    }
    
    protected MimeMessage createEmail(User user, School school, String type, FileItemStream fileItemStream) throws MessagingException, IOException {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", System.getProperty("mail.server","mail.greatschools.org"));
        Session session = Session.getDefaultInstance(props, null);
        String recipientEmail = "test_upload@greatschools.org";
        String senderEmail = "noreply@greatschools.org";

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(senderEmail));
        msg.setSubject(type + " upload file for " + school.getName() + ", " + school.getDatabaseState().getAbbreviation());
        msg.setSentDate(new Date());
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        
        StringBuffer body = new StringBuffer();
        body.append("School name: ").append(school.getName()).append("\n");
        body.append("School state: ").append(school.getDatabaseState().getAbbreviation()).append("\n");
        body.append("School ID: ").append(school.getId()).append("\n");
        body.append("ACT or SAT: ").append(type).append("\n");
        body.append("User ID: ").append(user.getId()).append("\n");
        body.append("First name: ").append(user.getFirstName()).append("\n");
        body.append("Last name: ").append(user.getLastName()).append("\n");

        String fileName = fileItemStream.getName();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(msg, true);
        mimeMessageHelper.addAttachment(fileName, new ByteArrayResource(IOUtils.toByteArray(fileItemStream.openStream())));
        mimeMessageHelper.setText(body.toString());
        return msg;
    }

    /**
     * Parses the state and schoolId out of the request and fetches the school. Returns null if
     * it can't parse parameters, can't find school, or the school is inactive
     */
    protected School getActiveNonPkOnlySchool(State state, Integer schoolId) {
        if (state == null || schoolId == null) {
            return null;
        }
        School school = null;
        try {
            school = _schoolDao.getSchoolById(state, schoolId);
        } catch (Exception e) {
            // handled below
        }
        if (school == null || !school.isActive()) {
            _log.error("School is null or inactive: " + school);
            return null;
        }

        if (school.isPreschoolOnly()) {
            _log.error("School is preschool only! " + school);
            return null;
        }

        return school;
    }

}