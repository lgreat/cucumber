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

import org.apache.commons.lang.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Sep 25, 2008
 * Time: 1:00:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryStructureUrlFields {
    private State _state = null;
    private String _cityName = null;
    private Set<SchoolType> _schoolTypes = new HashSet<SchoolType>();
    private String[] _schoolTypesParams = null;
    private LevelCode _levelCode = null;
    private String _schoolName = null;

    private boolean _hasSchoolsLabel = false;

    public static final String LEVEL_LABEL_PRESCHOOLS = "preschools";
    public static final String LEVEL_LABEL_ELEMENTARY_SCHOOLS = "elementary-schools";
    public static final String LEVEL_LABEL_MIDDLE_SCHOOLS = "middle-schools";
    public static final String LEVEL_LABEL_HIGH_SCHOOLS = "high-schools";
    public static final String LEVEL_LABEL_SCHOOLS = "schools";

    public static final Pattern LEVEL_CODE_PATTERN =
        Pattern.compile("(schools|preschools|elementary-schools|middle-schools|high-schools)");
    public static final Pattern SCHOOL_TYPE_PATTERN =
        Pattern.compile("(public|private|charter|public-private|public-charter|private-charter)");

    public DirectoryStructureUrlFields(HttpServletRequest request) {
        // require that the request uri starts and ends with /
        String requestUri = request.getRequestURI();
        if (StringUtils.isBlank(requestUri) || !requestUri.startsWith("/") || !requestUri.endsWith("/")) {
            return;
        }

        // state: always take from session context
        SessionContext context = SessionContextUtil.getSessionContext(request);
        State state = context.getState();
        _state = state;

        String[] pathComponents = requestUri.split("/");

        // first, since statement above requires requestUri to start and end with /, there cannot be fewer than 2 elements in the split array;
        // the Java String split function does not include trailing empty strings in the resulting array. 
        // second, this also means element [0] is an empty string and element [n-1] is the last string before the closing slash
        // third, we ignore element [1] because that would be the state long name
        // fourth, the /california/ case should never occur, i.e. never will pathComponents.length = 3 due to apache redirects that send those requests to R&C 

        if (pathComponents.length == 3) {
            // /california/sonoma/
            _cityName = pathComponents[2];
        } else if (pathComponents.length == 4) {
            // /california/sonoma/schools/ or /california/sonoma/preschools/ or /california/sonoma/public-charter/ or /california/sonoma/somethingUnexpected/
            _cityName = pathComponents[2];
            Matcher schoolTypeMatcher = SCHOOL_TYPE_PATTERN.matcher(pathComponents[3]);
            Matcher levelCodeMatcher = LEVEL_CODE_PATTERN.matcher(pathComponents[3]);
            if (schoolTypeMatcher.find()) {
                populateSchoolTypesFromLabel(pathComponents[3]);
            } else if (levelCodeMatcher.find()) {
                populateLevelCodeFromLabel(pathComponents[3]);
            }
        } else if (pathComponents.length == 5) {
            // /california/sonoma/public-charter/schools/ or /california/sonoma/preschools/Preschool-Name/
            _cityName = pathComponents[2];
            Matcher schoolTypeMatcher = SCHOOL_TYPE_PATTERN.matcher(pathComponents[3]);
            Matcher levelCodeMatcher = LEVEL_CODE_PATTERN.matcher(pathComponents[3]);
            if (schoolTypeMatcher.find()) {
                populateSchoolTypesFromLabel(pathComponents[3]);
                Matcher levelCodeMatcher2 = LEVEL_CODE_PATTERN.matcher(pathComponents[4]);
                if (levelCodeMatcher2.find()) {
                    populateLevelCodeFromLabel(pathComponents[4]);
                }
            } else if (levelCodeMatcher.find()) {
                populateLevelCodeFromLabel(pathComponents[3]);
                _schoolName = pathComponents[4]; 
            }
        }

        if (StringUtils.isNotBlank(_cityName)) {
            _cityName = _cityName.replaceAll("-", " ").replaceAll("_", "-");
        }

        if (StringUtils.isNotBlank(_schoolName)) {
            _schoolName = _schoolName.replaceAll("-", " ").replaceAll("_", "-").replaceAll("=", "#").replaceAll("|", "/");
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

    @Override
    public String toString() {
        return "hasState: " + hasState() + ", hasCityName: " + hasCityName() + ", hasSchoolTypes: " + hasSchoolTypes() +
                ", hasLevelCode: " + hasLevelCode() + ", hasSchoolsLabel: " + hasSchoolsLabel() + ", hasSchoolName: " + hasSchoolName() +
                ", schoolName: " + getSchoolName() + ", cityName: " + getCityName() + ", state: " + getState().getAbbreviation() +
                ", levelCode: " + getLevelCode() + ", schoolTypesParams: " + getSchoolTypesParams();
    }
}
