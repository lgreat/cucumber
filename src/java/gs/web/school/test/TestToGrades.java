package gs.web.school.test;

import gs.data.school.Grade;
import gs.data.test.TestDataTypeDisplayType;
import gs.web.school.SchoolProfileTestScoresController;
import gs.web.school.TestScoresHelper;

import java.util.List;

/**
 * Beans to encapsulate the test scores for the school.This bean is used to present data to the view.
 */
public class TestToGrades implements Comparable<TestToGrades> {
    String _testLabel;
    List<SchoolProfileTestScoresController.GradeToSubjects> _grades;
    String _description;
    String _source;
    String _scale;
    Grade _lowestGradeInTest;
    Integer _testDataTypeId;
    Boolean _isSubgroup;
    String _displayName;
    TestDataTypeDisplayType _displayType;

    public static int OHIO_PERFORMANCE_INDEX_DATA_TYPE_ID = 64;

    public boolean isHideGradesNav() {
        return _displayType == TestDataTypeDisplayType.sentence ||
                _displayType == TestDataTypeDisplayType.oh_value_added ||
               _testDataTypeId == OHIO_PERFORMANCE_INDEX_DATA_TYPE_ID;
    }

    public boolean isHideSubject() {
        return _testDataTypeId == OHIO_PERFORMANCE_INDEX_DATA_TYPE_ID;
    }

    public String getDisplayName() {
        return _displayName;
    }

    public void setDisplayName(String _displayName) {
        this._displayName = _displayName;
    }

    public String getTestLabel() {
        return _testLabel;
    }

    public void setTestLabel(String testLabel) {
        _testLabel = testLabel;
    }

    public List<SchoolProfileTestScoresController.GradeToSubjects> getGrades() {
        return _grades;
    }

    public void setGrades(List<SchoolProfileTestScoresController.GradeToSubjects> grades) {
        _grades = grades;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public String getSource() {
        return _source;
    }

    public void setSource(String source) {
        _source = source;
    }

    public String getScale() {
        return _scale;
    }

    public void setScale(String scale) {
        _scale = scale;
    }

    public Grade getLowestGradeInTest() {
        return _lowestGradeInTest;
    }

    public void setLowestGradeInTest(Grade lowestGradeInTest) {
        _lowestGradeInTest = lowestGradeInTest;
    }

    public Integer getTestDataTypeId() {
        return _testDataTypeId;
    }

    public void setTestDataTypeId(Integer testDataTypeId) {
        _testDataTypeId = testDataTypeId;
    }

    public Boolean getIsSubgroup() {
        return _isSubgroup;
    }

    public void setIsSubgroup(Boolean isSubgroup) {
        _isSubgroup = isSubgroup;
    }

    public TestDataTypeDisplayType getDisplayType() {
        return _displayType;
    }

    public void setDisplayType(TestDataTypeDisplayType displayType) {
        _displayType = displayType;
    }

    //The tests should be sorted in the order of - the lowest grade in the test followed by test data type id.
    //However if the test has subgroup data then the test should be followed by subgroup test.
    public int compareTo(TestToGrades testToGrades) {
        Integer gradeNum1 = TestScoresHelper.getGradeNum(getLowestGradeInTest());
        Integer gradeNum2 = TestScoresHelper.getGradeNum(testToGrades.getLowestGradeInTest());
        Integer dataTypeId1 = getTestDataTypeId();
        Integer dataTypeId2 = testToGrades.getTestDataTypeId();

        int rval;
        if (gradeNum1.compareTo(gradeNum2) == 0) {
            if (dataTypeId1.compareTo(dataTypeId2) == 0) {
                rval = getIsSubgroup() ? 1 : -1;
            } else {
                rval = dataTypeId1.compareTo(dataTypeId2);
            }
        } else {
            rval = gradeNum1.compareTo(gradeNum2);
        }
        return rval;
    }
}
