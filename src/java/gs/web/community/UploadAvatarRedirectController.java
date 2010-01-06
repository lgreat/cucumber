package gs.web.community;

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
        return new ModelAndView("redirect:" + mAndV.getModel().get("redirect"));
    }

}
