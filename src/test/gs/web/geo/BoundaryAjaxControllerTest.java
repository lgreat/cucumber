package gs.web.geo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import gs.data.geo.IDistrictBoundaryDao;
import gs.data.geo.ISchoolBoundaryDao;
import gs.data.geo.SchoolBoundary;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolWithRatings;
import gs.data.school.district.IDistrictDao;
import gs.data.search.services.DistrictSearchService;
import gs.data.search.services.SchoolSearchService;
import gs.data.state.State;
import gs.data.test.TestManager;
import gs.data.test.rating.IDistrictRatingDao;
import gs.web.BaseControllerTestCase;
import org.easymock.IArgumentMatcher;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author aroy@greatschools.org
 */
public class BoundaryAjaxControllerTest extends BaseControllerTestCase {
    private BoundaryAjaxController _controller;

    private ISchoolDao _schoolDao;
    private TestManager _testManager;
    private IDistrictDao _districtDao;
    private IDistrictRatingDao _districtRatingDao;
    private IDistrictBoundaryDao _districtBoundaryDao;
    private ISchoolBoundaryDao _schoolBoundaryDao;
    private DistrictSearchService _districtSearchService;
    private SchoolSearchService _schoolSearchService;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new BoundaryAjaxController();

        _schoolDao = createStrictMock(ISchoolDao.class);
        _testManager = createStrictMock(TestManager.class);
        _districtDao = createStrictMock(IDistrictDao.class);
        _districtRatingDao = createStrictMock(IDistrictRatingDao.class);
        _districtBoundaryDao = createStrictMock(IDistrictBoundaryDao.class);
        _schoolBoundaryDao = createStrictMock(ISchoolBoundaryDao.class);
        _districtSearchService = createStrictMock(DistrictSearchService.class);
        _schoolSearchService = createStrictMock(SchoolSearchService.class);

        _controller.setSchoolDao(_schoolDao);
        _controller.setTestManager(_testManager);
        _controller.setDistrictDao(_districtDao);
        _controller.setDistrictRatingDao(_districtRatingDao);
        _controller.setDistrictBoundaryDao(_districtBoundaryDao);
        _controller.setSchoolBoundaryDao(_schoolBoundaryDao);
        _controller.setDistrictSearchService(_districtSearchService);
        _controller.setSchoolSearchService(_schoolSearchService);
    }

    public void replayAllMocks() {
        replayMocks(_schoolDao, _testManager, _districtDao, _districtRatingDao, _districtBoundaryDao,
                _schoolBoundaryDao, _districtSearchService, _schoolSearchService);
    }

    public void verifyAllMocks() {
        verifyMocks(_schoolDao, _testManager, _districtDao, _districtRatingDao, _districtBoundaryDao,
                _schoolBoundaryDao, _districtSearchService, _schoolSearchService);
    }


    public void testGetSchoolsForLocation_None() {
        List<SchoolBoundary> boundaries = new ArrayList<SchoolBoundary>();
        expect(_schoolBoundaryDao.getSchoolBoundariesContainingPoint(1d, 2d, LevelCode.Level.ELEMENTARY_LEVEL)).andReturn(boundaries);
        replayAllMocks();
        ModelAndView mAndV = _controller.getSchoolsForLocation(1d, 2d, "e", new ExtendedModelMap(), getRequest(), getResponse());
        List schools = (List) mAndV.getModel().get("schools");
        assertNotNull(schools);
        assertEquals(0, schools.size());
        verifyAllMocks();
    }

    private School createSchool(Integer id, boolean active) {
        School rval = new School();
        rval.setId(id);
        rval.setDatabaseState(State.CA);
        rval.setName("School " + id);
        rval.setActive(active);
        rval.setCity("City");
        rval.setLevelCode(LevelCode.ELEMENTARY);
        return rval;
    }

    private SchoolBoundary createSchoolBoundary(int schoolId) {
        SchoolBoundary rval = new SchoolBoundary();
        rval.setSchoolId(schoolId);
        rval.setState(State.CA);

        GeometryFactory geomFactory = new GeometryFactory();
        rval.setGeometry(
                geomFactory.createPolygon(
                        geomFactory.createLinearRing(new Coordinate[]{new Coordinate(1, 1), new Coordinate(2, 1),
                                new Coordinate(2, 2), new Coordinate(1, 2), new Coordinate(1, 1)}), null));
        return rval;
    }

    private SchoolWithRatings createSchoolWithRatings(School s) {
        SchoolWithRatings rval = new SchoolWithRatings();
        rval.setSchool(s);
        return rval;
    }

    public void testGetSchoolsForLocation_One() {
        List<SchoolBoundary> boundaries = new ArrayList<SchoolBoundary>();
        SchoolBoundary firstBoundary = createSchoolBoundary(1);
        boundaries.add(firstBoundary);
        expect(_schoolBoundaryDao.getSchoolBoundariesContainingPoint(1d, 2d, LevelCode.Level.ELEMENTARY_LEVEL)).andReturn(boundaries);
        School firstSchool = createSchool(1, true);
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(firstSchool);
        List<SchoolWithRatings> schoolsWithRatings = new ArrayList<SchoolWithRatings>();
        SchoolWithRatings firstSchoolWithRatings = createSchoolWithRatings(firstSchool);
        schoolsWithRatings.add(firstSchoolWithRatings);
        expect(_schoolDao.populateSchoolsWithRatingsNewGSRating(eq(State.CA), eqListSize(1, School.class))).andReturn(schoolsWithRatings);
        replayAllMocks();
        ModelAndView mAndV = _controller.getSchoolsForLocation(1d, 2d, "e", new ExtendedModelMap(), getRequest(), getResponse());
        List schools = (List) mAndV.getModel().get("schools");
        assertNotNull(schools);
        assertEquals(1, schools.size());
        verifyAllMocks();
    }

    public void testGetSchoolsForLocation_Two() {
        List<SchoolBoundary> boundaries = new ArrayList<SchoolBoundary>();
        SchoolBoundary firstBoundary = createSchoolBoundary(1);
        boundaries.add(firstBoundary);
        SchoolBoundary secondBoundary = createSchoolBoundary(2);
        boundaries.add(secondBoundary);
        expect(_schoolBoundaryDao.getSchoolBoundariesContainingPoint(1d, 2d, LevelCode.Level.ELEMENTARY_LEVEL)).andReturn(boundaries);
        School firstSchool = createSchool(1, true);
        School secondSchool = createSchool(2, true);
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(firstSchool);
        expect(_schoolDao.getSchoolById(State.CA, 2)).andReturn(secondSchool);
        List<SchoolWithRatings> schoolsWithRatings = new ArrayList<SchoolWithRatings>();
        SchoolWithRatings firstSchoolWithRatings = createSchoolWithRatings(firstSchool);
        schoolsWithRatings.add(firstSchoolWithRatings);
        SchoolWithRatings secondSchoolWithRatings = createSchoolWithRatings(secondSchool);
        schoolsWithRatings.add(secondSchoolWithRatings);
        expect(_schoolDao.populateSchoolsWithRatingsNewGSRating(eq(State.CA), eqListSize(2, School.class))).andReturn(schoolsWithRatings);
        replayAllMocks();
        ModelAndView mAndV = _controller.getSchoolsForLocation(1d, 2d, "e", new ExtendedModelMap(), getRequest(), getResponse());
        List schools = (List) mAndV.getModel().get("schools");
        assertNotNull(schools);
        assertEquals(2, schools.size());
        verifyAllMocks();
    }

    public void testGetSchoolsForLocation_IgnoreInactive() {
        List<SchoolBoundary> boundaries = new ArrayList<SchoolBoundary>();
        SchoolBoundary firstBoundary = createSchoolBoundary(1);
        boundaries.add(firstBoundary);
        SchoolBoundary secondBoundary = createSchoolBoundary(2);
        boundaries.add(secondBoundary);
        expect(_schoolBoundaryDao.getSchoolBoundariesContainingPoint(1d, 2d, LevelCode.Level.ELEMENTARY_LEVEL)).andReturn(boundaries);
        School firstSchool = createSchool(1, true);
        School secondSchool = createSchool(2, false);
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(firstSchool);
        expect(_schoolDao.getSchoolById(State.CA, 2)).andReturn(secondSchool);
        List<SchoolWithRatings> schoolsWithRatings = new ArrayList<SchoolWithRatings>();
        SchoolWithRatings firstSchoolWithRatings = createSchoolWithRatings(firstSchool);
        schoolsWithRatings.add(firstSchoolWithRatings);
        expect(_schoolDao.populateSchoolsWithRatingsNewGSRating(eq(State.CA), eqListSize(1, School.class))).andReturn(schoolsWithRatings);
        replayAllMocks();
        ModelAndView mAndV = _controller.getSchoolsForLocation(1d, 2d, "e", new ExtendedModelMap(), getRequest(), getResponse());
        List schools = (List) mAndV.getModel().get("schools");
        assertNotNull(schools);
        assertEquals(1, schools.size());
        verifyAllMocks();
    }

    public void testGetSchoolsForLocation_IgnoreWrongLevel() {
        List<SchoolBoundary> boundaries = new ArrayList<SchoolBoundary>();
        SchoolBoundary firstBoundary = createSchoolBoundary(1);
        boundaries.add(firstBoundary);
        SchoolBoundary secondBoundary = createSchoolBoundary(2);
        boundaries.add(secondBoundary);
        expect(_schoolBoundaryDao.getSchoolBoundariesContainingPoint(1d, 2d, LevelCode.Level.ELEMENTARY_LEVEL)).andReturn(boundaries);
        School firstSchool = createSchool(1, true);
        School secondSchool = createSchool(2, true);
        secondSchool.setLevelCode(LevelCode.MIDDLE);
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(firstSchool);
        expect(_schoolDao.getSchoolById(State.CA, 2)).andReturn(secondSchool);
        List<SchoolWithRatings> schoolsWithRatings = new ArrayList<SchoolWithRatings>();
        SchoolWithRatings firstSchoolWithRatings = createSchoolWithRatings(firstSchool);
        schoolsWithRatings.add(firstSchoolWithRatings);
        expect(_schoolDao.populateSchoolsWithRatingsNewGSRating(eq(State.CA), eqListSize(1, School.class))).andReturn(schoolsWithRatings);
        replayAllMocks();
        ModelAndView mAndV = _controller.getSchoolsForLocation(1d, 2d, "e", new ExtendedModelMap(), getRequest(), getResponse());
        List schools = (List) mAndV.getModel().get("schools");
        assertNotNull(schools);
        assertEquals(1, schools.size());
        verifyAllMocks();
    }

    public static class ListSizeEquals implements IArgumentMatcher {
        private int _expectedSize;

        public ListSizeEquals(int expectedSize) {
            _expectedSize = expectedSize;
        }

        public boolean matches(Object argument) {
            if (!(argument instanceof Collection)) {
                return false;
            }
            Collection c = (Collection) argument;
            return c.size() == _expectedSize;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("eqListSize(").append(_expectedSize).append(")");
        }
    }

    public static <T> List<T> eqListSize(int expectedSize, @SuppressWarnings("unused") Class<T> clazz) {
        reportMatcher(new ListSizeEquals(expectedSize));
        return null;
    }
}
