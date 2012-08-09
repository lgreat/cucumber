package gs.web.school;

import gs.data.school.School;
import gs.data.test.ITestDataSetDao;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class RatingHelper {
    private IRatingsConfigDao _ratingsConfigDao;
    private ITestDataSetDao _testDataSetDao;
    private TestManager _testManager;

    protected static final Log _log = LogFactory.getLog(RatingHelper.class.getName());

    public Integer getGreatSchoolsOverallRating(School school, boolean useCache) {
        Integer greatSchoolsRating = null;

        Integer year = null;
        if (school.isSchoolForNewProfile()) {
            year = getTestDataSetDao().getLatestRatingYear(school.getDatabaseState());
        } else {
            IRatingsConfig ratingsConfig = null;
            try {
                ratingsConfig = getRatingsConfigDao().restoreRatingsConfig(school.getDatabaseState(), useCache);
            } catch (IOException e) {
                _log.debug("Failed to get ratings config from ratings config dao", e);
            }
            if (null != ratingsConfig) {
                year = ratingsConfig.getYear();
            }
        }

        if (null != year) {
            SchoolTestValue schoolTestValue = getTestManager().getOverallRating(school, year);

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

    public ITestDataSetDao getTestDataSetDao() {
        return _testDataSetDao;
    }

    public void setTestDataSetDao(ITestDataSetDao testDataSetDao) {
        _testDataSetDao = testDataSetDao;
    }
}
