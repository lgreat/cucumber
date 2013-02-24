package gs.web.school;

import gs.data.school.census.*;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Set;

public class SchoolProfileStatsDisplayRow implements Serializable {
    private Long _groupId;
    private Integer _dataTypeId;
    private Integer _censusDataSetId;
    private String _text;


    private String _schoolValue;
    private String _districtValue;
    private String _stateValue;

    private Set<CensusDescription> _censusDescriptions;
    private Integer _year;
    private boolean _manualOverride;

    private Integer _sort;


    public SchoolProfileStatsDisplayRow(Long groupId, Integer dataTypeId, Integer censusDataSetId, String text, SchoolCensusValue schoolValue, DistrictCensusValue districtValue, StateCensusValue stateCensusValue, Set<CensusDescription> censusDescriptions, Integer year, boolean manualOverride, Integer sort) {
        CensusDataType dataTypeEnum = CensusDataType.getEnum(dataTypeId);
        _groupId = groupId;
        _dataTypeId = dataTypeId;
        _censusDataSetId = censusDataSetId;
        _text = text;

        _schoolValue = getStringValue(schoolValue, dataTypeEnum);
        _districtValue = getStringValue(districtValue, dataTypeEnum);
        _stateValue = getStringValue(stateCensusValue, dataTypeEnum);


        _censusDescriptions = censusDescriptions;
        _year = year;
        _manualOverride = manualOverride;

        _sort = sort;
    }

    public Long getGroupId() {
        return _groupId;
    }

    public String getText() {
        return _text;
    }

    public String getSchoolValue() {
        return _schoolValue;
    }

    public String getDistrictValue() {
        return _districtValue;
    }

    public String getStateValue() {
        return _stateValue;
    }

    public static String getStringValue(AbstractCensusValue censusValue, CensusDataType dataTypeEnum) {
        String value = "";

        if (censusValue != null) {
            if (censusValue.getValueFloat() != null) {
                value = CensusDataHelper.formatValueAsString(censusValue.getValueFloat(), dataTypeEnum.getValueType());
            } else {
                value = String.valueOf(censusValue.getValueText());
            }
        }

        return value;
    }

    protected static boolean censusValueNotEmpty(String value) {
        return !StringUtils.isEmpty(value) && !"N/A".equalsIgnoreCase(value);
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

    public Integer getSort() {
        return _sort;
    }

    public Integer getDataTypeId() {
        return _dataTypeId;
    }
}