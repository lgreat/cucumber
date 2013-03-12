package gs.web.school.census;


import gs.data.school.census.CensusDescription;
import gs.web.school.SchoolProfileStatsDisplayRow;

import java.util.Set;

public interface SchoolProfileStatsDisplayRowFluentInterface {

    public SchoolProfileStatsDisplayRowFluentInterface groupId(Long groupId);
    public SchoolProfileStatsDisplayRowFluentInterface dataTypeId(Integer dataTypeId);
    public SchoolProfileStatsDisplayRowFluentInterface censusDataSetId(Integer censusDataSetId);
    public SchoolProfileStatsDisplayRowFluentInterface text(String text);
    public SchoolProfileStatsDisplayRowFluentInterface schoolValue(String schoolValue);
    public SchoolProfileStatsDisplayRowFluentInterface districtValue(String districtValue);
    public SchoolProfileStatsDisplayRowFluentInterface stateValue(String stateValue);
    public SchoolProfileStatsDisplayRowFluentInterface censusDescriptions(Set<CensusDescription> censusDescription);
    public SchoolProfileStatsDisplayRowFluentInterface year(Integer year);
    public SchoolProfileStatsDisplayRowFluentInterface manualOverride(boolean manualOverride);
    public SchoolProfileStatsDisplayRowFluentInterface sort(Integer sort);

    public SchoolProfileStatsDisplayRow create();
}
