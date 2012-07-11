package gs.web.school;


import gs.data.school.EspResponse;
import gs.data.school.School;
import gs.data.school.census.*;
import gs.data.school.district.District;
import gs.data.state.State;
import gs.web.util.ReadWriteAnnotationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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

    @RequestMapping(method= RequestMethod.GET)
    public Map<String,Object> handle(HttpServletRequest request,
                                     HttpServletResponse response
    ) {
        Long start = System.nanoTime();
        Map<String,Object> model = new HashMap<String,Object>();

        School school = getSchool(request);

        Map<String,Object> statsModel = new HashMap<String,Object>();
        statsModel = _censusCacheDao.getMapForSchool(school);

        if (statsModel == null || statsModel.size() == 0) {

            // Census Data Set ID --> Source
            Map<Integer, CensusDescription> dataTypeSourceMap = new HashMap<Integer,CensusDescription>();

            // Source --> source position (footnote)
            /*Map<CensusDescription, Integer> sourceFootnotes = getSourceFootnotes(dataTypeSourceMap);*/
            SchoolProfileCensusHelper.GroupedCensusDataSets groupedCensusDataSets = _schoolProfileCensusHelper.getGroupedCensusDataSets(request);


            // query for school values
            // CensusDataSet ID --> CensusSchoolValue
            Map<Integer, SchoolCensusValue> schoolValueMap = _schoolProfileCensusHelper.getSchoolCensusValues(request);

            System.out.println("Getting census school values took " + (System.nanoTime()-start)/1000000 + " milliseconds");

            // query for district values
            // CensusDataSet ID --> CensusDistrictValue
            Map<Integer, DistrictCensusValue> districtValueMap = new HashMap<Integer,DistrictCensusValue>();
            if (groupedCensusDataSets._dataSetsForDistrictData.size() > 0) {
                 districtValueMap = findDistrictValues(groupedCensusDataSets._dataSetsForDistrictData, school.getDatabaseState(), school);
            }

            System.out.println("Getting census district values took " + (System.nanoTime()-start)/1000000 + " milliseconds");

            // query for state averages
            // CensusDataSet ID --> CensusStateValue
            Map<Integer, StateCensusValue> stateValueMap = new HashMap<Integer,StateCensusValue>();
            if (groupedCensusDataSets._dataSetsForStateData.size() > 0) {
                stateValueMap = findStateValues(groupedCensusDataSets._dataSetsForStateData, school.getDatabaseState());
            }

            System.out.println("Getting census state values took " + (System.nanoTime()-start)/1000000 + " milliseconds");

            // group ID --> List of StatsRow
            Map<Long, List<StatsRow>> groupIdToStatsRows = combine(
                    _schoolProfileCensusHelper.getCensusStateConfig(request),
                    _schoolProfileCensusHelper.getCensusDataSets(request),
                    schoolValueMap, districtValueMap, stateValueMap);

            System.out.println("Combining took " + (System.nanoTime()-start)/1000000 + " milliseconds");

            statsModel.put("dataTypeSourceMap", dataTypeSourceMap);
            statsModel.put("censusStateConfig", _schoolProfileCensusHelper.getCensusStateConfig(request));
            statsModel.put("statsRows", groupIdToStatsRows);

            cacheStatsModel(statsModel, school);
        }

        Map<String, List<EspResponse>> espResults = _schoolProfileDataHelper.getEspDataForSchool(request);
        statsModel.put("espResults", espResults);

        model.putAll(statsModel);
        System.out.println("School profile stats controller took " + (System.nanoTime() - start) / 1000000 + " milliseconds");
        return model;
    }

    public void cacheStatsModel(Map<String,Object> statsModel, School school) {
        try {
            _censusCacheDao.save(school, statsModel);
        } catch (IOException e) {
            // all is lost. don't cache
        }
    }

    // group ID --> Stats Row
    public Map<Long,List<StatsRow>> combine(
            CensusStateConfig config,
            // CensusDataSet ID <--> CensusDataSet
            // If "extra" CensusDataSets are provided, they will be skipped
            Map<Integer, CensusDataSet> censusDataSets,
            // CensusDataSet ID --> SchoolCensusValue
            Map<Integer,SchoolCensusValue> schoolValueMap,
            // CensusDataSet ID --> DistrictCensusValue
            Map<Integer,DistrictCensusValue> districtValueMap,
            // CensusDataSet ID --> StateCensusValue
            Map<Integer,StateCensusValue> stateValueMap) {

        Map<Long,List<StatsRow>> statsRowMap = new HashMap<Long,List<StatsRow>>();


        LinkedHashMap<Integer,Integer> linkedHashMap = new LinkedHashMap<Integer, Integer>();
        new ArrayList<Integer>(linkedHashMap.values());


        // Data Type ID --> text label
        // TODO: get from cache
        /*Map<Integer,String> dataTypeIdsToLabels = getDataTypeLabels();*/
        for (Map.Entry<Integer,CensusDataSet> entry : censusDataSets.entrySet()) {
            Set<Integer> configuredDataTypeIds = config.allDataTypeIds();

            // if this method was provided with more CensusDataSets then what was configured for the stats page,
            // skip over them here
            if (!configuredDataTypeIds.contains(entry.getValue().getDataType().getId())) {
                continue;
            }

            Integer censusDataSetId = entry.getKey();
            Integer dataTypeId = entry.getValue().getDataType().getId();
            CensusDataType dataTypeEnum = CensusDataType.getEnum(dataTypeId);
            Integer groupIdInt = config.getDataTypeToGroupIdMap().get(entry.getValue().getDataType().getId());
            Long groupId = null;
            if (groupIdInt != null) {
                groupId = groupIdInt.longValue();
            }

            // look to see if there's a data type label "override" in the config entry for this data type
            // if not, just use the default data type label/description
            String label;
            Breakdown breakdown = entry.getValue().getBreakdownOnly();
            if (breakdown != null && breakdown.getEthnicity() != null) {
                label = breakdown.getEthnicity().getName();
            } else {
                label = config.getStateConfigEntryMap().get(dataTypeId).getDataTypeLabel();
                if (label == null) {
                    label = entry.getValue().getDataType().getDescription();
                }
            }

            String schoolValue = "";
            SchoolCensusValue schoolCensusValue = schoolValueMap.get(censusDataSetId);
            if (schoolCensusValue != null) {
                if (schoolCensusValue.getValueFloat() != null) {
                    schoolValue = formatValue(schoolCensusValue.getValueFloat(), dataTypeEnum.getValueType());
                } else {
                    schoolValue = String.valueOf(schoolCensusValue.getValueText());
                }
            }

            String districtValue = "";
            DistrictCensusValue districtCensusValue = districtValueMap.get(censusDataSetId);
            if (districtCensusValue != null) {
                if (districtCensusValue.getValueFloat() != null) {
                    districtValue = formatValue(districtCensusValue.getValueFloat(), dataTypeEnum.getValueType());
                } else {
                    districtValue = String.valueOf(districtCensusValue.getValueText());
                }
            }

            String stateValue = "";
            StateCensusValue stateCensusValue = stateValueMap.get(censusDataSetId);
            if (stateCensusValue != null) {
                if (stateCensusValue.getValueFloat() != null) {
                    stateValue = formatValue(stateCensusValue.getValueFloat(), dataTypeEnum.getValueType());
                } else {
                    stateValue = String.valueOf(stateCensusValue.getValueText());
                }
            }

            Set<CensusDescription> source = entry.getValue().getCensusDescription();

            StatsRow row = new StatsRow(
                    groupId,
                    censusDataSetId,
                    label,
                    schoolValue,
                    districtValue,
                    stateValue,
                    source,
                    entry.getValue().getYear()
                    );

            List<StatsRow> statsRows = statsRowMap.get(groupId);
            if (statsRows == null) {
                statsRows = new ArrayList<StatsRow>();
                statsRowMap.put(groupId, statsRows);
            }

            statsRows.add(row);
        }

        return statsRowMap;
    }

    private String formatValue(Float value, CensusDataType.ValueType valueType) {
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

    public class StatsRow implements Serializable {
        private Long _groupId;
        private Integer _censusDataSetId;
        private String _text;
        private String _schoolValue;
        private String _districtValue;
        private String _stateValue;
        private Set<CensusDescription> _censusDescriptions;
        private Integer _year;

        public StatsRow(Long groupId, Integer censusDataSetId, String text, String schoolValue, String districtValue, String stateValue, Set<CensusDescription> censusDescriptions, Integer year) {
            _groupId = groupId;
            _censusDataSetId = censusDataSetId;
            _text = text;
            _schoolValue = schoolValue;
            _districtValue = districtValue;
            _stateValue = stateValue;
            _censusDescriptions = censusDescriptions;
            _year = year;
        }

        public Long getGroupId() { return _groupId; }
        public String getText() { return _text; }
        public String getSchoolValue() { return _schoolValue; }
        public String getDistrictValue() { return _districtValue; }
        public String getStateValue() { return _stateValue; }
        public Set<CensusDescription> getCensusDescriptions() { return _censusDescriptions; }
        public Integer getYear() { return _year; }
    }

    public Map<Integer, DistrictCensusValue> findDistrictValues(Map<Integer, CensusDataSet> censusDataSets, State state, School school) {
        List<District> districts = new ArrayList<District>(1);
        districts.add(school.getDistrict());
        List<DistrictCensusValue> schoolCensusValues = _censusDataDistrictValueDao.findDistrictCensusValues(state, censusDataSets.values(), districts);
        Map<Integer, DistrictCensusValue> censusValueMap = new HashMap<Integer,DistrictCensusValue>();

        Iterator<DistrictCensusValue> iterator = schoolCensusValues.iterator();
        while (iterator.hasNext()) {
            DistrictCensusValue censusValue = iterator.next();
           censusValueMap.put(censusValue.getDataSet().getId(), censusValue);
        }

        return censusValueMap;
    }

    public Map<Integer, StateCensusValue> findStateValues(Map<Integer, CensusDataSet> censusDataSets, State state) {

        List<StateCensusValue> schoolCensusValues = _censusDataStateValueDao.findStateCensusValues(state, censusDataSets.values());
        Map<Integer, StateCensusValue> censusValueMap = new HashMap<Integer,StateCensusValue>();

        Iterator<StateCensusValue> iterator = schoolCensusValues.iterator();
        while (iterator.hasNext()) {
            StateCensusValue censusValue = iterator.next();
            censusValueMap.put(censusValue.getDataSet().getId(), censusValue);
        }

        return censusValueMap;
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
