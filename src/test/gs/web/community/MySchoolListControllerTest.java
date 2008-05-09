package gs.web.community;

import gs.data.community.FavoriteSchool;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import org.springframework.web.servlet.ModelAndView;

import java.util.Set;
import java.util.HashSet;
import java.util.List;

/**
 * Tests MySchoolListController.
 */
public class MySchoolListControllerTest extends BaseControllerTestCase {

    private MySchoolListController _controller;
    private SessionContextUtil _sessionContextUtil;

    private static final String TEST_VIEW_NAME = "/community/msl.page";

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new MySchoolListController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setViewName(TEST_VIEW_NAME);
        _controller.setSchoolDao((ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID));
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
    }

    public void testMySchoolListController() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setMethod("GET");
        request.setParameter(SessionContextUtil.MEMBER_PARAM, "1");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        ModelAndView modelAndView = _controller.handleRequest(request, getResponse());
        assertEquals(TEST_VIEW_NAME, modelAndView.getViewName());
        List<School> schools = (List<School>) modelAndView.getModel().get("schools");
        assertEquals(4, schools.size());
    }

    public void testGetRequestNoUser() throws Exception {
        getRequest().setMethod("GET");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertNull("no schools should be returned when there is no user", mAndV.getModel().get("schools"));
    }

    public void testGetSchoolList() throws Exception {
        Set<FavoriteSchool> favoriteSchools = new HashSet<FavoriteSchool>();
        favoriteSchools.add(makeFavoriteSchool(State.CA, 1));
        favoriteSchools.add(makeFavoriteSchool(State.CA, 2));
        favoriteSchools.add(makeFavoriteSchool(State.AK, 10));
        favoriteSchools.add(makeFavoriteSchool(State.AK, 11));
        List<School> schools = _controller.getSchoolList(favoriteSchools);
        assertEquals("There should be 4 schools in the school list", 4, schools.size());
        assertTrue(checkListContainsSchool(schools, State.CA, 1));
    }

    boolean checkListContainsSchool(List<School> schools, State state, int schoolId) {
        for (School s : schools) {
            if (s.getId().equals(schoolId) && s.getDatabaseState().equals(state)) {
                return true;
            }
        }
        return false;
    }

    static FavoriteSchool makeFavoriteSchool(State state, int school_id) {
        FavoriteSchool fs = new FavoriteSchool();
        fs.setId(1);
        fs.setSchoolId(school_id);
        fs.setState(state);
        return fs;
    }
}
