package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.NearbySchool;
import gs.data.school.LevelCode;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.state.State;
import org.easymock.MockControl;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides testing for the MapSchoolController.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class MapSchoolControllerTest extends BaseControllerTestCase {
    private MapSchoolController _controller;
    private ISchoolDao _schoolDao;
    private MockControl _schoolControl;
    private IReviewDao _reviewDao;
    private MockControl _reviewControl;
    private GsMockHttpServletRequest _request;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new MapSchoolController();

        _schoolControl = MockControl.createControl(ISchoolDao.class);
        _schoolDao = (ISchoolDao) _schoolControl.getMock();

        _reviewControl = MockControl.createControl(IReviewDao.class);
        _reviewDao = (IReviewDao) _reviewControl.getMock();

        _controller.setSchoolDao(_schoolDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setViewName("/viewName");

        _request = getRequest();

        SessionContextUtil.getSessionContext(_request).setState(State.CA);
    }

    public void testHandleRequest() throws Exception {
        _request.setParameter("id", "1");
        _request.setMethod("GET");

        School school = new School();
        school.setName("Alameda High School");
        school.setId(1);
        school.setLevelCode(LevelCode.HIGH);
        school.setActive(true);

        // set expectations
        _schoolControl.expectAndReturn(_schoolDao.getSchoolById(State.CA, 1), school);
        List<NearbySchool> nearbySchools = new ArrayList<NearbySchool>();
        _schoolControl.expectAndReturn(_schoolDao.findNearbySchools(school, 5), nearbySchools);
        _schoolControl.replay();

        // call controller
        _controller.handleRequest(_request, getResponse());
        // verify expectations
        _schoolControl.verify();

        // verify output
        School school2 = (School)_request.getAttribute(MapSchoolController.SCHOOL_ATTRIBUTE);
        assertNotNull("School should not be null", school2);
        assertEquals("Alameda High School", school2.getName());
        assertEquals(school.getId(), school2.getId());
        assertEquals(nearbySchools, _request.getAttribute("nearbySchools"));
        assertEquals(false, _request.getAttribute("hasNearby"));
        assertEquals("High", _request.getAttribute("levelLongName"));
        assertEquals("/viewName", _controller.getViewName());
        assertEquals(_schoolDao, _controller.getSchoolDao());
        assertEquals(_reviewDao, _controller.getReviewDao());
    }

    public void testLoadRatings() {
        School dadSchool = new School();
        dadSchool.setId(99);
        dadSchool.setLevelCode(LevelCode.HIGH);
        _request.setAttribute(MapSchoolController.SCHOOL_ATTRIBUTE, dadSchool);

        List<NearbySchool> nearbySchools = new ArrayList<NearbySchool>();
        // #1
        School school1 = new School();
        school1.setDatabaseState(State.CA);
        school1.setId(1);
        NearbySchool nearbySchool1 = new NearbySchool();
        nearbySchool1.setNeighbor(school1);
        nearbySchool1.setRating(5);
        nearbySchools.add(nearbySchool1);
        Ratings ratings1 = new Ratings();

        // #2
        School school2 = new School();
        school2.setDatabaseState(State.CA);
        school2.setId(1);
        NearbySchool nearbySchool2 = new NearbySchool();
        nearbySchool2.setNeighbor(school2);
        nearbySchool2.setRating(5);
        nearbySchools.add(nearbySchool2);
        Ratings ratings2 = new Ratings();

        // set expectations
        _schoolControl.expectAndReturn(_schoolDao.findNearbySchools(dadSchool, 5), nearbySchools);
        _schoolControl.replay();
        _reviewControl.expectAndReturn(_reviewDao.findRatingsBySchool(school1), ratings1);
        _reviewControl.expectAndReturn(_reviewDao.findRatingsBySchool(school2), ratings2);
        _reviewControl.replay();

        // call controller
        _controller.handleRequestInternal(_request, _response);
        // verify expectations
        _reviewControl.verify();

        // verify output
        List mapSchools = (List) _request.getAttribute("mapSchools");
        assertNotNull(mapSchools);
        assertEquals(2, mapSchools.size());
        // #1
        MapSchoolController.MapSchool mapSchool1 = (MapSchoolController.MapSchool) mapSchools.get(0);
        assertEquals(school1, mapSchool1.getNeighbor());
        assertEquals(new Integer(5), mapSchool1.getRating());
        assertEquals(ratings1, mapSchool1.getParentRatings());
        // #2
        MapSchoolController.MapSchool mapSchool2 = (MapSchoolController.MapSchool) mapSchools.get(1);
        assertEquals(school2, mapSchool2.getNeighbor());
        assertEquals(new Integer(5), mapSchool2.getRating());
        assertEquals(ratings2, mapSchool2.getParentRatings());
    }
}
