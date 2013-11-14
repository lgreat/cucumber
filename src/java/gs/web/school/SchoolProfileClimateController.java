package gs.web.school;

import gs.data.school.School;
import gs.data.school.census.CensusDataSet;
import gs.data.school.census.CensusDataType;
import gs.data.state.State;
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

    /** What respondent type corresponds to each breakdown data type, used for view labeling. */
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

    /** Which breakdown data types belong to which total data types, used to construct hierarchical view bean objects */
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

    /**
     * Configures the order in which various data types appear on the climate tab.
     */
    public static Map<CensusDataType, Integer> TOTAL_DATA_TYPE_ORDER_MAP = new HashMap<CensusDataType, Integer>() {
        {
            put(CensusDataType.CLIMATE_SAFETY_RESPECT_SCORE_TOTAL, 10);
            put(CensusDataType.CLIMATE_COMMUNICATION_SCORE_TOTAL, 20);
            put(CensusDataType.CLIMATE_ENGAGEMENT_SCORE_TOTAL, 30);
            put(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_SCORE_TOTAL, 40);
            put(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_PERCENT_AGREE_TOTAL, 50);
            put(CensusDataType.CLIMATE_RESPECT_RELATIONSHIPS_PERCENT_AGREE_TOTAL, 60);
            put(CensusDataType.CLIMATE_SAFETY_CLEANLINESS_PERCENT_AGREE_TOTAL, 70);
            put(CensusDataType.CLIMATE_FAMILY_ENGAGEMENT_PERCENT_AGREE_TOTAL, 80);
            put(CensusDataType.CLIMATE_TEACHER_COLLABORATION_SUPPORT_PERCENT_AGREE_TOTAL, 90);
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
        // fetch the copy
        DataDescription dataDescription = getDataDescription(school.getDatabaseState());
        modelMap.put("dataDescriptions", dataDescription.getDescriptions());

        processDataSetsAndPopulateModel(censusDataSetMap, modelMap, dataDescription);

        return VIEW;
    }

    /**
     * Process censusDataSetMap and populate dataTypeToBeanMap with the relevant view beans for the primary data.
     * Also populate otherDataSets with data sets needed for other things like # of responses.
     */
    protected void processDataSetsAndPopulateModel(Map<Integer, CensusDataSet> censusDataSetMap,
                                                   Map<String, Object> modelMap, DataDescription dataDescription) {
        // this maps the "total" data types to a display bean
        // the map is easier to deal with right now. Later we convert to a sorted list for the view.
        Map<CensusDataType, ClimateCategory> dataTypeToBeanMap = new HashMap<CensusDataType, ClimateCategory>();
        // Helper map to organize breakdown data sets under their respective "total" data set
        Map<CensusDataType, List<CensusDataSet>> totalDataTypeToBreakdownDataSets = new HashMap<CensusDataType, List<CensusDataSet>>();
        // used for data sets not specifically for the primary climate result tables
        Map<CensusDataType, CensusDataSet> otherDataSets = new HashMap<CensusDataType, CensusDataSet>();

        // first pass, create a ClimateCategory for each "total" data type with the right value.
        // Also store what breakdowns and other data sets we come across for later
        for (CensusDataSet censusDataSet: censusDataSetMap.values()) {
            CensusDataType myDataType = censusDataSet.getDataType();
            if (isDataTypeForClimate(myDataType) && (censusDataSet.getSchoolOverrideOrSchoolValue() != null)) {
                if (TOTAL_TO_BREAKDOWN_MAP.containsKey(myDataType)) {
                    // this is a total data type, create a new view bean for it
                    dataTypeToBeanMap.put(myDataType, new ClimateCategory(censusDataSet, dataDescription));
                } else if (BREAKDOWN_TO_TOTAL_MAP.containsKey(myDataType)) {
                    // this is a breakdown data type, store it for later
                    // I don't process it right away because we might not have come across the respective total
                    // and therefore the view bean may not be instantiated. Simpler to just handle these after
                    addToMapOfLists(totalDataTypeToBreakdownDataSets, BREAKDOWN_TO_TOTAL_MAP.get(myDataType), censusDataSet);
                } else {
                    // Not a total or breakdown, probably a # of responses or response rate. Store it for later
                    otherDataSets.put(myDataType, censusDataSet);
                }
            }
        }
        // If this school actually has climate data
        if (dataTypeToBeanMap.size() > 0) {
            // now populate each ClimateCategory with the right breakdown data sets, using totalDataTypeToBreakdownDataSets
            addInBreakdowns(dataTypeToBeanMap, totalDataTypeToBreakdownDataSets);

            // Now convert to the sorted set we'll be using in the view
            modelMap.put("climateData", new TreeSet<ClimateCategory>(dataTypeToBeanMap.values()));

            // now process the response counts and the total # of responses
            List<ClimateResponseCount> responseCounts = getClimateResponseCounts(otherDataSets);
            modelMap.put("climateResponseCounts", responseCounts);
            modelMap.put("climateTotalResponses", sumNumberOfResponses(responseCounts));
        }
    }

    /**
     * Sum up the values in the number of responses data set for each display bean to form a total # of responses.
     * And return it.
     */
    protected int sumNumberOfResponses(List<ClimateResponseCount> responseCounts) {
        int totalResponses = 0;
        for (ClimateResponseCount responseCount: responseCounts) {
            if (responseCount.getNumberOfResponses() != null && responseCount.getNumberOfResponses().getSchoolOverrideOrSchoolValue() != null && responseCount.getNumberOfResponses().getSchoolOverrideOrSchoolValue().getValueInteger() != null) {
                totalResponses += responseCount.getNumberOfResponses().getSchoolOverrideOrSchoolValue().getValueInteger();
            }
        }
        return totalResponses;
    }

    /**
     * Loop through the data sets in totalDataTypeToBreakdownDataSets, look up the related display bean for each
     * and add the data set into the display bean.
     */
    protected void addInBreakdowns(Map<CensusDataType, ClimateCategory> dataTypeToBeanMap, Map<CensusDataType, List<CensusDataSet>> totalDataTypeToBreakdownDataSets) {
        for (Map.Entry<CensusDataType, List<CensusDataSet>> entry: totalDataTypeToBreakdownDataSets.entrySet()) {
            CensusDataType totalDT = entry.getKey();
            for (CensusDataSet breakdownDS: entry.getValue()) {
                if (isValidBreakdownDataSet(dataTypeToBeanMap, totalDT, breakdownDS)) {
                    dataTypeToBeanMap.get(totalDT).addBreakdown
                            (breakdownDS, BREAKDOWN_TO_RESPONDENT_TYPE.get(breakdownDS.getDataType()));
                }
            }
        }
    }

    /**
     * Returns true if the breakdown data set meets the criteria for inclusion on the page. Generally this is
     * only false if there is a misconfiguration in code or in the database.
     */
    protected boolean isValidBreakdownDataSet(Map<CensusDataType, ClimateCategory> dataTypeToBeanMap, CensusDataType totalDT, CensusDataSet breakdownDS) {
        if (!BREAKDOWN_TO_RESPONDENT_TYPE.containsKey(breakdownDS.getDataType())) {
            // need to know the type of breakdown ("respondent type") for view labeling
            // if you're here, add a mapping to BREAKDOWN_TO_RESPONDENT_TYPE.
            _log.error("Found breakdown climate data set without mapping to a respondent type: " + breakdownDS);
            return false;
        } else if (!dataTypeToBeanMap.containsKey((totalDT))) {
            // If you're reading this, there is possibly a data problem. This school has a breakdown (e.g. "Parents say")
            // without a "total" value
            _log.error("Found breakdown climate data set without a \"total\" category to add it to: " + breakdownDS);
            return false;
        }
        return true;
    }

    /**
     * Get a sorted list of ClimateResponseCount view beans extracted from the map of data type id to data sets
     */
    protected static List<ClimateResponseCount> getClimateResponseCounts(Map<CensusDataType, CensusDataSet> dataTypeToDataSet) {
        List<ClimateResponseCount> responseCounts = new ArrayList<ClimateResponseCount>();
        addInResponseCount(responseCounts, dataTypeToDataSet, ClimateRespondentType.parents,
                CensusDataType.CLIMATE_RESPONSE_RATE_PARENT,          CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_PARENT);
        addInResponseCount(responseCounts, dataTypeToDataSet, ClimateRespondentType.students,
                CensusDataType.CLIMATE_RESPONSE_RATE_STUDENT,         CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_STUDENT);
        addInResponseCount(responseCounts, dataTypeToDataSet, ClimateRespondentType.teachers,
                CensusDataType.CLIMATE_RESPONSE_RATE_TEACHER,         CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_TEACHER);
        addInResponseCount(responseCounts, dataTypeToDataSet, ClimateRespondentType.employees,
                CensusDataType.CLIMATE_RESPONSE_RATE_SCHOOL_EMPLOYEE, CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_SCHOOL_EMPLOYEE);
        Collections.sort(responseCounts);
        return responseCounts;
    }

    /**
     * Add in one or both data sets to a new ClimateResponseCount object and add to list. Does nothing if both
     * data sets are null.
     */
    protected static void addInResponseCount(List<ClimateResponseCount> responseCounts, Map<CensusDataType, CensusDataSet> dataTypeToDataSet,
                                             ClimateRespondentType respondentType, CensusDataType responseRateDT,
                                             CensusDataType numberOfResponsesDT) {
        CensusDataSet responseRate = dataTypeToDataSet.get(responseRateDT);
        CensusDataSet numberOfResponses = dataTypeToDataSet.get(numberOfResponsesDT);
        if (responseRate != null || numberOfResponses != null) {
            responseCounts.add(new ClimateResponseCount(responseRate, numberOfResponses, respondentType));
        }
    }

    /**
     * Adds value to the list using key on mapOfLists. Handles creating the list for the first value.
     */
    public static <K, V> void addToMapOfLists(Map<K, List<V>> mapOfLists, K key, V value) {
        List<V> valueList = mapOfLists.get(key);
        if (valueList == null) {
            valueList = new ArrayList<V>();
            mapOfLists.put(key, valueList);
        }
        valueList.add(value);
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

    /**
     * Represents a climate survey response category. Contains the schools total rating, plus any breakdowns
     * e.g. by parent, student, teacher, employee. Will also contain district/state values if present.
     */
    public static class ClimateCategory implements Comparable<ClimateCategory> {
        private CensusDataSet _total;
        private SortedSet<ClimateCategoryBreakdown> _breakdowns;
        private String _title;
        private String _description;

        public ClimateCategory(CensusDataSet totalDS, DataDescription dataDescriptions) {
            _total = totalDS;
            _breakdowns = new TreeSet<ClimateCategoryBreakdown>();
            _title = dataDescriptions.get("climate_datatype_" + totalDS.getDataType().getId() + "_title");
            _description = dataDescriptions.get("climate_datatype_" + totalDS.getDataType().getId() + "_description");
        }

        protected void addBreakdown(CensusDataSet breakdownDataSet, ClimateRespondentType respondentType) {
            _breakdowns.add(new ClimateCategoryBreakdown(breakdownDataSet, respondentType));
        }

        public CensusDataSet getTotal() {
            return _total;
        }

        public SortedSet<ClimateCategoryBreakdown> getBreakdowns() { // used by view
            return _breakdowns;
        }

        public String getTitle() {
            return _title;
        }

        public String getDescription() {
            return _description;
        }

        public int compareTo(ClimateCategory o) {
            Integer myOrder = TOTAL_DATA_TYPE_ORDER_MAP.get(_total.getDataType());
            if (myOrder == null) {
                myOrder = _total.getDataType().getId();
            }
            Integer hisOrder = TOTAL_DATA_TYPE_ORDER_MAP.get(o.getTotal().getDataType());
            if (hisOrder == null) {
                hisOrder = o.getTotal().getDataType().getId();
            }
            return myOrder.compareTo(hisOrder);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClimateCategory that = (ClimateCategory) o;
            return _total.getId().equals(that.getTotal().getId());
        }

        @Override
        public int hashCode() {
            return _total.getId().hashCode();
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClimateCategoryBreakdown that = (ClimateCategoryBreakdown) o;
            return _dataSet.getId().equals(that.getDataSet().getId());
        }

        @Override
        public int hashCode() {
            return _dataSet.getId().hashCode();
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

    // For unit tests
    protected void setSchoolProfileCensusHelper(SchoolProfileCensusHelper schoolProfileCensusHelper) {
        _schoolProfileCensusHelper = schoolProfileCensusHelper;
    }

    protected SchoolProfileCensusHelper getSchoolProfileCensusHelper() {
        return _schoolProfileCensusHelper;
    }

    public DataDescription getDataDescription(State state) {
        Map<String, String> descriptions = new HashMap<String, String>();
        if (state == State.CA) {
            descriptions.put("climate_source1", "2012-2013 New York City Department of Education School Survey");
            descriptions.put("climate_about_learning_environment", "The Los Angeles Unified School District (LAUSD) School Experience Survey asks parents, students and employees about their school's learning environment. Results provide insight into school climate, such as whether the school is academically rigorous, engaging, safe, and collaborative.");
            descriptions.put("climate_learn_more_url", "http://notebook.lausd.net/portal/page?_pageid=33,1052381&_dad=ptl&_schema=PTL_EP");
            descriptions.put("climate_learn_more_url_text", "Learn more about the LAUSD survey");
            descriptions.put("climate_learn_more_body", "We organized questions from the LAUSD School Experience Survey to five categories. The group-level results (parents, students, and school employees) show the percent of each respondent group that agree or strongly agree that the school has positive results for that category. Overall school results for each category are calculated by averaging across group-level results, ensuring that each respondent group is equally represented. Alongside the results for each school are the aggregated results across all LAUSD schools, which are provided as a basis for comparisons.");
        } else if (state == State.NY) {
            descriptions.put("climate_source1", "2012-13 Los Angeles Unified School District School Experience Survey");
            descriptions.put("climate_about_learning_environment", "The NYC Department of Education asked parents, teachers and students about their school's learning environment. Results provide insight into school climate, such as whether the school is academically rigorous, safe, communicative and collaborative.");
            descriptions.put("climate_learn_more_url", "http://schools.nyc.gov/NR/rdonlyres/C5971763-B938-43CF-A525-0DBCCED94AA0/0/2013NYCSchoolSurveyScoringGuide.pdf");
            descriptions.put("climate_learn_more_url_text", "Learn more about the NYCDOE survey.");
            descriptions.put("climate_learn_more_body", "The information captured by the survey is designed to support a dialogue among all members of the school community about how to make the school a better place to learn. An overall category score is calculated for each respondent group (parents, teachers, or students) by averaging the scores of the questions within that survey category. Category scores for each of the respondent groups are then combined to form overall category scores. Alongside the results for each school are the aggregated results across all NYC public schools, which are provided as a basis for comparisons.");
            descriptions.put("climate_district_average_label", "City average");
        }
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_SCORE_TOTAL.getId() + "_title", "High academic expectations for all students");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_SCORE_TOTAL.getId() + "_description", "This score measures how well parents, students and teachers feel that the school develops rigorous and meaningful academic goals that encourage students to do their best.");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_COMMUNICATION_SCORE_TOTAL.getId() + "_title", "Clear, useful communication about educational goals");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_COMMUNICATION_SCORE_TOTAL.getId() + "_description", "This score measures whether parents, students and teachers feel that the school provides information about the school's educational goals and offers appropriate feedback on each student's learning outcome.");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_ENGAGEMENT_SCORE_TOTAL.getId() + "_title", "Strong parent, teacher and student engagement");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_ENGAGEMENT_SCORE_TOTAL.getId() + "_description", "This score measures how engaged parents, students and teachers feel they are in an active and vibrant partnership to promote student learning.");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_SAFETY_RESPECT_SCORE_TOTAL.getId() + "_title", "A safe and respectful environment");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_SAFETY_RESPECT_SCORE_TOTAL.getId() + "_description", "This score measures whether parents, students and teachers feel that the school creates a physically and emotionally secure environment in which everyone can focus on student learning.");

        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_PERCENT_AGREE_TOTAL.getId() + "_title", "High academic expectations for all students");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_PERCENT_AGREE_TOTAL.getId() + "_description", "This score measures the percent of parents and students that agree to strongly agree that this school sets high academic expectations for its students and expects them to be college-bound. This score is based on the average of the following LAUSD survey Content Areas: School Future Expectations (Parents), School Quality (Parents), Future Plans (Parents), Opportunities For Learning (Students), Future Plans (Students).");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_FAMILY_ENGAGEMENT_PERCENT_AGREE_TOTAL.getId() + "_title", "Strong family engagement");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_FAMILY_ENGAGEMENT_PERCENT_AGREE_TOTAL.getId() + "_description", "This score measures the percent of parents and employees that agree to strongly agree that this school engages parents and communicates with families to promote student learning. This score is based on the average of the following LAUSD survey Content Areas: Parent Involvement (Employees), Feeling of Welcome (Parents), School Involvement (Parents), Teacher to Parent Communication (Parents).");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_RESPECT_RELATIONSHIPS_PERCENT_AGREE_TOTAL.getId() + "_title", "Healthy, respectful relationships");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_RESPECT_RELATIONSHIPS_PERCENT_AGREE_TOTAL.getId() + "_description", "This score measures the percent of  students and employees that agree to strongly agree that this school has a positive learning environment and cultivates an atmosphere of respect. This score is based on the average of the following LAUSD survey Content Areas: School Support, Commitment and Collaboration (Employees), Satisfaction (Students), School Support (Students).");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_SAFETY_CLEANLINESS_PERCENT_AGREE_TOTAL.getId() + "_title", "A safe, clean and orderly environment");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_SAFETY_CLEANLINESS_PERCENT_AGREE_TOTAL.getId() + "_description", "This score measures the percent of parents, students and employees that agree to strongly agree that this school has a well-kept facility and a safe environment conducive to learning. This score is based on the average of the following LAUSD survey Content Areas: School Cleanliness (Employees), School Safety (Employees), Safety (Parents), School Cleanliness (Students), School Safety (Students).");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_TEACHER_COLLABORATION_SUPPORT_PERCENT_AGREE_TOTAL.getId() + "_title", "Teacher support and collaboration opportunities");
        descriptions.put("climate_datatype_" + CensusDataType.CLIMATE_TEACHER_COLLABORATION_SUPPORT_PERCENT_AGREE_TOTAL.getId() + "_description", "This score measures the percent of employees that agree to strongly agree that this school ensures that teachers work well together, learn from one another, have opportunities for professional development and feel supported by the administration. This score is based on the average of the following LAUSD survey Content Areas: Evaluation (Employees), Opportunities for Involvement (Employees), Professional Development (Employees), Resource Allocation (Employees), Teacher Collaboration and Data Use (Employees).");

        return new DataDescription(descriptions);
    }

    public static class DataDescription {
        private Map<String, String> _descriptions;

        public DataDescription(Map<String, String> descriptions) {
            if (descriptions != null) {
                _descriptions = descriptions;
            } else {
                _descriptions = new HashMap<String, String>();
            }
        }

        public String get(String key) {
            return _descriptions.get(key);
        }

        public Map<String, String> getDescriptions() {
            return _descriptions;
        }
    }
}
