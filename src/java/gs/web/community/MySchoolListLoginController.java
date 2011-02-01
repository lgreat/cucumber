package gs.web.community;

import gs.web.util.*;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MySchoolListLoginController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String BEAN_NAME = "/mySchoolListLogin.page";

    // Intercept the request in order to had the P3P header to all repsonses from this controller.
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.addHeader("P3P", "CP=\"CAO PSA OUR\"");
        UrlBuilder mslUrl = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST);
        // GS-7623 Pass back any command parameters to the MSL controller
        if (request.getParameter(MySchoolListController.PARAM_COMMAND) != null) {
            mslUrl.addParameter(MySchoolListController.PARAM_COMMAND,
                    request.getParameter(MySchoolListController.PARAM_COMMAND));
            mslUrl.addParameter(MySchoolListController.PARAM_SCHOOL_IDS,
                    request.getParameter(MySchoolListController.PARAM_SCHOOL_IDS));
            mslUrl.addParameter(MySchoolListController.PARAM_STATE,
                    request.getParameter(MySchoolListController.PARAM_STATE));
        }

        String redirectUrl;
        if (!PageHelper.isMemberAuthorized(request)) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, (String)null);
            urlBuilder.setParameter("redirect", mslUrl.asSiteRelative(request));
            redirectUrl = urlBuilder.asSiteRelative(request);
        } else {
            redirectUrl = mslUrl.asSiteRelative(request);
        }
        return new ModelAndView(new RedirectView301(redirectUrl));
    }

}
