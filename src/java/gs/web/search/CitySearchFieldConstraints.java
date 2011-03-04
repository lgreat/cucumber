package gs.web.search;

import gs.data.search.indexers.documentBuilders.CityDocumentBuilder;

public enum CitySearchFieldConstraints implements IFieldConstraint {

    STATE(CityDocumentBuilder.STATE);

    private String _fieldName;

    CitySearchFieldConstraints(String fieldName) {
        _fieldName = fieldName;
    }

    public String getFieldName() {
        return _fieldName;
    }

    public void setFieldName(String fieldName) {
        _fieldName = fieldName;
    }
}
