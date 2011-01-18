package gs.web.compare;

import java.util.List;
import java.util.Map;

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
    private Map<String, List<String>> _categoryResponses;

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

    public Map<String, List<String>> getCategoryResponses() {
        return _categoryResponses;
    }

    public void setCategoryResponses(Map<String, List<String>> categoryResponses) {
        _categoryResponses = categoryResponses;
    }
}
