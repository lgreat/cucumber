package gs.web.school;

import gs.data.school.census.*;

import java.io.Serializable;
import java.util.Set;

public class SchoolProfileStatsDisplayRow implements Serializable {
    private Long _groupId;
    private Integer _dataTypeId;
    private Integer _censusDataSetId;
    private String _text;

    private SchoolCensusValue _schoolCensusValue;
    private DistrictCensusValue _districtCensusValue;
    private StateCensusValue _stateCensusValue;

    private Set<CensusDescription> _censusDescriptions;
    private Integer _year;
    private boolean _manualOverride;

    private CensusDataType _dataTypeEnum;

    public SchoolProfileStatsDisplayRow(Long groupId, Integer dataTypeId, Integer censusDataSetId, String text, SchoolCensusValue schoolValue, DistrictCensusValue districtValue, StateCensusValue stateCensusValue, Set<CensusDescription> censusDescriptions, Integer year, boolean manualOverride) {
        _groupId = groupId;
        _dataTypeId = dataTypeId;
        _censusDataSetId = censusDataSetId;
        _text = text;
        _schoolCensusValue = schoolValue;
        _districtCensusValue = districtValue;
        _stateCensusValue = stateCensusValue;
        _censusDescriptions = censusDescriptions;
        _year = year;
        _manualOverride = manualOverride;

        _dataTypeEnum = CensusDataType.getEnum(_dataTypeId);
    }

    public Long getGroupId() {
        return _groupId;
    }

    public String getText() {
        return _text;
    }

    public String getSchoolValue() {
        String schoolValue = "";
        if (_schoolCensusValue != null) {
            if (_schoolCensusValue.getValueFloat() != null) {
                schoolValue = CensusDataHelper.formatValueAsString(_schoolCensusValue.getValueFloat(), _dataTypeEnum.getValueType());
            } else {
                schoolValue = String.valueOf(_schoolCensusValue.getValueText());
            }
        }
        return schoolValue;
    }

    public String getDistrictValue() {
        String value = "";

        if (_districtCensusValue != null) {
            if (_districtCensusValue.getValueFloat() != null) {
                value = CensusDataHelper.formatValueAsString(_districtCensusValue.getValueFloat(), _dataTypeEnum.getValueType());
            } else {
                value = String.valueOf(_districtCensusValue.getValueText());
            }
        }

        return value;
    }

    public String getStateValue() {
        String value = "";

        if (_stateCensusValue != null) {
            if (_stateCensusValue.getValueFloat() != null) {
                value = CensusDataHelper.formatValueAsString(_stateCensusValue.getValueFloat(), _dataTypeEnum.getValueType());
            } else {
                value = String.valueOf(_stateCensusValue.getValueText());
            }
        }

        return value;
    }

    public Float getSchoolValueFloat() {
        return _schoolCensusValue.getValueFloat();
    }

    public Float getDistrictValueFloat() {
        return _districtCensusValue.getValueFloat();
    }

    public Set<CensusDescription> getCensusDescriptions() {
        return _censusDescriptions;
    }

    public Integer getYear() {
        return _year;
    }

    public Integer getCensusDataSetId() {
        return _censusDataSetId;
    }

    public void setCensusDataSetId(Integer censusDataSetId) {
        _censusDataSetId = censusDataSetId;
    }

    public boolean isManualOverride() {
        return _manualOverride;
    }

    public void setManualOverride(boolean manualOverride) {
        _manualOverride = manualOverride;
    }
}