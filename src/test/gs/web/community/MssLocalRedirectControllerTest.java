package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.state.State;
import static org.easymock.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by chriskimm@greatschools.net
 */
public class MssLocalRedirectControllerTest extends BaseControllerTestCase {

    private MssLocalRedirectController _controller;
    private ISchoolDao _mockSchoolDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new MssLocalRedirectController();
        _mockSchoolDao = createMock(ISchoolDao.class);
        _controller.setSchoolDao(_mockSchoolDao);
        Map<String, String> pageMap = new HashMap<String, String>();
        pageMap.put("1", "foo/%STATE%/foo-%CITY%/foo");
        pageMap.put("2", "%STATE%-bar/%CITY%/bar-%CITY%");
        _controller.setPageMap(pageMap);
    }

    public void testRequestWithGoodParams() throws Exception {
        getRequest().setMethod("GET");
        getRequest().setParameter(MssLocalRedirectController.PARAM_STATE, "CA");
        getRequest().setParameter(MssLocalRedirectController.PARAM_SCHOOL_ID, "1");
        getRequest().setParameter(MssLocalRedirectController.PARAM_PAGE, "1");
        expect(_mockSchoolDao.getSchoolById(State.CA, 1)).andReturn(makeSchool(State.CA, 1, "Alameda"));
        replay(_mockSchoolDao);
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        RedirectView view = (RedirectView)mAndV.getView();
        assertEquals("foo/California/foo-Alameda/foo", view.getUrl());
        verify(_mockSchoolDao);

        //

        reset(_mockSchoolDao);
        getRequest().setParameter(MssLocalRedirectController.PARAM_SCHOOL_ID, "2");
        getRequest().setParameter(MssLocalRedirectController.PARAM_PAGE, "2");
        expect(_mockSchoolDao.getSchoolById(State.CA, 2)).andReturn(makeSchool(State.CA, 2, "San Francisco"));
        replay(_mockSchoolDao);
        mAndV = _controller.handleRequest(getRequest(), getResponse());
        view = (RedirectView)mAndV.getView();
        assertEquals("California-bar/San-Francisco/bar-San-Francisco", view.getUrl());
        verify(_mockSchoolDao);
    }

    private School makeSchool(State s, Integer id, String city) {
        School school = new School();
        school.setDatabaseState(s);
        school.setId(id);
        school.setActive(true);
        school.setCity(city);
        school.setLevelCode(LevelCode.ELEMENTARY);
        return school;
    }
}
