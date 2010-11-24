package gs.web.search;

import gs.data.search.Indexer;

public enum FieldSort {
    SCHOOL_NAME_DESCENDING(Indexer.SORTABLE_NAME, true),
    SCHOOL_NAME_ASCENDING(Indexer.SORTABLE_NAME, false),
    GS_RATING_DESCENDING(Indexer.OVERALL_RATING, true),
    GS_RATING_ASCENDING(Indexer.OVERALL_RATING, false),
    PARENT_RATING_DESCENDING(Indexer.PARENT_RATINGS_AVG_QUALITY, true),
    PARENT_RATING_ASCENDING(Indexer.PARENT_RATINGS_AVG_QUALITY, false);

    private String _field;
    private boolean _descending;

    FieldSort(String field, boolean descending) {
        _field = field;
        _descending = descending;
    }

    public String getField() {
        return _field;
    }

    public void setField(String field) {
        _field = field;
    }

    public boolean isDescending() {
        return _descending;
    }

    public void setDescending(boolean descending) {
        _descending = descending;
    }
}
