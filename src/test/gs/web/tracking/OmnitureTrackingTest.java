package gs.web.tracking;

import gs.web.BaseControllerTestCase;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class OmnitureTrackingTest  extends BaseControllerTestCase {

    private OmnitureTracking _tracking;

    public void setUp() throws Exception {
        super.setUp();

        _tracking = new OmnitureTracking(getRequest(), getResponse());
    }

    public void testAddEvar() {
        _tracking.addEvar(new OmnitureTracking.Evar(OmnitureTracking.EvarNumber.RegistrationSegment, "unittest"));
        assertNotNull("Expect property to be set", _tracking.getSubCookie().getProperty("eVar7"));
        assertEquals("unittest", _tracking.getSubCookie().getProperty("eVar7"));
        _tracking.addEvar(new OmnitureTracking.Evar(OmnitureTracking.EvarNumber.RegistrationSegment, "overwrite"));
        assertEquals("overwrite", _tracking.getSubCookie().getProperty("eVar7"));
    }

    public void testAddNullEvar() {
        try {
            _tracking.addEvar(null);
            fail("Expect illegal argument exception for null parameter");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    public void testAddSuccessEvent() {
        _tracking.addSuccessEvent(OmnitureTracking.SuccessEvent.CommunityRegistration);
        assertNotNull("Expect property to be set", _tracking.getSubCookie().getProperty("events"));
        assertEquals("event6;", _tracking.getSubCookie().getProperty("events"));
        _tracking.addSuccessEvent(OmnitureTracking.SuccessEvent.NewNewsLetterSubscriber);
        assertEquals("event6;event11;", _tracking.getSubCookie().getProperty("events"));
    }

    public void testAddOmnitureInformationToString_NoPreviousEvents(){
        String existingEvents = "";

        String result = OmnitureTracking.addOmnitureInformationToString(OmnitureTracking.SuccessEvent.ParentSurvey,existingEvents);
        assertEquals("Expect only the parent survey event", OmnitureTracking.SuccessEvent.ParentSurvey.toOmnitureString(), result);
    }

    public void testAddOmnitureInformationToString_OtherEventsNonInclusiveOfNewEvent(){
        String existingEvents = "event1;event101;";

        String result = OmnitureTracking.addOmnitureInformationToString(OmnitureTracking.SuccessEvent.ParentSurvey,existingEvents);
        assertEquals("Expect the new event to be concatenated to the end of the existing events string",
                     existingEvents + OmnitureTracking.SuccessEvent.ParentSurvey.toOmnitureString(), result);
    }

    public void testAddOmnitureInformationToString_OtherEventsInclusiveOfNewEvent(){
        String existingEvents = "event1;event101;" + OmnitureTracking.SuccessEvent.ParentSurvey.toOmnitureString();

        String result = OmnitureTracking.addOmnitureInformationToString(OmnitureTracking.SuccessEvent.ParentSurvey,existingEvents);
        assertEquals("Expect the new event to be concatenated to the end of the existing events string",
                     existingEvents, result);

    }

    public void testAddOmnitureInformationToString_WithBothParametersNull(){

        String result = OmnitureTracking.addOmnitureInformationToString(null,null);
        assertEquals("Expect an empty string when both params are null",
                     "", result);
    }

    public void testAddOmnitureInformationToString_WithDestinationParameterNull(){

        String result = OmnitureTracking.addOmnitureInformationToString(OmnitureTracking.SuccessEvent.ParentSurvey,null);
        assertEquals("Expect the string value of the newly added success event only",
                     OmnitureTracking.SuccessEvent.ParentSurvey.toOmnitureString(), result);
    }


    public void testAddOmnitureInformationToString_WithSuccessEventParameterNull(){
        String existingEvents = "event1;event101;";
        String result = OmnitureTracking.addOmnitureInformationToString(null,existingEvents);
        assertEquals("Expect the non null destination string only",
                     existingEvents, result);
    }

    public void testSuccessEventEnumValues(){
        assertEquals(OmnitureTracking.SuccessEvent.CommunityRegistration.toOmnitureString(), "event6;");
        assertEquals(OmnitureTracking.SuccessEvent.ArticleView.toOmnitureString(), "event7;");
        assertEquals(OmnitureTracking.SuccessEvent.ParentRating.toOmnitureString(), "event8;");
        assertEquals(OmnitureTracking.SuccessEvent.ParentReview.toOmnitureString(), "event9;");
        assertEquals(OmnitureTracking.SuccessEvent.ParentSurvey.toOmnitureString(), "event10;");
        assertEquals(OmnitureTracking.SuccessEvent.NewNewsLetterSubscriber.toOmnitureString(), "event11;");
    }

    public void testEvarEnumValues() {
        assertEquals(OmnitureTracking.EvarNumber.RegistrationSegment.getNumber(), 7);
    }
}
