package gs.web.school;

import gs.data.test.ITestDataSetDao;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.test.rating.*;
import gs.web.util.PageHelper;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component("schoolProfileRatingsHelper")
public class SchoolProfileRatingsHelper {

    public static final String BEAN_ID = "schoolProfileRatingsHelper";
    private final static Logger _log = Logger.getLogger(SchoolProfileRatingsHelper.class);

    private IRatingsConfigDao _ratingsConfigDao;
    private ITestDataSetDao _testDataSetDao;
    private TestManager _testManager;

    /**
     * Populates the RatingsCommand object by setting ratings year, overall rating, and ratings display
     * @param request
     * @param ratingsCommand command object that already has school and state set
     * @param showingSubjectGroups
     * @throws Exception
     */
    public void populateRatingsCommandWithData(HttpServletRequest request, RatingsCommand ratingsCommand, boolean showingSubjectGroups) throws Exception {
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        boolean isFromCache = true;

        if (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer()) {
            isFromCache = false;
        }
        IRatingsConfig ratingsConfig = _ratingsConfigDao.restoreRatingsConfig(ratingsCommand.getState(), isFromCache);

        if (null != ratingsConfig) {
            ratingsCommand.setRatingYear(ratingsConfig.getYear());

            SchoolTestValue schoolTestValue =
                    _testManager.getOverallRating(ratingsCommand.getSchool(), ratingsConfig.getYear());

            if (null != schoolTestValue && null != schoolTestValue.getValueInteger()) {
                IRatingsDisplay.IRowGroup.IRow.ICell overallRatingCell = new Cell(schoolTestValue.getValueInteger());
                ratingsCommand.setOverallRating(overallRatingCell);

                SchoolRatingsDisplay ratingsDisplay =
                        new SchoolRatingsDisplay(ratingsConfig, ratingsCommand.getSchool(), _testDataSetDao);

                if (showingSubjectGroups) {
                    ratingsCommand.setRatingsDisplay(ratingsDisplay);
                } else {
                    OverallRatingDecorator ratingDecorator = new OverallRatingDecorator(ratingsDisplay);
                    ratingsCommand.setRatingsDisplay(ratingDecorator);
                }
            }
        }
    }

    public void setTestManager(TestManager testManager) {
        _testManager = testManager;
    }

    public void setRatingsConfigDao(final IRatingsConfigDao ratingsConfigDao) {
        _ratingsConfigDao = ratingsConfigDao;
    }

    public void setTestDataSetDao(final ITestDataSetDao testDataSetDao) {
        _testDataSetDao = testDataSetDao;
    }
}


