package gs.web.search;

import gs.data.school.SchoolType;
import org.apache.commons.lang.StringUtils;
import gs.web.pagination.Pagination;
import gs.web.pagination.RequestedPage;

import java.util.HashSet;
import java.util.Set;

public class SchoolSearchCommand {

    private String _searchString;
    private String _state;
    private String[] _schoolTypes;
    private String _requestType = "html";
    private Double _lat;
    private Double _lon;
    private String _distance;
    private String[] _affiliations;
    private String _studentTeacherRatio;
    private String _schoolSize;
    private String _normalizedAddress;
    private boolean _sortChanged;
    private Integer minGreatSchoolsRating;
    private Integer minCommunityRating;
    private String _zipCode;

    // osp filters
    private String[] _beforeAfterCare;
    private Boolean _transportation;
    private Boolean _ell;
    private Boolean _studentsVouchers;
    private String[] _specialEdPrograms;
    private String[] _schoolFocus;
    private String[] _sports;
    private String[] _artsAndMusic;
    private String[] _studentClubs;


    private RequestedPage requestedPage;

    /**
     * The type of school search that will be performed
     */
    private SchoolSearchType _schoolSearchType;
    
    /**
     * Valid values are: [p e m h]
     */
    private String[] _gradeLevels;
    /**
     * The name of a <code>FieldSort</code enum. Enum specifies sort direction.
     */
    private String _sortBy;
    /**
     * The search result to start at, used for paging. Skips start-1 results.
     */
    private int _start;

    /**
     * Total number of results to display per page. 0 = all
     */
    private int _pageSize;

    public static int DEFAULT_PAGE_SIZE = 25;

    public SchoolSearchCommand() {
        _pageSize = DEFAULT_PAGE_SIZE;
        _schoolSearchType = SchoolSearchType.TIGHT;
    }

    public void setQ(String q) {
        setSearchString(q);
    }

    public String getSearchString() {
        return _searchString;
    }

    public void setSearchString(String searchString) {
        if (searchString != null) {
            _searchString = searchString.trim();
        }
    }

    public String getState() {
        return _state;
    }

    public void setState(String state) {
        _state = state;
    }

    // get rid of invalid and duplicate school types from array, and if no valid school types, then include all three (public, private, charter)
    public static String[] cleanSchoolTypes(String[] schoolTypesArray) {
        Set<SchoolType> schoolTypeSet = new HashSet<SchoolType>();
        if (schoolTypesArray != null) {
            for (String type : schoolTypesArray) {
                SchoolType schoolType = SchoolType.getSchoolType(type);
                if (schoolType != null && !schoolTypeSet.contains(schoolType)) {
                    schoolTypeSet.add(schoolType);
                }
            }
        }

        // if none are selected, show all
        if (schoolTypeSet.size() == 0) {
            return new String[0];
        } else {
            String[] cleanedTypes = new String[schoolTypeSet.size()];
            int i = 0;
            if (schoolTypeSet.contains(SchoolType.PUBLIC)) {
                cleanedTypes[i++] = SchoolType.PUBLIC.getSchoolTypeName();
            }
            if (schoolTypeSet.contains(SchoolType.PRIVATE)) {
                cleanedTypes[i++] = SchoolType.PRIVATE.getSchoolTypeName();
            }
            if (schoolTypeSet.contains(SchoolType.CHARTER)) {
                cleanedTypes[i++] = SchoolType.CHARTER.getSchoolTypeName();
            }
            return cleanedTypes;
        }
    }
    
    public String[] getSchoolTypes() {
        return cleanSchoolTypes(_schoolTypes);
    }

    /**
     * Sets school types
     * @param schoolTypes
     */
    public void setSt(String[] schoolTypes) {
        _schoolTypes = schoolTypes;
    }

    public String[] getGradeLevels() {
        return _gradeLevels;
    }

    public void setGradeLevels(String[] gradeLevels) {
        _gradeLevels = gradeLevels;
    }

    public String getSortBy() {
        return _sortBy;
    }

    public void setSortBy(String sortBy) {
        _sortBy = sortBy;
    }

    public int getStart() {
        return _start;
    }

    public void setStart(int start) {
        _start = start;
    }

    public int getPageSize() {
        return _pageSize;
    }

    public void setPageSize(int pageSize) {
        _pageSize = pageSize;
    }

    public String getRequestType() {
        return _requestType;
    }

    public void setRequestType(String requestType) {
        _requestType = requestType;
    }

    public boolean isAjaxRequest() {
        return "ajax".equals(_requestType);
    }

    public boolean hasSchoolTypes() {
        return (_schoolTypes != null && _schoolTypes.length > 0);
    }

    public boolean hasGradeLevels() {
        return (_gradeLevels != null && _gradeLevels.length > 0);
    }

    public SchoolSearchType getSearchType() {
        return _schoolSearchType;
    }

    public void setSchoolSearchType(String schoolSearchType) {
        _schoolSearchType = SchoolSearchType.valueOf(schoolSearchType);
    }

    public Double getLat() {
        return _lat;
    }

    public void setLat(Double lat) {
        _lat = lat;
    }

    public Double getLon() {
        return _lon;
    }

    public void setLon(Double lon) {
        _lon = lon;
    }

    public boolean hasLatLon() {
        return _lat != null && _lon != null;
    }

    public String getDistance() {
        return _distance;
    }

    public Float getDistanceAsFloat() {
        if (_distance != null) {
            return Float.parseFloat(_distance);
        }
        return null;
    }

    public void setDistance(String distance) {
        _distance = distance;
    }

    public boolean isNearbySearch() {
        return getLat() != null && getLon() != null &&
                getDistance() != null && getDistanceAsFloat() > 0.0f;
    }

    public boolean isNearbySearchByLocation() {
        return isNearbySearch() && StringUtils.isNotEmpty(_searchString);
    }

    public String[] getAffiliations() {
        return _affiliations;
    }

    public void setAffiliations(String[] affiliations) {
        _affiliations = affiliations;
    }

    public boolean hasAffiliations() {
        return (_affiliations != null && _affiliations.length > 0);
    }

    public String getStudentTeacherRatio() {
        return _studentTeacherRatio;
    }

    public void setStudentTeacherRatio(String studentTeacherRatio) {
        _studentTeacherRatio = studentTeacherRatio;
    }

    public String getSchoolSize() {
        return _schoolSize;
    }

    public void setSchoolSize(String schoolSize) {
        _schoolSize = schoolSize;
    }

    public RequestedPage getRequestedPage() {
        if (requestedPage == null) {
            requestedPage = Pagination.getRequestedPage(getPageSize(), getStart(), null, SchoolSearchController.SCHOOL_SEARCH_PAGINATION_CONFIG);
        }
        return requestedPage;
    }

    public Integer getMinGreatSchoolsRating() {
        return minGreatSchoolsRating;
    }

    public void setMinGreatSchoolsRating(Integer minGreatSchoolsRating) {
        this.minGreatSchoolsRating = minGreatSchoolsRating;
    }

    public Integer getMinCommunityRating() {
        return minCommunityRating;
    }

    public void setMinCommunityRating(Integer minCommunityRating) {
        this.minCommunityRating = minCommunityRating;
    }

    public void setRequestedPage(RequestedPage requestedPage) {
        this.requestedPage = requestedPage;
    }

    /**
     * The normalized address in by location searches (typically returned by a geocoder such as Google)
     */
    public String getNormalizedAddress() {
        return _normalizedAddress;
    }

    public void setNormalizedAddress(String normalizedAddress) {
        _normalizedAddress = normalizedAddress;
    }

    public boolean isSortChanged() {
        return _sortChanged;
    }

    public void setSortChanged(boolean sortChanged) {
        _sortChanged = sortChanged;
    }

    public String getZipCode() {
        return _zipCode;
    }

    public void setZipCode(String zipCode) {
        _zipCode = zipCode;
    }

    public String[] getBeforeAfterCare() {
        return _beforeAfterCare;
    }

    public void setBeforeAfterCare(String[] beforeAfterCare) {
        _beforeAfterCare = beforeAfterCare;
    }

    public Boolean getTransportation() {
        return _transportation;
    }

    public void setTransportation(Boolean transportation) {
        _transportation = transportation;
    }

    public Boolean getEll() {
        return _ell;
    }

    public void setEll(Boolean ell) {
        _ell = ell;
    }

    public Boolean getStudentsVouchers() {
        return _studentsVouchers;
    }

    public void setStudentsVouchers(Boolean studentsVouchers) {
        _studentsVouchers = studentsVouchers;
    }

    public String[] getSpecialEdPrograms() {
        return _specialEdPrograms;
    }

    public void setSpecialEdPrograms(String[] specialEdPrograms) {
        _specialEdPrograms = specialEdPrograms;
    }

    public String[] getSchoolFocus() {
        return _schoolFocus;
    }

    public void setSchoolFocus(String[] schoolFocus) {
        _schoolFocus = schoolFocus;
    }

    public String[] getSports() {
        return _sports;
    }

    public void setSports(String[] sports) {
        _sports = sports;
    }

    public String[] getArtsAndMusic() {
        return _artsAndMusic;
    }

    public void setArtsAndMusic(String[] artsAndMusic) {
        _artsAndMusic = artsAndMusic;
    }

    public String[] getStudentClubs() {
        return _studentClubs;
    }

    public void setStudentClubs(String[] studentClubs) {
        _studentClubs = studentClubs;
    }
}