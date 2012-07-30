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
    public static final String MODEL_DISTRICT_TEST_SCORE_RATING = "districtTestScoreRating";
    public static final String MODEL_STATE_TEST_SCORE_RATING = "stateTestScoreRating";
    public static final String MODEL_SHOW_STATE_TEST_SCORE_RATING = "showStateTestScoreRating";

    public static final String MODEL_STUDENT_GROWTH_RATING_YEAR = "studentGrowthRatingYear";
    public static final String MODEL_SCHOOL_STUDENT_GROWTH_RATING = "schoolTestScoreRating";
    public static final String MODEL_DISTRICT_STUDENT_GROWTH_RATING = "districtTestScoreRating";
    public static final String MODEL_STATE_STUDENT_GROWTH_RATING = "stateTestScoreRating";
    public static final String MODEL_SHOW_STATE_STUDENT_GROWTH_RATING = "showStateTestScoreRating";

    public static final String MODEL_POST_SECONDARY_READINESS_RATING_YEAR = "postSecondaryReadinessRatingYear";
    public static final String MODEL_POST_SECONDARY_READINESS_RATING = "postSecondaryReadinessRating";

    public static final String MODEL_TEST_SCORE_RATING_SOURCE = "testScoreRatingSource";
    public static final String MODEL_STUDENT_GROWTH_RATING_SOURCE = "studentGrowthRatingSource";
    public static final String MODEL_POST_SECONDARY_READINESS_SOURCE = "postSecondaryReadinessSource";

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

        // TODO-13012 placeholder
        modelMap.put(MODEL_TEST_SCORE_RATING_YEAR, 2011);
        modelMap.put(MODEL_SCHOOL_TEST_SCORE_RATING, 9);
        modelMap.put(MODEL_DISTRICT_TEST_SCORE_RATING, 5);
        modelMap.put(MODEL_STATE_TEST_SCORE_RATING, 3);
        if (State.DC.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_SHOW_STATE_TEST_SCORE_RATING, false);
        } else if (State.IN.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_SHOW_STATE_TEST_SCORE_RATING, true);
        } else if (State.WI.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_SHOW_STATE_TEST_SCORE_RATING, true);
        }

        modelMap.put(MODEL_STUDENT_GROWTH_RATING_YEAR, 2011);
        modelMap.put(MODEL_SCHOOL_STUDENT_GROWTH_RATING, 9);
        modelMap.put(MODEL_DISTRICT_STUDENT_GROWTH_RATING, 5);
        modelMap.put(MODEL_STATE_STUDENT_GROWTH_RATING, 3);
        if (State.DC.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_SHOW_STATE_STUDENT_GROWTH_RATING, false);
        } else if (State.IN.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_SHOW_STATE_STUDENT_GROWTH_RATING, true);
        } else if (State.WI.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_SHOW_STATE_STUDENT_GROWTH_RATING, true);
        }

        // TODO-13012 section 3 sources by state and if rating is available for this school
        // TODO-FIXME
        //modelMap.put(MODEL_TEST_SCORE_RATING_SOURCE, "Test scores are based on blah blah blah.");
        //modelMap.put(MODEL_STUDENT_GROWTH_RATING_SOURCE, "Student growth rating is based on blah blah blah.");
        //modelMap.put(MODEL_POST_SECONDARY_READINESS_SOURCE, Post-secondary readiness is based on blah blah blah.");


        if (State.DC.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_SHOW_CLIMATE_RATING_DETAILS, false);
        } else if (State.IN.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_SHOW_CLIMATE_RATING_DETAILS, false);
        } else if (State.WI.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_SHOW_CLIMATE_RATING_DETAILS, true);

            // TODO-13012 placeholder
            modelMap.put(MODEL_CLIMATE_RATING_NUM_RESPONSES, 16);
            // TODO-13012 placeholder
            modelMap.put(MODEL_SCHOOL_ENVIRONMENT_RATING, 6);
            // TODO-13012 placeholder
            modelMap.put(MODEL_SOCIAL_EMOTIONAL_LEARNING_RATING, 7);
            // TODO-13012 placeholder
            modelMap.put(MODEL_HIGH_EXPECTATIONS_RATING, 6);
            // TODO-13012 placeholder
            modelMap.put(MODEL_TEACHER_SUPPORT_RATING, 4);
            // TODO-13012 placeholder
            modelMap.put(MODEL_FAMILY_ENGAGEMENT_RATING, 7);
        }

        modelMap.addAllAttributes(getSection1Model(school));
        modelMap.addAllAttributes(getSection2Model(school));
        modelMap.addAllAttributes(getSection3Model(school));
        modelMap.addAllAttributes(getSection4Model(school));

        return VIEW;
    }

    // ===================== Section 1 ==============================
    
    public Map<String,Object> getSection1Model(School school) {
        Map<String,Object> model = new HashMap<String,Object>();

        // TODO-13012
        if (true) {
            model.put(MODEL_OVERALL_RATING, 10);
        } else {
            model.put(MODEL_OVERALL_RATING, null);
        }

        // TODO-13012
        if (true) {
            model.put(MODEL_OVERALL_ACADEMIC_RATING, 9.5);
            model.put(MODEL_OVERALL_ACADEMIC_RATING_LABEL, "High");
        }

        if (State.DC.equals(school.getDatabaseState())) {
            model.put(MODEL_CLIMATE_RATING_AVAILABILITY_TEXT, "Coming 2013");
        } else if (State.IN.equals(school.getDatabaseState())) {
            model.put(MODEL_CLIMATE_RATING_AVAILABILITY_TEXT, "Coming soon");
        } else if (State.WI.equals(school.getDatabaseState())) {
            // TODO-13012
            if (true) {
                model.put(MODEL_OVERALL_CLIMATE_RATING, 6);
                model.put(MODEL_OVERALL_CLIMATE_RATING_LABEL, "Average");
            } else {
                model.put(MODEL_CLIMATE_RATING_AVAILABILITY_TEXT, "Not available");
            }
        }

        if (State.DC.equals(school.getDatabaseState())) {
            model.put(MODEL_SECTION_1_COPY, SECTION_1_COPY_DC);
        } else if (State.IN.equals(school.getDatabaseState())) {
            model.put(MODEL_SECTION_1_COPY, SECTION_1_COPY_IN);
        } else if (State.WI.equals(school.getDatabaseState())) {
            model.put(MODEL_SECTION_1_COPY, SECTION_1_COPY_WI);
        }

        return model;
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

        if (State.DC.equals(school.getDatabaseState())) {
            model.put(MODEL_SECTION_3_COPY, SECTION_3_COPY_DC);
        } else if (State.IN.equals(school.getDatabaseState())) {
            model.put(MODEL_SECTION_3_COPY, SECTION_3_COPY_IN);
        } else if (State.WI.equals(school.getDatabaseState())) {
            model.put(MODEL_SECTION_3_COPY, SECTION_3_COPY_WI);
        }

        // TODO-13012 placeholder - also check if post-secondary readiness rating is available for this school
        LevelCode levelCode = school.getLevelCode();
        if (levelCode != null && levelCode.containsLevelCode(LevelCode.Level.HIGH_LEVEL)) {
            if (State.DC.equals(school.getDatabaseState())) {
                model.put(MODEL_SECTION_3_COPY_POST_SECONDARY_READINESS, SECTION_3_COPY_POST_SECONDARY_READINESS_DC);
            } else if (State.IN.equals(school.getDatabaseState())) {
                model.put(MODEL_SECTION_3_COPY_POST_SECONDARY_READINESS, SECTION_3_COPY_POST_SECONDARY_READINESS_IN);
            } else if (State.WI.equals(school.getDatabaseState())) {
                model.put(MODEL_SECTION_3_COPY_POST_SECONDARY_READINESS, SECTION_3_COPY_POST_SECONDARY_READINESS_WI);
            }
        }

        return model;
    }

    // ===================== Section 4 ==============================

    public Map<String,Object> getSection4Model(School school) {
        Map<String,Object> model = new HashMap<String,Object>();

        return model;
    }

    // ===================== UTILITY METHODS ========================
}