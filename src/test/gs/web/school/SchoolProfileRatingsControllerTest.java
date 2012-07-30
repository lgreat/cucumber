package gs.web.school;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SchoolProfileRatingsControllerTest extends BaseControllerTestCase {
    private SchoolProfileRatingsController _controller;

    private static Set<LevelCode> NON_HIGH_ONLY_LEVEL_CODES = new HashSet<LevelCode>();
    static {
        NON_HIGH_ONLY_LEVEL_CODES.add(LevelCode.ALL_LEVELS);
        NON_HIGH_ONLY_LEVEL_CODES.add(LevelCode.MIDDLE);
        NON_HIGH_ONLY_LEVEL_CODES.add(LevelCode.MIDDLE_HIGH);
        NON_HIGH_ONLY_LEVEL_CODES.add(LevelCode.ELEMENTARY);
        NON_HIGH_ONLY_LEVEL_CODES.add(LevelCode.ELEMENTARY_MIDDLE);
        NON_HIGH_ONLY_LEVEL_CODES.add(LevelCode.ELEMENTARY_MIDDLE_HIGH);
        NON_HIGH_ONLY_LEVEL_CODES.add(LevelCode.ELEMENTARY_HIGH);
        NON_HIGH_ONLY_LEVEL_CODES.add(LevelCode.PRESCHOOL);
        NON_HIGH_ONLY_LEVEL_CODES.add(LevelCode.PRESCHOOL_ELEMENTARY);
        NON_HIGH_ONLY_LEVEL_CODES.add(LevelCode.PRESCHOOL_ELEMENTARY_MIDDLE);
    }

    private static Set<LevelCode> NON_HIGH_LEVEL_CODES = new HashSet<LevelCode>();
    static {
        NON_HIGH_LEVEL_CODES.add(LevelCode.MIDDLE);
        NON_HIGH_LEVEL_CODES.add(LevelCode.ELEMENTARY);
        NON_HIGH_LEVEL_CODES.add(LevelCode.ELEMENTARY_MIDDLE);
        NON_HIGH_LEVEL_CODES.add(LevelCode.PRESCHOOL);
        NON_HIGH_LEVEL_CODES.add(LevelCode.PRESCHOOL_ELEMENTARY);
        NON_HIGH_LEVEL_CODES.add(LevelCode.PRESCHOOL_ELEMENTARY_MIDDLE);
    }

    private static Set<LevelCode> CONTAINS_HIGH_LEVEL_CODES = new HashSet<LevelCode>();
    static {
        CONTAINS_HIGH_LEVEL_CODES.add(LevelCode.HIGH);
        CONTAINS_HIGH_LEVEL_CODES.add(LevelCode.ALL_LEVELS);
        CONTAINS_HIGH_LEVEL_CODES.add(LevelCode.MIDDLE_HIGH);
        CONTAINS_HIGH_LEVEL_CODES.add(LevelCode.ELEMENTARY_HIGH);
        CONTAINS_HIGH_LEVEL_CODES.add(LevelCode.ELEMENTARY_MIDDLE_HIGH);
    }

    public void setUp() throws Exception {
        super.setUp();
        _controller = new SchoolProfileRatingsController();

    }

    // see individual tests for section 1 copy, etc.
    public void testGetSection1Model() throws Exception {
        Map<String,Object> model;
        School s = new School();
        Object overallRating;
        Object overallAcademicRating;
        Object overallAcademicRatingLabel;
        Object overallClimateRating;
        Object overallClimateRatingLabel;
        Object climateRatingAvailabilityText;
        Object section1Copy;

        // OVERALL RATING

        // TODO-13012 school with 1-10 overall rating
        s.setDatabaseState(State.WI);
        model = _controller.getSection1Model(s);
        overallRating =
                model.get(SchoolProfileRatingsController.MODEL_OVERALL_RATING);
        assertEquals(10, overallRating);

        // TODO-13012 school with no overall rating
        // assertNull(overallRating);

        // ACADEMIC RATING

        // TODO-13012 school with 1-10 academic rating
        model = _controller.getSection1Model(s);
        overallAcademicRating =
                model.get(SchoolProfileRatingsController.MODEL_OVERALL_ACADEMIC_RATING);
        overallAcademicRatingLabel =
                model.get(SchoolProfileRatingsController.MODEL_OVERALL_ACADEMIC_RATING_LABEL);
        assertEquals(9.5, overallAcademicRating);
        assertEquals("High", overallAcademicRatingLabel);

        // TODO-13012 school with no academic rating
        // assertNull(overallAcademicRating);
        // assertNull(overallAcademicRatingLabel);

        // CLIMATE RATING

        // Milwaukee - 1-10 rating
        s.setDatabaseState(State.WI);
        model = _controller.getSection1Model(s);
        climateRatingAvailabilityText =
                model.get(SchoolProfileRatingsController.MODEL_CLIMATE_RATING_AVAILABILITY_TEXT);
        overallClimateRating =
                model.get(SchoolProfileRatingsController.MODEL_OVERALL_CLIMATE_RATING);
        overallClimateRatingLabel =
                model.get(SchoolProfileRatingsController.MODEL_OVERALL_CLIMATE_RATING_LABEL);
        // TODO-13012 MI 1-10 rating, label
        assertEquals(6, overallClimateRating);
        assertEquals("Average", overallClimateRatingLabel);
        assertNull(climateRatingAvailabilityText);


        // TODO-13012 Milwaukee - no climate rating
        //assertEquals("Not available", climateRatingAvailabilityText);

        // DC
        s.setDatabaseState(State.DC);
        model = _controller.getSection1Model(s);
        climateRatingAvailabilityText =
                model.get(SchoolProfileRatingsController.MODEL_CLIMATE_RATING_AVAILABILITY_TEXT);
        assertEquals(SchoolProfileRatingsController.CLIMATE_RATING_AVAILABILITY_TEXT_DC, climateRatingAvailabilityText);

        // SECTION 1 COPY

        // Milwaukee
        s.setDatabaseState(State.WI);
        model = _controller.getSection1Model(s);
        section1Copy = model.get(SchoolProfileRatingsController.MODEL_SECTION_1_COPY);
        assertEquals(SchoolProfileRatingsController.SECTION_1_COPY_WI, section1Copy);
    }

    public void testGetClimateRatingAvailabilityText() {
        School s = new School();
        String copy;

        // Milwaukee
        s.setDatabaseState(State.WI);
        copy = _controller.getClimateRatingAvailabilityText(s);
        assertEquals(SchoolProfileRatingsController.CLIMATE_RATING_AVAILABILITY_TEXT_WI, copy);

        // DC
        s.setDatabaseState(State.DC);
        copy = _controller.getClimateRatingAvailabilityText(s);
        assertEquals(SchoolProfileRatingsController.CLIMATE_RATING_AVAILABILITY_TEXT_DC, copy);

        // Indy
        s.setDatabaseState(State.IN);
        copy = _controller.getClimateRatingAvailabilityText(s);
        assertEquals(SchoolProfileRatingsController.CLIMATE_RATING_AVAILABILITY_TEXT_IN, copy);

        // unsupported state
        boolean threwException = false;
        s.setDatabaseState(State.AK);
        try {
            copy = _controller.getClimateRatingAvailabilityText(s);
        } catch (Exception e) {
            threwException = true;
            assertTrue("Should have thrown IllegalArgumentException", e instanceof IllegalArgumentException);
            assertEquals("School is from unsupported state", e.getMessage());
        }
        assertTrue(threwException);
    }

    public void testGetSection1Copy() {
        School s = new School();
        String copy;

        // Milwaukee
        s.setDatabaseState(State.WI);
        copy = _controller.getSection1Copy(s);
        assertEquals(SchoolProfileRatingsController.SECTION_1_COPY_WI, copy);

        // DC
        s.setDatabaseState(State.DC);
        copy = _controller.getSection1Copy(s);
        assertEquals(SchoolProfileRatingsController.SECTION_1_COPY_DC, copy);

        // Indy
        s.setDatabaseState(State.IN);
        copy = _controller.getSection1Copy(s);
        assertEquals(SchoolProfileRatingsController.SECTION_1_COPY_IN, copy);

        // unsupported state
        boolean threwException = false;
        s.setDatabaseState(State.AK);
        try {
            copy = _controller.getSection1Copy(s);
        } catch (Exception e) {
            threwException = true;
            assertTrue("Should have thrown IllegalArgumentException", e instanceof IllegalArgumentException);
            assertEquals("School is from unsupported state", e.getMessage());
        }
        assertTrue(threwException);
    }

    public void testGetSection2Model() throws Exception {
        // TODO-13012 nothing to do right now
    }

    // see individual tests for section 3 copy, section 3 post-secondary readiness, etc.
    public void testGetSection3Model() throws Exception {
        Map<String,Object> model;

        School s = new School();
        s.setDatabaseState(State.WI);
        s.setLevelCode(LevelCode.ELEMENTARY_MIDDLE_HIGH);

        model = _controller.getSection3Model(s);

        // SECTION 3 COPY

        Object section3Copy = model.get(SchoolProfileRatingsController.MODEL_SECTION_3_COPY);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_WI, section3Copy);

        // SECTION 3 POST-SECONDARY READINESS COPY

        Object section3CopyPostSecondaryReadiness =
                model.get(SchoolProfileRatingsController.MODEL_SECTION_3_COPY_POST_SECONDARY_READINESS);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS_WI,
                section3CopyPostSecondaryReadiness);

        // SECTION 3 TEST SCORE RATING CHART

        // TODO-13012 fix unit tests after placeholders replaced with actual data calls, including school with no data
        Object testScoreRatingYear =
                model.get(SchoolProfileRatingsController.MODEL_TEST_SCORE_RATING_YEAR);
        assertEquals(2012, testScoreRatingYear);
        Object schoolTestScoreRating =
                model.get(SchoolProfileRatingsController.MODEL_SCHOOL_TEST_SCORE_RATING);
        assertEquals(9, schoolTestScoreRating);
        Object cityTestScoreRating =
                model.get(SchoolProfileRatingsController.MODEL_CITY_TEST_SCORE_RATING);
        assertEquals(5, cityTestScoreRating);
        Object stateTestScoreRating =
                model.get(SchoolProfileRatingsController.MODEL_STATE_TEST_SCORE_RATING);
        assertEquals(3, stateTestScoreRating);

        Object showStateTestScoreRating =
                model.get(SchoolProfileRatingsController.MODEL_SHOW_STATE_TEST_SCORE_RATING);
        assertEquals(Boolean.TRUE, showStateTestScoreRating);

        // SECTION 3 STUDENT GROWTH RATING CHART

        // TODO-13012 fix unit tests after placeholders replaced with actual data calls, including school with no data
        Object studentGrowthRatingYear =
                model.get(SchoolProfileRatingsController.MODEL_STUDENT_GROWTH_RATING_YEAR);
        assertEquals(2012, studentGrowthRatingYear);
        Object schoolStudentGrowthRating =
                model.get(SchoolProfileRatingsController.MODEL_SCHOOL_STUDENT_GROWTH_RATING);
        assertEquals(9, schoolStudentGrowthRating);
        Object cityStudentGrowthRating =
                model.get(SchoolProfileRatingsController.MODEL_CITY_STUDENT_GROWTH_RATING);
        assertEquals(5, cityStudentGrowthRating);
        Object stateStudentGrowthRating =
                model.get(SchoolProfileRatingsController.MODEL_STATE_STUDENT_GROWTH_RATING);
        assertEquals(3, stateStudentGrowthRating);

        Object showStateStudentGrowthRating =
                model.get(SchoolProfileRatingsController.MODEL_SHOW_STATE_STUDENT_GROWTH_RATING);
        assertEquals(Boolean.TRUE, showStateStudentGrowthRating);

        // SECTION 3 POST-SECONDARY READINESS RATING CHART

        // TODO-13012 fix unit tests after placeholders replaced with actual data calls, including school with no data
        Object postSecondaryReadinessRatingYear =
                model.get(SchoolProfileRatingsController.MODEL_POST_SECONDARY_READINESS_RATING_YEAR);
        assertEquals(2012, postSecondaryReadinessRatingYear);
        Object postSecondaryReadinessRating =
                model.get(SchoolProfileRatingsController.MODEL_POST_SECONDARY_READINESS_RATING);
        assertEquals(8, postSecondaryReadinessRating);

        // SECTION 3 SOURCES

        Object testScoreRatingSource =
                model.get(SchoolProfileRatingsController.MODEL_TEST_SCORE_RATING_SOURCE);
        assertEquals(SchoolProfileRatingsController.TEST_SCORE_RATING_SOURCE_WI,
                testScoreRatingSource);

        Object studentGrowthRatingSource =
                model.get(SchoolProfileRatingsController.MODEL_STUDENT_GROWTH_RATING_SOURCE);
        assertEquals(SchoolProfileRatingsController.STUDENT_GROWTH_RATING_SOURCE_WI,
                studentGrowthRatingSource);

        Object postSecondaryReadinessSource =
                model.get(SchoolProfileRatingsController.MODEL_POST_SECONDARY_READINESS_RATING_SOURCE);
        assertEquals(SchoolProfileRatingsController.POST_SECONDARY_READINESS_RATING_SOURCE_WI,
                postSecondaryReadinessSource);
    }

    public void testGetTestScoreRatingsModel() {
        Map<String,Object> model;

        School s = new School();

    }

    public void testGetStudentGrowthRatingsModel() {
        Map<String,Object> model;
        School s = new School();

        // schools with high-only level code: no student growth ratings

        s.setLevelCode(LevelCode.HIGH);
        model = _controller.getStudentGrowthRatingsModel(s, true);
        assertNull(model.get(SchoolProfileRatingsController.MODEL_STUDENT_GROWTH_RATING_YEAR));
        assertNull(model.get(SchoolProfileRatingsController.MODEL_SCHOOL_STUDENT_GROWTH_RATING));

        // TODO-13012 check if school has student growth rating regardless of level code

        // schools with non-high-only level codes should have student growth ratings

        for (LevelCode levelCode : NON_HIGH_ONLY_LEVEL_CODES) {
            s.setLevelCode(levelCode);
            model = _controller.getStudentGrowthRatingsModel(s,true);
            // TODO-13012 fix unit tests after placeholders replaced with actual data calls, including school with no data
            assertEquals(2012, model.get(SchoolProfileRatingsController.MODEL_STUDENT_GROWTH_RATING_YEAR));
            assertEquals(9, model.get(SchoolProfileRatingsController.MODEL_SCHOOL_STUDENT_GROWTH_RATING));
            assertEquals(5, model.get(SchoolProfileRatingsController.MODEL_CITY_STUDENT_GROWTH_RATING));
            assertEquals(3, model.get(SchoolProfileRatingsController.MODEL_STATE_STUDENT_GROWTH_RATING));
        }

        // omit state rating

        model = _controller.getStudentGrowthRatingsModel(s,false);
        s.setLevelCode(LevelCode.ALL_LEVELS);
        // TODO-13012 fix unit tests after placeholders replaced with actual data calls, including school with no data
        assertEquals(2012, model.get(SchoolProfileRatingsController.MODEL_STUDENT_GROWTH_RATING_YEAR));
        assertEquals(9, model.get(SchoolProfileRatingsController.MODEL_SCHOOL_STUDENT_GROWTH_RATING));
        assertEquals(5, model.get(SchoolProfileRatingsController.MODEL_CITY_STUDENT_GROWTH_RATING));
        assertNull(model.get(SchoolProfileRatingsController.MODEL_STATE_STUDENT_GROWTH_RATING));
    }

    public void testGetPostSecondaryReadinessRatingsModel() {
        Map<String,Object> model;
        School s = new School();

        // schools with no high level code: no post-secondary readiness ratings

        for (LevelCode levelCode : NON_HIGH_LEVEL_CODES) {
            s.setLevelCode(levelCode);
            model = _controller.getPostSecondaryReadinessRatingsModel(s);
            assertNull(model.get(SchoolProfileRatingsController.MODEL_POST_SECONDARY_READINESS_RATING_YEAR));
            assertNull(model.get(SchoolProfileRatingsController.MODEL_POST_SECONDARY_READINESS_RATING));
        }

        // TODO-13012 check if school has post-secondary readiness rating regardless of level code

        // schools containing high level code should have post-secondary readiness ratings

        for (LevelCode levelCode : CONTAINS_HIGH_LEVEL_CODES) {
            s.setLevelCode(levelCode);
            model = _controller.getPostSecondaryReadinessRatingsModel(s);
            // TODO-13012 fix unit tests after placeholders replaced with actual data calls, including school with no data
            assertEquals(2012, model.get(SchoolProfileRatingsController.MODEL_POST_SECONDARY_READINESS_RATING_YEAR));
            assertEquals(8, model.get(SchoolProfileRatingsController.MODEL_POST_SECONDARY_READINESS_RATING));
        }
    }

    public void testGetSection3Copy() {
        School s = new School();
        String copy;

        // TODO-13012 Data unavailable copy
        //s.setDatabaseState(State.WI);
        //copy = _controller.getSection3Copy(s);
        //assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_DATA_UNAVAILABLE, copy);

        // Milwaukee
        s.setDatabaseState(State.WI);
        copy = _controller.getSection3Copy(s);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_WI, copy);

        // DC
        s.setDatabaseState(State.DC);
        copy = _controller.getSection3Copy(s);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_DC, copy);

        // Indy
        s.setDatabaseState(State.IN);
        copy = _controller.getSection3Copy(s);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_IN, copy);

        // unsupported state
        boolean threwException = false;
        s.setDatabaseState(State.AK);
        try {
            copy = _controller.getSection3Copy(s);
        } catch (Exception e) {
            threwException = true;
            assertTrue("Should have thrown IllegalArgumentException", e instanceof IllegalArgumentException);
            assertEquals("School is from unsupported state", e.getMessage());
        }
        assertTrue(threwException);
    }

    public void testGetSection3CopyPostSecondaryReadiness() {
        School s = new School();
        String copy;

        // TODO-13012 check if post-secondary readiness rating is available for this school (even if contains High School level code)

        s.setLevelCode(LevelCode.HIGH);

        // Milwaukee
        s.setDatabaseState(State.WI);
        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS_WI, copy);

        // DC
        s.setDatabaseState(State.DC);
        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS_DC, copy);

        // Indy
        s.setDatabaseState(State.IN);
        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS_IN, copy);

        // any school with level code containing 'high' should have post-secondary readiness copy

        for (LevelCode levelCode : CONTAINS_HIGH_LEVEL_CODES) {
            s.setLevelCode(levelCode);
            copy = _controller.getSection3CopyPostSecondaryReadiness(s);
            assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS_IN, copy);
        }

        for (LevelCode levelCode : NON_HIGH_LEVEL_CODES) {
            s.setLevelCode(levelCode);
            copy = _controller.getSection3CopyPostSecondaryReadiness(s);
            assertNull(copy);
        }

        // unsupported state - contains High level code
        s.setDatabaseState(State.AK);
        boolean threwException = false;
        for (LevelCode levelCode : CONTAINS_HIGH_LEVEL_CODES) {
            s.setLevelCode(levelCode);
            try {

                copy = _controller.getSection3CopyPostSecondaryReadiness(s);
            } catch (Exception e) {
                threwException = true;
                assertTrue("Should have thrown IllegalArgumentException", e instanceof IllegalArgumentException);
                assertEquals("School is from unsupported state", e.getMessage());
            }
            assertTrue(threwException);
        }

        // unsupported state - does not contain High level code
        s.setDatabaseState(State.AK);
        for (LevelCode levelCode : NON_HIGH_LEVEL_CODES) {
            s.setLevelCode(levelCode);
            copy = _controller.getSection3CopyPostSecondaryReadiness(s);
            assertNull(copy);
        }

    }

    public void testGetTestScoreRatingSource() {
        School s = new School();
        String copy;

        // Milwaukee
        s.setDatabaseState(State.WI);
        copy = _controller.getTestScoreRatingSource(s);
        assertEquals(SchoolProfileRatingsController.TEST_SCORE_RATING_SOURCE_WI, copy);

        // DC
        s.setDatabaseState(State.DC);
        copy = _controller.getTestScoreRatingSource(s);
        assertEquals(SchoolProfileRatingsController.TEST_SCORE_RATING_SOURCE_DC, copy);

        // Indy
        s.setDatabaseState(State.IN);
        copy = _controller.getTestScoreRatingSource(s);
        assertEquals(SchoolProfileRatingsController.TEST_SCORE_RATING_SOURCE_IN, copy);

        // unsupported state
        boolean threwException = false;
        s.setDatabaseState(State.AK);
        try {
            copy = _controller.getTestScoreRatingSource(s);
        } catch (Exception e) {
            threwException = true;
            assertTrue("Should have thrown IllegalArgumentException", e instanceof IllegalArgumentException);
            assertEquals("School is from unsupported state", e.getMessage());
        }
        assertTrue(threwException);
    }

    public void testGetStudentGrowthRatingSource() {
        School s = new School();
        String copy;

        // Milwaukee
        s.setDatabaseState(State.WI);
        copy = _controller.getStudentGrowthRatingSource(s);
        assertEquals(SchoolProfileRatingsController.STUDENT_GROWTH_RATING_SOURCE_WI, copy);

        // DC
        s.setDatabaseState(State.DC);
        copy = _controller.getStudentGrowthRatingSource(s);
        assertEquals(SchoolProfileRatingsController.STUDENT_GROWTH_RATING_SOURCE_DC, copy);

        // Indy
        s.setDatabaseState(State.IN);
        copy = _controller.getStudentGrowthRatingSource(s);
        assertEquals(SchoolProfileRatingsController.STUDENT_GROWTH_RATING_SOURCE_IN, copy);

        // unsupported state
        boolean threwException = false;
        s.setDatabaseState(State.AK);
        try {
            copy = _controller.getStudentGrowthRatingSource(s);
        } catch (Exception e) {
            threwException = true;
            assertTrue("Should have thrown IllegalArgumentException", e instanceof IllegalArgumentException);
            assertEquals("School is from unsupported state", e.getMessage());
        }
        assertTrue(threwException);
    }

    public void testGetPostSecondaryReadinessRatingSource() {
        School s = new School();
        String copy;

        // Milwaukee
        s.setDatabaseState(State.WI);
        copy = _controller.getPostSecondaryReadinessRatingSource(s);
        assertEquals(SchoolProfileRatingsController.POST_SECONDARY_READINESS_RATING_SOURCE_WI, copy);

        // DC
        s.setDatabaseState(State.DC);
        copy = _controller.getPostSecondaryReadinessRatingSource(s);
        assertEquals(SchoolProfileRatingsController.POST_SECONDARY_READINESS_RATING_SOURCE_DC, copy);

        // Indy
        s.setDatabaseState(State.IN);
        copy = _controller.getPostSecondaryReadinessRatingSource(s);
        assertEquals(SchoolProfileRatingsController.POST_SECONDARY_READINESS_RATING_SOURCE_IN, copy);

        // unsupported state
        boolean threwException = false;
        s.setDatabaseState(State.AK);
        try {
            copy = _controller.getPostSecondaryReadinessRatingSource(s);
        } catch (Exception e) {
            threwException = true;
            assertTrue("Should have thrown IllegalArgumentException", e instanceof IllegalArgumentException);
            assertEquals("School is from unsupported state", e.getMessage());
        }
        assertTrue(threwException);
    }

    public void testIsShowStateTestScoreRating() {
        Boolean showStateRating;

        // Milwaukee
        showStateRating = _controller.isShowStateTestScoreRating(State.WI);
        assertTrue(showStateRating);

        // DC
        showStateRating = _controller.isShowStateTestScoreRating(State.DC);
        assertFalse(showStateRating);

        // Indy
        showStateRating = _controller.isShowStateTestScoreRating(State.IN);
        assertTrue(showStateRating);

        // other state
        showStateRating = _controller.isShowStateTestScoreRating(State.AK);
        assertTrue(showStateRating);
    }

    public void testIsShowStateStudentGrowthRating() {
        Boolean showStateRating;

        // Milwaukee
        showStateRating = _controller.isShowStateStudentGrowthRating(State.WI);
        assertTrue(showStateRating);

        // DC
        showStateRating = _controller.isShowStateStudentGrowthRating(State.DC);
        assertFalse(showStateRating);

        // Indy
        showStateRating = _controller.isShowStateStudentGrowthRating(State.IN);
        assertTrue(showStateRating);

        // other state
        showStateRating = _controller.isShowStateStudentGrowthRating(State.AK);
        assertTrue(showStateRating);
    }

    public void testGetSection4Model() throws Exception {
        Map<String,Object> model;

        School s = new School();
        s.setDatabaseState(State.WI);
        s.setLevelCode(LevelCode.ELEMENTARY_MIDDLE_HIGH);

        model = _controller.getSection4Model(s);

        // SECTION 4 COPY

        Object section4Copy = model.get(SchoolProfileRatingsController.MODEL_SECTION_4_COPY);
        assertEquals(SchoolProfileRatingsController.SECTION_4_COPY_WI, section4Copy);

        // SECTION 4 CLIMATE DETAILS

        Object showClimateRatingDetails =
                model.get(SchoolProfileRatingsController.MODEL_SHOW_CLIMATE_RATING_DETAILS);
        assertEquals(Boolean.TRUE, showClimateRatingDetails);

        // TODO-13012 fix unit tests after placeholders replaced with actual data calls, including school with no data
        assertEquals(16, model.get(SchoolProfileRatingsController.MODEL_CLIMATE_RATING_NUM_RESPONSES));
        assertEquals(6, model.get(SchoolProfileRatingsController.MODEL_SCHOOL_ENVIRONMENT_RATING));
        assertEquals(7, model.get(SchoolProfileRatingsController.MODEL_SOCIAL_EMOTIONAL_LEARNING_RATING));
        assertEquals(6, model.get(SchoolProfileRatingsController.MODEL_HIGH_EXPECTATIONS_RATING));
        assertEquals(4, model.get(SchoolProfileRatingsController.MODEL_TEACHER_SUPPORT_RATING));
        assertEquals(7, model.get(SchoolProfileRatingsController.MODEL_FAMILY_ENGAGEMENT_RATING));
    }

    public void testGetSection4Copy() {
        School s = new School();
        String copy;

        // TODO-13012 Data unavailable copy
        //s.setDatabaseState(State.WI);
        //copy = _controller.getSection4Copy(s);
        //assertEquals(SchoolProfileRatingsController.SECTION_4_COPY_DATA_UNAVAILABLE, copy);

        // Milwaukee
        s.setDatabaseState(State.WI);
        copy = _controller.getSection4Copy(s);
        assertEquals(SchoolProfileRatingsController.SECTION_4_COPY_WI, copy);

        // DC
        s.setDatabaseState(State.DC);
        copy = _controller.getSection4Copy(s);
        assertEquals(SchoolProfileRatingsController.SECTION_4_COPY_DC, copy);

        // Indy
        s.setDatabaseState(State.IN);
        copy = _controller.getSection4Copy(s);
        assertEquals(SchoolProfileRatingsController.SECTION_4_COPY_IN, copy);

        // unsupported state
        boolean threwException = false;
        s.setDatabaseState(State.AK);
        try {
            copy = _controller.getSection4Copy(s);
        } catch (Exception e) {
            threwException = true;
            assertTrue("Should have thrown IllegalArgumentException", e instanceof IllegalArgumentException);
            assertEquals("School is from unsupported state", e.getMessage());
        }
        assertTrue(threwException);
    }

    public void testIsShowClimateRatingDetails() {
        Boolean showStateRating;

        // Milwaukee
        showStateRating = _controller.isShowClimateRatingDetails(State.WI);
        assertTrue(showStateRating);

        // DC
        showStateRating = _controller.isShowClimateRatingDetails(State.DC);
        assertFalse(showStateRating);

        // Indy
        showStateRating = _controller.isShowClimateRatingDetails(State.IN);
        assertFalse(showStateRating);

        // other state
        showStateRating = _controller.isShowClimateRatingDetails(State.AK);
        assertFalse(showStateRating);
    }

    public void testGetClimateRatingDetailsModel() {
        Map<String,Object> model;
        School s = new School();

        model = _controller.getClimateRatingDetailsModel(s);

        // TODO-13012 fix unit tests after placeholders replaced with actual data calls, including school with no data
        assertEquals(16, model.get(SchoolProfileRatingsController.MODEL_CLIMATE_RATING_NUM_RESPONSES));
        assertEquals(6, model.get(SchoolProfileRatingsController.MODEL_SCHOOL_ENVIRONMENT_RATING));
        assertEquals(7, model.get(SchoolProfileRatingsController.MODEL_SOCIAL_EMOTIONAL_LEARNING_RATING));
        assertEquals(6, model.get(SchoolProfileRatingsController.MODEL_HIGH_EXPECTATIONS_RATING));
        assertEquals(4, model.get(SchoolProfileRatingsController.MODEL_TEACHER_SUPPORT_RATING));
        assertEquals(7, model.get(SchoolProfileRatingsController.MODEL_FAMILY_ENGAGEMENT_RATING));
    }
}
