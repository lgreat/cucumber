/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: StubRatingsConfig.java,v 1.4 2008/03/28 20:51:56 droy Exp $
 */
package gs.web.test.rating;

import gs.data.state.State;
import gs.data.test.SchoolTestValue;
import gs.data.test.Subject;
import gs.data.test.TestDataSet;
import gs.data.test.rating.IRatingsConfig;
import gs.data.school.Grade;

import java.util.HashMap;
import java.util.Map;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class StubRatingsConfig implements IRatingsConfig {
    public static final State STATE = State.CA;
    public static final int YEAR = 2004;
    private static final String DECILES = "1;1;2;3;4;5;6;7;8;9;10;";

    private Map _rowSubjectToDataSet;
    private Map _datasetToValue;

    private String [] _subgectGroupLabels = {
            "English",
            "Math",
            "Science",
    };

    private String [] _rowGroupLabels = {
            "By Grade",
            "By Gender",
            "By Ethnicity",
    };

    private String [] _rowLabels = {
            "Grade 1",
            "Grade 2",
            "Grade 3",
            "Grade 4",
            "Grade 5",
            "Grade 6",
            "Male",
            "Female",
            "All Students",
            "African American",
            "Asian",
            "Hispanic",
            "White",
    };

    private Subject [] _subjects = {
            Subject.ENGLISH,
            Subject.ENGLISH_LANGUAGE_ARTS,
            Subject.MATH,
            Subject.SCIENCE,
    };

    //english, reading, math, science
    private float[][] _values = {
            //By Grade RowGroup
            {9f, 8f, 8f, 9f,},       //grade1 - test (9+8)/2 rounds up to 9
            {9f, 8f, 8f, -1f,},      //grade2 - a null result
            {9f, 8f, 8f, 9f,},       //grade3 -
            {-1f, -1f, -1f, -1f},    //grade4 - null results appear
            {9f, 8f, 7f, 6f},        //grade5 - school has data for g5, but the level_code doesn't include it
            {-1f, -1f, -1f, -1f},    //grade6 - school does not have grade 6

            //By Gender RowGroup
            {9f, 8f, -1f, 9f,},      //male
            {-1f, -1f, -1f, -1f},   //female - null results..row should not appear

            //By Ethnicity RowGroup
            {9f, 8f, 8f, 9f,},       //all students
            {9f, 1f, 8f, 9f,},       //african american
            {9f, 8f, 1f, 9f,},       //asian
            {9f, 8f, 8f, 1f,},       //hispanic
            {1f, 2f, 3f, 10f,},       //white
    };


    public StubRatingsConfig() {
        _rowSubjectToDataSet = new HashMap();
        _datasetToValue = new HashMap();

        for (int i = 0; i < _rowLabels.length; i++) {
            String rowLabel = _rowLabels[i];

            for (int j = 0; j < _subjects.length; j++) {
                int subjectId = _subjects[j].getSubjectId();

                TestDataSet testDataSet = new TestDataSet();
                testDataSet.setId(new Integer(String.valueOf(i + 1) + String.valueOf(j + 1)));
                testDataSet.setSchoolDecileTops(DECILES);
                if (rowLabel.startsWith("Grade ")) {
                    Grade grade = Grade.getGradeLevel(rowLabel.substring(6));
                    testDataSet.setGrade(grade);
                }

                SchoolTestValue schoolTestValue = new SchoolTestValue();
                schoolTestValue.setDataSet(testDataSet);

                Float value = (_values[i][j] > 0) ? new Float(_values[i][j]) : null;
                schoolTestValue.setValueFloat(value);

                String key = rowLabel + String.valueOf(subjectId);
                _rowSubjectToDataSet.put(key, testDataSet);
                _datasetToValue.put(testDataSet, schoolTestValue);
            }
        }
    }

    public Map getRowSubjectToDataSet() {
        return _rowSubjectToDataSet;
    }

    public Map getDatasetToValue() {
        return _datasetToValue;
    }

    public String[] getRowLabels() {
        return _rowLabels;
    }

    public Subject[] getSubjects() {
        return _subjects;
    }

    public float[][] getValues() {
        return _values;
    }

    public State getState() {
        return StubRatingsConfig.STATE;
    }

    public int getYear() {
        return StubRatingsConfig.YEAR;
    }

    public ISubjectGroupConfig[] getSubjectGroupConfigs() {
        return new ISubjectGroupConfig[]{
                new StubSubjectGroupConfig(_subgectGroupLabels[0], new int []{_subjects[0].getSubjectId(), _subjects[1].getSubjectId()}),
                new StubSubjectGroupConfig(_subgectGroupLabels[1], new int []{_subjects[2].getSubjectId()}),
                new StubSubjectGroupConfig(_subgectGroupLabels[2], new int []{_subjects[3].getSubjectId()}),
        };
    }

    public IRowGroupConfig[] getRowGroupConfigs() {
        return new IRowGroupConfig []{
                new StubRowGroupConfig(_rowGroupLabels[0],
                        new IRowConfig[]{
                                new StubRowConfig(_rowLabels[0], _rowGroupLabels[0]),
                                new StubRowConfig(_rowLabels[1], _rowGroupLabels[0]),
                                new StubRowConfig(_rowLabels[2], _rowGroupLabels[0]),
                                new StubRowConfig(_rowLabels[3], _rowGroupLabels[0]),
                                new StubRowConfig(_rowLabels[4], _rowGroupLabels[0]),
                                new StubRowConfig(_rowLabels[5], _rowGroupLabels[0]),
                        }),

                new StubRowGroupConfig(_rowGroupLabels[1],
                        new IRowConfig[]{
                                new StubRowConfig(_rowLabels[6], _rowGroupLabels[1]),
                                new StubRowConfig(_rowLabels[7], _rowGroupLabels[1]),
                        }),

                new StubRowGroupConfig(_rowGroupLabels[2],
                        new IRowConfig[]{
                                new StubRowConfig(_rowLabels[8], _rowGroupLabels[2]),
                                new StubRowConfig(_rowLabels[9], _rowGroupLabels[2]),
                                new StubRowConfig(_rowLabels[10], _rowGroupLabels[2]),
                                new StubRowConfig(_rowLabels[11], _rowGroupLabels[2]),
                                new StubRowConfig(_rowLabels[12], _rowGroupLabels[2]),
                        }),
        };
    }

    public int[] getDataSetIds(ISubjectGroupConfig subjectGroupConfig, IRowConfig rowConfig) {

        int [] subjectIds = subjectGroupConfig.getSubjectIds();
        String rowLabel = rowConfig.getLabel();

        int [] dataSetIds = new int [subjectIds.length];

        for (int j = 0; j < subjectIds.length; j++) {
            String subjectId = String.valueOf(subjectIds[j]);
            String key = rowLabel + subjectId;
            TestDataSet testDataSet = (TestDataSet) _rowSubjectToDataSet.get(key);

            dataSetIds[j] = testDataSet.getId().intValue();
        }

        return dataSetIds;
    }


    public class StubSubjectGroupConfig implements ISubjectGroupConfig {
        private String _label;
        private int [] _subjectIds;

        public StubSubjectGroupConfig(String label, int[] subjectIds) {
            _label = label;
            _subjectIds = subjectIds;
        }

        public String getLabel() {
            return _label;
        }

        public int[] getSubjectIds() {
            return _subjectIds;
        }
    }

    public class StubRowGroupConfig implements IRowGroupConfig {
        private String _label;
        private IRowConfig [] _rowConfigs;

        public StubRowGroupConfig(String label, IRowConfig[] rowConfigs) {
            _label = label;
            _rowConfigs = rowConfigs;
        }

        public String getLabel() {
            return _label;
        }

        public IRowConfig[] getRowConfigs() {
            return _rowConfigs;
        }
    }

    public class StubRowConfig implements IRowConfig {
        private String _label;
        private String _rowGroupLabel;

        public StubRowConfig(String label, String rowGroupLabel) {
            _label = label;
            _rowGroupLabel = rowGroupLabel;
        }

        public String getLabel() {
            return _label;
        }

        public void setLabel(String label) {
            _label = label;
        }

        public String getRowGroupLabel() {
            return _rowGroupLabel;
        }

        public void setRowGroupLabel(String rowGroupLabel) {
            _rowGroupLabel = rowGroupLabel;
        }

    }
}
