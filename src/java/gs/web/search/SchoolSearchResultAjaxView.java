package gs.web.search;


import gs.data.geo.LatLon;
import gs.data.school.Grades;
import gs.data.search.beans.ISchoolSearchResult;
import gs.data.state.State;
import gs.data.util.Address;

public class SchoolSearchResultAjaxView {
    private final ISchoolSearchResult _schoolSearchResult;

    public SchoolSearchResultAjaxView(ISchoolSearchResult schoolSearchResult) {
        _schoolSearchResult = schoolSearchResult;
    }

    public Integer getId() {
        return _schoolSearchResult.getId();
    }

    public String getNcesCode() {
        return _schoolSearchResult.getNcesCode();
    }

    public String getWebsite() {
        return _schoolSearchResult.getWebsite();
    }

    public Integer getEnrollment() {
        return _schoolSearchResult.getEnrollment();
    }

    public String getDistrictName() {
        return _schoolSearchResult.getDistrictName();
    }

    public String getDatabaseState() {
        return _schoolSearchResult.getDatabaseState().getAbbreviation();
    }

    public String getPhone() {
        return _schoolSearchResult.getPhone();
    }

    public Integer getParentRating() {
        return _schoolSearchResult.getParentRating();
    }

    public String getAddress() {
        return _schoolSearchResult.getAddress().toString();
    }

    public String getCity() {
        return _schoolSearchResult.getAddress().getCity();
    }

    public String getZip() {
        return _schoolSearchResult.getAddress().getZip();
    }

    public String getStreet() {
        return _schoolSearchResult.getAddress().getStreet();
    }

    public String getStreetLine2() {
        return _schoolSearchResult.getAddress().getStreetLine2();
    }

    public String getFax() {
        return _schoolSearchResult.getFax();
    }

    public String getReviewBlurb() {
        return _schoolSearchResult.getReviewBlurb();
    }

    public String getName() {
        return _schoolSearchResult.getName();
    }

    public Integer getReviewCount() {
        return _schoolSearchResult.getReviewCount();
    }

    public String getSchoolType() {
        return _schoolSearchResult.getSchoolType();
    }

    public Double getDistance() {
        return _schoolSearchResult.getDistance();
    }

    public String getLevelCode() {
        return _schoolSearchResult.getLevelCode();
    }

    public Grades getGrades() {
        return _schoolSearchResult.getGrades();
    }

    public Integer getDistrictId() {
        return _schoolSearchResult.getDistrictId();
    }

    public Integer getGreatSchoolsRating() {
        return _schoolSearchResult.getGreatSchoolsRating();
    }

    public LatLon getLatLon() {
        return _schoolSearchResult.getLatLon();
    }
}
