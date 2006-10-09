/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RatingControllerTest.java,v 1.3 2006/10/09 18:27:19 dlee Exp $
 */
package gs.web.test.rating;

import gs.data.school.Grade;
import gs.data.school.Grades;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.ITestDataSetDao;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestDataSet;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.BaseControllerTestCase;
import org.easymock.MockControl;
import org.springframework.validation.BindException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class RatingControllerTest extends BaseControllerTestCase {
    RatingsController _controller;
    State _state;
    Integer _schoolId;
    School _school;

    protected void setUp() throws Exception {
        super.setUp();

        _state = State.CA;
        _schoolId = Integer.valueOf("1");
        _controller = (RatingsController) getApplicationContext().getBean("/school/rating.page");

        MockControl schoolResultControl = MockControl.createControl(ISchoolDao.class);
        ISchoolDao mockSchoolDao = (ISchoolDao) schoolResultControl.getMock();
        mockSchoolDao.getSchoolById(_state, _schoolId);

        _school = new School();
        _school.setGradeLevels(Grades.createGrades(Grade.G_1, Grade.G_10));
        schoolResultControl.setDefaultReturnValue(_school);
        schoolResultControl.replay();
        _controller.setSchoolDao(mockSchoolDao);
    }

    public void testOnBindNewForm() {

        RatingsCommand command = new RatingsCommand();
        command.setId(_schoolId.intValue());
        command.setState(_state);

        BindException errors = new BindException(command, "");
        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertEquals(false, errors.hasErrors());
    }

    public void testReferenceDataSkipsProcessingIfError() throws Exception {
        RatingsCommand command = new RatingsCommand();
        BindException errors = new BindException(command, "");
        errors.reject("set some error");

        Map model = _controller.referenceData(getRequest(), command, errors);
        command = (RatingsCommand) model.get(_controller.getCommandName());

        assertEquals(null, command.getOverallRating());
        assertEquals(null, command.getRatingsDisplay());
    }

    public void testReferenceDataNoRatingConfig() throws Exception {
        MockControl ratingsConfigControl = MockControl.createControl(IRatingsConfigDao.class);
        IRatingsConfigDao mockRatingsConfigDao = (IRatingsConfigDao) ratingsConfigControl.getMock();
        mockRatingsConfigDao.restoreRatingsConfig(_state, true);
        ratingsConfigControl.setDefaultReturnValue(null);
        ratingsConfigControl.replay();
        _controller.setRatingsConfigDao(mockRatingsConfigDao);

        RatingsCommand command = new RatingsCommand();
        BindException errors = new BindException(command, "");
        Map model = _controller.referenceData(getRequest(), command, errors);
        command = (RatingsCommand) model.get(_controller.getCommandName());

        assertEquals(null, command.getOverallRating());
        assertEquals(null, command.getRatingsDisplay());
    }

    public void testReferenceData() throws Exception {
        //set up IRatingConfig
        MockControl ratingsConfigControl = MockControl.createControl(IRatingsConfig.class);
        IRatingsConfig mockRatingsConfig = (IRatingsConfig) ratingsConfigControl.getMock();

        mockRatingsConfig.getDataSetIds(null, null);
        ratingsConfigControl.setDefaultReturnValue(null);

        mockRatingsConfig.getRowGroupConfigs();
        ratingsConfigControl.setDefaultReturnValue(new IRatingsConfig.IRowGroupConfig[] {});

        mockRatingsConfig.getState();
        ratingsConfigControl.setDefaultReturnValue(_state);

        mockRatingsConfig.getSubjectGroupConfigs();
        ratingsConfigControl.setDefaultReturnValue(new IRatingsConfig.ISubjectGroupConfig[] {});

        mockRatingsConfig.getYear();
        ratingsConfigControl.setDefaultReturnValue(2004);

        ratingsConfigControl.replay();
        //end set up of IRatingConfig



        //Set up IRatingConfigDao
        MockControl ratingsConfigDaoControl = MockControl.createControl(IRatingsConfigDao.class);
        IRatingsConfigDao mockRatingsConfigDao = (IRatingsConfigDao) ratingsConfigDaoControl.getMock();
        mockRatingsConfigDao.restoreRatingsConfig(_state, false);
        ratingsConfigDaoControl.setDefaultReturnValue(mockRatingsConfig);
        ratingsConfigDaoControl.replay();
        //end set up of IRatingConfigDao
        _controller.setRatingsConfigDao(mockRatingsConfigDao);

        //set up test manager
        //1) mock testDataSetDao
        MockControl testDataSetDaoControl = MockControl.createControl(ITestDataSetDao.class);
        ITestDataSetDao mockTestDataSetDao = (ITestDataSetDao) testDataSetDaoControl.getMock();
        mockTestDataSetDao.findDataSets(null, 0, null, null, null, null, null, true);
        List dataSets = new ArrayList();
        dataSets.add(new TestDataSet());
        testDataSetDaoControl.setDefaultReturnValue(dataSets);
        mockTestDataSetDao.findValue(null, null);
        SchoolTestValue value = new SchoolTestValue();
        value.setValueFloat(new Float("10"));
        testDataSetDaoControl.setDefaultReturnValue(value);
        testDataSetDaoControl.replay();

        TestManager testManager = new TestManager();
        testManager.setTestDataSetDao(mockTestDataSetDao);
        testManager.getOverallRating(_school, 2004);
        _controller.setTestManager(testManager);
        _controller.setTestDataSetDao(mockTestDataSetDao);

        RatingsCommand command = new RatingsCommand();
        command.setSchool(_school);
        _controller.setShowingSubjectGroups(false);
        BindException errors = new BindException(command, "");


        Map model = _controller.referenceData(getRequest(), command, errors);
        command = (RatingsCommand) model.get(_controller.getCommandName());

        assertEquals(Integer.valueOf("10"), command.getOverallRating().getRating());
        assertNotNull(command.getRatingsDisplay());

    }
}
