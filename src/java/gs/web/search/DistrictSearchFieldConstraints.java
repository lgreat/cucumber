package gs.web.search;

import gs.data.search.indexers.documentBuilders.DistrictDocumentBuilder;

public enum DistrictSearchFieldConstraints implements IFieldConstraint {

    STATE(DistrictDocumentBuilder.ADDRESS_STATE);

    private String _fieldName;

    DistrictSearchFieldConstraints(String fieldName) {
        _fieldName = fieldName;
    }

    public String getFieldName() {
        return _fieldName;
    }

    public void setFieldName(String fieldName) {
        _fieldName = fieldName;
    }
}
