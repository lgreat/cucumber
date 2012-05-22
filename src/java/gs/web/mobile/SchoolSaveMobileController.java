package gs.web.mobile;

import gs.data.json.JSONArray;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.Grades;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
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
@RequestMapping("/mobile/")
public class SchoolSaveMobileController implements ReadWriteAnnotationController {
    public static final String MY_SAVED_SCHOOLS_VIEW = "/mobile/savedSchools-mobile";

    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private IReviewDao _reviewDao;
    @Autowired
    private RatingHelper _ratingHelper;

    @RequestMapping (value="savedSchools.page", method= RequestMethod.GET)
    public String showSchoolList (HttpServletRequest request) {
        return MY_SAVED_SCHOOLS_VIEW;
    }

    @RequestMapping (value="savedSchools.page", method= RequestMethod.POST)
    public void showSchoolList (@RequestParam("savedSchoolsJson") String savedSchoolsJson,
                                HttpServletRequest request,
                                HttpServletResponse response) throws  JSONException, IOException {
        JSONArray savedSchoolsJsonArray;
        JSONObject schoolsResponseJson = new JSONObject();
        response.setContentType("application/json");

        try {
            JSONObject savedSchoolsJsonObject = new JSONObject(savedSchoolsJson, "UTF-8");
            savedSchoolsJsonArray = savedSchoolsJsonObject.getJSONArray("schools");
        }
        catch (JSONException ex) {
            schoolsResponseJson.accumulate("JsonError", true);
            schoolsResponseJson.write(response.getWriter());
            response.getWriter().flush();
            return;
        }
        int numEntriesInRequestJson = savedSchoolsJsonArray.length();
        int numSchoolsAddedToResponseJson = 0;

        schoolsResponseJson.accumulate("Schools", new HashMap<String, String>());

        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        boolean useCache = (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer());

        for(int i = 0; i < numEntriesInRequestJson; i++) {
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

            Ratings communityRatings = _reviewDao.findRatingsBySchool(school);
            Integer commRating = communityRatings.getOverall();
            schoolMap.put("commRating", commRating == null ? "" : Integer.toString(commRating));

            UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
            schoolMap.put("schoolUrl", urlBuilder.asFullUrlXml(request));

            Integer enrollment = null;
            try {
                enrollment = school.getEnrollmentOrCapacity();
            }
            catch (NullPointerException ex) {

            }
            schoolMap.put("enrollment", enrollment == null ? "" : Integer.toString(enrollment));

            Address address = school.getPhysicalAddress();
            schoolMap.put("address", address == null ? "" : address.toString());

            schoolsResponseJson.accumulate("Schools", schoolMap);
            numSchoolsAddedToResponseJson++;
        }

        schoolsResponseJson.accumulate("NumSavedSchools", numSchoolsAddedToResponseJson);
        schoolsResponseJson.write(response.getWriter());
        response.getWriter().flush();
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public RatingHelper getRatingHelper() {
        return _ratingHelper;
    }

    public void setRatingHelper(RatingHelper ratingHelper) {
        _ratingHelper = ratingHelper;
    }
}
