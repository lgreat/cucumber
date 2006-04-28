/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: BestPublicSchoolValuesController.java,v 1.13 2006/04/28 06:00:53 apeterson Exp $
 */

package gs.web.school.performance;

import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
import gs.data.geo.LatLon;
import gs.data.state.State;
import gs.data.util.SpringUtil;
import gs.data.content.Article;
import gs.web.ISessionFacade;
import gs.web.SessionFacade;
import gs.web.util.Anchor;
import gs.web.util.ListModel;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

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
public class BestPublicSchoolValuesController extends AbstractController {

    private static final String PARAM_LIMIT = "limit";
    private static final String PARAM_METRO = "metro"; // ignored for now, but passed for forward compatibility
    /**
     * What data to list. Default is our best values.
     */
     static final String PARAM_LIST = "list";
    static final String PARAM_LIST_VALUE_ABOVE_AVG_API = "above"; // ignored; default
    static final String PARAM_LIST_VALUE_BELOW_AVG_API = "below";

    static final String PARAM_MAP = "map"; // set to 1 to show map

    /**
     * List of IBestPublicSchoolValue objects.
     */
    public static final String MODEL_CITY_LIST = "cities";

    public static final String MODEL_PAGE_SUBTITLE = "subtitle";
    public static final String MODEL_SHOW_RANK = "showRank"; // Boolean
    public static final String MODEL_SHOW_MAP = "showMap"; // Boolean
    public static final String MODEL_LINKS = "links"; // ListModel


    final String PATH = "/school/performance/bestValues.page";


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

    static List _citiesOfValue;
    static List _citiesNotMatchingCriteria;

    private static final Log _log = LogFactory.getLog(BestPublicSchoolValuesController.class);

    private static synchronized void initializeCities() {

        /*
        Note: I used these regex to convert from the excel spreadsheet to this form:
        ^([^\t]+)\t([^\t]+)\t([^\t]+)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]+)$

                values.add(new Bpsv("\1","\2",\3,\4,\5,\6,\7,\8,\9,\10,\11,"\12"));

                NOTE: make sure home values don't have commas, and make sure school counts have 0s and not empty cells.
        */
        _citiesOfValue = new ArrayList();
        _citiesOfValue.add(new Bpsv("Albany", "Alameda", 595000, 88, 10.00, 11.3, 6, 3, 1, 2, 671000));
        _citiesOfValue.add(new Bpsv("Walnut Creek", "Contra Costa", 624100, 92, 9.89, 10.7, 15, 9, 2, 4, 625200));
        _citiesOfValue.add(new Bpsv("Benicia", "Solano", 579500, 86, 8.96, 10.4, 8, 5, 1, 2, 632000));
        _citiesOfValue.add(new Bpsv("Martinez", "Contra Costa", 511000, 76, 7.53, 9.9, 10, 6, 3, 4, 515000));
        _citiesOfValue.add(new Bpsv("Pleasant Hill", "Contra Costa", 619000, 92, 8.52, 9.3, 12, 7, 4, 3, 603000));
        _citiesOfValue.add(new Bpsv("Dublin", "Alameda", 640000, 95, 8.68, 9.2, 9, 6, 2, 2, 630000));
        _citiesOfValue.add(new Bpsv("Livermore", "Alameda", 599400, 89, 8.02, 9.0, 20, 12, 7, 5, 610000));
        _citiesOfValue.add(new Bpsv("Fairfax", "Marin", 725000, 107, 9.66, 9.0, 2, 1, 1, 0, 770900));
        _citiesOfValue.add(new Bpsv("Castro Valley", "Alameda", 637400, 94, 8.46, 9.0, 15, 10, 2, 3, 670000));
        _citiesOfValue.add(new Bpsv("San Ramon", "Contra Costa", 750000, 111, 9.91, 8.9, 15, 11, 4, 2, 712000));
        _citiesOfValue.add(new Bpsv("Fremont", "Alameda", 656700, 97, 8.57, 8.8, 41, 29, 8, 8, 618000));
        _citiesOfValue.add(new Bpsv("Clayton", "Contra Costa", 770000, 114, 10.00, 8.8, 2, 1, 1, 0, 828400));
        _citiesOfValue.add(new Bpsv("Palo Alto", "Santa Clara", 773600, 115, 9.94, 8.7, 16, 11, 4, 3, 1073300));
        _citiesOfValue.add(new Bpsv("Pleasanton", "Alameda", 774900, 115, 9.95, 8.7, 15, 9, 3, 3, 820000));
        _citiesOfValue.add(new Bpsv("Milpitas", "Santa Clara", 605000, 90, 7.71, 8.6, 14, 9, 3, 3, 655500));
        _citiesOfValue.add(new Bpsv("Foster City", "San Mateo", 783300, 116, 9.81, 8.5, 4, 3, 1, 0, 840800));
        _citiesOfValue.add(new Bpsv("Petaluma", "Sonoma", 607200, 90, 7.51, 8.3, 33, 24, 6, 6, 634000));
        _citiesOfValue.add(new Bpsv("Piedmont", "Alameda", 814000, 121, 10.00, 8.3, 6, 3, 1, 2, 1471300));
        _citiesOfValue.add(new Bpsv("Moraga", "Contra Costa", 820000, 121, 10.00, 8.2, 5, 3, 1, 1, 821200));
        _citiesOfValue.add(new Bpsv("Novato", "Marin", 712600, 106, 8.54, 8.1, 17, 10, 5, 5, 685000));
        _citiesOfValue.add(new Bpsv("Alameda", "Alameda", 658200, 98, 7.77, 8.0, 21, 12, 5, 6, 675200));
        _citiesOfValue.add(new Bpsv("San Anselmo", "Marin", 875000, 130, 10.00, 7.7, 3, 2, 0, 1, 985900));
        _citiesOfValue.add(new Bpsv("Sebastopol", "Sonoma", 687000, 102, 7.82, 7.7, 14, 9, 7, 3, 817600));
        _citiesOfValue.add(new Bpsv("Kensington", "Alameda", 890450, 132, 10.00, 7.6, 1, 1, 0, 0, 920600));
        _citiesOfValue.add(new Bpsv("Brisbane", "San Mateo", 673500, 100, 7.50, 7.5, 2, 1, 1, 0, 741200));
        _citiesOfValue.add(new Bpsv("Glen Ellen", "Sonoma", 630000, 93, 7.00, 7.5, 1, 1, 0, 0, 893900));
        _citiesOfValue.add(new Bpsv("Pacifica", "San Mateo", 685000, 101, 7.57, 7.5, 9, 6, 5, 2, 671000));
        _citiesOfValue.add(new Bpsv("Mountain View", "Santa Clara", 718000, 106, 7.89, 7.4, 12, 8, 2, 2, 664800));
        _citiesOfValue.add(new Bpsv("Cupertino", "Santa Clara", 901000, 133, 9.88, 7.4, 15, 9, 3, 3, 924000));
        _citiesOfValue.add(new Bpsv("Corte Madera", "Marin", 917300, 136, 10.00, 7.4, 1, 1, 0, 0, 957200));
        _citiesOfValue.add(new Bpsv("San Carlos", "San Mateo", 900000, 133, 9.61, 7.2, 7, 5, 3, 0, 826000));
        _citiesOfValue.add(new Bpsv("Sunnyvale", "Santa Clara", 705300, 104, 7.45, 7.1, 20, 13, 5, 2, 660000));
        _citiesOfValue.add(new Bpsv("Yountville", "Napa", 859000, 127, 9.00, 7.1, 1, 1, 0, 0, 807300));
        _citiesOfValue.add(new Bpsv("Millbrae", "San Mateo", 900000, 133, 9.27, 7.0, 5, 3, 1, 1, 1076300));
        _citiesOfValue.add(new Bpsv("Belmont", "San Mateo", 890000, 132, 8.97, 6.8, 6, 4, 1, 1, 855000));
        _citiesOfValue.add(new Bpsv("San Rafael", "Marin", 770600, 114, 7.25, 6.4, 17, 11, 4, 5, 685800));
        _citiesOfValue.add(new Bpsv("San Mateo", "San Mateo", 778500, 115, 7.06, 6.1, 19, 13, 3, 3, 747500));
        _citiesOfValue.add(new Bpsv("Half Moon Bay", "San Mateo", 806500, 119, 7.00, 5.9, 5, 2, 1, 2, 921500));



        BestPublicSchoolValuesController._citiesNotMatchingCriteria = new ArrayList();
        _citiesNotMatchingCriteria.add(new Bpsv("Alamo", "Contra Costa", 1392500, 206, 10.00, 4.8, 4, 3, 2, 1, 1553500));
        _citiesNotMatchingCriteria.add(new Bpsv("American Canyon", "Solano", 634100, 94, 6.31, 6.7, 5, 3, 2, 0, 556800));
        _citiesNotMatchingCriteria.add(new Bpsv("Angwin", "Napa", 971000, 144, 8.00, 5.6, 1, 1, 1, 0, 689800));
        _citiesNotMatchingCriteria.add(new Bpsv("Antioch", "Contra Costa", 496300, 74, 4.99, 6.8, 24, 16, 8, 5, 520000));
        _citiesNotMatchingCriteria.add(new Bpsv("Atherton", "San Mateo", 2275000, 337, 7.51, 2.2, 5, 4, 1, 1, 1676500));
        _citiesNotMatchingCriteria.add(new Bpsv("Berkeley", "Alameda", 730100, 108, 6.37, 5.9, 16, 11, 3, 2, 730000));
        _citiesNotMatchingCriteria.add(new Bpsv("Bolinas", "Marin", 1063800, 158, 8.00, 5.1, 1, 1, 1, 0, 833800));
        _citiesNotMatchingCriteria.add(new Bpsv("Brentwood", "Contra Costa", 605500, 90, 6.83, 7.6, 14, 8, 3, 5, 685000));
        _citiesNotMatchingCriteria.add(new Bpsv("Burlingame", "San Mateo", 1325000, 196, 8.91, 4.5, 7, 5, 1, 1, 1593400));
        _citiesNotMatchingCriteria.add(new Bpsv("Calistoga", "Napa", 615000, 91, 3.85, 4.2, 3, 1, 1, 2, 724000));
        _citiesNotMatchingCriteria.add(new Bpsv("Campbell", "Santa Clara", 645000, 96, 5.83, 6.1, 6, 4, 1, 1, 695000));
        _citiesNotMatchingCriteria.add(new Bpsv("Cloverdale", "Sonoma", 499000, 74, 5.00, 6.8, 4, 2, 1, 2, 588900));
        _citiesNotMatchingCriteria.add(new Bpsv("Colma", "San Mateo", 717600, 106, 6.00, 5.6, 2, 1, 1, 0, 630400));
        _citiesNotMatchingCriteria.add(new Bpsv("Concord", "Contra Costa", 504000, 75, 4.81, 6.4, 29, 16, 6, 10, 540000));
        _citiesNotMatchingCriteria.add(new Bpsv("Cotati", "Sonoma", 530000, 79, 6.00, 7.6, 1, 1, 0, 0, 640200));
        _citiesNotMatchingCriteria.add(new Bpsv("Crockett", "Contra Costa", 551000, 82, 4.38, 5.4, 3, 1, 2, 2, 553100));
        _citiesNotMatchingCriteria.add(new Bpsv("Daly City", "San Mateo", 675300, 100, 5.72, 5.7, 20, 15, 3, 3, 670000));
        _citiesNotMatchingCriteria.add(new Bpsv("Danville", "Contra Costa", 984700, 146, 10.00, 6.9, 13, 7, 3, 3, 960000));
        _citiesNotMatchingCriteria.add(new Bpsv("Dixon", "Solano", 469000, 69, 4.88, 7.0, 9, 5, 2, 3, 465000));
        _citiesNotMatchingCriteria.add(new Bpsv("East Palo Alto", "San Mateo", 616700, 91, 2.75, 3.0, 8, 7, 6, 0, 616400));
        _citiesNotMatchingCriteria.add(new Bpsv("El Cerrito", "Contra Costa", 616500, 91, 4.52, 5.0, 6, 4, 1, 1, 733000));
        _citiesNotMatchingCriteria.add(new Bpsv("El Sobrante", "Contra Costa", 520000, 77, 4.05, 5.3, 4, 3, 1, 0, 546700));
        _citiesNotMatchingCriteria.add(new Bpsv("Emeryville", "Alameda", 440000, 65, 3.08, 4.7, 2, 1, 1, 1, 460000));
        _citiesNotMatchingCriteria.add(new Bpsv("Fairfield", "Solano", 459400, 68, 5.71, 8.4, 29, 14, 9, 10, 465000));
        _citiesNotMatchingCriteria.add(new Bpsv("Forestville", "Sonoma", 420000, 62, 6.80, 10.9, 4, 1, 1, 3, 655700));
        _citiesNotMatchingCriteria.add(new Bpsv("Gilroy", "Santa Clara", 675000, 100, 4.98, 5.0, 15, 8, 4, 4, 690000));
        _citiesNotMatchingCriteria.add(new Bpsv("Guerneville", "Sonoma", 370000, 55, 7.00, 12.8, 2, 1, 2, 0, 641200));
        _citiesNotMatchingCriteria.add(new Bpsv("Hayward", "Alameda", 535300, 79, 3.61, 4.6, 39, 27, 10, 8, 545000));
        _citiesNotMatchingCriteria.add(new Bpsv("Healdsburg", "Sonoma", 575000, 85, 5.97, 7.0, 7, 4, 1, 2, 652900));
        _citiesNotMatchingCriteria.add(new Bpsv("Hercules", "Contra Costa", 480000, 71, 6.38, 9.0, 5, 3, 1, 1, 525000));
        _citiesNotMatchingCriteria.add(new Bpsv("Hillsborough", "San Mateo", 1471700, 218, 10.00, 4.6, 4, 3, 1, 0, 2846500));
        _citiesNotMatchingCriteria.add(new Bpsv("Kentfield", "Marin", 1777000, 263, 10.00, 3.8, 2, 1, 1, 0, 1506200));
        _citiesNotMatchingCriteria.add(new Bpsv("Lafayette", "Contra Costa", 997000, 148, 10.00, 6.8, 6, 4, 1, 1, 1197800));
        _citiesNotMatchingCriteria.add(new Bpsv("Larkspur", "Marin", 1185000, 176, 9.94, 5.7, 4, 0, 1, 3, 1259800));
        _citiesNotMatchingCriteria.add(new Bpsv("Los Altos", "Santa Clara", 1547200, 229, 9.71, 4.2, 10, 7, 2, 1, 1427500));
        _citiesNotMatchingCriteria.add(new Bpsv("Los Gatos", "Santa Clara", 1065800, 158, 9.72, 6.2, 10, 6, 3, 1, 972500));
        _citiesNotMatchingCriteria.add(new Bpsv("Menlo Park", "San Mateo", 973400, 144, 5.40, 3.7, 8, 7, 7, 1, 1020500));
        _citiesNotMatchingCriteria.add(new Bpsv("Mill Valley", "Marin", 992500, 147, 10.00, 6.8, 7, 5, 1, 1, 918000));
        _citiesNotMatchingCriteria.add(new Bpsv("Montara", "San Mateo", 776500, 115, 6.00, 5.2, 1, 1, 0, 0, 868600));
        _citiesNotMatchingCriteria.add(new Bpsv("Monte Rio", "Sonoma", 512600, 76, 5.00, 6.6, 1, 1, 1, 0, 539600));
        _citiesNotMatchingCriteria.add(new Bpsv("Morgan Hill", "Santa Clara", 725000, 107, 6.92, 6.4, 12, 8, 4, 4, 795000));
        _citiesNotMatchingCriteria.add(new Bpsv("Napa", "Napa", 569800, 84, 5.66, 6.7, 34, 24, 8, 7, 544000));
        _citiesNotMatchingCriteria.add(new Bpsv("Newark", "Alameda", 593000, 88, 5.20, 5.9, 16, 11, 5, 6, 588000));
        _citiesNotMatchingCriteria.add(new Bpsv("Oakland", "Alameda", 481700, 71, 3.19, 4.5, 127, 73, 40, 32, 500000));
        _citiesNotMatchingCriteria.add(new Bpsv("Oakley", "Contra Costa", 465000, 69, 5.89, 8.5, 7, 4, 2, 1, 501000));
        _citiesNotMatchingCriteria.add(new Bpsv("Occidental", "Sonoma", 1027200, 152, 8.07, 5.3, 2, 1, 1, 0, 825700));
        _citiesNotMatchingCriteria.add(new Bpsv("Orinda", "Contra Costa", 1116500, 165, 10.00, 6.0, 6, 4, 1, 1, 1151700));
        _citiesNotMatchingCriteria.add(new Bpsv("Pinole", "Contra Costa", 530000, 79, 4.45, 5.7, 7, 4, 2, 2, 545000));
        _citiesNotMatchingCriteria.add(new Bpsv("Pittsburg", "Contra Costa", 435000, 64, 2.71, 4.2, 17, 12, 4, 4, 452500));
        _citiesNotMatchingCriteria.add(new Bpsv("Portola Valley", "San Mateo", 3857100, 571, 10.00, 1.8, 2, 2, 1, 0, 1684300));
        _citiesNotMatchingCriteria.add(new Bpsv("Redwood City", "San Mateo", 712900, 106, 5.17, 4.9, 20, 15, 8, 5, 710000));
        _citiesNotMatchingCriteria.add(new Bpsv("Richmond", "Contra Costa", 418400, 62, 2.40, 3.9, 29, 18, 3, 8, 439000));
        _citiesNotMatchingCriteria.add(new Bpsv("Rio Vista", "Solano", 399000, 59, 4.56, 7.7, 3, 1, 1, 1, 386000));
        _citiesNotMatchingCriteria.add(new Bpsv("Rodeo", "Contra Costa", 511000, 76, 3.00, 4.0, 1, 1, 0, 0, 540600));
        _citiesNotMatchingCriteria.add(new Bpsv("Rohnert Park", "Sonoma", 490000, 73, 6.38, 8.8, 14, 8, 4, 4, 507000));
        _citiesNotMatchingCriteria.add(new Bpsv("Ross", "Marin", 2585000, 383, 10.00, 2.6, 1, 1, 1, 0, 1620800));
        _citiesNotMatchingCriteria.add(new Bpsv("San Bruno", "San Mateo", 665000, 99, 6.90, 7.0, 13, 9, 1, 3, 680000));
        _citiesNotMatchingCriteria.add(new Bpsv("San Francisco", "San Francisco", 779300, 115, 6.18, 5.4, 125, 78, 29, 30, 750000));
        _citiesNotMatchingCriteria.add(new Bpsv("San Jose", "Santa Clara", 622200, 92, 6.22, 6.8, 220, 134, 47, 51, 645000));
        _citiesNotMatchingCriteria.add(new Bpsv("San Leandro", "Alameda", 526600, 78, 4.66, 6.0, 16, 10, 4, 3, 547000));
        _citiesNotMatchingCriteria.add(new Bpsv("San Lorenzo", "Alameda", 540000, 80, 3.87, 4.8, 10, 6, 3, 2, 555000));
        _citiesNotMatchingCriteria.add(new Bpsv("San Martin", "Santa Clara", 890000, 132, 4.00, 3.0, 1, 1, 0, 0, 955300));
        _citiesNotMatchingCriteria.add(new Bpsv("San Pablo", "Contra Costa", 440000, 65, 1.67, 2.6, 11, 8, 3, 4, 488200));
        _citiesNotMatchingCriteria.add(new Bpsv("Santa Clara", "Santa Clara", 622900, 92, 6.43, 7.0, 20, 15, 3, 4, 675000));
        _citiesNotMatchingCriteria.add(new Bpsv("Santa Rosa", "Sonoma", 533500, 79, 6.44, 8.1, 69, 44, 20, 16, 519000));
        _citiesNotMatchingCriteria.add(new Bpsv("Saratoga", "Santa Clara", 1367000, 203, 9.35, 4.6, 9, 6, 3, 2, 1455000));
        _citiesNotMatchingCriteria.add(new Bpsv("Sausalito", "Marin", 750000, 111, 4.00, 3.6, 2, 2, 1, 0, 818000));
        _citiesNotMatchingCriteria.add(new Bpsv("Sonoma", "Sonoma", 618000, 92, 5.61, 6.1, 12, 7, 5, 4, 699000));
        _citiesNotMatchingCriteria.add(new Bpsv("South San Francisco", "San Mateo", 685000, 101, 6.35, 6.3, 12, 6, 4, 3, 720000));
        _citiesNotMatchingCriteria.add(new Bpsv("St. Helena", "Napa", 844500, 125, 6.73, 5.4, 5, 2, 1, 2, 1047200));
        _citiesNotMatchingCriteria.add(new Bpsv("Stanford", "Santa Clara", 1827350, 271, 10.00, 3.7, 2, 2, 0, 0, 1560100));
        _citiesNotMatchingCriteria.add(new Bpsv("Suisun City", "Solano", 425000, 63, 5.34, 8.5, 3, 2, 1, 0, 439500));
        _citiesNotMatchingCriteria.add(new Bpsv("Tiburon", "Marin", 1755000, 260, 10.00, 3.8, 3, 2, 1, 0, 1616100));
        _citiesNotMatchingCriteria.add(new Bpsv("Union City", "Alameda", 617500, 91, 6.38, 7.0, 11, 7, 3, 1, 663000));
        _citiesNotMatchingCriteria.add(new Bpsv("Vacaville", "Solano", 438100, 65, 5.93, 9.1, 21, 13, 5, 7, 440000));
        _citiesNotMatchingCriteria.add(new Bpsv("Vallejo", "Solano", 423200, 63, 3.23, 5.1, 28, 17, 8, 7, 420000));
        _citiesNotMatchingCriteria.add(new Bpsv("Windsor", "Sonoma", 589500, 87, 6.13, 7.0, 6, 3, 2, 2, 602000));
        _citiesNotMatchingCriteria.add(new Bpsv("Woodside", "San Mateo", 1040000, 154, 7.56, 4.9, 2, 1, 1, 1, 1146900));
    }


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (_citiesOfValue == null) {
                initializeCities();
        }

        ISessionFacade sc = SessionFacade.getInstance(request);

        final boolean showMap = StringUtils.equals(request.getParameter(PARAM_MAP), "1");
        boolean listBelowAverage = StringUtils.equals(request.getParameter(PARAM_LIST), PARAM_LIST_VALUE_BELOW_AVG_API);

        int limit = Integer.MAX_VALUE;
        if (StringUtils.isNumeric(request.getParameter(PARAM_LIMIT))) {
            limit = Integer.valueOf(request.getParameter(PARAM_LIMIT)).intValue();
        }

        ModelAndView modelAndView = new ModelAndView("/school/performance/schoolValues");

        List values;
        String subtitle;
        ListModel links = new ListModel();
        String top20Class = null;
        String top20MapClass = null;
        String allClass = null;
        String belowClass = null;
        if (listBelowAverage) {
            subtitle = "Cities not meeting the API or median home price criteria";
            values = _citiesNotMatchingCriteria;
            modelAndView.addObject(MODEL_SHOW_RANK, Boolean.FALSE);
            belowClass = "selected";
        } else {
            values = _citiesOfValue;
            if (limit == Integer.MAX_VALUE) {
                subtitle = "All Bay Area Cities";
                allClass = "selected";
            } else {
                subtitle = "Top " + limit + " Bay Area Cities";
                values = values.subList(0, limit);
                if (showMap) {
                    top20MapClass = "selected";
                } else {
                    top20Class = "selected";
                }
            }
            modelAndView.addObject(MODEL_SHOW_RANK, Boolean.TRUE);
        }
        Article article = new Article();
        article.setId(new Integer(594));
        UrlBuilder builder = new UrlBuilder(article, State.CA, false);
        links.addResult(builder.asAnchor(request, "Back to Article"));
        links.addResult(new Anchor(PATH + "?metro=SFBay&limit=20", "Top 20 Bay Area Cities", top20Class));
        if (!sc.isYahooCobrand()) {
            links.addResult(new Anchor(PATH + "?metro=SFBay&limit=20&map=1", "Map of Top 20 Bay Area Cities", top20MapClass));
        }
        links.addResult(new Anchor(PATH + "?metro=SFBay", "All Bay Area Best Public School Values", allClass));
        links.addResult(new Anchor(PATH + "?metro=SFBay&list=below", "Cities not meeting the API or median home price criteria", belowClass));
        modelAndView.addObject(MODEL_LINKS, links);


        modelAndView.addObject(MODEL_CITY_LIST, values);
        modelAndView.addObject(MODEL_PAGE_SUBTITLE, subtitle);
        modelAndView.addObject(MODEL_SHOW_MAP, Boolean.valueOf(showMap));

        return modelAndView;
    }


    private static class Bpsv
            implements BestPublicSchoolValuesController.IBestPublicSchoolValue {

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


        public Bpsv(String cityName,
                    String countyName,
                    int medianHomePrice,
                    double mhpRanking,
                    double averageApiRank,
                    double bpsv,
                    int schoolsCount,
                    int elementarySchoolsCount,
                    int middleSchoolsCount,
                    int highSchoolsCount,
                    int population) {
            _rank = _citiesOfValue.size() + 1;
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
                _latLon = new LatLon(c.getLat(), c.getLon());
                _population = new Long(population);
            } else {
                _log.error("Cannot find city information for " + cityName);
                _population = new Long(0);
                _latLon = null;
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

        public float getLat() {
            return _latLon.getLat();
        }

        public float getLon() {
            return _latLon.getLon();
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

}
