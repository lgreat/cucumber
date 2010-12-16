package gs.web.school;

import gs.data.school.School;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.util.PageHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class RatingHelper {
    private IRatingsConfigDao _ratingsConfigDao;
    private TestManager _testManager;

    protected static final Log _log = LogFactory.getLog(RatingHelper.class.getName());

    public Integer getGreatSchoolsOverallRating(School school, boolean useCache) {
        Integer greatSchoolsRating = null;
        IRatingsConfig ratingsConfig = null;

        try {
            ratingsConfig = getRatingsConfigDao().restoreRatingsConfig(school.getDatabaseState(), useCache);
        } catch (IOException e) {
            _log.debug("Failed to get ratings config from ratings config dao", e);
        }

        if (null != ratingsConfig) {
            SchoolTestValue schoolTestValue = getTestManager().getOverallRating(school, ratingsConfig.getYear());

            if (null != schoolTestValue && null != schoolTestValue.getValueInteger()) {
                greatSchoolsRating = schoolTestValue.getValueInteger();
            }
        }

        return greatSchoolsRating;
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
