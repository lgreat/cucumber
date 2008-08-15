package gs.web.about.feedback;

public class SubmitPreschoolCommand extends SubmitSchoolCommand {
    private String _lowestAge;
    private String _highestAge;
    private String _bilingualEd;
    private String _specialEd;
    private String _computersPresent;
    private String _extendedCare;
    private String _preschoolSubtype;

    public String getLowestAge() {
        return _lowestAge;
    }

    public void setLowestAge(String lowestAge) {
        _lowestAge = lowestAge;
    }

    public String getHighestAge() {
        return _highestAge;
    }

    public void setHighestAge(String highestAge) {
        _highestAge = highestAge;
    }

    public String getBilingualEd() {
        return _bilingualEd;
    }

    public void setBilingualEd(String bilingualEd) {
        _bilingualEd = bilingualEd;
    }

    public String getSpecialEd() {
        return _specialEd;
    }

    public void setSpecialEd(String specialEd) {
        _specialEd = specialEd;
    }

    public String getComputersPresent() {
        return _computersPresent;
    }

    public void setComputersPresent(String computersPresent) {
        _computersPresent = computersPresent;
    }

    public String getExtendedCare() {
        return _extendedCare;
    }

    public void setExtendedCare(String extendedCare) {
        _extendedCare = extendedCare;
    }

    public String getPreschoolSubtype() {
        return _preschoolSubtype;
    }

    public void setPreschoolSubtype(String preschoolSubtype) {
        _preschoolSubtype = preschoolSubtype;
    }
}
