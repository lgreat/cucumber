/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolRatingsDisplayTest.java,v 1.5 2006/10/07 00:42:10 dlee Exp $
 */

package gs.web.test.rating;

import gs.data.school.Grade;
import gs.data.school.Grades;
import gs.data.school.School;
import gs.data.test.ITestDataSetDao;
import gs.data.test.SchoolTestValue;
import gs.data.test.Subject;
import gs.data.test.TestDataSet;
import gs.data.test.rating.IRatingsConfig;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.MockControl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Tests SchoolRatingsDisplay.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SchoolRatingsDisplayTest extends TestCase {
    private MockControl _ratingsConfigControl;
    private IRatingsConfig _ratingsConfig;

    private School _school;

    private SchoolRatingsDisplay _schoolRatingsDisplay;

    protected final Log _log = LogFactory.getLog(SchoolRatingsDisplayTest.class);

    protected void setUp() throws Exception {
        super.setUp();

        _ratingsConfigControl = MockControl.createControl(IRatingsConfig.class);
        _ratingsConfig = (IRatingsConfig) _ratingsConfigControl.getMock();

        MockControl testDataSetDaoControl = MockControl.createControl(ITestDataSetDao.class);
        ITestDataSetDao testDataSetDao = (ITestDataSetDao) testDataSetDaoControl.getMock();


        _school = new School();

        _ratingsConfigControl.expectAndReturn(_ratingsConfig.getSubjectGroupConfigs(), new IRatingsConfig.ISubjectGroupConfig[] {});
        _ratingsConfigControl.expectAndReturn(_ratingsConfig.getRowGroupConfigs(), new IRatingsConfig.IRowGroupConfig[] {});
        _ratingsConfigControl.replay();
        _schoolRatingsDisplay = new SchoolRatingsDisplay(_ratingsConfig, _school, testDataSetDao, null);
        _ratingsConfigControl.reset();
    }

    public void testEmpty() {

        StubRatingsConfig stubRatingsConfig = new StubRatingsConfig();

        School school = new School();
        school.setGradeLevels(Grades.createGrades(Grade.G_1, Grade.G_11));

        Map rowAndSubjectToDataSet = stubRatingsConfig.getRowSubjectToDataSet();
        Map dataSetToValue = stubRatingsConfig.getDatasetToValue();
        List subjects = stubRatingsConfig.getSubjects();
        List rowLabels = stubRatingsConfig.getRowLabels();

        MockControl testDataSetDaoControl = MockControl.createControl(ITestDataSetDao.class);
        ITestDataSetDao testDataSetDao = (ITestDataSetDao) testDataSetDaoControl.getMock();


        for (Iterator rowLabelIter = rowLabels.iterator(); rowLabelIter.hasNext(); ) {
            String rowLabel = (String) rowLabelIter.next();

            for (Iterator subjectIter = subjects.iterator(); subjectIter.hasNext(); ) {
                Subject subject = (Subject) subjectIter.next();

                String key = rowLabel + String.valueOf(subject.getSubjectId());

                TestDataSet testDataSet = (TestDataSet) rowAndSubjectToDataSet.get(key);
                SchoolTestValue schoolValue = (SchoolTestValue) dataSetToValue.get(testDataSet);

                testDataSetDaoControl.expectAndReturn(testDataSetDao.findTestDataSet(StubRatingsConfig.STATE, testDataSet.getId().intValue()), testDataSet );
                testDataSetDaoControl.expectAndReturn(testDataSetDao.findValue(testDataSet, school), schoolValue);
            }
        }

        testDataSetDaoControl.replay();

        SchoolRatingsDisplay schoolRatingsDisplay = new SchoolRatingsDisplay(stubRatingsConfig, school, testDataSetDao, null);
        assertNotNull(schoolRatingsDisplay.getRowGroups());
        assertNotNull(schoolRatingsDisplay.getSubjectGroupLabels());


        List rowGroups = schoolRatingsDisplay.getRowGroups();

        for (Iterator rowGroupIter = rowGroups.iterator(); rowGroupIter.hasNext(); ) {
            IRatingsDisplay.IRowGroup rowGroup = (IRatingsDisplay.IRowGroup) rowGroupIter.next();
            _log.debug(rowGroup.getLabel());
            List rows = rowGroup.getRows();

            for (Iterator rowIter = rows.iterator(); rowIter.hasNext();) {
                IRatingsDisplay.IRowGroup.IRow row = (IRatingsDisplay.IRowGroup.IRow) rowIter.next();
                _log.debug(row.getLabel());
                List cells = row.getCells();

                for (Iterator cellIter = cells.iterator(); cellIter.hasNext(); ) {
                    IRatingsDisplay.IRowGroup.IRow.ICell cell = (IRatingsDisplay.IRowGroup.IRow.ICell) cellIter.next();
                    _log.debug(cell.getRating());
                }
            }
        }
    }
}
