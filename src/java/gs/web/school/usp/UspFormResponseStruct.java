package gs.web.school.usp;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 5/1/13
 * Time: 9:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class UspFormResponseStruct {
    /**
     * Form section object
     */
    String _fieldName;
    String _title;
    String _ghostText;
    boolean _isSchoolAdmin;
    boolean _hasNoneField;
    boolean _hasOtherField;
    boolean _isNoneChecked;
    boolean _isOtherChecked;
    String _otherTextValue;
    List<SectionResponse> _SectionResponses = new LinkedList<SectionResponse>();

    public String getTitle() {
        return _title;
    }

    public void setTitle(String _title) {
        this._title = _title;
    }

    public String getGhostText() {
        return _ghostText;
    }

    public void setGhostText(String _ghostText) {
        this._ghostText = _ghostText;
    }

    public boolean isSchoolAdmin() {
        return _isSchoolAdmin;
    }

    public void setIsSchoolAdmin(boolean _isSchoolAdmin) {
        this._isSchoolAdmin = _isSchoolAdmin;
    }

    public boolean getHasNoneField() {
        return _hasNoneField;
    }

    public void setHasNoneField(boolean _hasNoneField) {
        this._hasNoneField = _hasNoneField;
    }

    public boolean getHasOtherField() {
        return _hasOtherField;
    }

    public void setHasOtherField(boolean _hasOtherField) {
        this._hasOtherField = _hasOtherField;
    }

    public boolean isNoneChecked() {
        return _isNoneChecked;
    }

    public void setIsNoneChecked(boolean _isNoneChecked) {
        this._isNoneChecked = _isNoneChecked;
    }

    public boolean isOtherChecked() {
        return _isOtherChecked;
    }

    public void setIsOtherChecked(boolean _isOtherChecked) {
        this._isOtherChecked = _isOtherChecked;
    }

    public String getOtherTextValue() {
        return _otherTextValue;
    }

    public void setOtherTextValue(String _otherTextValue) {
        this._otherTextValue = _otherTextValue;
    }

    public List<SectionResponse> getSectionResponses() {
        return _SectionResponses;
    }

    public void setSectionResponses(List<SectionResponse> _SectionResponses) {
        this._SectionResponses = _SectionResponses;
    }

    public String getFieldName() {
        return _fieldName;
    }

    public void setFieldName(String _fieldName) {
        this._fieldName = _fieldName;
    }

    /**
     *  Section response key object - one or more for a section (Subsection if more than one).
     *  Title is null if only one response key for the form section.
     */
    protected class SectionResponse {
        private String _title;
        private String _responseKey;
        private List<UspResponseValueStruct> _responses = new LinkedList<UspResponseValueStruct>();

        public String getTitle() {
            return _title;
        }

        public void setTitle(String _title) {
            this._title = _title;
        }

        public List<UspResponseValueStruct> getResponses() {
            return _responses;
        }

        public void setResponses(List<UspResponseValueStruct> _responses) {
            this._responses = _responses;
        }

        public SectionResponse(String responseKey) {
            _responseKey = responseKey;
        }

        public String getResponseKey() {
            return _responseKey;
        }

        public void setResponseKey(String _responseKey) {
            this._responseKey = _responseKey;
        }

        /**
         * Response values for section/subsection response keys
         */
        protected class UspResponseValueStruct {
            private String _label;
            private String _responseValue;
            private boolean _isSelected;

            public String getLabel() {
                return _label;
            }

            public void setLabel(String _label) {
                this._label = _label;
            }

            public String getResponseValue() {
                return _responseValue;
            }

            public void setResponseValue(String _responseValue) {
                this._responseValue = _responseValue;
            }

            public boolean isSelected() {
                return _isSelected;
            }

            public void setIsSelected(boolean _isSelected) {
                this._isSelected = _isSelected;
            }

            public UspResponseValueStruct (String responseValue) {
                _responseValue = responseValue;
                _isSelected = false;
            }
        }
    }

    /**
     * Form section must have name and label
     * @param fieldName
     * @param title
     */
    public UspFormResponseStruct(String fieldName, String title) {
        _fieldName = fieldName;
        _title = title;
    }
}
