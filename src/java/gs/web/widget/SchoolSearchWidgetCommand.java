package gs.web.widget;

import gs.data.school.SchoolWithRatings;
import gs.data.geo.City;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolSearchWidgetCommand {
    private String _displayTab = "search";
    private String _searchQuery = "Enter city & state or zip code";
    private List<SchoolWithRatings> _schools = new ArrayList<SchoolWithRatings>();
    private City _city;
    private String _mapLocationPrefix;
    private String _mapLocationString;
    private boolean _preschoolFilterChecked = true;
    private boolean _elementaryFilterChecked = true;
    private boolean _middleFilterChecked = true;
    private boolean _highFilterChecked = true;

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

    public boolean isPreschoolFilterChecked() {
        return _preschoolFilterChecked;
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
}
