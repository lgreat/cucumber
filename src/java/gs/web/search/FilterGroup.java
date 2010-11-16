package gs.web.search;

public class FilterGroup {
    private FieldFilter[] _fieldFilters;

    public FieldFilter[] getFieldFilters() {
        return _fieldFilters;
    }

    public void setFieldFilters(FieldFilter[] fieldFilters) {
        _fieldFilters = fieldFilters;
    }
}
