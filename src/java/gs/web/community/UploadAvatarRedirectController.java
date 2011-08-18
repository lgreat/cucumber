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
    
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mAndV = super.handleRequest(request, response);
        
        Errors errors = (Errors) mAndV.getModel().get(BindException.MODEL_KEY_PREFIX + "uploadAvatarCommand");
        String redirectTo = String.valueOf(mAndV.getModel().get("redirect"));

        if (errors != null && errors.getFieldError("avatar") != null) {
            String errorCode = errors.getFieldError("avatar").getCode();
            redirectTo = UrlUtil.addParameter(redirectTo, "errorCode=" + errorCode);
        }

        return new ModelAndView("redirect:" + redirectTo);
    }
}
