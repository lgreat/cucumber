package gs.web.school;

import gs.data.school.*;
import gs.data.school.breakdown.EthnicityDaoJava;
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
    ICensusDataConfigEntryDao _censusDataConfigDao;

    @Autowired
    ICensusDataConfigEntryDao _censusStateConfigDao;

    @Autowired
    SchoolProfileDataHelper _schoolProfileDataHelper;

    EthnicityDaoJava _ethnicityDaoJava = new EthnicityDaoJava();


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
            censusDataSetMap = getCensusDataSets(censusStateConfig.getState(), dataTypeIds, school);
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
    protected CensusDataHolder getGroupedCensusDataSets( HttpServletRequest request ) {
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
            Triplet<Map<Integer,CensusDataSet>, // CensusDataSet ID --> CensusDataSet
                    Map<Integer,CensusDataSet>, // CensusDataSet ID --> CensusDataSet
                    Map<Integer,CensusDataSet>> // CensusDataSet ID --> CensusDataSet
                    triplet = censusStateConfig.splitCensusDataSets(censusDataSetMap);

            for (Map.Entry<Integer,CensusDataSet> entry : censusDataSetMap.entrySet()) {
                // since the group of data sets retrieved from CensusStateConfig only contain data types that
                // should be displayed on the stats tab, we have to add back in the datasets retrieved from getCensusDataSets method
                if (dataTypeIdsForOverview.contains(entry.getValue().getDataType().getId())) {
                    triplet.getObj1().put(entry.getKey(), entry.getValue());
                }
            }

            censusDataHolder = (CensusDataHolder) _beanFactory.getBean("censusDataHandler", new Object[] {
                    school, censusDataSetMap, triplet.getObj1(), triplet.getObj2(), triplet.getObj3()
            });

            setSharedData(request, CENSUS_DATA_HOLDER, censusDataHolder);
        }

        return censusDataHolder;
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

    // @return CensusDataSet ID --> CensusDataSet
    protected Map<Integer, CensusDataSet> getCensusDataSetsWithSchoolData(HttpServletRequest request) {

        School school = _requestAttributeHelper.getSchool( request );
        if( school == null ) {
            throw new IllegalArgumentException( "The request must already contain a school object" );
        }

        CensusDataHolder groupedCensusDataSets = getGroupedCensusDataSets(request);
        return groupedCensusDataSets.retrieveDataSetsAndSchoolData();
    }

    protected Map<String, String> getEthnicityLabelValueMap(HttpServletRequest request) {
        Map<String,String> ethnicityLabelMap = new HashMap<String,String>();

        // Data Set ID --> SchoolCensusValue
        Map<Integer, CensusDataSet> censusDataSetMap = getCensusDataSetsWithSchoolData(request);

        Integer ethnicityDataTypeId = CensusDataType.STUDENTS_ETHNICITY.getId();

        for (Map.Entry<Integer, CensusDataSet> entry : censusDataSetMap.entrySet()) {
            CensusDataSet censusDataSet = entry.getValue();

            SchoolCensusValue schoolCensusValue = censusDataSet.getTheOnlySchoolValue();

            if (schoolCensusValue != null && censusDataSet.getDataType().getId().equals(ethnicityDataTypeId)) {
                Float floatValue = schoolCensusValue.getValueFloat();
                String value = null;
                if (floatValue == null) {
                    value = schoolCensusValue.getValueText();
                } else {
                    value = formatValueAsString(floatValue, CensusDataType.STUDENTS_ETHNICITY.getValueType());
                }

                if (value != null) {
                    ethnicityLabelMap.put(censusDataSet.getBreakdownOnly().getEthnicity().getName(), value);
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
     * Warning: do not use this with CensusDataSets that are attached to a hibernate session
     * Finds override CensusSchoolValues attached to override CensusDataSets, and re-attaches those school values
     * to CensusDataSets with same data type, breakdown, level, subject, etc (but with year non-zero).
     * Used to make display-generation code easier
     *
     * @param censusDataSets
     * @return Map of CensusDataSet ID to SchoolCensusValue
     */
    public void handleSchoolValueOverrides(Collection<CensusDataSet> censusDataSets)
    {
        if (censusDataSets == null || censusDataSets.isEmpty()) {
            throw new IllegalArgumentException("CensusDataSets cannot be null or empty");
        }

        // Data Type ID  -->  (max) year
        Map<Integer,Integer> dataTypeMaxYears = new HashMap<Integer,Integer>();

        // grade + data_type_id + breakdown_id | level_code + subject_id --> Census Data Set
        Map<String, CensusDataSet> overrides = new HashMap<String, CensusDataSet>();

        // grade + data_type_id + breakdown_id | level_code + subject_id --> Census Data Set
        Map<String, CensusDataSet> nonOverrides = new HashMap<String, CensusDataSet>();

        // Most logic here is to handle manual overrides.
        // requires one iteration over CensusDataSets, and one iteration over all fetched SchoolCensusValues

        // first, figure out what the most recent year is for each data type
        for (CensusDataSet censusDataSet : censusDataSets) {
            Integer year = dataTypeMaxYears.get(censusDataSet.getDataType().getId());
            if (year == null) {
                year = censusDataSet.getYear();
            } else {
                year = Math.max(year, censusDataSet.getYear());
            }
            dataTypeMaxYears.put(censusDataSet.getDataType().getId(), year);
        }

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
        //for (SchoolCensusValue schoolCensusValue : schoolCensusValues) {
        Iterator<CensusDataSet> iterator = censusDataSets.iterator();
        while (iterator.hasNext()) {
            CensusDataSet censusDataSet = iterator.next();
            SchoolCensusValue schoolCensusValue = censusDataSet.getTheOnlySchoolValue();
            if (schoolCensusValue == null) {
                continue;
            }

            Boolean override = false;
            // Here we get the CensusDataSet that was passed in, by ID. The CensusDataSets that are on the
            // CensusDataSchoolValue are not complete
            //CensusDataSet censusDataSet = censusDataSets.get(schoolCensusValue.getDataSet().getId());
            Integer maxYear = dataTypeMaxYears.get(censusDataSet.getDataType().getId());

            // if this SchoolValue's got override potential...
            if (censusDataSet.getYear() == 0 && schoolCensusValue.getModified() != null) {
                Date modified = schoolCensusValue.getModified();
                manualCalendar.setTime(modified);
                dataSetCalendar.set(Calendar.YEAR, maxYear-1);

                override = manualCalendar.after(dataSetCalendar);

                // it's an override!
                if (override) {
                    overrides.put(getCensusDataSetHash(censusDataSet), censusDataSet);
                }
            } else {
                // sorry, no override potential
                nonOverrides.put(getCensusDataSetHash(censusDataSet), censusDataSet);
            }
        }

        // for each dataset that contains a valid override:
        //  find the cooresponding non-year-zero dataset that is the one being overridden
        //  take the school value (should be only one) from the override dataset, and set it onto the school value being
        // overridden
        for (Map.Entry<String, CensusDataSet> overrideEntry : overrides.entrySet()) {
            CensusDataSet overridingDataSet = overrideEntry.getValue();
            String key = overrideEntry.getKey();
            CensusDataSet overriddenDataSet = nonOverrides.get(key);

            if (overriddenDataSet != null) {
                overriddenDataSet.getTheOnlySchoolValue().setOverrideValue(overridingDataSet.getTheOnlySchoolValue());
            } else {
                _log.debug("Something went wrong; no non-zero dataset corresponds to overriding dataset with year zero");
            }
        }

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

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        _beanFactory = beanFactory;
    }
}



