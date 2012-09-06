package gs.web.school;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import gs.data.school.*;
import gs.data.school.breakdown.EthnicityDaoJava;
import gs.data.school.census.*;
import gs.data.state.State;
import gs.data.util.Triplet;
import gs.web.request.RequestAttributeHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
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
            Triplet<Map<Integer,CensusDataSet>, // CensusDataSet ID --> CensusDataSet.
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

    protected Map<String, String> getEthnicityLabelValueMap(Map<Integer, CensusDataSet> censusDataSetMap) {

        LinkedHashMap<String,String> ethnicityLabelMap = new LinkedHashMap<String,String>();

        // stackoverflow.com/questions/109383/É
        Comparator<String> valueComparator = Ordering.from(new EthnicityComparator()).onResultOf(Functions.forMap(ethnicityLabelMap)).compound(Ordering.natural());

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

        return ImmutableSortedMap.copyOf(ethnicityLabelMap, valueComparator);
    }



    protected Map<String, String> getEthnicityLabelValueMap(HttpServletRequest request) {

        // Data Set ID --> SchoolCensusValue
        Map<Integer, CensusDataSet> censusDataSetMap = getCensusDataSetsWithSchoolData(request);

        return getEthnicityLabelValueMap(censusDataSetMap);
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

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        _beanFactory = beanFactory;
    }
}

class EthnicityComparator implements Comparator<String>, Serializable {
    private final static Logger _log = Logger.getLogger(EthnicityComparator.class);
    public int compare(String value1, String value2) {
        Float row1Value = formatValueAsFloat(value1);
        Float row2Value = formatValueAsFloat(value2);
        // reverse sort
        return row2Value.compareTo(row1Value);
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
}


