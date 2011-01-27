package gs.web.search;

import gs.data.search.IndexField;
import gs.data.search.Indexer;

public enum FieldConstraint {
    //before solr migration
    DISTRICT_ID(IndexField.DISTRICT),
    CITY(Indexer.CITY_KEYWORD),
    STATE(Indexer.STATE);
    //


    //after solr migration
    /*DISTRICT_ID(SchoolDocumentBuilder.DISTRICT),
    CITY(SchoolDocumentBuilder.ADDRESS_CITY_KEYWORD),
    STATE(SchoolDocumentBuilder.PHYSICAL_STATE);*/
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
