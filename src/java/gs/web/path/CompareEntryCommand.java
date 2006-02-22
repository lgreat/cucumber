package gs.web.path;

/**
 * A simple javabean to collect form data from the compare
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CompareEntryCommand {

    private String _schoolType;
    private String _area;

    public String getSchoolType() {
        return _schoolType;
    }

    public void setSchoolType(String schoolType) {
        _schoolType = schoolType;
    }

    public String getArea() {
        return _area;
    }

    public void setArea(String area) {
        _area = area;
    }
}
