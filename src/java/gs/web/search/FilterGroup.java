package gs.web.search;

public class FilterGroup<FILTER_TYPE extends FieldFilter> {
    
    private FILTER_TYPE[] _fieldFilters;

    public FILTER_TYPE[] getFieldFilters() {
        return _fieldFilters;
    }

    public void setFieldFilters(FILTER_TYPE[] fieldFilters) {
        _fieldFilters = fieldFilters;
    }
}
