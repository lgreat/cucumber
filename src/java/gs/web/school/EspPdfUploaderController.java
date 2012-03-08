package gs.web.school;

import gs.data.community.User;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
                        if (formFields.containsKey("schoolId") && formFields.containsKey("schoolDatabaseState") && fileStream != null) {
                            try {
                                schoolId = Integer.valueOf(formFields.get("schoolId"));
                                schoolDatabaseState = State.fromString(formFields.get("schoolDatabaseState"));

                                if (!_espFormValidationHelper.checkUserHasAccess(user, schoolDatabaseState, schoolId)) {
                                    setErrorOnModel(model, UNAUTHORIZED_ERROR);
                                    return model;
                                }
                            } catch (Exception e) {
                                _log.debug("Problem converting request param:", e);
                                return model;
                            }

                            // TODO: do something with the file
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

}