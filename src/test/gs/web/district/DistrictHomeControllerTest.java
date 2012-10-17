package gs.web.district;

import gs.data.school.district.*;
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
    private IDistrictBoilerplateDao _districtBoilerplateDao;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new DistrictHomeController();
        _districtStateLevelBoilerplateDao = createStrictMock(IDistrictStateLevelBoilerplateDao.class);
        _districtBoilerplateDao = createStrictMock(IDistrictBoilerplateDao.class);

        _controller.setDistrictStateLevelBoilerplateDao(_districtStateLevelBoilerplateDao);
        _controller.setDistrictBoilerplateDao(_districtBoilerplateDao);
    }

    private void replayAllMocks() {
        replayMocks(_districtStateLevelBoilerplateDao, _districtBoilerplateDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_districtStateLevelBoilerplateDao, _districtBoilerplateDao);
    }

    public void testBasics() {
        assertNotNull(_controller);
        assertSame(_districtStateLevelBoilerplateDao, _controller.getDistrictStateLevelBoilerplateDao());
        assertSame(_districtBoilerplateDao, _controller.getDistrictBoilerplateDao());
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

    public void testGetBoilerPlateForDistrict_Null() {
        Map<String, Object> model = new HashMap<String, Object>();
        District d = new District();
        d.setId(1);
        d.setDatabaseState(State.CA);
        expect(_districtBoilerplateDao.getByDistrict(d)).andReturn(null);
        replayAllMocks();
        try {
            _controller.getBoilerPlateForDistrict(d, model);
        } catch (Exception e) {
            fail("Do not expect exception when no boilerplate is found.");
        }
        verifyAllMocks();
        assertEquals(1, model.size());
        assertNotNull(model.get("isDistrictBoilerplatePresent"));
        assertEquals(Boolean.FALSE, model.get("isDistrictBoilerplatePresent"));

    }

    public void testGetBoilerPlateForDistrict() {
        Map<String, Object> model = new HashMap<String, Object>();
        District d = new District();
        d.setId(1);
        d.setDatabaseState(State.CA);
        d.setName("Foo District");
        DistrictBoilerplate boilerplate = new DistrictBoilerplate();
        boilerplate.setDistrictId(1);
        boilerplate.setState(State.CA);
        boilerplate.setAcronym("acr");
        boilerplate.setChoiceLink("cho");
        boilerplate.setLocatorLink("loc");
        boilerplate.setSuperintendent("sup");
        boilerplate.setBoilerplate("boi");
        boilerplate.setHeading("hea");
        expect(_districtBoilerplateDao.getByDistrict(d)).andReturn(boilerplate);
        replayAllMocks();
        try {
            _controller.getBoilerPlateForDistrict(d, model);
        } catch (Exception e) {
            fail("Do not expect exception when no boilerplate is found.");
        }
        verifyAllMocks();
        assertEquals(10, model.size());
        assertNotNull(model.get("isDistrictBoilerplatePresent"));
        assertEquals(Boolean.TRUE, model.get("isDistrictBoilerplatePresent"));
        assertEquals(1, model.get("id"));
        assertEquals(State.CA, model.get("state"));
        assertEquals("Foo District", model.get("name"));
        assertEquals("acr", model.get("acronym"));
        assertEquals("cho", model.get("choicelink"));
        assertEquals("loc", model.get("locatorlink"));
        assertEquals("sup", model.get("superintendent"));
        assertEquals("boi", model.get("boilerplate"));
        assertEquals("hea", model.get("districtBoilerplateHeading"));
    }
}
