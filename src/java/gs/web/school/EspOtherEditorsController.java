package gs.web.school;

import gs.data.community.User;
import gs.data.json.JSONObject;
import gs.data.school.*;
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

        Map debugInfo = new HashMap();

        //get a valid logged in user.
        User user = _espDashboardController.getValidUser(request);
        if (user == null) {
            return;
        }

        //get the user's membership.
        EspMembership membership = _espDashboardController.getEspMembershipForUser(user);
        if (membership == null) {
            return;
        }

        //Check to see that the post is from the logged in user by comparing the school and state.
        //TODO :If the user that is editing the form has multi-school access then this wont work.
        if (!(membership.getState().getAbbreviation().equals(state.getAbbreviation()) || (membership.getSchoolId() == schoolId))) {
            return;
        }

        //Add the new pre-approved user.
        //TODO right now this wont add more rows if already a approved or pre-approved for another school.What to do about Multi access?
        _espCreateUsersController.addUser(request, email, state, schoolId.toString(), firstName, lastName, jobTitle, debugInfo);

        //Get the new list of pre-approved users.
        //TODO This makes a call to get the school out of the DB.Definitely not required, since only a schoolId is required.
        //Therefore add a new method to the Dao to get getOtherEspMembersForSchoolId.This requires a
        //refactor of getOtherEspMembersForSchool.
        School school = _espDashboardController.getSchool(state, schoolId, new ModelMap());

        //TODO maybe there is another way to do this, without another database call for the new count of memberships.
        List<EspMembership> espMemberships = _espDashboardController.getOtherEspMembersForSchool(school, user);
        debugInfo.put("newOtherEspMemberships",espMemberships);

        JSONObject rval = new JSONObject(debugInfo);
        _cacheInterceptor.setNoCacheHeaders(response);
        response.setContentType("application/json");
        response.getWriter().print(rval.toString());
        response.getWriter().flush();
    }

}