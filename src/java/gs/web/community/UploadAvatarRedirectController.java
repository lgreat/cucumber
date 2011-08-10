package gs.web.community;

import gs.web.util.UrlUtil;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class UploadAvatarRedirectController extends UploadAvatarHoverController {
    public static final String MAX_UPLOAD_SIZE_ERROR = "78";
    public static final String MIN_UPLOAD_SIZE_ERROR = "71";

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mAndV = super.handleRequest(request, response);
        
        Errors errors = (Errors) mAndV.getModel().get(BindException.MODEL_KEY_PREFIX + "uploadAvatarCommand");
        Boolean sizeLimit = request.getAttribute("sizeLimitExceeded") != null ? (Boolean) request.getAttribute("sizeLimitExceeded") : false;
        String redirectTo = String.valueOf(mAndV.getModel().get("redirect"));

        if (sizeLimit || (errors != null && errors.getFieldError("avatar") != null && errors.getFieldError("avatar").getCode() != null && errors.getFieldError("avatar").getCode().equals("maxLimit"))) {
            redirectTo = UrlUtil.addParameter(redirectTo, "errorCode=" + MAX_UPLOAD_SIZE_ERROR);
        } else if ((errors != null && errors.getFieldError("avatar") != null && errors.getFieldError("avatar").getCode() != null && errors.getFieldError("avatar").getCode().equals("minLimit"))) {
            redirectTo = UrlUtil.addParameter(redirectTo, "errorCode=" + MIN_UPLOAD_SIZE_ERROR);
        }
        return new ModelAndView("redirect:" + redirectTo);
    }
}
