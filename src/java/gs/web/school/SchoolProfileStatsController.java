package gs.web.school;


import gs.data.school.EspResponse;
import gs.data.school.School;
import gs.data.school.census.*;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@Controller
@RequestMapping("/school/profileStats.page")
public class SchoolProfileStatsController extends AbstractSchoolProfileController implements ReadWriteAnnotationController {

    @Autowired
    ICensusDataConfigEntryDao _censusStateConfigDao;

    ICensusDataSetDao _censusDataSetDao;

    @Autowired
    ICensusDataSchoolValueDao _censusDataSchoolValueDao;

    @Autowired
    ICensusDataDistrictValueDao _censusDataDistrictValueDao;

    @Autowired
    ICensusDataStateValueDao _censusDataStateValueDao;

    @Autowired
    SchoolProfileDataHelper _schoolProfileDataHelper;

    @Autowired
    SchoolProfileCensusHelper _schoolProfileCensusHelper;


    @Autowired
    ICensusCacheDao _censusCacheDao;

    Logger _log = Logger.getLogger(SchoolProfileStatsController.class);

    @RequestMapping(method= RequestMethod.GET)
    public Map<String,Object> handle(HttpServletRequest request,
                                     HttpServletResponse response
    ) {
        Long start = System.nanoTime();
        Map<String,Object> model = new HashMap<String,Object>();

        School school = getSchool(request);

        Map<String,Object> statsModel = null;
        statsModel = _censusCacheDao.getMapForSchool(school);

        if (statsModel == null) {
            statsModel = new HashMap<String,Object>();

            // Census Data Set ID --> Source
            Map<Integer, CensusDescription> dataTypeSourceMap = new HashMap<Integer,CensusDescription>();

            // Source --> source position (footnote)
            /*Map<CensusDescription, Integer> sourceFootnotes = getSourceFootnotes(dataTypeSourceMap);*/
            CensusDataHolder groupedCensusDataSets = _schoolProfileCensusHelper.getGroupedCensusDataSets(request);

            // make the census data holder load school data onto the census data sets
            groupedCensusDataSets.retrieveDataSetsAndSchoolData();

            // make the census data holder load district data onto the census data sets
            groupedCensusDataSets.retrieveDataSetsAndDistrictData();

            // make the census data holder load state data onto the census data sets
            groupedCensusDataSets.retrieveDataSetsAndStateData();

            // group ID --> List of StatsRow
            Map<Long, List<SchoolProfileStatsDisplayRow>> groupIdToStatsRows =
                    buildDisplayRows(_schoolProfileCensusHelper.getCensusStateConfig(request), groupedCensusDataSets);


            statsModel.put("dataTypeSourceMap", dataTypeSourceMap);
            statsModel.put("censusStateConfig", _schoolProfileCensusHelper.getCensusStateConfig(request));
            statsModel.put("statsRows", groupIdToStatsRows);

            Map<String,String> ethnicityMap = _schoolProfileCensusHelper.getEthnicityLabelValueMap(request);
            model.put("ethnicityMap", ethnicityMap);

            cacheStatsModel(statsModel, school);
        }

        Map<String, List<EspResponse>> espResults = _schoolProfileDataHelper.getEspDataForSchool(request);
        statsModel.put("espResults", espResults);


        model.putAll(statsModel);
        return model;
    }

    public void cacheStatsModel(Map<String,Object> statsModel, School school) {
        try {
            _censusCacheDao.save(school, statsModel);
        } catch (IOException e) {
            _log.debug("Error while attempting to cache stats model. ", e);
            // all is lost. don't cache
        }
    }

    // group ID --> Stats Row
    public Map<Long,List<SchoolProfileStatsDisplayRow>> buildDisplayRows(CensusStateConfig config,
        CensusDataHolder groupedCensusDataSets
    ) {

        Map<Integer, CensusDataSet> censusDataSets = groupedCensusDataSets.getAllCensusDataSets();

        Map<Long,List<SchoolProfileStatsDisplayRow>> statsRowMap = new HashMap<Long,List<SchoolProfileStatsDisplayRow>>();

        for (Map.Entry<Integer,CensusDataSet> entry : censusDataSets.entrySet()) {

            Set<Integer> configuredDataTypeIds = config.allDataTypeIds();
            CensusDataSet censusDataSet = entry.getValue();
            Integer censusDataSetId = entry.getKey();
            Integer dataTypeId = censusDataSet.getDataType().getId();
            CensusDataType dataTypeEnum = CensusDataType.getEnum(dataTypeId);
            Integer breakdownId = null;
            Breakdown breakdown = censusDataSet.getBreakdownOnly();
            if (breakdown != null) {
                breakdownId = breakdown.getId();
            }

            // DataType enum gives us an int
            Integer groupIdInt = config.getDataTypeToGroupIdMap().get(censusDataSet.getDataType().getId());
            Long groupId = null;
            // But sometimes we store groupIds as longs, because JSTL converts number literals to longs
            if (groupIdInt != null) {
                groupId = groupIdInt.longValue();
            }

            // look to see if there's a data type label "override" in the config entry for this data type
            // if not, just use the default data type label/description
            String label = null;


            // if this method was provided with more CensusDataSets then what was configured for the stats page,
            // skip over them here
            if (!configuredDataTypeIds.contains(censusDataSet.getDataType().getId())) {
                continue;
            }

            // Get all the census data config entries for data type
            List<ICensusDataConfigEntry> censusDataConfigEntries = config.getStateConfigEntryMap().get(dataTypeId);


            // If there are more than one config entry per data type, then there should be
            // a config entry for each breakdown within the data type
            if (censusDataConfigEntries.size() > 1) {
                // Find the entry that cooresponds with the data type and breakdown on this data set.
                // If none exist, skip this data set
                ICensusDataConfigEntry configEntry = config.getEntry(dataTypeId, breakdownId);

                // If config doesnt exist for data type + breakdown, skip this data set
                if (configEntry == null) {
                    continue;
                } else {
                    // use the label specified in the census config entry
                    label = configEntry.getDataTypeLabel();
                    if (label == null) {
                        // no entry was specified; if we have an ethnicity, use the ethnicity name. otherwise use data type description
                        if (breakdown != null && breakdown.getEthnicity() != null) {
                            label = breakdown.getEthnicity().getName();
                        } else {
                            label = configEntry.getDataType().getDescription();
                        }
                    }
                };
            } else {
                // there's only one config entry for this one data type
                ICensusDataConfigEntry configEntry = censusDataConfigEntries.get(0);

                // If breakdown is set, but there was only one census config entry, this means that
                // we're by default supposed to display all breakdowns available
                if (breakdown != null && breakdown.getEthnicity() != null) {
                    // set label to the breakdown's ethnicity name
                    label = breakdown.getEthnicity().getName();
                } else if (configEntry.getDataTypeLabel() != null) {
                    // no breakdown available, but label was set on the config entry, so use that
                    label = configEntry.getDataTypeLabel();
                } else {
                    // no labels specified; use data type's description
                    label = configEntry.getDataType().getDescription();
                }
            }

            String schoolValue = "";
            SchoolCensusValue schoolCensusValue = censusDataSet.getTheOnlySchoolValue();
            if (schoolCensusValue != null) {
                if (schoolCensusValue.getValueFloat() != null) {
                    schoolValue = formatValueAsString(schoolCensusValue.getValueFloat(), dataTypeEnum.getValueType());
                } else {
                    schoolValue = String.valueOf(schoolCensusValue.getValueText());
                }
            }

            String districtValue = "";
            DistrictCensusValue districtCensusValue = censusDataSet.getTheOnlyDistrictValue();
            if (districtCensusValue != null) {
                if (districtCensusValue.getValueFloat() != null) {
                    districtValue = formatValueAsString(districtCensusValue.getValueFloat(), dataTypeEnum.getValueType());
                } else {
                    districtValue = String.valueOf(districtCensusValue.getValueText());
                }
            }


            String stateValue = "";
            StateCensusValue stateCensusValue = censusDataSet.getStateCensusValue();
            if (stateCensusValue != null) {
                if (stateCensusValue.getValueFloat() != null) {
                    stateValue = formatValueAsString(stateCensusValue.getValueFloat(), dataTypeEnum.getValueType());
                } else {
                    stateValue = String.valueOf(stateCensusValue.getValueText());
                }
            }

            Set<CensusDescription> source = entry.getValue().getCensusDescription();

            // filter out rows where school, district, and state values are all N/A
            if (censusValueNotEmpty(schoolValue) || censusValueNotEmpty(districtValue) || censusValueNotEmpty(stateValue) ) {

                SchoolProfileStatsDisplayRow row = new SchoolProfileStatsDisplayRow(
                    groupId,
                    censusDataSetId,
                    label,
                    schoolValue,
                    districtValue,
                    stateValue,
                    source,
                    entry.getValue().getYear()
                );

                List<SchoolProfileStatsDisplayRow> statsRows = statsRowMap.get(groupId);
                if (statsRows == null) {
                    statsRows = new ArrayList<SchoolProfileStatsDisplayRow>();
                    statsRowMap.put(groupId, statsRows);
                }

                statsRows.add(row);
            }
        }


        // Sort ethnicities based on school / state value
        // TODO: move to its own method
        Long ethnicityTableGroupId = 6l;
        List<SchoolProfileStatsDisplayRow> statsRows = statsRowMap.get(ethnicityTableGroupId);
        if (statsRows != null && statsRows.size() > 1) {
            Collections.sort(statsRows, new Comparator<SchoolProfileStatsDisplayRow>() {
                public int compare(SchoolProfileStatsDisplayRow statsRow1, SchoolProfileStatsDisplayRow statsRow2) {
                    Float row1Value = formatValueAsFloat(statsRow1.getSchoolValue());
                    Float row2Value = formatValueAsFloat(statsRow2.getSchoolValue());
                    // reverse sort
                    return row2Value.compareTo(row1Value);
                }
            });
        }



        return statsRowMap;
    }

    private boolean censusValueNotEmpty(String value) {
        return !StringUtils.isEmpty(value) && !"N/A".equalsIgnoreCase(value);
    }

    private String formatValueAsString(Float value, CensusDataType.ValueType valueType) {
        String result;
        if (CensusDataType.ValueType.PERCENT.equals(valueType)) {
            result = String.valueOf(Math.round(value)) + "%";
        } else if (CensusDataType.ValueType.MONETARY.equals(valueType)) {
            result = "$" + String.valueOf(value);
        } else {
            result = String.valueOf(Math.round(value));
        }

        return result;
    }

    private Float formatValueAsFloat(String value) {
        Float result = 0f;

        if (StringUtils.isBlank(value)) {
            return result;
        }

        value = value.replaceAll("[^0-9.]", "");

        try {
            result = new Float(value);
        } catch (NumberFormatException e) {
            _log.debug("Could not format " + value + " to float.", e);
        }

        return result;
    }

    /**
     * Converts from map of Data Type ID --> Source
     * to
     * map of Source --> footnote number
     * @return
     */
    public Map<CensusDescription, Integer> getSourceFootnotes(Map<Integer, CensusDescription> dataTypeSourceMap) {
        Map<CensusDescription, Integer> sourceFootnoteMap = new HashMap<CensusDescription, Integer>();
        for (Map.Entry<Integer, CensusDescription> entry : dataTypeSourceMap.entrySet()) {
            if (!sourceFootnoteMap.containsKey(entry.getValue())) {
                sourceFootnoteMap.put(entry.getValue(), sourceFootnoteMap.size()+1);
            }
        }

        return sourceFootnoteMap;
    }


    public ICensusDataSetDao getCensusDataSetDao() {
        return _censusDataSetDao;
    }

    public void setCensusDataSetDao(ICensusDataSetDao censusDataSetDao) {
        _censusDataSetDao = censusDataSetDao;
    }
}
