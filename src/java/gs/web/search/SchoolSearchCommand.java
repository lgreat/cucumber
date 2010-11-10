package gs.web.search;

public class SchoolSearchCommand {

    private String _searchString;
    private String _state;
    private String[] _schoolTypes;
    private String _format = "html";
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
    private Integer _pageSize;

    public SchoolSearchCommand() {
        _pageSize = 20;
    }

    public void setQ(String q) {
        setSearchString(q);
    }

    public String getSearchString() {
        return _searchString;
    }

    public void setSearchString(String searchString) {
        _searchString = searchString;
    }

    public String getState() {
        return _state;
    }

    public void setState(String state) {
        _state = state;
    }

    public String[] getSchoolTypes() {
        return _schoolTypes;
    }

    public void setSchoolTypes(String[] schoolTypes) {
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

    public Integer getPageSize() {
        return _pageSize;
    }

    public void setPageSize(Integer pageSize) {
        _pageSize = pageSize;
    }

    public String getFormat() {
        return _format;
    }

    public void setFormat(String format) {
        _format = format;
    }

    public boolean isJsonFormat() {
        return "json".equals(_format);
    }
}
