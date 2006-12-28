/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: BestPublicSchoolValuesController.java,v 1.28 2006/12/28 20:35:29 thuss Exp $
 */

package gs.web.school.performance;

import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
import gs.data.geo.LatLon;
import gs.data.state.State;
import gs.data.util.SpringUtil;
import gs.web.util.list.AnchorListModel;
import gs.web.util.UrlBuilder;
import gs.web.util.list.Anchor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Generates a model to show recent parent reviews in a geographical region.
 * Inputs:
 * <li>state - required
 * <li>city - optional
 * <li>max - optional limit on the number of reviews to show. Default is 3.
 * Output model:
 * <li>reviews - a List of IParentReviewModel objects
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class BestPublicSchoolValuesController extends ParameterizableViewController   {

    private static final String PARAM_LIMIT = "limit"; // override of property

    /**
     * List of IBestPublicSchoolValue objects.
     */
    public static final String MODEL_CITY_LIST = "cities";

    public static final String MODEL_PAGE_TITLE = "title";
    public static final String MODEL_SHOW_RANK = "showRank"; // Boolean

    public static final String MODEL_CITY_NAME_LIST = "cityNames";

    /**
     * A list of these is sent to the view.
     */
    public interface IBestPublicSchoolValue extends ICity {
        int getRank();

        String getCityName();

        String getCountyName();

        int getMedianHomePrice();

        double getAverageApiRank();

        int getSchoolsCount();

        int getElementarySchoolsCount();

        int getMiddleSchoolsCount();

        int getHighSchoolsCount();

        String getCityPageHref();

        String getSchoolsPageHref();
    }

    private  List _citiesOfValue;
    private  List _allCities;

    private boolean _showingAll;
    private boolean _showingRank;
    private String _title;
    private Integer _limit; // optional max number of cities to show

    private static final Log _log = LogFactory.getLog(BestPublicSchoolValuesController.class);


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (_citiesOfValue == null) {
            initializeCities();
        }

        // Limit can be configured with spring and then overriden by the URL.
        int limit = Integer.MAX_VALUE;
        if (_limit != null) {
            limit = _limit.intValue();
        }
        if (StringUtils.isNumeric(request.getParameter(PARAM_LIMIT))) {
            limit = Integer.valueOf(request.getParameter(PARAM_LIMIT)).intValue();
        }

        ModelAndView modelAndView = new ModelAndView(getViewName());

        modelAndView.addObject(MODEL_SHOW_RANK, Boolean.valueOf(_showingRank));

        // Figure out what cities we're talking about...
        List values;
        if (_showingAll) {
            values = _allCities;
        } else {
            values = _citiesOfValue;
            if (limit != Integer.MAX_VALUE) {
                values = values.subList(0, limit);
            }
        }

        // Build a separate list of city names
        AnchorListModel links = new AnchorListModel();
        for (Iterator iter= values.iterator(); iter.hasNext();  ){
            IBestPublicSchoolValue bpsv = (IBestPublicSchoolValue) iter.next();
            UrlBuilder builder = new UrlBuilder(bpsv, UrlBuilder.CITY_PAGE);
            String label = bpsv.getCityName();// + "<span>"+bpsv.getAverageApiRank()+"</span>";
            Anchor anchor = builder.asAnchor(request, label);
            anchor.setBefore("" + bpsv.getRank() + ". ");
            links.add(anchor);
        }
        modelAndView.addObject(MODEL_CITY_NAME_LIST, links);

        modelAndView.addObject(MODEL_CITY_LIST, values);
        modelAndView.addObject(MODEL_PAGE_TITLE, _title);

        return modelAndView;
    }

    private static class Bpsv implements BestPublicSchoolValuesController.IBestPublicSchoolValue {

        private final int _rank;
        private final String _cityName;
        private final String _countyName;
        private final int _medianHomePrice;
        private final double _averageApiRank;
        private final int _schoolsCount;
        private final int _elementarySchoolsCount;
        private final int _middleSchoolsCount;
        private final int _highSchoolsCount;
        private final String _cityPageHref;
        private final String _schoolsPageUrl;
        private final Long _population;
        private final LatLon _latLon;


        public Bpsv(int gsRank, String cityName,
                    String countyName,
                    int medianHomePrice,
                    double averageApiRank,
                    int schoolsCount,
                    int elementarySchoolsCount,
                    int middleSchoolsCount,
                    int highSchoolsCount,
                    int population) {
            _rank = gsRank;
            _cityName = cityName;
            _countyName = countyName;
            _medianHomePrice = medianHomePrice;
            _averageApiRank = averageApiRank;
            _schoolsCount = schoolsCount;
            _elementarySchoolsCount = elementarySchoolsCount;
            _middleSchoolsCount = middleSchoolsCount;
            _highSchoolsCount = highSchoolsCount;
            UrlBuilder builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.CA, _cityName);
            _cityPageHref = builder.asSiteRelativeXml(null);
            builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.CA, _cityName);
            _schoolsPageUrl = builder.asSiteRelativeXml(null);

            // set _city
            ApplicationContext context = SpringUtil.getApplicationContext();
            IGeoDao geoDao = (IGeoDao) context.getBean(IGeoDao.BEAN_ID);
            ICity c = geoDao.findCity(State.CA, cityName);
            if (c != null) {
                _latLon = new LatLon(c.getLatLon().getLat(), c.getLatLon().getLon());
                _population = new Long(population);
            } else {
                _log.error("Cannot find city information for " + cityName);
                _population = new Long(0);
                _latLon = new LatLon(37.7599f, -122.437f);
            }
        }

        public int getRank() {
            return _rank;
        }

        public String getCityName() {
            return _cityName;
        }

        public String getCountyName() {
            return _countyName;
        }

        public int getMedianHomePrice() {
            return _medianHomePrice;
        }

        public double getAverageApiRank() {
            return _averageApiRank;
        }

        public int getSchoolsCount() {
            return _schoolsCount;
        }

        public int getElementarySchoolsCount() {
            return _elementarySchoolsCount;
        }

        public int getMiddleSchoolsCount() {
            return _middleSchoolsCount;
        }

        public int getHighSchoolsCount() {
            return _highSchoolsCount;
        }

        public String getCityPageHref() {
            return _cityPageHref;
        }

        public String getSchoolsPageHref() {
            return _schoolsPageUrl;
        }

        public String getName() {
            return _cityName;
        }

        public State getState() {
            return State.CA;
        }

        public String getCountyFips() {
            throw new UnsupportedOperationException();
        }

        public Long getPopulation() {
            return _population;
        }

        public LatLon getLatLon() {
            return _latLon;
        }
    }


    private  synchronized void initializeCities() {

        /*
        Note: I used these regex to convert from the excel spreadsheet to this form:
        ^([^\t]+)\t([^\t]+)\t([^\t]+)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]+)$

                values.add(new Bpsv("\1","\2",\3,\4,\5,\6,\7,\8,\9,\10,\11,"\12"));

                NOTE: make sure home values don't have commas, and make sure school counts have 0s and not empty cells.
        */
        int rank = 1;
        _citiesOfValue = new ArrayList();
        _citiesOfValue.add(new Bpsv(rank++, "Albany", "Alameda", 590000, 10.0, 6, 3, 1, 2, 16216));
        _citiesOfValue.add(new Bpsv(rank++, "Walnut Creek", "Contra Costa", 644500, 9.9, 15, 9, 2, 4, 64822));
        _citiesOfValue.add(new Bpsv(rank++, "Benicia", "Solano", 600000, 9.0, 8, 5, 1, 2, 26828));
        _citiesOfValue.add(new Bpsv(rank++, "Martinez", "Contra Costa", 515500, 7.5, 10, 6, 3, 4, 36305));
        _citiesOfValue.add(new Bpsv(rank++, "Pleasant Hill", "Contra Costa", 616500, 8.5, 12, 7, 4, 3, 33529));
        _citiesOfValue.add(new Bpsv(rank++, "Dublin", "Alameda", 640000, 8.7, 9, 6, 2, 2, 36995));
        _citiesOfValue.add(new Bpsv(rank++, "Fremont", "Alameda", 635000, 8.6, 41, 29, 8, 8, 202373));
        _citiesOfValue.add(new Bpsv(rank++, "Livermore", "Alameda", 600000, 8.0, 20, 12, 7, 5, 77983));
        _citiesOfValue.add(new Bpsv(rank++, "Fairfax", "Marin", 723000, 9.7, 2, 1, 1, 0, 7159));
        _citiesOfValue.add(new Bpsv(rank++, "Castro Valley", "Alameda", 635000, 8.5, 15, 10, 2, 3, 58327));
        _citiesOfValue.add(new Bpsv(rank++, "Pleasanton", "Alameda", 758000, 10.0, 15, 9, 3, 3, 65951));
        _citiesOfValue.add(new Bpsv(rank++, "Clayton", "Contra Costa", 775000, 10.0, 2, 1, 1, 0, 11126));
        _citiesOfValue.add(new Bpsv(rank++, "Milpitas", "Santa Clara", 610000, 7.7, 14, 9, 3, 3, 62698));
        _citiesOfValue.add(new Bpsv(rank++, "Foster City", "San Mateo", 780000, 9.8, 4, 3, 1, 0, 28847));
        _citiesOfValue.add(new Bpsv(rank++, "Petaluma", "Sonoma", 605000, 7.5, 33, 24, 6, 6, 55359));
        _citiesOfValue.add(new Bpsv(rank++, "Kensington", "Alameda", 812500, 10.0, 1, 1, 0, 0, 5206));
        _citiesOfValue.add(new Bpsv(rank++, "Novato", "Marin", 701000, 8.5, 17, 10, 5, 5, 49238));
        _citiesOfValue.add(new Bpsv(rank++, "Moraga", "Contra Costa", 829000, 10.0, 5, 3, 1, 1, 16797));
        _citiesOfValue.add(new Bpsv(rank++, "Mountain View", "Santa Clara", 670000, 7.9, 12, 8, 2, 2, 69011));
        _citiesOfValue.add(new Bpsv(rank++, "Alameda", "Alameda", 669900, 7.8, 21, 12, 5, 6, 71136));
        _citiesOfValue.add(new Bpsv(rank++, "San Ramon", "Contra Costa", 855000, 9.9, 15, 11, 4, 2, 45616));
        _citiesOfValue.add(new Bpsv(rank++, "San Anselmo", "Marin", 876250, 10.0, 3, 2, 0, 1, 12110));
        _citiesOfValue.add(new Bpsv(rank++, "Sebastopol", "Sonoma", 690000, 7.8, 14, 9, 7, 3, 7685));
        _citiesOfValue.add(new Bpsv(rank++, "Pacifica", "San Mateo", 685000, 7.6, 9, 6, 5, 2, 37182));
        _citiesOfValue.add(new Bpsv(rank++, "Cupertino", "Santa Clara", 900000, 9.9, 15, 9, 3, 3, 51405));
        _citiesOfValue.add(new Bpsv(rank++, "Corte Madera", "Marin", 918500, 10.0, 1, 1, 0, 0, 9177));
        _citiesOfValue.add(new Bpsv(rank++, "Sunnyvale", "Santa Clara", 687500, 7.5, 20, 13, 5, 2, 128012));
        _citiesOfValue.add(new Bpsv(rank++, "San Carlos", "San Mateo", 900000, 9.6, 7, 5, 3, 0, 26915));
        _citiesOfValue.add(new Bpsv(rank++, "Los Gatos", "Santa Clara", 915000, 9.7, 10, 6, 3, 1, 27930));
        _citiesOfValue.add(new Bpsv(rank++, "Millbrae", "San Mateo", 894250, 9.3, 5, 3, 1, 1, 20419));
        _citiesOfValue.add(new Bpsv(rank++, "Palo Alto", "Santa Clara", 965000, 9.9, 16, 11, 4, 3, 56862));
        _citiesOfValue.add(new Bpsv(rank++, "Danville", "Contra Costa", 982000, 10.0, 13, 7, 3, 3, 42199));
        _citiesOfValue.add(new Bpsv(rank++, "Mill Valley", "Marin", 995000, 10.0, 7, 5, 1, 1, 13359));
        _citiesOfValue.add(new Bpsv(rank++, "Belmont", "San Mateo", 894000, 9.0, 6, 4, 1, 1, 24449));
        _citiesOfValue.add(new Bpsv(rank++, "Lafayette", "Contra Costa", 997000, 10.0, 6, 4, 1, 1, 24665));
        _citiesOfValue.add(new Bpsv(rank++, "San Rafael", "Marin", 753000, 7.3, 17, 11, 4, 5, 55560));
        _citiesOfValue.add(new Bpsv(rank++, "San Mateo", "San Mateo", 740000, 7.1, 19, 13, 3, 3, 91275));
        _citiesOfValue.add(new Bpsv(rank++, "Orinda", "Contra Costa", 1110000, 10.0, 6, 4, 1, 1, 18176));
        _citiesOfValue.add(new Bpsv(rank++, "Half Moon Bay", "San Mateo", 820000, 7.0, 5, 2, 1, 2, 12208));
        _citiesOfValue.add(new Bpsv(rank++, "Larkspur", "Marin", 1190000, 9.9, 4, 0, 1, 3, 11797));
        _citiesOfValue.add(new Bpsv(rank++, "Burlingame", "San Mateo", 1110000, 8.9, 7, 5, 1, 1, 27420));
        _citiesOfValue.add(new Bpsv(rank++, "Alamo", "Contra Costa", 1397000, 10.0, 4, 3, 2, 1, 16255));
        _citiesOfValue.add(new Bpsv(rank++, "Piedmont", "Alameda", 1400000, 10.0, 6, 3, 1, 2, 10713));
        _citiesOfValue.add(new Bpsv(rank++, "Saratoga", "Santa Clara", 1370000, 9.3, 9, 6, 3, 2, 29633));
        _citiesOfValue.add(new Bpsv(rank++, "Los Altos", "Santa Clara", 1535000, 9.7, 10, 7, 2, 1, 26992));
        _citiesOfValue.add(new Bpsv(rank++, "Kentfield", "Marin", 1600000, 10.0, 2, 1, 1, 0, 6335));
        _citiesOfValue.add(new Bpsv(rank++, "Tiburon", "Marin", 1717000, 10.0, 3, 2, 1, 0, 8691));
        _citiesOfValue.add(new Bpsv(rank++, "Woodside", "San Mateo", 1482500, 7.6, 2, 1, 1, 1, 5284));
        _citiesOfValue.add(new Bpsv(rank++, "Hillsborough", "San Mateo", 2450000, 10.0, 4, 3, 1, 0, 10600));
        _citiesOfValue.add(new Bpsv(rank++, "Atherton", "San Mateo", 2275000, 7.5, 5, 4, 1, 1, 7127));


        List citiesNotMatchingApiCriteria = new ArrayList();
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "American Canyon", "Solano", 637705, 6.3, 5, 3, 2, 0, 13887));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Antioch", "Contra Costa", 490000, 5.0, 24, 16, 8, 5, 100923));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Berkeley", "Alameda", 690000, 6.4, 16, 11, 3, 2, 101517));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Brentwood", "Contra Costa", 621250, 6.8, 14, 8, 3, 5, 39827));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Byron", "Contra Costa", 610000, 6.2, 5, 3, 3, 1, 10711));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Calistoga", "Napa", 625000, 3.9, 3, 1, 1, 2, 5207));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Campbell", "Santa Clara", 646250, 5.8, 6, 4, 1, 1, 37013));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Cloverdale", "Sonoma", 516750, 5.0, 4, 2, 1, 2, 7844));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Concord", "Contra Costa", 505000, 4.8, 29, 16, 6, 10, 124328));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Cotati", "Sonoma", 530000, 6.0, 1, 1, 0, 0, 7089));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Daly City", "San Mateo", 670000, 5.7, 20, 15, 3, 3, 100620));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Dixon", "Solano", 475000, 4.9, 9, 5, 2, 3, 16710));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "East Palo Alto", "San Mateo", 585000, 2.8, 8, 7, 6, 0, 32042));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "El Cerrito", "Contra Costa", 620000, 4.5, 6, 4, 1, 1, 23138));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "El Granada", "San Mateo", 775000, 6.0, 1, 1, 0, 0, 5714));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "El Sobrante", "Contra Costa", 491000, 4.1, 4, 3, 1, 0, 12458));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Emeryville", "Alameda", 408500, 3.1, 2, 1, 1, 1, 8023));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Fairfield", "Solano", 470000, 5.7, 29, 14, 9, 10, 103949));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Gilroy", "Santa Clara", 675000, 5.0, 15, 8, 4, 4, 44356));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Hayward", "Alameda", 535000, 3.6, 39, 27, 10, 8, 140795));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Healdsburg", "Sonoma", 583409, 6.0, 7, 4, 1, 2, 11130));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Hercules", "Contra Costa", 530000, 6.4, 5, 3, 1, 1, 23425));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Menlo Park", "San Mateo", 880000, 5.4, 8, 7, 7, 1, 29759));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Morgan Hill", "Santa Clara", 730500, 6.9, 12, 8, 4, 4, 34885));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Napa", "Napa", 565000, 5.7, 34, 24, 8, 7, 75465));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Newark", "Alameda", 595000, 5.2, 16, 11, 5, 6, 42511));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Oakland", "Alameda", 460000, 3.2, 127, 73, 40, 32, 397976));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Oakley", "Contra Costa", 465500, 5.9, 7, 4, 2, 1, 26818));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Pinole", "Contra Costa", 528000, 4.4, 7, 4, 2, 2, 19272));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Pittsburg", "Contra Costa", 441000, 2.7, 17, 12, 4, 4, 62600));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Redwood City", "San Mateo", 775000, 5.2, 20, 15, 8, 5, 73346));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Richmond", "Contra Costa", 435000, 2.4, 29, 18, 3, 8, 102318));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Rio Vista", "Solano", 392000, 4.6, 3, 1, 1, 1, 6556));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Rodeo", "Contra Costa", 507500, 3.0, 1, 1, 0, 0, 9310));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Rohnert Park", "Sonoma", 492500, 6.4, 14, 8, 4, 4, 41966));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "San Bruno", "San Mateo", 665000, 6.9, 13, 9, 1, 3, 39661));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "San Francisco", "San Francisco", 740000, 6.2, 125, 78, 29, 30, 744230));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "San Jose", "Santa Clara", 620000, 6.2, 220, 134, 47, 51, 904522));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "San Leandro", "Alameda", 530000, 4.7, 16, 10, 4, 3, 79183));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "San Lorenzo", "Alameda", 540000, 3.9, 10, 6, 3, 2, 22297));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "San Pablo", "Contra Costa", 430000, 1.7, 11, 8, 3, 4, 31041));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Santa Clara", "Santa Clara", 630250, 6.4, 20, 15, 3, 4, 104001));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Santa Rosa", "Sonoma", 525000, 6.4, 69, 44, 20, 16, 153636));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Sausalito", "Marin", 730000, 4.0, 2, 2, 1, 0, 7228));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Sonoma", "Sonoma", 625000, 5.6, 12, 7, 5, 4, 9680));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "South San Francisco", "San Mateo", 689000, 6.4, 12, 6, 4, 3, 59897));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "St. Helena", "Napa", 860000, 6.7, 5, 2, 1, 2, 6026));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Suisun City", "Solano", 425000, 5.3, 3, 2, 1, 0, 26945));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Union City", "Alameda", 595000, 6.4, 11, 7, 3, 1, 68938));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Vacaville", "Solano", 437000, 5.9, 21, 13, 5, 7, 94303));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Vallejo", "Solano", 415000, 3.2, 28, 17, 8, 7, 118349));
        citiesNotMatchingApiCriteria.add(new Bpsv(rank++, "Windsor", "Sonoma", 581000, 6.1, 6, 3, 2, 2, 24751));

        List citiesTooSmall = new ArrayList();
        citiesTooSmall.add(new Bpsv(rank++, "Alviso", "Santa Clara", 454000, 6.0, 1, 1, 0, 0, -1));
        citiesTooSmall.add(new Bpsv(rank++, "Angwin", "Napa", 225000, 8.0, 1, 1, 1, 0, 3340));
        citiesTooSmall.add(new Bpsv(rank++, "Annapolis", "Sonoma", 335000, 3.0, 1, 1, 1, 0, -1));
        citiesTooSmall.add(new Bpsv(rank++, "Bay Point", "Contra Costa", 405000, 1.0, 1, 0, 1, 0, -1));
        citiesTooSmall.add(new Bpsv(rank++, "Bolinas", "Marin", 856000, 8.0, 1, 1, 1, 0, 1240));
        citiesTooSmall.add(new Bpsv(rank++, "Brisbane", "San Mateo", 670000, 7.5, 2, 1, 1, 0, 3559));
        citiesTooSmall.add(new Bpsv(rank++, "Cazadero", "Sonoma", 387500, 6.6, 2, 2, 2, 0, -1));
        citiesTooSmall.add(new Bpsv(rank++, "Colma", "San Mateo", 800000, 6.0, 2, 1, 1, 0, 1410));
        citiesTooSmall.add(new Bpsv(rank++, "Crockett", "Contra Costa", 469000, 4.4, 3, 1, 2, 2, 3538));
        citiesTooSmall.add(new Bpsv(rank++, "Forestville", "Sonoma", 420000, 6.8, 4, 1, 1, 3, 2566));
        citiesTooSmall.add(new Bpsv(rank++, "Geyserville", "Sonoma", 625000, 2.6, 5, 1, 2, 2, -1));
        citiesTooSmall.add(new Bpsv(rank++, "Glen Ellen", "Sonoma", 632500, 7.0, 1, 1, 0, 0, 956));
        citiesTooSmall.add(new Bpsv(rank++, "Guerneville", "Sonoma", 361250, 7.0, 2, 1, 2, 0, 2525));
        citiesTooSmall.add(new Bpsv(rank++, "Kenwood", "Sonoma", 812000, 9.0, 1, 1, 0, 0, -1));
        citiesTooSmall.add(new Bpsv(rank++, "Knightsen", "Contra Costa", 285000, 6.0, 1, 1, 1, 0, -1));
        citiesTooSmall.add(new Bpsv(rank++, "La Honda", "San Mateo", 570000, 10.0, 1, 1, 0, 0, -1));
        citiesTooSmall.add(new Bpsv(rank++, "Montara", "San Mateo", 800000, 6.0, 1, 1, 0, 0, 2935));
        citiesTooSmall.add(new Bpsv(rank++, "Monte Rio", "Sonoma", 402000, 5.0, 1, 1, 1, 0, 1109));
        citiesTooSmall.add(new Bpsv(rank++, "Nicasio", "Marin", 1450000, 10.0, 1, 1, 1, 0, -1));
        citiesTooSmall.add(new Bpsv(rank++, "Occidental", "Sonoma", 735000, 8.1, 2, 1, 1, 0, 1529));
        citiesTooSmall.add(new Bpsv(rank++, "Pescadero", "San Mateo", 765000, 4.0, 3, 1, 1, 2, -1));
        citiesTooSmall.add(new Bpsv(rank++, "Pope Valley", "Napa", 895000, 8.0, 1, 1, 1, 0, -1));
        citiesTooSmall.add(new Bpsv(rank++, "Portola Valley", "San Mateo", 1650000, 10.0, 2, 2, 1, 0, 4418));
        citiesTooSmall.add(new Bpsv(rank++, "Ross", "Marin", 2585000, 10.0, 1, 1, 1, 0, 2294));
        citiesTooSmall.add(new Bpsv(rank++, "San Martin", "Santa Clara", 894500, 4.0, 1, 1, 0, 0, 3959));
        citiesTooSmall.add(new Bpsv(rank++, "Yountville", "Napa", 859000, 9.0, 1, 1, 0, 0, 3328));

        List citiesMissingData = new ArrayList();
        citiesMissingData.add(new Bpsv(rank++, "Penngrove", "Sonoma", 850000, -1, 1, 1, 0, 0, -1));
        citiesMissingData.add(new Bpsv(rank++, "Los Altos Hills", "Santa Clara", 2150000, -1, 1, 1, 0, 0, 8122));
        citiesMissingData.add(new Bpsv(rank++, "Sunol", "Alameda", -1, 9.0, 1, 1, 1, 0, -1));
        citiesMissingData.add(new Bpsv(rank++, "Canyon", "Contra Costa", -1, 9.0, 1, 1, 1, 0, -1));
        citiesMissingData.add(new Bpsv(rank++, "Travis AFB", "Solano", -1, 8.7, 3, 3, 0, 0, -1));
        citiesMissingData.add(new Bpsv(rank++, "Point Reyes Station", "Marin", -1, 7.0, 2, 2, 1, 0, -1));
        citiesMissingData.add(new Bpsv(rank++, "Tomales", "Marin", -1, 6.5, 5, 2, 2, 3, -1));
        citiesMissingData.add(new Bpsv(rank++, "San Geronimo", "Marin", -1, 6.0, 2, 2, 1, 0, -1));
        citiesMissingData.add(new Bpsv(rank++, "Suisun", "Solano", -1, 5.0, 1, 1, 0, 0, -1));
        citiesMissingData.add(new Bpsv(rank++, "Treasure Island", "San Francisco", -1, 2.5, 2, 2, 1, 0, -1));
        citiesMissingData.add(new Bpsv(rank++, "Marin City", "Marin", -1, 2.0, 1, 0, 1, 0, -1));
        citiesMissingData.add(new Bpsv(rank++, "Mount Hamilton", "Santa Clara", -1, -1, 1, 1, 0, 0, -1));
        citiesMissingData.add(new Bpsv(rank++, "Stewarts Point", "Sonoma", -1, -1, 1, 1, 1, 0, -1));

        _allCities = new ArrayList(_citiesOfValue);
        _allCities.addAll(citiesNotMatchingApiCriteria);
        _allCities.addAll(citiesTooSmall);
        //_allCities.addAll(_citiesMissingData);
        Collections.sort(_allCities, new Comparator() {
            public int compare(Object o, Object o1) {
                return ((Bpsv) o).getName().compareTo(((Bpsv) o1).getName());
            }
        });
    }


    public boolean isShowingAll() {
        return _showingAll;
    }

    public void setShowingAll(boolean showingAll) {
        _showingAll = showingAll;
    }

    public boolean isShowingRank() {
        return _showingRank;
    }

    public void setShowingRank(boolean showingRank) {
        _showingRank = showingRank;
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
    }

    public Integer getLimit() {
        return _limit;
    }

    public void setLimit(Integer limit) {
        _limit = limit;
    }
}
