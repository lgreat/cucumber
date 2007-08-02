package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class SchoolInterceptorTest extends TestCase {
    SchoolPageInterceptor _interceptor;
    ISchoolDao _schoolDao;
    School _school;
    HttpServletRequest _request;
    HttpServletResponse _response;
    SessionContext _context;

    public void setUp() throws Exception {
        super.setUp();
        _interceptor = new SchoolPageInterceptor();
        _schoolDao = createMock(ISchoolDao.class);
        _interceptor.setSchoolDao(_schoolDao);

        _school = new School();
        _school.setId(1);
        _school.setDatabaseState(State.CA);
        _school.setActive(true);

        _request = createMock(HttpServletRequest.class);
        _response = createMock(HttpServletResponse.class);

        _context = new SessionContext();
    }

    public void testPutSchoolInRequest() throws Exception {
        _context.setState(State.CA);
        expect(_request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME)).andReturn(_context);
        expect(_request.getParameter("id")).andReturn("1");

        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(_school);
        _request.setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, _school);

        replay(_request);
        replay(_schoolDao);

        assertTrue(_interceptor.preHandle(_request, _response, null));

        verify(_request);
        verify(_schoolDao);
    }

    public void testNoStateParameter() throws Exception {
        _context.setState(null);
        expect(_request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME)).andReturn(_context);
        expect(_request.getRequestDispatcher("/school/error.page")).
                andReturn(createMock(RequestDispatcher.class));

        replay(_request);

        assertFalse(_interceptor.preHandle(_request, _response, null));

        verify(_request);
    }

    public void testNoIdParameter() throws Exception {
        _context.setState(State.CA);
        expect(_request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME)).andReturn(_context);
        expect(_request.getParameter("id")).andReturn(null);
        expect(_request.getParameter("schoolId")).andReturn(null);
        expect(_request.getRequestDispatcher("/school/error.page")).
                andReturn(createMock(RequestDispatcher.class));

        replay(_request);

        assertFalse(_interceptor.preHandle(_request, _response, null));
        verify(_request);
    }

    public void testInactiveSchoolNotInRequest() throws Exception {
        _school.setActive(false);
        _context.setState(State.CA);

        expect(_request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME)).andReturn(_context);
        expect(_request.getParameter("id")).andReturn("1");

        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(_school);
        expect(_request.getRequestDispatcher("/school/error.page")).
                andReturn(createMock(RequestDispatcher.class));

        replay(_request);
        replay(_schoolDao);

        assertFalse(_interceptor.preHandle(_request, _response, null));
        verify(_request);
        verify(_schoolDao);
    }

    public void testUseSchoolIdParamWhenIdNotSpecified() throws Exception {
        _context.setState(State.CA);
        expect(_request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME)).andReturn(_context);

        expect(_request.getParameter("id")).andReturn(null);
        expect(_request.getParameter("schoolId")).andReturn("1");

        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(_school);
        _request.setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, _school);

        replay(_request);
        replay(_schoolDao);

        assertTrue(_interceptor.preHandle(_request, _response, null));
        verify(_request);
    }
}
