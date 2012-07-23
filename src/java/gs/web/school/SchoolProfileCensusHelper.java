package gs.web.school;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.school.*;
import gs.data.school.breakdown.Ethnicity;
import gs.data.school.breakdown.EthnicityDaoJava;
import gs.data.school.census.*;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.web.request.RequestAttributeHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component("schoolProfileCensusHelper")
public class SchoolProfileCensusHelper extends AbstractDataHelper {

    private final static String CENSUS_DATA = "censusData";
    public final static String CENSUS_DATA_SETS = "censusDataSets";
    public final static String CENSUS_STATE_CONFIG = "censusStateConfig";
    private final static Logger _log = Logger.getLogger(SchoolProfileCensusHelper.class);

    @Autowired
    private RequestAttributeHelper _requestAttributeHelper;

    @Autowired
    private ICensusDataSchoolValueDao _censusDataSchoolValueDao;

    @Autowired
    private ICensusDataSetDao _censusDataSetDao;

    @Autowired
    ICensusDataConfigEntryDao _censusDataConfigDao;

    @Autowired
    ICensusDataConfigEntryDao _censusStateConfigDao;

    @Autowired
    SchoolProfileDataHelper _schoolProfileDataHelper;

    EthnicityDaoJava _ethnicityDaoJava = new EthnicityDaoJava();


    /**
     * GroupedCensusDataSets = collections of CensusDataSets to be used to obtain school values, district values, and state values
     */
    public class GroupedCensusDataSets {
        // CensusDataSet ID --> CensusDataSet
        public final Map<Integer,CensusDataSet> _dataSetsForSchoolData;
        public final Map<Integer,CensusDataSet>_dataSetsForDistrictData;
        public final Map<Integer,CensusDataSet> _dataSetsForStateData;

        public GroupedCensusDataSets(Map<Integer, CensusDataSet> dataSetsForSchoolData, Map<Integer, CensusDataSet> dataSetsForDistrictData, Map<Integer, CensusDataSet> dataSetsForStateData) {
            _dataSetsForSchoolData = dataSetsForSchoolData;
            _dataSetsForDistrictData = dataSetsForDistrictData;
            _dataSetsForStateData = dataSetsForStateData;
        }
    }


    /**
     * Gets CensusDataSets for the passed-in dataTypeIds.
     * @return map of CensusDataSet ID to CensusDataSets
     */
    public Map<Integer, CensusDataSet> getCensusDataSets(State state, Set<Integer> dataTypeIds, School school ) {
        List<CensusDataSet> censusDataSets = _censusDataSetDao.findLatestDataSetsForDataTypes(state, dataTypeIds, school);

        // places Census Data Sets into a map of  groupID --> Census Data Set
        Map<Integer, CensusDataSet> groupIdsToCensusDataSets = new HashMap<Integer, CensusDataSet>();

        for (CensusDataSet censusDataSet : censusDataSets) {
            groupIdsToCensusDataSets.put(censusDataSet.getId(), censusDataSet);
        }

        return groupIdsToCensusDataSets;
    }

    /**
     * @return map of CensusDataSet ID --> CensusDataSet
     */
    protected Map<Integer, CensusDataSet> getCensusDataSets( HttpServletRequest request ) {
        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // CensusDataSet ID --> CensusDataSet
        Map<Integer, CensusDataSet> censusDataSetMap = (Map<Integer, CensusDataSet>) getSharedData(request, CENSUS_DATA_SETS);
        if (censusDataSetMap == null) {
            // get the census config for this state, school type, and level code
            CensusStateConfig censusStateConfig = getCensusStateConfig(request);
            // get the data type IDs for our census config
            Set<Integer> dataTypeIds = censusStateConfig.allDataTypeIds();
            Set<Integer> dataTypeIdsForOverview = _schoolProfileDataHelper.getCensusDataTypeIdsForOverview();
            Set<Integer> allDataTypeIds = new HashSet<Integer>();
            allDataTypeIds.addAll(dataTypeIds);
            allDataTypeIds.addAll(dataTypeIdsForOverview);
            censusDataSetMap = getCensusDataSets(censusStateConfig.getState(), dataTypeIds, school);
            setSharedData(request, CENSUS_DATA_SETS, censusDataSetMap);
        }

        return censusDataSetMap;
    }


    /**
     * Gets CensusDataSets grouped into school,district,state buckets. Each bucket contains the CensusDataSets
     * that will be used to obtain school values, district values, and state values, respectively
     * GroupedCensusDataSets = collections of CensusDataSets to be used to obtain school values, district values, and state values
     */
    public GroupedCensusDataSets splitDataSets(Map<Integer,CensusDataSet> censusDataSets, CensusStateConfig config) {

        // CensusDataSet ID --> CensusDataSet
        Map<Integer,CensusDataSet> dataSetsForSchoolData = new HashMap<Integer, CensusDataSet>();
        Map<Integer,CensusDataSet>  dataSetsForDistrictData = new HashMap<Integer, CensusDataSet>();
        Map<Integer,CensusDataSet> dataSetsForStateData = new HashMap<Integer, CensusDataSet>();

        for (Map.Entry<Integer, CensusDataSet> censusDataSetEntry : censusDataSets.entrySet()) {
            Integer dataTypeId = censusDataSetEntry.getValue().getDataType().getId();
            Integer breakdownId = null;
            if (censusDataSetEntry.getValue().getBreakdownOnly() != null) {
                breakdownId = censusDataSetEntry.getValue().getBreakdownOnly().getId();
            }

            //ICensusDataConfigEntry stateConfigEntry = config.get(censusDataSetEntry.getValue().getDataType().getId());

            // There may be one or many items in stateConfigEntries. but all of them must have the same data type ID
            List<ICensusDataConfigEntry> stateConfigEntries = config.get(dataTypeId);


            //TODO: should be iterate over all items in stateConfigEntries if there are multiple, and find the one with
            // the matching breakdownId?
            ICensusDataConfigEntry stateConfigEntry = stateConfigEntries.get(0);

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
        return new GroupedCensusDataSets(dataSetsForSchoolData, dataSetsForDistrictData, dataSetsForStateData);
    }

    /**
     * Gets CensusDataSets grouped into school,district,state buckets. Each bucket contains the CensusDataSets
     * that will be used to obtain school values, district values, and state values, respectively
     * @param request
     * @return
     */
    protected GroupedCensusDataSets getGroupedCensusDataSets( HttpServletRequest request ) {

        // get the census config for this state, school type, and level code
        CensusStateConfig censusStateConfig = getCensusStateConfig(request);

        Set<Integer> dataTypeIdsForOverview = _schoolProfileDataHelper.getCensusDataTypeIdsForOverview();

        Map<Integer, CensusDataSet> censusDataSetMap = getCensusDataSets(request);

        GroupedCensusDataSets groupedCensusDataSets = splitDataSets(censusDataSetMap, censusStateConfig);

        for (Map.Entry<Integer,CensusDataSet> entry : censusDataSetMap.entrySet()) {
            if (dataTypeIdsForOverview.contains(entry.getValue().getDataType().getId())) {
                groupedCensusDataSets._dataSetsForSchoolData.put(entry.getKey(), entry.getValue());
            }
        }

        return groupedCensusDataSets;
    }

    protected CensusStateConfig getCensusStateConfig( HttpServletRequest request ) {
        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        CensusStateConfig censusStateConfig = (CensusStateConfig) getSharedData(request, CENSUS_STATE_CONFIG);
        if (censusStateConfig == null) {
            censusStateConfig =  _censusStateConfigDao.getConfigForSchoolsStateAndType(school);
            setSharedData(request, CENSUS_STATE_CONFIG, censusStateConfig);
        }

        return censusStateConfig;
    }

    // @return CensusDataSet ID --> SchoolCensusValue
    protected Map<Integer, SchoolCensusValue> getSchoolCensusValues( HttpServletRequest request ) {

        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // CensusDataSet ID --> SchoolCensusValue
        Map<Integer, SchoolCensusValue> schoolCensusValueMap = (Map<Integer, SchoolCensusValue>) getSharedData(request, CENSUS_DATA);

        if (schoolCensusValueMap == null) {
            schoolCensusValueMap = new HashMap<Integer, SchoolCensusValue>();
            GroupedCensusDataSets groupedCensusDataSets = getGroupedCensusDataSets(request);

            if (groupedCensusDataSets._dataSetsForSchoolData != null && groupedCensusDataSets._dataSetsForSchoolData.size() > 0) {
                schoolCensusValueMap = findSchoolCensusValuesAndHandleOverrides(groupedCensusDataSets._dataSetsForSchoolData, school);
            }

            setSharedData(request, CENSUS_DATA, schoolCensusValueMap);
        }

        return schoolCensusValueMap;
    }

    protected Map<String, String> getEthnicityLabelValueMap(HttpServletRequest request) {
        Map<String,String> ethnicityLabelMap = new HashMap<String,String>();

        // Data Set ID --> SchoolCensusValue
        Map<Integer, SchoolCensusValue> schoolCensusValueMap = getSchoolCensusValues(request);

        Integer ethnicityDataTypeId = CensusDataType.STUDENTS_ETHNICITY.getId();
        CensusDataType dataTypeEnum = CensusDataType.STUDENTS_ETHNICITY;

        for (Map.Entry<Integer, SchoolCensusValue> entry : schoolCensusValueMap.entrySet()) {
            SchoolCensusValue schoolCensusValue = entry.getValue();

            if (schoolCensusValue.getDataSet().getDataType().getId().equals(ethnicityDataTypeId)) {
                Float floatValue = entry.getValue().getValueFloat();
                String value = null;
                if (floatValue == null) {
                    value = entry.getValue().getValueText();
                } else {
                    value = formatValueAsString(floatValue, CensusDataType.STUDENTS_ETHNICITY.getValueType());
                }

                if (value != null) {
                    ethnicityLabelMap.put(_ethnicityDaoJava.getEthnicity(schoolCensusValue.getDataSet().getBreakdown().getId()).getName(), value);
                }
            }
        }
        return ethnicityLabelMap;
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

    /**
     * @param censusDataSets
     * @param school
     * @return Map of CensusDataSet ID to SchoolCensusValue
     */
    public Map<Integer, SchoolCensusValue> findSchoolCensusValuesAndHandleOverrides(
            Map<Integer, CensusDataSet> censusDataSets, // CensusDataSet ID --> CensusDataSet
            School school)
    {
        if (censusDataSets == null || school == null) {
            throw new IllegalArgumentException("Neither censusDataSets nor school should be null");
        }

        List<School> schools = new ArrayList<School>(1);
        schools.add(school);
        System.out.println("before searching census school values: " + System.nanoTime() / 1000000);
        List<SchoolCensusValue> schoolCensusValues = new ArrayList<SchoolCensusValue>();
        if (censusDataSets.size() > 0) {
            schoolCensusValues = _censusDataSchoolValueDao.findSchoolCensusValues(school.getDatabaseState(), censusDataSets.values(), schools);
        }

        System.out.println("after searching census school values: " + System.nanoTime() / 1000000);
        Map<Integer, SchoolCensusValue> schoolCensusValueMap = new HashMap<Integer,SchoolCensusValue>();

        // Data Type ID  -->  (max) year
        Map<Integer,Integer> dataTypeMaxYears = new HashMap<Integer,Integer>();


        // grade + data_type_id + breakdown_id | level_code + subject_id --> Census Data Set
        Map<String, SchoolCensusValue> dataSetSchoolValueMap = new HashMap<String, SchoolCensusValue>();

        // Most logic here is to handle manual overrides.
        // requires one iteration over CensusDataSets, and one iteration over all fetched SchoolCensusValues

        // first, figure out what the most recent year is for each data type
        for (CensusDataSet censusDataSet : censusDataSets.values()) {
            Integer year = dataTypeMaxYears.get(censusDataSet.getDataType().getId());
            if (year == null) {
                year = censusDataSet.getYear();
            } else {
                year = Math.max(year, censusDataSet.getYear());
            }
            dataTypeMaxYears.put(censusDataSet.getDataType().getId(), year);
        }
        System.out.println("after populating census dataTypeMaxYears: " + System.nanoTime() / 1000000);


        Calendar manualCalendar = Calendar.getInstance();
        Calendar dataSetCalendar = Calendar.getInstance();
        dataSetCalendar.roll(Calendar.YEAR, -1); // better than (dataSet.getYear() - 1)
        dataSetCalendar.set(Calendar.MONTH, Calendar.OCTOBER);
        dataSetCalendar.set(Calendar.DAY_OF_MONTH, 1);
        //noinspection MagicNumber
        dataSetCalendar.set(Calendar.HOUR_OF_DAY, 0);
        dataSetCalendar.set(Calendar.MINUTE, 0);
        dataSetCalendar.set(Calendar.SECOND, 0);
        dataSetCalendar.set(Calendar.MILLISECOND, 0);
        // second, figure out if each School has any CensusSchoolValues with recent-enough manual override
        for (SchoolCensusValue schoolCensusValue : schoolCensusValues) {
            Boolean override = false;
            // Here we get the CensusDataSet that was passed in, by ID. The CensusDataSets that are on the
            // CensusDataSchoolValue are not complete
            CensusDataSet censusDataSet = censusDataSets.get(schoolCensusValue.getDataSet().getId());
            Integer maxYear = dataTypeMaxYears.get(censusDataSet.getDataType().getId());

            // if this SchoolValue's got override potential...
            if (schoolCensusValue.getDataSet().getYear() == 0 && schoolCensusValue.getModified() != null) {
                Date modified = schoolCensusValue.getModified();
                manualCalendar.setTime(modified);
                dataSetCalendar.set(Calendar.YEAR, maxYear-1);

                override = manualCalendar.after(dataSetCalendar);

                // it's an override!
                if (override) {
                    // use this school value for the data set
                    dataSetSchoolValueMap.put(getCensusDataSetHash(censusDataSet), schoolCensusValue);
                }
            } else {
                // sorry, no override potential
                // only use this school value if there's no existing school value for this data set
                if (dataSetSchoolValueMap.get(getCensusDataSetHash(censusDataSet)) == null) {
                    dataSetSchoolValueMap.put(getCensusDataSetHash(censusDataSet), schoolCensusValue);
                }
            }
        }
        System.out.println("done with census school values: " + System.nanoTime() / 1000000);

        for (SchoolCensusValue schoolCensusValue : dataSetSchoolValueMap.values()) {
            schoolCensusValueMap.put(schoolCensusValue.getDataSet().getId(), schoolCensusValue);
        }

        return schoolCensusValueMap;
    }

    /**
     * Construct a hash based on the properties of a CensusDataSet, minus the year
     * @param censusDataSet
     * @return
     */
    public String getCensusDataSetHash(CensusDataSet censusDataSet) {
        // grade + data_type_id + breakdown_id | level_code + subject_id --> Census Data Set
        String result = (censusDataSet.getBreakdownOnly() != null ? String.valueOf(censusDataSet.getBreakdownOnly().hashCode()) : "|");
        result += (censusDataSet.getLevelCode() != null ? String.valueOf(censusDataSet.getLevelCode().getCommaSeparatedString()) : "|");
        result += (censusDataSet.getDataType() != null ? String.valueOf(censusDataSet.getDataType().getId()) : "|");
        result += (censusDataSet.getGradeLevels() != null ? String.valueOf(censusDataSet.getGradeLevels().getCommaSeparatedString()) : "|");
        result += (censusDataSet.getSubject() != null ? String.valueOf(censusDataSet.getSubject().getSubjectId()) : "|");

        return result;
    }

}



