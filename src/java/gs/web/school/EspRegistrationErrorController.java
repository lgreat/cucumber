package gs.web.school;

import gs.data.community.User;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.State;
import gs.web.community.HoverHelper;
import gs.web.util.SitePrefCookie;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SubCookie;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Shows the error page that can be triggered during espSignin or espRegistration
 */
@Controller
@RequestMapping("/official-school-profile/registrationError.page")
public class EspRegistrationErrorController {
    private static final Log _log = LogFactory.getLog(EspRegistrationErrorController.class);
    public static final String VIEW = "school/espRegistrationError";
    public static final String PARAM_MESSAGE = "message";

    @RequestMapping(method = RequestMethod.GET)
    public String showLandingPage(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam(value=PARAM_MESSAGE, required=true) String message) {

        modelMap.put("message", message);

        return VIEW;
    }

}