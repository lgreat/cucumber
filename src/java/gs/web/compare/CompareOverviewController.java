package gs.web.compare;

import gs.data.state.State;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.util.PageHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareOverviewController extends AbstractCompareSchoolController {
    private String _successView;

    private IRatingsConfigDao _ratingsConfigDao;
    private TestManager _testManager;

    @Override
    protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                        List<ComparedSchoolBaseStruct> schools, Map<String, Object> model) throws
                                                                                                       IOException {
        model.put(MODEL_TAB, "overview");
        handleGSRating(request, schools);
    }

    protected void handleGSRating(HttpServletRequest request, List<ComparedSchoolBaseStruct> schools) throws IOException {
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        boolean isFromCache = true;
        if (pageHelper != null && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer()) {
            isFromCache = false;
        }

        State state = schools.get(0).getSchool().getDatabaseState();
        IRatingsConfig ratingsConfig = _ratingsConfigDao.restoreRatingsConfig(state, isFromCache);

        if (null != ratingsConfig) {
            for (ComparedSchoolBaseStruct baseStruct: schools) {
                handleGSRating((ComparedSchoolOverviewStruct)baseStruct, ratingsConfig);
            }
        }
    }

    protected void handleGSRating(ComparedSchoolOverviewStruct struct, IRatingsConfig ratingsConfig) {
        SchoolTestValue schoolTestValue =
                _testManager.getOverallRating(struct.getSchool(), ratingsConfig.getYear());
        if (null != schoolTestValue && null != schoolTestValue.getValueInteger()) {
            struct.setGsRating(schoolTestValue.getValueInteger());
        }
    }

    @Override
    protected ComparedSchoolBaseStruct getStruct() {
        return new ComparedSchoolOverviewStruct();
    }

    @Override
    public String getSuccessView() {
        return _successView;
    }

    public void setSuccessView(String successView) {
        _successView = successView;
    }

    public IRatingsConfigDao getRatingsConfigDao() {
        return _ratingsConfigDao;
    }

    public void setRatingsConfigDao(IRatingsConfigDao ratingsConfigDao) {
        _ratingsConfigDao = ratingsConfigDao;
    }

    public TestManager getTestManager() {
        return _testManager;
    }

    public void setTestManager(TestManager testManager) {
        _testManager = testManager;
    }
}
