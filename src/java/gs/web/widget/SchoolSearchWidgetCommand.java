package gs.web.widget;

import gs.data.school.School;
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
    private String _mapLocationString;

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

    public String getMapLocationString() {
        return _mapLocationString;
    }

    public void setMapLocationString(String mapLocationString) {
        _mapLocationString = mapLocationString;
    }
}
