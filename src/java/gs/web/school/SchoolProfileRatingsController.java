package gs.web.school;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.*;
import gs.data.test.rating.CityRating2;
import gs.data.test.rating.ICityRating2Dao;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilyResolver;
import gs.web.IControllerFamilySpecifier;
import gs.web.request.RequestInfo;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

// TODO-13012 validate simplifying assumption -- behavior switches between Milwaukee, DC, and Indianapolis use state only, not city
// TODO-13012 add logic to handle incomplete data, e.g. only some climate ratings

@Controller
@RequestMapping("/school/profileRatings.page")
public class SchoolProfileRatingsController extends AbstractSchoolProfileController implements ReadWriteAnnotationController, IControllerFamilySpecifier {
    private static final Log _log = LogFactory.getLog(SchoolProfileRatingsController.class);
    public static final String VIEW = "school/profileRatings";
    public static final String MOBILE_VIEW = "school/profileRatings-mobile";
    private ControllerFamily _controllerFamily;

    @Autowired
    private ICityRating2Dao _cityRating2Dao;

    @Autowired
    private SchoolProfileDataHelper _schoolProfileDataHelper;

    // ===================== COPY ===================================

    public static final String CLIMATE_RATING_AVAILABILITY_TEXT_DC =
            "Coming 2013";
    public static final String CLIMATE_RATING_AVAILABILITY_TEXT_IN =
            "Coming soon";
    public static final String CLIMATE_RATING_AVAILABILITY_TEXT_WI =
            "Not available";

    public static final String SECTION_1_COPY_DC =
            "This GreatSchools Rating is based on academics, including students' test scores, " +
                    "academic growth and college readiness. Soon, the rating will also measure school climate," +
                    " which includes safety, cleanliness, parent involvement and more.";
    public static final String SECTION_1_COPY_IN =
            "This school's GreatSchools Rating is based on academics (90%) and climate (10%). " +
                    "The academic rating measures students' test scores, academic growth and college readiness." +
                    " The climate rating measures safety, cleanliness, parent involvement and more.";
    public static final String SECTION_1_COPY_WI =
            "This school's GreatSchools Rating is based on academics (90%) and climate (10%). " +
                    "The academic rating measures students' test scores, academic growth and college readiness." +
                    " The climate rating measures safety, cleanliness, parent involvement and more.";

    public static final String SECTION_3_COPY =
            "The academic rating is made up of equally-weighted parts: students' test scores, their academic growth " +
                    "(for elementary and middle schools) and their readiness for college (for high schools). " +
                    "The graphs below compare this school's results in each area to other schools in the city and state. "+
                    "Growth and college readiness ratings are coming soon, pending publication of 2012 data.";
    public static final String SECTION_3_COPY_DATA_UNAVAILABLE =
            "The academic rating is made up of equally-weighted parts: students' test scores, their academic growth " +
                    "(for elementary and middle schools) and their readiness for college (for high schools). " +
                    "Unfortunately, this school doesn't have sufficient data to generate an academic rating.";

    public static final String TEST_SCORE_RATING_SOURCE_DC =
            "Test scores are based on <a href=\"/students/local-facts-resources/453-testing-in-DC.gs\">2011 DC-CAS</a> results" +
                    " from the District of Columbia.";
    public static final String TEST_SCORE_RATING_SOURCE_IN =
            "Test scores are based on <a href=\"/students/local-facts-resources/442-testing-in-IN.gs\">2011 ISTEP and ECA</a> results" +
                    " from the state of Indiana.";
    public static final String TEST_SCORE_RATING_SOURCE_WI =
            "Test scores are based on the <a href=\"/students/local-facts-resources/445-testing-in-WI.gs\">2011 WSAS</a> results" +
                    " from the state of Wisconsin.";

    public static final String STUDENT_GROWTH_RATING_SOURCE_DC =
            "The academic growth rating measures how schools affect student test score improvement over time in reading and math." +
                    "  The data is for the 2011-12 school year and is provided by the Office of the State Superintendent of Education.";
    public static final String STUDENT_GROWTH_RATING_SOURCE_IN =
            "The academic growth rating measures how schools affect student test score improvement over time in reading and math." +
                    " Data is from the 2011-12 school year and is provided by the Indiana Department of Education.";
    public static final String STUDENT_GROWTH_RATING_SOURCE_WI =
            "The academic growth rating measures how schools affect student test score improvement over time in reading and math." +
                    " This data is from 2011 and is provided by the Value-Added Research Center and Milwaukee Public Schools.";

    public static final String POST_SECONDARY_READINESS_RATING_SOURCE_DC =
            "This rating is based on the average SAT score for 12th graders from the 2011-12 school year." +
                    "  Data is provided by the Office of the State Superintendent of Education";
    public static final String POST_SECONDARY_READINESS_RATING_SOURCE_IN =
            "This rating is based on the average SAT scores of students that graduated in 2012. " +
                    "ACT scores were used if more students took that test. Data is provided by the Indiana Department of Education.";
    public static final String POST_SECONDARY_READINESS_RATING_SOURCE_WI =
            "This rating is based on composite ACT scores for all 12th graders in 2011-12. This rating takes into account" +
                    " how many students took the ACT, giving more credit to schools with a higher percentage of graduates" +
                    " taking the ACT exam. The ACT data is provided by Milwaukee Public Schools";

    public static final String SECTION_4_COPY_DC =
            "Starting in fall 2013, we plan to release a climate rating as part of this school's overall GreatSchools Rating." +
                    " The climate rating will be based on survey data about various aspects of this school's climate, such as safety, " +
                    "cleanliness, expectations for students, parent involvement, and more.";
    public static final String SECTION_4_COPY_IN =
            "Starting in fall 2012, we plan to include a climate rating as part of this school's overall GreatSchools Rating." +
                    " The climate rating will be based on feedback from teachers about various aspects of their school's climate," +
                    " such as safety, cleanliness, expectations for students, parent involvement, and more.";
    public static final String SECTION_4_COPY_WI =
            "This rating encompasses five elements of school climate: safety and cleanliness, respect and " +
                    "relationships, expectations for students, teacher collaboration and support, and parent " +
                    "involvement. This school's climate ratings are the result of GreatSchools' analysis of teacher " +
                    "survey data from the Spring 2011 School Climate Survey developed by Milwaukee Public Schools.";
    public static final String SECTION_4_COPY_DATA_UNAVAILABLE =
           // "Unfortunately, this school didn't provide enough survey responses to generate a climate rating.";
             "Climate rating coming soon.";
    public static final String SECTION_4_SCHOOL_ENVIRONMENT_COPY="This rating evaluates a school's " +
            "environment, based on its safety, order, cleanliness and more.  More highly rated schools have well-kept facilities " +
            "and a safe environment conducive to learning. Schools rated poorly may have a chaotic environment, conflicts among " +
            "students or even theft or violence.";
    public static final String SECTION_4_SOCIAL_EMOTIONAL_LEARNING_COPY="This rating measures whether the school has a positive " +
            "learning environment and cultivates an atmosphere of respect. " +
            "At a school with a higher rating, it's more likely that the school's culture celebrates hard work and learning," +
            " students treat their peers and teachers with respect and class lessons reinforce character strengths such " +
            "as kindness and tolerance. A school with a lower rating may have a weaker learning environment or " +
            "allow disrespectful behavior.";
    public static final String SECTION_4_HIGH_EXPECTATIONS_COPY="This rating sheds light on the academic expectations that" +
            " teachers have for students. At a school with a higher rating, educators are more likely to stress academic success," +
            " ask kids to work hard and expect kids to be college-bound. At schools with lower ratings, it may be more acceptable" +
            " for students to put in average or minimal effort, perform poorly on tests and lack strong academic goals.";
    public static final String SECTION_4_TEACHER_SUPPORT_COPY="This rating indicates how teachers feel about their school's professional environment." +
            " At a highly rated school, teachers are more likely to work well together, learn from one another," +
            " have opportunities for professional development and feel supported by the administration." +
            " At a school with lower ratings, teachers may not interact much, feel appreciated or have much" +
            " input in school decisions and policies.";
    public static final String SECTION_4_FAMILY_ENGAGEMENT_COPY="This rating reflects how much communication parents can" +
            " expect from this school. A highly rated school is more likely to have regular communication " +
            "(e.g. newsletters, emails, and meetings) between administrators, teachers and parents. " +
            "This may include information about student progress, homework help and volunteer opportunities. " +
            "At lower-rated schools, parents may not get regular updates and may feel less welcome at school.";

    // ===================== MODEL ==================================

    public static final String MODEL_OVERALL_RATING = "overallRating";
    public static final String MODEL_OVERALL_ACADEMIC_RATING = "overallAcademicRating";
    public static final String MODEL_OVERALL_ACADEMIC_RATING_LABEL = "overallAcademicRatingLabel";
    public static final String MODEL_OVERALL_CLIMATE_RATING = "overallClimateRating";
    public static final String MODEL_OVERALL_CLIMATE_RATING_LABEL = "overallClimateRatingLabel";

    public static final String MODEL_CLIMATE_RATING_AVAILABILITY_TEXT = "climateRatingAvailabilityText";

    public static final String MODEL_SECTION_1_COPY = "section1Copy";

    public static final String MODEL_SECTION_3_COPY = "section3Copy";
    public static final String MODEL_SECTION_3_COPY_POST_SECONDARY_READINESS = "section3CopyPostSecondaryReadiness";

    public static final String MODEL_TEST_SCORE_RATING_YEAR = "testScoreRatingYear";
    public static final String MODEL_SCHOOL_TEST_SCORE_RATING = "schoolTestScoreRating";
    public static final String MODEL_CITY_TEST_SCORE_RATING = "cityTestScoreRating";
    public static final String MODEL_STATE_TEST_SCORE_RATING = "stateTestScoreRating";
    public static final String MODEL_SHOW_STATE_TEST_SCORE_RATING = "showStateTestScoreRating";

    public static final String MODEL_STUDENT_GROWTH_RATING_YEAR = "studentGrowthRatingYear";
    public static final String MODEL_SCHOOL_STUDENT_GROWTH_RATING = "schoolStudentGrowthRating";
    public static final String MODEL_CITY_STUDENT_GROWTH_RATING = "cityStudentGrowthRating";
    public static final String MODEL_STATE_STUDENT_GROWTH_RATING = "stateStudentGrowthRating";
    public static final String MODEL_SHOW_STATE_STUDENT_GROWTH_RATING = "showStateStudentGrowthRating";
    public static final String MODEL_SCHOOL_STUDENT_GROWTH_RATING_BREAKDOWN_MAP = "schoolStudentGrowthRatingBreakdown"; // TestDataType.id = 165 with Reading and Math as subjects

    public static final String MODEL_POST_SECONDARY_READINESS_RATING_YEAR = "postSecondaryReadinessRatingYear";
    public static final String MODEL_SCHOOL_POST_SECONDARY_READINESS_RATING = "schoolPostSecondaryReadinessRating";
    public static final String MODEL_CITY_POST_SECONDARY_READINESS_RATING = "cityPostSecondaryReadinessRating";
    public static final String MODEL_STATE_POST_SECONDARY_READINESS_RATING = "statePostSecondaryReadinessRating";
    public static final String MODEL_SHOW_STATE_POST_SECONDARY_READINESS_RATING  = "showStatePostSecondaryReadinessRating";
    public static final String MODEL_POST_SECONDARY_READINESS_BREAKDOWN_TEST_NAME = "postSecondaryReadinessBreakdownTestName";
    public static final String MODEL_POST_SECONDARY_READINESS_BREAKDOWN_TEST_SCORE = "postSecondaryReadinessBreakdownTestScore";
    public static final String MODEL_POST_SECONDARY_READINESS_BREAKDOWN_PERCENT_TESTED = "postSecondaryReadinessBreakdownPercentTested";

    public static final String MODEL_TEST_SCORE_RATING_SOURCE = "testScoreRatingSource";

    public static final String MODEL_STUDENT_GROWTH_RATING_SOURCE = "studentGrowthRatingSource";

    public static final String MODEL_POST_SECONDARY_READINESS_RATING_SOURCE = "postSecondaryReadinessSource";

    public static final String MODEL_SHOW_CLIMATE_RATING_DETAILS = "showClimateRatingsDetails";
    public static final String MODEL_CLIMATE_RATING_NUM_RESPONSES = "climateRatingNumResponses";

    public static final String MODEL_SECTION_4_COPY = "section4Copy";

    public static final String MODEL_SCHOOL_ENVIRONMENT_RATING = "schoolEnvironmentRating";
    public static final String MODEL_SOCIAL_EMOTIONAL_LEARNING_RATING = "socialEmotionalLearningRating";
    public static final String MODEL_HIGH_EXPECTATIONS_RATING = "highExpectationsRating";
    public static final String MODEL_TEACHER_SUPPORT_RATING = "teacherSupportRating";
    public static final String MODEL_FAMILY_ENGAGEMENT_RATING = "familyEngagementRating";

    public static final String MODEL_SECTION_4_FAMILY_ENGAGEMENT_COPY = "familyEngagementCopy";
    public static final String MODEL_SECTION_4_HIGH_EXPECTATIONS_COPY = "highExpectationsCopy";
    public static final String MODEL_SECTION_4_SCHOOL_ENVIRONMENT_COPY = "SchoolEnvironmentCopy";
    public static final String MODEL_SECTION_4_SOCIAL_EMOTIONAL_LEARNING_COPY = "socialEmotionalLearningCopy";
    public static final String MODEL_SECTION_4_TEACHER_SUPPORT_COPY = "teacherSupportCopy";


    // ===================== DATA ===================================

    public static final String DATA_OVERALL_RATING = "overallRating"; // TestDataType.id = 174
    public static final String DATA_OVERALL_ACADEMIC_RATING = "overallAcademicRating"; // TestDataType.id = 167
    public static final String DATA_OVERALL_CLIMATE_RATING = "overallClimateRating"; // TestDataType.id = 173

    public static final String DATA_TEST_SCORE_RATING_YEAR = "testScoreRatingYear"; // TestDataType.id = 164 (TestDataSchoolValue.year)
    public static final String DATA_SCHOOL_TEST_SCORE_RATING = "schoolTestScoreRating";  // TestDataType.id = 164
    public static final String DATA_CITY_TEST_SCORE_RATING = "cityTestScoreRating"; // TBD
    public static final String DATA_STATE_TEST_SCORE_RATING = "stateTestScoreRating";  // TestDataType.id = 164

    public static final String DATA_STUDENT_GROWTH_RATING_YEAR = "studentGrowthRatingYear"; // TestDataType.id = 165 (TestDataSchoolValue.year)
    public static final String DATA_SCHOOL_STUDENT_GROWTH_RATING = "schoolStudentGrowthRating"; // TestDataType.id = 165
    public static final String DATA_CITY_STUDENT_GROWTH_RATING = "cityStudentGrowthRating"; // TBD
    public static final String DATA_STATE_STUDENT_GROWTH_RATING = "stateStudentGrowthRating"; // TestDataType.id = 165
    public static final String DATA_SCHOOL_STUDENT_GROWTH_RATING_BREAKDOWN_MAP = "schoolStudentGrowthRatingBreakdown"; // TestDataType.id = 165 with Reading and Math as subjects

    public static final String DATA_POST_SECONDARY_READINESS_RATING_YEAR = "postSecondaryReadinessRatingYear"; // TestDataType.id = 166 (TestDataSchoolValue.year)
    public static final String DATA_SCHOOL_POST_SECONDARY_READINESS_RATING = "schoolPostSecondaryReadinessRating"; // TestDataType.id = 166
    public static final String DATA_CITY_POST_SECONDARY_READINESS_RATING = "cityPostSecondaryReadinessRating"; // TBD
    public static final String DATA_STATE_POST_SECONDARY_READINESS_RATING = "statePostSecondaryReadinessRating"; // TestDataType.id = 166
    public static final String DATA_SCHOOL_ACT_SCORE = "schoolACTScore"; // TestDataType.id = 120
    public static final String DATA_SCHOOL_ACT_PERCENT_TAKING_TEST = "schoolACTPercentTakingTest"; // TestDataType.id = 175
    public static final String DATA_SCHOOL_SAT_SCORE = "schoolSATScore"; // TestDataType.id = 177
    public static final String DATA_SCHOOL_SAT_PERCENT_TAKING_TEST = "schoolSATPercentTakingTest"; // TestDataType.id = 176

    public static final String DATA_CLIMATE_RATING_NUM_RESPONSES = "climateRatingNumResponses"; // TestDataType.id = 173 (TestDataSchoolValue.number_tested)
    public static final String DATA_SCHOOL_ENVIRONMENT_RATING = "schoolEnvironmentRating"; // TestDataType.id = 172
    public static final String DATA_SOCIAL_EMOTIONAL_LEARNING_RATING = "socialEmotionalLearningRating"; // TestDataType.id = 172
    public static final String DATA_HIGH_EXPECTATIONS_RATING = "highExpectationsRating"; // TestDataType.id = 168
    public static final String DATA_TEACHER_SUPPORT_RATING = "teacherSupportRating"; // TestDataType.id = 170
    public static final String DATA_FAMILY_ENGAGEMENT_RATING = "familyEngagementRating"; // TestDataType.id = 169


    // ===================== REQUEST HANDLERS =======================

    @RequestMapping(method=RequestMethod.GET)
    public String showRatingsPage(ModelMap modelMap,
                                     HttpServletRequest request) {

        School school = getSchool(request);
        modelMap.put("school", school);

        Map<String,Object> dataMap = getData(school,request);

        populateSection1Model(school, dataMap, modelMap);

        // no dynamic data in section 2

        populateSection3Model(school, dataMap, modelMap);

        populateSection4Model(school, dataMap, modelMap);

        // need to check which view to return
        RequestInfo requestInfo = RequestInfo.getRequestInfo(request);
        if (requestInfo != null && requestInfo.shouldRenderMobileView()) {
            return MOBILE_VIEW;
        }

        return VIEW;
    }

    // ===================== Data ===================================

    public Map<String,Object> getData(School school,HttpServletRequest request) {

        //Get school and state ratings data
        Map<String, Object> dataMap =  _schoolProfileDataHelper.getGsRatings(request);

        if(dataMap == null){
          return new HashMap<String, Object>();
        }

        // must be run after school data is fetched
        if (dataMap.containsKey(DATA_SCHOOL_TEST_SCORE_RATING) || dataMap.containsKey(DATA_SCHOOL_STUDENT_GROWTH_RATING)) {

            //Get city ratings data
            populateCityData(school,dataMap);
        }

        return dataMap;
    }

    public void populateCityData(School school, Map<String, Object> dataMap) {

        List<CityRating2> ratings = _cityRating2Dao.getLatestCityRatingsByCity(school.getDatabaseState(), school.getCity());
        if (ratings != null) {
            for (CityRating2 rating : ratings) {
                switch (rating.getDataTypeId()) {
                    // overall ratings
                    case TestDataType.RATING_ACADEMIC_ACHIEVEMENT :
                        dataMap.put(DATA_CITY_TEST_SCORE_RATING, rating.getRating());
                        break;
                    case TestDataType.RATING_ACADEMIC_VALUE_ADDED :
                        dataMap.put(DATA_CITY_STUDENT_GROWTH_RATING, rating.getRating());
                        break;
                    case TestDataType.RATING_ACADEMIC_POST_SECONDARY_READINESS :
                        dataMap.put(DATA_CITY_POST_SECONDARY_READINESS_RATING, rating.getRating());
                        break;
                }
            }
        }

    }

    // ===================== Section 1 ==============================
    public static void populateSection1Model(School school, Map<String,Object> dataMap ,ModelMap model) {

        // OVERALL RATING

        Object overallRating = dataMap.get(DATA_OVERALL_RATING);
        if (overallRating != null) {
            model.put(MODEL_OVERALL_RATING, overallRating);
        } else {
            model.put(MODEL_OVERALL_RATING, null);
        }

        // ACADEMIC RATING

        Object overallAcademicRating = dataMap.get(DATA_OVERALL_ACADEMIC_RATING);
        if (overallAcademicRating != null) {
            model.put(MODEL_OVERALL_ACADEMIC_RATING, overallAcademicRating);
            // TODO-13012 replace with better call to more permanent rating-to-label helper method for new rating; fix object->string->float->string conversion
            model.put(MODEL_OVERALL_ACADEMIC_RATING_LABEL,
                    getLabelForAcademicRating(Float.valueOf(overallAcademicRating.toString())));
        }

        // CLIMATE RATING

        Object overallClimateRating = dataMap.get(DATA_OVERALL_CLIMATE_RATING);
        boolean hasClimateRating = (overallClimateRating != null);
        if ((State.DC.equals(school.getDatabaseState()) || State.IN.equals(school.getDatabaseState())) ||
            (State.WI.equals(school.getDatabaseState()) && !hasClimateRating)) {
            model.put(MODEL_CLIMATE_RATING_AVAILABILITY_TEXT, getClimateRatingAvailabilityText(school));
        } else if (hasClimateRating) {
            model.put(MODEL_OVERALL_CLIMATE_RATING, overallClimateRating);
            // TODO-13012 replace with better call to more permanent rating-to-label helper method for new rating; fix object->string->float->string conversion
            model.put(MODEL_OVERALL_CLIMATE_RATING_LABEL,
                    getLabelForClimateRating(Float.valueOf(overallClimateRating.toString())));
        }

        // SECTION 1 COPY

        model.put(MODEL_SECTION_1_COPY, getSection1Copy(school));
    }

    public static String getClimateRatingAvailabilityText(School school) {
        if (State.DC.equals(school.getDatabaseState())) {
            return CLIMATE_RATING_AVAILABILITY_TEXT_DC;
        } else if (State.IN.equals(school.getDatabaseState())) {
            return CLIMATE_RATING_AVAILABILITY_TEXT_IN;
        } else if (State.WI.equals(school.getDatabaseState())) {
            return CLIMATE_RATING_AVAILABILITY_TEXT_WI;
        } else {
            throw new IllegalArgumentException("School is from unsupported state");
        }
    }

    public static String getSection1Copy(School school) {
        if (State.DC.equals(school.getDatabaseState())) {
            return SECTION_1_COPY_DC;
        } else if (State.IN.equals(school.getDatabaseState())) {
            return SECTION_1_COPY_IN;
        } else if (State.WI.equals(school.getDatabaseState())) {
            return SECTION_1_COPY_WI;
        } else {
            throw new IllegalArgumentException("School is from unsupported state");
        }
    }

    // ===================== SECTIONS -==============================

    // ===================== Section 2 ==============================

    // no code needed here

    // ===================== Section 3 ==============================

    public static void populateSection3Model(School school, Map<String,Object> dataMap,ModelMap model) {

        // SECTION 3 COPY
        model.put(MODEL_SECTION_3_COPY, getSection3Copy(dataMap));

        // SECTION 3 CHARTS

        // test score ratings

        boolean showStateTestScoreRating = isShowStateTestScoreRating(school.getDatabaseState());
        model.put(MODEL_SHOW_STATE_TEST_SCORE_RATING, showStateTestScoreRating);
        populateTestScoreRatingsModel(school, showStateTestScoreRating, dataMap, model);

        // student growth ratings

        boolean showStateStudentGrowthRating = isShowStateStudentGrowthRating(school.getDatabaseState());
        model.put(MODEL_SHOW_STATE_STUDENT_GROWTH_RATING, showStateStudentGrowthRating);
        populateStudentGrowthRatingsModel(school, showStateStudentGrowthRating, dataMap, model);

        // post-secondary readiness ratings

        boolean showStatePostSecondaryReadinessGrowthRating = isShowStatePostSecondaryReadinessRating(school.getDatabaseState());
        model.put(MODEL_SHOW_STATE_POST_SECONDARY_READINESS_RATING, showStatePostSecondaryReadinessGrowthRating);
        populatePostSecondaryReadinessRatingsModel(school, showStatePostSecondaryReadinessGrowthRating, dataMap, model);

        // SECTION 3 SOURCES

        model.put(MODEL_TEST_SCORE_RATING_SOURCE, getTestScoreRatingSource(school));

        model.put(MODEL_STUDENT_GROWTH_RATING_SOURCE, getStudentGrowthRatingSource(school));

        model.put(MODEL_POST_SECONDARY_READINESS_RATING_SOURCE, getPostSecondaryReadinessRatingSource(school));

    }

    public static boolean isShowStateTestScoreRating(State state) {
        return !State.DC.equals(state);
    }

    public static boolean isShowStateStudentGrowthRating(State state) {
        return !State.DC.equals(state);
    }

    public static boolean isShowStatePostSecondaryReadinessRating(State state) {
        return !State.DC.equals(state);
    }

    public static boolean isShowClimateRatingDetails(State state) {
        return State.WI.equals(state);
    }

    public static void populateTestScoreRatingsModel(School school, boolean showStateRating, Map<String,Object> dataMap,ModelMap model) {

        if (dataMap.containsKey(DATA_SCHOOL_TEST_SCORE_RATING)) {

            model.put(MODEL_TEST_SCORE_RATING_YEAR, dataMap.get(DATA_TEST_SCORE_RATING_YEAR));
            model.put(MODEL_SCHOOL_TEST_SCORE_RATING, dataMap.get(DATA_SCHOOL_TEST_SCORE_RATING));
            model.put(MODEL_CITY_TEST_SCORE_RATING, dataMap.get(DATA_CITY_TEST_SCORE_RATING));

            if (showStateRating) {
                model.put(MODEL_STATE_TEST_SCORE_RATING, dataMap.get(DATA_STATE_TEST_SCORE_RATING));
            }
        }
    }

    public static void populateStudentGrowthRatingsModel(School school, boolean showStateRating, Map<String,Object> dataMap,ModelMap model) {

        if (dataMap.containsKey(DATA_SCHOOL_STUDENT_GROWTH_RATING )) {
            model.put(MODEL_STUDENT_GROWTH_RATING_YEAR, dataMap.get(DATA_STUDENT_GROWTH_RATING_YEAR));
            model.put(MODEL_SCHOOL_STUDENT_GROWTH_RATING, dataMap.get(DATA_SCHOOL_STUDENT_GROWTH_RATING));
            model.put(MODEL_CITY_STUDENT_GROWTH_RATING, dataMap.get(DATA_CITY_STUDENT_GROWTH_RATING));
            //Add the Student Growth breakdown data for subjects.
            if(dataMap.containsKey(DATA_SCHOOL_STUDENT_GROWTH_RATING_BREAKDOWN_MAP)){
                model.put(MODEL_SCHOOL_STUDENT_GROWTH_RATING_BREAKDOWN_MAP,dataMap.get(DATA_SCHOOL_STUDENT_GROWTH_RATING_BREAKDOWN_MAP));
            }
            if (showStateRating) {
                model.put(MODEL_STATE_STUDENT_GROWTH_RATING, dataMap.get(DATA_STATE_STUDENT_GROWTH_RATING));
            }
        }
    }

    public static void populatePostSecondaryReadinessRatingsModel(School school,boolean showStateRating, Map<String,Object> dataMap,ModelMap model) {

        if (dataMap.containsKey(DATA_SCHOOL_POST_SECONDARY_READINESS_RATING) &&
                school.getLevelCode() != null &&
                school.getLevelCode().containsLevelCode(LevelCode.Level.HIGH_LEVEL)) {

            model.put(MODEL_POST_SECONDARY_READINESS_RATING_YEAR, dataMap.get(DATA_POST_SECONDARY_READINESS_RATING_YEAR));
            model.put(MODEL_SCHOOL_POST_SECONDARY_READINESS_RATING, dataMap.get(DATA_SCHOOL_POST_SECONDARY_READINESS_RATING));
            model.put(MODEL_CITY_POST_SECONDARY_READINESS_RATING, dataMap.get(DATA_CITY_POST_SECONDARY_READINESS_RATING));
            if (showStateRating) {
                model.put(MODEL_STATE_POST_SECONDARY_READINESS_RATING, dataMap.get(DATA_STATE_POST_SECONDARY_READINESS_RATING));
            }

            //Add the Post Secondary Readiness breakdown data for ACT and SAT tests.
            //School can have only ACT score or SAT score but not both.
            if (dataMap.containsKey(DATA_SCHOOL_ACT_SCORE) && dataMap.containsKey(DATA_SCHOOL_ACT_PERCENT_TAKING_TEST)) {
                model.put(MODEL_POST_SECONDARY_READINESS_BREAKDOWN_TEST_SCORE, dataMap.get(DATA_SCHOOL_ACT_SCORE));
                model.put(MODEL_POST_SECONDARY_READINESS_BREAKDOWN_TEST_NAME, "ACT");
                model.put(MODEL_POST_SECONDARY_READINESS_BREAKDOWN_PERCENT_TESTED, dataMap.get(DATA_SCHOOL_ACT_PERCENT_TAKING_TEST));
            } else if (dataMap.containsKey(DATA_SCHOOL_SAT_SCORE) && dataMap.containsKey(DATA_SCHOOL_SAT_PERCENT_TAKING_TEST)) {
                model.put(MODEL_POST_SECONDARY_READINESS_BREAKDOWN_TEST_SCORE, dataMap.get(DATA_SCHOOL_SAT_SCORE));
                model.put(MODEL_POST_SECONDARY_READINESS_BREAKDOWN_TEST_NAME, "SAT");
                model.put(MODEL_POST_SECONDARY_READINESS_BREAKDOWN_PERCENT_TESTED, dataMap.get(DATA_SCHOOL_SAT_PERCENT_TAKING_TEST));
            }
        }
    }

    public static String getTestScoreRatingSource(School school) {
        if (State.DC.equals(school.getDatabaseState())) {
            return TEST_SCORE_RATING_SOURCE_DC;
        } else if (State.IN.equals(school.getDatabaseState())) {
            return TEST_SCORE_RATING_SOURCE_IN;
        } else if (State.WI.equals(school.getDatabaseState())) {
            return TEST_SCORE_RATING_SOURCE_WI;
        } else {
            throw new IllegalArgumentException("School is from unsupported state");
        }
    }

    public static String getStudentGrowthRatingSource(School school) {
        if (State.DC.equals(school.getDatabaseState())) {
            return STUDENT_GROWTH_RATING_SOURCE_DC;
        } else if (State.IN.equals(school.getDatabaseState())) {
            return STUDENT_GROWTH_RATING_SOURCE_IN;
        } else if (State.WI.equals(school.getDatabaseState())) {
            return STUDENT_GROWTH_RATING_SOURCE_WI;
        } else {
            throw new IllegalArgumentException("School is from unsupported state");
        }
    }

    public static String getPostSecondaryReadinessRatingSource(School school) {
        if (State.DC.equals(school.getDatabaseState())) {
            return POST_SECONDARY_READINESS_RATING_SOURCE_DC;
        } else if (State.IN.equals(school.getDatabaseState())) {
            return POST_SECONDARY_READINESS_RATING_SOURCE_IN;
        } else if (State.WI.equals(school.getDatabaseState())) {
            return POST_SECONDARY_READINESS_RATING_SOURCE_WI;
        } else {
            throw new IllegalArgumentException("School is from unsupported state");
        }
    }

    public static String getSection3Copy(Map<String,Object> dataMap) {
        if (dataMap.containsKey(DATA_OVERALL_ACADEMIC_RATING)) {
            return SECTION_3_COPY;
        } else {
            return SECTION_3_COPY_DATA_UNAVAILABLE;
        }
    }

    // ===================== Section 4 ==============================

    public static void populateSection4Model(School school, Map<String, Object> dataMap, ModelMap model) {

        // SECTION 4 COPY

        model.put(MODEL_SECTION_4_COPY, getSection4Copy(school, dataMap));
        model.put(MODEL_SECTION_4_FAMILY_ENGAGEMENT_COPY,SECTION_4_FAMILY_ENGAGEMENT_COPY);
        model.put(MODEL_SECTION_4_HIGH_EXPECTATIONS_COPY,SECTION_4_HIGH_EXPECTATIONS_COPY);
        model.put(MODEL_SECTION_4_SCHOOL_ENVIRONMENT_COPY,SECTION_4_SCHOOL_ENVIRONMENT_COPY);
        model.put(MODEL_SECTION_4_SOCIAL_EMOTIONAL_LEARNING_COPY,SECTION_4_SOCIAL_EMOTIONAL_LEARNING_COPY);
        model.put(MODEL_SECTION_4_TEACHER_SUPPORT_COPY,SECTION_4_TEACHER_SUPPORT_COPY);

        // SECTION 4 CLIMATE DETAILS

        boolean showClimateRatingDetails = isShowClimateRatingDetails(school.getDatabaseState());
        Map<String, Object> climateRatingDetailsMap = getClimateRatingDetailsModel(school, dataMap);
        showClimateRatingDetails = showClimateRatingDetails && climateRatingDetailsMap != null && !climateRatingDetailsMap.isEmpty();
        model.put(MODEL_SHOW_CLIMATE_RATING_DETAILS, showClimateRatingDetails);

        if (showClimateRatingDetails) {
            model.putAll(climateRatingDetailsMap);
        }

    }

    public static String getSection4Copy(School school, Map<String, Object> dataMap) {
        if (State.DC.equals(school.getDatabaseState())) {
            return SECTION_4_COPY_DC;
        } else if (State.IN.equals(school.getDatabaseState())) {
            return SECTION_4_COPY_IN;
        } else if (State.WI.equals(school.getDatabaseState())) {
            StringBuilder s = new StringBuilder(SECTION_4_COPY_WI);
            if (!dataMap.containsKey(DATA_OVERALL_CLIMATE_RATING)) {
                s.append(" ");
                s.append(SECTION_4_COPY_DATA_UNAVAILABLE);
            }
            return s.toString();
        } else {
            throw new IllegalArgumentException("School is from unsupported state");
        }
    }

    public static Map<String,Object> getClimateRatingDetailsModel(School school, Map<String,Object> dataMap) {
        Map<String,Object> model = new HashMap<String,Object>();

        if (dataMap.containsKey(DATA_OVERALL_CLIMATE_RATING)) {
            model.put(MODEL_CLIMATE_RATING_NUM_RESPONSES, dataMap.get(DATA_CLIMATE_RATING_NUM_RESPONSES));

            model.put(MODEL_SCHOOL_ENVIRONMENT_RATING, dataMap.get(DATA_SCHOOL_ENVIRONMENT_RATING));

            model.put(MODEL_SOCIAL_EMOTIONAL_LEARNING_RATING, dataMap.get(DATA_SOCIAL_EMOTIONAL_LEARNING_RATING));

            model.put(MODEL_HIGH_EXPECTATIONS_RATING, dataMap.get(DATA_HIGH_EXPECTATIONS_RATING));

            model.put(MODEL_TEACHER_SUPPORT_RATING, dataMap.get(DATA_TEACHER_SUPPORT_RATING));

            model.put(MODEL_FAMILY_ENGAGEMENT_RATING, dataMap.get(DATA_FAMILY_ENGAGEMENT_RATING));
        }

        return model;
    }

    // ===================== UTILITY METHODS ========================

    // TODO-13012 temporary: to be replaced with a more global helper/util method for new ratings
    public static String getLabelForAcademicRating(double rating) {
        if (rating >= 1 && rating <= 3) {
            return "Poor";
        } else if (rating >= 4 && rating <= 7) {
            return "Average";
        } else if (rating >= 8 && rating <= 10) {
            return "High";
        } else {
            throw new IllegalArgumentException("Rating must be from 1 to 10");
        }
    }

    // TODO-13012 temporary: to be replaced with a more global helper/util method for new ratings
    public static String getLabelForClimateRating(double climateRating) {
        if (climateRating >= 1 && climateRating <= 3) {
            return "Poor";
        } else if (climateRating >= 4 && climateRating <= 7) {
            return "Average";
        } else if (climateRating >= 8 && climateRating <= 10) {
            return "High";
        } else {
            throw new IllegalArgumentException("Rating must be from 1 to 10");
        }
    }

    // ===================== SETTERS FOR UNIT TESTS =================

    public void setCityRating2Dao(ICityRating2Dao cityRating2Dao) {
        _cityRating2Dao = cityRating2Dao;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }
}