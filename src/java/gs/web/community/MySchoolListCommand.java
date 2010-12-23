package gs.web.community;

public class MySchoolListCommand {
    private String _email;

    /**
     * The database state of the school to work with
     */
    private String _schoolDatabaseState;

    /**
     * The ID of the school to work with
     */
    private Integer _schoolId;

    public String getEmail() {
        return _email;
    }

    /**
     * Where to redirect to when performing page refresh in order to set login cookie
     */
    private String _redirectUrl;

    public void setEmail(String email) {
        _email = email;
    }

    public String getSchoolDatabaseState() {
        return _schoolDatabaseState;
    }

    public void setSchoolDatabaseState(String schoolDatabaseState) {
        _schoolDatabaseState = schoolDatabaseState;
    }

    public Integer getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(Integer schoolId) {
        _schoolId = schoolId;
    }

    public String getRedirectUrl() {
        return _redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        _redirectUrl = redirectUrl;
    }

}
