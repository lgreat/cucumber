package gs.web.search;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.LevelCode;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.web.path.DirectoryStructureUrlFields;
import org.apache.commons.lang.StringUtils;

class SchoolSearchCommandWithFields {
    
    private final SchoolSearchCommand _command;
    private final DirectoryStructureUrlFields _fields;

    private District _district;
    private City _cityFromUrl;
    private City _cityFromSearchString;
    private IDistrictDao _districtDao;
    private IGeoDao _geoDao;

    public SchoolSearchCommandWithFields(SchoolSearchCommand command, DirectoryStructureUrlFields fields) {
        _command = command;
        _fields = fields;
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

    public City getCityFromUrl() {
        if (_cityFromUrl == null) {
            State state = _fields.getState();
            String cityName = _fields.getCityName();
            if (StringUtils.isNotBlank(cityName) && state != null) {
                _cityFromUrl = getCity(state, cityName);
            }
        }
        return _cityFromUrl;
    }

    public City getCityFromSearchString() {
        if (_cityFromSearchString == null) {
            if (_command != null && _command.getSearchString() != null) {
                State state = getState();
                _cityFromSearchString = getCity(state, _command.getSearchString());
            }
        }
        return _cityFromSearchString;
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
}
