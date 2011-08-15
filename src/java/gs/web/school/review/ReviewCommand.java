package gs.web.school.review;

import gs.data.school.review.CategoryRating;
import gs.data.school.review.Poster;
import gs.web.util.validator.EmailValidator;

/**
 * Command for parent reviews
 */
public class ReviewCommand implements EmailValidator.IEmail {
    private String _email;
    private String _confirmEmail;
    private String _firstName;
    private String _lastName;
    private String _comments;
    private String _client;
    private String _output;
    private String jobTitle;
    private String _how;

    private CategoryRating _teacher = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _parent = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _safety = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _overall = CategoryRating.DECLINE_TO_STATE;

    /**
     * K-12 parameters
     */
    private CategoryRating _principal = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _activities = CategoryRating.DECLINE_TO_STATE;

    /**
     * Preschool
     */
    private CategoryRating _pProgram = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _pFacilities = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _pSafetyPreschool = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _pTeachersPreschool = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _pParentsPreschool = CategoryRating.DECLINE_TO_STATE;

    private Poster _poster;


    private boolean _givePermission;
    private boolean _wantMssNL;
    private boolean _allowContact;
    private boolean _mssSub = true;

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public String getConfirmEmail() {
        return _confirmEmail;
    }

    public void setConfirmEmail(String confirmEmail) {
        _confirmEmail = confirmEmail;
    }

    public String getFirstName() {
        return _firstName;
    }

    public void setFirstName(String firstName) {
        _firstName = firstName;
    }

    public String getLastName() {
        return _lastName;
    }

    public void setLastName(String lastName) {
        _lastName = lastName;
    }

    public String getComments() {
        return _comments;
    }

    public void setComments(String comments) {
        _comments = comments;
    }

    public String getClient() {
        return _client;
    }

    public void setClient(String client) {
        _client = client;
    }

    public CategoryRating getPrincipal() {
        return _principal;
    }

    public void setPrincipal(CategoryRating principal) {
        _principal = principal;
    }

    public CategoryRating getTeacher() {
        return _teacher;
    }

    public void setTeacher(CategoryRating teacher) {
        _teacher = teacher;
    }

    public CategoryRating getActivities() {
        return _activities;
    }

    public void setActivities(CategoryRating activities) {
        _activities = activities;
    }

    public CategoryRating getParent() {
        return _parent;
    }

    public void setParent(CategoryRating parent) {
        _parent = parent;
    }

    public CategoryRating getSafety() {
        return _safety;
    }

    public void setSafety(CategoryRating safety) {
        _safety = safety;
    }

    public CategoryRating getOverall() {
        return _overall;
    }

    public void setOverall(CategoryRating overall) {
        _overall = overall;
    }

    public CategoryRating getPOverall() {
        return _overall;
    }

    public void setPOverall(CategoryRating pOverall) {
        _overall = pOverall;
    }

    public CategoryRating getPProgram() {
        return _pProgram;
    }

    public void setPProgram(CategoryRating pProgram) {
        _pProgram = pProgram;
    }

    public CategoryRating getPFacilities() {
        return _pFacilities;
    }

    public void setPFacilities(CategoryRating pFacilities) {
        _pFacilities = pFacilities;
    }

    public CategoryRating getPSafetyPreschool() {
        return _pSafetyPreschool;
    }

    public void setPSafetyPreschool(CategoryRating pSafetyPreschool) {
        _pSafetyPreschool = pSafetyPreschool;
    }

    public CategoryRating getPTeachersPreschool() {
        return _pTeachersPreschool;
    }

    public void setPTeachersPreschool(CategoryRating pTeachersPreschool) {
        _pTeachersPreschool = pTeachersPreschool;
    }

    public CategoryRating getPParentsPreschool() {
        return _pParentsPreschool;
    }

    public void setPParentsPreschool(CategoryRating pParentsPreschool) {
        _pParentsPreschool = pParentsPreschool;
    }

    public CategoryRating getPSafety() {
        return _safety;
    }

    public void setPSafety(CategoryRating pSafety) {
        _safety = pSafety;
    }

    public CategoryRating getPTeachers() {
        return _teacher;
    }

    public void setPTeachers(CategoryRating pTeachers) {
        _teacher = pTeachers;
    }

    public CategoryRating getPParents() {
        return _parent;
    }

    public void setPParents(CategoryRating pParents) {
        _parent = pParents;
    }

    public String getPosterAsString() {
        if (null == _poster) {
            return "";
        } else {
            return _poster.getName();
        }
    }

    public void setPosterAsString(String posterAsString) {
        _poster = Poster.createPoster(posterAsString);
    }

    public Poster getPoster() {
        return _poster;
    }

    public void setPoster(Poster poster) {
        _poster = poster;
    }

    public String getOutput() {
        return _output;
    }

    /** Set output format to html,xml,or json */
    public void setOutput(String output) {
        _output = output;
    }

    public boolean isGivePermission() {
        return _givePermission;
    }

    public void setGivePermission(boolean givePermission) {
        _givePermission = givePermission;
    }

    public boolean isWantMssNL() {
        return _wantMssNL;
    }

    public void setWantMssNL(boolean wantMssNL) {
        _wantMssNL = wantMssNL;
    }

    public boolean isAllowContact() {
        return _allowContact;
    }

    public void setAllowContact(boolean allowContact) {
        _allowContact = allowContact;
    }


    public String getPrincipalAsString() {
        if (_principal == null) return null;
        return _principal.getName();
    }

    public void setPrincipalAsString(String principalAsString) {
        _principal = CategoryRating.getCategoryRating(principalAsString);
    }

    public String getTeacherAsString() {
        if (_teacher == null) return null;
        return _teacher.getName();
    }

    public void setTeacherAsString(String teacherAsString) {
        _teacher = CategoryRating.getCategoryRating(teacherAsString);
    }

    public String getActivitiesAsString() {
        if (_activities == null) return null;
        return _activities.getName();
    }

    public void setActivitiesAsString(String activitiesAsString) {
        _activities = CategoryRating.getCategoryRating(activitiesAsString);
    }

    public String getParentAsString() {
        if (_parent == null) return null;
        return _parent.getName();
    }

    public void setParentAsString(String parentAsString) {
        _parent = CategoryRating.getCategoryRating(parentAsString);
    }

    public String getSafetyAsString() {
        if (_safety == null) return null;
        return _safety.getName();
    }

    public void setSafetyAsString(String safetyAsString) {
        _safety = CategoryRating.getCategoryRating(safetyAsString);
    }

    public String getOverallAsString() {
        if (_overall == null) return null;
        return _overall.getName();
    }

    public void setOverallAsString(String overallAsString) {
        _overall = CategoryRating.getCategoryRating(overallAsString);
    }

    public String getPProgramAsString() {
        if (_pProgram == null) return null;
        return _pProgram.getName();
    }

    public void setPProgramAsString(String rating) {
        _pProgram = CategoryRating.getCategoryRating(rating);
    }

     public String getPSafetyPreschoolAsString() {
         if (_pSafetyPreschool == null) return null;
        return _pSafetyPreschool.getName();
    }

    public void setPSafetyPreschoolAsString(String rating) {
        _pSafetyPreschool = CategoryRating.getCategoryRating(rating);
    }
    public String getPTeachersPreschoolAsString() {
        if (_pTeachersPreschool == null) return null;
        return _pTeachersPreschool.getName();
    }

    public void setPTeachersPreschoolAsString(String rating) {
        _pTeachersPreschool = CategoryRating.getCategoryRating(rating);
    }
    public String getPParentsPreschoolAsString() {
        if (_pParentsPreschool == null) return null;
        return _pParentsPreschool.getName();
    }

    public void setPParentsPreschoolAsString(String rating) {
        _pParentsPreschool = CategoryRating.getCategoryRating(rating);
    }

    public String getPParentsAsString() {
        if (_parent == null) return null;
        return _parent.getName();
    }

    public void setPParentsAsString(String rating) {
        _parent = CategoryRating.getCategoryRating(rating);
    }

    public String getPFacilitiesAsString() {
        if (_pFacilities == null) return null;
        return _pFacilities.getName();
    }

    public void setPFacilitiesAsString(String rating) {
        _pFacilities = CategoryRating.getCategoryRating(rating);
    }

    //Spring camelcases the form attribute pFacilitiesAsString to pFacilitiesAsString and looks for setpFacilitesAsString
    public void setpFacilitiesAsString(String rating) {
        setPFacilitiesAsString(rating);
    }

    public String getPSafetyAsString() {
        if (_safety == null) return null;
        return _safety.getName();
    }

    public void setPSafetyAsString(String rating) {
        _safety = CategoryRating.getCategoryRating(rating);
    }

    public String getPTeachersAsString() {
        if (_teacher == null) return null;
        return _teacher.getName();
    }

    public void setPTeachersAsString(String rating) {
        _teacher = CategoryRating.getCategoryRating(rating);
    }

    public String getPOverallAsString() {
        if (_overall == null) return null;
        return _overall.getName();
    }

    public void setPOverallAsString(String rating) {
        _overall = CategoryRating.getCategoryRating(rating);
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getHow() {
        return _how;
    }

    public void setHow(String how) {
        _how = how;
    }

    public boolean isMssSub() {
        return _mssSub;
    }

    public void setMssSub(boolean mssSub) {
        _mssSub = mssSub;
    }
}
