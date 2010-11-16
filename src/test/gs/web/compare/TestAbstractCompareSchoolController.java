package gs.web.compare;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.BaseControllerTestCase;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gs.web.compare.AbstractCompareSchoolController.PARAM_SCHOOLS;
import static gs.web.compare.AbstractCompareSchoolController.PARAM_PAGE;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TestAbstractCompareSchoolController extends BaseControllerTestCase {
    private AbstractCompareSchoolController _controller;
    private ISchoolDao _schoolDao;
    private IRatingsConfigDao _ratingsConfigDao;
    private TestManager _testManager;
    private Map<String, Object> _model;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new AbstractCompareSchoolController() {
            @Override
            protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                                List<ComparedSchoolBaseStruct> schools,
                                                Map<String, Object> model) {
            }
            @Override
            public String getSuccessView() {
                return "success";
            }
            @Override
            protected ComparedSchoolBaseStruct getStruct() {
                return new ComparedSchoolBaseStruct();
            }
        };

        _schoolDao = createStrictMock(ISchoolDao.class);
        _ratingsConfigDao = createStrictMock(IRatingsConfigDao.class);
        _testManager = createStrictMock(TestManager.class);

        _controller.setSchoolDao(_schoolDao);
        _controller.setRatingsConfigDao(_ratingsConfigDao);
        _controller.setTestManager(_testManager);
        _controller.setErrorView("error");
        _controller.setPageSize(4);

        _model = new HashMap<String, Object>();
    }

    public void testBasics() {
        assertSame(_schoolDao, _controller.getSchoolDao());
        assertSame(_ratingsConfigDao, _controller.getRatingsConfigDao());
        assertSame(_testManager, _controller.getTestManager());
        assertEquals("error", _controller.getErrorView());
        assertEquals(4, _controller.getPageSize());
    }

    private void replayAllMocks() {
        replayMocks(_schoolDao, _ratingsConfigDao, _testManager);
    }

    private void verifyAllMocks() {
        verifyMocks(_schoolDao, _ratingsConfigDao, _testManager);
    }

    private void resetAllMocks() {
        resetMocks(_schoolDao, _ratingsConfigDao, _testManager);
    }

    public void testPaginateSchools() {
        String[] schools = new String[] {};
        assertEquals(0, _controller.paginateSchools(getRequest(), schools, _model).length);

        schools = new String[] {"ca1"};
        assertEquals(1, _controller.paginateSchools(getRequest(), schools, _model).length);

        schools = new String[] {"ca1","ca2"};
        assertEquals(2, _controller.paginateSchools(getRequest(), schools, _model).length);
        
        schools = new String[] {"ca1","ca2","ca3"};
        assertEquals(3, _controller.paginateSchools(getRequest(), schools, _model).length);

        schools = new String[] {"ca1","ca2","ca3","ca4"};
        assertEquals(4, _controller.paginateSchools(getRequest(), schools, _model).length);

        schools = new String[] {"ca1","ca2","ca3","ca4","ca5"};
        assertEquals(4, _controller.paginateSchools(getRequest(), schools, _model).length);
        assertEquals("ca1", _controller.paginateSchools(getRequest(), schools, _model)[0]);
        assertEquals("ca2", _controller.paginateSchools(getRequest(), schools, _model)[1]);
        assertEquals("ca3", _controller.paginateSchools(getRequest(), schools, _model)[2]);
        assertEquals("ca4", _controller.paginateSchools(getRequest(), schools, _model)[3]);

        getRequest().setParameter(PARAM_PAGE, "two"); // defaults to first page
        assertEquals(4, _controller.paginateSchools(getRequest(), schools, _model).length);
        assertEquals("ca1", _controller.paginateSchools(getRequest(), schools, _model)[0]);
        assertEquals("ca2", _controller.paginateSchools(getRequest(), schools, _model)[1]);
        assertEquals("ca3", _controller.paginateSchools(getRequest(), schools, _model)[2]);
        assertEquals("ca4", _controller.paginateSchools(getRequest(), schools, _model)[3]);
        
        getRequest().setParameter(PARAM_PAGE, "2");
        assertEquals(4, _controller.paginateSchools(getRequest(), schools, _model).length);
        assertEquals("ca2", _controller.paginateSchools(getRequest(), schools, _model)[0]);
        assertEquals("ca3", _controller.paginateSchools(getRequest(), schools, _model)[1]);
        assertEquals("ca4", _controller.paginateSchools(getRequest(), schools, _model)[2]);
        assertEquals("ca5", _controller.paginateSchools(getRequest(), schools, _model)[3]);

        schools = new String[] {"ca1","ca2","ca3","ca4","ca5","ca6","ca7","ca8"};
        assertEquals(4, _controller.paginateSchools(getRequest(), schools, _model).length);
        assertEquals("ca5", _controller.paginateSchools(getRequest(), schools, _model)[0]);
        assertEquals("ca6", _controller.paginateSchools(getRequest(), schools, _model)[1]);
        assertEquals("ca7", _controller.paginateSchools(getRequest(), schools, _model)[2]);
        assertEquals("ca8", _controller.paginateSchools(getRequest(), schools, _model)[3]);

        getRequest().setParameter(PARAM_PAGE, "1");
        assertEquals(4, _controller.paginateSchools(getRequest(), schools, _model).length);
        assertEquals("ca1", _controller.paginateSchools(getRequest(), schools, _model)[0]);
        assertEquals("ca2", _controller.paginateSchools(getRequest(), schools, _model)[1]);
        assertEquals("ca3", _controller.paginateSchools(getRequest(), schools, _model)[2]);
        assertEquals("ca4", _controller.paginateSchools(getRequest(), schools, _model)[3]);
    }

    public void testValidateSchools() {
        assertTrue("Expect too few schools to pass validation",
                    _controller.validateSchools(new String[] {}));
        assertTrue("Expect too few schools to pass validation",
                    _controller.validateSchools(new String[] {"ca1"}));
        assertFalse("Expect too many schools to fail validation",
                    _controller.validateSchools(new String[] {"ca1", "ca2", "ca3", "ca4", "ca5", "ca6", "ca7", "ca8", "ca9"}));
        assertFalse("Expect different states to fail validation",
                    _controller.validateSchools(new String[] {"ca1", "ak1"}));
        assertFalse("Expect duplicate schools to fail validation",
                    _controller.validateSchools(new String[] {"ca1", "ca2", "ca1"}));
        assertTrue(_controller.validateSchools(new String[] {"ca1", "ca2", "ca3"}));
    }

    public void testGetSchools() {
        assertNotNull(_controller.getSchools(getRequest(), _model));

        getRequest().setParameter(PARAM_SCHOOLS, "");
        assertNotNull(_controller.getSchools(getRequest(), _model));

        getRequest().setParameter(PARAM_SCHOOLS, "garbage");
        assertNull(_controller.getSchools(getRequest(), _model));

        getRequest().setParameter(PARAM_SCHOOLS, "la,di,da");
        assertNull(_controller.getSchools(getRequest(), _model));

        getRequest().setParameter(PARAM_SCHOOLS, "caone");
        assertNull(_controller.getSchools(getRequest(), _model));

        getRequest().setParameter(PARAM_SCHOOLS, "cd5");
        assertNull(_controller.getSchools(getRequest(), _model));

        // test no school found
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(null);
        getRequest().setParameter(PARAM_SCHOOLS, "ca1,ca2");
        replayAllMocks();
        assertNull(_controller.getSchools(getRequest(), _model));
        verifyAllMocks();
        resetAllMocks();

        // test no school found
        expect(_schoolDao.getSchoolById(State.CA, 1)).andThrow(new ObjectRetrievalFailureException("Test", null));
        getRequest().setParameter(PARAM_SCHOOLS, "ca1,ca2");
        replayAllMocks();
        assertNull(_controller.getSchools(getRequest(), _model));
        verifyAllMocks();
        resetAllMocks();

        School ca1 = new School();
        School ca2 = new School();
        List<ComparedSchoolBaseStruct> schools;

        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(ca1);
        expect(_schoolDao.getSchoolById(State.CA, 2)).andReturn(ca2);
        getRequest().setParameter(PARAM_SCHOOLS, "ca1,ca2");
        replayAllMocks();
        schools = _controller.getSchools(getRequest(), _model);
        verifyAllMocks();
        assertNotNull(schools);
        assertEquals(2, schools.size());
        assertSame(ca1, schools.get(0).getSchool());
        assertSame(ca2, schools.get(1).getSchool());
        resetAllMocks();

        expect(_schoolDao.getSchoolById(State.CA, 2)).andReturn(ca2);
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(ca1);
        getRequest().setParameter(PARAM_SCHOOLS, "ca2,ca1");
        replayAllMocks();
        schools = _controller.getSchools(getRequest(), _model);
        verifyAllMocks();
        assertNotNull(schools);
        assertEquals(2, schools.size());
        assertSame(ca2, schools.get(0).getSchool());
        assertSame(ca1, schools.get(1).getSchool());
        resetAllMocks();
    }

    public void testHandleRequestInternal() throws Exception {
        ModelAndView mAndV;

        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull(mAndV);
        assertEquals(_controller.getSuccessView(), mAndV.getViewName());

        getRequest().setParameter(PARAM_SCHOOLS, "foo");
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull(mAndV);
        assertEquals(_controller.getErrorView(), mAndV.getViewName());

        School ca1 = new School();
        ca1.setDatabaseState(State.CA);
        School ak1 = new School();
        ak1.setDatabaseState(State.AK);

        // too few schools
        getRequest().setParameter(PARAM_SCHOOLS, "ca1");
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(ca1);
        replayAllMocks();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
        resetAllMocks();
        assertNotNull(mAndV);
        assertEquals(_controller.getSuccessView(), mAndV.getViewName());

        // different states
        getRequest().setParameter(PARAM_SCHOOLS, "ca1,ak1");
        replayAllMocks();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
        resetAllMocks();
        assertNotNull(mAndV);
        assertEquals(_controller.getErrorView(), mAndV.getViewName());

        // too many schools
        getRequest().setParameter(PARAM_SCHOOLS, "ca1,ca2,ca3,ca4,ca5,ca6,ca7,ca8,ca9");
        replayAllMocks();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
        resetAllMocks();
        assertNotNull(mAndV);
        assertEquals(_controller.getErrorView(), mAndV.getViewName());

        // success
        getRequest().setParameter(PARAM_SCHOOLS, "ca1,ca2");
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(ca1);
        expect(_schoolDao.getSchoolById(State.CA, 2)).andReturn(ca1);
        replayAllMocks();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
        resetAllMocks();
        assertNotNull(mAndV);
        assertEquals("success", mAndV.getViewName()); // as configured in setUp
    }

    public void testHandleGSRatingSingle() {
        ComparedSchoolOverviewStruct struct = new ComparedSchoolOverviewStruct();
        School school = new School();
        struct.setSchool(school);

        IRatingsConfig ratingsConfig = createStrictMock(IRatingsConfig.class);
        SchoolTestValue stv = new SchoolTestValue();
        stv.setValueFloat(3f);

        expect(ratingsConfig.getYear()).andReturn(2010);
        expect(_testManager.getOverallRating(school, 2010)).andReturn(stv);
        replayAllMocks();
        replay(ratingsConfig);
        _controller.handleGSRating(struct, ratingsConfig);
        verifyAllMocks();
        verify(ratingsConfig);
        assertNotNull(struct.getGsRating());
        assertEquals(3, struct.getGsRating().intValue());
    }

    public void testHandleGSRating() throws Exception {
        List<ComparedSchoolBaseStruct> structs = new ArrayList<ComparedSchoolBaseStruct>();
        ComparedSchoolOverviewStruct struct1 = new ComparedSchoolOverviewStruct();
        School school1 = new School();
        school1.setDatabaseState(State.CA);
        struct1.setSchool(school1);

        ComparedSchoolOverviewStruct struct2 = new ComparedSchoolOverviewStruct();
        School school2 = new School();
        struct2.setSchool(school2);

        structs.add(struct1);
        structs.add(struct2);

        SchoolTestValue stv1 = new SchoolTestValue();
        stv1.setValueFloat(3f);

        SchoolTestValue stv2 = new SchoolTestValue();
        stv2.setValueFloat(6f);

        IRatingsConfig ratingsConfig = createStrictMock(IRatingsConfig.class);
        expect(_ratingsConfigDao.restoreRatingsConfig(State.CA, true)).andReturn(ratingsConfig);
        expect(ratingsConfig.getYear()).andReturn(2010);
        expect(_testManager.getOverallRating(school1, 2010)).andReturn(stv1);
        expect(ratingsConfig.getYear()).andReturn(2010);
        expect(_testManager.getOverallRating(school2, 2010)).andReturn(stv2);
        replayAllMocks();
        replay(ratingsConfig);
        _controller.handleGSRating(getRequest(), structs);
        verifyAllMocks();
        verify(ratingsConfig);
        assertNotNull(struct1.getGsRating());
        assertEquals(3, struct1.getGsRating().intValue());
        assertNotNull(struct2.getGsRating());
        assertEquals(6, struct2.getGsRating().intValue());
    }
}
