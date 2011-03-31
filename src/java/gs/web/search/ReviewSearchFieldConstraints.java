package gs.web.search;

import gs.data.search.IFieldConstraint;
import gs.data.search.fields.ReviewFields;

public enum ReviewSearchFieldConstraints implements IFieldConstraint {
    SCHOOL_ID(ReviewFields.REVIEW_SCHOOL_ID.getName()),
    SCHOOL_STATE(ReviewFields.REVIEW_SCHOOL_DATABASE_STATE.getName());

    private String _fieldName;

    ReviewSearchFieldConstraints(String fieldName) {
        _fieldName = fieldName;
    }

    public String getFieldName() {
        return _fieldName;
    }

    public void setFieldName(String fieldName) {
        _fieldName = fieldName;
    }
}
