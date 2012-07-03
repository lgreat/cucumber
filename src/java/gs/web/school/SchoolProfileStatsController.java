package gs.web.school;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gs.data.school.School;
import gs.data.school.census.*;
import gs.data.school.district.District;
import gs.data.state.State;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.ReadWriteController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
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
    ICensusCacheDao _censusCacheDao;

    @RequestMapping(method= RequestMethod.GET)
    public Map<String,Object> handle(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestParam(value = "schoolId", required = false) Integer schoolId,
                                     @RequestParam(value = "state", required = false) State state
    ) {
        Long start = System.nanoTime();
        Map<String,Object> model = new HashMap<String,Object>();

        School school = getSchool(request, state, schoolId);

        Map<String,Object> statsModel = new HashMap<String,Object>();
        statsModel = _censusCacheDao.getMapForSchool(school);

        if (statsModel == null || statsModel.size() == 0) {

            // Census Data Set ID --> Source
            Map<Integer, CensusDescription> dataTypeSourceMap = new HashMap<Integer,CensusDescription>();

            // get the census config for this state, school type, and level code
            CensusStateConfig censusStateConfig =
                    _censusStateConfigDao.getConfigForState(school.getDatabaseState(), school.getType());

            System.out.println("Getting census state config took " + (System.nanoTime()-start)/1000000 + " milliseconds");


            // get the data type IDs for our census config
            Set<Integer> dataTypeIds = censusStateConfig.allDataTypeIds();


            // query to get the latest data sets for each data type
            // CensusDataSet ID --> CensusDataSet
            BiMap<Integer, CensusDataSet> censusDataSets = getDataSets(censusStateConfig, dataTypeIds, school);
            System.out.println("Getting census data sets config took " + (System.nanoTime()-start)/1000000 + " milliseconds");

            // Source --> source position (footnote)
            /*Map<CensusDescription, Integer> sourceFootnotes = getSourceFootnotes(dataTypeSourceMap);*/


            SplitDataSets splitDataSets = splitDataSets(censusDataSets, censusStateConfig);


            // query for school values
            // CensusDataSet ID --> CensusSchoolValue
            Map<Integer, SchoolCensusValue> schoolValueMap = new HashMap<Integer, SchoolCensusValue>();
            if (splitDataSets._dataSetsForSchoolData.size() > 0) {
                schoolValueMap = findSchoolValues(splitDataSets._dataSetsForSchoolData, school.getDatabaseState(), school);
            }

            System.out.println("Getting census school values took " + (System.nanoTime()-start)/1000000 + " milliseconds");

            // query for district values
            // CensusDataSet ID --> CensusDistrictValue
            Map<Integer, DistrictCensusValue> districtValueMap = new HashMap<Integer,DistrictCensusValue>();
            if (splitDataSets._dataSetsForDistrictData.size() > 0) {
                 districtValueMap = findDistrictValues(splitDataSets._dataSetsForDistrictData, school.getDatabaseState(), school);
            }

            System.out.println("Getting census district values took " + (System.nanoTime()-start)/1000000 + " milliseconds");

            // query for state averages
            // CensusDataSet ID --> CensusStateValue
            Map<Integer, StateCensusValue> stateValueMap = new HashMap<Integer,StateCensusValue>();
            if (splitDataSets._dataSetsForStateData.size() > 0) {
                stateValueMap = findStateValues(splitDataSets._dataSetsForStateData, school.getDatabaseState());
            }

            System.out.println("Getting census state values took " + (System.nanoTime()-start)/1000000 + " milliseconds");

            // group ID --> List of StatsRow
            Map<Long, List<StatsRow>> groupIdToStatsRows = combine(censusStateConfig, censusDataSets, schoolValueMap, districtValueMap, stateValueMap);

            System.out.println("Combining took " + (System.nanoTime()-start)/1000000 + " milliseconds");

            statsModel.put("dataTypeSourceMap", dataTypeSourceMap);
            statsModel.put("censusStateConfig", censusStateConfig);
            statsModel.put("statsRows", groupIdToStatsRows);

            cacheStatsModel(statsModel, school);

        }

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
            BiMap<Integer, CensusDataSet> censusDataSets,
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
            Integer censusDataSetId = entry.getKey();
            Integer dataTypeId = entry.getValue().getDataType().getId();
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
                    schoolValue = String.valueOf(Math.round(schoolCensusValue.getValueFloat()));
                } else {
                    schoolValue = String.valueOf(schoolCensusValue.getValueText());
                }
            }

            String districtValue = "";
            DistrictCensusValue districtCensusValue = districtValueMap.get(censusDataSetId);
            if (districtCensusValue != null) {
                if (districtCensusValue.getValueFloat() != null) {
                    districtValue = String.valueOf(Math.round(districtCensusValue.getValueFloat()));
                } else {
                    districtValue = String.valueOf(districtCensusValue.getValueText());
                }
            }

            String stateValue = "";
            StateCensusValue stateCensusValue = stateValueMap.get(censusDataSetId);
            if (stateCensusValue != null) {
                if (stateCensusValue.getValueFloat() != null) {
                    stateValue = String.valueOf(Math.round(stateCensusValue.getValueFloat()));
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

    /**
     * Map of CensusDataSet ID to SchoolCensusValue
     * @param censusDataSets
     * @param state
     * @param school
     * @return
     */
    public Map<Integer, SchoolCensusValue> findSchoolValues(Map<Integer, CensusDataSet> censusDataSets, State state, School school) {
        List<School> schools = new ArrayList<School>(1);
        schools.add(school);
        List<SchoolCensusValue> schoolCensusValues = _censusDataSchoolValueDao.findSchoolCensusValues(state, censusDataSets.values(), schools);
        Map<Integer, SchoolCensusValue> schoolCensusValueMap = new HashMap<Integer,SchoolCensusValue>();

        // Data Type ID  -->  (max) year
        Map<Integer,Integer> dataTypeMaxYears = new HashMap<Integer,Integer>();

        // Most logic here is to handle manual overrides.
        // requires one iteration over CensusDataSets, and one iteration over all fetched SchoolCensusValues

        // first, figure out what the most recent year is for each data type
        for (CensusDataSet censusDataSet : censusDataSets.values()) {
            Integer year = dataTypeMaxYears.get(censusDataSet.getDataType());
            if (year == null) {
                year = censusDataSet.getYear();
            } else {
                year = Math.max(year, censusDataSet.getYear());
            }
            dataTypeMaxYears.put(censusDataSet.getDataType().getId(), year);
        }


        // second, figure out if each School has any CensusSchoolValues with recent-enough manual override
        for (SchoolCensusValue schoolCensusValue : schoolCensusValues) {
            Boolean override = false;
            Integer maxYear = dataTypeMaxYears.get(schoolCensusValue.getDataSet().getDataType().getId());

            // if this SchoolValue's got override potential...
            if (schoolCensusValue.getDataSet().getYear() == 0 && schoolCensusValue.getModified() != null) {
                Date modified = schoolCensusValue.getModified();
                Calendar manualCalendar = Calendar.getInstance();
                manualCalendar.setTime(modified);
                Calendar dataSetCalendar = Calendar.getInstance();
                dataSetCalendar.set(Calendar.YEAR, maxYear);
                dataSetCalendar.roll(Calendar.YEAR, -1); // better than (dataSet.getYear() - 1)
                dataSetCalendar.set(Calendar.MONTH, Calendar.OCTOBER);
                dataSetCalendar.set(Calendar.DAY_OF_MONTH, 1);
                //noinspection MagicNumber
                dataSetCalendar.set(Calendar.HOUR_OF_DAY, 0);
                dataSetCalendar.set(Calendar.MINUTE, 1);
                dataSetCalendar.set(Calendar.SECOND, 0);
                dataSetCalendar.set(Calendar.MILLISECOND, 0);

                override = manualCalendar.after(dataSetCalendar);

                // it's an override!
                if (override) {
                    // use this school value for the data set
                    schoolCensusValueMap.put(schoolCensusValue.getDataSet().getId(), schoolCensusValue);
                }
            } else {
                // sorry, no override potential
                // only use this school value if there's no existing school value for this data set
                if (schoolCensusValueMap.get(schoolCensusValue.getDataSet().getId()) == null) {
                    schoolCensusValueMap.put(schoolCensusValue.getDataSet().getId(), schoolCensusValue);
                }
            }
        }

        return schoolCensusValueMap;
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


    public Map<Integer,String> getDataTypeLabels() {
        Map<Integer, String> dataTypeLabels = new HashMap<Integer,String>();
        Iterator<CensusDataType> iterator = CensusDataType.iterator();
        while (iterator.hasNext()) {
            CensusDataType censusDataType = iterator.next();
            dataTypeLabels.put(censusDataType.getId(), censusDataType.getDescription());
        }

        return dataTypeLabels;
    }

    public List<StateCensusValue> findStateValues() {
        return null;
    }

    public SplitDataSets splitDataSets(Map<Integer,CensusDataSet> censusDataSets, CensusStateConfig config) {
        BiMap<Integer,CensusDataSet> dataSetsForSchoolData = HashBiMap.create();
        BiMap<Integer,CensusDataSet>  dataSetsForDistrictData = HashBiMap.create();
        BiMap<Integer,CensusDataSet> dataSetsForStateData = HashBiMap.create();


        for (Map.Entry<Integer, CensusDataSet> censusDataSetEntry : censusDataSets.entrySet()) {
            ICensusDataConfigEntry stateConfigEntry = config.get(censusDataSetEntry.getValue().getDataType().getId());
            if (stateConfigEntry.hasSchoolData()) {
                dataSetsForSchoolData.put(censusDataSetEntry.getKey(), censusDataSetEntry.getValue());
            }

            if (stateConfigEntry.hasDistrictData()) {
                dataSetsForDistrictData.put(censusDataSetEntry.getKey(), censusDataSetEntry.getValue());
            }

            if (stateConfigEntry.hasStateData()) {
                dataSetsForStateData.put(censusDataSetEntry.getKey(), censusDataSetEntry.getValue());
            }
        }
        return new SplitDataSets(dataSetsForSchoolData, dataSetsForDistrictData, dataSetsForStateData);
    }

    /**
     * Return map of CensusDataSet IDs to CensusDataSets
     *
     */
    public BiMap<Integer, CensusDataSet> getDataSets(CensusStateConfig config, Set<Integer> dataTypeIds, School school ) {
        List<CensusDataSet> censusDataSets = _censusDataSetDao.findLatestDataSetsForDataTypes(config.getState(), dataTypeIds, school);

        // places Census Data Sets into a map of  groupID --> Census Data Set
        BiMap<Integer, CensusDataSet> groupIdsToCensusDataSets = HashBiMap.create();
        for (CensusDataSet censusDataSet : censusDataSets) {
            Integer dataTypeId = censusDataSet.getDataType().getId();
            Integer groupId = config.getDataTypeToGroupIdMap().get(dataTypeId);
            if (groupId != null) {
                groupIdsToCensusDataSets.put(censusDataSet.getId(), censusDataSet);
            }
        }

        // TODO: cache data sets for state

        return groupIdsToCensusDataSets;
    }

    public class SplitDataSets {
        public final BiMap<Integer,CensusDataSet> _dataSetsForSchoolData;
        public final BiMap<Integer,CensusDataSet>_dataSetsForDistrictData;
        public final BiMap<Integer,CensusDataSet> _dataSetsForStateData;

        public SplitDataSets(BiMap<Integer,CensusDataSet> dataSetsForSchoolData, BiMap<Integer,CensusDataSet> dataSetsForDistrictData, BiMap<Integer,CensusDataSet> dataSetsForStateData) {
            _dataSetsForSchoolData = dataSetsForSchoolData;
            _dataSetsForDistrictData = dataSetsForDistrictData;
            _dataSetsForStateData = dataSetsForStateData;
        }
    }

    public ICensusDataSetDao getCensusDataSetDao() {
        return _censusDataSetDao;
    }

    public void setCensusDataSetDao(ICensusDataSetDao censusDataSetDao) {
        _censusDataSetDao = censusDataSetDao;
    }
}
