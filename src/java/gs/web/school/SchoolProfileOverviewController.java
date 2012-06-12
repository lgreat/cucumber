package gs.web.school;

import gs.data.json.JSONException;
import gs.data.school.*;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;
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
@RequestMapping("/school/profileOverview.page")
public class SchoolProfileOverviewController extends AbstractSchoolProfileController {
    protected static final Log _log = LogFactory.getLog(SchoolProfileOverviewController.class.getName());

    String _viewName;

    @Autowired
    private IEspResponseDao _espResponseDao;


    @RequestMapping(method= RequestMethod.GET)
    public Map<String,Object> handle(HttpServletRequest request,
        HttpServletResponse response,
        @RequestParam(value = "schoolId", required = false) Integer schoolId,
        @RequestParam(value = "state", required = false) State state
    ) {

        Map<String,Object> model = new HashMap<String,Object>();
        School school = getSchool(request, state, schoolId);

        model.put("bestKnownFor", getBestKnownForQuote(school));


        return model;
    }

    public String getBestKnownForQuote(School school) {
        // get PQ data to find quote if it exists
        Set<String> pqKeys = new HashSet<String>(1);
        pqKeys.add("best_known_for");
        String bestKnownFor = null;
        List<EspResponse> espResponses = _espResponseDao.getResponsesByKeys(school, pqKeys);
        if (espResponses != null && espResponses.size() > 0) {
            bestKnownFor = espResponses.get(0).getSafeValue();
            if (StringUtils.isNotBlank(bestKnownFor)) {
                if (!StringUtils.endsWith(bestKnownFor, ".")) {
                    bestKnownFor += ".";
                }
            }
        }
        return bestKnownFor;
    }






    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}