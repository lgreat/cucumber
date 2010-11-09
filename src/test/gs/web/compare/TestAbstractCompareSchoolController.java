package gs.web.compare;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static gs.web.compare.AbstractCompareSchoolController.PARAM_SCHOOLS;
import static gs.web.compare.AbstractCompareSchoolController.PARAM_PAGE;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TestAbstractCompareSchoolController extends BaseControllerTestCase {
    private AbstractCompareSchoolController _controller;
    private ISchoolDao _schoolDao;
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
            protected String getSuccessView() {
                return "success";
            }
            @Override
            protected ComparedSchoolBaseStruct getStruct() {
                return new ComparedSchoolBaseStruct();
            }
        };

        _schoolDao = createStrictMock(ISchoolDao.class);

        _controller.setSchoolDao(_schoolDao);
        _controller.setErrorView("error");

        _model = new HashMap<String, Object>();
    }

    public void testBasics() {
        assertSame(_schoolDao, _controller.getSchoolDao());
        assertEquals("error", _controller.getErrorView());
    }

    private void replayAllMocks() {
        replayMocks(_schoolDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_schoolDao);
    }

    private void resetAllMocks() {
        resetMocks(_schoolDao);
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
        assertFalse("Expect too few schools to fail validation",
                    _controller.validateSchools(new String[] {}));
        assertFalse("Expect too few schools to fail validation",
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
        assertNull(_controller.getSchools(getRequest(), _model));

        getRequest().setParameter(PARAM_SCHOOLS, "");
        assertNull(_controller.getSchools(getRequest(), _model));

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
        assertEquals(_controller.getErrorView(), mAndV.getViewName());

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
        replayAllMocks();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
        resetAllMocks();
        assertNotNull(mAndV);
        assertEquals(_controller.getErrorView(), mAndV.getViewName());

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
}
