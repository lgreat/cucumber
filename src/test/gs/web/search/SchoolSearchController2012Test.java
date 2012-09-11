package gs.web.search;

import gs.data.search.GsSolrQuery;
import gs.data.search.GsSolrSearcher;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import gs.data.search.beans.SolrSchoolSearchResult;
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
}
