package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.school.review.CategoryRating;
import gs.data.state.State;
import gs.data.util.NameValuePair;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.jsp.Util;
import gs.web.survey.SurveyController;
import gs.web.util.MockSessionContext;
import gs.web.util.context.SessionContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolOverviewControllerTest extends BaseControllerTestCase {

    private SchoolOverviewController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SchoolOverviewController)getApplicationContext().getBean(SchoolOverviewController.BEAN_ID);
    }

    public void testHandleRequest() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setMethod("GET");
        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        School school = (School)mAndV.getModel().get("school");
        assertEquals("Alameda High School", school.getName());
        assertEquals(new Integer(4), mAndV.getModel().get("reviewCount"));
        assertTrue(StringUtils.isNotBlank((String)mAndV.getModel().get("reviewText")));
    }

    public void testHasTestData() throws Exception {
        ISchoolDao _schoolDao = (ISchoolDao)getApplicationContext().getBean(ISchoolDao.BEAN_ID);
        School _school = _schoolDao.getSchoolById(State.CA, new Integer(1));
        assertTrue("School should have test data.", _controller.hasTestData(_school));

        _school = _schoolDao.getSchoolById(State.CA, new Integer(11));
        assertFalse("School should have no test data.", _controller.hasTestData(_school));
    }

    public void testCameFromSurveyPage() throws Exception {
        getRequest().setMethod("GET");
        getRequest().setAttribute("state", State.CA);
        getRequest().setParameter("id", "1");
        getRequest().setMethod("GET");

        getSessionContext().setTempMsg(SurveyController.TMP_MSG_COOKIE_PREFIX + "CA1");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());

        assertTrue((Boolean) mAndV.getModel().get("fromSurveyPage"));
        assertEquals("", getResponse().getCookie("TMP_MSG").getValue());

        getSessionContext().setTempMsg(SurveyController.TMP_MSG_COOKIE_PREFIX + "AK1");
        mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertNull(mAndV.getModel().get("fromSurveyPage"));
        assertEquals("", getResponse().getCookie("TMP_MSG").getValue());
    }

    public void testHasTeacherData() throws Exception {
        // todo
    }

    public void testHasStudentData() throws Exception {
        // todo
    }

    public void testHasFinanceData() throws Exception {
        // todo
    }

    public void testHasAPExams() throws Exception {
        ISchoolDao _schoolDao = (ISchoolDao)getApplicationContext().getBean(ISchoolDao.BEAN_ID);
        School _school = _schoolDao.getSchoolById(State.CA, new Integer(1));
        assertFalse("School should have no ap exams", _controller.hasAPExams(_school));
    }
    
    /*
     * When cobrand is number1expert and cookies set up right, this should behave as above.
     */
    public void testLeadGenPass() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setMethod("GET");
        MockSessionContext sessionContext = new MockSessionContext();
        sessionContext.setCobrand("number1expert");
        sessionContext.setState(State.CA);
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext);

        Cookie[] cookies = new Cookie[] {
                new Cookie("AGENTID", "1234"),
                new Cookie("BIREG1234", "1")
        };
        request.setCookies(cookies);

        MockHttpServletResponse response = getResponse();
        assertNull(response.getRedirectedUrl());
        ModelAndView mAndV = _controller.handleRequest(request, response);
        School school = (School)mAndV.getModel().get("school");
        assertEquals("Alameda High School", school.getName());
        assertEquals(new Integer(4), mAndV.getModel().get("reviewCount"));
        assertTrue(StringUtils.isNotBlank((String)mAndV.getModel().get("reviewText")));
        assertNull(response.getRedirectedUrl());
    }

    /*
     * When cobrand is number1expert and cookies are set up wrong, this should send a redirect
     */
    public void testLeadGenFail() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setMethod("GET");
        MockSessionContext sessionContext = new MockSessionContext();
        sessionContext.setCobrand("number1expert");
        sessionContext.setState(State.CA);
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext);

        Cookie[] cookies = new Cookie[] {
                new Cookie("AGENTID", "1234"),
                new Cookie("BIREG1234", "0")
        };
        request.setCookies(cookies);

        MockHttpServletResponse response = getResponse();
        assertNull(response.getRedirectedUrl());
        _controller.handleRequest(request, response);
        assertNotNull(response.getRedirectedUrl());
    }

    /**
     * test the functionality of the static function to create the random Rating
     *
     * Insure that the contents of the map have expected values
     */
    public void testDoRandomRating(){
        Ratings.Category ratingsCategory = Ratings.Category.Principal;
        Integer rating = 3;

        NameValuePair<Ratings.Category,Integer> randomRating = new NameValuePair<Ratings.Category, Integer>(ratingsCategory,rating);

        Map reviewMap = new HashMap();

        SchoolOverviewController.doRandomRating(randomRating, reviewMap);

        assertEquals("Expect reviewMap.size to be 2", 2, reviewMap.size());
        assertNotNull("Expected to find key: 'randomCategory'", reviewMap.get("randomCategory")) ;
        assertNotNull("Expected to find key: 'randomRating'", reviewMap.get("randomRating")) ;

        assertEquals("Expected the rating to be: 'average'", "average", (String)reviewMap.get("randomRating"));
        assertEquals("Expected the category to be: 'principal leadership is", "principal leadership is", (String)reviewMap.get("randomCategory")) ;
    }

    /**
     *  test the functionality of the static function to create the random Rating
     *  with no random rating to display
     */
    public void testDoRandomRating_NullRandomRating(){

        // an null random rating would signify that not enough ratings were submitted
        NameValuePair<Ratings.Category,Integer> randomRating = null;

        Map reviewMap = new HashMap();

        SchoolOverviewController.doRandomRating(randomRating, reviewMap);
        assertEquals("Expect reviewMap.size to be 0", 0, reviewMap.size());
    }

    /**
     * Tests the functionality of the static function to create the display reviews for a school
     * No reviews
     */
    public void testDoMultiParentReviews_None(){

        List<Review> reviewList = new ArrayList<Review>();

        Map reviewMap = new HashMap();

        SchoolOverviewController.doMultiParentReviews(reviewList,reviewMap);
        assertEquals("Expect reviewMap.size to be 0", 0, reviewMap.size());

    }

    /**
     * Tests the functionality of the static function to create the display reviews for a school
     * One Review
     */
    public void testDoMultiParentReviews_OneReview(){

        List<Review> reviewList = new ArrayList<Review>();

        School school = new School();
        school.setLevelCode(LevelCode.ELEMENTARY);
        school.setId(1);
        school.setType(SchoolType.PUBLIC);
        school.setDatabaseState(State.CA);

        Review aReview = new Review();
        aReview.setQuality(CategoryRating.RATING_1);
        aReview.setComments("Comment 1");
        aReview.setSchool(school);
        aReview.setId(1);
        reviewList.add(aReview);

        Map reviewMap = new HashMap();

        SchoolOverviewController.doMultiParentReviews(reviewList,reviewMap);
        assertEquals("Expect reviewMap.size to be 2", 2, reviewMap.size());
        assertNotNull("Expected to find key: 'totalReviews'", reviewMap.get("totalReviews"));
        assertNotNull("Expected to find key: 'schoolReviews'", reviewMap.get("schoolReviews"));

        assertEquals("Expected totalReviews to be 1", new Integer(1), (Integer)reviewMap.get("totalReviews"));
        assertNotNull("Expected schoolReviews not to be null", reviewMap.get("schoolReviews"));

        List<Map> schoolReviews = (List<Map>)reviewMap.get("schoolReviews");

        assertEquals("Expected schoolReviews to have one entry", 1, schoolReviews.size());
        // get the map from the list
        Map<String, String> schoolData = schoolReviews.get(0);

        assertNotNull("Expected to find key: 'psRating'", schoolData.get("psRating"));
        assertNotNull("Expected to find key: 'psComment'", schoolData.get("psComment"));

        assertEquals("Expected psRating to be: ",aReview.getQuality().getName(), (String) schoolData.get("psRating")) ;
        assertEquals("Expected psComment to be: ", Util.abbreviateAtWhitespace(aReview.getComments(), SchoolOverviewController.REVIEW_LENGTH),(String)schoolData.get("psComment"));
    }

    /**
     * Tests the functionality of the static function to create the display reviews for a school
     * One Review
     */
    public void testDoMultiParentReviews_OneReview_Preschool(){

        List<Review> reviewList = new ArrayList<Review>();

        School school = new School();
        school.setLevelCode(LevelCode.PRESCHOOL);
        school.setId(1);
        school.setType(SchoolType.PUBLIC);
        school.setDatabaseState(State.CA);

        Review aReview = new Review();
        aReview.setPOverall(CategoryRating.RATING_1);
        aReview.setComments("Comment 1");
        aReview.setSchool(school);
        aReview.setId(1);
        reviewList.add(aReview);

        Map<String, Object> reviewMap = new HashMap<String, Object>();

        SchoolOverviewController.doMultiParentReviews(reviewList, reviewMap);
        assertEquals("Expect reviewMap.size to be 2", 2, reviewMap.size());
        assertNotNull("Expected to find key: 'totalReviews'", reviewMap.get("totalReviews"));
        assertNotNull("Expected to find key: 'schoolReviews'", reviewMap.get("schoolReviews"));

        assertEquals("Expected totalReviews to be 1", new Integer(1), reviewMap.get("totalReviews"));
        assertNotNull("Expected schoolReviews not to be null", reviewMap.get("schoolReviews"));

        List<Map<String, String>> schoolReviews = (List<Map<String,String>>)reviewMap.get("schoolReviews");

        assertEquals("Expected schoolReviews to have one entry", 1, schoolReviews.size());
        // get the map from the list
        Map<String, String> schoolData = schoolReviews.get(0);

        assertNotNull("Expected to find key: 'psRating'", schoolData.get("psRating"));
        assertNotNull("Expected to find key: 'psComment'", schoolData.get("psComment"));

        assertEquals("Expected psRating to be: ",aReview.getPOverall().getName(), schoolData.get("psRating")) ;
        assertEquals("Expected psComment to be: ", Util.abbreviateAtWhitespace(aReview.getComments(), SchoolOverviewController.REVIEW_LENGTH),(String)schoolData.get("psComment"));
    }

    /**
     * Tests the functionality of the static function to create the display reviews for a school
     * Three Reviews
     */
    public void testDoMultiParentReviews_MaxReviews(){

        List<Review> reviewList = new ArrayList<Review>();

        School school = new School();
        school.setLevelCode(LevelCode.ELEMENTARY);
        school.setId(1);
        school.setType(SchoolType.PUBLIC);
        school.setDatabaseState(State.CA);

        for (int i = 0; i<3; i++){
            Review aReview = new Review();
            aReview.setQuality(CategoryRating.RATING_1);
            aReview.setComments("Comment 1");
            aReview.setId(1);
            aReview.setSchool(school);
            reviewList.add(aReview);
        }

        Map reviewMap = new HashMap();

        SchoolOverviewController.doMultiParentReviews(reviewList,reviewMap);
        assertEquals("Expect reviewMap.size to be 2", 2, reviewMap.size());
        assertNotNull("Expected to find key: 'totalReviews'", reviewMap.get("totalReviews"));
        assertNotNull("Expected to find key: 'schoolReviews'", reviewMap.get("schoolReviews"));

        assertEquals("Expected totalReviews to be 3", new Integer(3), (Integer)reviewMap.get("totalReviews"));
        assertNotNull("Expected schoolReviews not to be null", reviewMap.get("schoolReviews"));

        List<Map> schoolReviews = (List<Map>)reviewMap.get("schoolReviews");
        assertEquals("Expected 3 reviews", 3, schoolReviews.size());


    }

    /**
     * Tests the functionality of the static function to create the display reviews for a school
     * 5 Reviews
     */
    public void testDoMultiParentReviews_MoreThanMaxReview(){

        List<Review> reviewList = new ArrayList<Review>();

        School school = new School();
        school.setLevelCode(LevelCode.ELEMENTARY);
        school.setId(1);
        school.setType(SchoolType.PUBLIC);
        school.setDatabaseState(State.CA);

        for (int i = 0; i<5; i++){
            Review aReview = new Review();
            aReview.setQuality(CategoryRating.RATING_1);
            aReview.setComments("Comment 1");
            aReview.setId(1);
            aReview.setSchool(school);
            reviewList.add(aReview);
        }

        Map reviewMap = new HashMap();

        SchoolOverviewController.doMultiParentReviews(reviewList,reviewMap);
        assertEquals("Expect reviewMap.size to be 2", 2, reviewMap.size());
        assertNotNull("Expected to find key: 'totalReviews'", reviewMap.get("totalReviews"));
        assertNotNull("Expected to find key: 'schoolReviews'", reviewMap.get("schoolReviews"));


        assertEquals("Expected totalReviews to be 5", new Integer(5), (Integer)reviewMap.get("totalReviews"));
        assertNotNull("Expected schoolReviews not to be null", reviewMap.get("schoolReviews"));

        List<Map> schoolReviews = (List<Map>)reviewMap.get("schoolReviews");
        assertEquals("Expected 3 reviews", 3, schoolReviews.size());
 
    }

    /**
     * Tests the functionality of the static function to create the display reviews for a school
     * Decline to state 
     */
    public void testDoMultiParentReviews_DeclineToState(){

        School school = new School();
        school.setLevelCode(LevelCode.ELEMENTARY);
        school.setId(1);
        school.setType(SchoolType.PUBLIC);
        school.setDatabaseState(State.CA);

           List<Review> reviewList = new ArrayList<Review>();
           Review aReview = new Review();
           aReview.setQuality(CategoryRating.DECLINE_TO_STATE);
           aReview.setComments("Comment 1");
           aReview.setId(1);
        aReview.setSchool(school);

           reviewList.add(aReview);

           Map reviewMap = new HashMap();

           SchoolOverviewController.doMultiParentReviews(reviewList,reviewMap);
           assertEquals("Expect reviewMap.size to be 0", 0, reviewMap.size());
    }


    /**
     * Tests the functionality of the static function to create the display reviews for a school
     * Decline to state
     */
    public void testDoSingleParentReview_DeclineToState(){


           List<Review> reviewList = new ArrayList<Review>();
           Review aReview = new Review();
           aReview.setQuality(CategoryRating.DECLINE_TO_STATE);
           aReview.setComments("Comment 1");

           reviewList.add(aReview);

           Map reviewMap = new HashMap();

           SchoolOverviewController.doSingleParentReview(reviewList, reviewMap);
           assertEquals("Expect reviewMap.size to be 0", 0, reviewMap.size());
    }

}
