package gs.web.request;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.school.AbstractSchoolController;
import org.springframework.orm.ObjectRetrievalFailureException;

import static org.easymock.EasyMock.*;

/**
 * @author aroy@greatschools.org
 */
public class RequestAttributeHelperTest extends BaseControllerTestCase {
    private RequestAttributeHelper _requestAttributeHelper;
    private ISchoolDao _schoolDao;

    public void setUp() throws Exception {
        super.setUp();
        _requestAttributeHelper = new RequestAttributeHelper();

        _schoolDao = createStrictMock(ISchoolDao.class);

        _requestAttributeHelper.setSchoolDao(_schoolDao);

        clearRequest();
    }

    private void clearRequest() {
        _request.removeAttribute(RequestAttributeHelper.SCHOOL_ID_ATTRIBUTE);
        _request.removeAttribute(RequestAttributeHelper.SCHOOL_STATE_ATTRIBUTE);
        _request.removeAttribute(IDirectoryStructureUrlController.FIELDS);
        _request.removeAttribute(AbstractSchoolController.SCHOOL_ATTRIBUTE);

        _request.removeParameter("id");
        _request.removeParameter("schoolId");
        _request.removeParameter("state");

        _request.setRequestURI("/");
        getSessionContext().setState(null);
    }

    public void testGetSchoolId() {
        assertNull(RequestAttributeHelper.getSchoolId(_request));

        _request.setParameter("id", "1");
        assertNotNull(RequestAttributeHelper.getSchoolId(_request));
        assertEquals("Should pull id from params", new Integer(1), RequestAttributeHelper.getSchoolId(_request));

        _request.setParameter("id", "2");
        assertEquals("Request parameters ignored if value already in request attribute",
                new Integer(1), RequestAttributeHelper.getSchoolId(_request));

        clearRequest();
        _request.setAttribute(RequestAttributeHelper.SCHOOL_ID_ATTRIBUTE, 4);
        assertNotNull(RequestAttributeHelper.getSchoolId(_request));
        assertEquals("Should pull id from request attribute", new Integer(4), RequestAttributeHelper.getSchoolId(_request));

        clearRequest();

        _request.setParameter("id", "2");
        assertEquals("Should pull id from params",
                new Integer(2), RequestAttributeHelper.getSchoolId(_request));

        clearRequest();

        _request.setRequestURI("/california/alameda/5-Alameda-High-School/");
        assertEquals("Should pull id from directory structure url fields",
                new Integer(5), RequestAttributeHelper.getSchoolId(_request));

        clearRequest();

        _request.setParameter("schoolId", "3");
        assertEquals("Should pull id from params",
                new Integer(3), RequestAttributeHelper.getSchoolId(_request));

        clearRequest();

        _request.setParameter("schoolId", "foo");
        assertNull("Expect garbage parameter to be ignored", RequestAttributeHelper.getSchoolId(_request));
    }

    public void testGetState() {
        assertNull(RequestAttributeHelper.getState(_request));

        getSessionContext().setState(State.CA);
        assertNotNull("Expect state from SessionContext", RequestAttributeHelper.getState(_request));
        assertEquals("Expect state from SessionContext", State.CA, RequestAttributeHelper.getState(_request));

        clearRequest();
        assertNull(RequestAttributeHelper.getState(_request));

        _request.setRequestURI("/california/");
        getSessionContext().setState(State.CA);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, new DirectoryStructureUrlFields(_request));
        getSessionContext().setState(null);
        assertNotNull("Expect state from DirectoryStructureUrlFields", RequestAttributeHelper.getState(_request));
        assertEquals("Expect state from DirectoryStructureUrlFields", State.CA, RequestAttributeHelper.getState(_request));

        clearRequest();
        assertNull(RequestAttributeHelper.getState(_request));
        _request.setParameter("state", "CA");
        assertNotNull("Expect state from parameter", RequestAttributeHelper.getState(_request));
        assertEquals("Expect state from parameter", State.CA, RequestAttributeHelper.getState(_request));

        clearRequest();
        assertNull(RequestAttributeHelper.getState(_request));
        _request.setParameter("state", "foo");
        assertNull("Expect garbage parameter to be ignored", RequestAttributeHelper.getState(_request));
    }

    public void testGetDirectoryStructureUrlFields() {
        assertNotNull(RequestAttributeHelper.getDirectoryStructureUrlFields(_request));
        assertFalse(RequestAttributeHelper.getDirectoryStructureUrlFields(_request).hasState());

        clearRequest();

        _request.setRequestURI("/california/");
        getSessionContext().setState(State.CA);
        assertTrue(RequestAttributeHelper.getDirectoryStructureUrlFields(_request).hasState());
    }

    public void testGetSchool() {
        replay(_schoolDao);
        assertNull(_requestAttributeHelper.getSchool(_request));
        verify(_schoolDao);

        reset(_schoolDao);

        _request.setAttribute(RequestAttributeHelper.SCHOOL_ID_ATTRIBUTE, 1);
        _request.setAttribute(RequestAttributeHelper.SCHOOL_STATE_ATTRIBUTE, State.CA);

        School school = new School();
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        replay(_schoolDao);
        assertNotNull(_requestAttributeHelper.getSchool(_request));
        verify(_schoolDao);
        assertSame(school, _requestAttributeHelper.getSchool(_request));

        reset(_schoolDao);
        clearRequest();
        _request.setAttribute(RequestAttributeHelper.SCHOOL_ID_ATTRIBUTE, 1);
        _request.setAttribute(RequestAttributeHelper.SCHOOL_STATE_ATTRIBUTE, State.CA);

        expect(_schoolDao.getSchoolById(State.CA, 1)).andThrow(new ObjectRetrievalFailureException(School.class, 1));
        replay(_schoolDao);
        assertNull(_requestAttributeHelper.getSchool(_request));
        verify(_schoolDao);
    }
}
