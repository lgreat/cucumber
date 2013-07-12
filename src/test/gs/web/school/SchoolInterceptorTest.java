package gs.web.school;

import gs.data.school.School;
import gs.data.state.State;
import gs.web.GsMockHttpServletRequest;
import gs.web.request.RequestAttributeHelper;
import gs.web.util.context.SessionContext;
import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.easymock.classextension.EasyMock.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 */
public class SchoolInterceptorTest extends TestCase {
    SchoolPageInterceptor _interceptor;
    RequestAttributeHelper _requestAttributeHelper;
    School _school;
    HttpServletRequest _request;
    HttpServletResponse _response;
    SessionContext _context;

    public void setUp() throws Exception {
        super.setUp();
        _interceptor = new SchoolPageInterceptor();
        _requestAttributeHelper = createMock(RequestAttributeHelper.class);
        _interceptor.setRequestAttributeHelper(_requestAttributeHelper);

        _school = new School();
        _school.setId(1);
        _school.setDatabaseState(State.CA);
        _school.setCity("Alameda");
        _school.setActive(true);

        _request = new GsMockHttpServletRequest();
        _response = new MockHttpServletResponse();

        _context = new SessionContext();
    }

    public void testActiveSchoolReturnsTrue() throws Exception {
        expect(_requestAttributeHelper.getSchool(_request)).andReturn(_school);

        replay(_requestAttributeHelper);

        assertTrue(_interceptor.preHandle(_request, _response, null));

        verify(_requestAttributeHelper);
    }

    public void testDemoSchoolReturnsTrue() throws Exception {
        _school.setActive(false);
        _school.setNotes("GREATSCHOOLS_DEMO_SCHOOL_PROFILE"); // demo marker
        expect(_requestAttributeHelper.getSchool(_request)).andReturn(_school);

        replay(_requestAttributeHelper);

        assertTrue(_interceptor.preHandle(_request, _response, null));

        verify(_requestAttributeHelper);
    }

    public void testInactiveSchoolReturnsFalse() throws Exception {
        _school.setActive(false);

        expect(_requestAttributeHelper.getSchool(_request)).andReturn(_school);
        replay(_requestAttributeHelper);

        assertFalse(_interceptor.preHandle(_request, _response, null));
        verify(_requestAttributeHelper);
    }

    public void testNoSchoolReturnsFalse() throws Exception {
        expect(_requestAttributeHelper.getSchool(_request)).andReturn(null);
        replay(_requestAttributeHelper);

        assertFalse(_interceptor.preHandle(_request, _response, null));
        verify(_requestAttributeHelper);
    }
}
