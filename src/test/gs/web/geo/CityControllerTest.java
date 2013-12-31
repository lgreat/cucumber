/*
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: CityControllerTest.java,v 1.34 2012/09/04 20:16:55 npatury Exp $
 */

package gs.web.geo;

import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.test.rating.ICityRatingDao;
import gs.data.community.local.ILocalBoardDao;
import gs.data.zillow.ZillowRegionDao;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.Anchor;
import gs.web.util.list.AnchorListModel;
import gs.web.util.list.AnchorListModelFactory;
import gs.web.util.RedirectView301;
import org.easymock.classextension.EasyMock;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;

/**
 * Tests CityController.
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 */
public class CityControllerTest extends BaseControllerTestCase {

    private CityController _controller;
    private SessionContextUtil _sessionContextUtil;
    private CityHubHelper _cityHubHelper;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new CityController();

        _controller.setApplicationContext(getApplicationContext());
        _controller.setZillowDao((ZillowRegionDao) getApplicationContext().getBean("zillowDao"));
        _controller.setSchoolDao((ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID));
        _controller.setDistrictDao((IDistrictDao) getApplicationContext().getBean(IDistrictDao.BEAN_ID));
        _controller.setGeoDao((IGeoDao) getApplicationContext().getBean(IGeoDao.BEAN_ID));
        _controller.setCityRatingDao((ICityRatingDao) getApplicationContext().getBean(ICityRatingDao.BEAN_ID));
        _controller.setStateManager((StateManager) getApplicationContext().getBean(StateManager.BEAN_ID));
        _controller.setAnchorListModelFactory( (AnchorListModelFactory) getApplicationContext().getBean(AnchorListModelFactory.BEAN_ID));
        _controller.setLocalBoardDao( (ILocalBoardDao) getApplicationContext().getBean(ILocalBoardDao.BEAN_ID));
        _controller.setStateSpecificFooterHelper(org.easymock.classextension.EasyMock.createMock(
                StateSpecificFooterHelper.class));
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().
                getBean(SessionContextUtil.BEAN_ID);

        _cityHubHelper = EasyMock.createStrictMock(CityHubHelper.class);
        _controller.setCityHubHelper(_cityHubHelper);
    }



    public void testFindDistricts() throws Exception {
        EasyMock.reset(_cityHubHelper);
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "CA");
        request.setParameter("city", "alameda");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        expect(_cityHubHelper.getCollectionBrowseLinks((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (String) anyObject(), (State) anyObject())).andReturn(new AnchorListModel());

        EasyMock.replay(_cityHubHelper);
        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());
        EasyMock.verify(_cityHubHelper);

        Map model = mav.getModel();

        AnchorListModel anchorListModel = (AnchorListModel) model.get(CityController.MODEL_DISTRICTS);

        List list = anchorListModel.getResults();
        assertTrue(list.size() > 0);
        assertTrue(list.size() <= 2);
        assertEquals("/california/alameda/Alameda-City-Unified/", ((Anchor) list.get(0)).getHref());
    }

    public void testBadCity() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "WY");
        request.setParameter("city", "XXXWorland");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        View view = mav.getView();
        assertTrue(view instanceof RedirectView);
    }

    public void testCapitalization() throws Exception {
        // Alison complained that "city=oakland" fails to capitalize as "Oakland".
        EasyMock.reset(_cityHubHelper);
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "CA");
        request.setParameter("city", "alameda");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        expect(_cityHubHelper.getCollectionBrowseLinks((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (String) anyObject(), (State) anyObject())).andReturn(new AnchorListModel());

        EasyMock.replay(_cityHubHelper);
        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());
        EasyMock.verify(_cityHubHelper);

        Map model = mav.getModel();

        assertEquals("Alameda", model.get(CityController.MODEL_CITY_NAME));
    }


    public void testOakland() throws Exception {
        EasyMock.reset(_cityHubHelper);
        GsMockHttpServletRequest request = getRequest();
        request.addParameter("city", "oakland");
        request.addParameter("state", "CA");
        expect(_cityHubHelper.getCollectionBrowseLinks((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (String) anyObject(), (State) anyObject())).andReturn(new AnchorListModel());

        EasyMock.replay(_cityHubHelper);
        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());
        EasyMock.verify(_cityHubHelper);

        Object city = modelAndView.getModel().get(CityController.MODEL_CITY_NAME);
        assertNotNull(city);

        Object schools = modelAndView.getModel().get("schools");
        assertNull("Expected no schools in Oakland", schools);
    }

    public void testAlamedaHasSchools() throws Exception {
        EasyMock.reset(_cityHubHelper);
        GsMockHttpServletRequest request = getRequest();
        request.addParameter("city", "alameda");
        request.addParameter("state", "CA");
        expect(_cityHubHelper.getCollectionBrowseLinks((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (String) anyObject(), (State) anyObject())).andReturn(new AnchorListModel());

        EasyMock.replay(_cityHubHelper);
        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());
        EasyMock.verify(_cityHubHelper);

        Object city = modelAndView.getModel().get(CityController.MODEL_CITY_NAME);
        assertNotNull(city);

        Object schools = modelAndView.getModel().get("schools");
        assertNotNull(schools);
        assertTrue(schools instanceof Collection);
        assertTrue(((Collection) schools).size() >= 3);
        assertTrue(((Collection) schools).size() <= 10);

    }


    public void testSFIsAvalidCity() throws Exception {
        EasyMock.reset(_cityHubHelper);
        GsMockHttpServletRequest request = getRequest();
        request.addParameter("city", "san francisco");
        request.addParameter("state", "CA");
        expect(_cityHubHelper.getCollectionBrowseLinks((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (String) anyObject(), (State) anyObject())).andReturn(new AnchorListModel());

        EasyMock.replay(_cityHubHelper);
        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());
        EasyMock.verify(_cityHubHelper);

        Object city = modelAndView.getModel().get(CityController.MODEL_CITY_NAME);
        assertNotNull(city);
    }


    public void testRedirects() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/city/Anchorage/AK");
        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());
        assertTrue(modelAndView.getView() instanceof RedirectView301);
    }

    public void testSearchEngineFriendlyUrlsWork() throws Exception {
        EasyMock.reset(_cityHubHelper);
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/alaska/anchorage");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);
        expect(_cityHubHelper.getCollectionBrowseLinks((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (String) anyObject(), (State) anyObject())).andReturn(new AnchorListModel());

        EasyMock.replay(_cityHubHelper);
        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());
        EasyMock.verify(_cityHubHelper);
        ICity city = (ICity) modelAndView.getModel().get(CityController.MODEL_CITY);
        assertNotNull(city);
        assertEquals("Anchorage", city.getName());
        assertEquals(State.AK, city.getState());

        EasyMock.reset(_cityHubHelper);
        request.setRequestURI("/alaska/big-lake");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);
        expect(_cityHubHelper.getCollectionBrowseLinks((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (String) anyObject(), (State) anyObject())).andReturn(new AnchorListModel());

        EasyMock.replay(_cityHubHelper);
        modelAndView = _controller.handleRequestInternal(request, getResponse());
        EasyMock.verify(_cityHubHelper);
        city = (ICity) modelAndView.getModel().get(CityController.MODEL_CITY);
        assertNotNull(city);
        assertEquals("Big Lake", city.getName());
        assertEquals(State.AK, city.getState());

        EasyMock.reset(_cityHubHelper);
        request.setRequestURI("/alaska/st.-marys");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);
        expect(_cityHubHelper.getCollectionBrowseLinks((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (String) anyObject(), (State) anyObject())).andReturn(new AnchorListModel());

        EasyMock.replay(_cityHubHelper);
        modelAndView = _controller.handleRequestInternal(request, getResponse());
        EasyMock.verify(_cityHubHelper);
         city = (ICity) modelAndView.getModel().get(CityController.MODEL_CITY);
        assertNotNull(city);
        assertEquals("St. Marys", city.getName());
        assertEquals(State.AK, city.getState());

        // Make sure old one works
        EasyMock.reset(_cityHubHelper);
        request.addParameter("city", "st. marys");
        request.addParameter("state", "AK");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        expect(_cityHubHelper.getCollectionBrowseLinks((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (String) anyObject(), (State) anyObject())).andReturn(new AnchorListModel());

        EasyMock.replay(_cityHubHelper);
        modelAndView = _controller.handleRequestInternal(request, getResponse());
        EasyMock.verify(_cityHubHelper);
         city = (ICity) modelAndView.getModel().get(CityController.MODEL_CITY);
        assertNotNull(city);
        assertEquals("St. Marys", city.getName());
        assertEquals(State.AK, city.getState());
        request.setParameter("city", (String)null);
        request.setParameter("state", (String)null);
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        // Make sure gs-web gets stripped off
        EasyMock.reset(_cityHubHelper);
        request.setRequestURI("/gs-web/alaska/anchorage");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);
        expect(_cityHubHelper.getCollectionBrowseLinks((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (String) anyObject(), (State) anyObject())).andReturn(new AnchorListModel());

        EasyMock.replay(_cityHubHelper);
         modelAndView = _controller.handleRequestInternal(request, getResponse());
        EasyMock.verify(_cityHubHelper);
         city = (ICity) modelAndView.getModel().get(CityController.MODEL_CITY);
        assertNotNull(city);
        assertEquals("Anchorage", city.getName());
        assertEquals(State.AK, city.getState());

        EasyMock.reset(_cityHubHelper);
        request.setRequestURI("/gs-web/alaska/big-lake");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);
        expect(_cityHubHelper.getCollectionBrowseLinks((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (String) anyObject(), (State) anyObject())).andReturn(new AnchorListModel());

        EasyMock.replay(_cityHubHelper);
        modelAndView = _controller.handleRequestInternal(request, getResponse());
        EasyMock.verify(_cityHubHelper);
        city = (ICity) modelAndView.getModel().get(CityController.MODEL_CITY);
        assertNotNull(city);
        assertEquals("Big Lake", city.getName());
        assertEquals(State.AK, city.getState());

        EasyMock.reset(_cityHubHelper);
        request.setRequestURI("/gs-web/alaska/st.-marys");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);
        expect(_cityHubHelper.getCollectionBrowseLinks((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (String) anyObject(), (State) anyObject())).andReturn(new AnchorListModel());

        EasyMock.replay(_cityHubHelper);
        modelAndView = _controller.handleRequestInternal(request, getResponse());
        EasyMock.verify(_cityHubHelper);
         city = (ICity) modelAndView.getModel().get(CityController.MODEL_CITY);
        assertNotNull(city);
        assertEquals("St. Marys", city.getName());
        assertEquals(State.AK, city.getState());

        // Make sure old one works
        /*request.setRequestURI("/gs-web/city.page?city=St.+Marys&state=AK");
        request.addParameter("city", "St. Marys");
        request.addParameter("state", "AK");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        modelAndView = _controller.handleRequestInternal(request, getResponse());
         city = (ICity) modelAndView.getModel().get(CityController.MODEL_CITY);
        assertNotNull(city);
        assertEquals("St. Marys", city.getName());
        assertEquals(State.AK, city.getState());
        */

    }

    public void xtestBuffaloNY() throws Exception {
        EasyMock.reset(_cityHubHelper);
        GsMockHttpServletRequest request = getRequest();
        request.addParameter("city", "Buffalo");
        request.addParameter("state", "NY");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        expect(_cityHubHelper.getCollectionBrowseLinks((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (String) anyObject(), (State) anyObject())).andReturn(new AnchorListModel());

        EasyMock.replay(_cityHubHelper);
        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());
        EasyMock.verify(_cityHubHelper);

        Object city = modelAndView.getModel().get(CityController.MODEL_CITY_NAME);
        assertNull(city);

        Object schools = modelAndView.getModel().get(CityController.MODEL_SCHOOLS);
        assertNotNull(schools);
        assertTrue(schools instanceof Collection);
        assertTrue(((Collection) schools).size() >= 3);

    }


    public void testModelContainsProperCityCanonicalPath() throws Exception {
        EasyMock.reset(_cityHubHelper);
        GsMockHttpServletRequest request = getRequest();
        request.addParameter(CityController.PARAM_CITY, "san francisco");
        request.addParameter("state", "CA");

        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        expect(_cityHubHelper.getCollectionBrowseLinks((GsMockHttpServletRequest) anyObject(), (Integer) anyObject(),
                (String) anyObject(), (State) anyObject())).andReturn(new AnchorListModel());

        EasyMock.replay(_cityHubHelper);
        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());
        EasyMock.verify(_cityHubHelper);

        String path = (String) modelAndView.getModel().get(CityController.PARAM_CITY_CANONICAL_PATH);

        assertEquals("http://" + request.getServerName() + ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "") + "/california/san-francisco/", path);
    }

    public void testModelDoesntContainCityCanonicalPathWhenCityIncorrect() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.addParameter(CityController.PARAM_CITY, "nonexistent city");
        request.addParameter("state", "CA");

        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());

        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());

        String path = (String) modelAndView.getModel().get(CityController.PARAM_CITY_CANONICAL_PATH);

        assertNull("path shouldnt exist in model if city doesnt exist", path);
    }

    public void testBuildTopRatedSchoolCompareString() {
        List<ISchoolDao.ITopRatedSchool> topRatedSchools = new ArrayList<ISchoolDao.ITopRatedSchool>();

        assertNull(_controller.buildTopRatedSchoolCompareString(null, null));
        assertNull(_controller.buildTopRatedSchoolCompareString(State.CA, null));
        assertNull(_controller.buildTopRatedSchoolCompareString(State.CA, topRatedSchools));

        topRatedSchools.add(createTopRatedSchool(2));
        topRatedSchools.add(createTopRatedSchool(1));
        topRatedSchools.add(createTopRatedSchool(3));

        String rval = _controller.buildTopRatedSchoolCompareString(State.CA, topRatedSchools);
        assertNotNull(rval);
        assertEquals("CA2,CA1,CA3", rval);
    }
    
    public void testFindTopRatedSchoolsForCompare() {
        Map<String, Object> model = new HashMap<String, Object>();

        _controller.findTopRatedSchoolsForCompare(_controller.getGeoDao().findCity(State.CA, "Alameda"), model);

        assertNotNull(model.get(CityController.MODEL_TOP_RATED_E_SCHOOLS));
        assertEquals("CA2,CA4,CA5,CA7,CA8", model.get(CityController.MODEL_TOP_RATED_E_SCHOOLS));
        assertNotNull(model.get(CityController.MODEL_TOP_RATED_M_SCHOOLS));
        assertEquals("CA10,CA1,CA3", model.get(CityController.MODEL_TOP_RATED_M_SCHOOLS));
        assertNotNull(model.get(CityController.MODEL_TOP_RATED_H_SCHOOLS));
        assertEquals("CA1,CA6,CA9", model.get(CityController.MODEL_TOP_RATED_H_SCHOOLS));
    }

    private ISchoolDao.ITopRatedSchool createTopRatedSchool(final int id) {
        return new ISchoolDao.ITopRatedSchool() {
            public String getName() {
                return "Top rated school";
            }

            public int getId() {
                return id;
            }

            public int getRating() {
                return 0;
            }

            public School getSchool() {
                return new School();
            }
        };
    }
}
