package gs.web.school;

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

    public static final String MODEL_TEST_SCORE_RATING_FOOTNOTE = "testScoreRatingFootnote";
    public static final String MODEL_STUDENT_GROWTH_RATING_FOOTNOTE = "studentGrowthRatingFootnote";

    public static final String MODEL_SHOW_CLIMATE_RATING_DETAILS = "showClimateRatingsDetails";
    public static final String MODEL_CLIMATE_RATING_NUM_RESPONSES = "climateRatingNumResponses";

    public static final String MODEL_SCHOOL_ENVIRONMENT_RATING = "schoolEnvironmentRating";
    public static final String MODEL_SOCIAL_EMOTIONAL_LEARNING_RATING = "socialEmotionalLearningRating";
    public static final String MODEL_HIGH_EXPECTATIONS_RATING = "highExpectationsRating";
    public static final String MODEL_TEACHER_SUPPORT_RATING = "teacherSupportRating";
    public static final String MODEL_FAMILY_ENGAGEMENT_RATING = "familyEngagementRating";

    @RequestMapping(method=RequestMethod.GET)
    public String showRatingsPage(ModelMap modelMap,
                                     HttpServletRequest request,
                                     HttpServletResponse response
    ) {
        School school = getSchool(request);
        modelMap.put("school", school);

        modelMap.put(MODEL_OVERALL_RATING, 10);
        modelMap.put(MODEL_OVERALL_ACADEMIC_RATING, 9.5);
        modelMap.put(MODEL_OVERALL_ACADEMIC_RATING_LABEL, "High");
        modelMap.put(MODEL_OVERALL_CLIMATE_RATING, 6);
        modelMap.put(MODEL_OVERALL_CLIMATE_RATING_LABEL, "Average");
        if (State.DC.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_CLIMATE_RATING_AVAILABILITY_TEXT, "Coming 2013");
        } else if (State.IN.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_CLIMATE_RATING_AVAILABILITY_TEXT, "Coming soon");
        } else if (State.WI.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_CLIMATE_RATING_AVAILABILITY_TEXT, null);
        }

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

        modelMap.put(MODEL_TEST_SCORE_RATING_FOOTNOTE, "Test scores are based on blah blah blah.");
        modelMap.put(MODEL_STUDENT_GROWTH_RATING_FOOTNOTE, "Student growth rating is based on blah blah blah.");


        if (State.DC.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_SHOW_CLIMATE_RATING_DETAILS, false);
        } else if (State.IN.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_SHOW_CLIMATE_RATING_DETAILS, false);
        } else if (State.WI.equals(school.getDatabaseState())) {
            modelMap.put(MODEL_SHOW_CLIMATE_RATING_DETAILS, true);

            modelMap.put(MODEL_CLIMATE_RATING_NUM_RESPONSES, 16);
            modelMap.put(MODEL_SCHOOL_ENVIRONMENT_RATING, 6);
            modelMap.put(MODEL_SOCIAL_EMOTIONAL_LEARNING_RATING, 7);
            modelMap.put(MODEL_HIGH_EXPECTATIONS_RATING, 6);
            modelMap.put(MODEL_TEACHER_SUPPORT_RATING, 4);
            modelMap.put(MODEL_FAMILY_ENGAGEMENT_RATING, 7);
        }

        return VIEW;
    }
}