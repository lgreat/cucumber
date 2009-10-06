package gs.web.path;

import gs.data.state.State;
import gs.data.school.SchoolType;
import gs.data.school.LevelCode;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:yfan@greatschools.net">Young Fan</a>
 */
public class DirectoryStructureUrlFields {
    private State _state = null;
    private String _cityName = null;
    private String _districtName = null;
    private Set<SchoolType> _schoolTypes = new HashSet<SchoolType>();
    private String[] _schoolTypesParams = null;
    private LevelCode _levelCode = null;
    private String _schoolName = null;
    private String _schoolID = null; 

    private boolean _hasSchoolsLabel = false;

    public static final String LEVEL_LABEL_PRESCHOOLS = "preschools";
    public static final String LEVEL_LABEL_ELEMENTARY_SCHOOLS = "elementary-schools";
    public static final String LEVEL_LABEL_MIDDLE_SCHOOLS = "middle-schools";
    public static final String LEVEL_LABEL_HIGH_SCHOOLS = "high-schools";
    public static final String LEVEL_LABEL_SCHOOLS = "schools";

    public static final Pattern LEVEL_CODE_PATTERN =
        Pattern.compile("^(schools|preschools|elementary-schools|middle-schools|high-schools)$");
    public static final Pattern SCHOOL_TYPE_PATTERN =
        Pattern.compile("^(public|private|charter|public-private|public-charter|private-charter)$");

    public DirectoryStructureUrlFields(HttpServletRequest request) {
        // require that the request uri starts and ends with /
        String requestUri = request.getRequestURI();
        requestUri = requestUri.replaceAll("/gs-web", "");
        if (StringUtils.isBlank(requestUri) || !requestUri.startsWith("/")) {
            return;
        }

        // state: always take from session context
        SessionContext context = SessionContextUtil.getSessionContext(request);
        _state = context.getState();

        String[] pathComponents = requestUri.split("/");

        // first, since statement above requires requestUri to start and end with /, there cannot be fewer than 2 elements in the split array;
        // the Java String split function does not include trailing empty strings in the resulting array. 
        // second, this also means element [0] is an empty string and element [n-1] is the last string before the closing slash
        // third, we ignore element [1] because that would be the state long name

        if (pathComponents.length == 2) {
            // /california
            // this could have just been the default case, but this is more explicit
        } else if (pathComponents.length == 3) {
            // /california/sonoma/
            _cityName = pathComponents[2];
        } else if (pathComponents.length == 4) {
            // /california/sonoma/schools/ or /california/sonoma/preschools/ or /california/sonoma/public-charter/
            // or /california/san-francisco/San-Francisco-Unified-School-District/
            _cityName = pathComponents[2];
            Matcher schoolTypeMatcher = SCHOOL_TYPE_PATTERN.matcher(pathComponents[3]);
            Matcher levelCodeMatcher = LEVEL_CODE_PATTERN.matcher(pathComponents[3]);
            if (schoolTypeMatcher.find()) {
                populateSchoolTypesFromLabel(pathComponents[3]);
            } else if (levelCodeMatcher.find()) {
                populateLevelCodeFromLabel(pathComponents[3]);
            } else {
                try {
                    _districtName = URLDecoder.decode(pathComponents[3], "UTF-8");
                } catch (UnsupportedEncodingException uee) {
                    _districtName = pathComponents[3];
                }
            }
        } else if (pathComponents.length == 5) {
            // /california/sonoma/public-charter/schools/ or
            // /california/san-francisco/San-Francisco-Unified-School-District/schools/
            _cityName = pathComponents[2];
            Matcher schoolTypeMatcher = SCHOOL_TYPE_PATTERN.matcher(pathComponents[3]);
            if (schoolTypeMatcher.find()) {
                populateSchoolTypesFromLabel(pathComponents[3]);
                Matcher levelCodeMatcher = LEVEL_CODE_PATTERN.matcher(pathComponents[4]);
                if (levelCodeMatcher.find()) {
                    populateLevelCodeFromLabel(pathComponents[4]);
                }
            } else if (pathComponents[4].equals(LEVEL_LABEL_SCHOOLS)) {
                try {
                    _districtName = URLDecoder.decode(pathComponents[3], "UTF-8");
                } catch (UnsupportedEncodingException uee) {
                    _districtName = pathComponents[3];
                }
                _hasSchoolsLabel = true;
            }
        } else if (pathComponents.length == 6) {
            // /california/sonoma/preschools/Preschool-Name/123/
            _cityName = pathComponents[2];
            Matcher levelCodeMatcher = LEVEL_CODE_PATTERN.matcher(pathComponents[3]);
            if (levelCodeMatcher.find()) {
                populateLevelCodeFromLabel(pathComponents[3]);
                _schoolName = pathComponents[4];
                _schoolID = pathComponents[5];
            }
        }

        if (StringUtils.isNotBlank(_cityName)) {
            _cityName = _cityName.replaceAll("-", " ").replaceAll("_", "-");
            if (_state.equals(State.NY) &&_cityName.equalsIgnoreCase("new york city")) {
                _cityName = "new york";
            }
        }

        if (StringUtils.isNotBlank(_districtName)) {
            _districtName = _districtName.replaceAll("-", " ").replaceAll("_", "-");
        }

        if (StringUtils.isNotBlank(_schoolName)) {
            // hyphens could have originally been space, hyphen, #, or / -- maybe other characters as needed,
            // so this is not deterministic for a search by school name
            _schoolName = _schoolName.replaceAll("-", " ");
        }
    }

    public void populateSchoolTypesFromLabel(String label) {
        String[] paramSchoolType = null;
        Set<SchoolType> schoolTypeSet = new HashSet<SchoolType>();
        List<String> schoolTypes = new ArrayList<String>();
        if (label.contains(SchoolType.PUBLIC.getSchoolTypeName())) {
            schoolTypes.add(SchoolType.PUBLIC.getSchoolTypeName());
            schoolTypeSet.add(SchoolType.PUBLIC);
        }
        if (label.contains(SchoolType.PRIVATE.getSchoolTypeName())) {
            schoolTypes.add(SchoolType.PRIVATE.getSchoolTypeName());
            schoolTypeSet.add(SchoolType.PRIVATE);
        }
        if (label.contains(SchoolType.CHARTER.getSchoolTypeName())) {
            schoolTypes.add(SchoolType.CHARTER.getSchoolTypeName());
            schoolTypeSet.add(SchoolType.CHARTER);
        }

        if (schoolTypes.size() > 0) {
            paramSchoolType = schoolTypes.toArray(new String[schoolTypes.size()]);
        }

        _schoolTypes = schoolTypeSet;
        _schoolTypesParams = paramSchoolType;
    }

    public void populateLevelCodeFromLabel(String label) {
        if (LEVEL_LABEL_PRESCHOOLS.equals(label)) {
            _levelCode = LevelCode.PRESCHOOL;
            _hasSchoolsLabel = true;
        } else if (LEVEL_LABEL_ELEMENTARY_SCHOOLS.equals(label)) {
            _levelCode = LevelCode.ELEMENTARY;
            _hasSchoolsLabel = true;
        } else if (LEVEL_LABEL_MIDDLE_SCHOOLS.equals(label)) {
            _levelCode = LevelCode.MIDDLE;
            _hasSchoolsLabel = true;
        } else if (LEVEL_LABEL_HIGH_SCHOOLS.equals(label)) {
            _levelCode = LevelCode.HIGH;
            _hasSchoolsLabel = true;
        } else if (LEVEL_LABEL_SCHOOLS.equals(label)) {
            _levelCode = null;
            _hasSchoolsLabel = true;
        } else {
            _hasSchoolsLabel = false;
        }
    }

    public State getState() {
        return _state;
    }

    public boolean hasState() {
        return _state != null;
    }

    public String getCityName() {
        return _cityName;
    }

    public boolean hasCityName() {
        return StringUtils.isNotBlank(_cityName);
    }

    public String getDistrictName() {
        return _districtName;
    }

    public boolean hasDistrictName() {
        return StringUtils.isNotBlank(_districtName);
    }

    public Set<SchoolType> getSchoolTypes() {
        return _schoolTypes;
    }

    public boolean hasSchoolTypes() {
        // empty set means all school types
        return _schoolTypes != null;
    }

    public String[] getSchoolTypesParams() {
        return _schoolTypesParams;
    }

    public LevelCode getLevelCode() {
        return _levelCode;
    }

    public boolean hasLevelCode() {
        return _levelCode != null;
    }

    public String getSchoolName() {
        return _schoolName;
    }

    public boolean hasSchoolName() {
        return _schoolName != null;
    }

    public boolean hasSchoolsLabel() {
        return _hasSchoolsLabel;
    }

    public String getSchoolID() {
        return _schoolID;
    }

    public boolean hasSchoolID() {
        return _schoolID != null;
    }

    @Override
    public String toString() {
        return "hasState: " + hasState() + ", hasCityName: " + hasCityName() + ", hasSchoolTypes: " + hasSchoolTypes() +
                ", hasLevelCode: " + hasLevelCode() + ", hasSchoolsLabel: " + hasSchoolsLabel() + ", hasSchoolName: " + hasSchoolName() +
                ", hasSchoolID: " + hasSchoolID() + ", schoolName: " + getSchoolName() + ", schoolID: " + getSchoolID() +
                ", cityName: " + getCityName() + ", state: " + getState().getAbbreviation() +
                ", levelCode: " + getLevelCode() + ", schoolTypesParams: " + getSchoolTypesParams();
    }
}
