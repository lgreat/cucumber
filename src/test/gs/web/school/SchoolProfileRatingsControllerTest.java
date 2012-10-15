package gs.web.school;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.springframework.ui.ModelMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SchoolProfileRatingsControllerTest extends BaseControllerTestCase {
    private SchoolProfileRatingsController _controller;
    private SchoolProfileDataHelper _profileDataHelper;
    private Map<String,Object> _dataMap;

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
        _profileDataHelper = new SchoolProfileDataHelper();
        _dataMap = getSampleData();
    }

//    private static Map<String,Object> copyDataMap(Map<String,Object> srcMap) {
//        Map<String,Object> destMap = new HashMap<String,Object>();
//        for (Map.Entry<String,Object> entry : srcMap.entrySet()) {
//            entry.clo
//        }
//    }

    // see individual tests for section 1 copy, etc.
    public void testGetSection1Model() throws Exception {
        ModelMap model = new ModelMap();
        School s = new School();
        Object overallRating;
        Object overallAcademicRating;
        Object overallAcademicRatingLabel;
        Object overallClimateRating;
        Object overallClimateRatingLabel;
        Object climateRatingAvailabilityText;
        Object section1Copy;

        // OVERALL RATING

        // school with 1-10 overall rating

        s.setDatabaseState(State.WI);
        _controller.populateSection1Model(s, _dataMap,model);
        overallRating =
                model.get(SchoolProfileRatingsController.MODEL_OVERALL_RATING);
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_OVERALL_RATING), overallRating);

        // school with no overall rating

        // temporarily remove rating for this test
        Object overallRatingOrigValue = _dataMap.remove(SchoolProfileRatingsController.DATA_OVERALL_RATING);

        _controller.populateSection1Model(s, _dataMap,model);
        overallRating =
                model.get(SchoolProfileRatingsController.MODEL_OVERALL_RATING);
        assertNull(overallRating);

        // restore rating
        _dataMap.put(SchoolProfileRatingsController.DATA_OVERALL_RATING, overallRatingOrigValue);

        // ACADEMIC RATING

        // school with 1-10 academic rating

        _controller.populateSection1Model(s, _dataMap,model);
        overallAcademicRating =
                model.get(SchoolProfileRatingsController.MODEL_OVERALL_ACADEMIC_RATING);
        overallAcademicRatingLabel =
                model.get(SchoolProfileRatingsController.MODEL_OVERALL_ACADEMIC_RATING_LABEL);
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_OVERALL_ACADEMIC_RATING), overallAcademicRating);
        assertEquals(SchoolProfileRatingsController.getLabelForAcademicRating(
                Float.valueOf(_dataMap.get(SchoolProfileRatingsController.DATA_OVERALL_ACADEMIC_RATING).toString())),
                overallAcademicRatingLabel);

        // school with no academic rating

        // temporarily remove rating for this test
        Object overallAcademicRatingOrigValue = _dataMap.remove(SchoolProfileRatingsController.DATA_OVERALL_ACADEMIC_RATING);
        ModelMap emptyModel = new ModelMap();

        _controller.populateSection1Model(s, _dataMap, emptyModel);
        overallAcademicRating =
                emptyModel.get(SchoolProfileRatingsController.MODEL_OVERALL_ACADEMIC_RATING);
        overallAcademicRatingLabel =
                emptyModel.get(SchoolProfileRatingsController.MODEL_OVERALL_ACADEMIC_RATING_LABEL);
        assertNull(overallAcademicRating);
        assertNull(overallAcademicRatingLabel);
        // restore rating
        _dataMap.put(SchoolProfileRatingsController.DATA_OVERALL_ACADEMIC_RATING, overallAcademicRatingOrigValue);

        // CLIMATE RATING

        // Milwaukee - 1-10 rating

        s.setDatabaseState(State.WI);
        _controller.populateSection1Model(s, _dataMap,model);
        climateRatingAvailabilityText =
                model.get(SchoolProfileRatingsController.MODEL_CLIMATE_RATING_AVAILABILITY_TEXT);
        overallClimateRating =
                model.get(SchoolProfileRatingsController.MODEL_OVERALL_CLIMATE_RATING);
        overallClimateRatingLabel =
                model.get(SchoolProfileRatingsController.MODEL_OVERALL_CLIMATE_RATING_LABEL);
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_OVERALL_CLIMATE_RATING), overallClimateRating);
        assertEquals(SchoolProfileRatingsController.getLabelForClimateRating(
                Float.valueOf(_dataMap.get(SchoolProfileRatingsController.DATA_OVERALL_CLIMATE_RATING).toString())),
                overallClimateRatingLabel);
        assertNull(climateRatingAvailabilityText);


        // Milwaukee - no climate rating for this school

        // temporarily remove rating for this test
        Object overallClimateRatingOrigValue = _dataMap.remove(SchoolProfileRatingsController.DATA_OVERALL_CLIMATE_RATING);

        s.setDatabaseState(State.WI);
        emptyModel = new ModelMap();

        _controller.populateSection1Model(s, _dataMap,emptyModel);
        overallClimateRating =
                emptyModel.get(SchoolProfileRatingsController.MODEL_OVERALL_CLIMATE_RATING);
        overallClimateRatingLabel =
                emptyModel.get(SchoolProfileRatingsController.MODEL_OVERALL_CLIMATE_RATING_LABEL);
        climateRatingAvailabilityText =
                emptyModel.get(SchoolProfileRatingsController.MODEL_CLIMATE_RATING_AVAILABILITY_TEXT);
        assertNull(overallClimateRating);
        assertNull(overallClimateRatingLabel);
        assertEquals(SchoolProfileRatingsController.CLIMATE_RATING_AVAILABILITY_TEXT_WI, climateRatingAvailabilityText);

        // restore rating
        _dataMap.put(SchoolProfileRatingsController.DATA_OVERALL_CLIMATE_RATING, overallClimateRatingOrigValue);

        // DC
        s.setDatabaseState(State.DC);
        _controller.populateSection1Model(s, _dataMap,model);
        climateRatingAvailabilityText =
                model.get(SchoolProfileRatingsController.MODEL_CLIMATE_RATING_AVAILABILITY_TEXT);
        assertEquals(SchoolProfileRatingsController.CLIMATE_RATING_AVAILABILITY_TEXT_DC, climateRatingAvailabilityText);

        // SECTION 1 COPY

        // Milwaukee
        s.setDatabaseState(State.WI);
        _controller.populateSection1Model(s, _dataMap,model);
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

    // see individual tests for section 3 copy, section 3 post-secondary readiness, etc.
    public void testGetSection3Model() throws Exception {
        ModelMap model = new ModelMap();
        School s = new School();
        s.setDatabaseState(State.WI);
        s.setLevelCode(LevelCode.ELEMENTARY_MIDDLE_HIGH);

        _controller.populateSection3Model(s, _dataMap,model);

        // SECTION 3 COPY

        Object section3Copy = model.get(SchoolProfileRatingsController.MODEL_SECTION_3_COPY);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY, section3Copy);


        // SECTION 3 TEST SCORE RATING CHART

        Object testScoreRatingYear =
                model.get(SchoolProfileRatingsController.MODEL_TEST_SCORE_RATING_YEAR);
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_TEST_SCORE_RATING_YEAR), testScoreRatingYear);
        Object schoolTestScoreRating =
                model.get(SchoolProfileRatingsController.MODEL_SCHOOL_TEST_SCORE_RATING);
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_SCHOOL_TEST_SCORE_RATING), schoolTestScoreRating);
        Object cityTestScoreRating =
                model.get(SchoolProfileRatingsController.MODEL_CITY_TEST_SCORE_RATING);
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_CITY_TEST_SCORE_RATING), cityTestScoreRating);
        Object stateTestScoreRating =
                model.get(SchoolProfileRatingsController.MODEL_STATE_TEST_SCORE_RATING);
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_STATE_TEST_SCORE_RATING), stateTestScoreRating);

        Object showStateTestScoreRating =
                model.get(SchoolProfileRatingsController.MODEL_SHOW_STATE_TEST_SCORE_RATING);
        assertEquals(Boolean.TRUE, showStateTestScoreRating);

        // SECTION 3 STUDENT GROWTH RATING CHART

        Object studentGrowthRatingYear =
                model.get(SchoolProfileRatingsController.MODEL_STUDENT_GROWTH_RATING_YEAR);
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_STUDENT_GROWTH_RATING_YEAR), studentGrowthRatingYear);
        Object schoolStudentGrowthRating =
                model.get(SchoolProfileRatingsController.MODEL_SCHOOL_STUDENT_GROWTH_RATING);
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_SCHOOL_STUDENT_GROWTH_RATING), schoolStudentGrowthRating);
        Object cityStudentGrowthRating =
                model.get(SchoolProfileRatingsController.MODEL_CITY_STUDENT_GROWTH_RATING);
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_CITY_STUDENT_GROWTH_RATING), cityStudentGrowthRating);
        Object stateStudentGrowthRating =
                model.get(SchoolProfileRatingsController.MODEL_STATE_STUDENT_GROWTH_RATING);
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_STATE_STUDENT_GROWTH_RATING), stateStudentGrowthRating);

        Object showStateStudentGrowthRating =
                model.get(SchoolProfileRatingsController.MODEL_SHOW_STATE_STUDENT_GROWTH_RATING);
        assertEquals(Boolean.TRUE, showStateStudentGrowthRating);

        // SECTION 3 POST-SECONDARY READINESS RATING CHART

        Object postSecondaryReadinessRatingYear =
                model.get(SchoolProfileRatingsController.MODEL_POST_SECONDARY_READINESS_RATING_YEAR);
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_POST_SECONDARY_READINESS_RATING_YEAR), postSecondaryReadinessRatingYear);
        Object postSecondaryReadinessRating =
                model.get(SchoolProfileRatingsController.MODEL_SCHOOL_POST_SECONDARY_READINESS_RATING);
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_SCHOOL_POST_SECONDARY_READINESS_RATING), postSecondaryReadinessRating);

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
        ModelMap model = new ModelMap();
        School s = new School();

        // check if school has student growth rating regardless of level code

        // temporarily remove rating for this test
        Object overallSchoolStudentGrowthRatingOrigValue = _dataMap.remove(SchoolProfileRatingsController.DATA_SCHOOL_STUDENT_GROWTH_RATING);

        s.setLevelCode(LevelCode.ELEMENTARY);
        _controller.populateStudentGrowthRatingsModel(s, true, _dataMap,model);
        assertNull(model.get(SchoolProfileRatingsController.MODEL_STUDENT_GROWTH_RATING_YEAR));
        assertNull(model.get(SchoolProfileRatingsController.MODEL_SCHOOL_STUDENT_GROWTH_RATING));

        // restore rating
        _dataMap.put(SchoolProfileRatingsController.DATA_SCHOOL_STUDENT_GROWTH_RATING, overallSchoolStudentGrowthRatingOrigValue);


        // schools with non-high-only level codes should have student growth ratings

        for (LevelCode levelCode : NON_HIGH_ONLY_LEVEL_CODES) {
            s.setLevelCode(levelCode);
            _controller.populateStudentGrowthRatingsModel(s, true, _dataMap,model);
            assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_STUDENT_GROWTH_RATING_YEAR),
                    model.get(SchoolProfileRatingsController.MODEL_STUDENT_GROWTH_RATING_YEAR));
            assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_SCHOOL_STUDENT_GROWTH_RATING),
                    model.get(SchoolProfileRatingsController.MODEL_SCHOOL_STUDENT_GROWTH_RATING));
            assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_CITY_STUDENT_GROWTH_RATING),
                    model.get(SchoolProfileRatingsController.MODEL_CITY_STUDENT_GROWTH_RATING));
            assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_STATE_STUDENT_GROWTH_RATING),
                    model.get(SchoolProfileRatingsController.MODEL_STATE_STUDENT_GROWTH_RATING));
        }

        // omit state rating
        model = new ModelMap();
        _controller.populateStudentGrowthRatingsModel(s, false, _dataMap,model);
        s.setLevelCode(LevelCode.ALL_LEVELS);
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_STUDENT_GROWTH_RATING_YEAR),
                model.get(SchoolProfileRatingsController.MODEL_STUDENT_GROWTH_RATING_YEAR));
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_SCHOOL_STUDENT_GROWTH_RATING),
                model.get(SchoolProfileRatingsController.MODEL_SCHOOL_STUDENT_GROWTH_RATING));
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_CITY_STUDENT_GROWTH_RATING),
                model.get(SchoolProfileRatingsController.MODEL_CITY_STUDENT_GROWTH_RATING));
        assertNull(model.get(SchoolProfileRatingsController.MODEL_STATE_STUDENT_GROWTH_RATING));
    }

    public void testGetPostSecondaryReadinessRatingsModel() {
        ModelMap model = new ModelMap();
        School s = new School();

        // schools with no high level code: no post-secondary readiness ratings

        for (LevelCode levelCode : NON_HIGH_LEVEL_CODES) {
            s.setLevelCode(levelCode);
            _controller.populatePostSecondaryReadinessRatingsModel(s,true, _dataMap,model);
            assertNull(model.get(SchoolProfileRatingsController.MODEL_POST_SECONDARY_READINESS_RATING_YEAR));
            assertNull(model.get(SchoolProfileRatingsController.MODEL_SCHOOL_POST_SECONDARY_READINESS_RATING));
        }

        // check if school has post-secondary readiness rating regardless of level code

        // temporarily remove rating for this test
        Object postSecondaryReadinessRatingOrigValue = _dataMap.remove(SchoolProfileRatingsController.DATA_SCHOOL_POST_SECONDARY_READINESS_RATING);

        s.setLevelCode(LevelCode.ELEMENTARY);
        _controller.populatePostSecondaryReadinessRatingsModel(s,true, _dataMap,model);
        assertNull(model.get(SchoolProfileRatingsController.DATA_POST_SECONDARY_READINESS_RATING_YEAR));
        assertNull(model.get(SchoolProfileRatingsController.DATA_SCHOOL_POST_SECONDARY_READINESS_RATING));

        // restore rating
        _dataMap.put(SchoolProfileRatingsController.DATA_SCHOOL_POST_SECONDARY_READINESS_RATING, postSecondaryReadinessRatingOrigValue);

        // schools containing high level code should have post-secondary readiness ratings

        for (LevelCode levelCode : CONTAINS_HIGH_LEVEL_CODES) {
            s.setLevelCode(levelCode);
            _controller.populatePostSecondaryReadinessRatingsModel(s,true, _dataMap,model);
            assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_POST_SECONDARY_READINESS_RATING_YEAR),
                    model.get(SchoolProfileRatingsController.MODEL_POST_SECONDARY_READINESS_RATING_YEAR));
            assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_SCHOOL_POST_SECONDARY_READINESS_RATING),
                    model.get(SchoolProfileRatingsController.MODEL_SCHOOL_POST_SECONDARY_READINESS_RATING));
        }
    }

    public void testGetSection3Copy() {
        String copy;

        // Milwaukee, no climate rating - data unavailable copy

        // temporarily remove rating for this test
        Object overallAcademicRatingOrigValue = _dataMap.remove(SchoolProfileRatingsController.DATA_OVERALL_ACADEMIC_RATING);

        copy = _controller.getSection3Copy( _dataMap);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_DATA_UNAVAILABLE, copy);

        // restore rating
        _dataMap.put(SchoolProfileRatingsController.DATA_OVERALL_ACADEMIC_RATING, overallAcademicRatingOrigValue);

        copy = _controller.getSection3Copy(_dataMap);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY, copy);
    }

//    public void testGetSection3CopyPostSecondaryReadiness() {
//        School s = new School();
//        String copy;
//
//        s.setLevelCode(LevelCode.HIGH);
//
//        // Milwaukee
//        s.setDatabaseState(State.WI);
//        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
//        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS_WI, copy);
//
//        // DC
//        s.setDatabaseState(State.DC);
//        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
//        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS_DC, copy);
//
//        // Indy
//        s.setDatabaseState(State.IN);
//        copy = _controller.getSection3CopyPostSecondaryReadiness(s);
//        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS_IN, copy);
//
//        // any school with level code containing 'high' should have post-secondary readiness copy
//
//        for (LevelCode levelCode : CONTAINS_HIGH_LEVEL_CODES) {
//            s.setLevelCode(levelCode);
//            copy = _controller.getSection3CopyPostSecondaryReadiness(s);
//            assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS_IN, copy);
//        }
//
//        for (LevelCode levelCode : NON_HIGH_LEVEL_CODES) {
//            s.setLevelCode(levelCode);
//            copy = _controller.getSection3CopyPostSecondaryReadiness(s);
//            assertNull(copy);
//        }
//
//        // unsupported state - contains High level code
//        s.setDatabaseState(State.AK);
//        boolean threwException = false;
//        for (LevelCode levelCode : CONTAINS_HIGH_LEVEL_CODES) {
//            s.setLevelCode(levelCode);
//            try {
//
//                copy = _controller.getSection3CopyPostSecondaryReadiness(s);
//            } catch (Exception e) {
//                threwException = true;
//                assertTrue("Should have thrown IllegalArgumentException", e instanceof IllegalArgumentException);
//                assertEquals("School is from unsupported state", e.getMessage());
//            }
//            assertTrue(threwException);
//        }
//
//        // unsupported state - does not contain High level code
//        s.setDatabaseState(State.AK);
//        for (LevelCode levelCode : NON_HIGH_LEVEL_CODES) {
//            s.setLevelCode(levelCode);
//            copy = _controller.getSection3CopyPostSecondaryReadiness(s);
//            assertNull(copy);
//        }
//
//    }

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
        ModelMap model = new ModelMap();

        School s = new School();
        s.setDatabaseState(State.WI);
        s.setLevelCode(LevelCode.ELEMENTARY_MIDDLE_HIGH);

        _controller.populateSection4Model(s, _dataMap,model);

        // SECTION 4 COPY

        Object section4Copy = model.get(SchoolProfileRatingsController.MODEL_SECTION_4_COPY);
        assertEquals(SchoolProfileRatingsController.SECTION_4_COPY_WI, section4Copy);

        // SECTION 4 CLIMATE DETAILS

        Object showClimateRatingDetails =
                model.get(SchoolProfileRatingsController.MODEL_SHOW_CLIMATE_RATING_DETAILS);
        assertEquals(Boolean.TRUE, showClimateRatingDetails);

        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_CLIMATE_RATING_NUM_RESPONSES),
                model.get(SchoolProfileRatingsController.MODEL_CLIMATE_RATING_NUM_RESPONSES));
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_SCHOOL_ENVIRONMENT_RATING),
                model.get(SchoolProfileRatingsController.MODEL_SCHOOL_ENVIRONMENT_RATING));
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_SOCIAL_EMOTIONAL_LEARNING_RATING),
                model.get(SchoolProfileRatingsController.MODEL_SOCIAL_EMOTIONAL_LEARNING_RATING));
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_HIGH_EXPECTATIONS_RATING),
                model.get(SchoolProfileRatingsController.MODEL_HIGH_EXPECTATIONS_RATING));
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_TEACHER_SUPPORT_RATING),
                model.get(SchoolProfileRatingsController.MODEL_TEACHER_SUPPORT_RATING));
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_FAMILY_ENGAGEMENT_RATING),
                model.get(SchoolProfileRatingsController.MODEL_FAMILY_ENGAGEMENT_RATING));
    }

    public void testGetSection4Copy() {
        School s = new School();
        String copy;

        // Milwaukee, no climate rating - data unavailable copy

        // temporarily remove rating for this test
        Object overallClimateRatingOrigValue = _dataMap.remove(SchoolProfileRatingsController.DATA_OVERALL_CLIMATE_RATING);

        s.setDatabaseState(State.WI);
        copy = _controller.getSection4Copy(s, _dataMap);
        assertEquals(SchoolProfileRatingsController.SECTION_4_COPY_WI + " " + SchoolProfileRatingsController.SECTION_4_COPY_DATA_UNAVAILABLE, copy);

        // restore rating
        _dataMap.put(SchoolProfileRatingsController.DATA_OVERALL_CLIMATE_RATING, overallClimateRatingOrigValue);

        // Milwaukee
        s.setDatabaseState(State.WI);
        copy = _controller.getSection4Copy(s, _dataMap);
        assertEquals(SchoolProfileRatingsController.SECTION_4_COPY_WI, copy);

        // DC
        s.setDatabaseState(State.DC);
        copy = _controller.getSection4Copy(s, _dataMap);
        assertEquals(SchoolProfileRatingsController.SECTION_4_COPY_DC, copy);

        // Indy
        s.setDatabaseState(State.IN);
        copy = _controller.getSection4Copy(s, _dataMap);
        assertEquals(SchoolProfileRatingsController.SECTION_4_COPY_IN, copy);

        // unsupported state
        boolean threwException = false;
        s.setDatabaseState(State.AK);
        try {
            copy = _controller.getSection4Copy(s, _dataMap);
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

        model = _controller.getClimateRatingDetailsModel(s, _dataMap);

        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_CLIMATE_RATING_NUM_RESPONSES),
                model.get(SchoolProfileRatingsController.MODEL_CLIMATE_RATING_NUM_RESPONSES));
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_SCHOOL_ENVIRONMENT_RATING),
                model.get(SchoolProfileRatingsController.MODEL_SCHOOL_ENVIRONMENT_RATING));
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_SOCIAL_EMOTIONAL_LEARNING_RATING),
                model.get(SchoolProfileRatingsController.MODEL_SOCIAL_EMOTIONAL_LEARNING_RATING));
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_HIGH_EXPECTATIONS_RATING),
                model.get(SchoolProfileRatingsController.MODEL_HIGH_EXPECTATIONS_RATING));
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_TEACHER_SUPPORT_RATING),
                model.get(SchoolProfileRatingsController.MODEL_TEACHER_SUPPORT_RATING));
        assertEquals(_dataMap.get(SchoolProfileRatingsController.DATA_FAMILY_ENGAGEMENT_RATING),
                model.get(SchoolProfileRatingsController.MODEL_FAMILY_ENGAGEMENT_RATING));
    }

    // sample data: used by unit test and controller

    public static Map<String,Object> getSampleData() {
        Map<String,Object> dataMap = new HashMap<String,Object>();

        dataMap.put(SchoolProfileDataHelper.DATA_OVERALL_RATING, 10);
        dataMap.put(SchoolProfileDataHelper.DATA_OVERALL_ACADEMIC_RATING, 9.5);
        dataMap.put(SchoolProfileDataHelper.DATA_OVERALL_CLIMATE_RATING, 6);

        dataMap.put(SchoolProfileDataHelper.DATA_TEST_SCORE_RATING_YEAR, 2012);
        dataMap.put(SchoolProfileDataHelper.DATA_SCHOOL_TEST_SCORE_RATING, 9);
        dataMap.put(SchoolProfileRatingsController.DATA_CITY_TEST_SCORE_RATING, 5);
        dataMap.put(SchoolProfileDataHelper.DATA_STATE_TEST_SCORE_RATING, 3);

        dataMap.put(SchoolProfileDataHelper.DATA_STUDENT_GROWTH_RATING_YEAR, 2012);
        dataMap.put(SchoolProfileDataHelper.DATA_SCHOOL_STUDENT_GROWTH_RATING, 9);
        dataMap.put(SchoolProfileRatingsController.DATA_CITY_STUDENT_GROWTH_RATING, 5);
        dataMap.put(SchoolProfileDataHelper.DATA_STATE_STUDENT_GROWTH_RATING, 3);

        dataMap.put(SchoolProfileDataHelper.DATA_POST_SECONDARY_READINESS_RATING_YEAR, 2012);
        dataMap.put(SchoolProfileDataHelper.DATA_SCHOOL_POST_SECONDARY_READINESS_RATING, 8);
        dataMap.put(SchoolProfileRatingsController.DATA_CITY_POST_SECONDARY_READINESS_RATING, 3);
        dataMap.put(SchoolProfileDataHelper.DATA_STATE_POST_SECONDARY_READINESS_RATING, 4);

        dataMap.put(SchoolProfileDataHelper.DATA_CLIMATE_RATING_NUM_RESPONSES, 16);

        dataMap.put(SchoolProfileDataHelper.DATA_SCHOOL_ENVIRONMENT_RATING, 6);
        dataMap.put(SchoolProfileDataHelper.DATA_SOCIAL_EMOTIONAL_LEARNING_RATING, 7);
        dataMap.put(SchoolProfileDataHelper.DATA_HIGH_EXPECTATIONS_RATING, 6);
        dataMap.put(SchoolProfileDataHelper.DATA_TEACHER_SUPPORT_RATING, 4);
        dataMap.put(SchoolProfileDataHelper.DATA_FAMILY_ENGAGEMENT_RATING, 7);

        return dataMap;
    }
}
