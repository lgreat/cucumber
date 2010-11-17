package gs.web.compare;


/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ComparedSchoolRatingsStruct extends ComparedSchoolBaseStruct {
    private int _teacherRating = 0;
    private int _principalRating = 0;
    private int _parentRating = 0;

    public int getTeacherRating() {
        return _teacherRating;
    }

    public void setTeacherRating(int teacherRating) {
        _teacherRating = teacherRating;
    }

    public int getPrincipalRating() {
        return _principalRating;
    }

    public void setPrincipalRating(int principalRating) {
        _principalRating = principalRating;
    }

    public int getParentRating() {
        return _parentRating;
    }

    public void setParentRating(int parentRating) {
        _parentRating = parentRating;
    }

}
