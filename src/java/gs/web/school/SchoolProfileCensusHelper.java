package gs.web.school;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.school.*;
import gs.data.school.census.*;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.web.request.RequestAttributeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component("schoolProfileCensusHelper")
public class SchoolProfileCensusHelper {

    private final static String CENSUS_DATA = "censusData";
    public final static String CENSUS_DATA_SETS = "censusDataSets";
    public final static String CENSUS_STATE_CONFIG = "censusStateConfig";

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
     * Return map of CensusDataSet ID to CensusDataSets
     */
    public Map<Integer, CensusDataSet> getCensusDataSets(CensusStateConfig config, Set<Integer> dataTypeIds, School school ) {
        List<CensusDataSet> censusDataSets = _censusDataSetDao.findLatestDataSetsForDataTypes(config.getState(), dataTypeIds, school);

        // places Census Data Sets into a map of  groupID --> Census Data Set
        Map<Integer, CensusDataSet> groupIdsToCensusDataSets = new HashMap<Integer, CensusDataSet>();
        for (CensusDataSet censusDataSet : censusDataSets) {
            Integer dataTypeId = censusDataSet.getDataType().getId();
            Integer groupId = config.getDataTypeToGroupIdMap().get(dataTypeId);
            if (groupId != null) {
                groupIdsToCensusDataSets.put(censusDataSet.getId(), censusDataSet);
            }
        }

        return groupIdsToCensusDataSets;
    }

    /**
     * Returns map of CensusDataSet ID --> CensusDataSet
     */
    protected Map<Integer, CensusDataSet> getCensusDataSets( HttpServletRequest request ) {
        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // CensusDataSet ID --> CensusDataSet
        Map<Integer, CensusDataSet> censusDataSetMap = (Map<Integer, CensusDataSet>) request.getAttribute(CENSUS_DATA_SETS);
        if (censusDataSetMap == null) {
            // get the census config for this state, school type, and level code
            CensusStateConfig censusStateConfig = getCensusStateConfig(request);
            // get the data type IDs for our census config
            Set<Integer> dataTypeIds = censusStateConfig.allDataTypeIds();
            Set<Integer> dataTypeIdsForOverview = _schoolProfileDataHelper.getCensusDataTypeIdsForOverview();
            Set<Integer> allDataTypeIds = new HashSet<Integer>();
            allDataTypeIds.addAll(dataTypeIds);
            allDataTypeIds.addAll(dataTypeIdsForOverview);
            censusDataSetMap = getCensusDataSets(censusStateConfig, dataTypeIds, school);
            request.setAttribute(CENSUS_DATA_SETS, censusDataSetMap);
        }

        return censusDataSetMap;
    }


    /**
     * Splits CensusDataSets into GroupedCensusDataSets
     * GroupedCensusDataSets = collections of CensusDataSets to be used to obtain school values, district values, and state values
     */
    public GroupedCensusDataSets splitDataSets(Map<Integer,CensusDataSet> censusDataSets, CensusStateConfig config) {

        // CensusDataSet ID --> CensusDataSet
        Map<Integer,CensusDataSet> dataSetsForSchoolData = new HashMap<Integer, CensusDataSet>();
        Map<Integer,CensusDataSet>  dataSetsForDistrictData = new HashMap<Integer, CensusDataSet>();
        Map<Integer,CensusDataSet> dataSetsForStateData = new HashMap<Integer, CensusDataSet>();

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
        return new GroupedCensusDataSets(dataSetsForSchoolData, dataSetsForDistrictData, dataSetsForStateData);
    }

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

        CensusStateConfig censusStateConfig = (CensusStateConfig) request.getAttribute(CENSUS_STATE_CONFIG);
        if (censusStateConfig == null) {
            censusStateConfig =  _censusStateConfigDao.getConfigForSchoolsStateAndType(school);
            request.setAttribute(CENSUS_STATE_CONFIG, censusStateConfig);
        }

        return censusStateConfig;
    }

    // CensusDataType ID --> SchoolCensusValue
    protected Map<Integer, SchoolCensusValue> getSchoolCensusValues( HttpServletRequest request ) {

        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        // CensusDataType ID --> SchoolCensusValue
        Map<Integer, SchoolCensusValue> schoolCensusValueMap = (Map<Integer, SchoolCensusValue>) request.getAttribute(CENSUS_DATA);
        if (schoolCensusValueMap == null) {
            GroupedCensusDataSets groupedCensusDataSets = getGroupedCensusDataSets(request);

            schoolCensusValueMap = findSchoolCensusValuesAndHandleOverrides(groupedCensusDataSets._dataSetsForSchoolData, school);

            request.setAttribute(CENSUS_DATA, schoolCensusValueMap);
        }

        return schoolCensusValueMap;
    }

    /**
     * Map of CensusDataSet ID to SchoolCensusValue
     * @param censusDataSets
     * @param school
     * @return
     */
    public Map<Integer, SchoolCensusValue> findSchoolCensusValuesAndHandleOverrides(Map<Integer, CensusDataSet> censusDataSets, School school) {
        List<School> schools = new ArrayList<School>(1);
        schools.add(school);
        System.out.println("before searching census school values: " + System.nanoTime() / 1000000);
        List<SchoolCensusValue> schoolCensusValues = _censusDataSchoolValueDao.findSchoolCensusValues(school.getDatabaseState(), censusDataSets.values(), schools);
        System.out.println("after searching census school values: " + System.nanoTime() / 1000000);
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
            Integer maxYear = dataTypeMaxYears.get(schoolCensusValue.getDataSet().getDataType().getId());

            // if this SchoolValue's got override potential...
            if (schoolCensusValue.getDataSet().getYear() == 0 && schoolCensusValue.getModified() != null) {
                Date modified = schoolCensusValue.getModified();
                manualCalendar.setTime(modified);
                dataSetCalendar.set(Calendar.YEAR, maxYear);

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
        System.out.println("done with census school values: " + System.nanoTime() / 1000000);

        return schoolCensusValueMap;
    }


    // ============== The following setters are just for unit testing ===================

    public void setRequestAttributeHelper( RequestAttributeHelper requestAttributeHelper ) {
        _requestAttributeHelper = requestAttributeHelper;
    }

    public void setCensusDataSchoolValueDao(ICensusDataSchoolValueDao censusDataSchoolValueDao) {
        _censusDataSchoolValueDao = censusDataSchoolValueDao;
    }

    public void setCensusDataSetDao(ICensusDataSetDao censusDataSetDao) {
        _censusDataSetDao = censusDataSetDao;
    }

    public void setCensusDataConfigDao(ICensusDataConfigEntryDao censusDataConfigDao) {
        _censusDataConfigDao = censusDataConfigDao;
    }
}


