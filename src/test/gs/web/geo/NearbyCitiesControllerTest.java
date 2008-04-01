/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: NearbyCitiesControllerTest.java,v 1.11 2008/04/01 17:25:22 aroy Exp $
 */

package gs.web.geo;

import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.geo.bestplaces.BpZip;
import gs.data.geo.bestplaces.BpCity;
import gs.data.test.rating.ICityRatingDao;
import gs.data.test.rating.CityRating;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.AnchorListModelFactory;
import gs.web.util.list.AnchorListModel;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import static org.easymock.classextension.EasyMock.*;

/**
 * Provides tests for NearbyCitiesController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class NearbyCitiesControllerTest extends BaseControllerTestCase {

    private NearbyCitiesController _controller;
    private IGeoDao _geoDao;
    private ICityRatingDao _cityRatingDao;
    private AnchorListModelFactory _anchorListModelFactory;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new NearbyCitiesController();
        _geoDao = createStrictMock(IGeoDao.class);
        _cityRatingDao = createStrictMock(ICityRatingDao.class);
        _anchorListModelFactory = createStrictMock(AnchorListModelFactory.class);
        _controller.setGeoDao(_geoDao);
        _controller.setCityRatingDao(_cityRatingDao);
        _controller.setAnchorListModelFactory(_anchorListModelFactory);

        getRequest().setParameter(NearbyCitiesController.PARAM_CITY, "Alameda");
        SessionContextUtil.getSessionContext(getRequest()).setState(State.CA);
    }

    public void testBasics() {
        assertSame(_geoDao, _controller.getGeoDao());
        assertSame(_cityRatingDao, _controller.getCityRatingDao());
        assertSame(_anchorListModelFactory, _controller.getAnchorListModelFactory());

        _controller.setViewName("/view");
        assertEquals("/view", _controller.getViewName());

        NearbyCitiesController.CityAndRating car = new NearbyCitiesController.CityAndRating();
        City city = new City();
        CityRating rating = new CityRating();
        car.setCity(city);
        car.setRating(rating);
        car.setFromZip(true);
        car.setCenter(true);
        assertSame(city, car.getCity());
        assertSame(rating, car.getRating());
        assertTrue(car.isFromZip());
        assertTrue(car.isCenter());
    }

    public void testNoCity() throws Exception {
        getRequest().setParameter(NearbyCitiesController.PARAM_CITY, (String)null);
        replay(_geoDao);
        replay(_cityRatingDao);
        replay(_anchorListModelFactory);
        _controller.handleRequestInternal(getRequest(), getResponse());
        // nothing happens
        verify(_geoDao);
        verify(_cityRatingDao);
        verify(_anchorListModelFactory);
    }

    public void testCantFindCity() throws Exception {
        expect(_geoDao.findCitiesByState(State.CA)).andReturn(new ArrayList<City>());
        expect(_geoDao.findCity(State.CA, "Alameda")).andReturn(null);
        replay(_geoDao);
        replay(_cityRatingDao);
        replay(_anchorListModelFactory);
        _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_geoDao);
        verify(_cityRatingDao);
        verify(_anchorListModelFactory);
    }

    public void testLoadCityList() {
        Map<String, Object> model = new HashMap<String, Object>();
        List<City> cities = new ArrayList<City>();
        expect(_geoDao.findCitiesByState(State.CA)).andReturn(cities);
        replay(_geoDao);
        replay(_cityRatingDao);
        replay(_anchorListModelFactory);
        _controller.loadCityList(State.CA, model);
        verify(_geoDao);
        verify(_cityRatingDao);
        verify(_anchorListModelFactory);

        assertNotNull(model.get(NearbyCitiesController.MODEL_COMPARE_CITIES));
        assertSame(cities, model.get(NearbyCitiesController.MODEL_COMPARE_CITIES));
        assertEquals("Expect default option to have been added",
                1, ((List)model.get(NearbyCitiesController.MODEL_COMPARE_CITIES)).size());
    }

    public void testGetBpZipNoZip() {
        City city = new City();
        city.setName("Alameda");
        city.setState(State.CA);

        expect(_geoDao.findZip(State.CA, "Alameda")).andReturn(null);
        replay(_geoDao);
        replay(_cityRatingDao);
        replay(_anchorListModelFactory);

        ICity icity = _controller.getBpZip(city);
        verify(_geoDao);
        verify(_cityRatingDao);
        verify(_anchorListModelFactory);

        assertNull(icity);
    }

    public void testGetBpZip() {
        City city = new City();
        city.setName("Oakland");
        city.setState(State.CA);
        city.setLat(39.43f);
        city.setLon(-121.35f);

        BpZip zip = new BpZip();
        zip.setHouseMedianValue(150000f);
        zip.setZip("94612");
        expect(_geoDao.findZip(State.CA, "Oakland")).andReturn(zip);
        replay(_geoDao);
        replay(_cityRatingDao);
        replay(_anchorListModelFactory);

        ICity icity = _controller.getBpZip(city);
        verify(_geoDao);
        verify(_cityRatingDao);
        verify(_anchorListModelFactory);
        assertNotNull(icity);
        assertEquals(BpCity.class, icity.getClass());

        assertEquals(150000f, ((BpCity)icity).getHouseMedianValue());
        assertEquals("94612", ((BpCity)icity).getZip());
    }

    public void testGetBpCity() {
        City city = new City();
        city.setName("San Diego");
        city.setState(State.CA);

        expect(_geoDao.findBpCity(State.CA, "San Diego")).andReturn(null);
        replay(_geoDao);
        replay(_cityRatingDao);
        replay(_anchorListModelFactory);

        ICity icity = _controller.getBpCity(city);
        verify(_geoDao);
        verify(_cityRatingDao);
        verify(_anchorListModelFactory);

        assertNull(icity);

        reset(_geoDao);
        reset(_cityRatingDao);
        reset(_anchorListModelFactory);

        BpCity bpcity = new BpCity();
        expect(_geoDao.findBpCity(State.CA, "San Diego")).andReturn(bpcity);
        replay(_geoDao);
        replay(_cityRatingDao);
        replay(_anchorListModelFactory);

        icity = _controller.getBpCity(city);
        verify(_geoDao);
        verify(_cityRatingDao);
        verify(_anchorListModelFactory);
        assertNotNull(icity);
        assertSame(bpcity, icity);
    }

    public void testAttachCityRatings() {
        List<ICity> cities = new ArrayList<ICity>();
        City city1 = new City();
        city1.setName("San Diego");
        city1.setState(State.CA);
        City city2 = new City();
        city2.setName("Oakland");
        city2.setState(State.CA);

        cities.add(city1);
        cities.add(city2);

        CityRating rating1 = new CityRating();
        rating1.setRating(10);

        expect(_cityRatingDao.getCityRatingByCity(city1.getState(), city1.getName())).andReturn(rating1);
        expect(_geoDao.findBpCity(city1.getState(), city1.getName())).andReturn(null);
        expect(_geoDao.findZip(city1.getState(), city1.getName())).andReturn(null);
        expect(_cityRatingDao.getCityRatingByCity(city2.getState(), city2.getName())).andReturn(null);
        expect(_geoDao.findBpCity(city2.getState(), city2.getName())).andReturn(null);
        expect(_geoDao.findZip(city2.getState(), city2.getName())).andReturn(null);

        replay(_geoDao);
        replay(_cityRatingDao);
        replay(_anchorListModelFactory);

        List<NearbyCitiesController.CityAndRating> cars = _controller.attachCityRatings(cities);
        verify(_geoDao);
        verify(_cityRatingDao);
        verify(_anchorListModelFactory);

        assertNotNull(cars);
        assertEquals(2, cars.size());

        NearbyCitiesController.CityAndRating car1 = cars.get(0);
        assertSame(city1, car1.getCity());
        assertSame(rating1, car1.getRating());
        assertFalse(car1.isFromZip());

        NearbyCitiesController.CityAndRating car2 = cars.get(1);
        assertSame(city2, car2.getCity());
        assertNull(car2.getRating());
        assertFalse(car2.isFromZip());
    }

    public void testUseBpCity() {
        List<ICity> cities = new ArrayList<ICity>();
        City city1 = new City();
        city1.setName("San Diego");
        city1.setState(State.CA);

        cities.add(city1);

        BpCity bpcity = new BpCity();
        expect(_cityRatingDao.getCityRatingByCity(city1.getState(), city1.getName())).andReturn(null);
        expect(_geoDao.findBpCity(city1.getState(), city1.getName())).andReturn(bpcity);

        replay(_geoDao);
        replay(_cityRatingDao);
        replay(_anchorListModelFactory);

        List<NearbyCitiesController.CityAndRating> cars = _controller.attachCityRatings(cities);
        verify(_geoDao);
        verify(_cityRatingDao);
        verify(_anchorListModelFactory);

        assertNotNull(cars);
        assertEquals(1, cars.size());

        NearbyCitiesController.CityAndRating car1 = cars.get(0);
        assertSame(bpcity, car1.getCity());
        assertNull(car1.getRating());
        assertFalse(car1.isFromZip());
    }

    public void testUseBpZip() {
        List<ICity> cities = new ArrayList<ICity>();
        City city1 = new City();
        city1.setName("San Diego");
        city1.setState(State.CA);
        city1.setLat(39.43f);
        city1.setLon(-121.35f);

        cities.add(city1);

        BpZip bpzip = new BpZip();
        bpzip.setHouseMedianValue(150000f);
        bpzip.setZip("92122");

        expect(_cityRatingDao.getCityRatingByCity(city1.getState(), city1.getName())).andReturn(null);
        expect(_geoDao.findBpCity(city1.getState(), city1.getName())).andReturn(null);
        expect(_geoDao.findZip(city1.getState(), city1.getName())).andReturn(bpzip);

        replay(_geoDao);
        replay(_cityRatingDao);
        replay(_anchorListModelFactory);

        List<NearbyCitiesController.CityAndRating> cars = _controller.attachCityRatings(cities);
        verify(_geoDao);
        verify(_cityRatingDao);
        verify(_anchorListModelFactory);

        assertNotNull(cars);
        assertEquals(1, cars.size());

        NearbyCitiesController.CityAndRating car1 = cars.get(0);
        assertNotNull(car1.getCity());
        assertEquals(BpCity.class, car1.getCity().getClass());
        BpCity bpcity = (BpCity) car1.getCity();
        assertEquals(150000f, bpcity.getHouseMedianValue());
        assertEquals("92122", bpcity.getZip());
        assertNull(car1.getRating());
        assertTrue(car1.isFromZip());
    }

    public void testIsModule() throws Exception {
        getRequest().setParameter(NearbyCitiesController.PARAM_MODULE, "true");

        City city = new City();
        city.setName("Alameda");
        city.setState(State.CA);

        List<ICity> cities = new ArrayList<ICity>();
        City nearbyCity = new City();
        cities.add(nearbyCity);

        expect(_geoDao.findCity(State.CA, "Alameda")).andReturn(city);
        expect(_geoDao.findNearbyCities(city, NearbyCitiesController.DEFAULT_MAX_CITIES)).andReturn(cities);
        AnchorListModel alm = new AnchorListModel();
        expect(_anchorListModelFactory.createNearbyCitiesAnchorListModel(
                            "Cities Near Alameda", city,
                            cities,
                            NearbyCitiesController.DEFAULT_MAX_CITIES,
                            false,
                            false,
                            false,
                            getRequest())).andReturn(alm);
        replay(_geoDao);
        replay(_cityRatingDao);
        replay(_anchorListModelFactory);
        _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_geoDao);
        verify(_cityRatingDao);
        verify(_anchorListModelFactory);
    }
}
