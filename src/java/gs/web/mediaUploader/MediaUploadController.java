package gs.web.mediaUploader;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.media.*;
import gs.data.media.IMediaUploadDao;
import gs.data.realEstateAgent.AgentAccount;
import gs.data.realEstateAgent.IAgentAccountDao;
import gs.web.photoUploader.SchoolPhotoProcessor;
import gs.web.realEstateAgent.RealEstateAgentHelper;
import gs.web.util.ReadWriteAnnotationController;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 2/19/13
 * Time: 12:33 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/mediaUpload/")
public class MediaUploadController implements ReadWriteAnnotationController {

    private static final String RESP_SUCCESS = "{\"jsonrpc\" : \"2.0\", \"result\" : null, \"id\" : \"id\"}";
    private static final String RESP_ERROR = "{\"jsonrpc\" : \"2.0\", \"error\" : {\"code\": 101, \"message\": \"Failed to open input stream.\"}, \"id\" : \"id\"}";
    public static final String JSON = "application/json";

    public static final String UNAUTHORIZED_ERROR = "Unauthorized"; // value referenced in JS
    public static final String REQUEST_TOO_LARGE_ERROR = "Request too large"; // value referenced in JS
    public static final String INVALID_CONTENT_TYPE_ERROR = "File type not supported"; // value referenced in JS
    public static final String[] VALID_IMAGE_TYPES = {"image/gif", "image/jpeg", "image/png", "application/octet-stream"};
    public static final String PDF_TYPE = "application/pdf";

    public static final String REAL_ESTATE_AGENT_UPLOAD = "Real Estate Agent Upload";

    public static final String UPLOAD_TYPE_MEDIA = "media";
    public static final String UPLOAD_TYPE_ESP = "school_media";

    @Autowired
    private IMediaUploadDao _mediaUploadDao;

    @Autowired
    private IMediaFileDao _mediaFileDao;

    @Autowired
    private IUserDao _userDao;

    @Autowired
    private IAgentAccountDao _agentAccountDao;

    @Autowired
    private RealEstateAgentHelper _realEstateAgentHelper;

    protected static final Log _log = LogFactory.getLog(MediaUploadController.class);

    @RequestMapping(method = RequestMethod.POST, value = "realEstateAgentUpload.page")
    public void onAgentUpload (HttpServletRequest request,
                               HttpServletResponse response) {

        Integer userId = _realEstateAgentHelper.getUserId(request);

        if(userId != null && getAgentAccountDao().findAgentAccountByUserId(userId) != null) {
            User user = getUserDao().findUserFromId(userId);

            if(user == null || user.getId() == null) {
                error(response, "-1", UNAUTHORIZED_ERROR);
                return;
            }

            upload(request, response, user, REAL_ESTATE_AGENT_UPLOAD);
            return;
        }

        error(response, "-1", UNAUTHORIZED_ERROR);
        return;
    }

    public void upload (HttpServletRequest request, HttpServletResponse response, User user, String uploadType) {
        Map<String,String> formFields = new HashMap<String,String>();
        MediaUpload mediaUpload;
        List<MediaFile> mediaFiles = new ArrayList<MediaFile>();

        boolean mediaFileRecordInserted = false;
        boolean photoPassedOnSuccessfully = false;

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (isMultipart) {
            // not meant to be accurate. Error when content length (which mostly contains an image) is overly large
            int wiggleRoom = 100000; // actual content will be a little larger than the image
            if (request.getContentLength() > SchoolPhotoProcessor.MAX_PHOTO_BYTES + wiggleRoom) {
                error(response, "-1", REQUEST_TOO_LARGE_ERROR);
                return;
            }

            ServletFileUpload upload = new ServletFileUpload();

            try {

                FileItemIterator iter = upload.getItemIterator(request);
                while (iter.hasNext()) {
                    FileItemStream fileItemStream = iter.next();
                    if(!fileItemStream.isFormField()) {
                        if (ArrayUtils.contains(VALID_IMAGE_TYPES, fileItemStream.getContentType())) {

                            if(REAL_ESTATE_AGENT_UPLOAD.equals(uploadType) && fileItemStream != null && formFields.containsKey("mediaType")) {
                                Dimension dimension;
                                boolean isPhoto = false;
                                boolean isLogo = false;

                                try {
                                    if("photo".equals(formFields.get("mediaType"))) {
                                        dimension = Dimension.DIM_432_432;
                                        isPhoto = true;
                                    }
                                    else if("logo".equals(formFields.get("mediaType"))) {
                                        dimension = Dimension.DIM_324_324;
                                        isLogo = true;
                                    }
                                    else {
                                        throw new Exception();
                                    }
                                }
                                catch (Exception e) {
                                    _log.debug("Problem converting request param:", e);
                                    return;
                                }

                                mediaUpload = insertMediaUploadRecord(fileItemStream.getName());

                                MediaFile mediaFileForPreview = insertMediaFileRecord(mediaUpload, Dimension.DIM_100_100);
                                mediaFiles.add(mediaFileForPreview);

                                MediaFile mediaFileForPdf = insertMediaFileRecord(mediaUpload, dimension);
                                mediaFiles.add(mediaFileForPdf);

                                mediaFileRecordInserted = true;

                                photoPassedOnSuccessfully = processPhoto(fileItemStream, user, mediaUpload.getId(), UPLOAD_TYPE_MEDIA);

                                if(photoPassedOnSuccessfully) {
                                    updateAgentAccount(user.getId(), isPhoto, isLogo, mediaUpload);
                                }
                            }
                        }
                        else if(PDF_TYPE.equals(fileItemStream.getContentType())) {
                            if(fileItemStream != null) {
                                mediaFileRecordInserted = true;
                            }
                        }
                        else {
                            error(response, "-1", INVALID_CONTENT_TYPE_ERROR);
                            return;
                        }
                    } else {
                        // put multi-part form fields into a map for later use
                        formFields.put(fileItemStream.getFieldName(), IOUtils.toString(fileItemStream.openStream(), "UTF-8"));
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
            } finally {
                if (mediaFileRecordInserted && !photoPassedOnSuccessfully && !mediaFiles.isEmpty()) {
                    for(MediaFile mediaFile : mediaFiles) {
                        mediaFile.setStatus(MediaFileDaoHibernate.Status.ERROR.value);
                    }
                    ThreadLocalTransactionManager.commitOrRollback();
                }
            }
        } else {
            // Not a multi-part MIME request.
            error(response, "101", "Not a multi-part MIME request");
            return;
        }

        success(response);
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

    protected void success(HttpServletResponse response) {
        try {
            outputJson(response, RESP_SUCCESS);
        } catch (IOException e) {
            _log.debug("Error occured while trying to write to response: " + RESP_SUCCESS, e);
        }
    }

    private MediaUpload insertMediaUploadRecord(String fileName) {
        MediaUpload mediaUpload = new MediaUpload();
        mediaUpload.setOriginalFileName(fileName);
        getMediaUploadDao().save(mediaUpload);
        return mediaUpload;
    }

    private MediaFile insertMediaFileRecord(MediaUpload mediaUpload, Dimension dimension) {
        MediaFile mediaFile = new MediaFile();
        mediaFile.setMediaUpload(mediaUpload);
        mediaFile.setStatus(MediaFileDaoHibernate.Status.PENDING.value);
        mediaFile.setDimensions(dimension);
        getMediaFileDao().save(mediaFile);
        return mediaFile;
    }

    private boolean processPhoto (FileItemStream fileItemStream, User user, int uploadId, String uploadType) throws IOException{
        boolean photoPassedOnSuccessfully;
        PhotoProcessor processor = new PhotoProcessor(fileItemStream);
        processor.handleScaledPhoto(user, uploadId, uploadType);
        photoPassedOnSuccessfully = true;
        processor.finish();
        return photoPassedOnSuccessfully;
    }

    private void updateAgentAccount(int userId, boolean isPhoto, boolean isLogo, MediaUpload mediaUpload) {
        AgentAccount agentAccount = getAgentAccountDao().findAgentAccountByUserId(userId);
        if(isPhoto) {
            agentAccount.setPhotoMediaUpload(mediaUpload);
        }
        else if(isLogo) {
            agentAccount.setLogoMediaUpload(mediaUpload);
        }
        getAgentAccountDao().updateAgentAccount(agentAccount);
    }

    public IMediaUploadDao getMediaUploadDao() {
        return _mediaUploadDao;
    }

    public void setMediaDao(IMediaUploadDao _mediaDao) {
        this._mediaUploadDao = _mediaDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }

    public IMediaFileDao getMediaFileDao() {
        return _mediaFileDao;
    }

    public void setMediaFileDao(IMediaFileDao _mediaFileDao) {
        this._mediaFileDao = _mediaFileDao;
    }

    public IAgentAccountDao getAgentAccountDao() {
        return _agentAccountDao;
    }

    public void setAgentAccountDao(IAgentAccountDao _agentAccountDao) {
        this._agentAccountDao = _agentAccountDao;
    }

}
