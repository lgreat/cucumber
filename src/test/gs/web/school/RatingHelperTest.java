package gs.web.school;

import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.data.test.rating.RatingsConfigBean;
import gs.data.test.rating.RatingsConfigDao;
import junit.framework.TestCase;

import java.io.IOException;

import static org.easymock.EasyMock.*;

public class RatingHelperTest extends TestCase {
    RatingHelper _ratingHelper = new RatingHelper();
    IRatingsConfigDao _ratingsConfigDao;
    TestManager testManager;

    public void setUp() throws Exception {
        _ratingHelper = new RatingHelper();
        _ratingsConfigDao = createStrictMock(IRatingsConfigDao.class);
        testManager = org.easymock.classextension.EasyMock.createStrictMock(TestManager.class);
        _ratingHelper.setRatingsConfigDao(_ratingsConfigDao);
        _ratingHelper.setTestManager(testManager);
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
        org.easymock.classextension.EasyMock.expect(testManager.getOverallRating(eq(school), eq(config.getYear()))).andReturn(schoolTestValue);
        replay(_ratingsConfigDao);
        org.easymock.classextension.EasyMock.replay(testManager);
        
        float answer = _ratingHelper.getGreatSchoolsOverallRating(school, false);

        assertEquals("Should have received correct value.", 90f, answer);
        
        verify(_ratingsConfigDao);
        org.easymock.classextension.EasyMock.verify(testManager);
    }

    public void testGetGreatSchoolsOverallRatingNullRatingConfig() throws Exception {
        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);

        RatingsConfigBean config = new RatingsConfigBean();
        config.setYear(2000);

        expect(_ratingsConfigDao.restoreRatingsConfig(eq(school.getDatabaseState()), eq(false))).andThrow(new IOException());
        replay(_ratingsConfigDao);
        
        Integer answer = _ratingHelper.getGreatSchoolsOverallRating(school, false);

        assertNull("Should have received null value.", answer);

        verify(_ratingsConfigDao);
    }

    public void testGetGreatSchoolsOverallRatingNullValue() throws Exception {
        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);

        RatingsConfigBean config = new RatingsConfigBean();
        config.setYear(2000);

        expect(_ratingsConfigDao.restoreRatingsConfig(eq(school.getDatabaseState()), eq(false))).andReturn(config);
        org.easymock.classextension.EasyMock.expect(testManager.getOverallRating(eq(school),eq(config.getYear()))).andReturn(null);
        replay(_ratingsConfigDao);
        org.easymock.classextension.EasyMock.replay(testManager);
        
        Integer answer = _ratingHelper.getGreatSchoolsOverallRating(school, false);

        assertNull("Should have received correct value.", answer);
        
        verify(_ratingsConfigDao);

    }
}
