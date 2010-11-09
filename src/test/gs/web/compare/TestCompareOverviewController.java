package gs.web.compare;

import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.BaseControllerTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TestCompareOverviewController extends BaseControllerTestCase {
    private CompareOverviewController _controller;
    private IRatingsConfigDao _ratingsConfigDao;
    private TestManager _testManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CompareOverviewController();

        _ratingsConfigDao = createStrictMock(IRatingsConfigDao.class);
        _testManager = createStrictMock(TestManager.class);

        _controller.setRatingsConfigDao(_ratingsConfigDao);
        _controller.setTestManager(_testManager);
        _controller.setSuccessView("success");
    }

    private void replayAllMocks() {
        replayMocks(_ratingsConfigDao, _testManager);
    }

    private void verifyAllMocks() {
        verifyMocks(_ratingsConfigDao, _testManager);
    }

//    private void resetAllMocks() {
//        resetMocks(_ratingsConfigDao, _testManager);
//    }

    public void testBasics() {
        assertSame(_ratingsConfigDao, _controller.getRatingsConfigDao());
        assertSame(_testManager, _controller.getTestManager());
        assertEquals("success", _controller.getSuccessView());
    }

    public void testHandleGSRatingSingle() {
        ComparedSchoolOverviewStruct struct = new ComparedSchoolOverviewStruct();
        School school = new School();
        struct.setSchool(school);

        IRatingsConfig ratingsConfig = createStrictMock(IRatingsConfig.class);
        SchoolTestValue stv = new SchoolTestValue();
        stv.setValueFloat(3f);

        expect(ratingsConfig.getYear()).andReturn(2010);
        expect(_testManager.getOverallRating(school, 2010)).andReturn(stv);
        replayAllMocks();
        replay(ratingsConfig);
        _controller.handleGSRating(struct, ratingsConfig);
        verifyAllMocks();
        verify(ratingsConfig);
        assertNotNull(struct.getGsRating());
        assertEquals(3, struct.getGsRating().intValue());
    }

    public void testHandleGSRating() throws Exception {
        List<ComparedSchoolBaseStruct> structs = new ArrayList<ComparedSchoolBaseStruct>();
        ComparedSchoolOverviewStruct struct1 = new ComparedSchoolOverviewStruct();
        School school1 = new School();
        school1.setDatabaseState(State.CA);
        struct1.setSchool(school1);

        ComparedSchoolOverviewStruct struct2 = new ComparedSchoolOverviewStruct();
        School school2 = new School();
        struct2.setSchool(school2);

        structs.add(struct1);
        structs.add(struct2);

        SchoolTestValue stv1 = new SchoolTestValue();
        stv1.setValueFloat(3f);

        SchoolTestValue stv2 = new SchoolTestValue();
        stv2.setValueFloat(6f);

        IRatingsConfig ratingsConfig = createStrictMock(IRatingsConfig.class);
        expect(_ratingsConfigDao.restoreRatingsConfig(State.CA, true)).andReturn(ratingsConfig);
        expect(ratingsConfig.getYear()).andReturn(2010);
        expect(_testManager.getOverallRating(school1, 2010)).andReturn(stv1);
        expect(ratingsConfig.getYear()).andReturn(2010);
        expect(_testManager.getOverallRating(school2, 2010)).andReturn(stv2);
        replayAllMocks();
        replay(ratingsConfig);
        _controller.handleGSRating(getRequest(), structs);
        verifyAllMocks();
        verify(ratingsConfig);
        assertNotNull(struct1.getGsRating());
        assertEquals(3, struct1.getGsRating().intValue());
        assertNotNull(struct2.getGsRating());
        assertEquals(6, struct2.getGsRating().intValue());
    }
}