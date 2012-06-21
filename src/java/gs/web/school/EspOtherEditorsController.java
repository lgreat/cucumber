package gs.web.school;

import gs.data.community.User;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.State;
import gs.web.admin.EspCreateUsersController;
import gs.web.util.HttpCacheInterceptor;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/official-school-profile/addPreApprovedMemberships.page")
public class EspOtherEditorsController implements ReadWriteAnnotationController {
    private static final Log _log = LogFactory.getLog(EspOtherEditorsController.class);
    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "schoolId";

    @Autowired
    private EspCreateUsersController _espCreateUsersController;

    @Autowired
    private EspDashboardController _espDashboardController;

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    HttpCacheInterceptor _cacheInterceptor = new HttpCacheInterceptor();

    @RequestMapping(method = RequestMethod.POST)
    public void add(HttpServletRequest request, HttpServletResponse response,
                    @RequestParam(value = PARAM_SCHOOL_ID, required = false) Integer schoolId,
                    @RequestParam(value = PARAM_STATE, required = false) State state,
                    @RequestParam(value = "email", required = false) String email,
                    @RequestParam(value = "firstName", required = false) String firstName,
                    @RequestParam(value = "lastName", required = false) String lastName,
                    @RequestParam(value = "jobTitle", required = false) String jobTitle
    ) throws Exception {

        Map<String, Object> debugInfo = new HashMap<String, Object>();

        //get a valid logged in user.
        User user = _espDashboardController.getValidUser(request);
        if (user == null) {
            writeErrorResponse(response, "noCookie");
            return;
        }

        // Make sure the user has membership to the school or is a superuser
        EspMembership membership = getEspMembershipForUserAndSchool(user, state, schoolId);
        if (membership == null && !user.hasRole(Role.ESP_SUPERUSER)) {
            writeErrorResponse(response, "noMembership");
            return;
        }

        //Add the new pre-approved user.
        // This won't add more rows if already a approved or pre-approved for another school. Users with multi-access
        // must contact our data team to get further approvals
        _espCreateUsersController.addUser(request, email, state, schoolId.toString(), firstName, lastName, jobTitle, debugInfo, user);

        JSONObject rval = new JSONObject();
        if (debugInfo.get("errorCode") != null) {
            rval.put("errorCode", debugInfo.get("errorCode"));
        } else {
            rval.put("success", true);
        }
        _cacheInterceptor.setNoCacheHeaders(response);
        response.setContentType("application/json");
        response.getWriter().print(rval.toString());
        response.getWriter().flush();
    }

    protected void writeErrorResponse(HttpServletResponse response, String errorCode) throws JSONException, IOException {
        JSONObject rval = new JSONObject();
        rval.put("errorCode", errorCode);
        _cacheInterceptor.setNoCacheHeaders(response);
        response.setContentType("application/json");
        rval.write(response.getWriter());
        response.getWriter().flush();
    }

    protected EspMembership getEspMembershipForUserAndSchool(User user, State schoolState, Integer schoolId) {
        if (user != null && user.hasRole(Role.ESP_MEMBER) && schoolState != null && schoolId != null) {
            return _espMembershipDao.findEspMembershipByStateSchoolIdUserId(schoolState, schoolId, user.getId(), true);
        }
        return null;
    }

}