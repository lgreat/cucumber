package gs.web.tracking;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: Aug 4, 2008
 * Time: 1:08:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class OmnitureSuccessEventTest extends TestCase {


    
    public void testAddEvent_NoPreviousEvents(){
        String existingEvents = "";

        String result = OmnitureSuccessEvent.addEvent(OmnitureSuccessEvent.SuccessEvent.ParentSurvey,existingEvents);
        assertEquals("Expect only the parent survey event", OmnitureSuccessEvent.SuccessEvent.ParentSurvey.toOmnitureSuccessEvent(), result);
    }

    public void testAddEvent_OtherEventsNonInclusiveOfNewEvent(){
        String existingEvents = "event1;event101;";

        String result = OmnitureSuccessEvent.addEvent(OmnitureSuccessEvent.SuccessEvent.ParentSurvey,existingEvents);
        assertEquals("Expect the new event to be concatenated to the end of the existing events string",
                     existingEvents + OmnitureSuccessEvent.SuccessEvent.ParentSurvey.toOmnitureSuccessEvent(), result);
    }

    public void testAddEvent_OtherEventsInclusiveOfNewEvent(){
        String existingEvents = "event1;event101;" + OmnitureSuccessEvent.SuccessEvent.ParentSurvey.toOmnitureSuccessEvent();

        String result = OmnitureSuccessEvent.addEvent(OmnitureSuccessEvent.SuccessEvent.ParentSurvey,existingEvents);
        assertEquals("Expect the new event to be concatenated to the end of the existing events string",
                     existingEvents, result);

    }

    public void testAddEvent_WithBothParametersNull(){

        String result = OmnitureSuccessEvent.addEvent(null,null);
        assertEquals("Expect an empty string when both params are null",
                     "", result);
    }

    public void testAddEvent_WithDestinationParameterNull(){

        String result = OmnitureSuccessEvent.addEvent(OmnitureSuccessEvent.SuccessEvent.ParentSurvey,null);
        assertEquals("Expect the string value of the newly added success event only",
                     OmnitureSuccessEvent.SuccessEvent.ParentSurvey.toOmnitureSuccessEvent(), result);
    }


    public void testAddEvent_WithSuccessEventParameterNull(){
        String existingEvents = "event1;event101;";
        String result = OmnitureSuccessEvent.addEvent(null,existingEvents);
        assertEquals("Expect the non null destination string only",
                     existingEvents, result);
    }

    public void testEnumValues(){
        assertEquals(OmnitureSuccessEvent.SuccessEvent.CommunityRegistration.toOmnitureSuccessEvent(), "event6;");
        assertEquals(OmnitureSuccessEvent.SuccessEvent.ArticleView.toOmnitureSuccessEvent(), "event7;");
        assertEquals(OmnitureSuccessEvent.SuccessEvent.ParentRating.toOmnitureSuccessEvent(), "event8;");
        assertEquals(OmnitureSuccessEvent.SuccessEvent.ParentReview.toOmnitureSuccessEvent(), "event9;");
        assertEquals(OmnitureSuccessEvent.SuccessEvent.ParentSurvey.toOmnitureSuccessEvent(), "event10;");
        assertEquals(OmnitureSuccessEvent.SuccessEvent.NewNewsLetterSubscriber.toOmnitureSuccessEvent(), "event11;");
    }
}
