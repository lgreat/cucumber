package gs.web.about.feedback;

import gs.data.state.State;
import gs.web.community.ICaptchaCommand;

/**
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class ContactUsCommand implements ICaptchaCommand {
    public static enum FeedbackType {
        defaultOption,
        incorrectSchoolDistrictInfo_incorrectSchool,
        incorrectSchoolDistrictInfo_incorrectDistrict,
        schoolRatingsReviews,
        esp,
        join
    }
    private FeedbackType _feedbackType = FeedbackType.defaultOption;
    private SchoolInfoFields _schoolInfoFields;
    private DistrictInfoFields _districtInfoFields;
    private JoinFields _schoolRatingsReviewsFields;
    private EspFields _espFields;
    private JoinFields _joinFields;
    private String submitterName;
    private String submitterEmail;
    private String _schoolId;
    private String _schoolName;
    private String _cityName;
    private State _state;
    private String _challenge = "";
    private String _response = "";

    public ContactUsCommand() {
        _schoolInfoFields = new SchoolInfoFields();
        _districtInfoFields = new DistrictInfoFields();
        _schoolRatingsReviewsFields = new JoinFields();
        _espFields = new EspFields();
        _joinFields = new JoinFields();
    }

    public FeedbackType getFeedbackType() {
        return _feedbackType;
    }

    public void setFeedbackType(FeedbackType feedbackType) {
        _feedbackType = feedbackType;
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

    public JoinFields getSchoolRatingsReviewsFields() {
        return _schoolRatingsReviewsFields;
    }

    public void setSchoolRatingsReviewsFields(JoinFields schoolRatingsReviewsFields) {
        _schoolRatingsReviewsFields = schoolRatingsReviewsFields;
    }

    public EspFields getEspFields() {
        return _espFields;
    }

    public void setEspFields(EspFields espFields) {
        _espFields = espFields;
    }

    public JoinFields getJoinFields() {
        return _joinFields;
    }

    public void setJoinFields(JoinFields joinFields) {
        _joinFields = joinFields;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public void setSubmitterEmail(String submitterEmail) {
        this.submitterEmail = submitterEmail;
    }

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

    public String getResponse() {
        return _response;
    }

    public void setResponse(String response) {
        _response = response;
    }

    public String getChallenge() {
        return _challenge;
    }

    public void setChallenge(String challenge) {
        _challenge = challenge;
    }
}
