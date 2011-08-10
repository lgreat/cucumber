package gs.web.community;

import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.ClientHttpRequest;
import gs.web.util.SitePrefCookie;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.User;
import gs.data.community.IUserDao;
import gs.data.util.CommunityUtil;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.imageio.ImageIO;
import javax.naming.OperationNotSupportedException;
import java.util.Map;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class UploadAvatarHoverController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    /** Largest image size in bytes to allow to be uploaded.  */
    private final int MAX_UPLOAD_SIZE_BYTES = 1000000;
    public static final String POST_URL_PROPERTY_KEY = "gs.avatarUploadURL";
    public static final String SYNCHRONOUS_RESPONSE = "ASUCCESS";
    public static final String ASYNCHRONOUS_RESPONSE = "SUCCESS";
    /** Images larged than this will be scaled down to this size.  This should be larger than we ever anticipate
     * our avatars being. */
    public static final int MAX_IMAGE_DIMENSIONS_PIXELS = 600;
    public static final int MIN_IMAGE_DIMENSIONS_PIXELS = 95;
    protected static final String SIZE_LIMIT_EXCEEDED = "sizeLimitExceeded";
    private static final String MODEL_STOCK_AVATAR_URL_PREFIX = "stockAvatarUrlPrefix";
    private IUserDao _userDao;
    private CommonsMultipartResolver _multipartResolver;

    private void logDuration(long durationInMillis, String eventName) {
        _log.info(eventName + " took " + durationInMillis + " milliseconds");
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long startTime = System.currentTimeMillis();
        ModelAndView mAndV;
        // add redirect to model for the subclass UploadAvatarRedirectController
        try {
            if (_multipartResolver.isMultipart(request)) {
                HttpServletRequest mpRequest = _multipartResolver.resolveMultipart(request);
                mAndV = super.handleRequest(mpRequest, response);
                mAndV.getModel().put("redirect", mpRequest.getParameter("redirect"));
            } else {
                mAndV = super.handleRequest(request, response);
                mAndV.getModel().put("redirect", request.getParameter("redirect"));
            }
        } catch (MaxUploadSizeExceededException musee) {
            request.setAttribute(SIZE_LIMIT_EXCEEDED, true);
            mAndV = super.handleRequest(request, response);
            mAndV.getModel().put("redirect", request.getParameter("redirect"));
        }
        logDuration(System.currentTimeMillis() - startTime, "UploadAvatarHoverController handleRequest");

        return mAndV;
    }

    @Override
    protected void onBindAndValidate(HttpServletRequest request, Object commandObj, BindException errors) throws Exception {
        super.onBindAndValidate(request, commandObj, errors);
        UploadAvatarCommand command = (UploadAvatarCommand) commandObj;
        if (request.getAttribute(SIZE_LIMIT_EXCEEDED) != null) {
            errors.rejectValue("avatar", null, "Maximum image size is 1 megabyte.");            
        } else if (command.getAvatar() == null) {
            if (StringUtils.isBlank(command.getStockPhoto()) || !isValidStockPhoto(command.getStockPhoto())) {
                errors.rejectValue("avatar", null, "Please upload your own picture or select an image.");
            }
        } else {
            if (command.getAvatar().getSize() > MAX_UPLOAD_SIZE_BYTES) {
                errors.rejectValue("avatar", "maxLimit", "Maximum image size is 1 megabyte.");
            } else if (command.getAvatar().getSize() == 0) {
                errors.rejectValue("avatar", null, "Invalid image.");
            } else if (command.getAvatar().getContentType() == null ||
                        !(command.getAvatar().getContentType().equals("image/jpeg") ||
                          command.getAvatar().getContentType().equals("image/gif") ||
                          command.getAvatar().getContentType().equals("image/pjpeg"))) {
                errors.rejectValue("avatar", null, "Image must be a jpeg or gif.");
            } else {
                BufferedImage incomingImage = ImageIO.read(command.getAvatar().getInputStream());
                if (incomingImage == null) {
                    errors.rejectValue("avatar", null, "The file does not appear to be a valid image.");
                } else {
                    if (incomingImage.getWidth() < MIN_IMAGE_DIMENSIONS_PIXELS || incomingImage.getHeight() < MIN_IMAGE_DIMENSIONS_PIXELS) {
                        errors.rejectValue("avatar", "minLimit", "Minimum image dimensions are 95x95 pixels.");
                    } else {
                        // Remove the file from the command object to save memory
                        command.setAvatar(null);
                        command.setImage(incomingImage); // this is for onSubmit
                    }
                }
            }
        }
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object commandObj, BindException errors) throws Exception {
        if (PageHelper.isMemberAuthorized(request)) {
            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            User uploader = sessionContext.getUser();

            UploadAvatarCommand command = (UploadAvatarCommand) commandObj;
            if (StringUtils.isNotBlank(command.getStockPhoto())) {
                uploader.getUserProfile().setAvatarType(command.getStockPhoto());
                getUserDao().updateUser(uploader);
            } else if (command.getImage() != null) {
                BufferedImage incomingImage = command.getImage();

                // Scaling in java is MUCH faster if the image is RGB, and also crop the image to square if necessary
                incomingImage = preProcessImage(incomingImage);

                if (incomingImage.getWidth() > MAX_IMAGE_DIMENSIONS_PIXELS || incomingImage.getHeight() > MAX_IMAGE_DIMENSIONS_PIXELS) {
                    incomingImage = getScaledInstance(incomingImage,
                                                        MAX_IMAGE_DIMENSIONS_PIXELS, MAX_IMAGE_DIMENSIONS_PIXELS,
                                                        RenderingHints.VALUE_INTERPOLATION_BILINEAR,
                                                        true);
                }

                String postResponse = postImages(incomingImage, uploader);
                if (StringUtils.equals(SYNCHRONOUS_RESPONSE, postResponse)) {
                    SitePrefCookie sitePrefCookie = new SitePrefCookie(request, response);
                    sitePrefCookie.setProperty("avatarAlertType", "sync");
                } else if (StringUtils.equals(ASYNCHRONOUS_RESPONSE, postResponse)) {
                    SitePrefCookie sitePrefCookie = new SitePrefCookie(request, response);
                    sitePrefCookie.setProperty("avatarAlertType", "async");
                } else {
                    _log.error("Error posting to content upload service: " + postResponse);
                    // error
                    SitePrefCookie sitePrefCookie = new SitePrefCookie(request, response);
                    sitePrefCookie.setProperty("avatarAlertType", "error");
                }
            } else {
                _log.warn("onSubmit reached without valid image!");
            }
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("closeHover", true);
            return new ModelAndView(getFormView(), model);
        } else {
            throw new OperationNotSupportedException("Cannot upload avatar without a user");
        }
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
           throws ServletException {
           // to actually be able to convert Multipart instance to byte[]
           // we have to register a custom editor
           binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
           // now Spring knows how to handle multipart object and convert them
    }

    @Override
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors)
           throws Exception {
        Map<String, String> model = new HashMap<String, String>();

        model.put(MODEL_STOCK_AVATAR_URL_PREFIX, CommunityUtil.getAvatarURLPrefix() + "stock/");
        return model;
    }

    protected boolean isValidStockPhoto(String photoKey) {
        // TODO validate that the posted photoKey is valid for a stock photo
        return true;
    }

    protected String postImages(BufferedImage source, User user) {
        URL url = null;
        try {
            url = new URL(System.getProperty(POST_URL_PROPERTY_KEY));
        } catch (MalformedURLException murle) {
            _log.error("Failed to determine avatar post url from " + POST_URL_PROPERTY_KEY +
                    " (value=" + System.getProperty(POST_URL_PROPERTY_KEY) + ").", murle);
        }
        if (url == null) {
            return null;
        }

        String dir = CommunityUtil.getAvatarUploadFolder(user.getId());

        try {
            ClientHttpRequest clientHttpRequest = new ClientHttpRequest(url);
            clientHttpRequest.setParameter("upload_type", "avatar");
            clientHttpRequest.setParameter("numblobs", 2);
            clientHttpRequest.setParameter("user_id", user.getId());

            // All images are stored as jpg because Java 1.5 doesn't have a default gif writing plugin.
            scaleAndSetImageParameter(source, 48, "image/jpeg", "jpg", user, dir, clientHttpRequest, 1);
            scaleAndSetImageParameter(source, 95, "image/jpeg", "jpg", user, dir, clientHttpRequest, 2);

            _log.info("Posting image to " + url);
            return clientHttpRequest.postAsString();
        } catch (IOException ioe) {
            _log.error("Failed to post avatar.", ioe);
        }
        return null;
    }

    protected void scaleAndSetImageParameter(BufferedImage source, int size, String mimetype, String formatName,
                                             User user, String dir, ClientHttpRequest clientHttpRequest,
                                             int paramNum) throws IOException {
        BufferedImage thumb = getScaledInstance(source,
                size, size,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR,
                true);

        String destFilename = CommunityUtil.getAvatarFilename(user.getId(), size, formatName);

        clientHttpRequest.setParameter("blob" + paramNum, destFilename, mimetype, formatName, thumb);
        clientHttpRequest.setParameter("path" + paramNum, dir + destFilename);
    }


/*
    protected void storeImage(BufferedImage source, int size, User user) {
        BufferedImage thumb = getScaledInstance(source,
                                                size, size,
                                                RenderingHints.VALUE_INTERPOLATION_BILINEAR,
                                                true);

        try {
            int path1 = user.getId() % 100;
            int path2 = ((user.getId() % 10000) - path1) / 100;
            if (ImageIO.write(thumb, "jpg", new File("C:\\dev\\GSWeb\\src\\webapp\\res\\img\\avatar\\custom\\" + path1 + "\\" + path2 + "\\" + user.getId() + "-" + size + ".jpg"))) {
                _log.info("  Wrote " + size + " image here: C:\\dev\\GSWeb\\src\\webapp\\res\\img\\avatar\\custom\\" + path1 + "\\" + path2 + "\\" + user.getId() + "-" + size + ".jpg");
            } else {
                _log.info("  Failed to store image.");
            }
        } catch (IOException ioe) {
            _log.info("  Failed to store image.", ioe);
        }
    }
*/
    
    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img           the original image to be scaled
     * @param targetWidth   the desired width of the scaled instance,
     *                      in pixels
     * @param targetHeight  the desired height of the scaled instance,
     *                      in pixels
     * @param hint          one of the rendering hints that corresponds to
     *                      {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *                      {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *                      {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *                      {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *                      scaling technique that provides higher quality than the usual
     *                      one-step technique (only useful in downscaling cases, where
     *                      {@code targetWidth} or {@code targetHeight} is
     *                      smaller than the original dimensions, and generally only when
     *                      the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    protected BufferedImage getScaledInstance(BufferedImage img,
                                           int targetWidth,
                                           int targetHeight,
                                           Object hint,
                                           boolean higherQuality) {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
                BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

    /**
     * Conver the passed image into an RGB format, and also crop it if it isn't square.
     *
     * @param source Image to process
     * @return processed image
     */
    protected BufferedImage preProcessImage(BufferedImage source) {
        int w = source.getWidth();
        int h = source.getHeight();
        
        int dim;
        if (w == h) {
            // If it is already square keep the new square the same size
            dim = w;
        } else {
            if (h > w) {
                dim = w;
                // Crop off an equal amount of the top and bottom of the pic to make it square
                source = source.getSubimage(0, (h-w)/2, dim, dim);
            } else {
                dim = h;
                // Crop off an equal amount of the right and left of the pic to make it square
                source = source.getSubimage((w-h)/2, 0, dim, dim);
            }
        }
        
        BufferedImage target = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = target.createGraphics();
        // Paint the background white so that when converted to jpg the transparent pixels will have some neutral and
        // default color
        g.drawImage(source, 0, 0, source.getWidth(), source.getHeight(), Color.WHITE, null);
        g.dispose();
        return target;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public CommonsMultipartResolver getMultipartResolver() {
        return _multipartResolver;
    }

    public void setMultipartResolver(CommonsMultipartResolver multipartResolver) {
        _multipartResolver = multipartResolver;
    }
}