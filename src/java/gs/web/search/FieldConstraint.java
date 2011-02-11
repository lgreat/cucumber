package gs.web.search;

import gs.data.search.indexers.documentBuilders.SchoolDocumentBuilder;

public enum FieldConstraint {
    //before solr migration
    /*DISTRICT_ID(IndexField.DISTRICT),
    CITY(Indexer.CITY_KEYWORD),
    STATE(Indexer.STATE);*/
    //

    //after solr migration
    DISTRICT_ID(SchoolDocumentBuilder.SCHOOL_DISTRICT_ID),
    CITY(SchoolDocumentBuilder.ADDRESS_CITY),
    STATE(SchoolDocumentBuilder.ADDRESS_STATE);
    //

    private String _fieldName;
    FieldConstraint(String fieldName) {
        _fieldName = fieldName;
    }

    public String getFieldName() {
        return _fieldName;
    }

    public void setFieldName(String fieldName) {
        _fieldName = fieldName;
    }
}
