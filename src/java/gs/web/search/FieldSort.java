package gs.web.search;

import gs.data.search.indexers.documentBuilders.SchoolDocumentBuilder;

public enum FieldSort {
    //before solr migration
    /*SCHOOL_NAME_DESCENDING(Indexer.SORTABLE_NAME, true),
    SCHOOL_NAME_ASCENDING(Indexer.SORTABLE_NAME, false),
    GS_RATING_DESCENDING(Indexer.OVERALL_RATING, true),
    GS_RATING_ASCENDING(Indexer.OVERALL_RATING_SORTED_ASC, false),
    PARENT_RATING_DESCENDING(Indexer.PARENT_RATINGS_AVG_QUALITY, true),
    PARENT_RATING_ASCENDING(Indexer.COMMUNITY_RATING_SORTED_ASC, false);*/
    //

    //after solr migration
    SCHOOL_NAME_DESCENDING(SchoolDocumentBuilder.SCHOOL_SORTABLE_NAME, true),
    SCHOOL_NAME_ASCENDING(SchoolDocumentBuilder.SCHOOL_SORTABLE_NAME, false),
    GS_RATING_DESCENDING(SchoolDocumentBuilder.OVERALL_GS_RATING, true),
    GS_RATING_ASCENDING(SchoolDocumentBuilder.OVERALL_GS_RATING_SORTED_ASC, false),
    PARENT_RATING_DESCENDING(SchoolDocumentBuilder.COMMUNITY_RATING, true),
    PARENT_RATING_ASCENDING(SchoolDocumentBuilder.COMMUNITY_RATING, false),
    DISTANCE("distance",false);
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
