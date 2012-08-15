package gs.web.school;

import gs.data.school.census.CensusDescription;

import java.io.Serializable;
import java.util.Set;

public class SchoolProfileStatsDisplayRow implements Serializable {
    private Long _groupId;
    private Integer _censusDataSetId;
    private String _text;
    private String _schoolValue;
    private String _districtValue;
    private String _stateValue;
    private Set<CensusDescription> _censusDescriptions;
    private Integer _year;
    private boolean _manualOverride;

    public SchoolProfileStatsDisplayRow(Long groupId, Integer censusDataSetId, String text, String schoolValue, String districtValue, String stateValue, Set<CensusDescription> censusDescriptions, Integer year, boolean manualOverride) {
        _groupId = groupId;
        _censusDataSetId = censusDataSetId;
        _text = text;
        _schoolValue = schoolValue;
        _districtValue = districtValue;
        _stateValue = stateValue;
        _censusDescriptions = censusDescriptions;
        _year = year;
        _manualOverride = manualOverride;
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