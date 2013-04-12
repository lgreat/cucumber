package gs.web.school;

import gs.data.school.*;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;
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

/**
 * Shows the error page that can be triggered during espSignin or espRegistration
 */
@Controller
@RequestMapping("/official-school-profile/registrationError.page")
public class EspRegistrationErrorController {
    private static final Log _log = LogFactory.getLog(EspRegistrationErrorController.class);
    public static final String VIEW = "school/espRegistrationError";
    public static final String PARAM_MESSAGE = "message";
    public static final String PARAM_SCHOOL_ID = "schoolId";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_PROVISIONAL_USER_NAME = "provisionalUserName";

    @Autowired
    private ISchoolDao _schoolDao;

    @RequestMapping(method = RequestMethod.GET)
    public String showLandingPage(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam(value = PARAM_MESSAGE, required = true) String message,
                                  @RequestParam(value = PARAM_SCHOOL_ID, required = false) Integer schoolId,
                                  @RequestParam(value = PARAM_STATE, required = false) State state,
                                  @RequestParam(value = PARAM_PROVISIONAL_USER_NAME, required = false) String provisionalUserName) {

        modelMap.put("message", message);
        if (schoolId != null && state != null) {
            School school = _schoolDao.getSchoolById(state, schoolId);
            if (school != null && school.isActive()) {
                modelMap.put("school", school);
            }
        }
        if(StringUtils.isNotBlank(provisionalUserName)){
            modelMap.put("provisionalUserName", provisionalUserName);
        }
        return VIEW;
    }
}