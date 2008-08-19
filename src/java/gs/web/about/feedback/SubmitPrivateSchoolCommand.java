package gs.web.about.feedback;

import gs.data.school.Grade;

public class SubmitPrivateSchoolCommand extends SubmitSchoolCommand {
    private String _lowestGrade;
    private String _highestGrade;
    private String _gender;

    public String getLowestGrade() {
        return _lowestGrade;
    }

    public void setLowestGrade(String lowestGrade) {
        _lowestGrade = lowestGrade;
    }

    public String getHighestGrade() {
        return _highestGrade;
    }

    public void setHighestGrade(String highestGrade) {
        _highestGrade = highestGrade;
    }

    public String getGender() {
        return _gender;
    }

    public void setGender(String gender) {
        _gender = gender;
    }
}
