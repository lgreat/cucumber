package gs.web.mobile;

import gs.data.json.JSONException;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.census.ICensusInfo;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.school.RatingHelper;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.io.IOException;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.createStrictMock;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 5/14/12
 * Time: 8:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class SchoolSaveMobileTestController extends BaseControllerTestCase {
    private SchoolSaveMobileController _schoolSaveMobileController;

    private ISchoolDao _schoolDao;
    private RatingHelper _ratingHelper;
    private IReviewDao _reviewDao;
    private ICensusInfo _censusInfo;

    private School _school;

    public void setUp() throws Exception {
        super.setUp();
        _schoolSaveMobileController = new SchoolSaveMobileController();

        _schoolDao = createStrictMock(ISchoolDao.class);
        _ratingHelper = createStrictMock(RatingHelper.class);
        _reviewDao = createStrictMock(IReviewDao.class);
        _censusInfo = createMock(ICensusInfo.class);

        _schoolSaveMobileController.setSchoolDao(_schoolDao);
        _schoolSaveMobileController.setRatingHelper(_ratingHelper);
        _schoolSaveMobileController.setReviewDao(_reviewDao);

        _school = setSampleSchool(124, State.CA, "Ellis Elementary School",
                "Sunnyvale", true, _reviewDao, _censusInfo);
    }

    public void replayAll() {
        super.replayMocks(_schoolDao, _ratingHelper, _reviewDao, _censusInfo);
    }

    public void verifyAll() {
        super.verifyMocks(_schoolDao, _ratingHelper, _reviewDao, _censusInfo);
    }

    public void resetAll() {
        super.resetMocks(_schoolDao, _ratingHelper, _reviewDao, _censusInfo);
    }

    public void testShowSchoolListSuccess() throws JSONException, IOException {
        String json = "{'schools':[{},{'state':'CA','id':'124'}]}";
        Ratings ratings = new Ratings();

        String expectedJsonResponse = "{\"NumSavedSchools\":1,\"Schools\":[{},{\"gradeLevels\":\"\",\"commRating\":\"\"," +
                "\"type\":\"public\",\"gsRating\":\"7\",\"schoolUrl\":\"http://www.greatschools.org/california/sunnyvale/124-Ellis-Elementary-School/\"," +
                "\"address\":\"null, \\nSunnyvale, CA  null\",\"state\":\"CA\",\"enrollment\":\"4\"," +
                "\"name\":\"Ellis Elementary School\",\"city\":\"Sunnyvale\",\"id\":\"124\"}]}";
        resetAll();

        expect(_schoolDao.getSchoolById(State.CA, 124)).andReturn(_school);
        expect(_ratingHelper.getGreatSchoolsOverallRating(_school, false)).andReturn(7);
        expect(_reviewDao.findRatingsBySchool(_school)).andReturn(ratings);
        expect(_censusInfo.getEnrollmentAsInteger(_school)).andReturn(4);

        replayAll();

        _schoolSaveMobileController.showSchoolList(json, getRequest(), getResponse());

        verifyAll();

        assertEquals("application/json", getResponse().getContentType());
        assertEquals(expectedJsonResponse, getResponse().getContentAsString());
    }

    public void testShowSchoolListWithIncorrectJsonFormat() throws JSONException, IOException {
        /*throws JSONException.*/
        String json = "'schools':[{}]";
        String expectedJsonResponse = "{\"JsonError\":true}";

        resetAll();

        replayAll();
        _schoolSaveMobileController.showSchoolList(json, getRequest(), getResponse());

        verifyAll();

//        assertEquals(0, getResponse().getContentLength());
        assertEquals(expectedJsonResponse, getResponse().getContentAsString());

        /* key "school" does not exist */
        json = "{'school':[{'state':'CA','id':'124'}]}";
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        resetAll();

        replayAll();
        _schoolSaveMobileController.showSchoolList(json, request, response);

        verifyAll();

//        assertEquals(0, getResponse().getContentLength());
        assertEquals(expectedJsonResponse, getResponse().getContentAsString());
    }


    public void testShowSchoolListNonExistingSchool() throws JSONException, IOException {
        /* skips the first school in the request json (non existing school + state), adds only the second entry */
        String json = "{'schools':[{'state':'CA','id':'125'},{'state':'CA','id':'124'}]}";
        Ratings ratings = new Ratings();
        String expectedJsonResponse = "{\"NumSavedSchools\":1,\"Schools\":[{},{\"gradeLevels\":\"\",\"commRating\":\"\"," +
                "\"type\":\"public\",\"gsRating\":\"7\",\"schoolUrl\":\"http://www.greatschools.org/california/sunnyvale/124-Ellis-Elementary-School/\"," +
                "\"address\":\"null, \\nSunnyvale, CA  null\",\"state\":\"CA\",\"enrollment\":\"4\"," +
                "\"name\":\"Ellis Elementary School\",\"city\":\"Sunnyvale\",\"id\":\"124\"}]}";

        resetAll();

        expect(_schoolDao.getSchoolById(State.CA, 125)).andThrow(new ObjectRetrievalFailureException("Schools not found",
                new Throwable()));
        expect(_schoolDao.getSchoolById(State.CA, 124)).andReturn(_school);
        expect(_ratingHelper.getGreatSchoolsOverallRating(_school, false)).andReturn(7);
        expect(_reviewDao.findRatingsBySchool(_school)).andReturn(ratings);
        expect(_censusInfo.getEnrollmentAsInteger(_school)).andReturn(4);

        replayAll();
        _schoolSaveMobileController.showSchoolList(json, getRequest(), getResponse());
        verifyAll();

        assertEquals(expectedJsonResponse, getResponse().getContentAsString());
    }

    public void testShowSchoolListWithNoCensusInfo() throws JSONException, IOException {
        /* returns json with blank value for enrollemnt for the school that had Census Info as null */
        School school = setSampleSchool(124, State.CA, "Ellis Elementary School",
                "Sunnyvale", true, _reviewDao, null);
        String json = "{'schools':[{},{'state':'CA','id':'124'}]}";
        Ratings ratings = new Ratings();
        String expectedJsonResponse = "{\"NumSavedSchools\":1,\"Schools\":[{},{\"gradeLevels\":\"\",\"commRating\":\"\"," +
                "\"type\":\"public\",\"gsRating\":\"7\",\"schoolUrl\":\"http://www.greatschools.org/california/sunnyvale/124-Ellis-Elementary-School/\"," +
                "\"address\":\"null, \\nSunnyvale, CA  null\",\"state\":\"CA\",\"enrollment\":\"\"," +
                "\"name\":\"Ellis Elementary School\",\"city\":\"Sunnyvale\",\"id\":\"124\"}]}";
        resetAll();

        expect(_schoolDao.getSchoolById(State.CA, 124)).andReturn(school);
        expect(_ratingHelper.getGreatSchoolsOverallRating(school, false)).andReturn(7);
        expect(_reviewDao.findRatingsBySchool(school)).andReturn(ratings);

        replayAll();
        _schoolSaveMobileController.showSchoolList(json, getRequest(), getResponse());
        verifyAll();

        assertEquals(expectedJsonResponse, getResponse().getContentAsString());
    }

    public School setSampleSchool(int id, State state, String name, String city,
                                  boolean isActive, IReviewDao reviewDao, ICensusInfo censusInfo) {
        School school = new School();
        school.setId(id);
        school.setStateAbbreviation(state);
        school.setName(name);
        school.setCity(city);
        school.setActive(isActive);
        school.setReviewDao(reviewDao);
        school.setCensusInfo(censusInfo);
        return school;
    }
}
