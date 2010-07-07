package gs.web.widget;

import gs.data.school.SchoolWithRatings;
import gs.data.geo.City;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class SchoolSearchWidgetCommand {
    private String _displayTab = "search";
    private String _searchQuery = "Enter city & state or zip code";
    private List<SchoolWithRatings> _schools = new ArrayList<SchoolWithRatings>();
    private City _city;
    private String _cityName;
    private String _state;
    private String _normalizedAddress;
    private String _mapLocationPrefix;
    private String _mapLocationString;
    private String _mapLocationSuffix;
    private boolean _hidePreschools = false;
    private boolean _preschoolFilterChecked = true;
    private boolean _elementaryFilterChecked = true;
    private boolean _middleFilterChecked = true;
    private boolean _highFilterChecked = true;
    private int _width = 290;
    private int _height = 346;
    private String _textColor = "0066B8";
    private String _bordersColor = "FFCC66";
    private float _lat;
    private float _lon;
    private float _locationMarkerLat;
    private float _locationMarkerLon;
    private boolean _showLocationMarker;
    private int _zoom;
    private String _cobrandHostname;

    public String getDisplayTab() {
        return _displayTab;
    }

    public void setDisplayTab(String displayTab) {
        _displayTab = displayTab;
    }

    public String getSearchQuery() {
        return _searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        _searchQuery = searchQuery;
    }

    public List<SchoolWithRatings> getSchools() {
        return _schools;
    }

    public void setSchools(List<SchoolWithRatings> schools) {
        _schools = schools;
    }

    public City getCity() {
        return _city;
    }

    public void setCity(City city) {
        _city = city;
    }

    public String getMapLocationPrefix() {
        return _mapLocationPrefix;
    }

    public void setMapLocationPrefix(String mapLocationPrefix) {
        _mapLocationPrefix = mapLocationPrefix;
    }

    public String getMapLocationString() {
        return _mapLocationString;
    }

    public void setMapLocationString(String mapLocationString) {
        _mapLocationString = mapLocationString;
    }

    public String getMapLocationSuffix() {
        return _mapLocationSuffix;
    }

    public void setMapLocationSuffix(String mapLocationSuffix) {
        _mapLocationSuffix = mapLocationSuffix;
    }

    public boolean isHidePreschools() {
        return _hidePreschools;
    }

    public void setHidePreschools(boolean hidePreschools) {
        _hidePreschools = hidePreschools;
    }

    public boolean isPreschoolFilterChecked() {
        return !isHidePreschools() && _preschoolFilterChecked;
    }

    public void setPreschoolFilterChecked(boolean preschoolFilterChecked) {
        _preschoolFilterChecked = preschoolFilterChecked;
    }

    public boolean isElementaryFilterChecked() {
        return _elementaryFilterChecked;
    }

    public void setElementaryFilterChecked(boolean elementaryFilterChecked) {
        _elementaryFilterChecked = elementaryFilterChecked;
    }

    public boolean isMiddleFilterChecked() {
        return _middleFilterChecked;
    }

    public void setMiddleFilterChecked(boolean middleFilterChecked) {
        _middleFilterChecked = middleFilterChecked;
    }

    public boolean isHighFilterChecked() {
        return _highFilterChecked;
    }

    public void setHighFilterChecked(boolean highFilterChecked) {
        _highFilterChecked = highFilterChecked;
    }

    public int getWidth() {
        return _width;
    }

    public void setWidth(int width) {
        _width = width;
    }

    public int getHeight() {
        return _height;
    }

    public void setHeight(int height) {
        _height = height;
    }

    public String getTextColor() {
        return _textColor;
    }

    public void setTextColor(String textColor) {
        _textColor = textColor;
    }

    public String getBordersColor() {
        return _bordersColor;
    }

    public void setBordersColor(String bordersColor) {
        _bordersColor = bordersColor;
    }

    public float getLat() {
        return _lat;
    }

    public void setLat(float lat) {
        _lat = lat;
    }

    public float getLon() {
        return _lon;
    }

    public void setLon(float lon) {
        _lon = lon;
    }

    public float getLocationMarkerLat() {
        return _locationMarkerLat;
    }

    public void setLocationMarkerLat(float locationMarkerLat) {
        _locationMarkerLat = locationMarkerLat;
    }

    public float getLocationMarkerLon() {
        return _locationMarkerLon;
    }

    public void setLocationMarkerLon(float locationMarkerLon) {
        _locationMarkerLon = locationMarkerLon;
    }

    public boolean isShowLocationMarker() {
        return _showLocationMarker;
    }

    public void setShowLocationMarker(boolean showLocationMarker) {
        _showLocationMarker = showLocationMarker;
    }

    public int getZoom() {
        return _zoom;
    }

    public void setZoom(int zoom) {
        _zoom = zoom;
    }

    public String getCobrandHostname() {
        return _cobrandHostname;
    }

    public void setCobrandHostname(String cobrandHostname) {
        _cobrandHostname = cobrandHostname;
    }

    public String getCityName() {
        return _cityName;
    }

    public void setCityName(String cityName) {
        _cityName = cityName;
    }

    public String getState() {
        return _state;
    }

    public void setState(String state) {
        _state = state;
    }

    public String getNormalizedAddress() {
        return _normalizedAddress;
    }

    public void setNormalizedAddress(String normalizedAddress) {
        _normalizedAddress = normalizedAddress;
    }

    public String getLevelCodeString() {
        String rval = "";
        if (isPreschoolFilterChecked()) {
            rval += "p";
        }
        if (isElementaryFilterChecked()) {
            rval += (rval.length() > 0)?",e":"e";
        }
        if (isMiddleFilterChecked()) {
            rval += (rval.length() > 0)?",m":"m";
        }
        if (isHighFilterChecked()) {
            rval += (rval.length() > 0)?",h":"h";
        }

        return rval;
    }
}
