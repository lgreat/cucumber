package gs.web.photoUploader;

import gs.data.community.User;
import gs.web.util.ClientHttpRequest;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;


public class SchoolPhotoProcessor {
    protected static final Log _log = LogFactory.getLog(SchoolPhotoProcessor.class);

    public static final int BUFFER_SIZE = 4096;
    public static final String POST_URL_PROPERTY_KEY = "gs.avatarUploadURL";
    
    private BufferedInputStream _stream;
    private FileItemStream _fileItemStream;
    private URL _destinationUrl;
    
    private static final String UPLOAD_TYPE_ESP = "school_media";

    public SchoolPhotoProcessor(FileItemStream fileItemStream) throws IOException {


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
    
    public SchoolPhotoProcessor(FileItemStream fileItemStream, URL destinationUrl) {
        _fileItemStream = fileItemStream;
        _destinationUrl = destinationUrl;
    }

    /**
     * Handles a photo that should have already been scaled to the correct size
     *
     * @param user the User who submitted the photo
     * @param schoolMediaId ID of the school media row for this photo
     * @return
     */
    protected void handleScaledPhoto(User user, int schoolMediaId) {
        _destinationUrl = getDestinationUrl(); // might throw IllegalStateException if system property not set

        if (_destinationUrl == null) {
            throw new IllegalStateException("Cannot handle photo because no URL is set");
        }

        try {
            _stream.reset();
            ClientHttpRequest clientHttpRequest = createClientHttpRequestForEsp(user, schoolMediaId);
            
            String result = clientHttpRequest.postAsString();
        } catch (IOException e) {
            _log.debug("Problem while attempting to send photo to " + _destinationUrl.toString());
        }
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
     * @throws MalformedURLException
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

    protected ClientHttpRequest createClientHttpRequestForEsp(User user, int schoolMediaId) throws IOException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (schoolMediaId < 0) {
            throw new IllegalArgumentException("School media ID cannot be less than zero");
        }

        int imageCount = 1; // just uploading one image for esp upload

        _stream.reset();

        ClientHttpRequest clientHttpRequest = new ClientHttpRequest(getDestinationUrl());
        clientHttpRequest.setParameter("upload_type", UPLOAD_TYPE_ESP);
        clientHttpRequest.setParameter("school_media_id", schoolMediaId);
        clientHttpRequest.setParameter("numblobs", imageCount);
        clientHttpRequest.setParameter("user_id", user.getId());
        clientHttpRequest.setParameter("blob" + imageCount, _fileItemStream.getName(), _stream);
        return clientHttpRequest;
    }

    private BufferedInputStream getStream() {
        return _stream;
    }

}
