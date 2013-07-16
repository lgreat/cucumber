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

    private String _stateId;
    private String _ncesCode;

    private String _stateName;
    private String _cityName;
    private String _schoolType;
    private String _grades;

    private String _submitterName;
    private String _submitterEmail;
    private String _submitterEmailConfirm;
    private String _submitterConnectionToSchool;
    private String _submitterConnectionToSchoolText;
    private String _verificationUrl;

    private String _name;
    private String _street;
    private String _streetLine2;
    private String _city;
    private String _zipcode;
    private String _county;
    private String _county1;
    private String _enrollment;
    private String _phone;
    private String _fax;
    private String _webSite;
    private String _headOfficialName;
    private String _headOfficialEmail;
    private String _startTime;
    private String _endTime;
    private String _affiliation;
    private String _association;
    private String _lowAge;
    private String _highAge;
    private String _contactNotes;
    private String _gender;
    private String _bilingual;
    private String _specialEd;
    private String _computers;
    private String _extendedCare;
    private String _preschoolSubtype;
    private String _operatingSystem;
    private String _browser;

    private String _category;

    private String _open;
    private String _openSeason;
    private String _openYear;
    private String _applicable;
    private String _applicableSeason;
    private String _applicableYear;


    //private String[] listSubmitterName = {"eddie","andy"};

    private ContactUsCommand.FeedbackType _feedbackType = ContactUsCommand.FeedbackType.defaultOption;
    private SchoolInfoFields _schoolInfoFields;
    private DistrictInfoFields _districtInfoFields;
    private String _schoolId;
    private String _districtId;
    private State _state;


    /*
    */
    private String _challenge = "";
    private String _response = "";

    private String _schoolDistFieldsDisplayed;


    public String getDistrictId() {
        return _districtId;
    }

    public void setDistrictId(String districtId) {
        _districtId = districtId;
    }

    public String getCategory() {
        return _category;
    }

    public void setCategory(String category) {
        _category = category;
    }

    public String getPreschoolSubtype() {
        return _preschoolSubtype;
    }

    public void setPreschoolSubtype(String preschoolSubtype) {
        _preschoolSubtype = preschoolSubtype;
    }

    public String getStateName() {
        return _stateName;
    }

    public void setStateName(String stateName) {
        _stateName = stateName;
    }

    public String getCityName() {
        return _cityName;
    }

    public void setCityName(String cityName) {
        _cityName = cityName;
    }

    public ContactUsCommand.FeedbackType getFeedbackType() {
        return _feedbackType;
    }

    public void setFeedbackType(ContactUsCommand.FeedbackType feedbackType) {
        _feedbackType = feedbackType;
    }

    public String getVerificationUrl() {
        return _verificationUrl;
    }

    public void setVerificationUrl(String verificationUrl) {
        _verificationUrl = verificationUrl;
    }

    public String getEnrollment() {
        return _enrollment;
    }

    public void setEnrollment(String enrollment) {
        _enrollment = enrollment;
    }

    public String getPhone() {
        return _phone;
    }

    public void setPhone(String phone) {
        _phone = phone;
    }

    public String getFax() {
        return _fax;
    }

    public void setFax(String fax) {
        _fax = fax;
    }

    public String getWebSite() {
        return _webSite;
    }

    public void setWebSite(String webSite) {
        _webSite = webSite;
    }

    public String getHeadOfficialName() {
        return _headOfficialName;
    }

    public void setHeadOfficialName(String headOfficialName) {
        _headOfficialName = headOfficialName;
    }

    public String getHeadOfficialEmail() {
        return _headOfficialEmail;
    }

    public void setHeadOfficialEmail(String headOfficialEmail) {
        _headOfficialEmail = headOfficialEmail;
    }

    public String getStartTime() {
        return _startTime;
    }

    public void setStartTime(String startTime) {
        _startTime = startTime;
    }

    public String getEndTime() {
        return _endTime;
    }

    public void setEndTime(String endTime) {
        _endTime = endTime;
    }

    public String getGender() {
        return _gender;
    }

    public void setGender(String gender) {
        _gender = gender;
    }

    public String getAffiliation() {
        return _affiliation;
    }

    public void setAffiliation(String affiliation) {
        _affiliation = affiliation;
    }

    public String getAssociation() {
        return _association;
    }

    public void setAssociation(String association) {
        _association = association;
    }

    public String getLowAge() {
        return _lowAge;
    }

    public void setLowAge(String lowAge) {
        _lowAge = lowAge;
    }

    public String getHighAge() {
        return _highAge;
    }

    public void setHighAge(String highAge) {
        _highAge = highAge;
    }

    public String getContactNotes() {
        return _contactNotes;
    }

    public void setContactNotes(String contactNotes) {
        _contactNotes = contactNotes;
    }

    public String getBilingual() {
        return _bilingual;
    }

    public void setBilingual(String bilingual) {
        _bilingual = bilingual;
    }

    public String getSpecialEd() {
        return _specialEd;
    }

    public void setSpecialEd(String specialEd) {
        _specialEd = specialEd;
    }

    public String getComputers() {
        return _computers;
    }

    public void setComputers(String computers) {
        _computers = computers;
    }

    public String getExtendedCare() {
        return _extendedCare;
    }

    public void setExtendedCare(String extendedCare) {
        _extendedCare = extendedCare;
    }

    public String getOperatingSystem() {
        return _operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        _operatingSystem = operatingSystem;
    }

    public String getBrowser() {
        return _browser;
    }

    public void setBrowser(String browser) {
        _browser = browser;
    }

    public String getStateId() {
        return _stateId;
    }

    public void setStateId(String stateId) {
        _stateId = stateId;
    }

    public String getNcesCode() {
        return _ncesCode;
    }

    public void setNcesCode(String ncesCode) {
        _ncesCode = ncesCode;
    }

    public String getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(String schoolId) {
        _schoolId = schoolId;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getStreet() {
        return _street;
    }

    public void setStreet(String street) {
        _street = street;
    }

    public String getStreetLine2() {
        return _streetLine2;
    }

    public void setStreetLine2(String streetLine2) {
        _streetLine2 = streetLine2;
    }
    
    public String getCity() {
        return _city;
    }

    public void setCity(String city) {
        _city = city;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public String getZipcode() {
        return _zipcode;
    }

    public void setZipcode(String zipcode) {
        _zipcode = zipcode;
    }

    public String getCounty() {
        return _county;
    }

    public void setCounty(String county) {
        _county = county;
    }


    public String getCounty1() {
        return _county1;
    }

    public void setCounty1(String county1) {
        _county1 = county1;
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


    public String getSubmitterConnectionToSchoolText() {
        return _submitterConnectionToSchoolText;
    }

    public void setSubmitterConnectionToSchoolText(String submitterConnectionToSchoolText) {
        _submitterConnectionToSchoolText = submitterConnectionToSchoolText;
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

    public String getOpen() {
        return _open;
    }

    public void setOpen(String open) {
        _open = open;
    }


    public String getOpenYear() {
        return _openYear;
    }

    public void setOpenYear(String openYear) {
        _openYear = openYear;
    }

    public String getOpenSeason() {
        return _openSeason;
    }

    public void setOpenSeason(String openSeason) {
        _openSeason = openSeason;
    }


    public String getApplicable() {
        if(_applicable == null){_applicable = "now";}
        return _applicable;
    }

    public void setApplicable(String applicable) {
        _applicable = applicable;
    }

    public String getApplicableSeason() {
        return _applicableSeason;
    }

    public void setApplicableSeason(String applicableSeason) {
        _applicableSeason = applicableSeason;
    }

    public String getApplicableYear() {
        return _applicableYear;
    }

    public void setApplicableYear(String applicableYear) {
        _applicableYear = applicableYear;
    }

    public String getSchoolDistFieldsDisplayed() {
        return _schoolDistFieldsDisplayed;
    }

    public void setSchoolDistFieldsDisplayed(String schoolDistFieldsDisplayed) {
        _schoolDistFieldsDisplayed = schoolDistFieldsDisplayed;
    }




}
