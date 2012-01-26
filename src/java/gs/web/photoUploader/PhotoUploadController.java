package gs.web.photoUploader;

import gs.data.community.User;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.json.JSONObject;
import gs.data.school.ISchoolMediaDao;
import gs.data.school.SchoolMedia;
import gs.data.school.SchoolMediaDaoHibernate;
import gs.data.state.State;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyEditorSupport;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/photoUploader/photoUploaderTest")
public class PhotoUploadController implements ReadWriteAnnotationController {

    private static final String RESP_SUCCESS = "{\"jsonrpc\" : \"2.0\", \"result\" : null, \"id\" : \"id\"}";
    private static final String RESP_ERROR = "{\"jsonrpc\" : \"2.0\", \"error\" : {\"code\": 101, \"message\": \"Failed to open input stream.\"}, \"id\" : \"id\"}";
    public static final String JSON = "application/json";
    public static final int FULL_SIZE_IMAGE_MAX_DIMENSION = 500;
    
    public static final String NOT_LOGGED_IN_ERROR = "Unauthorized"; // value referenced in JS

    private ISchoolMediaDao _schoolMediaDao;

    protected static final Log _log = LogFactory.getLog(PhotoUploadController.class);

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

    @RequestMapping(method=RequestMethod.GET)
    public String handleGet(
            @RequestParam(value="schoolId", required=false, defaultValue="1") Integer schoolId,
            @RequestParam(value="schoolDatabaseState", required=false, defaultValue="CA") State schoolDatabaseState,
            ModelMap modelMap) {

        if (schoolId != null && schoolDatabaseState != null) {
            List<SchoolMedia> schoolMedias = _schoolMediaDao.getAllActiveAndPendingBySchool(schoolId, schoolDatabaseState);
            modelMap.put("schoolMedias", schoolMedias);
        }

        modelMap.put("schoolId",schoolId);
        modelMap.put("schoolDatabaseState",schoolDatabaseState.getAbbreviation());

        return "uploaderTest2";
    }

    @RequestMapping(method=RequestMethod.POST)
    public void handlePost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();
        Integer schoolId;
        State schoolDatabaseState;
        Map<String,String> formFields = new HashMap<String,String>();

        // handle user not logged in
        if (user == null) {
            error(response, "-1", NOT_LOGGED_IN_ERROR);
        }

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (isMultipart) {
            ServletFileUpload upload = new ServletFileUpload();

            try {
                FileItemIterator iter = upload.getItemIterator(request);
                while (iter.hasNext()) {
                    FileItemStream fileStream = iter.next();

                    if (!fileStream.isFormField()) {
                        // Handle a multi-part MIME encoded file.
                        if (formFields.containsKey("schoolId") && formFields.containsKey("schoolDatabaseState") && fileStream != null) {
                            try {
                                schoolId = Integer.valueOf(formFields.get("schoolId"));
                                schoolDatabaseState = State.fromString(formFields.get("schoolDatabaseState"));
                            } catch (Exception e) {
                                _log.debug("Problem converting request param:", e);
                                return;
                            }

                            SchoolPhotoProcessor processor = createSchoolPhotoProcessor(fileStream);
                            SchoolMedia schoolMedia = createSchoolMediaObject(user.getId(), schoolId, schoolDatabaseState, fileStream.getName());
                            getSchoolMediaDao().save(schoolMedia);
                            processor.handleScaledPhoto(user, schoolMedia.getId());
                            processor.finish();
                        }
                    } else {
                        // put multi-part form fields into a map for later use
                        formFields.put(fileStream.getFieldName(), IOUtils.toString(fileStream.openStream(), "UTF-8"));
                    }
                }

            } catch (FileUploadException e) {
                _log.debug("Eror while trying to upload file:", e);
                error(response, "-1", "Unknown error while uploading file.");
                return;
            } catch (IOException e) {
                _log.debug("Eror while trying to upload file:", e);
                error(response, "-1", "Unknown error while uploading file.");
                return;
            }

        } else {
            // Not a multi-part MIME request.
            error(response, "101", "Not a multi-part MIME request");
            return;
        }

        success(response);
    }

    @RequestMapping(method=RequestMethod.PUT)
    protected void handleUpdate(@RequestParam(value="schoolMediaId", required=false) Integer schoolMediaId,
                                @RequestParam(value="schoolId", required=false) Integer schoolId,
                                @RequestParam(value="schoolDatabaseState", required=false) State schoolDatabaseState,
                                HttpServletResponse response) {

        State databaseState = null;

        SchoolMedia schoolMedia = _schoolMediaDao.getById((int)schoolMediaId);

        if (schoolMedia != null && schoolMedia.getSchoolId().equals(schoolId) && schoolMedia.getSchoolState().equals(databaseState)) {
            schoolMedia.setStatus(SchoolMediaDaoHibernate.Status.ACTIVE.value);
            _schoolMediaDao.save(schoolMedia);
            ThreadLocalTransactionManager.commitOrRollback();
        } else {
            error(response, "102", "Error updating photo.");
            return;
        }

        success(response);

    }

    public SchoolPhotoProcessor createSchoolPhotoProcessor(FileItemStream fileItemStream) throws IOException {
        return new SchoolPhotoProcessor(fileItemStream);
    }

    @RequestMapping(method=RequestMethod.DELETE)
    protected void handleDelete(@RequestParam(value="schoolMediaId", required=false) Integer schoolMediaId,
                                @RequestParam(value="schoolId", required=false) Integer schoolId,
                                @RequestParam(value="schoolDatabaseState", required=false) State schoolDatabaseState,
                                HttpServletResponse response) {

        SchoolMedia schoolMedia = _schoolMediaDao.getById((int)schoolMediaId);

        if (schoolMedia != null && schoolMedia.getSchoolId().equals(schoolId) && schoolMedia.getSchoolState().equals(schoolDatabaseState)) {
            schoolMedia.setStatus(SchoolMediaDaoHibernate.Status.DELETED.value);
            _schoolMediaDao.save(schoolMedia);
            ThreadLocalTransactionManager.commitOrRollback();
        } else {
            error(response, "102", "Error updating photo.");
            return;
        }

        success(response);
    }

    public SchoolMedia createSchoolMediaObject(Integer memberId, Integer schoolId, State schoolState, String fileName) {
        int schoolMediaStatusProcessing = SchoolMediaDaoHibernate.Status.PENDING.value;

        SchoolMedia schoolMedia = new SchoolMedia(schoolId, schoolState);
        schoolMedia.setMemberId(memberId);
        schoolMedia.setStatus(schoolMediaStatusProcessing);
        schoolMedia.setOriginalFileName(fileName);
        return schoolMedia;
    }
    
    protected void success(HttpServletResponse response) {
        try {
            outputJson(response, RESP_SUCCESS);
        } catch (IOException e) {
            _log.debug("Error occured while trying to write to response: " + RESP_SUCCESS, e);
        }
    }
    
    protected void error(HttpServletResponse response, String code, String message) {
        String json = "{\"jsonrpc\" : \"2.0\", \"error\" : {\"code\": " + code + ", \"message\": \"" + message + "\"}, \"id\" : \"id\"}";
        
        try {
            outputJson(response, json);
        } catch (IOException e) {
            _log.debug("Error occured while trying to write to response: " + RESP_ERROR, e);
        }
    }

    protected void outputJson(HttpServletResponse response, String responseString) throws IOException {
        response.setContentType(JSON);
        byte[] responseBytes = responseString.getBytes();
        response.setContentLength(responseBytes.length);
        ServletOutputStream output = response.getOutputStream();
        output.write(responseBytes);
        output.flush();
    }

    protected void jsonResponse(HttpServletResponse response, Map<Object,Object> data) {
        try {
            response.setContentType("application/json");
            JSONObject rval = new JSONObject(data);
            response.getWriter().print(rval.toString());
            response.getWriter().flush();
        } catch (IOException e) {
            _log.info("Failed to get response writer");
            //give up
        }
    }


    public ISchoolMediaDao getSchoolMediaDao() {
        return _schoolMediaDao;
    }

    public void setSchoolMediaDao(ISchoolMediaDao schoolMediaDao) {
        _schoolMediaDao = schoolMediaDao;
    }
}
