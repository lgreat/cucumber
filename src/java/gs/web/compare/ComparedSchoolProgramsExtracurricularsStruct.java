package gs.web.compare;

import java.util.Map;
import java.util.Set;

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
    private Map<String, Set<String>> _categoryResponses;

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

    public Map<String, Set<String>> getCategoryResponses() {
        return _categoryResponses;
    }

    public void setCategoryResponses(Map<String, Set<String>> categoryResponses) {
        _categoryResponses = categoryResponses;
    }
}
