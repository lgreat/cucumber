package gs.web.compare;

import org.apache.commons.lang.StringUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ComparedSchoolOverviewStruct extends ComparedSchoolBaseStruct {


    /* Convenience methods */

    public String getType() {
        return getSchool().getType().getName();
    }

    public boolean isPrivate() {
        return "private".equals(getType());
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
