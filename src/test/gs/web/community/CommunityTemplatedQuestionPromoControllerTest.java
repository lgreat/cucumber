package gs.web.community;


import gs.data.util.table.ITableRow;
import gs.data.geo.City;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import static org.easymock.classextension.EasyMock.*;

import java.util.Set;
import java.util.HashSet;

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

        setUpSessionContextWithValidCityAndState();
        
        _controller.getCityAndStateFromSession(_request);
        String noTarget = "This is a string with out a target";
        assertEquals("No target in the string should return the original string",noTarget, _controller.replaceTargets(noTarget, false));

        verify(_sessionContext);
    }


    public void testReplaceTargets_WithValidCityTarget(){

        setUpSessionContextWithValidCityAndState();

        _controller.getCityAndStateFromSession(_request);
        String original = "This is a string with a target: <city>";
        String expected = "This is a string with a target: New York City";
        assertEquals("Target <city> should be replaced with 'New York City'",expected, _controller.replaceTargets(original, false));

        verify(_sessionContext);
    }

    public void testReplaceTargets_WithValidCityTargetInALink(){

        setUpSessionContextWithValidCityAndState();

        _controller.getCityAndStateFromSession(_request);
        String original = "/urlBase/<city>/something/else/";
        String expected = "/urlBase/New-York-City/something/else/";
        assertEquals("Target <city> should be replaced with 'New-York-City'",expected, _controller.replaceTargets(original, true));

        verify(_sessionContext);
    }


    public void testReplaceTargets_WithValidStateTarget(){

        setUpSessionContextWithValidCityAndState();

        _controller.getCityAndStateFromSession(_request);
        String original = "This is a string with a target: <state>";
        String expected = "This is a string with a target: New York";
        assertEquals("Target <state> should be replaced with 'New York'",expected, _controller.replaceTargets(original, false));

        verify(_sessionContext);
    }

    public void testReplaceTargets_WithValidStateTargetInALink(){

        setUpSessionContextWithValidCityAndState();

        _controller.getCityAndStateFromSession(_request);
        String original = "/urlBase/<state>/something/else/";
        String expected = "/urlBase/New-York/something/else/";
        assertEquals("Target <state> should be replaced with 'New-York'",expected, _controller.replaceTargets(original, true));

        verify(_sessionContext);
    }



    public void testReplaceTargets_WithValidCityAndStateTargets(){

        setUpSessionContextWithValidCityAndState();

        _controller.getCityAndStateFromSession(_request);
        String original = "This is a string with a target: <city>, <state>";
        String expected = "This is a string with a target: New York City, New York";
        assertEquals("Target <city> should be replaced with 'New York City' and <state> target replace with 'New York'",expected, _controller.replaceTargets(original, false));

        verify(_sessionContext);
    }

    public void testReplaceTargets_WithValidCityAndStateTargetsInALink(){

        setUpSessionContextWithValidCityAndState();

        _controller.getCityAndStateFromSession(_request);
        String original = "/urlBase/<state>/<city>/something/else/";
        String expected = "/urlBase/New-York/New-York-City/something/else/";
        assertEquals("Target <city> should be replaced with 'New-York-City', and Target <state> should be replaced with 'New-York'",expected, _controller.replaceTargets(original, true));

        verify(_sessionContext);
    }

    public void testReplaceTargets_WithValidCityWithAnUnderscoreTargetInALink(){

        expect(_sessionContext.getState()).andReturn(State.CA);
        expect(_sessionContext.getCity()).andReturn(new City("Cardiff-By-the-Sea", State.CA));
        replay(_sessionContext);

        _request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        _controller.getCityAndStateFromSession(_request);
        String original = "/urlBase/<city>/something/else/";
        String expected = "/urlBase/Cardiff_By_the_Sea/something/else/";
        assertEquals("Target <city> should be replaced with 'Cardiff_By_the_Sea'",expected, _controller.replaceTargets(original, true));

        verify(_sessionContext);
    }


    private void setUpSessionContextWithValidCityAndState() {
        expect(_sessionContext.getState()).andReturn(State.NY);
        expect(_sessionContext.getCity()).andReturn(new City("New York City", State.NY));
        replay(_sessionContext);

        _request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);
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
        expect(row.get("text")).andReturn(textWithoutTarget);
        expect(row.get("link")).andReturn(linkWithoutTarget);
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
}
