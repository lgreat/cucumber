package gs.web.about.feedback;

import gs.data.state.State;
import gs.data.school.Grade;

/**
 * @author
 */
public class SubmitSchoolCommand {
    private String _submitterName;
    private String _submitterEmail;
    private String _submitterEmailConfirm;
    private String _submitterConnectionToSchool;

    private String _schoolName;
    private String _streetAddress;
    private String _city;
    private State _state;
    private String _zipCode;
    private String _county;

    private Integer _numStudentsEnrolled;
    private String _phoneNumber;
    private String _faxNumber;
    private String _schoolWebSite;

    private String _religion;
    private String _associationMemberships;

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

    public String getSchoolName() {
        return _schoolName;
    }

    public void setSchoolName(String schoolName) {
        _schoolName = schoolName;
    }

    public String getStreetAddress() {
        return _streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        _streetAddress = streetAddress;
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

    public String getZipCode() {
        return _zipCode;
    }

    public void setZipCode(String zipCode) {
        _zipCode = zipCode;
    }

    public String getCounty() {
        return _county;
    }

    public void setCounty(String county) {
        _county = county;
    }

    public Integer getNumStudentsEnrolled() {
        return _numStudentsEnrolled;
    }

    public void setNumStudentsEnrolled(Integer numStudentsEnrolled) {
        _numStudentsEnrolled = numStudentsEnrolled;
    }

    public String getPhoneNumber() {
        return _phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        _phoneNumber = phoneNumber;
    }

    public String getFaxNumber() {
        return _faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        _faxNumber = faxNumber;
    }

    public String getSchoolWebSite() {
        return _schoolWebSite;
    }

    public void setSchoolWebSite(String schoolWebSite) {
        _schoolWebSite = schoolWebSite;
    }

    public String getReligion() {
        return _religion;
    }

    public void setReligion(String religion) {
        _religion = religion;
    }

    public String getAssociationMemberships() {
        return _associationMemberships;
    }

    public void setAssociationMemberships(String associationMemberships) {
        _associationMemberships = associationMemberships;
    }
}
