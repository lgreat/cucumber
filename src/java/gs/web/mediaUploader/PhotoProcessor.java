package gs.web.mediaUploader;

import gs.data.community.User;
import gs.web.util.ClientHttpRequest;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 2/20/13
 * Time: 3:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhotoProcessor {
    protected static final Log _log = LogFactory.getLog(PhotoProcessor.class);

    public static final int BUFFER_SIZE = 4096;
    public static final String POST_URL_PROPERTY_KEY = "gs.avatarUploadURL";

    private BufferedInputStream _stream;
    private FileItemStream _fileItemStream;
    private URL _destinationUrl;
    public static final int MAX_PHOTO_BYTES = 1024 * 1024 * 2;

    private static final String UPLOAD_TYPE_KEY = "upload_type";
    private static final String MEDIA_UPLOAD_ID_KEY = "media_upload_id";

    public PhotoProcessor(FileItemStream fileItemStream) throws IOException {

        _fileItemStream = fileItemStream;

        _stream = new BufferedInputStream(fileItemStream.openStream());
        _stream.mark(0);
    }

    /**
     * Should be called when
     */
    public void finish() {
        try {
            _stream.close();
        } catch (IOException e) {
            _log.debug("Error when attempting to close stream.", e);
        }
    }

    public PhotoProcessor(FileItemStream fileItemStream, URL destinationUrl) {
        _fileItemStream = fileItemStream;
        _destinationUrl = destinationUrl;
    }

    /**
     * Handles a photo that should have already been scaled to the correct size
     *
     * @param user the User who submitted the photo
     * @param uploadId ID in the media_upload table
     * @return
     */
    protected void handleScaledPhoto(User user, int uploadId, String uploadType) throws IOException {
        _destinationUrl = getDestinationUrl(); // might throw IllegalStateException if system property not set

        if (_destinationUrl == null) {
            throw new IllegalStateException("Cannot handle photo because no URL is set");
        }

        _stream.reset();
        ClientHttpRequest clientHttpRequest = createClientHttpRequest(user, uploadId, uploadType);

        String result = clientHttpRequest.postAsString();
    }

    /**
     * Saves a file to local disk. Handy for testing
     * @param path
     * @throws IOException
     */
    protected void saveToDisk(String path) throws IOException {
        File localFile = new File(path + "/" + _fileItemStream.getName());
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(localFile));
        byte[] data = new byte[BUFFER_SIZE];
        _stream.reset();

        int count;
        while ((count = _stream.read(data, 0, BUFFER_SIZE)) != -1) {
            output.write(data, 0, count);
        }

        output.flush();
        output.close();
    }

    /**
     * Returns the URL of the place to send processed photos to
     * @return
     * @throws java.net.MalformedURLException
     */
    public URL getDestinationUrl() {
        URL url;
        try {
            url = new URL(System.getProperty(POST_URL_PROPERTY_KEY));
        } catch (MalformedURLException murle) {
            _log.error("Failed to determine avatar post url from " + POST_URL_PROPERTY_KEY +
                    " (value=" + System.getProperty(POST_URL_PROPERTY_KEY) + ").", murle);
            throw new IllegalStateException("Invalid state - failured to determine URL to send to.", murle);
        }
        return url;
    }

    protected ClientHttpRequest createClientHttpRequest(User user, int uploadId, String uploadType) throws IOException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (uploadId < 0) {
            throw new IllegalArgumentException("School media ID cannot be less than zero");
        }

        int imageCount = 1; // just uploading one image for esp/real estate agent upload

        _stream.reset();

        int wiggleRoom = 100000;
        ClientHttpRequest clientHttpRequest = new ClientHttpRequest(getDestinationUrl(), MAX_PHOTO_BYTES + wiggleRoom);

        if(MediaUploadController.UPLOAD_TYPE_MEDIA.equals(uploadType)) {
            clientHttpRequest.setParameter(MEDIA_UPLOAD_ID_KEY, uploadId);
            clientHttpRequest.setParameter(UPLOAD_TYPE_KEY, uploadType);
        }
        else if(MediaUploadController.UPLOAD_TYPE_ESP.equals(uploadType)) {
            clientHttpRequest.setParameter(UPLOAD_TYPE_KEY, uploadType);
            clientHttpRequest.setParameter("media_id", uploadId);
            clientHttpRequest.setParameter("numblobs", imageCount);
            clientHttpRequest.setParameter("user_id", user.getId());
        }

        clientHttpRequest.setParameter("blob" + imageCount, _fileItemStream.getName(), _stream);

        return clientHttpRequest;
    }

    private BufferedInputStream getStream() {
        return _stream;
    }
}
