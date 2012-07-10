package gs.web.school;

import gs.data.json.JSONArray;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/school/schoolDetails.page")
public class SchoolDetailsAjaxController implements ReadWriteAnnotationController {
    private static final Logger _log = Logger.getLogger(SchoolDetailsAjaxController.class);

    @Autowired
    private ISchoolDao _schoolDao;

    @RequestMapping(method = RequestMethod.POST)
    public void getSchoolDetails(@RequestParam("schoolIdsAndStates") String schoolIdsAndStatesJson,
                                 @RequestParam("responseFormat") String responseFormat,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws JSONException, IOException {

        List<School> schools = getSchools(schoolIdsAndStatesJson);
        if (responseFormat.equals("json")) {
            getSchoolDetailsJson(schools, request, response);
        }
    }

    protected List<School> getSchools(String schoolIdsAndStatesJson) throws JSONException, IOException {
        JSONArray schoolIdsAndStatesJsonArray;
        List<School> schools = new ArrayList<School>();

        try {
            JSONObject schoolIdsAndStatesJsonObj = new JSONObject(schoolIdsAndStatesJson, "UTF-8");
            schoolIdsAndStatesJsonArray = schoolIdsAndStatesJsonObj.getJSONArray("schools");
        } catch (JSONException ex) {
            _log.error("Error Parsing schoolIds and states JSON:" + ex);
            return schools;
        }

        for (int i = 0; i < schoolIdsAndStatesJsonArray.length(); i++) {
            JSONObject schoolIdAndState = (JSONObject) schoolIdsAndStatesJsonArray.get(i);

            try {
                State state = State.fromString((String) schoolIdAndState.get("state"));
                Integer schoolId = Integer.parseInt((String) schoolIdAndState.get("schoolId"));
                School school = _schoolDao.getSchoolById(state, schoolId);
                if (school != null && school.isActive()) {
                    schools.add(school);
                }

            } catch (Exception ex) {
                _log.error("Exception while fetching school." + ex);
                continue;
            }
        }
        return schools;
    }

    protected void getSchoolDetailsJson(List<School> schools, HttpServletRequest request, HttpServletResponse response) throws JSONException, IOException {
        JSONObject schoolsResponseJson = new JSONObject();
        response.setContentType("application/json");
        JSONArray schoolsJsonArray = new JSONArray();

        for (School school : schools) {
            Map<String, String> schoolMap = new HashMap<String, String>();

            schoolMap.put("schoolId", school.getId().toString());
            schoolMap.put("state", school.getStateAbbreviation().getAbbreviation());
            schoolMap.put("name", school.getName());

            schoolMap.put("type", school.getType() == null ? "" : school.getType().getName());
            schoolMap.put("gradeRange", school.getGradeLevels() == null ? "" : school.getGradeLevels().getRangeString());
            schoolMap.put("city", school.getCity() == null ? "" : school.getCity());
            UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
            schoolMap.put("schoolUrl", urlBuilder.asFullUrlXml(request));

            schoolsJsonArray.put(schoolMap);
        }
        schoolsResponseJson.put("schools", schoolsJsonArray);
        schoolsResponseJson.write(response.getWriter());
        response.getWriter().flush();
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

}
