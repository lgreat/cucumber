package gs.web.about.feedback;

import gs.data.school.Grade;

public class SubmitPrivateSchoolCommand extends SubmitSchoolCommand {
    private Grade _lowestGrade;
    private Grade _highestGrade;
    private String _gender;

    public Grade getLowestGrade() {
        return _lowestGrade;
    }

    public void setLowestGrade(Grade lowestGrade) {
        _lowestGrade = lowestGrade;
    }

    public Grade getHighestGrade() {
        return _highestGrade;
    }

    public void setHighestGrade(Grade highestGrade) {
        _highestGrade = highestGrade;
    }

    public String getGender() {
        return _gender;
    }

    public void setGender(String gender) {
        _gender = gender;
    }
}
