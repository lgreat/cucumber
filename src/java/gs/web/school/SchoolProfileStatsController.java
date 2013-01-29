package gs.web.school;


import gs.data.admin.IPropertyDao;
import gs.data.school.EspResponse;
import gs.data.school.Grades;
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

    @Autowired
    IPropertyDao _propertyDao;

    Logger _log = Logger.getLogger(SchoolProfileStatsController.class);

    @RequestMapping(method= RequestMethod.GET)
    public Map<String,Object> handle(HttpServletRequest request,
                                     HttpServletResponse response
    ) {
        Long start = System.nanoTime();
        Map<String,Object> model = new HashMap<String,Object>();

        School school = getSchool(request);

        Map<String,Object> statsModel = null;

        String prop = _propertyDao.getProperty(IPropertyDao.CENSUS_CACHE_ENABLED_KEY);
        boolean censusCacheEnabled = "true".equalsIgnoreCase(prop);
        if (censusCacheEnabled) {
            statsModel = _censusCacheDao.getMapForSchool(school);
        }

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

            statsModel.put("footnotesMap", getFootnotesMap(groupIdToStatsRows));

            statsModel.put("dataTypeSourceMap", dataTypeSourceMap);
            statsModel.put("censusStateConfig", _schoolProfileCensusHelper.getCensusStateConfig(request));
            statsModel.put("statsRows", groupIdToStatsRows);

            Map<String,String> ethnicityMap = _schoolProfileCensusHelper.getEthnicityLabelValueMap(request);
            statsModel.put("ethnicityMap", ethnicityMap);

            if (censusCacheEnabled) {
                cacheStatsModel(statsModel, school);
            }
        }

        Map<String, List<EspResponse>> espResults = _schoolProfileDataHelper.getEspDataForSchool(request);
        statsModel.put("espResults", espResults);


        model.putAll(statsModel);
        return model;
    }

    /**
     * Pre-calculate all footnotes
     */
    protected Map<Long, SchoolProfileCensusSourceHelper> getFootnotesMap(Map<Long, List<SchoolProfileStatsDisplayRow>> groupIdToStatsRows) {
        Map<Long, SchoolProfileCensusSourceHelper> rval = new HashMap<Long, SchoolProfileCensusSourceHelper>();
        if (groupIdToStatsRows == null) {
            return rval;
        }
        for (Long groupId: groupIdToStatsRows.keySet()) {
            SchoolProfileCensusSourceHelper sourceHelper = new SchoolProfileCensusSourceHelper();
            for (SchoolProfileStatsDisplayRow row: groupIdToStatsRows.get(groupId)) {
                sourceHelper.recordSource(row);
            }
            rval.put(groupId, sourceHelper);
        }
        return rval;
    }

    public void cacheStatsModel(Map<String,Object> statsModel, School school) {
        try {
            _censusCacheDao.insert(school, statsModel);
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
            Grades grades = censusDataSet.getGradeLevels();

            // if this dataset has year zero, it's an override dataset, and it's school value should have been assigned
            // to the companion dataset that doesn't have year zero. If a non-year-zero dataset didn't exist, this
            // data set will have it's schoolOverrideValue set
            if (censusDataSet.getYear() == 0 && censusDataSet.getSchoolOverrideValue() == null) {
                continue;
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
                // Find the entry that cooresponds with the data type, breakdown and grade on this data set.
                // If none exist, skip this data set
                ICensusDataConfigEntry configEntry = config.getEntry(dataTypeId, breakdownId, grades);

                // If config doesnt exist for data type + breakdown, skip this data set
                if (configEntry == null) {
                    continue;
                } else {
                    // use the label specified in the census config entry
                    label = configEntry.getLabel();
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
                } else if (configEntry.getLabel() != null) {
                    // no breakdown available, but label was set on the config entry, so use that
                    label = configEntry.getLabel();
                } else {
                    // no labels specified; use data type's description
                    label = configEntry.getDataType().getDescription();
                }
            }

            SchoolCensusValue schoolCensusValue = null;
            if (censusDataSet.getSchoolOverrideValue() != null) {
                schoolCensusValue = censusDataSet.getSchoolOverrideValue();
            } else {
                schoolCensusValue = censusDataSet.getTheOnlySchoolValue();
            }
            DistrictCensusValue districtCensusValue = censusDataSet.getTheOnlyDistrictValue();
            StateCensusValue stateCensusValue = censusDataSet.getStateCensusValue();

            Set<CensusDescription> source = censusDataSet.getCensusDescription();

            SchoolProfileStatsDisplayRow row = new SchoolProfileStatsDisplayRow(
                groupId,
                dataTypeId,
                censusDataSetId,
                label,
                schoolCensusValue,
                districtCensusValue,
                stateCensusValue,
                source,
                entry.getValue().getYear(),
                censusDataSet.getSchoolOverrideValue() != null
            );

            // filter out rows where school and district values are N/A
            boolean showRow;
            if (dataTypeId == 9) {
                showRow = (censusValueNotEmpty(row.getSchoolValue()) || censusValueNotEmpty(row.getDistrictValue()) || censusValueNotEmpty(row.getStateValue()));
            } else {
                showRow = (censusValueNotEmpty(row.getSchoolValue()) || censusValueNotEmpty(row.getDistrictValue()));
            }
            if (showRow) {
                List<SchoolProfileStatsDisplayRow> statsRows = statsRowMap.get(groupId);
                if (statsRows == null) {
                    statsRows = new ArrayList<SchoolProfileStatsDisplayRow>();
                    statsRowMap.put(groupId, statsRows);
                }

                statsRows.add(row);
            }
        }


        // Sort ethnicities based on school / state value
        Long ethnicityTableGroupId = 6l;
        sortEthnicityValues(statsRowMap.get(ethnicityTableGroupId));

        return statsRowMap;
    }

    protected void sortEthnicityValues(List<SchoolProfileStatsDisplayRow> statsRows) {
        if (statsRows != null && statsRows.size() > 1) {
            Collections.sort(statsRows, new Comparator<SchoolProfileStatsDisplayRow>() {
                public int compare(SchoolProfileStatsDisplayRow statsRow1, SchoolProfileStatsDisplayRow statsRow2) {
                    Float row1Value = CensusDataHelper.formatValueAsFloat(statsRow1.getSchoolValue());
                    Float row2Value = CensusDataHelper.formatValueAsFloat(statsRow2.getSchoolValue());
                    int compare = row2Value.compareTo(row1Value);
                    // reverse sort
                    if(compare == 0 && statsRow1.getText() != null && statsRow2.getText() != null) {
                        return statsRow1.getText().compareTo(statsRow2.getText());
                    }
                    return compare;
                }
            });
        }
    }

    protected boolean censusValueNotEmpty(String value) {
        return !StringUtils.isEmpty(value) && !"N/A".equalsIgnoreCase(value);
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

    public void setCensusStateConfigDao(ICensusDataConfigEntryDao censusStateConfigDao) {
        _censusStateConfigDao = censusStateConfigDao;
    }

    public void setCensusDataSchoolValueDao(ICensusDataSchoolValueDao censusDataSchoolValueDao) {
        _censusDataSchoolValueDao = censusDataSchoolValueDao;
    }

    public void setCensusDataDistrictValueDao(ICensusDataDistrictValueDao censusDataDistrictValueDao) {
        _censusDataDistrictValueDao = censusDataDistrictValueDao;
    }

    public void setCensusDataStateValueDao(ICensusDataStateValueDao censusDataStateValueDao) {
        _censusDataStateValueDao = censusDataStateValueDao;
    }

    public void setSchoolProfileDataHelper(SchoolProfileDataHelper schoolProfileDataHelper) {
        _schoolProfileDataHelper = schoolProfileDataHelper;
    }

    public void setSchoolProfileCensusHelper(SchoolProfileCensusHelper schoolProfileCensusHelper) {
        _schoolProfileCensusHelper = schoolProfileCensusHelper;
    }

    public void setCensusCacheDao(ICensusCacheDao censusCacheDao) {
        _censusCacheDao = censusCacheDao;
    }

    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
    }
}