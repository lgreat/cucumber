package gs.web.school;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.*;
import gs.data.test.rating.CityRating2;
import gs.data.test.rating.ICityRating2Dao;
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
public class SchoolProfileRatingsController extends AbstractSchoolProfileController implements ReadWriteAnnotationController {
    private static final Log _log = LogFactory.getLog(SchoolProfileRatingsController.class);
    public static final String VIEW = "school/profileRatings";

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

    // TODO-13012 placeholder
    public static final String SECTION_1_COPY_DC =
            "DC - section 1 copy";
    // TODO-13012 placeholder
    public static final String SECTION_1_COPY_IN =
            "IN - section 1 copy";
    // TODO-13012 placeholder
    public static final String SECTION_1_COPY_WI =
            "WI - section 1 copy";

    // TODO-13012 placeholder
    public static final String SECTION_3_COPY_DATA_UNAVAILABLE =
            "Data unavailable - section 3 copy";
    // TODO-13012 placeholder
    public static final String SECTION_3_COPY_DC =
            "DC - section 3 copy";
    // TODO-13012 placeholder
    public static final String SECTION_3_COPY_IN =
            "IN - section 3 copy";
    // TODO-13012 placeholder
    public static final String SECTION_3_COPY_WI =
            "WI - section 3 copy";
    // TODO-13012 placeholder
    public static final String SECTION_3_COPY_POST_SECONDARY_READINESS_DC =
            "DC - section 3 copy about post-secondary readiness";
    // TODO-13012 placeholder
    public static final String SECTION_3_COPY_POST_SECONDARY_READINESS_IN =
            "IN - section 3 copy about post-secondary readiness";
    // TODO-13012 placeholder
    public static final String SECTION_3_COPY_POST_SECONDARY_READINESS_WI =
            "WI - section 3 copy about post-secondary readiness";

    // TODO-13012 placeholder
    public static final String TEST_SCORE_RATING_SOURCE_DC =
            "DC - copy about test score rating source";
    // TODO-13012 placeholder
    public static final String TEST_SCORE_RATING_SOURCE_IN =
            "IN - copy about test score rating source";
    // TODO-13012 placeholder
    public static final String TEST_SCORE_RATING_SOURCE_WI =
            "WI - copy about test score rating source";

    // TODO-13012 placeholder
    public static final String STUDENT_GROWTH_RATING_SOURCE_DC =
            "DC - copy about student growth rating source";
    // TODO-13012 placeholder
    public static final String STUDENT_GROWTH_RATING_SOURCE_IN =
            "IN - copy about student growth rating source";
    // TODO-13012 placeholder
    public static final String STUDENT_GROWTH_RATING_SOURCE_WI =
            "WI - copy about student growth rating source";

    // TODO-13012 placeholder
    public static final String POST_SECONDARY_READINESS_RATING_SOURCE_DC =
            "DC - copy about post-secondary readiness rating source";
    // TODO-13012 placeholder
    public static final String POST_SECONDARY_READINESS_RATING_SOURCE_IN =
            "IN - copy about post-secondary readiness rating source";
    // TODO-13012 placeholder
    public static final String POST_SECONDARY_READINESS_RATING_SOURCE_WI =
            "WI - copy about post-secondary readiness rating source";

    // TODO-13012 placeholder
    public static final String SECTION_4_COPY_DATA_UNAVAILABLE =
            "Data unavailable - section 4 copy";
    // TODO-13012 placeholder
    public static final String SECTION_4_COPY_DC =
            "DC - section 4 copy - no data this year";
    // TODO-13012 placeholder
    public static final String SECTION_4_COPY_IN =
            "IN - section 4 copy - no data yet";
    // TODO-13012 placeholder
    public static final String SECTION_4_COPY_WI =
            "WI - section 4 copy - data is present in the section below";

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

    public static final String MODEL_POST_SECONDARY_READINESS_RATING_YEAR = "postSecondaryReadinessRatingYear";
    public static final String MODEL_SCHOOL_POST_SECONDARY_READINESS_RATING = "schoolPostSecondaryReadinessRating";
    public static final String MODEL_CITY_POST_SECONDARY_READINESS_RATING = "cityPostSecondaryReadinessRating";
    public static final String MODEL_STATE_POST_SECONDARY_READINESS_RATING = "statePostSecondaryReadinessRating";
    public static final String MODEL_SHOW_STATE_POST_SECONDARY_READINESS_RATING  = "showStatePostSecondaryReadinessRating";

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

    public static final String DATA_POST_SECONDARY_READINESS_RATING_YEAR = "postSecondaryReadinessRatingYear"; // TestDataType.id = 166 (TestDataSchoolValue.year)
    public static final String DATA_SCHOOL_POST_SECONDARY_READINESS_RATING = "schoolPostSecondaryReadinessRating"; // TestDataType.id = 166
    public static final String DATA_CITY_POST_SECONDARY_READINESS_RATING = "cityPostSecondaryReadinessRating"; // TBD
    public static final String DATA_STATE_POST_SECONDARY_READINESS_RATING = "statePostSecondaryReadinessRating"; // TestDataType.id = 166

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

        modelMap.addAllAttributes(getSection1Model(school, dataMap));

        // no dynamic data in section 2

        modelMap.addAllAttributes(getSection3Model(school, dataMap));

        modelMap.addAllAttributes(getSection4Model(school, dataMap));

        return VIEW;
    }

    // ===================== Data ===================================

    // TODO-13012 not optimized, may need to be rewritten
    public Map<String,Object> getData(School school,HttpServletRequest request) {
        Map<String,Object> dataMap = new HashMap<String,Object>();

        // TODO-13012 don't hard-code year to fetch
        Set<Integer> years = new HashSet<Integer>();
        years.add(2012);

        //Get school and state ratings data
        Map<String, Object> ratingsMap =  _schoolProfileDataHelper.getGsRatings(request);
        if (ratingsMap != null) {
            dataMap.putAll(ratingsMap);
        }

        // must be run after school data is fetched
        if (dataMap.containsKey(DATA_SCHOOL_TEST_SCORE_RATING) || dataMap.containsKey(DATA_SCHOOL_STUDENT_GROWTH_RATING)) {

            //Get city ratings data
            dataMap.putAll(getCityData(school,years));
        }

        return dataMap;
    }

    public Map<String,Object> getCityData(School school, Set<Integer> years) {
        Map<String,Object> dataMap = new HashMap<String,Object>();

        List<CityRating2> ratings = _cityRating2Dao.getLatestCityRatingsByCity(school.getDatabaseState(), school.getCity());
        if (ratings != null) {
            for (CityRating2 rating : ratings) {
                switch (rating.getDataTypeId()) {
                    // overall ratings
                    case TestDataType.RATING_ACADEMIC_ACHIEVEMENT :
                        dataMap.put(DATA_CITY_TEST_SCORE_RATING, new Float(rating.getRating()).intValue());
                        break;
                    case TestDataType.RATING_ACADEMIC_VALUE_ADDED :
                        dataMap.put(DATA_CITY_STUDENT_GROWTH_RATING, new Float(rating.getRating()).intValue());
                        break;
                    case TestDataType.RATING_ACADEMIC_POST_SECONDARY_READINESS :
                        dataMap.put(DATA_CITY_POST_SECONDARY_READINESS_RATING, new Float(rating.getRating()).intValue());
                        break;
                }
            }
        }

        return dataMap;
    }

    // ===================== Section 1 ==============================

    public static Map<String,Object> getSection1Model(School school, Map<String,Object> dataMap) {
        Map<String,Object> model = new HashMap<String,Object>();

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

        return model;
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

    public static Map<String,Object> getSection3Model(School school, Map<String,Object> dataMap) {
        Map<String,Object> model = new HashMap<String,Object>();

        // SECTION 3 COPY

        model.put(MODEL_SECTION_3_COPY, getSection3Copy(school, dataMap));

        // SECTION 3 POST-SECONDARY READINESS COPY

        model.put(MODEL_SECTION_3_COPY_POST_SECONDARY_READINESS, getSection3CopyPostSecondaryReadiness(school));

        // SECTION 3 CHARTS

        // test score ratings

        boolean showStateTestScoreRating = isShowStateTestScoreRating(school.getDatabaseState());
        model.put(MODEL_SHOW_STATE_TEST_SCORE_RATING, showStateTestScoreRating);
        model.putAll(getTestScoreRatingsModel(school, showStateTestScoreRating, dataMap));

        // student growth ratings

        boolean showStateStudentGrowthRating = isShowStateStudentGrowthRating(school.getDatabaseState());
        model.put(MODEL_SHOW_STATE_STUDENT_GROWTH_RATING, showStateStudentGrowthRating);
        model.putAll(getStudentGrowthRatingsModel(school, showStateStudentGrowthRating, dataMap));

        // post-secondary readiness ratings

        boolean showStatePostSecondaryReadinessGrowthRating = isShowStatePostSecondaryReadinessRating(school.getDatabaseState());
        model.put(MODEL_SHOW_STATE_POST_SECONDARY_READINESS_RATING, showStatePostSecondaryReadinessGrowthRating);
        model.putAll(getPostSecondaryReadinessRatingsModel(school, showStatePostSecondaryReadinessGrowthRating, dataMap));

        // SECTION 3 SOURCES

        model.put(MODEL_TEST_SCORE_RATING_SOURCE, getTestScoreRatingSource(school));

        model.put(MODEL_STUDENT_GROWTH_RATING_SOURCE, getStudentGrowthRatingSource(school));

        model.put(MODEL_POST_SECONDARY_READINESS_RATING_SOURCE, getPostSecondaryReadinessRatingSource(school));

        return model;
    }

    public static boolean isShowStateTestScoreRating(State state) {
        return !State.DC.equals(state);
    }

    public static boolean isShowStateStudentGrowthRating(State state) {
        return !State.DC.equals(state);
    }

    //TODO is this needed?
    public static boolean isShowStatePostSecondaryReadinessRating(State state) {
        return !State.DC.equals(state);
    }

    public static boolean isShowClimateRatingDetails(State state) {
        return State.WI.equals(state);
    }

    public static Map<String,Object> getTestScoreRatingsModel(School school, boolean showStateRating, Map<String,Object> dataMap) {
        Map<String,Object> model = new HashMap<String,Object>();

        if (dataMap.containsKey(DATA_SCHOOL_TEST_SCORE_RATING)) {

            model.put(MODEL_TEST_SCORE_RATING_YEAR, dataMap.get(DATA_TEST_SCORE_RATING_YEAR));
            model.put(MODEL_SCHOOL_TEST_SCORE_RATING, dataMap.get(DATA_SCHOOL_TEST_SCORE_RATING));
            model.put(MODEL_CITY_TEST_SCORE_RATING, dataMap.get(DATA_CITY_TEST_SCORE_RATING));

            if (showStateRating) {
                model.put(MODEL_STATE_TEST_SCORE_RATING, dataMap.get(DATA_STATE_TEST_SCORE_RATING));
            }
        }

        return model;
    }

    public static Map<String,Object> getStudentGrowthRatingsModel(School school, boolean showStateRating, Map<String,Object> dataMap) {
        Map<String,Object> model = new HashMap<String,Object>();

        if (dataMap.containsKey(DATA_SCHOOL_STUDENT_GROWTH_RATING) &&
                school.getLevelCode() != null && !school.getLevelCode().equals(LevelCode.HIGH)) {

            model.put(MODEL_STUDENT_GROWTH_RATING_YEAR, dataMap.get(DATA_STUDENT_GROWTH_RATING_YEAR));
            model.put(MODEL_SCHOOL_STUDENT_GROWTH_RATING, dataMap.get(DATA_SCHOOL_STUDENT_GROWTH_RATING));
            model.put(MODEL_CITY_STUDENT_GROWTH_RATING, dataMap.get(DATA_CITY_STUDENT_GROWTH_RATING));

            if (showStateRating) {
                model.put(MODEL_STATE_STUDENT_GROWTH_RATING, dataMap.get(DATA_STATE_STUDENT_GROWTH_RATING));
            }
        }

        return model;
    }

    public static Map<String,Object> getPostSecondaryReadinessRatingsModel(School school,boolean showStateRating, Map<String,Object> dataMap) {
        Map<String,Object> model = new HashMap<String,Object>();

        if (dataMap.containsKey(DATA_SCHOOL_POST_SECONDARY_READINESS_RATING) &&
                school.getLevelCode() != null &&
                school.getLevelCode().containsLevelCode(LevelCode.Level.HIGH_LEVEL)) {

            model.put(MODEL_POST_SECONDARY_READINESS_RATING_YEAR, dataMap.get(DATA_POST_SECONDARY_READINESS_RATING_YEAR));
            model.put(MODEL_SCHOOL_POST_SECONDARY_READINESS_RATING, dataMap.get(DATA_SCHOOL_POST_SECONDARY_READINESS_RATING));
            model.put(MODEL_CITY_POST_SECONDARY_READINESS_RATING, dataMap.get(DATA_CITY_POST_SECONDARY_READINESS_RATING));
            if (showStateRating) {
                model.put(MODEL_STATE_POST_SECONDARY_READINESS_RATING, dataMap.get(DATA_STATE_POST_SECONDARY_READINESS_RATING));
            }
        }
            
        return model;
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

    public static String getSection3Copy(School school, Map<String,Object> dataMap) {
        if (State.DC.equals(school.getDatabaseState())) {
            return SECTION_3_COPY_DC;
        } else if (State.IN.equals(school.getDatabaseState())) {
            return SECTION_3_COPY_IN;
        } else if (State.WI.equals(school.getDatabaseState())) {
            if (dataMap.containsKey(DATA_OVERALL_ACADEMIC_RATING)) {
                return SECTION_3_COPY_WI;
            } else {
                return SECTION_3_COPY_DATA_UNAVAILABLE;
            }
        } else {
            throw new IllegalArgumentException("School is from unsupported state");
        }
    }

    // this ignores whether or not the school actually has data; we'll leave it to the view
    public static String getSection3CopyPostSecondaryReadiness(School school) {
        LevelCode levelCode = school.getLevelCode();
        if (levelCode != null && levelCode.containsLevelCode(LevelCode.Level.HIGH_LEVEL)) {
            if (State.DC.equals(school.getDatabaseState())) {
                return SECTION_3_COPY_POST_SECONDARY_READINESS_DC;
            } else if (State.IN.equals(school.getDatabaseState())) {
                return SECTION_3_COPY_POST_SECONDARY_READINESS_IN;
            } else if (State.WI.equals(school.getDatabaseState())) {
                return SECTION_3_COPY_POST_SECONDARY_READINESS_WI;
            } else {
                throw new IllegalArgumentException("School is from unsupported state");
            }
        } else {
            return null;
        }
    }

    // ===================== Section 4 ==============================

    public static Map<String,Object> getSection4Model(School school, Map<String,Object> dataMap) {
        Map<String,Object> model = new HashMap<String,Object>();

        // SECTION 4 COPY

        model.put(MODEL_SECTION_4_COPY, getSection4Copy(school, dataMap));

        // SECTION 4 CLIMATE DETAILS

        boolean showClimateRatingDetails = isShowClimateRatingDetails(school.getDatabaseState());
        model.put(MODEL_SHOW_CLIMATE_RATING_DETAILS, showClimateRatingDetails);

        Map<String,Object> climateRatingDetailsMap = getClimateRatingDetailsModel(school, dataMap);
        if (showClimateRatingDetails && climateRatingDetailsMap!= null && !climateRatingDetailsMap.isEmpty()) {
            model.putAll(climateRatingDetailsMap);
        }

        return model;
    }

    public static String getSection4Copy(School school, Map<String,Object> dataMap) {
        if (State.DC.equals(school.getDatabaseState())) {
            return SECTION_4_COPY_DC;
        } else if (State.IN.equals(school.getDatabaseState())) {
            return SECTION_4_COPY_IN;
        } else if (State.WI.equals(school.getDatabaseState())) {
            if (dataMap.containsKey(DATA_OVERALL_CLIMATE_RATING)) {
                return SECTION_4_COPY_WI;
            } else {
                return SECTION_4_COPY_DATA_UNAVAILABLE;
            }
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
}