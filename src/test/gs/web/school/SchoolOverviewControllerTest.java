package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolOverviewControllerTest extends BaseControllerTestCase {

    private SchoolOverviewController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new SchoolOverviewController();
        ISchoolDao schoolDao = (ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID);
        _controller.setSchoolDao(schoolDao);
        _controller.setViewName("school/overview");
    }

    public void testHandleRequestInternal() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        ModelAndView mAndV = _controller.handleRequestInternal(request, getResponse());
        School school = (School)mAndV.getModel().get("school");
        assertEquals("Alameda High School", school.getName());

        //Integer reviewCount = (Integer)mAndV.getModel().get("reviewCount");
    }
}
