package gs.web.district;

import gs.data.school.district.DistrictStateLevelBoilerplate;
import gs.data.school.district.IDistrictStateLevelBoilerplateDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author aroy@greatschools.org
 */
public class DistrictHomeControllerTest extends BaseControllerTestCase {
    DistrictHomeController _controller;
    private IDistrictStateLevelBoilerplateDao _districtStateLevelBoilerplateDao;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new DistrictHomeController();
        _districtStateLevelBoilerplateDao = createStrictMock(IDistrictStateLevelBoilerplateDao.class);

        _controller.setDistrictStateLevelBoilerplateDao(_districtStateLevelBoilerplateDao);
    }

    private void replayAllMocks() {
        replayMocks(_districtStateLevelBoilerplateDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_districtStateLevelBoilerplateDao);
    }

    public void testBasics() {
        assertNotNull(_controller);
        assertSame(_districtStateLevelBoilerplateDao, _controller.getDistrictStateLevelBoilerplateDao());
    }
    
    public void testGetBoilerPlateForState_Null() {
        Map<String, Object> model = new HashMap<String, Object>();
        expect(_districtStateLevelBoilerplateDao.getByState(State.CA)).andReturn(null);
        replayAllMocks();
        try {
            _controller.getBoilerPlateForState(State.CA,  model);
        } catch (Exception e) {
            fail("Do not expect exception when no boilerplate found.");
        }
        verifyAllMocks();
        assertEquals(2, model.size());
        assertNotNull(model.get("stateBoilerplate"));
        assertEquals("", model.get("stateBoilerplate"));
        assertNotNull(model.get("stateBoilerplateHeading"));
        assertEquals("", model.get("stateBoilerplateHeading"));
    }

    public void testGetBoilerPlateForState() {
        Map<String, Object> model = new HashMap<String, Object>();
        DistrictStateLevelBoilerplate boilerplate = new DistrictStateLevelBoilerplate();
        boilerplate.setState(State.CA);
        boilerplate.setBoilerplate("foo\nbar");
        boilerplate.setHeading("taz");
        expect(_districtStateLevelBoilerplateDao.getByState(State.CA)).andReturn(boilerplate);
        replayAllMocks();
        _controller.getBoilerPlateForState(State.CA,  model);
        verifyAllMocks();
        assertEquals(2, model.size());
        assertNotNull(model.get("stateBoilerplate"));
        assertEquals("foo<br/>bar", model.get("stateBoilerplate"));
        assertNotNull(model.get("stateBoilerplateHeading"));
        assertEquals("taz", model.get("stateBoilerplateHeading"));
    }
}
