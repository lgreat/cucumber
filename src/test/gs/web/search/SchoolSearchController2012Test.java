package gs.web.search;

import gs.data.geo.City;
import gs.data.search.GsSolrQuery;
import gs.data.search.GsSolrSearcher;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.path.DirectoryStructureUrlFields;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author aroy@greatschools.org
 */
public class SchoolSearchController2012Test extends BaseControllerTestCase {
    SchoolSearchController2012 _controller;
    SchoolSearchCommandWithFields _commandWithFields;
    SchoolSearchCommand _command;
    DirectoryStructureUrlFields _fields;
    private GsSolrSearcher _gsSolrSearcher;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new SchoolSearchController2012();

        _gsSolrSearcher = createStrictMock(GsSolrSearcher.class);
        _controller.setGsSolrSearcher(_gsSolrSearcher);

        _command = new SchoolSearchCommand2012();
        _request.setRequestURI("abc");
        _fields = new DirectoryStructureUrlFields(_request);

    }

    public void testRegressionIllegalEnumArgument_SchoolSizeAll() throws Exception{
        _command.setSchoolSize("All");
        baseIllegalEnumArgumentTest();
    }

    public void testRegressionIllegalEnumArgument_SchoolSizeBlank() throws Exception{
        _command.setSchoolSize("");
        baseIllegalEnumArgumentTest();
    }

    private void baseIllegalEnumArgumentTest() throws SearchException {
        _commandWithFields = new SchoolSearchCommandWithFields(_command, _fields);

        expect(_gsSolrSearcher.search(isA(GsSolrQuery.class), eq(SolrSchoolSearchResult.class), eq(true)))
                .andReturn(new SearchResultsPage<SolrSchoolSearchResult>(0, null));
        replay(_gsSolrSearcher);
        try {
            _controller.searchForSchools(_commandWithFields);
        } catch (IllegalArgumentException iae) {
            fail("Should not get an IllegalArgumentException for invalid SchoolSize enum: " + iae.getMessage());
        }
        verify(_gsSolrSearcher);
    }

    /**
     * Adding test case for default behaviour to false when no packard Parameters are set -GS-14110 -Shomi Arora 
     */

    public void testPackardFilterDefaultBehaviour() {
       _commandWithFields = new SchoolSearchCommandWithFields(_command, _fields);
       assertEquals(_controller.isPackardFilters(_command,_commandWithFields),false);

    }

    public void testShouldRedirectFromByNameToCityBrowse() {
        // These cases SHOULD redirect
        shouldRedirectFromByNameToCityBrowse("Milwaukee", State.WI);
        shouldRedirectFromByNameToCityBrowse("Indianapolis", State.IN);
        shouldRedirectFromByNameToCityBrowse("Speedway", State.IN);
        shouldRedirectFromByNameToCityBrowse("Beech Grove", State.IN);
        shouldRedirectFromByNameToCityBrowse("Washington", State.DC);
        shouldRedirectFromByNameToCityBrowse("San Francisco", State.CA);
        shouldRedirectFromByNameToCityBrowse("Oakland", State.CA);

        // These cases should NOT redirect
        shouldNotRedirectFromByNameToCityBrowse("Milwaukee", State.IN);
        shouldNotRedirectFromByNameToCityBrowse("Washington", State.IN);
        shouldNotRedirectFromByNameToCityBrowse("San Francisco", State.DC);
        shouldNotRedirectFromByNameToCityBrowse("Oakland", State.WI);
        shouldNotRedirectFromByNameToCityBrowse("Beech Grove", State.WI);
        shouldNotRedirectFromByNameToCityBrowse("Speedway", State.DC);
        shouldNotRedirectFromByNameToCityBrowse("Alameda", State.CA);
        shouldNotRedirectFromByNameToCityBrowse("Los Angeles", State.CA);
        shouldNotRedirectFromByNameToCityBrowse("Houston", State.TX);
        shouldNotRedirectFromByNameToCityBrowse(null, State.TX);
        shouldNotRedirectFromByNameToCityBrowse("Houston", null);
        shouldNotRedirectFromByNameToCityBrowse(null, null);
    }

    private void shouldRedirectFromByNameToCityBrowse(String searchString, State state) {
        _command.setSearchString(searchString);
        _command.setState(state.getAbbreviation());
        _commandWithFields = new SchoolSearchCommandWithFields(_command, _fields);
        assertTrue("Expect \"" + searchString + "\" by name search in " + state + " to be redirected to city browse",
                _controller.shouldRedirectFromByNameToCityBrowse(_command, _commandWithFields));
    }

    private void shouldNotRedirectFromByNameToCityBrowse(String searchString, State state) {
        _command.setSearchString(searchString);
        _command.setState(state == null? null : state.getAbbreviation());
        _commandWithFields = new SchoolSearchCommandWithFields(_command, _fields);
        assertFalse("Expect \"" + searchString + "\" by name search in " + state + " to be left alone",
                _controller.shouldRedirectFromByNameToCityBrowse(_command, _commandWithFields));
    }

}
