package gs.web.search;

import gs.data.geo.City;
import gs.web.BaseControllerTestCase;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.web.GsMockHttpServletRequest;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 2/26/13
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class SchoolSearchHelperTest extends BaseControllerTestCase {

    private SchoolSearchHelper _schoolSearchHelper;
    private SearchAdHelper _searchAdHelper;

    private GsMockHttpServletRequest _request;
    private SchoolSearchCommand _command;
    private DirectoryStructureUrlFields _fields;
    private SchoolSearchCommandWithFields _schoolSearchCommandWithFields;
    private PageHelper _pageHelper;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        _schoolSearchHelper = new SchoolSearchHelper();

        _searchAdHelper = createMock(SearchAdHelper.class);
        _schoolSearchHelper.setSearchAdHelper(_searchAdHelper);

        _request = getRequest();
        _command = new SchoolSearchCommand();
        _command.setState("CA");
        _fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, _fields);


        _pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, _pageHelper);
        _schoolSearchCommandWithFields = new SchoolSearchCommandWithFields(_command, _fields);
    }

    public void replayAll() {
        EasyMock.replay(_searchAdHelper);
    }

    public void verifyAll() {
        EasyMock.verify(_searchAdHelper);
    }

    public void resetAll() {
        EasyMock.reset(_searchAdHelper);
    }

    @Test
    public void testAddGamAttributes() throws Exception {
        //test 1
        resetAll();

        _searchAdHelper.addSchoolTypeAdKeywords(_pageHelper, _schoolSearchCommandWithFields.getSchoolTypes());
        expectLastCall();

        _searchAdHelper.addLevelCodeAdKeywords(_pageHelper, _schoolSearchCommandWithFields.getGradeLevels());
        expectLastCall();

        _searchAdHelper.addNearbySearchInfoKeywords(_pageHelper, _request);
        expectLastCall();

        _searchAdHelper.addAdvancedFiltersKeywords(_pageHelper, true);
        expectLastCall();

        _searchAdHelper.addCountyAdKeywords(_pageHelper, null);
        expectLastCall();

        _searchAdHelper.addSearchBrowseAdKeyword(_pageHelper);
        expectLastCall();

        replayAll();

        _schoolSearchHelper.addGamAttributes(_request, _schoolSearchCommandWithFields, null, true);

        verifyAll();


        //test 2
        resetAll();

        List<SolrSchoolSearchResult> schoolResults = new ArrayList<SolrSchoolSearchResult>();
        String searchString = "Alameda, CA";

        //setting command object properties so that returns true for isNearbySearch()
        _command.setSearchString(searchString);
        _command.setCity("Alameda");
        _command.setLat(0.0);
        _command.setLon(0.0);
        _command.setDistance("1.0");
        _schoolSearchCommandWithFields = new SchoolSearchCommandWithFields(_command, _fields);

        _searchAdHelper.addSearchResultsAdKeywords(_pageHelper, schoolResults);
        expectLastCall();

        _searchAdHelper.addSchoolTypeAdKeywords(_pageHelper, _schoolSearchCommandWithFields.getSchoolTypes());
        expectLastCall();

        _searchAdHelper.addLevelCodeAdKeywords(_pageHelper, _schoolSearchCommandWithFields.getGradeLevels());
        expectLastCall();

        _searchAdHelper.addSearchQueryAdKeywords(_pageHelper, searchString);
        expectLastCall();

        _searchAdHelper.addZipCodeAdKeyword(_pageHelper, searchString);
        expectLastCall();

        _searchAdHelper.addCityAdKeyword(eq(_pageHelper), isA(City.class));
        expectLastCall();

        _searchAdHelper.addNearbySearchInfoKeywords(_pageHelper, _request);
        expectLastCall();

        _searchAdHelper.addCountyAdKeywords(_pageHelper, schoolResults);
        expectLastCall();

        _searchAdHelper.addSearchBrowseAdKeyword(_pageHelper);
        expectLastCall();

        replayAll();

        _schoolSearchHelper.addGamAttributes(_request, _schoolSearchCommandWithFields, schoolResults, false);

        verifyAll();
    }
}
