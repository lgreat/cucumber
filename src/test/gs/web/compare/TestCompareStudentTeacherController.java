package gs.web.compare;

import gs.data.school.census.ICensusDataSetDao;
import gs.data.school.census.ICensusInfo;
import gs.web.BaseControllerTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static gs.web.compare.AbstractCompareSchoolController.MODEL_TAB;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TestCompareStudentTeacherController extends BaseControllerTestCase {
    private CompareStudentTeacherController _controller;
    private ICensusDataSetDao _censusDataSetDao;
    private ICensusInfo _censusInfo;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CompareStudentTeacherController();

        _censusDataSetDao = createStrictMock(ICensusDataSetDao.class);
        _censusInfo = createStrictMock(ICensusInfo.class);

        _controller.setSuccessView("success");
        _controller.setCensusDataSetDao(_censusDataSetDao);
        _controller.setCensusInfo(_censusInfo);
    }

    private void replayAllMocks() {
        replayMocks(_censusDataSetDao, _censusInfo);
    }

    private void verifyAllMocks() {
        verifyMocks(_censusDataSetDao, _censusInfo);
    }

//    private void resetAllMocks() {
//        resetMocks(_censusDataSetDao, _censusInfo);
//    }

    public void testBasics() {
        assertEquals("success", _controller.getSuccessView());
        assertSame(_censusDataSetDao, _controller.getCensusDataSetDao());
        assertSame(_censusInfo, _controller.getCensusInfo());
        assertEquals(ComparedSchoolStudentTeacherStruct.class, _controller.getStruct().getClass());
    }

    public void testEmptyList() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        replayAllMocks();
        _controller.handleCompareRequest(getRequest(), getResponse(), 
                                         new ArrayList<ComparedSchoolBaseStruct>(), model);
        verifyAllMocks();
        assertEquals(CompareStudentTeacherController.TAB_NAME, model.get(MODEL_TAB));
    }
}
