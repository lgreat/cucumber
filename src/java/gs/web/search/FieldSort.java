package gs.web.search;

import gs.data.search.Indexer;

public enum FieldSort {
    //before solr migration
    SCHOOL_NAME_DESCENDING(Indexer.SORTABLE_NAME, true),
    SCHOOL_NAME_ASCENDING(Indexer.SORTABLE_NAME, false),
    GS_RATING_DESCENDING(Indexer.OVERALL_RATING, true),
    GS_RATING_ASCENDING(Indexer.OVERALL_RATING_SORTED_ASC, false),
    PARENT_RATING_DESCENDING(Indexer.PARENT_RATINGS_AVG_QUALITY, true),
    PARENT_RATING_ASCENDING(Indexer.COMMUNITY_RATING_SORTED_ASC, false);
    //


    //after solr migration
    /*SCHOOL_NAME_DESCENDING(SchoolIndexer.SORTABLE_NAME, true),
    SCHOOL_NAME_ASCENDING(SchoolIndexer.SORTABLE_NAME, false),
    GS_RATING_DESCENDING(SchoolIndexer.OVERALL_RATING, true),
    GS_RATING_ASCENDING(SchoolIndexer.OVERALL_RATING_SORTED_ASC, false),
    PARENT_RATING_DESCENDING(SchoolIndexer.PARENT_RATINGS_AVG_QUALITY, true),
    PARENT_RATING_ASCENDING(SchoolIndexer.COMMUNITY_RATING_SORTED_ASC, false);*/
    //

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
