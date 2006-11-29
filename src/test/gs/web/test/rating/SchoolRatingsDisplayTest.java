/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolRatingsDisplayTest.java,v 1.9 2006/11/29 07:35:01 eddie Exp $
 */

package gs.web.test.rating;

import gs.data.school.Grade;
import gs.data.school.Grades;
import gs.data.school.School;
import gs.data.test.*;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.MockControl;

import java.util.*;

/**
 * Tests SchoolRatingsDisplay.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SchoolRatingsDisplayTest extends TestCase {
    protected final Log _log = LogFactory.getLog(SchoolRatingsDisplayTest.class);

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testEmpty() {
        StubRatingsConfig stubRatingsConfig = new StubRatingsConfig();

        School school = new School();
        school.setGradeLevels(Grades.createGrades(Grade.G_1, Grade.G_4));

        Map rowAndSubjectToDataSet = stubRatingsConfig.getRowSubjectToDataSet();
        Map dataSetToValue = stubRatingsConfig.getDatasetToValue();

        Subject [] subjects = stubRatingsConfig.getSubjects();
        String [] rowLabels = stubRatingsConfig.getRowLabels();

        Map results = new HashMap();
        for (int i=0; i<rowLabels.length; i++) {
            String rowLabel = rowLabels[i];

            for (int j=0; j<subjects.length; j++) {
                Subject subject = subjects[j];

                String key = rowLabel + String.valueOf(subject.getSubjectId());

                TestDataSet testDataSet = (TestDataSet) rowAndSubjectToDataSet.get(key);
                SchoolTestValue schoolValue = (SchoolTestValue) dataSetToValue.get(testDataSet);
                schoolValue.setDataSet(testDataSet);

                results.put(testDataSet.getId(),
                        createMockedRawResultDao(testDataSet.getId(),
                                testDataSet.convertFloatValueToDecile(schoolValue.getValueFloat()),
                                testDataSet));
            }
        }

        MockControl testDataSetDaoControl = MockControl.createControl(ITestDataSetDao.class);
        ITestDataSetDao testDataSetDao = (ITestDataSetDao) testDataSetDaoControl.getMock();
        testDataSetDao.findAllRawResults(null, new int [] {2002}, true);
        testDataSetDaoControl.setDefaultReturnValue(results);

        testDataSetDao.findDataSets(null, 2004, null, null, null, null, null, true,null);
        testDataSetDaoControl.setDefaultReturnValue(new ArrayList());
        testDataSetDaoControl.replay();

        TestManager testManager = new TestManager();
        testManager.setTestDataSetDao(testDataSetDao);

        SchoolRatingsDisplay schoolRatingsDisplay = new SchoolRatingsDisplay(stubRatingsConfig, school,
                testDataSetDao, testManager);

        assertNotNull(schoolRatingsDisplay.getRowGroups());
        assertNotNull(schoolRatingsDisplay.getSubjectGroupLabels());

        List rowGroups = schoolRatingsDisplay.getRowGroups();

        for (Iterator rowGroupIter = rowGroups.iterator(); rowGroupIter.hasNext();) {
            IRatingsDisplay.IRowGroup rowGroup = (IRatingsDisplay.IRowGroup) rowGroupIter.next();
            _log.debug(rowGroup.getLabel());
            List rows = rowGroup.getRows();

            for (Iterator rowIter = rows.iterator(); rowIter.hasNext();) {
                IRatingsDisplay.IRowGroup.IRow row = (IRatingsDisplay.IRowGroup.IRow) rowIter.next();
                _log.debug(row.getLabel());
                List cells = row.getCells();

                for (Iterator cellIter = cells.iterator(); cellIter.hasNext();) {
                    IRatingsDisplay.IRowGroup.IRow.ICell cell = (IRatingsDisplay.IRowGroup.IRow.ICell) cellIter.next();
                    _log.debug(cell.getRating());
                }
            }
        }

        assertEquals(3, rowGroups.size());
        assertEquals("By Grade", ((IRatingsDisplay.IRowGroup) rowGroups.get(0)).getLabel());
        assertEquals("By Gender", ((IRatingsDisplay.IRowGroup) rowGroups.get(1)).getLabel());
        assertEquals("By Ethnicity", ((IRatingsDisplay.IRowGroup) rowGroups.get(2)).getLabel());

        //4 grades
        assertEquals(4, ((IRatingsDisplay.IRowGroup) rowGroups.get(0)).getNumRows());
        //1 gender
        assertEquals(1, ((IRatingsDisplay.IRowGroup) rowGroups.get(1)).getNumRows());
        //4 ethnicities
        assertEquals(4, ((IRatingsDisplay.IRowGroup) rowGroups.get(2)).getNumRows());

        assertEquals("Grade 1", ((IRatingsDisplay.IRowGroup.IRow)((IRatingsDisplay.IRowGroup) rowGroups.get(0)).getRows().get(0)).getLabel());
        assertEquals("Grade 2", ((IRatingsDisplay.IRowGroup.IRow)((IRatingsDisplay.IRowGroup) rowGroups.get(0)).getRows().get(1)).getLabel());
        assertEquals("Grade 3", ((IRatingsDisplay.IRowGroup.IRow)((IRatingsDisplay.IRowGroup) rowGroups.get(0)).getRows().get(2)).getLabel());
        assertEquals("Grade 4", ((IRatingsDisplay.IRowGroup.IRow)((IRatingsDisplay.IRowGroup) rowGroups.get(0)).getRows().get(3)).getLabel());

        assertEquals("Male", ((IRatingsDisplay.IRowGroup.IRow)((IRatingsDisplay.IRowGroup) rowGroups.get(1)).getRows().get(0)).getLabel());
        assertEquals(Integer.valueOf("9"), ((IRatingsDisplay.IRowGroup.IRow.ICell)((IRatingsDisplay.IRowGroup.IRow)((IRatingsDisplay.IRowGroup) rowGroups.get(1)).getRows().get(0)).getCells().get(0)).getRating());
        assertEquals(null, ((IRatingsDisplay.IRowGroup.IRow.ICell)((IRatingsDisplay.IRowGroup.IRow)((IRatingsDisplay.IRowGroup) rowGroups.get(1)).getRows().get(0)).getCells().get(1)).getRating());
        assertEquals(Integer.valueOf("9"), ((IRatingsDisplay.IRowGroup.IRow.ICell)((IRatingsDisplay.IRowGroup.IRow)((IRatingsDisplay.IRowGroup) rowGroups.get(1)).getRows().get(0)).getCells().get(2)).getRating());

        assertEquals("African American", ((IRatingsDisplay.IRowGroup.IRow)((IRatingsDisplay.IRowGroup) rowGroups.get(2)).getRows().get(0)).getLabel());
        assertEquals("Asian", ((IRatingsDisplay.IRowGroup.IRow)((IRatingsDisplay.IRowGroup) rowGroups.get(2)).getRows().get(1)).getLabel());
        assertEquals("Hispanic", ((IRatingsDisplay.IRowGroup.IRow)((IRatingsDisplay.IRowGroup) rowGroups.get(2)).getRows().get(2)).getLabel());
        assertEquals("White", ((IRatingsDisplay.IRowGroup.IRow)((IRatingsDisplay.IRowGroup) rowGroups.get(2)).getRows().get(3)).getLabel());

    }

    private ITestDataSetDao.IRawResult createMockedRawResultDao(Integer dataSetId, int decile, TestDataSet testDataSet) {

        MockControl mockControl = MockControl.createControl(ITestDataSetDao.IRawResult.class);

        ITestDataSetDao.IRawResult result = (ITestDataSetDao.IRawResult) mockControl.getMock();
        mockControl.expectAndReturn(result.getDecile(), decile);
        mockControl.expectAndReturn(result.getDataSetId(), dataSetId);
        mockControl.expectAndReturn(result.getTestDataSet(), testDataSet);
        mockControl.replay();

        return result;
    }

}
