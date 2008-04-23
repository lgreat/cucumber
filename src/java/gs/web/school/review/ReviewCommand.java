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
    
    private CategoryRating _principal = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _teacher = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _activities = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _parent = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _safety = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _overall = CategoryRating.DECLINE_TO_STATE;

    private CategoryRating _pOverall = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _pProgram = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _pFacilities = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _pSafety = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _pTeachers = CategoryRating.DECLINE_TO_STATE;
    private CategoryRating _pParents = CategoryRating.DECLINE_TO_STATE;

    private Poster _poster;

    private boolean _givePermission;
    private boolean _wantMssNL;
    private boolean _allowContact;

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
        return _pOverall;
    }

    public void setPOverall(CategoryRating pOverall) {
        _pOverall = pOverall;
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

    public CategoryRating getPSafety() {
        return _pSafety;
    }

    public void setPSafety(CategoryRating pSafety) {
        _pSafety = pSafety;
    }

    public CategoryRating getPTeachers() {
        return _pTeachers;
    }

    public void setPTeachers(CategoryRating pTeachers) {
        _pTeachers = pTeachers;
    }

    public CategoryRating getPParents() {
        return _pParents;
    }

    public void setPParents(CategoryRating pParents) {
        _pParents = pParents;
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
        return _principal.getName();
    }

    public void setPrincipalAsString(String principalAsString) {
        _principal = CategoryRating.getCategoryRating(principalAsString);
    }

    public String getTeacherAsString() {
        return _teacher.getName();
    }

    public void setTeacherAsString(String teacherAsString) {
        _teacher = CategoryRating.getCategoryRating(teacherAsString);
    }

    public String getActivitiesAsString() {
        return _activities.getName();
    }

    public void setActivitiesAsString(String activitiesAsString) {
        _activities = CategoryRating.getCategoryRating(activitiesAsString);
    }

    public String getParentAsString() {
        return _parent.getName();
    }

    public void setParentAsString(String parentAsString) {
        _parent = CategoryRating.getCategoryRating(parentAsString);
    }

    public String getSafetyAsString() {
        return _safety.getName();
    }

    public void setSafetyAsString(String safetyAsString) {
        _safety = CategoryRating.getCategoryRating(safetyAsString);
    }

    public String getOverallAsString() {
        return _overall.getName();
    }

    public void setOverallAsString(String overallAsString) {
        _overall = CategoryRating.getCategoryRating(overallAsString);
    }

    public String getPProgramAsString() {
        return _pProgram.getName();
    }

    public void setPProgramAsString(String rating) {
        _pProgram = CategoryRating.getCategoryRating(rating);
    }

    public String getPParentsAsString() {
        return _pParents.getName();
    }

    public void setPParentsAsString(String rating) {
        _pParents = CategoryRating.getCategoryRating(rating);
    }

    public String getPFacilitiesAsString() {
        return _pFacilities.getName();
    }

    public void setPFacilitiesAsString(String rating) {
        _pFacilities = CategoryRating.getCategoryRating(rating);
    }

    public String getPSafetyAsString() {
        return _pSafety.getName();
    }

    public void setPSafetyAsString(String rating) {
        _pSafety = CategoryRating.getCategoryRating(rating);
    }

    public String getPTeachersAsString() {
        return _pTeachers.getName();
    }

    public void setPTeachersAsString(String rating) {
        _pTeachers = CategoryRating.getCategoryRating(rating);
    }

    public String getPOverallAsString() {
        return _pOverall.getName();
    }

    public void setPOverallAsString(String rating) {
        _pOverall = CategoryRating.getCategoryRating(rating);
    }
}
