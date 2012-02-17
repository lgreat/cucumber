package gs.web.community;


import gs.data.util.table.ITableRow;
import gs.data.util.table.HashMapTableRow;
import gs.data.geo.City;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import static gs.web.community.CommunityQuestionPromoController.*;
import static org.easymock.classextension.EasyMock.*;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: Oct 13, 2008
 * Time: 11:10:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class CommunityTemplatedQuestionPromoControllerTest extends CommunityQuestionPromoControllerTest{


    private CommunityTemplatedQuestionPromoController _controller;


    public void setUp() throws Exception {
        super.setUp();
        _controller = new CommunityTemplatedQuestionPromoController();
        _sessionContext = createMock(SessionContext.class) ;
    }


    public void testReplaceTargets_WithNoTarget(){

        Map<String, String> targets = createTargets("New York", "New York City");

        String noTarget = "This is a string with out a target";
        assertEquals("No target in the string should return the original string",noTarget, _controller.replaceTargets(targets, noTarget, false));
    }


    public void testReplaceTargets_WithValidCityTarget(){

        Map<String, String> targets = createTargets("New York", "New York City");
        String original = "This is a string with a target: <city>";
        String expected = "This is a string with a target: New York City";
        assertEquals("Target <city> should be replaced with 'New York City'",expected, _controller.replaceTargets(targets,original, false));
    }

    public void testReplaceTargets_WithValidCityTargetInALink(){

        Map<String, String> targets = createTargets("New York", "New York City");
        String original = "/urlBase/<city>/something/else/";
        String expected = "/urlBase/New-York-City/something/else/";
        assertEquals("Target <city> should be replaced with 'New-York-City'",expected, _controller.replaceTargets(targets, original, true));
    }


    public void testReplaceTargets_WithValidStateTarget(){

        Map<String, String> targets = createTargets("New York", "New York City");
        String original = "This is a string with a target: <state>";
        String expected = "This is a string with a target: New York";
        assertEquals("Target <state> should be replaced with 'New York'",expected, _controller.replaceTargets(targets, original, false));
    }

    public void testReplaceTargets_WithValidStateTargetInALink(){

        Map<String, String> targets = createTargets("New York", "New York City");
        String original = "/urlBase/<state>/something/else/";
        String expected = "/urlBase/New-York/something/else/";
        assertEquals("Target <state> should be replaced with 'New-York'",expected, _controller.replaceTargets(targets, original, true));
    }



    public void testReplaceTargets_WithValidCityAndStateTargets(){

        Map<String, String> targets = createTargets("New York", "New York City");
        String original = "This is a string with a target: <city>, <state>";
        String expected = "This is a string with a target: New York City, New York";
        assertEquals("Target <city> should be replaced with 'New York City' and <state> target replace with 'New York'",expected, _controller.replaceTargets(targets, original, false));
    }

    public void testReplaceTargets_WithValidCityAndStateTargetsInALink(){

        Map<String, String> targets = createTargets("New York", "New York City");
        String original = "/urlBase/<state>/<city>/something/else/";
        String expected = "/urlBase/New-York/New-York-City/something/else/";
        assertEquals("Target <city> should be replaced with 'New-York-City', and Target <state> should be replaced with 'New-York'",expected, _controller.replaceTargets(targets, original, true));
    }

    public void testReplaceTargets_WithValidCityWithAnUnderscoreTargetInALink(){

        Map<String, String> targets = createTargets("California", "Cardiff-By-the-Sea");
        String original = "/urlBase/<city>/something/else/";
        String expected = "/urlBase/Cardiff_By_the_Sea/something/else/";
        assertEquals("Target <city> should be replaced with 'Cardiff_By_the_Sea'",expected, _controller.replaceTargets(targets, original, true));
    }


    public void testReplaceTargets_WithValidCityWithAnApostroheTargetInALink(){
        Map<String, String> targets = createTargets("Idaho", "Cour d' Alene");
        String original = "/urlBase/<city>/something/else/";
        String expected = "/urlBase/Cour-d%27-Alene/something/else/";
        assertEquals("Target <city> should be replaced with 'Cour-d%27-Alene'",expected, _controller.replaceTargets(targets, original, true));
    }

    public void testUrlEncode(){
        String original = "Cour-d'-Alene";
        String expected = "Cour-d%27-Alene";
        assertEquals("Target <city> should be replaced with 'Cour-d%27-Alene'",expected,_controller.urlEncode(original));
    }



    public void testIsTemplated_False(){
        String textWithoutTarget = "this is text without a target";
        String linkWithoutTarget = "/someURLBase/with/some/more" ;
        String linkTextWithoutTarget = "This is link text";

        Set<Object> columnSet = new HashSet<Object>();
        columnSet.add("text");
        columnSet.add("link");
        columnSet.add("linktext");

        ITableRow row = createMock(ITableRow.class);
        expect(row.getColumnNames()).andReturn(new HashSet<Object>(columnSet));
        expect(row.get("text")).andReturn(textWithoutTarget);
        expect(row.get("link")).andReturn(linkWithoutTarget);
        expect(row.get("linktext")).andReturn(linkTextWithoutTarget);
        replay(row);
        assertFalse("Since NONE of the fields are templated, the row in not templated", _controller.isTemplated(row));
        verify(row);
    }

    public void testIsTemplated_LinkTextTrue(){
        String textWithoutTarget = "this is text without a target";
        String linkWithoutTarget = "/someURLBase/with/some/more" ;
        String linkTextWithoutTarget = "This is link text with <state>";

        Set<Object> columnSet = new HashSet<Object>();
        columnSet.add("text");
        columnSet.add("link");
        columnSet.add("linktext");

        ITableRow row = createMock(ITableRow.class);
        expect(row.getColumnNames()).andReturn(new HashSet<Object>(columnSet));
        expect(row.get("text")).andReturn(textWithoutTarget).anyTimes();
        expect(row.get("link")).andReturn(textWithoutTarget).anyTimes();
        expect(row.get("linktext")).andReturn(linkTextWithoutTarget);
        replay(row);
        assertTrue("Since linktext is templated, the row is templated", _controller.isTemplated(row));
        verify(row);
    }

    public void testIsTemplated_TextCityTrue(){
        String textWithoutTarget = "this is text without a target<city>";

        Set<Object> columnSet = new HashSet<Object>();
        columnSet.add("text");
        columnSet.add("link");
        columnSet.add("linktext");

        ITableRow row = createMock(ITableRow.class);
        expect(row.getColumnNames()).andReturn(new HashSet<Object>(columnSet));
        expect(row.get("text")).andReturn(textWithoutTarget);

        replay(row);
        assertTrue("Since linktext is templated, the row is templated", _controller.isTemplated(row));
        verify(row);
    }

    public void testIsTemplated_TextStateTrue(){
        String textWithoutTarget = "this is text without a target<state>";

        Set<Object> columnSet = new HashSet<Object>();
        columnSet.add("text");
        columnSet.add("link");
        columnSet.add("linktext");

        ITableRow row = createMock(ITableRow.class);
        expect(row.getColumnNames()).andReturn(new HashSet<Object>(columnSet));
        expect(row.get("text")).andReturn(textWithoutTarget);

        replay(row);
        assertTrue("Since linktext is templated, the row is templated", _controller.isTemplated(row));
        verify(row);
    }

    public void testIsTemplated_WithANonStringColumn(){
        Integer nonStringColumn = 3239;
        String textWithoutTarget = "this is text without a target";
        String linkWithoutTarget = "/someURLBase/with/some/more" ;
        String linkTextWithoutTarget = "This is link text with";

        Set<Object> columnSet = new HashSet<Object>();
        columnSet.add("id");
        columnSet.add("text");
        columnSet.add("link");
        columnSet.add("linktext");

        ITableRow row = createMock(ITableRow.class);
        expect(row.getColumnNames()).andReturn(new HashSet<Object>(columnSet));
        expect(row.get("id")).andReturn(nonStringColumn);
        expect(row.get("text")).andReturn(textWithoutTarget);
        expect(row.get("link")).andReturn(linkWithoutTarget);
        expect(row.get("linktext")).andReturn(linkTextWithoutTarget);
        replay(row);
        assertFalse("Since none of the fields are templated, the row is not templated", _controller.isTemplated(row));
        verify(row);
    }

    public void testFillModel(){
        Map<String, Object> model = new HashMap<String, Object>();
        String originalQuestionText = "Some Question with <city> and <state>";
        String originalQuestionLink = "/<state>/<city>/more/and/more-of-the-<city>";
        String originalQuestionLinkText = "learn more about <city> and <state>";

        String expectedQuestionText = "Some Question with Cour d' Alene and Idaho";
        String expectedQuestionLink = "/Idaho/Cour-d%27-Alene/more/and/more-of-the-Cour-d%27-Alene";
        String expectedQuestionLinkText = "learn more about Cour d' Alene and Idaho";

        HashMapTableRow row = new HashMapTableRow();

        row.addCell("text", originalQuestionText);
        row.addCell("link", originalQuestionLink);
        row.addCell("linktext", originalQuestionLinkText);

        expect(_sessionContext.getState()).andReturn(State.ID);
        expect(_sessionContext.getCity()).andReturn(new City("Cour d' Alene", State.ID));
        replay(_sessionContext);

        _request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _controller.fillModel(_request,model, row);

        assertEquals(expectedQuestionText, model.get(MODEL_QUESTION_TEXT));
        assertEquals(expectedQuestionLink, model.get(MODEL_QUESTION_LINK));
        assertEquals(expectedQuestionLinkText, model.get(MODEL_QUESTION_LINK_TEXT));

        verify(_sessionContext);
    }

    public Map<String, String> createTargets(String state, String city){
        Map<String, String> targets = new HashMap<String, String>();
        targets.put("city", city);
        targets.put("state", state);

        return targets;
    }
}
