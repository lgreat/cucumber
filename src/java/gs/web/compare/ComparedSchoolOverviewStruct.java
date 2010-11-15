package gs.web.compare;

import gs.data.school.SchoolSubtype;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ComparedSchoolOverviewStruct extends ComparedSchoolBaseStruct {


    /* Convenience methods */
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
