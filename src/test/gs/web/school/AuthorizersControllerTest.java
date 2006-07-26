package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.data.school.ISchoolDao;
import gs.data.school.census.MockCharterSchoolInfoDao;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class AuthorizersControllerTest extends BaseControllerTestCase {

    private AuthorizersController _controller;
    private MockCharterSchoolInfoDao _mockDao;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new AuthorizersController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setSchoolDao((ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID));
        _mockDao = new MockCharterSchoolInfoDao();
        _controller.setCharterSchoolInfoDao(_mockDao);
    }

    /**
     * Just make sure that we get the correct view back when no parameters
     * are provided.
     * @throws Exception
     */
    public void testNoParameters() throws Exception {
        getRequest().setMethod("GET");
        ModelAndView mAndV =
                _controller.handleRequest(getRequest(), getResponse());
        assertEquals("school/authorizers", mAndV.getViewName());
    }

    public void testNullSchool() throws Exception {
        getRequest().setMethod("GET");
        // a school that doesn't exist in the test database
        getRequest().setParameter("state", "HI");
        getRequest().setParameter("school", "66666666");
        ModelAndView mAndV =
                _controller.handleRequest(getRequest(), getResponse());
        assertEquals("school/authorizers", mAndV.getViewName());
        assertNull(mAndV.getModel().get("school"));
    }

    public void testHandleRequestInternal() throws Exception {
        getRequest().setMethod("GET");
        getRequest().setParameter("state", "CA");
        getRequest().setParameter("school", "1");
        ModelAndView mAndV =
                _controller.handleRequest(getRequest(), getResponse());
        Map model = mAndV.getModel();
        assertNotNull(model.get("school"));
        assertNotNull(model.get("structureData"));
        assertNotNull(model.get("missionData"));
        assertNotNull(model.get("academicData"));
        assertNotNull(model.get("demandData"));
        assertEquals("Source: State University of New York Charter Schools Institute",
                model.get("source"));
    }

    public void testEmptySets() throws Exception {
        getRequest().setMethod("GET");
        getRequest().setParameter("state", "CA");
        getRequest().setParameter("school", "1");
        _mockDao.setCharterSchoolInfo(MockCharterSchoolInfoDao.CharterSchoolInfoStub.NO_STRUCTURE);

        ModelAndView mAndV =
                _controller.handleRequest(getRequest(), getResponse());
        Map model = mAndV.getModel();
        assertNotNull(model.get("school"));
        assertNull(model.get("structureData"));
        assertNotNull(model.get("missionData"));
        assertNotNull(model.get("academicData"));
        assertNotNull(model.get("demandData"));
        assertEquals("Source: State University of New York Charter Schools Institute",
                model.get("source"));
    }
}
