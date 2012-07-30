package gs.web.school;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// TODO-13012 simplifying assumption -- uses state only, not city
public class SchoolProfileRatingsControllerTest extends BaseControllerTestCase {
    private SchoolProfileRatingsController _controller;

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

        Set<LevelCode> nonHighOnlyLevelCodes = new HashSet<LevelCode>();
        nonHighOnlyLevelCodes.add(LevelCode.ALL_LEVELS);
        nonHighOnlyLevelCodes.add(LevelCode.MIDDLE);
        nonHighOnlyLevelCodes.add(LevelCode.MIDDLE_HIGH);
        nonHighOnlyLevelCodes.add(LevelCode.ELEMENTARY);
        nonHighOnlyLevelCodes.add(LevelCode.ELEMENTARY_MIDDLE);
        nonHighOnlyLevelCodes.add(LevelCode.ELEMENTARY_MIDDLE_HIGH);
        nonHighOnlyLevelCodes.add(LevelCode.ELEMENTARY_HIGH);
        nonHighOnlyLevelCodes.add(LevelCode.PRESCHOOL);
        nonHighOnlyLevelCodes.add(LevelCode.PRESCHOOL_ELEMENTARY);
        nonHighOnlyLevelCodes.add(LevelCode.PRESCHOOL_ELEMENTARY_MIDDLE);

        for (LevelCode levelCode : nonHighOnlyLevelCodes) {
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

        Set<LevelCode> nonHighLevelCodes = new HashSet<LevelCode>();
        nonHighLevelCodes.add(LevelCode.MIDDLE);
        nonHighLevelCodes.add(LevelCode.ELEMENTARY);
        nonHighLevelCodes.add(LevelCode.ELEMENTARY_MIDDLE);
        nonHighLevelCodes.add(LevelCode.PRESCHOOL);
        nonHighLevelCodes.add(LevelCode.PRESCHOOL_ELEMENTARY);
        nonHighLevelCodes.add(LevelCode.PRESCHOOL_ELEMENTARY_MIDDLE);

        for (LevelCode levelCode : nonHighLevelCodes) {
            s.setLevelCode(levelCode);
            model = _controller.getPostSecondaryReadinessRatingsModel(s);
            assertNull(model.get(SchoolProfileRatingsController.MODEL_POST_SECONDARY_READINESS_RATING_YEAR));
            assertNull(model.get(SchoolProfileRatingsController.MODEL_POST_SECONDARY_READINESS_RATING));
        }

        // TODO-13012 check if school has post-secondary readiness rating regardless of level code

        // schools containing high level code should have post-secondary readiness ratings

        Set<LevelCode> containsHighLevelCodes = new HashSet<LevelCode>();
        containsHighLevelCodes.add(LevelCode.HIGH);
        containsHighLevelCodes.add(LevelCode.ALL_LEVELS);
        containsHighLevelCodes.add(LevelCode.MIDDLE_HIGH);
        containsHighLevelCodes.add(LevelCode.ELEMENTARY_HIGH);
        containsHighLevelCodes.add(LevelCode.ELEMENTARY_MIDDLE_HIGH);

        for (LevelCode levelCode : containsHighLevelCodes) {
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

        s.setLevelCode(LevelCode.ELEMENTARY_HIGH);
        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS_IN, copy);

        s.setLevelCode(LevelCode.ELEMENTARY_MIDDLE_HIGH);
        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS_IN, copy);

        s.setLevelCode(LevelCode.MIDDLE_HIGH);
        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS_IN, copy);

        s.setLevelCode(LevelCode.ALL_LEVELS);
        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS_IN, copy);

        s.setLevelCode(LevelCode.ELEMENTARY);
        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        assertNull(copy);

        s.setLevelCode(LevelCode.MIDDLE);
        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        assertNull(copy);

        s.setLevelCode(LevelCode.ELEMENTARY_MIDDLE);
        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        assertNull(copy);

        s.setLevelCode(LevelCode.PRESCHOOL);
        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        assertNull(copy);

        s.setLevelCode(LevelCode.PRESCHOOL_ELEMENTARY);
        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        assertNull(copy);

        s.setLevelCode(LevelCode.PRESCHOOL_ELEMENTARY_MIDDLE);
        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        assertNull(copy);

        // unsupported state - does not contains High level code
        s.setDatabaseState(State.AK);
        s.setLevelCode(LevelCode.MIDDLE);
        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        assertNull(copy);

        // unsupported state - contains High level code
        s.setDatabaseState(State.AK);
        s.setLevelCode(LevelCode.MIDDLE_HIGH);
        boolean threwException = false;
        try {
            copy = _controller.getSection3CopyPostSecondaryReadiness(s);
        } catch (Exception e) {
            threwException = true;
            assertTrue("Should have thrown IllegalArgumentException", e instanceof IllegalArgumentException);
            assertEquals("School is from unsupported state", e.getMessage());
        }
        assertTrue(threwException);
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
        School s = new School();
        Boolean showStateRating;

        // Milwaukee
        s.setDatabaseState(State.WI);
        showStateRating = _controller.isShowStateTestScoreRating(s);
        assertTrue(showStateRating);

        // DC
        s.setDatabaseState(State.DC);
        showStateRating = _controller.isShowStateTestScoreRating(s);
        assertFalse(showStateRating);

        // Indy
        s.setDatabaseState(State.IN);
        showStateRating = _controller.isShowStateTestScoreRating(s);
        assertTrue(showStateRating);

        // other state
        s.setDatabaseState(State.AK);
        showStateRating = _controller.isShowStateTestScoreRating(s);
        assertTrue(showStateRating);
    }

    public void testIsShowStateStudentGrowthRating() {
        School s = new School();
        Boolean showStateRating;

        // Milwaukee
        s.setDatabaseState(State.WI);
        showStateRating = _controller.isShowStateStudentGrowthRating(s);
        assertTrue(showStateRating);

        // DC
        s.setDatabaseState(State.DC);
        showStateRating = _controller.isShowStateStudentGrowthRating(s);
        assertFalse(showStateRating);

        // Indy
        s.setDatabaseState(State.IN);
        showStateRating = _controller.isShowStateStudentGrowthRating(s);
        assertTrue(showStateRating);

        // other state
        s.setDatabaseState(State.AK);
        showStateRating = _controller.isShowStateStudentGrowthRating(s);
        assertTrue(showStateRating);
    }

    public void testGetSection4Model() throws Exception {

    }
}
