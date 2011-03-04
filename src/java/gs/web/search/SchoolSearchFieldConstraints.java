package gs.web.search;

import gs.data.search.indexers.documentBuilders.SchoolDocumentBuilder;

public enum SchoolSearchFieldConstraints implements IFieldConstraint {
    DISTRICT_ID(SchoolDocumentBuilder.SCHOOL_DISTRICT_ID),
    CITY(SchoolDocumentBuilder.ADDRESS_CITY),
    STATE(SchoolDocumentBuilder.ADDRESS_STATE);

    private String _fieldName;

    SchoolSearchFieldConstraints(String fieldName) {
        _fieldName = fieldName;
    }

    public String getFieldName() {
        return _fieldName;
    }

    public void setFieldName(String fieldName) {
        _fieldName = fieldName;
    }
}
