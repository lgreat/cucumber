package gs.web.community;

import gs.data.json.JSONArray;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.Grades;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.review.IReviewDao;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.school.RatingHelper;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 5/10/12
 * Time: 8:46 AM
 * To change this template use File | Settings | File Templates.
 */

@Controller
@RequestMapping("/community/")
public class SchoolSaveMobileController implements ReadWriteAnnotationController {
    public static final String MY_SAVED_SCHOOLS_VIEW = "/community/mySchoolList-mobile";

    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private IReviewDao _reviewDao;
    @Autowired
    private RatingHelper _ratingHelper;

    @RequestMapping (value="mySchoolList-mobile.page", method= RequestMethod.GET)
    public String showSchoolList (HttpServletRequest request) {
        return MY_SAVED_SCHOOLS_VIEW;
    }

    @RequestMapping (value="mySchoolList-mobile.page", method= RequestMethod.POST)
    public void showSchoolList (@RequestParam("savedSchoolsJson") String savedSchoolsJson,
                                HttpServletRequest request,
                                HttpServletResponse response) throws  JSONException, IOException {
        JSONObject savedSchoolsJsonObject = new JSONObject(savedSchoolsJson, "UTF-8");
        JSONArray savedSchoolsJsonArray = savedSchoolsJsonObject.getJSONArray("schools");
        int numOfSavedSchools = savedSchoolsJsonArray.length();

        JSONObject schoolsResponseJson = new JSONObject();
        schoolsResponseJson.accumulate("Schools", new HashMap<String, String>());

        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        boolean useCache = (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer());

        for(int i = 0; i < numOfSavedSchools; i++) {
            JSONObject savedSchool = (JSONObject) savedSchoolsJsonArray.get(i);
            Map<String, String> schoolMap = new HashMap<String, String>();
            School school = new School();

            try {
                State state = State.fromString((String) savedSchool.get("state"));
                Integer schoolId = Integer.parseInt((String) savedSchool.get("id"));

                school = _schoolDao.getSchoolById(state, schoolId);
            }
            catch (Exception ex) {

            }

            if(!school.isActive()) {
                continue;
            }

            schoolMap.put("id", (String) savedSchool.get("id"));
            schoolMap.put("state", school.getStateAbbreviation().getAbbreviation());
            schoolMap.put("name", school.getName());

            SchoolType schoolType = school.getType();
            schoolMap.put("type", schoolType == null ? "" : schoolType.getName());

            Grades grades = school.getGradeLevels();
            schoolMap.put("gradeLevels", grades == null ? "" : grades.getRangeString());

            schoolMap.put("city", school.getCity() == null ? "" : school.getCity());

            Integer gsRating = _ratingHelper.getGreatSchoolsOverallRating(school, useCache);
            schoolMap.put("gsRating", gsRating == null ? "" : Integer.toString(gsRating));

            Integer commRating = _reviewDao.findRatingsBySchool(school).getOverall();
            schoolMap.put("commRating", commRating == null ? "" : Integer.toString(commRating));

            UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
            schoolMap.put("schoolUrl", urlBuilder.asFullUrlXml(request));

            Integer enrollment = school.getEnrollment();
            schoolMap.put("enrollment", enrollment == null ? "" : Integer.toString(enrollment));

            Address address = school.getPhysicalAddress();
            schoolMap.put("address", address == null ? "" : address.toString());

            schoolsResponseJson.accumulate("Schools", schoolMap);
        }

        schoolsResponseJson.accumulate("NumSavedSchools", numOfSavedSchools);
        schoolsResponseJson.write(response.getWriter());
        response.getWriter().flush();
    }
}
