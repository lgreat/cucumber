package gs.web.about.feedback;

import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.web.community.ICaptchaCommand;

/**
 * Created by IntelliJ IDEA.
 * User: eddie
 * Date: 12/20/11
 * Time: 5:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddEditSchoolOrDistrictCommand  implements ICaptchaCommand {

    public AddEditSchoolOrDistrictCommand() {
        _schoolInfoFields = new SchoolInfoFields();
        _districtInfoFields = new DistrictInfoFields();
        _state = State.AK;
    }

    private String _schoolOrDistrict;
    private String _addEdit;
    private String _schoolType;
    private String _grades;

    private String _submitterName;
    private String _submitterEmail;
    private String _submitterEmailConfirm;
    private String _submitterConnectionToSchool;

    //private String[] listSubmitterName = {"eddie","andy"};

    public ContactUsCommand.FeedbackType getFeedbackType() {
        return _feedbackType;
    }

    public void setFeedbackType(ContactUsCommand.FeedbackType feedbackType) {
        _feedbackType = feedbackType;
    }

    private ContactUsCommand.FeedbackType _feedbackType = ContactUsCommand.FeedbackType.defaultOption;
    private SchoolInfoFields _schoolInfoFields;
    private DistrictInfoFields _districtInfoFields;
    private String _schoolId;
    private String _schoolName;
    private String _cityName;
    private State _state;
    private String _challenge = "";
    private String _response = "";

    public String getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(String schoolId) {
        _schoolId = schoolId;
    }

    public String getSchoolName() {
        return _schoolName;
    }

    public void setSchoolName(String schoolName) {
        _schoolName = schoolName;
    }

    public String getCityName() {
        return _cityName;
    }

    public void setCityName(String cityName) {
        _cityName = cityName;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public String getChallenge() {
        return _challenge;
    }

    public void setChallenge(String challenge) {
        _challenge = challenge;
    }

    public String getResponse() {
        return _response;
    }

    public void setResponse(String response) {
        _response = response;
    }



    public SchoolInfoFields getSchoolInfoFields() {
        return _schoolInfoFields;
    }

    public void setSchoolInfoFields(SchoolInfoFields schoolInfoFields) {
        _schoolInfoFields = schoolInfoFields;
    }


    public DistrictInfoFields getDistrictInfoFields() {
        return _districtInfoFields;
    }

    public void setDistrictInfoFields(DistrictInfoFields districtInfoFields) {
        _districtInfoFields = districtInfoFields;
    }


    public String getSubmitterName() {
        return _submitterName;
    }

    public void setSubmitterName(String submitterName) {
        _submitterName = submitterName;
    }

    public String getSubmitterEmail() {
        return _submitterEmail;
    }

    public void setSubmitterEmail(String submitterEmail) {
        _submitterEmail = submitterEmail;
    }

    public String getSubmitterEmailConfirm() {
        return _submitterEmailConfirm;
    }

    public void setSubmitterEmailConfirm(String submitterEmailConfirm) {
        _submitterEmailConfirm = submitterEmailConfirm;
    }

    public String getSubmitterConnectionToSchool() {
        return _submitterConnectionToSchool;
    }

    public void setSubmitterConnectionToSchool(String submitterConnectionToSchool) {
        _submitterConnectionToSchool = submitterConnectionToSchool;
    }

    public String getSchoolOrDistrict() {
        return _schoolOrDistrict;
    }

    public void setSchoolOrDistrict(String schoolOrDistrict) {
        _schoolOrDistrict = schoolOrDistrict;
    }

    public String getSchoolType() {
        return _schoolType;
    }

    public void setSchoolType(String schoolType) {
        _schoolType = schoolType;
    }

    public String getAddEdit() {
        return _addEdit;
    }

    public void setAddEdit(String addEdit) {
        _addEdit = addEdit;
    }

    public String getGrades() {
        return _grades;
    }

    public void setGrades(String grades) {
        _grades = grades;
    }

}
