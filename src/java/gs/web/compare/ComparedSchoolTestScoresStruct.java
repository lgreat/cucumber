package gs.web.compare;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ComparedSchoolTestScoresStruct extends ComparedSchoolBaseStruct {
    private boolean _hasTestScores = false;

    public boolean isHasTestScores() {
        return _hasTestScores;
    }

    public void setHasTestScores(boolean hasTestScores) {
        _hasTestScores = hasTestScores;
    }
}
