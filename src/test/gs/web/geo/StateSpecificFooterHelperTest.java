package gs.web.geo;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.search.Searcher;
import gs.data.state.State;
import gs.web.BaseTestCase;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;

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
    private Searcher _searcher;
    private Map<String, Object> _model;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _helper = new StateSpecificFooterHelper();

        _geoDao = createStrictMock(IGeoDao.class);
        _searcher = createStrictMock(Searcher.class);

        _helper.setGeoDao(_geoDao);
        _helper.setSearcher(_searcher);

        _model = new HashMap<String, Object>();
    }

    public void testBasics() {
        assertSame(_geoDao, _helper.getGeoDao());
        assertSame(_searcher, _helper.getSearcher());
    }

    public void testPlacePopularCitiesInModelNull() {
        expect(_geoDao.findTopCitiesByPopulationInState(State.CA, StateSpecificFooterHelper.NUM_CITIES))
                .andReturn(null);
        expect(_searcher.search(isA(Query.class), isA(Sort.class), (HitCollector) isNull(), (Filter) isNull())).andReturn(null);

        replayMocks(_geoDao, _searcher);
        _helper.placePopularCitiesInModel(State.CA, _model);
        verifyMocks(_geoDao, _searcher);
        
        assertNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
    }

    public void testPlacePopularCitiesInModelEmpty() {
        expect(_geoDao.findTopCitiesByPopulationInState(State.CA, StateSpecificFooterHelper.NUM_CITIES))
                .andReturn(new ArrayList<City>(0));
        expect(_searcher.search(isA(Query.class), isA(Sort.class), (HitCollector) isNull(), (Filter) isNull())).andReturn(null);

        replayMocks(_geoDao, _searcher);
        _helper.placePopularCitiesInModel(State.CA, _model);
        verifyMocks(_geoDao, _searcher);

        assertNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
    }

    public void testPlacePopularCitiesInModelTooSmall() {
        assertFalse("This test will fail if the expected number of cities is 1",
                     1 == StateSpecificFooterHelper.NUM_CITIES);
        List<City> cities = new ArrayList<City>(1);
        cities.add(new City());
        expect(_geoDao.findTopCitiesByPopulationInState(State.CA, StateSpecificFooterHelper.NUM_CITIES))
                .andReturn(cities);
        expect(_searcher.search(isA(Query.class), isA(Sort.class), (HitCollector) isNull(), (Filter) isNull())).andReturn(null);

        replayMocks(_geoDao, _searcher);
        _helper.placePopularCitiesInModel(State.CA, _model);
        verifyMocks(_geoDao, _searcher);

        assertNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
    }

    public void testPlacePopularCitiesInModelException() {
        expect(_geoDao.findTopCitiesByPopulationInState(State.CA, StateSpecificFooterHelper.NUM_CITIES))
                .andThrow(new RuntimeException("testPlacePopularCitiesInModelException"));

        replayMocks(_geoDao, _searcher);
        _helper.placePopularCitiesInModel(State.CA, _model);
        verifyMocks(_geoDao, _searcher);

        assertNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
    }

    public void testPlacePopularCitiesInModelNullState() {
        replayMocks(_geoDao, _searcher);
        _helper.placePopularCitiesInModel(null, _model);
        verifyMocks(_geoDao, _searcher);

        assertNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
    }

    public void testPlacePopularCitiesInModelNullModel() {
        replayMocks(_geoDao, _searcher);
        _helper.placePopularCitiesInModel(State.CA, null);
        verifyMocks(_geoDao, _searcher);

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
        expect(_searcher.search(isA(Query.class), isA(Sort.class), (HitCollector) isNull(), (Filter) isNull())).andReturn(null);

        replayMocks(_geoDao, _searcher);
        _helper.placePopularCitiesInModel(State.CA, _model);
        verifyMocks(_geoDao, _searcher);

        assertNotNull(_model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
        assertSame(cities, _model.get(StateSpecificFooterHelper.MODEL_TOP_CITIES));
    }
}
