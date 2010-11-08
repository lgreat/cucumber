package gs.web.compare;

import gs.data.school.School;
import gs.data.school.SchoolSubtype;
import gs.data.school.district.District;
import org.apache.commons.lang.StringUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ComparedSchoolBaseStruct {
    private School _school;
    private Integer _gsRating;

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
}
