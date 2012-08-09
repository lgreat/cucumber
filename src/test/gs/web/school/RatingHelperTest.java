package gs.web.school;

import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.ITestDataSetDao;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfigDao;
import gs.data.test.rating.RatingsConfigBean;
import gs.web.BaseTestCase;

import java.io.IOException;

import static org.easymock.classextension.EasyMock.*;

public class RatingHelperTest extends BaseTestCase {
    RatingHelper _ratingHelper = new RatingHelper();
    IRatingsConfigDao _ratingsConfigDao;
    ITestDataSetDao _testDataSetDao;
    TestManager testManager;

    public void setUp() throws Exception {
        _ratingHelper = new RatingHelper();
        _ratingsConfigDao = createStrictMock(IRatingsConfigDao.class);
        _testDataSetDao = createStrictMock(ITestDataSetDao.class);
        testManager = createStrictMock(TestManager.class);
        _ratingHelper.setRatingsConfigDao(_ratingsConfigDao);
        _ratingHelper.setTestManager(testManager);
        _ratingHelper.setTestDataSetDao(_testDataSetDao);
    }

    public void replayAll() {
        super.replayMocks(_ratingsConfigDao, _testDataSetDao, testManager);
    }

    public void verifyAll() {
        super.verifyMocks(_ratingsConfigDao, _testDataSetDao, testManager);
    }

    public void testGetNewGreatSchoolsOverallRating() throws Exception {
        School school = new School();
        school.setId(1);
        school.setNewProfileSchool(1);
        school.setDatabaseState(State.CA);

        SchoolTestValue schoolTestValue = new SchoolTestValue();
        schoolTestValue.setValueFloat(9.0f);

        expect(_testDataSetDao.getLatestRatingYear(school.getDatabaseState())).andReturn(2012);
        expect(testManager.getOverallRating(school, 2012)).andReturn(schoolTestValue);
        replayAll();

        float answer = _ratingHelper.getGreatSchoolsOverallRating(school, false);

        assertEquals("Should have received correct value.", 9.0f, answer);

        verifyAll();
    }

    public void testGetGreatSchoolsOverallRating() throws Exception {
        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);

        RatingsConfigBean config = new RatingsConfigBean();
        config.setYear(2000);

        SchoolTestValue schoolTestValue = new SchoolTestValue();
        schoolTestValue.setValueFloat(90f);

        expect(_ratingsConfigDao.restoreRatingsConfig(eq(school.getDatabaseState()), eq(false))).andReturn(config);
        expect(testManager.getOverallRating(eq(school), eq(config.getYear()))).andReturn(schoolTestValue);
        replayAll();
        
        float answer = _ratingHelper.getGreatSchoolsOverallRating(school, false);

        assertEquals("Should have received correct value.", 90f, answer);
        
        verifyAll();
    }

    public void testGetGreatSchoolsOverallRatingNullRatingConfig() throws Exception {
        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);

        RatingsConfigBean config = new RatingsConfigBean();
        config.setYear(2000);

        expect(_ratingsConfigDao.restoreRatingsConfig(eq(school.getDatabaseState()), eq(false))).andThrow(new IOException());
        replayAll();
        
        Integer answer = _ratingHelper.getGreatSchoolsOverallRating(school, false);

        assertNull("Should have received null value.", answer);

        verifyAll();
    }

    public void testGetGreatSchoolsOverallRatingNullValue() throws Exception {
        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);

        RatingsConfigBean config = new RatingsConfigBean();
        config.setYear(2000);

        expect(_ratingsConfigDao.restoreRatingsConfig(eq(school.getDatabaseState()), eq(false))).andReturn(config);
        expect(testManager.getOverallRating(eq(school),eq(config.getYear()))).andReturn(null);
        replayAll();
        
        Integer answer = _ratingHelper.getGreatSchoolsOverallRating(school, false);

        assertNull("Should have received correct value.", answer);
        
        verifyAll();

    }
}
