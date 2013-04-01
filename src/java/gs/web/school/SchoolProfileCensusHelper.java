package gs.web.school;

import gs.data.school.*;
import gs.data.school.census.*;
import gs.data.state.State;
import gs.data.util.Triplet;
import gs.web.request.RequestAttributeHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component("schoolProfileCensusHelper")
public class SchoolProfileCensusHelper extends AbstractDataHelper implements BeanFactoryAware {

    private final static String CENSUS_DATA = "censusData";
    public final static String CENSUS_DATA_SETS = "censusDataSets";
    public final static String CENSUS_STATE_CONFIG = "censusStateConfig";
    public final static String CENSUS_DATA_HOLDER = "censusDataHolder";

    private BeanFactory _beanFactory;
    private final static Logger _log = Logger.getLogger(SchoolProfileCensusHelper.class);

    @Autowired
    private RequestAttributeHelper _requestAttributeHelper;

    @Autowired
    private ICensusDataSchoolValueDao _censusDataSchoolValueDao;

    @Autowired
    private ICensusDataSetDao _censusDataSetDao;

    @Autowired
    ICensusDataConfigEntryDao _censusStateConfigDao;

    @Autowired
    SchoolProfileDataHelper _schoolProfileDataHelper;

    /**
     * Tries to reverse sort by the DisplayRow's float value. If there's no float value, try to reverse sort by the
     * DisplayRow's text label instead.
     */
     static class DisplayRowSchoolValueDescComparator implements Comparator<SchoolProfileStatsDisplayRow> {
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
    }
    public static Comparator<SchoolProfileStatsDisplayRow> SCHOOL_VALUE_DESCENDING_COMPARATOR = new DisplayRowSchoolValueDescComparator();

    static class DisplayRowSortOrderComparator implements Comparator<SchoolProfileStatsDisplayRow> {
        public int compare(SchoolProfileStatsDisplayRow row1, SchoolProfileStatsDisplayRow row2) {
            Integer sort1 = row1.getSort();
            Integer sort2 = row2.getSort();
            if (sort1 == null && sort2 == null) {
                return 0;
            } else if (sort1 == null) {
                return 1;
            } else if (sort2 == null) {
                return -1;
            } else {
                return sort1.compareTo(sort2);
            }
        }
    }
    public static DisplayRowSortOrderComparator DISPLAY_ROW_SORT_ORDER_COMPARATOR = new DisplayRowSortOrderComparator();


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
     * @return map of CensusDataSet ID --> CensusDataSet.
     * Returns censusDataSets for display on stats tab and overview tab of school profile
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
            censusDataSetMap = getCensusDataSets(censusStateConfig.getState(), allDataTypeIds, school);
            setSharedData(request, CENSUS_DATA_SETS, censusDataSetMap);
        }

        return censusDataSetMap;
    }


    /**
     * Gets CensusDataSets grouped into school,district,state buckets. Each bucket contains the CensusDataSets
     * that will be used to obtain school values, district values, and state values, respectively
     * @param request
     * @return
     */
    protected CensusDataHolder getCensusDataHolder(HttpServletRequest request) {
        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        CensusDataHolder censusDataHolder = (CensusDataHolder) getSharedData(request, CENSUS_DATA_HOLDER);

        if (censusDataHolder == null) {
            // get the census config for this state, school type, and level code
            // Only contains info about censusDataSets that were configured for display on census tables on stats page on school profile
            CensusStateConfig censusStateConfig = getCensusStateConfig(request);

            Set<Integer> dataTypeIdsForOverview = _schoolProfileDataHelper.getCensusDataTypeIdsForOverview();

            // Returns censusDataSets for display on stats tab and overview tab of school profile
            Map<Integer, CensusDataSet> censusDataSetMap = getCensusDataSets(request);

            // Split censusDataSets that were configured for display on census tables on stats page on school profile
            Triplet<Map<Integer,CensusDataSet>, // CensusDataSet ID --> CensusDataSet.
                    Map<Integer,CensusDataSet>, // CensusDataSet ID --> CensusDataSet
                    Map<Integer,CensusDataSet>> // CensusDataSet ID --> CensusDataSet
                    triplet = censusStateConfig.splitCensusDataSets(censusDataSetMap);

            censusDataHolder = (CensusDataHolder) _beanFactory.getBean("censusDataHandler", new Object[] {
                    school, censusDataSetMap, triplet.getObj1(), triplet.getObj2(), triplet.getObj3()
            });

            setSharedData(request, CENSUS_DATA_HOLDER, censusDataHolder);
        }

        return censusDataHolder;
    }

    protected CensusStateConfig getCensusStateConfig(HttpServletRequest request) {
        return getCensusStateConfig(request, CENSUS_STATE_CONFIG);
    }

    protected CensusStateConfig getCensusStateConfig(HttpServletRequest request, String key) {
        // Make sure we have a school
        School school = _requestAttributeHelper.getSchool(request);
        if (school == null) {
            throw new IllegalArgumentException("The request must already contain a school object");
        }

        CensusStateConfig censusStateConfig = (CensusStateConfig) getSharedData(request, key);
        if (censusStateConfig == null) {
            censusStateConfig = _censusStateConfigDao.getConfigForSchoolsStateAndType(school);
            setSharedData(request, key, censusStateConfig);
        }
        return censusStateConfig;
    }

    // @return CensusDataSet ID --> CensusDataSet
    protected Map<Integer, CensusDataSet> getCensusDataSetsWithSchoolData(HttpServletRequest request) {

        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        CensusDataHolder groupedCensusDataSets = getCensusDataHolder(request);
        return groupedCensusDataSets.retrieveDataSetsAndSchoolData();
    }

    /**
     * look to see if there's a data type label "override" in the config entry for this data type
     * if not, just use the default data type label/description
     *
     * @param censusDataSet
     * @param configEntry
     * @return
     */
    protected String getLabel(CensusDataSet censusDataSet, ICensusDataConfigEntry configEntry) {
        String label;
        boolean dataSetHasBreakdown = censusDataSet.getBreakdownOnly() != null;
        boolean configEntryHasBreakdown = configEntry.getBreakdownId() != null;

        if (configEntryHasBreakdown) {
            label = CensusDataHelper.getCensusConfigLabelOrDefault(configEntry, censusDataSet);
        } else {
            if (dataSetHasBreakdown) {
                // if there's not a breakdown configured, BUT the data set does have a breakdown, that means
                // that the label for each breakdown isn't configured in the config table, and we should use
                // the breakdown's name as the label
                label = CensusDataHelper.getDataSetDefaultLabel(censusDataSet);
            } else {
                label = CensusDataHelper.getCensusConfigLabelOrDefault(configEntry, censusDataSet);
            }
        }
        return label;
    }


    protected Map<String, String> getEthnicityLabelValueMap(HttpServletRequest request) {
        return getLabelValueMapForGroup(request, CensusGroup.Student_Ethnicity);
    }

    protected Map<String, String> getLabelValueMapForGroup(HttpServletRequest request, CensusGroup group) {

        // Data Set ID --> SchoolCensusValue
        Map<Integer, CensusDataSet> censusDataSetMap = getCensusDataSetsWithSchoolData(request);

        CensusStateConfig config = getCensusStateConfig(request);

        Map<CensusGroup, GroupOfStudentTeacherViewRows> groupIdToDisplayRows = buildDisplayRows(config, censusDataSetMap);

        return groupIdToDisplayRows.get(group).getSchoolValueMap();
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        _beanFactory = beanFactory;
    }

    // group ID --> Stats Row
    public Map<CensusGroup,GroupOfStudentTeacherViewRows> buildDisplayRows(CensusStateConfig config, Map<Integer, CensusDataSet> censusDataSets) {

        Map<CensusGroup,GroupOfStudentTeacherViewRows> statsRowMap = new LinkedHashMap<CensusGroup,GroupOfStudentTeacherViewRows>();

        for (Map.Entry<Integer,CensusDataSet> entry : censusDataSets.entrySet()) {

            CensusDataSet censusDataSet = entry.getValue();

            // if this dataset has year zero, it's an override dataset, and it's school value should have been assigned
            // to the companion dataset that doesn't have year zero. If a non-year-zero dataset didn't exist, this
            // data set will have it's schoolOverrideValue set
            if (censusDataSet.getYear() == 0 && censusDataSet.getSchoolOverrideValue() == null) {
                continue;
            }

            // Find the entry that cooresponds with the data type, breakdown and grade on this data set.
            ICensusDataConfigEntry configEntry = config.getEntry(censusDataSet);

            // If config doesnt exist for data type + breakdown, skip this data set
            if (configEntry == null) {
                continue;
            }

            String label = getLabel(censusDataSet, configEntry);

            CensusGroup group = CensusGroup.getById(configEntry.getGroupId().longValue());

            SchoolProfileStatsDisplayRow row = new SchoolProfileStatsDisplayRow(
                group.getId(),
                censusDataSet.getDataType().getId(),
                censusDataSet.getId(),
                label,
                censusDataSet.getSchoolOverrideOrSchoolValue(),
                censusDataSet.getTheOnlyDistrictValue(),
                censusDataSet.getStateCensusValue(),
                censusDataSet.getCensusDescription(),
                censusDataSet.getYear(),
                censusDataSet.getSchoolOverrideValue() != null,
                configEntry.getSort()
            );

            // filter out rows where school and district values are N/A
            if (showRow(row)) {
                GroupOfStudentTeacherViewRows statsRows = statsRowMap.get(group);
                if (statsRows == null) {
                    statsRows = new GroupOfStudentTeacherViewRows(group, new ArrayList<SchoolProfileStatsDisplayRow>());
                    statsRowMap.put(group, statsRows);
                }

                statsRows.add(row);
            }
        }

        sortDisplayRows(statsRowMap);

        return statsRowMap;
    }

    public void sortDisplayRows(Map<CensusGroup, GroupOfStudentTeacherViewRows> map) {
        if (map == null) {
            return;
        }

        Iterator<Map.Entry<CensusGroup, GroupOfStudentTeacherViewRows>> iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<CensusGroup, GroupOfStudentTeacherViewRows> entry = iterator.next();
            Collections.sort(entry.getValue(), getComparator(entry.getKey()));
        }
    }

    public Comparator<SchoolProfileStatsDisplayRow> getComparator(CensusGroup group) {
        Comparator<SchoolProfileStatsDisplayRow> comparator = getGroupSortConfig().get(group);

        if (comparator == null) {
            comparator = SchoolProfileCensusHelper.DISPLAY_ROW_SORT_ORDER_COMPARATOR;
        }

        return comparator;
    }

    public Map<CensusGroup, Comparator<SchoolProfileStatsDisplayRow>> getGroupSortConfig() {
        Map<CensusGroup, Comparator<SchoolProfileStatsDisplayRow>> map =
                new HashMap<CensusGroup, Comparator<SchoolProfileStatsDisplayRow>>();

        map.put(CensusGroup.Student_Ethnicity, SchoolProfileCensusHelper.SCHOOL_VALUE_DESCENDING_COMPARATOR);
        map.put(CensusGroup.Home_Languages_of_English_Learners, SchoolProfileCensusHelper.SCHOOL_VALUE_DESCENDING_COMPARATOR);

        return map;
    }


    protected boolean showRow(SchoolProfileStatsDisplayRow row) {
        boolean showRow;
        if (CensusDataType.STUDENTS_ETHNICITY.getId().equals(row.getDataTypeId())) {
            showRow = (
                   SchoolProfileStatsDisplayRow.censusValueNotEmpty(row.getSchoolValue())
                || SchoolProfileStatsDisplayRow.censusValueNotEmpty(row.getDistrictValue())
                || SchoolProfileStatsDisplayRow.censusValueNotEmpty(row.getStateValue()));
        } else {
            showRow = (SchoolProfileStatsDisplayRow.censusValueNotEmpty(row.getSchoolValue())
                    || SchoolProfileStatsDisplayRow.censusValueNotEmpty(row.getDistrictValue()));
        }
        return showRow;
    }
}

