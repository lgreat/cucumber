package gs.web.school;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;

import java.util.Map;

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
        Object section3Copy;
        Object section3CopyPostSecondaryReadiness;

        s.setDatabaseState(State.WI);
        s.setLevelCode(LevelCode.HIGH);
        model = _controller.getSection3Model(s);

        // SECTION 3 COPY

        section3Copy = model.get(SchoolProfileRatingsController.MODEL_SECTION_3_COPY);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_WI, section3Copy);

        // SECTION 3 POST-SECONDARY READINESS COPY

        section3CopyPostSecondaryReadiness =
                model.get(SchoolProfileRatingsController.MODEL_SECTION_3_COPY_POST_SECONDARY_READINESS);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS_WI,
                section3CopyPostSecondaryReadiness);

        // SECTION 3 SOURCES

        section3CopyPostSecondaryReadiness =
                model.get(SchoolProfileRatingsController.MODEL_POST_SECONDARY_READINESS_RATING_SOURCE);
        assertEquals(SchoolProfileRatingsController.POST_SECONDARY_READINESS_RATING_SOURCE_WI,
                section3CopyPostSecondaryReadiness);


        // TODO-13012 section 3 sources
        // TODO-FIXME
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

    public void testGetSection4Model() throws Exception {

    }
}
