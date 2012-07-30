package gs.web.school;

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

        // CLIMATE RATING BLOCK

        // Milwaukee - 1-10 rating
        s.setDatabaseState(State.WI);
        model = _controller.getSection1Model(s);
        climateRatingAvailabilityText =
                model.get(SchoolProfileRatingsController.MODEL_CLIMATE_RATING_AVAILABILITY_TEXT);
        overallClimateRating =
                model.get(SchoolProfileRatingsController.MODEL_OVERALL_CLIMATE_RATING);
        overallClimateRatingLabel =
                model.get(SchoolProfileRatingsController.MODEL_OVERALL_CLIMATE_RATING_LABEL);
        // TODO-13012 MI 1-10 rating
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
        assertEquals("Coming 2013", climateRatingAvailabilityText);

        // Indy
        s.setDatabaseState(State.IN);
        model = _controller.getSection1Model(s);
        climateRatingAvailabilityText =
                model.get(SchoolProfileRatingsController.MODEL_CLIMATE_RATING_AVAILABILITY_TEXT);
        assertEquals("Coming soon", climateRatingAvailabilityText);

        // SECTION 1 COPY

        // Milwaukee
        s.setDatabaseState(State.WI);
        model = _controller.getSection1Model(s);
        section1Copy = model.get(SchoolProfileRatingsController.MODEL_SECTION_1_COPY);
        assertEquals(SchoolProfileRatingsController.SECTION_1_COPY_WI, section1Copy);

        // DC
        s.setDatabaseState(State.DC);
        model = _controller.getSection1Model(s);
        section1Copy = model.get(SchoolProfileRatingsController.MODEL_SECTION_1_COPY);
        assertEquals(SchoolProfileRatingsController.SECTION_1_COPY_DC, section1Copy);

        // Indy
        s.setDatabaseState(State.IN);
        model = _controller.getSection1Model(s);
        section1Copy = model.get(SchoolProfileRatingsController.MODEL_SECTION_1_COPY);
        assertEquals(SchoolProfileRatingsController.SECTION_1_COPY_IN, section1Copy);
    }

    public void testGetSection2Model() throws Exception {

    }

    public void testGetSection3Model() throws Exception {
        Map<String,Object> model;
        School s = new School();
        Object section3Copy;
        Object section3CopyPostSecondaryReadiness;

        // SECTION 3 COPY

        // Milwaukee
        s.setDatabaseState(State.WI);
        model = _controller.getSection3Model(s);
        section3Copy = model.get(SchoolProfileRatingsController.MODEL_SECTION_3_COPY);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_WI, section3Copy);

        // DC
        s.setDatabaseState(State.DC);
        model = _controller.getSection3Model(s);
        section3Copy = model.get(SchoolProfileRatingsController.MODEL_SECTION_3_COPY);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_DC, section3Copy);

        // Indy
        s.setDatabaseState(State.IN);
        model = _controller.getSection3Model(s);
        section3Copy = model.get(SchoolProfileRatingsController.MODEL_SECTION_3_COPY);
        assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_IN, section3Copy);

        // TODO-13012 Data unavailable copy
        // TODO-FIXME

        // SECTION 3 POST-SECONDARY READINESS COPY

        // TODO-13012 post-secondary readiness copy - show or not show
        model = _controller.getSection3Model(s);
        section3CopyPostSecondaryReadiness =
                model.get(SchoolProfileRatingsController.MODEL_SECTION_3_COPY_POST_SECONDARY_READINESS);
        // TODO-FIXME
        //assertEquals(SchoolProfileRatingsController.SECTION_3_COPY_POST_SECONDARY_READINESS,
        //        section3CopyPostSecondaryReadiness);

        // TODO-13012 section 3 sources
        // TODO-FIXME
    }

    public void testGetSection4Model() throws Exception {

    }
}
