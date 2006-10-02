/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolRatingsDisplay.java,v 1.5 2006/10/02 23:02:42 dlee Exp $
 */

package gs.web.test.rating;

import gs.data.school.Grade;
import gs.data.school.Grades;
import gs.data.school.School;
import gs.data.test.ITestDataSetDao;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestDataSet;
import gs.data.test.rating.IRatingsConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generates to display based on a RatingsConfig object.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 * @todo implement other row groups. This should probably come from the ratings config, but it's not there yet.
 * @todo move to GSWeb?
 * @todo unit test
 */
public class SchoolRatingsDisplay implements IRatingsDisplay {

    private final School _school;
    private final IRatingsConfig _ratingsConfig;
    private final List _subjectGroupLabels;
    private final List _rowGroups;
    private ITestDataSetDao _testDataSetDao;


    public SchoolRatingsDisplay(final IRatingsConfig ratingsConfig, School school, final ITestDataSetDao testDataSetDao) {

        _school = school;
        _ratingsConfig = ratingsConfig;
        _testDataSetDao = testDataSetDao;

        Grades grades = school.getGradeLevels();

        // Subject group labels are precalculated.
        _subjectGroupLabels = new ArrayList();
        for (int i = 0; i < _ratingsConfig.getSubjectGroupConfigs().length; i++) {
            IRatingsConfig.ISubjectGroupConfig subjectGroupConfig = _ratingsConfig.getSubjectGroupConfigs()[i];
            _subjectGroupLabels.add(subjectGroupConfig.getLabel());

        }

        // must combine row groups from the different subject groups
        _rowGroups = new ArrayList();
        final IRatingsConfig.IRowGroupConfig[] rowGroupConfigs = _ratingsConfig.getRowGroupConfigs();
        for (int j = 0; j < rowGroupConfigs.length; j++) {
            IRatingsConfig.IRowGroupConfig rowGroupConfig = rowGroupConfigs[j];
            RowGroup rowGroup = new RowGroup(rowGroupConfig);
            String label = rowGroup.getLabel();
            boolean isGradeRowgroup = false;

            if (label.startsWith("By Grade")) {
                isGradeRowgroup = true;
            }

            final IRatingsConfig.IRowConfig[] rowConfigs = rowGroupConfig.getRowConfigs();
            for (int i = 0; i < rowConfigs.length; i++) {
                final IRatingsConfig.IRowConfig rowConfig = rowConfigs[i];
                Row row = new Row(rowConfig);

                if (isGradeRowgroup) {
                    String rowLabel = row.getLabel();
                    String grade = rowLabel.replaceAll("Grade ", "");

                    if (grades.contains(Grade.getGradeLevel(grade))) {
                        rowGroup.add(row);
                    }
                } else {
                    if (!row.isAllEmptyCells()) rowGroup.add(row);
                }
            }

            if (rowGroup.getRows().size() > 0) {
                _rowGroups.add(rowGroup);
            }
        }
    }

    private class RowGroup implements IRowGroup {
        final String _label;
        final List _rows;

        RowGroup(IRatingsConfig.IRowGroupConfig rowGroupConfig) {
            _label = rowGroupConfig.getLabel();
            _rows = new ArrayList();
        }

        public String getLabel() {
            return _label;
        }

        public int getNumRows() {
            return _rows.size();
        }

        public List getRows() {
            return _rows;
        }

        public void add(IRow row) {
            _rows.add(row);
        }
    }

    private class Row implements IRowGroup.IRow {
        private final IRatingsConfig.IRowConfig _rowConfig;
        private List _cells;
        private String _label;

        Row(final IRatingsConfig.IRowConfig rowConfig) {
            _rowConfig = rowConfig;
            _label = rowConfig.getLabel();
            _cells = new ArrayList();

            IRatingsConfig.ISubjectGroupConfig [] subjects = _ratingsConfig.getSubjectGroupConfigs();

            for (int numSubjectGroups=0; numSubjectGroups < subjects.length; numSubjectGroups++) {
                int[] ids = _ratingsConfig.getDataSetIds(subjects[numSubjectGroups], _rowConfig);

                int count = 0;
                int total = 0;
                for (int i = 0; i < ids.length; i++) {
                    int id = ids[i];
                    TestDataSet testDataSet = _testDataSetDao.findTestDataSet(_ratingsConfig.getState(), id);
                    SchoolTestValue value = _testDataSetDao.findValue(testDataSet, _school);
                    if (value != null && value.getValueFloat() != null) {
                        Float floatValue = value.getValueFloat();
                        int decile = testDataSet.convertFloatValueToDecile(floatValue);
                        if (decile > 0) {
                            count ++;
                            total += decile;
                        }
                    }
                }
                Integer rating = null;
                if (count > 0) {
                    rating = new Integer(total / count);
                }
                _cells.add(new Cell(rating, null));
            }
        }

        public String getLabel() {
            return _rowConfig.getLabel();
        }

        public List getCells() {
            return _cells;
        }

        boolean isAllEmptyCells() {
            for (Iterator iter = _cells.iterator(); iter.hasNext(); ) {
                Cell cell = (Cell) iter.next();
                if (cell.getRating() != null) {
                    return false;
                }
            }

            return true;
        }
    }

    private class Cell implements IRowGroup.IRow.ICell {
        private Integer _rating;
        private Integer _trend;

        Cell(Integer rating, Integer trend) {
            _rating = rating;
            _trend = trend;
        }

        public Integer getRating() {
            return _rating;
        }

        public Integer getTrend() {
            return _trend;
        }
    }

    public List getSubjectGroupLabels() {
        return _subjectGroupLabels;
    }

    public List getRowGroups() {
        // list of IRowGroup objects
        return _rowGroups;
    }


}
