package gs.web.geo;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.state.State;
import gs.web.BaseTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class StateSpecificFooterHelperTest extends BaseTestCase {
    private StateSpecificFooterHelper _helper;
    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private Map<String, Object> _model;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _helper = new StateSpecificFooterHelper();

        _geoDao = createStrictMock(IGeoDao.class);
        _schoolDao = createStrictMock(ISchoolDao.class);

        _helper.setGeoDao(_geoDao);
        _helper.setSchoolDao(_schoolDao);

        _model = new HashMap<String, Object>();
    }

    public void testBasics() {
        assertSame(_geoDao, _helper.getGeoDao());
        assertSame(_schoolDao, _helper.getSchoolDao());
    }

    public void testPlacePopularCitiesInModelNull() {
        expect(_geoDao.findTopCitiesByPopulationInState(State.CA, StateSpecificFooterHelper.NUM_CITIES))
                .andReturn(null);
        expect(_schoolDao.getCitySchoolCountForActiveSchools(eq(State.CA))).andReturn(null);

        replayMocks(_geoDao, _schoolDao);
        _helper.placePopularCitiesInModel(State.CA, _model);
        verifyMocks(_geoDao, _schoolDao);
        
        assertNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
    }

    public void testPlacePopularCitiesInModelEmpty() {
        StateSpecificFooterHelper.clearCache();
        expect(_geoDao.findTopCitiesByPopulationInState(State.CA, StateSpecificFooterHelper.NUM_CITIES))
                .andReturn(new ArrayList<City>(0));
        expect(_schoolDao.getCitySchoolCountForActiveSchools(eq(State.CA))).andReturn(null);

        replayMocks(_geoDao, _schoolDao);
        _helper.placePopularCitiesInModel(State.CA, _model);
        verifyMocks(_geoDao, _schoolDao);

        assertNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));

        //same operation again should pull from cache
        resetMocks(_geoDao, _schoolDao);
        replayMocks(_geoDao, _schoolDao);
        _model = new HashMap<String, Object>();
        _helper.placePopularCitiesInModel(State.CA, _model);
        verifyMocks(_geoDao, _schoolDao);

        assertNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
    }

    public void testPlacePopularCitiesInModelTooSmall() {
        StateSpecificFooterHelper.clearCache();
        assertFalse("This test will fail if the expected number of cities is 1", 1 == StateSpecificFooterHelper.NUM_CITIES);
        List<City> cities = new ArrayList<City>(1);
        cities.add(new City());
        expect(_geoDao.findTopCitiesByPopulationInState(State.CA, StateSpecificFooterHelper.NUM_CITIES))
                .andReturn(cities);
        expect(_schoolDao.getCitySchoolCountForActiveSchools(eq(State.CA))).andReturn(null);

        replayMocks(_geoDao, _schoolDao);
        _helper.placePopularCitiesInModel(State.CA, _model);
        verifyMocks(_geoDao, _schoolDao);

        assertNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));

        //same operation again should use cache
        
        resetMocks(_geoDao, _schoolDao);
        replayMocks(_geoDao, _schoolDao);
        _model = new HashMap<String,Object>();
        _helper.placePopularCitiesInModel(State.CA, _model);
        verifyMocks(_geoDao, _schoolDao);

        assertNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
    }

    public void testPlacePopularCitiesInModelException() {
        StateSpecificFooterHelper.clearCache();
        expect(_geoDao.findTopCitiesByPopulationInState(State.CA, StateSpecificFooterHelper.NUM_CITIES))
                .andThrow(new RuntimeException("testPlacePopularCitiesInModelException"));

        replayMocks(_geoDao, _schoolDao);
        _helper.placePopularCitiesInModel(State.CA, _model);
        verifyMocks(_geoDao, _schoolDao);

        assertNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
    }

    public void testPlacePopularCitiesInModelNullState() {
        replayMocks(_geoDao, _schoolDao);
        _helper.placePopularCitiesInModel(null, _model);
        verifyMocks(_geoDao, _schoolDao);

        assertNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
    }

    public void testPlacePopularCitiesInModelNullModel() {
        replayMocks(_geoDao, _schoolDao);
        _helper.placePopularCitiesInModel(State.CA, null);
        verifyMocks(_geoDao, _schoolDao);

        assertNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
    }

    public void testPlacePopularCitiesInModel() {
        List<City> cities = new ArrayList<City>(StateSpecificFooterHelper.NUM_CITIES);
        for (int x=0; x < StateSpecificFooterHelper.NUM_CITIES; x++) {
            City city = new City();
            city.setName("a");
            cities.add(city);
        }
        expect(_geoDao.findTopCitiesByPopulationInState(State.CA, StateSpecificFooterHelper.NUM_CITIES))
                .andReturn(cities);
        expect(_schoolDao.getCitySchoolCountForActiveSchools(eq(State.CA))).andReturn(null);

        replayMocks(_geoDao, _schoolDao);
        _helper.placePopularCitiesInModel(State.CA, _model);
        verifyMocks(_geoDao, _schoolDao);

        assertNotNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
        assertSame(cities, _model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
    }
}
