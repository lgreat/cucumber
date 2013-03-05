/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: SchoolRatingsDisplayTest.java,v 1.15 2009/12/04 22:27:16 chriskimm Exp $
 */

package gs.web.test.rating;

import gs.data.school.Grade;
import gs.data.school.Grades;
import gs.data.school.School;
import gs.data.test.*;
import junit.framework.TestCase;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.MockControl;

import java.util.*;

/**
 * Tests SchoolRatingsDisplay.
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
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

        Subject[] subjects = stubRatingsConfig.getSubjects();
        String[] rowLabels = stubRatingsConfig.getRowLabels();

        Map results = new HashMap();
        for (int i = 0; i < rowLabels.length; i++) {
            String rowLabel = rowLabels[i];

            if (rowLabel.equals("Grade 6")) {
                // There are data sets for Grade 6 but this school doesn't have any values for it
                continue;
            }
            for (int j = 0; j < subjects.length; j++) {
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
        testDataSetDao.findAllRawResults(null, new int[]{2002}, true);
        testDataSetDaoControl.setDefaultReturnValue(results);

        testDataSetDao.findDataSets(null, 2004, null, null, null, null, null, true, null);
        testDataSetDaoControl.setDefaultReturnValue(new ArrayList());
        testDataSetDaoControl.replay();

        TestManager testManager = new TestManager();
        testManager.setTestDataSetDao(testDataSetDao);

        SchoolRatingsDisplay schoolRatingsDisplay = new SchoolRatingsDisplay(stubRatingsConfig, school,
                testDataSetDao);

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

        //5 grades
        assertEquals(5, ((IRatingsDisplay.IRowGroup) rowGroups.get(0)).getNumRows());
        //1 gender
        assertEquals(1, ((IRatingsDisplay.IRowGroup) rowGroups.get(1)).getNumRows());
        //4 ethnicities
        assertEquals(4, ((IRatingsDisplay.IRowGroup) rowGroups.get(2)).getNumRows());

        assertEquals("Grade 1", ((IRatingsDisplay.IRowGroup.IRow) ((IRatingsDisplay.IRowGroup) rowGroups.get(0)).getRows().get(0)).getLabel());
        assertEquals("Grade 2", ((IRatingsDisplay.IRowGroup.IRow) ((IRatingsDisplay.IRowGroup) rowGroups.get(0)).getRows().get(1)).getLabel());
        assertEquals("Grade 3", ((IRatingsDisplay.IRowGroup.IRow) ((IRatingsDisplay.IRowGroup) rowGroups.get(0)).getRows().get(2)).getLabel());
        assertEquals("Grade 4", ((IRatingsDisplay.IRowGroup.IRow) ((IRatingsDisplay.IRowGroup) rowGroups.get(0)).getRows().get(3)).getLabel());
        // Note that the schools level was set to grades 1-4, but we put a g5 value in the results.  This test shows
        // that it was picked up.
        assertEquals("Grade 5", ((IRatingsDisplay.IRowGroup.IRow) ((IRatingsDisplay.IRowGroup) rowGroups.get(0)).getRows().get(4)).getLabel());

        assertEquals("Male", ((IRatingsDisplay.IRowGroup.IRow) ((IRatingsDisplay.IRowGroup) rowGroups.get(1)).getRows().get(0)).getLabel());
        assertEquals(Integer.valueOf("9"), ((IRatingsDisplay.IRowGroup.IRow.ICell) ((IRatingsDisplay.IRowGroup.IRow) ((IRatingsDisplay.IRowGroup) rowGroups.get(1)).getRows().get(0)).getCells().get(0)).getRating());
        assertEquals(null, ((IRatingsDisplay.IRowGroup.IRow.ICell) ((IRatingsDisplay.IRowGroup.IRow) ((IRatingsDisplay.IRowGroup) rowGroups.get(1)).getRows().get(0)).getCells().get(1)).getRating());
        assertEquals(Integer.valueOf("9"), ((IRatingsDisplay.IRowGroup.IRow.ICell) ((IRatingsDisplay.IRowGroup.IRow) ((IRatingsDisplay.IRowGroup) rowGroups.get(1)).getRows().get(0)).getCells().get(2)).getRating());

        assertEquals("African American", ((IRatingsDisplay.IRowGroup.IRow) ((IRatingsDisplay.IRowGroup) rowGroups.get(2)).getRows().get(0)).getLabel());
        assertEquals("Asian", ((IRatingsDisplay.IRowGroup.IRow) ((IRatingsDisplay.IRowGroup) rowGroups.get(2)).getRows().get(1)).getLabel());
        assertEquals("Hispanic", ((IRatingsDisplay.IRowGroup.IRow) ((IRatingsDisplay.IRowGroup) rowGroups.get(2)).getRows().get(2)).getLabel());
        assertEquals("White", ((IRatingsDisplay.IRowGroup.IRow) ((IRatingsDisplay.IRowGroup) rowGroups.get(2)).getRows().get(3)).getLabel());

        // Now test OverallRatingDecorator
        OverallRatingDecorator overall = new OverallRatingDecorator(schoolRatingsDisplay);
        List subjectGroupLabels = overall.getSubjectGroupLabels();
        assertEquals(1, subjectGroupLabels.size());
        assertEquals("GREATSCHOOLS<br/>RATING", subjectGroupLabels.get(0));
        List decoratedRowGroups = overall.getRowGroups();
        assertEquals(3, decoratedRowGroups.size());
        assertEquals("By Grade", ((IRatingsDisplay.IRowGroup) decoratedRowGroups.get(0)).getLabel());
        assertEquals("By Gender", ((IRatingsDisplay.IRowGroup) decoratedRowGroups.get(1)).getLabel());
        assertEquals("By Ethnicity", ((IRatingsDisplay.IRowGroup) decoratedRowGroups.get(2)).getLabel());
        assertEquals(5, ((IRatingsDisplay.IRowGroup) decoratedRowGroups.get(0)).getNumRows());
        assertEquals(1, ((IRatingsDisplay.IRowGroup) decoratedRowGroups.get(1)).getNumRows());
        assertEquals(4, ((IRatingsDisplay.IRowGroup) decoratedRowGroups.get(2)).getNumRows());
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

    public void testAddIntArrayToList() {
        List<Integer> a = new ArrayList<Integer>();
        int [] b = new int[3];
        b[0] = 0;
        b[1] = 1;
        b[2] = 2;
        int [] c = new int[3];
        c[0] = 2;
        c[1] = 3;
        c[2] = 4;

        a = SchoolRatingsDisplay.addIntArrayToList(a,b);
        a = SchoolRatingsDisplay.addIntArrayToList(a,c);

        assertEquals(0, (a.get(0)).intValue());
        assertEquals(2, (a.get(3)).intValue());
        assertEquals(4, (a.get(5)).intValue());
        assertEquals(a.size(),6);
    }

    public void testgetValuesFromIntList() {
        List<Integer> a = new ArrayList<Integer>();
        a.add(0);
        a.add(1);
        a.add(2);
        a.add(2);

        int[] ints =  SchoolRatingsDisplay.getValuesFromIntList(a);
        assertEquals(0,ints[0]);
        assertEquals(1,ints[1]);
        assertEquals(2,ints[2]);
        assertEquals(2,ints[3]);
    }

    public void testAddIntArrayToSet() {
        Set<Integer> a = new HashSet<Integer>();
        int [] b = new int[3];
        b[0] = 0;
        b[1] = 1;
        b[2] = 2;
        int [] c = new int[3];
        c[0] = 2;
        c[1] = 3;
        c[2] = 4;

        a = SchoolRatingsDisplay.addIntArrayToSet(a, b);
        a = SchoolRatingsDisplay.addIntArrayToSet(a, c);

        assertEquals("Expect duplicates to be stripped out", a.size(),5);
        assertTrue(a.contains(0));
        assertTrue(a.contains(1));
        assertTrue(a.contains(2));
        assertTrue(a.contains(3));
        assertTrue(a.contains(4));
        assertFalse(a.contains(5));
    }

    public void testgetValuesFromIntSet() {
        Set<Integer> a = new HashSet<Integer>();
        a.add(0);
        a.add(1);
        a.add(2);
        a.add(2);

        int[] ints =  SchoolRatingsDisplay.getValuesFromIntCollection(a);
        assertEquals(3, ints.length);
        assertTrue(ArrayUtils.contains(ints, 0));
        assertTrue(ArrayUtils.contains(ints, 1));
        assertTrue(ArrayUtils.contains(ints, 2));
        assertFalse(ArrayUtils.contains(ints, 3));
    }

}
