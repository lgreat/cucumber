package gs.web.compare;

import gs.data.geo.ILocation;
import gs.data.geo.LatLon;
import gs.data.school.School;
import gs.data.school.SchoolSubtype;
import gs.data.school.district.District;
import gs.web.jsp.Util;
import org.apache.commons.lang.StringUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ComparedSchoolBaseStruct implements ILocation {
    private School _school;
    private Integer _gsRating;
    private Integer _communityRating;

    public School getSchool() {
        return _school;
    }

    public void setSchool(School school) {
        _school = school;
    }

    public Integer getGsRating() {
        return _gsRating;
    }

    public void setGsRating(Integer gsRating) {
        _gsRating = gsRating;
    }

    public Integer getCommunityRating() {
        return _communityRating;
    }

    public void setCommunityRating(Integer communityRating) {
        _communityRating = communityRating;
    }

    /* Convenience methods */

    public String getName() {
        return getSchool().getName();
    }

    public String getStreet() {
        return getSchool().getPhysicalAddress().getStreet();
    }

    public String getCityStateZip() {
        return getSchool().getPhysicalAddress().getCityStateZip();
    }

    public String getEnrollment() {
        Integer enrollment = getSchool().getEnrollmentOrCapacity();
        if (enrollment != null && enrollment > 0) {
            return enrollment + " students";
        }
        return "";
    }

    public String getPhone() {
        return getSchool().getPhone();
    }

    public District getDistrict() {
        return getSchool().getDistrict();
    }

    public String getGender() {
        SchoolSubtype subtype = getSchool().getSubtype();
        if (subtype != null) {
            String schoolSubtype = subtype.getCommaSeparatedString();
            if (schoolSubtype.contains("all_male")) {
                return "all male";
            } else if (schoolSubtype.contains("all_female")) {
                return "all female";
            } else if (schoolSubtype.contains("coed")) {
                return "coed";
            }
        }
        return "";
    }

    public LatLon getLatLon() {
        return _school.getLatLon();
    }
    
    public String getType() {
        return Util.capitalize(getSchool().getType().getSchoolTypeName());
    }

    public boolean isPrivate() {
        return "Private".equals(getType());
    }

    public String getUniqueIdentifier() {
        return getSchool().getDatabaseState().getAbbreviationLowerCase() + getSchool().getId();
    }

    public String getGradeLevels() {
        // empty school.gradeLevels or school.gradeLevels eq 'n/a' or school.gradeLevels eq 'N/A'
        String rangeString = getSchool().getGradeLevels().getRangeString();
        if (StringUtils.equalsIgnoreCase("n/a", rangeString)) {
            rangeString = "";
        }
        return rangeString;
    }
}
