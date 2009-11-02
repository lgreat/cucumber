package gs.web.community;

import gs.web.util.ReadWriteController;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class UploadAvatarHoverController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    private final int MAX_UPLOAD_SIZE = 1000000;

    @Override
    protected void onBindAndValidate(HttpServletRequest request, Object commandObj, BindException errors) throws Exception {
        super.onBindAndValidate(request, commandObj, errors);
        UploadAvatarCommand command = (UploadAvatarCommand) commandObj;
        if ((command.getAvatar() == null || command.getAvatar().length == 0)
                && StringUtils.isBlank(command.getStockPhoto())) {
            errors.rejectValue("avatar", null, "Please upload your own picture or select an image.");
        } else if (command.getAvatar() != null && command.getAvatar().length > MAX_UPLOAD_SIZE) {
            errors.rejectValue("avatar", null, "Maximum image size is 1 megabyte.");
        }
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object commandObj, BindException errors) throws Exception {
        UploadAvatarCommand command = (UploadAvatarCommand) commandObj;
        if (StringUtils.isNotBlank(command.getStockPhoto())) {
            _log.info("User chose stock photo #" + command.getStockPhoto());
        } else if (command.getAvatar() != null && command.getAvatar().length > 0) {
            _log.info("File upload succeeded: " + command.getAvatar().length + " bytes");
        } else {
            _log.warn("onSubmit reached without valid image!");
        }
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("closeHover", true);
        return new ModelAndView(getFormView(), model);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
           throws ServletException {
           // to actually be able to convert Multipart instance to byte[]
           // we have to register a custom editor
           binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
           // now Spring knows how to handle multipart object and convert them
       }

}