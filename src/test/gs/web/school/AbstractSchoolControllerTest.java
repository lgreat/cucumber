package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.data.school.School;
import gs.data.school.ISchoolDao;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class AbstractSchoolControllerTest extends BaseControllerTestCase {

    private AbstractSchoolController _controller;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = createController();
        ISchoolDao sd = (ISchoolDao)getApplicationContext().getBean(ISchoolDao.BEAN_ID);
        _controller.setSchoolDao(sd);
    }

    public void testHandleRequestInternal() throws Exception {
        GsMockHttpServletRequest request = getRequest();

        // Inactive school
        request.setParameter("state", "CA");
        request.setParameter("id", "11");
        request.setMethod("GET");
        
        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        assertNotNull(mAndV); // make sure handleRequestInternal is not called.
        assertEquals("/school/error", mAndV.getViewName());

        // a valid school
        request.setParameter("id", "1");
        mAndV = _controller.handleRequest(request, getResponse());
        assertNull(mAndV); // make sure handleRequestInternal *is* called.
        School school = _controller.getSchool();
        assertNotNull(school);
        assertEquals("Alameda High School", school.getName());
    }

    private static AbstractSchoolController createController() {
        return new AbstractSchoolController() {
            protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
                return null;
            }
        };
    }
}
