package gs.web.admin;

import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.IEspMembershipDao;
import gs.data.school.IEspResponseDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author aroy@greatschools.org
 */
@Controller
@RequestMapping("/admin/status/")
public class OfficialSchoolProfileStatsController {
    @Autowired private IEspResponseDao _espResponseDao;
    @Autowired private IEspMembershipDao _espMembershipDao;
    @Autowired private StateManager _stateManager;

    @RequestMapping(value="osp.page", method= RequestMethod.GET)
    public String monitor(ModelMap modelMap) {
        modelMap.put("states", _stateManager.getSortedAbbreviations());
        return "admin/ospStats";
    }

    @RequestMapping(value="ospAjax.page", method= RequestMethod.GET)
    public void ajax(HttpServletRequest request, HttpServletResponse response) throws JSONException, IOException {
        response.setContentType("application/json");
        
        State state = State.fromString(request.getParameter("state"));
        
        int totalOsp = _espResponseDao.getNumSchools(state, false, false);
        int totalNewOsp = _espResponseDao.getNumSchools(state, true, false);
        int totalContributingMembers = _espResponseDao.getNumUsers(state, true);
        int activeMembers = _espMembershipDao.countActiveMembers(state);

        JSONObject output = new JSONObject();
        output.put("total", totalOsp);
        output.put("totalNew", totalNewOsp);
        output.put("totalContributingMembers", totalContributingMembers);
        output.put("activeMembers", activeMembers);
        output.write(response.getWriter());
    }

}
