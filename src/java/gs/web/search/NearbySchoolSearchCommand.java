package gs.web.search;

import gs.data.search.FieldSort;
import org.apache.commons.lang.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: 3/2/11
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class NearbySchoolSearchCommand {
    private SchoolSearchCommand _schoolSearchCommand;
    private String _zipCode;
    private String _redirectUrl;

    public NearbySchoolSearchCommand() {
        _schoolSearchCommand = new SchoolSearchCommand();
        setSortBy(FieldSort.DISTANCE.name());
    }

    public SchoolSearchCommand getSchoolSearchCommand() {
        return _schoolSearchCommand;
    }

    public String getZipCode() {
        return _zipCode;
    }

    public void setZipCode(String zipCode) {
        _zipCode = StringUtils.trim(zipCode);
    }

    public String getRedirectUrl() {
        return _redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        _redirectUrl = redirectUrl;
    }

    public void setQ(String q) {
        _schoolSearchCommand.setQ(q);
    }

    public String getSearchString() {
        return _schoolSearchCommand.getSearchString();
    }

    public void setSearchString(String searchString) {
        _schoolSearchCommand.setSearchString(searchString);
    }

    public String getState() {
        return _schoolSearchCommand.getState();
    }

    public void setState(String state) {
        _schoolSearchCommand.setState(state);
    }

    public String[] cleanSchoolTypes(String[] schoolTypesArray) {
        return _schoolSearchCommand.cleanSchoolTypes(schoolTypesArray);
    }

    public String[] getSchoolTypes() {
        return _schoolSearchCommand.getSchoolTypes();
    }

    public void setSt(String[] schoolTypes) {
        _schoolSearchCommand.setSt(schoolTypes);
    }

    public String[] getGradeLevels() {
        return _schoolSearchCommand.getGradeLevels();
    }

    public void setGradeLevels(String[] gradeLevels) {
        _schoolSearchCommand.setGradeLevels(gradeLevels);
    }

    public String getSortBy() {
        return _schoolSearchCommand.getSortBy();
    }

    public void setSortBy(String sortBy) {
        _schoolSearchCommand.setSortBy(sortBy);
    }

    public int getStart() {
        return _schoolSearchCommand.getStart();
    }

    public void setStart(int start) {
        _schoolSearchCommand.setStart(start);
    }

    public int getPageSize() {
        return _schoolSearchCommand.getPageSize();
    }

    public void setPageSize(int pageSize) {
        _schoolSearchCommand.setPageSize(pageSize);
    }

    public int getCurrentPage() {
        return _schoolSearchCommand.getCurrentPage();
    }

    public String getRequestType() {
        return _schoolSearchCommand.getRequestType();
    }

    public void setRequestType(String requestType) {
        _schoolSearchCommand.setRequestType(requestType);
    }

    public boolean isAjaxRequest() {
        return _schoolSearchCommand.isAjaxRequest();
    }

    public boolean hasSchoolTypes() {
        return _schoolSearchCommand.hasSchoolTypes();
    }

    public boolean hasGradeLevels() {
        return _schoolSearchCommand.hasGradeLevels();
    }

    public SchoolSearchType getSearchType() {
        return _schoolSearchCommand.getSearchType();
    }

    public void setSchoolSearchType(String schoolSearchType) {
        _schoolSearchCommand.setSchoolSearchType(schoolSearchType);
    }

    public Double getLat() {
        return _schoolSearchCommand.getLat();
    }

    public void setLat(Double lat) {
        _schoolSearchCommand.setLat(lat);
    }

    public Double getLon() {
        return _schoolSearchCommand.getLon();
    }

    public void setLon(Double lon) {
        _schoolSearchCommand.setLon(lon);
    }

    public String getDistance() {
        return _schoolSearchCommand.getDistance();
    }

    public Float getDistanceAsFloat() {
        return _schoolSearchCommand.getDistanceAsFloat();
    }

    public void setDistance(String distance) {
        _schoolSearchCommand.setDistance(distance);
    }

    public boolean isNearbySearch() {
        return _schoolSearchCommand.isNearbySearch();
    }

    public void setStudentTeacherRatio(String studentTeacherRatio) {
        _schoolSearchCommand.setStudentTeacherRatio(studentTeacherRatio);
    }

    public String getStudentTeacherRatio() {
        return _schoolSearchCommand.getStudentTeacherRatio();
    }

    public String[] getAffiliations() {
        return _schoolSearchCommand.getAffiliations();
    }

    public void setAffiliations(String[] affiliations) {
        _schoolSearchCommand.setAffiliations(affiliations);
    }

    public boolean hasAffiliations() {
        return _schoolSearchCommand.hasAffiliations();
    }

    public String getSchoolSize() {
        return _schoolSearchCommand.getSchoolSize();
    }

    public void setSchoolSize(String schoolSize) {
        _schoolSearchCommand.setSchoolSize(schoolSize);
    }
}
