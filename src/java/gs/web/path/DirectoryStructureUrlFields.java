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
 * @author <a href="mailto:yfan@greatschools.org">Young Fan</a>
 */
public class DirectoryStructureUrlFields {

    public static final String CHOOSING_SCHOOLS_PAGE = "choosing-schools";


    public static final String ENROLLMENT_SCHOOLS_PAGE = "enrollment";

    public static final String EDUCATION_COMMUNITY_SCHOOLS_PAGE = "education-community";



    /**
     * Identifier providing additional information for identifying a single url
     * resource wrt the {@link DirectoryStructureUrlFields} approach.
     * <p>
     * <code>IDirectoryStructureUrlController</code> implementing controllers
     * may base their shouldSupport(...) decision making using this type.
     */
    public static enum ExtraResourceIdentifier {
        /**
         * Display of the ESP data for a school.
         */
        ESP_DISPLAY_PAGE;
    }
    
    private State _state = null;
    private String _cityName = null;
    private String _districtName = null;
    private final Set<SchoolType> _schoolTypes = new HashSet<SchoolType>();
    private String[] _schoolTypesParams = null;
    private LevelCode _levelCode = null;
    private String _schoolName = null;
    private String _schoolID = null; 
    private boolean _hasSchoolsLabel = false;

    private boolean _isChoosePage= false;

    private boolean _isEnrollmentPage= false;


    private boolean _isEducationCommunityPage= false;


    private ExtraResourceIdentifier _eri;

    public static final String LEVEL_LABEL_PRESCHOOLS = "preschools";
    public static final String LEVEL_LABEL_ELEMENTARY_SCHOOLS = "elementary-schools";
    public static final String LEVEL_LABEL_MIDDLE_SCHOOLS = "middle-schools";
    public static final String LEVEL_LABEL_HIGH_SCHOOLS = "high-schools";
    public static final String LEVEL_LABEL_SCHOOLS = "schools";

    public static final Pattern LEVEL_CODE_PATTERN =
        Pattern.compile("^(schools|preschools|elementary-schools|middle-schools|high-schools)$");
    public static final Pattern SCHOOL_TYPE_PATTERN =
        Pattern.compile("^(public|private|charter|public-private|public-charter|private-charter)$");
    public static final Pattern SCHOOL_NAME_OVERVIEW_PATTERN =
        Pattern.compile("^(\\d+)-(.+)$");

    public DirectoryStructureUrlFields(HttpServletRequest request) {
        // require that the request uri starts and ends with /
        String requestUri = request.getRequestURI();
        requestUri = requestUri.replaceAll("/gs-web", "");
        if (StringUtils.isBlank(requestUri) || !requestUri.startsWith("/")) {
            return;
        }

        // state: always take from session context
        SessionContext context = SessionContextUtil.getSessionContext(request);
        // if context is null, we can't be under a DirectoryStructureUrlController so let's exit early
        if (context != null) {
            _state = context.getState();
            String[] pathComponents = requestUri.split("/");
            set(pathComponents);
        }
    }
    
    private void clear() {
        _cityName = null;
        _districtName = null;
        _schoolTypes.clear();
        _schoolTypesParams = null;
        _levelCode = null;
        _schoolName = null;
        _schoolID = null; 
        _hasSchoolsLabel = false;
        _isChoosePage= false;
        _isEnrollmentPage= false;
        _isEducationCommunityPage= false;
        _eri = null;
    }
    
    private void set(String[] pathComponents) {
        clear();
        
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
            // or /california/alameda/1-Alameda-High-School/
            _cityName = pathComponents[2];
            if (pathComponents[3].equalsIgnoreCase(CHOOSING_SCHOOLS_PAGE)) {
                _isChoosePage= true;
            } else if (pathComponents[3].equalsIgnoreCase(ENROLLMENT_SCHOOLS_PAGE))
            {
                _isEnrollmentPage= true;
            } else if (pathComponents[3].equalsIgnoreCase(EDUCATION_COMMUNITY_SCHOOLS_PAGE))
            {
                _isEducationCommunityPage= true;
            }
            else
            {

            Matcher schoolTypeMatcher = SCHOOL_TYPE_PATTERN.matcher(pathComponents[3]);
            Matcher levelCodeMatcher = LEVEL_CODE_PATTERN.matcher(pathComponents[3]);
            Matcher schoolNameMatcher = SCHOOL_NAME_OVERVIEW_PATTERN.matcher(pathComponents[3]);
            if (schoolTypeMatcher.find()) {
                populateSchoolTypesFromLabel(pathComponents[3]);
            } else if (levelCodeMatcher.find()) {
                populateLevelCodeFromLabel(pathComponents[3]);
            } else if (schoolNameMatcher.matches()) {
                _schoolID = schoolNameMatcher.group(1);
                _schoolName = schoolNameMatcher.group(2);
            } else {
                try {
                    _districtName = URLDecoder.decode(pathComponents[3], "UTF-8");
                } catch (UnsupportedEncodingException uee) {
                    _districtName = pathComponents[3];
                }
            }
            }
        } else if (pathComponents.length == 5 ) {
            // /california/sonoma/public-charter/schools/ or
            // /california/san-francisco/San-Francisco-Unified-School-District/schools/ or
            // /california/alameda/1-Alameda-High-School/{eri}
            _cityName = pathComponents[2];
            Matcher schoolTypeMatcher = SCHOOL_TYPE_PATTERN.matcher(pathComponents[3]);
            Matcher levelCodeMatcher = LEVEL_CODE_PATTERN.matcher(pathComponents[4]);
            if (schoolTypeMatcher.find()) {
                populateSchoolTypesFromLabel(pathComponents[3]);
                if (levelCodeMatcher.find()) {
                    populateLevelCodeFromLabel(pathComponents[4]);
                }
            } else if (levelCodeMatcher.find()) {
                populateLevelCodeFromLabel(pathComponents[4]);
                _hasSchoolsLabel = true;
                try {
                    _districtName = URLDecoder.decode(pathComponents[3], "UTF-8");
                } catch (UnsupportedEncodingException uee) {
                    _districtName = pathComponents[3];
                }
            } else {
                // assume trailing eri token in URI
                String[] newPathComponents = new String[4];
                System.arraycopy(pathComponents, 0, newPathComponents, 0, 4);
                set(newPathComponents);
                _eri = ExtraResourceIdentifier.ESP_DISPLAY_PAGE;
                return;
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
            if (_state != null && _state.equals(State.NY) &&_cityName.equalsIgnoreCase("new york city")) {
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

        _schoolTypes.clear();
        _schoolTypes.addAll(schoolTypeSet);
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

    public void populateSchoolInfoFromLabel(String label) {
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

        _schoolTypes.clear();
        _schoolTypes.addAll(schoolTypeSet);
        _schoolTypesParams = paramSchoolType;
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

    public void setSchoolTypesParams(String[] schoolTypesParams) {
        _schoolTypesParams = schoolTypesParams;
    }

    public void setLevelCode(LevelCode levelCode) {
        _levelCode = levelCode;
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

    public boolean hasChoosePage() {
        return _isChoosePage;
    }


    public boolean hasEnrollmentPage() {
        return _isEnrollmentPage;
    }

    public boolean hasEducationCommunityPage() {
        return _isEducationCommunityPage;
    }




    public String getSchoolID() {
        return _schoolID;
    }

    public boolean hasSchoolID() {
        return _schoolID != null;
    }
    
    public ExtraResourceIdentifier getExtraResourceIdentifier() {
        return _eri;
    }

    @Override
    public String toString() {
        return "hasState: " + hasState() + ", hasCityName: " + hasCityName() + ", hasChoosePage " + hasChoosePage() + ", hasSchoolTypes: " + hasSchoolTypes() +
                ", hasLevelCode: " + hasLevelCode() + ", hasSchoolsLabel: " + hasSchoolsLabel() + ", hasSchoolName: " + hasSchoolName() +
                ", hasSchoolID: " + hasSchoolID() + ", schoolName: " + getSchoolName() + ", schoolID: " + getSchoolID() +
                ", cityName: " + getCityName() + (hasState()?", state: " + getState().getAbbreviation():"") +
                ", levelCode: " + getLevelCode() + ", schoolTypesParams: " + getSchoolTypesParams();
    }
}
