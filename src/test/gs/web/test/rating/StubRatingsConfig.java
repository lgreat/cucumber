/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: StubRatingsConfig.java,v 1.1 2006/10/07 00:42:10 dlee Exp $
 */
package gs.web.test.rating;

import gs.data.state.State;
import gs.data.test.SchoolTestValue;
import gs.data.test.Subject;
import gs.data.test.TestDataSet;
import gs.data.test.rating.IRatingsConfig;

import java.util.*;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class StubRatingsConfig implements IRatingsConfig {
    public static final State STATE = State.CA;
    public static final int YEAR = 2004;

    private List _rowLabels;
    private List _subjects;
    private Map _rowSubjectToDataSet;
    private Map _datasetToValue;


    public StubRatingsConfig() {
        _rowLabels = new ArrayList();
        _rowLabels.add("Grade 1");
        _rowLabels.add("Grade 2");
        _rowLabels.add("Grade 3");
        _rowLabels.add("Grade 4");
        _rowLabels.add("Grade 5");
        _rowLabels.add("Male");
        _rowLabels.add("Female");
        _rowLabels.add("African American");
        _rowLabels.add("Asian");
        _rowLabels.add("Hispanic");
        _rowLabels.add("White");
        _rowLabels.add("All Students");
        _rowLabels.add("Economically disadvantaged");
        _rowLabels.add("Not economically disadvantaged");
        _rowLabels.add("Students with disabilities");
        _rowLabels.add("Students without disabilities");

        _subjects = new ArrayList();
        _subjects.add(Subject.ENGLISH);
        _subjects.add(Subject.ENGLISH_LANGUAGE_ARTS);
        _subjects.add(Subject.MATH);
        _subjects.add(Subject.SCIENCE);

        _rowSubjectToDataSet = new HashMap();
        _datasetToValue = new HashMap();

        int i = 1;
        for (Iterator rowIter = _rowLabels.iterator(); rowIter.hasNext();) {
            String rowLabel = (String) rowIter.next();

            for (Iterator subjectIter = _subjects.iterator(); subjectIter.hasNext();) {
                Subject subject = (Subject) subjectIter.next();
                String id = String.valueOf(subject.getSubjectId());
                String key = rowLabel + id;

                TestDataSet testDataSet = new TestDataSet();
                testDataSet.setId(new Integer(i));
                testDataSet.setSubject(subject);
                //testDataSet.setSchoolDecileTops("0;1;2;3;4;5;6;7;8;9;10");

                SchoolTestValue schoolTestValue = new SchoolTestValue();
                schoolTestValue.setDataSet(testDataSet);
                schoolTestValue.setValueFloat(new Float(i));

                _rowSubjectToDataSet.put(key, testDataSet);
                _datasetToValue.put(testDataSet, schoolTestValue);
                i++;
            }

        }
    }

    public Map getRowSubjectToDataSet() {
        return _rowSubjectToDataSet;
    }

    public Map getDatasetToValue() {
        return _datasetToValue;
    }

    public List getSubjects() {
        return _subjects;
    }

    public List getRowLabels() {
        return _rowLabels;
    }

    public State getState() {
        return StubRatingsConfig.STATE;
    }

    public int getYear() {
        return StubRatingsConfig.YEAR;
    }

    public ISubjectGroupConfig[] getSubjectGroupConfigs() {
        ISubjectGroupConfig englishGroupConfig = new StubSubjectGroupConfig("English", new int []{Subject.ENGLISH.getSubjectId(), Subject.ENGLISH_LANGUAGE_ARTS.getSubjectId()});
        ISubjectGroupConfig mathGroupConfig = new StubSubjectGroupConfig("Math", new int []{Subject.MATH.getSubjectId()});
        ISubjectGroupConfig scienceGroup = new StubSubjectGroupConfig("Science", new int []{Subject.SCIENCE.getSubjectId()});

        return new ISubjectGroupConfig[]{englishGroupConfig, mathGroupConfig, scienceGroup};
    }

    public IRowGroupConfig[] getRowGroupConfigs() {
        return new IRowGroupConfig []{
                new StubRowGroupConfig("By Grade",
                        new IRowConfig[]{
                                new StubRowConfig("Grade 1", "By Grade"),
                                new StubRowConfig("Grade 2", "By Grade"),
                                new StubRowConfig("Grade 3", "By Grade"),
                                new StubRowConfig("Grade 4", "By Grade"),
                                new StubRowConfig("Grade 5", "By Grade"),
                        }),

                new StubRowGroupConfig("By Gender",
                        new IRowConfig[]{
                                new StubRowConfig("Male", "By Gender"),
                                new StubRowConfig("Female", "By Gender"),
                        }),

                new StubRowGroupConfig("By Ethnicity",
                        new IRowConfig[]{
                                new StubRowConfig("African American", "By Ethnicity"),
                                new StubRowConfig("Asian", "By Ethnicity"),
                                new StubRowConfig("Hispanic", "By Ethnicity"),
                                new StubRowConfig("White", "By Ethnicity"),
                        }),

                new StubRowGroupConfig("By Category",
                        new IRowConfig[]{
                                new StubRowConfig("All Students", "By Category"),
                                new StubRowConfig("Economically disadvantaged", "By Category"),
                                new StubRowConfig("Not economically disadvantaged", "By Category"),
                                new StubRowConfig("Students with disabilities", "By Category"),
                                new StubRowConfig("Students without disabilities", "By Category"),
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
