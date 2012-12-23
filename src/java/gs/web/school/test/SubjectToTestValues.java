package gs.web.school.test;

import gs.web.school.SchoolProfileTestScoresController;

import java.util.List;

/**
* Created 12/22/12
*
* @author yfan@greatschools.org
*/
public class SubjectToTestValues implements Comparable<SubjectToTestValues> {
    String _subjectLabel;
    List<SchoolProfileTestScoresController.TestValues> _testValues;

    public String getSubjectLabel() {
        return _subjectLabel;
    }

    public void setSubjectLabel(String subjectLabel) {
        _subjectLabel = subjectLabel;
    }

    public List<SchoolProfileTestScoresController.TestValues> getTestValues() {
        return _testValues;
    }

    public void setTestValues(List<SchoolProfileTestScoresController.TestValues> testValues) {
        _testValues = testValues;
    }

    public int compareTo(SubjectToTestValues subjectToTestValues) {
        return getSubjectLabel().compareTo(subjectToTestValues.getSubjectLabel());
    }
}
