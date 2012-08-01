package gs.web.search;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.LevelCode;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.search.FieldSort;
import gs.data.search.filters.SchoolFilters;
import gs.data.state.State;
import gs.web.pagination.RequestedPage;
import gs.web.path.DirectoryStructureUrlFields;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class SchoolSearchCommandWithFields {
    
    private final SchoolSearchCommand _command;
    private final DirectoryStructureUrlFields _fields;

    // This has to be added since getting the correct state for the search requires looking in this map
    // that was added to the HttpServletRequest
    private Map<String,Object> _nearbySearchInfo;

    private District _district;
    private City _cityFromUrl;
    private City _cityFromSearchString;
    private IDistrictDao _districtDao;
    private IGeoDao _geoDao;

    private boolean _hasAlreadyLookedForCityInSearchString;

    public SchoolSearchCommandWithFields(SchoolSearchCommand command, DirectoryStructureUrlFields fields) {
        _command = command;
        _fields = fields;
    }

    public SchoolSearchCommandWithFields(SchoolSearchCommand command, DirectoryStructureUrlFields fields, Map<String,Object> nearbySearchInfo) {
        _command = command;
        _fields = fields;
        _nearbySearchInfo = nearbySearchInfo;
    }

    public String[] getSchoolTypes() {
        String[] schoolTypes = null;

        if (_command != null && _command.hasSchoolTypes()) {
            schoolTypes = _command.getSchoolTypes();
        } else if (_fields != null) {
            schoolTypes = _fields.getSchoolTypesParams();
        } else {
            //return null
        }

        return schoolTypes;
    }

    public String[] getGradeLevels() {
        String[] gradeLevels = null;
        if (_command != null && _command.hasGradeLevels() && !(_command.getGradeLevels().length == 1 && "".equals(_command.getGradeLevels()[0]))) {
            gradeLevels = _command.getGradeLevels();
        } else if (_fields != null && _fields.getLevelCode() != null) {
            gradeLevels = _fields.getLevelCode().getCommaSeparatedString().split(",");
        }
        return gradeLevels;
    }

    public String[] getGradeLevelNames() {
        LevelCode levelCode = getLevelCode();
        Set<LevelCode.Level> levels = levelCode.getIndividualLevelCodes();
        String[] gradeLevelNames = new String[levels.size()];

        int i = 0;
        for (LevelCode.Level level : levels) {
            gradeLevelNames[i++] = level.getLongName();
        }

        return gradeLevelNames;
    }

    public LevelCode getLevelCode() {
        LevelCode levelCode = null;
        if (_command != null && _command.hasGradeLevels()) {
            levelCode = LevelCode.createLevelCode(_command.getGradeLevels());
        }
        if (levelCode == null && _fields != null) {
            levelCode = _fields.getLevelCode();
        }
        return levelCode;
    }

    public boolean hasSchoolTypes() {
        String[] schoolTypes = getSchoolTypes();
        return schoolTypes != null && schoolTypes.length > 0;
    }

    //TODO: move these methods into DirectoryStructureUrlFields class
    public boolean isDistrictBrowse() {
        return _fields != null
                && StringUtils.isNotBlank(_fields.getDistrictName())
                && StringUtils.isNotBlank(_fields.getCityName());
    }
    public boolean isCityBrowse() {
        return _fields != null
                && StringUtils.isNotBlank(_fields.getCityName())
                && StringUtils.isBlank(_fields.getDistrictName());
    }

    /**
     * Copied from SchoolSearchController
     * @return
     */
    public boolean isSearch() {
        return !isCityBrowse() && !isDistrictBrowse();
    }

    public boolean isNearbySearch() {
        return _command.isNearbySearch();
    }

    public boolean isNearbySearchByLocation() {
        return _command.isNearbySearchByLocation();
    }



    public boolean isAjaxRequest() {
        return _command.isAjaxRequest();
    }

    public String getNormalizedAddress() {
        return _command.getNormalizedAddress();
    }

    public RequestedPage getRequestedPage() {
        return _command.getRequestedPage();
    }

    /**
     * @return a <code>District</code> if one is found, otherwise null.
     */
    public District getDistrict() {
        if (_district == null) {
            if (isDistrictBrowse()) {
                // might be null
                _district = getDistrictDao().findDistrictByNameAndCity(_fields.getState(), _fields.getDistrictName(), _fields.getCityName());
            }
        }
       return _district;
    }

    /**
     * Used by unit tests
     */
    public void setDistrict(District district) {
        _district = district;
    }

    /**
     * Used by unit tests
     */
    public void setCityFromUrl(City city) {
        _cityFromUrl = city;
    }

    public City getCityFromUrl() {
        if (_cityFromUrl == null) {
            if (_fields != null) {
                State state = _fields.getState();
                String cityName = _fields.getCityName();
                if (StringUtils.isNotBlank(cityName) && state != null) {
                    _cityFromUrl = getCity(state, cityName);
                }
            }
        }
        return _cityFromUrl;
    }

    public City getCityFromSearchString() {
        if (_cityFromSearchString == null && !_hasAlreadyLookedForCityInSearchString) {
            if (_command != null && _command.getSearchString() != null) {
                State state = getState();
                _cityFromSearchString = getCity(state, _command.getSearchString());
                _hasAlreadyLookedForCityInSearchString = true;
            }
        }
        return _cityFromSearchString;
    }

    public City getCity() {
        if (isDistrictBrowse() || isCityBrowse()) {
            return getCityFromUrl();
        } else {
            return getCityFromSearchString();
        }
    }

    protected City getCity(State state, String cityName) {
        City city = null;
        if (StringUtils.isNotBlank(cityName) && state != null) {
            // might be null
            city = getGeoDao().findCity(state, cityName);
        }
        return city;
    }

    public State getState() {
        State state = null;

        // copied over from code near the top of SchoolSearchController.handle()
        if (isNearbySearch()) {
            if (_nearbySearchInfo != null && _nearbySearchInfo.get("state") != null && _nearbySearchInfo.get("state") instanceof State) {
                state = (State) _nearbySearchInfo.get("state");
                return state; // early exit
            }
        }

        if (_command != null && _command.getState() != null) {
            try {
                state = State.fromString(_command.getState());
            } catch (IllegalArgumentException iae) {
                // invalid state, use default
                // TODO-11405 - get rid of CA as default state, allow null state
                //state = State.CA;
            }
        } else if (_fields != null && _fields.getState() != null) {
            state = _fields.getState();
        } else {
            // TODO-11405 - get rid of CA as default state, allow null state
            //state = State.CA;
        }
        return state;
    }

    /**
     * Returns the latitude from the command if already exists, otherwise tries to find a latitude in
     * a district or city object if one exists.
     * @return
     */
    public Float getLatitude() {
        Float latitude = null;
        if (_command.getLat() != null && _command.getLat() != null) {
            latitude = _command.getLat().floatValue();
        } else if (_district != null && _district.getLat() != null) {
            latitude = _district.getLat();
        } else if (_cityFromUrl != null && _cityFromUrl.getLat() != 0f) {
            latitude = _cityFromUrl.getLat();
        } else if (_cityFromSearchString != null && _cityFromSearchString.getLat() != 0f) {
            latitude = _cityFromSearchString.getLat();
        }
        return latitude;
    }

    /**
     * Returns the longitude from the command if already exists, otherwise tries to find a longitude in
     * a district or city object if one exists.
     * @return
     */
    public Float getLongitude() {
        Float longitude = null;
        if (_command.getLon() != null && _command.getLon() != null) {
            longitude = _command.getLon().floatValue();
        } else if (_district != null && _district.getLon() != null) {
            longitude = _district.getLon();
        } else if (_cityFromUrl != null && _cityFromUrl.getLon() != 0f) {
            longitude = _cityFromUrl.getLon();
        } else if (_cityFromSearchString != null && _cityFromSearchString.getLon() != 0f) {
            longitude = _cityFromSearchString.getLon();
        }
        return longitude;
    }

    public FieldSort getFieldSort() {
        boolean sortChanged = _command.isSortChanged();
        FieldSort sort = _command.getSortBy() == null ? ((isCityBrowse() || isDistrictBrowse()) && !sortChanged ? FieldSort.GS_RATING_DESCENDING : null) : FieldSort.valueOf(_command.getSortBy().toUpperCase());

        // TODO: find better place to do this. Just copied over existing logic as-is for now. See line 293 in SchoolSearchController
        if (sort != null) {
            _command.setSortBy(sort.name());
        } else {
            _command.setSortBy(null);
        }
        return sort;
    }

    public String getSearchString() {
        return _command.getSearchString();
    }

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public String[] getAffiliations() {
        return _command.getAffiliations();
    }

    public boolean hasAffiliations() {
        return _command.hasAffiliations();
    }

    public String getStudentTeacherRatio() {
        return _command.getStudentTeacherRatio();
    }

    public String getSchoolSize() {
        return _command.getSchoolSize();
    }

    public SchoolSearchCommand getSchoolSearchCommand() {
        return _command;
    }

    public Map getNearbySearchInfo() {
        return _nearbySearchInfo;
    }
}
