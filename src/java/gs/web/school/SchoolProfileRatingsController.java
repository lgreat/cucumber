package gs.web.school;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/school/profileRatings.page")
public class SchoolProfileRatingsController extends AbstractSchoolProfileController implements ReadWriteAnnotationController {
    private static final Log _log = LogFactory.getLog(SchoolProfileRatingsController.class);
    public static final String VIEW = "school/profileRatings";

    public static final String MODEL_OVERALL_RATING = "overallRating";
    public static final String MODEL_OVERALL_ACADEMIC_RATING = "overallAcademicRating";
    public static final String MODEL_OVERALL_ACADEMIC_RATING_LABEL = "overallAcademicRatingLabel";
    public static final String MODEL_OVERALL_CLIMATE_RATING = "overallClimateRating";
    public static final String MODEL_OVERALL_CLIMATE_RATING_LABEL = "overallClimateRatingLabel";

    public static final String MODEL_CLIMATE_RATING_AVAILABILITY_TEXT = "climateRatingAvailabilityText";
    public static final String CLIMATE_RATING_AVAILABILITY_TEXT_DC =
            "Coming 2013";
    public static final String CLIMATE_RATING_AVAILABILITY_TEXT_IN =
            "Coming soon";
    public static final String CLIMATE_RATING_AVAILABILITY_TEXT_WI =
            "Not available";

    public static final String MODEL_SECTION_1_COPY = "section1Copy";
    // TODO-13012 placeholder
    public static final String SECTION_1_COPY_DC =
            "DC - section 1 copy - The GreatSchools Rating is composed of two categories, Academic and Climate. " +
            "Academic rating makes up 90% of the total rating, while the Climate rating " +
            "makes up the remaining 10%.";
    // TODO-13012 placeholder
    public static final String SECTION_1_COPY_IN =
            "IN - section 1 copy - The GreatSchools Rating is composed of two categories, Academic and Climate. " +
            "Academic rating makes up 90% of the total rating, while the Climate rating " +
            "makes up the remaining 10%.";
    // TODO-13012 placeholder
    public static final String SECTION_1_COPY_WI =
            "WI - section 1 copy - The GreatSchools Rating is composed of two categories, Academic and Climate. " +
            "Academic rating makes up 90% of the total rating, while the Climate rating " +
            "makes up the remaining 10%.";

    public static final String MODEL_SECTION_3_COPY = "section3Copy";
    public static final String MODEL_SECTION_3_COPY_POST_SECONDARY_READINESS = "section3CopyPostSecondaryReadiness";
    // TODO-13012 placeholder
    public static final String SECTION_3_COPY_DATA_UNAVAILABLE =
            "Data unavailable - section 3 copy - There is no academic data for this school, " +
            "due to insufficient data.";
    // TODO-13012 placeholder
    public static final String SECTION_3_COPY_DC =
            "DC - section 3 copy - Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Curabitur nisi eros, elementum commodo luctus nec, sodales id odio. Ut at " +
            "tellus aliquet nisi viverra vestibulum. Praesent risus nunc, pellentesque " +
            "et faucibus non, vulputate ut sem. Aliquam a lectus urna, eget aliquam libero. " +
            "Pellentesque consectetur libero nunc, nec posuere nibh. Curabitur euismod " +
            "eleifend dignissim. Morbi lorem felis, tincidunt ut eleifend nec, tempor at leo.";
    // TODO-13012 placeholder
    public static final String SECTION_3_COPY_IN =
            "IN - section 3 copy - Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Curabitur nisi eros, elementum commodo luctus nec, sodales id odio. Ut at " +
            "tellus aliquet nisi viverra vestibulum. Praesent risus nunc, pellentesque " +
            "et faucibus non, vulputate ut sem. Aliquam a lectus urna, eget aliquam libero. " +
            "Pellentesque consectetur libero nunc, nec posuere nibh. Curabitur euismod " +
            "eleifend dignissim. Morbi lorem felis, tincidunt ut eleifend nec, tempor at leo.";
    // TODO-13012 placeholder
    public static final String SECTION_3_COPY_WI =
            "WI - section 3 copy - Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Curabitur nisi eros, elementum commodo luctus nec, sodales id odio. Ut at " +
            "tellus aliquet nisi viverra vestibulum. Praesent risus nunc, pellentesque " +
            "et faucibus non, vulputate ut sem. Aliquam a lectus urna, eget aliquam libero. " +
            "Pellentesque consectetur libero nunc, nec posuere nibh. Curabitur euismod " +
            "eleifend dignissim. Morbi lorem felis, tincidunt ut eleifend nec, tempor at leo.";
    // TODO-13012 placeholder
    public static final String SECTION_3_COPY_POST_SECONDARY_READINESS_DC =
            "DC - section 3 copy about post-secondary readiness";
    // TODO-13012 placeholder
    public static final String SECTION_3_COPY_POST_SECONDARY_READINESS_IN =
            "IN - section 3 copy about post-secondary readiness";
    // TODO-13012 placeholder
    public static final String SECTION_3_COPY_POST_SECONDARY_READINESS_WI =
            "WI - section 3 copy about post-secondary readiness";

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
    public static final String MODEL_POST_SECONDARY_READINESS_RATING = "postSecondaryReadinessRating";

    public static final String MODEL_TEST_SCORE_RATING_SOURCE = "testScoreRatingSource";
    // TODO-13012 placeholder
    public static final String TEST_SCORE_RATING_SOURCE_DC =
            "DC - copy about test score rating source";
    // TODO-13012 placeholder
    public static final String TEST_SCORE_RATING_SOURCE_IN =
            "IN - copy about test score rating source";
    // TODO-13012 placeholder
    public static final String TEST_SCORE_RATING_SOURCE_WI =
            "WI - copy about test score rating source";

    public static final String MODEL_STUDENT_GROWTH_RATING_SOURCE = "studentGrowthRatingSource";
    // TODO-13012 placeholder
    public static final String STUDENT_GROWTH_RATING_SOURCE_DC =
            "DC - copy about student growth rating source";
    // TODO-13012 placeholder
    public static final String STUDENT_GROWTH_RATING_SOURCE_IN =
            "IN - copy about student growth rating source";
    // TODO-13012 placeholder
    public static final String STUDENT_GROWTH_RATING_SOURCE_WI =
            "WI - copy about student growth rating source";

    public static final String MODEL_POST_SECONDARY_READINESS_RATING_SOURCE = "postSecondaryReadinessSource";
    // TODO-13012 placeholder
    public static final String POST_SECONDARY_READINESS_RATING_SOURCE_DC =
            "DC - copy about post-secondary readiness rating source";
    // TODO-13012 placeholder
    public static final String POST_SECONDARY_READINESS_RATING_SOURCE_IN =
            "IN - copy about post-secondary readiness rating source";
    // TODO-13012 placeholder
    public static final String POST_SECONDARY_READINESS_RATING_SOURCE_WI =
            "WI - copy about post-secondary readiness rating source";

    public static final String MODEL_SHOW_CLIMATE_RATING_DETAILS = "showClimateRatingsDetails";
    public static final String MODEL_CLIMATE_RATING_NUM_RESPONSES = "climateRatingNumResponses";

    public static final String MODEL_SCHOOL_ENVIRONMENT_RATING = "schoolEnvironmentRating";
    public static final String MODEL_SOCIAL_EMOTIONAL_LEARNING_RATING = "socialEmotionalLearningRating";
    public static final String MODEL_HIGH_EXPECTATIONS_RATING = "highExpectationsRating";
    public static final String MODEL_TEACHER_SUPPORT_RATING = "teacherSupportRating";
    public static final String MODEL_FAMILY_ENGAGEMENT_RATING = "familyEngagementRating";

    // ===================== REQUEST HANDLERS =======================

    @RequestMapping(method=RequestMethod.GET)
    public String showRatingsPage(ModelMap modelMap,
                                     HttpServletRequest request,
                                     HttpServletResponse response
    ) {
        School school = getSchool(request);
        modelMap.put("school", school);

        modelMap.addAllAttributes(getSection1Model(school));
        modelMap.addAllAttributes(getSection2Model(school));
        modelMap.addAllAttributes(getSection3Model(school));
        modelMap.addAllAttributes(getSection4Model(school));

        return VIEW;
    }

    // TODO-13012 convert some class methods to static methods if possible, after placeholders are replaced with dao calls

    // ===================== Section 1 ==============================
    
    public Map<String,Object> getSection1Model(School school) {
        Map<String,Object> model = new HashMap<String,Object>();

        // OVERALL RATING

        // TODO-13012 check if school has overall rating
        if (true) {
            model.put(MODEL_OVERALL_RATING, 10);
        } else {
            model.put(MODEL_OVERALL_RATING, null);
        }

        // ACADEMIC RATING

        // TODO-13012 check if school has overall academic rating
        if (true) {
            // TODO-13012 placeholder - replace with call to get actual rating
            model.put(MODEL_OVERALL_ACADEMIC_RATING, 9.5);
            // TODO-13012 replace string literal with call to helper method translating numeric rating to label
            model.put(MODEL_OVERALL_ACADEMIC_RATING_LABEL, "High");
        }

        // CLIMATE RATING

        // TODO-13012 check if school has a climate rating (irrespective of state)
        boolean hasClimateRating = true;
        if ((State.DC.equals(school.getDatabaseState()) || State.IN.equals(school.getDatabaseState())) ||
            (State.WI.equals(school.getDatabaseState()) && !hasClimateRating)) {
            model.put(MODEL_CLIMATE_RATING_AVAILABILITY_TEXT, getClimateRatingAvailabilityText(school));
        } else if (hasClimateRating) {
            // TODO-13012 placeholder - replace with call to get actual rating
            model.put(MODEL_OVERALL_CLIMATE_RATING, 6);
            // TODO-13012 replace string literal with call to helper method translating numeric rating to label
            model.put(MODEL_OVERALL_CLIMATE_RATING_LABEL, "Average");
        }

        // SECTION 1 COPY

        model.put(MODEL_SECTION_1_COPY, getSection1Copy(school));

        return model;
    }

    public String getClimateRatingAvailabilityText(School school) {
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

    public String getSection1Copy(School school) {
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

    public Map<String,Object> getSection2Model(School school) {
        Map<String,Object> model = new HashMap<String,Object>();

        // TODO-13012 nothing to do right now

        return model;
    }

    // ===================== Section 3 ==============================

    public Map<String,Object> getSection3Model(School school) {
        Map<String,Object> model = new HashMap<String,Object>();

        // SECTION 3 COPY

        model.put(MODEL_SECTION_3_COPY, getSection3Copy(school));

        // SECTION 3 POST-SECONDARY READINESS COPY

        model.put(MODEL_SECTION_3_COPY_POST_SECONDARY_READINESS, getSection3CopyPostSecondaryReadiness(school));

        // SECTION 3 CHARTS

        // test score ratings

        boolean showStateTestScoreRating = isShowStateTestScoreRating(school);
        model.put(MODEL_SHOW_STATE_TEST_SCORE_RATING, showStateTestScoreRating);
        model.putAll(getTestScoreRatingsModel(school, showStateTestScoreRating));

        // student growth ratings

        boolean showStateStudentGrowthRating = isShowStateStudentGrowthRating(school);
        model.put(MODEL_SHOW_STATE_STUDENT_GROWTH_RATING, showStateStudentGrowthRating);
        model.putAll(getStudentGrowthRatingsModel(school, showStateStudentGrowthRating));

        // post-secondary readiness ratings

        model.putAll(getPostSecondaryReadinessRatingsModel(school));

        // SECTION 3 SOURCES

        model.put(MODEL_TEST_SCORE_RATING_SOURCE, getTestScoreRatingSource(school));

        model.put(MODEL_STUDENT_GROWTH_RATING_SOURCE, getStudentGrowthRatingSource(school));

        model.put(MODEL_POST_SECONDARY_READINESS_RATING_SOURCE, getPostSecondaryReadinessRatingSource(school));

        return model;
    }

    public boolean isShowStateTestScoreRating(School school) {
        return !State.DC.equals(school.getDatabaseState());
    }

    public boolean isShowStateStudentGrowthRating(School school) {
        return !State.DC.equals(school.getDatabaseState());
    }

    public Map<String,Object> getTestScoreRatingsModel(School school, boolean showStateRating) {
        Map<String,Object> model = new HashMap<String,Object>();

        // TODO-13012 check if school has test score rating regardless of level code
        if (true) {
            // TODO-13012 placeholder - replace with call to get actual year
            model.put(MODEL_TEST_SCORE_RATING_YEAR, 2012);

            // TODO-13012 placeholder - replace with call to get actual rating
            model.put(MODEL_SCHOOL_TEST_SCORE_RATING, 9);

            // TODO-13012 placeholder - replace with call to get actual rating
            model.put(MODEL_CITY_TEST_SCORE_RATING, 5);

            if (showStateRating) {
                // TODO-13012 placeholder - replace with call to get actual rating
                model.put(MODEL_STATE_TEST_SCORE_RATING, 3);
            }
        }

        return model;
    }

    public Map<String,Object> getStudentGrowthRatingsModel(School school, boolean showStateRating) {
        Map<String,Object> model = new HashMap<String,Object>();

        // TODO-13012 check if school has student growth rating regardless of level code
        if (true && school.getLevelCode() != null && !school.getLevelCode().equals(LevelCode.HIGH)) {
            // TODO-13012 placeholder - replace with call to get actual year
            model.put(MODEL_STUDENT_GROWTH_RATING_YEAR, 2012);
            // TODO-13012 placeholder - replace with call to get actual rating
            model.put(MODEL_SCHOOL_STUDENT_GROWTH_RATING, 9);
            // TODO-13012 placeholder - replace with call to get actual rating
            model.put(MODEL_CITY_STUDENT_GROWTH_RATING, 5);
            if (showStateRating) {
                // TODO-13012 placeholder - replace with call to get actual rating
                model.put(MODEL_STATE_STUDENT_GROWTH_RATING, 3);
            }
        }

        return model;
    }

    public Map<String,Object> getPostSecondaryReadinessRatingsModel(School school) {
        Map<String,Object> model = new HashMap<String,Object>();

        // TODO-13012 check if school has post-secondary readiness rating regardless of level code
        if (true && school.getLevelCode() != null && school.getLevelCode().containsLevelCode(LevelCode.Level.HIGH_LEVEL)) {
            // TODO-13012 placeholder - replace with call to get actual year
            model.put(MODEL_POST_SECONDARY_READINESS_RATING_YEAR, 2012);
            // TODO-13012 placeholder - replace with call to get actual rating
            model.put(MODEL_POST_SECONDARY_READINESS_RATING, 8);
        }
            
        return model;
    }

    public String getTestScoreRatingSource(School school) {
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

    public String getStudentGrowthRatingSource(School school) {
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

    public String getPostSecondaryReadinessRatingSource(School school) {
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

    public String getSection3Copy(School school) {
        if (State.DC.equals(school.getDatabaseState())) {
            return SECTION_3_COPY_DC;
        } else if (State.IN.equals(school.getDatabaseState())) {
            return SECTION_3_COPY_IN;
        } else if (State.WI.equals(school.getDatabaseState())) {
            // TODO-13012 placeholder - check whether school has academic performance data
            if (true) {
                return SECTION_3_COPY_WI;
            } else {
                return SECTION_3_COPY_DATA_UNAVAILABLE;
            }
        } else {
            throw new IllegalArgumentException("School is from unsupported state");
        }
    }

    public String getSection3CopyPostSecondaryReadiness(School school) {
        // TODO-13012 placeholder - check if post-secondary readiness rating is available for this school (even if contains High School level code)
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

    public Map<String,Object> getSection4Model(School school) {
        Map<String,Object> model = new HashMap<String,Object>();

        // TODO-13012
        // TODO-FIXME

        if (State.DC.equals(school.getDatabaseState())) {
            model.put(MODEL_SHOW_CLIMATE_RATING_DETAILS, false);
        } else if (State.IN.equals(school.getDatabaseState())) {
            model.put(MODEL_SHOW_CLIMATE_RATING_DETAILS, false);
        } else if (State.WI.equals(school.getDatabaseState())) {
            model.put(MODEL_SHOW_CLIMATE_RATING_DETAILS, true);

            // TODO-13012 placeholder
            model.put(MODEL_CLIMATE_RATING_NUM_RESPONSES, 16);
            // TODO-13012 placeholder
            model.put(MODEL_SCHOOL_ENVIRONMENT_RATING, 6);
            // TODO-13012 placeholder
            model.put(MODEL_SOCIAL_EMOTIONAL_LEARNING_RATING, 7);
            // TODO-13012 placeholder
            model.put(MODEL_HIGH_EXPECTATIONS_RATING, 6);
            // TODO-13012 placeholder
            model.put(MODEL_TEACHER_SUPPORT_RATING, 4);
            // TODO-13012 placeholder
            model.put(MODEL_FAMILY_ENGAGEMENT_RATING, 7);
        }

        return model;
    }

    // ===================== UTILITY METHODS ========================
    // TODO-13012 any methods to add here?
}