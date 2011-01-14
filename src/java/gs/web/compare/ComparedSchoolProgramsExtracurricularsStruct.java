package gs.web.compare;

import static gs.web.compare.ComparedSchoolProgramsExtracurricularsStruct.SourceType.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ComparedSchoolProgramsExtracurricularsStruct extends ComparedSchoolBaseStruct {
    public static enum SourceType {
        Parents,
        Principal
    }

    private SourceType _programSource = Parents;
    private int _numResponses = 0;

    public SourceType getProgramSource() {
        return _programSource;
    }

    public void setProgramSource(SourceType programSource) {
        _programSource = programSource;
    }

    public int getNumResponses() {
        return _numResponses;
    }

    public void setNumResponses(int numResponses) {
        _numResponses = numResponses;
    }
}
