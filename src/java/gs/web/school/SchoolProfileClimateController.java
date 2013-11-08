package gs.web.school;

import gs.data.school.School;
import gs.data.school.census.CensusDataSet;
import gs.data.school.census.CensusDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author aroy@greatschools.org
 */
@Controller
@RequestMapping("/school/profileClimate.page")
public class SchoolProfileClimateController extends AbstractSchoolProfileController {
    public static final String VIEW = "school/profileClimate";
    @Autowired
    SchoolProfileCensusHelper _schoolProfileCensusHelper;

    /** What respondent type corresponds to each breakdown data type */
    public static Map<CensusDataType, ClimateRespondentType> BREAKDOWN_TO_RESPONDENT_TYPE = new HashMap<CensusDataType, ClimateRespondentType>() {
        {
            put(CensusDataType.CLIMATE_COMMUNICATION_SCORE_PARENT, ClimateRespondentType.parents);
            put(CensusDataType.CLIMATE_ENGAGEMENT_SCORE_PARENT, ClimateRespondentType.parents);
            put(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_SCORE_PARENT, ClimateRespondentType.parents);
            put(CensusDataType.CLIMATE_SAFETY_RESPECT_SCORE_PARENT, ClimateRespondentType.parents);

            put(CensusDataType.CLIMATE_COMMUNICATION_SCORE_STUDENT, ClimateRespondentType.students);
            put(CensusDataType.CLIMATE_ENGAGEMENT_SCORE_STUDENT, ClimateRespondentType.students);
            put(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_SCORE_STUDENT, ClimateRespondentType.students);
            put(CensusDataType.CLIMATE_SAFETY_RESPECT_SCORE_STUDENT, ClimateRespondentType.students);

            put(CensusDataType.CLIMATE_COMMUNICATION_SCORE_TEACHER, ClimateRespondentType.teachers);
            put(CensusDataType.CLIMATE_ENGAGEMENT_SCORE_TEACHER, ClimateRespondentType.teachers);
            put(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_SCORE_TEACHER, ClimateRespondentType.teachers);
            put(CensusDataType.CLIMATE_SAFETY_RESPECT_SCORE_TEACHER, ClimateRespondentType.teachers);

            put(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_PERCENT_AGREE_PARENT, ClimateRespondentType.parents);
            put(CensusDataType.CLIMATE_FAMILY_ENGAGEMENT_PERCENT_AGREE_PARENT, ClimateRespondentType.parents);
            put(CensusDataType.CLIMATE_SAFETY_CLEANLINESS_PERCENT_AGREE_PARENT, ClimateRespondentType.parents);

            put(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_PERCENT_AGREE_STUDENT, ClimateRespondentType.students);
            put(CensusDataType.CLIMATE_SAFETY_CLEANLINESS_PERCENT_AGREE_STUDENT, ClimateRespondentType.students);
            put(CensusDataType.CLIMATE_RESPECT_RELATIONSHIPS_PERCENT_AGREE_STUDENT, ClimateRespondentType.students);

            put(CensusDataType.CLIMATE_FAMILY_ENGAGEMENT_PERCENT_AGREE_SCHOOL_EMPLOYEE, ClimateRespondentType.employees);
            put(CensusDataType.CLIMATE_SAFETY_CLEANLINESS_PERCENT_AGREE_SCHOOL_EMPLOYEE, ClimateRespondentType.employees);
            put(CensusDataType.CLIMATE_RESPECT_RELATIONSHIPS_PERCENT_AGREE_SCHOOL_EMPLOYEE, ClimateRespondentType.employees);
            put(CensusDataType.CLIMATE_TEACHER_COLLABORATION_SUPPORT_PERCENT_AGREE_SCHOOL_EMPLOYEE, ClimateRespondentType.employees);
        }
    };

    /** Which breakdown data types belong to which total data types */
    public static Map<CensusDataType, List<CensusDataType>> TOTAL_TO_BREAKDOWN_MAP = new HashMap<CensusDataType, List<CensusDataType>>() {
        {
            put(CensusDataType.CLIMATE_COMMUNICATION_SCORE_TOTAL, Arrays.asList(
                    CensusDataType.CLIMATE_COMMUNICATION_SCORE_PARENT,
                    CensusDataType.CLIMATE_COMMUNICATION_SCORE_STUDENT,
                    CensusDataType.CLIMATE_COMMUNICATION_SCORE_TEACHER)
            );
            put(CensusDataType.CLIMATE_ENGAGEMENT_SCORE_TOTAL, Arrays.asList(
                    CensusDataType.CLIMATE_ENGAGEMENT_SCORE_PARENT,
                    CensusDataType.CLIMATE_ENGAGEMENT_SCORE_STUDENT,
                    CensusDataType.CLIMATE_ENGAGEMENT_SCORE_TEACHER)
            );
            put(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_SCORE_TOTAL, Arrays.asList(
                    CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_SCORE_PARENT,
                    CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_SCORE_STUDENT,
                    CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_SCORE_TEACHER)
            );
            put(CensusDataType.CLIMATE_SAFETY_RESPECT_SCORE_TOTAL, Arrays.asList(
                    CensusDataType.CLIMATE_SAFETY_RESPECT_SCORE_PARENT,
                    CensusDataType.CLIMATE_SAFETY_RESPECT_SCORE_STUDENT,
                    CensusDataType.CLIMATE_SAFETY_RESPECT_SCORE_TEACHER)
            );
            put(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_PERCENT_AGREE_TOTAL, Arrays.asList(
                    CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_PERCENT_AGREE_PARENT,
                    CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_PERCENT_AGREE_STUDENT)
            );
            put(CensusDataType.CLIMATE_FAMILY_ENGAGEMENT_PERCENT_AGREE_TOTAL, Arrays.asList(
                    CensusDataType.CLIMATE_FAMILY_ENGAGEMENT_PERCENT_AGREE_PARENT,
                    CensusDataType.CLIMATE_FAMILY_ENGAGEMENT_PERCENT_AGREE_SCHOOL_EMPLOYEE)
            );
            put(CensusDataType.CLIMATE_RESPECT_RELATIONSHIPS_PERCENT_AGREE_TOTAL, Arrays.asList(
                    CensusDataType.CLIMATE_RESPECT_RELATIONSHIPS_PERCENT_AGREE_STUDENT,
                    CensusDataType.CLIMATE_RESPECT_RELATIONSHIPS_PERCENT_AGREE_SCHOOL_EMPLOYEE)
            );
            put(CensusDataType.CLIMATE_SAFETY_CLEANLINESS_PERCENT_AGREE_TOTAL, Arrays.asList(
                    CensusDataType.CLIMATE_SAFETY_CLEANLINESS_PERCENT_AGREE_PARENT,
                    CensusDataType.CLIMATE_SAFETY_CLEANLINESS_PERCENT_AGREE_STUDENT,
                    CensusDataType.CLIMATE_SAFETY_CLEANLINESS_PERCENT_AGREE_SCHOOL_EMPLOYEE)
            );
            put(CensusDataType.CLIMATE_TEACHER_COLLABORATION_SUPPORT_PERCENT_AGREE_TOTAL, Arrays.asList(
                    CensusDataType.CLIMATE_TEACHER_COLLABORATION_SUPPORT_PERCENT_AGREE_SCHOOL_EMPLOYEE)
            );
        }
    };

    /** Another view of which breakdown data types belong to which total data types */
    public static Map<CensusDataType, CensusDataType> BREAKDOWN_TO_TOTAL_MAP = new HashMap<CensusDataType, CensusDataType>() {
        {
            for (CensusDataType totalDT: TOTAL_TO_BREAKDOWN_MAP.keySet()) {
                for (CensusDataType breakdownDT: TOTAL_TO_BREAKDOWN_MAP.get(totalDT)) {
                    put(breakdownDT, totalDT);
                }
            }
        }
    };

    @RequestMapping(method = RequestMethod.GET)
    public String handle(ModelMap modelMap, HttpServletRequest request) {
        School school = getSchool(request);
        modelMap.put("school", school );

        CensusDataHolder censusDataHolder = _schoolProfileCensusHelper.getCensusDataHolder(request);

        // make the census data holder load school, district, and state data onto the census data sets
        Map<Integer, CensusDataSet> censusDataSetMap = censusDataHolder.retrieveDataSetsAndAllData();

        Map<Long, ClimateCategory> dataTypeToBeanMap = new HashMap<Long, ClimateCategory>(); // needs to be Long for jstl to index into it

        // first pass for total data types and response rates
        Map<CensusDataType, List<CensusDataSet>> totalDataTypeToBreakdownDataSets = new HashMap<CensusDataType, List<CensusDataSet>>();
        Map<Integer, CensusDataSet> otherDataSets = new HashMap<Integer, CensusDataSet>();
        for (Integer dataSetId: censusDataSetMap.keySet()) {
            CensusDataSet censusDataSet = censusDataSetMap.get(dataSetId);
            if (isDataTypeForClimate(censusDataSet.getDataType())
                    && (censusDataSet.getSchoolOverrideOrSchoolValue() != null)) {
                if (TOTAL_TO_BREAKDOWN_MAP.get(censusDataSet.getDataType()) != null) {
                    // this is a total data type
                    dataTypeToBeanMap.put(censusDataSet.getDataType().getId().longValue(), new ClimateCategory(censusDataSet));
                } else if (BREAKDOWN_TO_TOTAL_MAP.get(censusDataSet.getDataType()) != null) {
                    // this is a breakdown data type
                    addToMapOfLists(totalDataTypeToBreakdownDataSets, BREAKDOWN_TO_TOTAL_MAP.get(censusDataSet.getDataType()), censusDataSet);
                } else {
                    otherDataSets.put(censusDataSet.getDataType().getId(), censusDataSet);
                }
            }
        }
        // now process the breakdown data types
        for (CensusDataType totalDT: totalDataTypeToBreakdownDataSets.keySet()) {
            for (CensusDataSet breakdownDS: totalDataTypeToBreakdownDataSets.get(totalDT)) {
                if (dataTypeToBeanMap.get(totalDT.getId().longValue()) != null && BREAKDOWN_TO_RESPONDENT_TYPE.get(breakdownDS.getDataType()) != null) {
                    dataTypeToBeanMap.get(totalDT.getId().longValue()).addBreakdown(breakdownDS, BREAKDOWN_TO_RESPONDENT_TYPE.get(breakdownDS.getDataType()));
                } else if (dataTypeToBeanMap.get(totalDT.getId().longValue()) == null) {
                    // If you're reading this, there is possibly a data problem. This school has a breakdown (e.g. "Parents say")
                    // without a "total" value
                    _log.error("Found breakdown climate data set without a \"total\" category to add it to: " + breakdownDS);
                } else {
                    // if you're here, add a mapping to BREAKDOWN_TO_RESPONDENT_TYPE.
                    _log.error("Found breakdown climate data set without mapping to a respondent type: " + breakdownDS);
                }
            }
        }
        // now sort the breakdown lists
        for (ClimateCategory category : dataTypeToBeanMap.values()) {
            if (category.getBreakdowns() != null ) {
                Collections.sort(category.getBreakdowns());
            }
        }

        if (dataTypeToBeanMap.size() > 0) {
            modelMap.put("climateData", dataTypeToBeanMap);

            // now process the response rates
            List<ClimateResponseCount> responseCounts = new ArrayList<ClimateResponseCount>();
            addInResponseCount(responseCounts, otherDataSets, ClimateRespondentType.parents,
                    CensusDataType.CLIMATE_RESPONSE_RATE_PARENT, CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_PARENT);
            addInResponseCount(responseCounts, otherDataSets, ClimateRespondentType.students,
                    CensusDataType.CLIMATE_RESPONSE_RATE_STUDENT, CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_STUDENT);
            addInResponseCount(responseCounts, otherDataSets, ClimateRespondentType.teachers,
                    CensusDataType.CLIMATE_RESPONSE_RATE_TEACHER, CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_TEACHER);
            addInResponseCount(responseCounts, otherDataSets, ClimateRespondentType.employees,
                    CensusDataType.CLIMATE_RESPONSE_RATE_SCHOOL_EMPLOYEE, CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_SCHOOL_EMPLOYEE);
            Collections.sort(responseCounts);

            modelMap.put("climateResponseCounts", responseCounts);
        }

        return VIEW;
    }

    /**
     * Add in one or both data sets to a new ClimateResponseCount object and add to list. Does nothing if both
     * data sets are null.
     */
    protected static void addInResponseCount(List<ClimateResponseCount> responseCounts, Map<Integer, CensusDataSet> dataSets,
                                             ClimateRespondentType respondentType, CensusDataType responseRateDT,
                                             CensusDataType numberOfResponsesDT) {
        CensusDataSet responseRate = dataSets.get(responseRateDT.getId());
        CensusDataSet numberOfResponses = dataSets.get(numberOfResponsesDT.getId());
        if (responseRate != null || numberOfResponses != null) {
            responseCounts.add(new ClimateResponseCount(responseRate, numberOfResponses, respondentType));
        }
    }

    /**
     * Adds value to the list using key on mapOfLists. Handles creating the list for the first value.
     */
    protected static <K, V> void addToMapOfLists(Map<K, List<V>> mapOfLists, K key, V value) {
        List<V> valueList = mapOfLists.get(key);
        if (valueList == null) {
            valueList = new ArrayList<V>();
            mapOfLists.put(key, valueList);
        }
        valueList.add(value);
    }

    /**
     * Represents a climate survey response category. Contains the schools total rating, plus any breakdowns
     * e.g. by parent, student, teacher, employee. Will also contain district/state values if present.
     */
    public static class ClimateCategory {
        private CensusDataSet _total;
        private List<ClimateCategoryBreakdown> _breakdowns;

        public ClimateCategory(CensusDataSet totalDS) {
            _total = totalDS;
            _breakdowns = new ArrayList<ClimateCategoryBreakdown>();
        }

        protected void addBreakdown(CensusDataSet breakdownDataSet, ClimateRespondentType respondentType) {
            _breakdowns.add(new ClimateCategoryBreakdown(breakdownDataSet, respondentType));
        }

        public CensusDataSet getTotal() {
            return _total;
        }

        public List<ClimateCategoryBreakdown> getBreakdowns() {
            return _breakdowns;
        }
    }

    /**
     * Represents a breakdown data set. Is really just here to store the respondent type metadata and for sorting.
     */
    public static class ClimateCategoryBreakdown implements Comparable<ClimateCategoryBreakdown> {
        private CensusDataSet _dataSet;
        private ClimateRespondentType _respondentType;
        public ClimateCategoryBreakdown(CensusDataSet dataSet, ClimateRespondentType respondent) {
            _dataSet = dataSet;
            _respondentType = respondent;
        }

        public CensusDataSet getDataSet() {
            return _dataSet;
        }

        public ClimateRespondentType getRespondentType() {
            return _respondentType;
        }

        public int compareTo(ClimateCategoryBreakdown o) {
            return _respondentType.getSortOrder().compareTo(o.getRespondentType().getSortOrder());
        }
    }

    /**
     * Contains the response rate and number of responses data for a particular respondent type.
     */
    public static class ClimateResponseCount implements Comparable<ClimateResponseCount> {
        private CensusDataSet _responseRate;
        private CensusDataSet _numberOfResponses;
        private ClimateRespondentType _respondentType;

        public ClimateResponseCount(CensusDataSet responseRate, CensusDataSet numberOfResponses, ClimateRespondentType respondentType) {
            _responseRate=responseRate;
            _numberOfResponses=numberOfResponses;
            _respondentType=respondentType;
        }

        public CensusDataSet getResponseRate() {
            return _responseRate;
        }

        public CensusDataSet getNumberOfResponses() {
            return _numberOfResponses;
        }

        public ClimateRespondentType getRespondentType() {
            return _respondentType;
        }

        public int compareTo(ClimateResponseCount o) {
            return _respondentType.getSortOrder().compareTo(o.getRespondentType().getSortOrder());
        }
    }

    /**
     * Enum for a respondent type. Contains a view-friendly label and a sort order.
     */
    public static enum ClimateRespondentType {
        parents("Parents", 10),
        students("Students", 20),
        teachers("Teachers", 30),
        employees("Employees", 40);

        private int _sortOrder;
        private String _label;
        ClimateRespondentType(String label, int sortOrder) {
            _label = label;
            _sortOrder = sortOrder;
        }

        public Integer getSortOrder() {
            return _sortOrder;
        }

        public String getLabel() {
            return _label;
        }
    }

    protected static boolean isDataTypeForClimate(CensusDataType censusDataType) {
        return censusDataType != null && (
                BREAKDOWN_TO_TOTAL_MAP.keySet().contains(censusDataType) ||
                        TOTAL_TO_BREAKDOWN_MAP.keySet().contains(censusDataType) ||
                        censusDataType.equals(CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_PARENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_RESPONSE_RATE_PARENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_STUDENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_RESPONSE_RATE_STUDENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_TEACHER) ||
                        censusDataType.equals(CensusDataType.CLIMATE_RESPONSE_RATE_TEACHER) ||
                        censusDataType.equals(CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_SCHOOL_EMPLOYEE) ||
                        censusDataType.equals(CensusDataType.CLIMATE_RESPONSE_RATE_SCHOOL_EMPLOYEE));
    }
}
