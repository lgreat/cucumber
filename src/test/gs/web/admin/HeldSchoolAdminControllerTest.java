package gs.web.admin;

import gs.data.school.HeldSchool;
import gs.data.school.IHeldSchoolDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class HeldSchoolAdminControllerTest extends BaseControllerTestCase {
    private HeldSchoolAdminController _controller;
    private IHeldSchoolDao _heldSchoolDao;
    private ISchoolDao _schoolDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new HeldSchoolAdminController();

        _heldSchoolDao = createStrictMock(IHeldSchoolDao.class);
        _schoolDao = createStrictMock(ISchoolDao.class);

        _controller.setHeldSchoolDao(_heldSchoolDao);
        _controller.setSchoolDao(_schoolDao);
        _controller.setViewName("view");
    }

    public void testBasics() {
        assertSame(_heldSchoolDao, _controller.getHeldSchoolDao());
        assertSame(_schoolDao, _controller.getSchoolDao());
        assertEquals("view", _controller.getViewName());
    }

    protected void replayAllMocks() {
        replayMocks(_heldSchoolDao, _schoolDao);
    }

    protected void verifyAllMocks() {
        verifyMocks(_heldSchoolDao, _schoolDao);
    }

    public void testGetAllHeldSchools() {
        List<HeldSchool> expectedSchools = new ArrayList<HeldSchool>(2);
        expectedSchools.add(new HeldSchool(1, State.CA, "first"));
        expectedSchools.add(new HeldSchool(2, State.CA, "second"));

        expect(_heldSchoolDao.getAll()).andReturn(expectedSchools);

        replayAllMocks();
        List<HeldSchool> heldSchools = _controller.getAllHeldSchools();
        verifyAllMocks();
        assertNotNull(heldSchools);
        assertEquals(2, heldSchools.size());

        // test manual wiring of school dao
        reset(_schoolDao);
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(new School());
        expect(_schoolDao.getSchoolById(State.CA, 2)).andReturn(new School());
        replay(_schoolDao);
        for (HeldSchool heldSchool: heldSchools) {
            assertNotNull(heldSchool.getSchool());
        }
        verify(_schoolDao);
    }

    public void testRemoveSchoolFromHold() {
        getRequest().setParameter(HeldSchoolAdminController.PARAM_HELD_SCHOOL_ID, "5");

        HeldSchool heldSchool = new HeldSchool();
        expect(_heldSchoolDao.getById(5)).andReturn(heldSchool);
        _heldSchoolDao.delete(heldSchool);

        replayAllMocks();
        assertTrue(_controller.removeSchoolFromHold(getRequest()));
        verifyAllMocks();
    }

    public void testAddSchoolToHold() throws HeldSchoolAdminController.DuplicateSchoolException {
        getRequest().setParameter(HeldSchoolAdminController.PARAM_SCHOOL_ID, "1");
        getRequest().setParameter(HeldSchoolAdminController.PARAM_SCHOOL_STATE, "CA");
        getRequest().setParameter(HeldSchoolAdminController.PARAM_NOTES, "notes");

        School school = new School();
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        expect(_heldSchoolDao.isSchoolOnHoldList(school)).andReturn(false);
        _heldSchoolDao.save(isA(HeldSchool.class));

        replayAllMocks();
        assertTrue(_controller.addSchoolToHold(getRequest()));
        verifyAllMocks();
    }

    public void testAddDuplicateSchoolToHold() {
        getRequest().setParameter(HeldSchoolAdminController.PARAM_SCHOOL_ID, "1");
        getRequest().setParameter(HeldSchoolAdminController.PARAM_SCHOOL_STATE, "CA");
        getRequest().setParameter(HeldSchoolAdminController.PARAM_NOTES, "notes");

        School school = new School();
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        expect(_heldSchoolDao.isSchoolOnHoldList(school)).andReturn(true);

        replayAllMocks();
        try {
            _controller.addSchoolToHold(getRequest());
            fail("Expect duplicate school to be ignored.");
        } catch (HeldSchoolAdminController.DuplicateSchoolException dse) {
            // ok!
        }
        verifyAllMocks();
    }
}
