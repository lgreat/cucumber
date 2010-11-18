package gs.web.compare;

import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ComparedSchoolStudentTeacherStruct extends ComparedSchoolBaseStruct {
    private Map<String, String> _ethnicities;

    public Map<String, String> getEthnicities() {
        return _ethnicities;
    }

    public void setEthnicities(Map<String, String> ethnicities) {
        _ethnicities = ethnicities;
    }
}
