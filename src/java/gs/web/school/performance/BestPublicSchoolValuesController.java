/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: BestPublicSchoolValuesController.java,v 1.2 2006/04/14 20:55:22 apeterson Exp $
 */

package gs.web.school.performance;

import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionFacade;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
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
    private static final String PARAM_METRO = "metro";
    /**
     * What data to list. Default is "
     */
    private static final String PARAM_LIST = "list";
    private static final String PARAM_LIST_VALUE_ABOVE_AVG_API = "above";
    private static final String PARAM_LIST_VALUE_BELOW_AVG_API = "below";

    /**
     * List of IBestPublicSchoolValue objects.
     */
    public static final String MODEL_CITY_LIST = "cities";

    public static final String MODEL_PAGE_SUBTITLE = "subtitle";
    public static final String MODEL_SHOW_RANK = "showRank"; // Boolean

    public interface IBestPublicSchoolValue {
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

    static List _citiesAboveApiCutoff;
    static List _citiesBelowApiCutoff;


    static {

        /*
        Note: I used these regex to convert from the excel spreadsheet to this form:
        ^([^\t]+)\t([^\t]+)\t([^\t]+)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]*)\t([^\t]+)$

                values.add(new Bpsv("\1","\2",\3,\4,\5,\6,\7,\8,\9,\10,\11,"\12"));
        */
        _citiesAboveApiCutoff = new ArrayList();
        _citiesAboveApiCutoff.add(new Bpsv("Guerneville", "Sonoma", 370000, 0.5, 7.00, 12.8, 2, 1, 2, 0, 641200, "Urban Fringe of Mid-size City "));
        _citiesAboveApiCutoff.add(new Bpsv("Albany", "Alameda", 595000, 0.9, 10.00, 11.3, 6, 3, 1, 2, 671000, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Walnut Creek", "Contra Costa", 624100, 0.9, 9.89, 10.7, 15, 9, 2, 4, 625200, "Mid-size City, Urban Fringe of Large City"));
        _citiesAboveApiCutoff.add(new Bpsv("Benicia", "Solano", 579500, 0.9, 8.96, 10.4, 8, 5, 1, 2, 632000, "Urban Fringe of Mid-size City "));
        _citiesAboveApiCutoff.add(new Bpsv("Martinez", "Contra Costa", 511000, 0.8, 7.53, 9.9, 10, 6, 3, 4, 515000, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Pleasant Hill", "Contra Costa", 619000, 0.9, 8.52, 9.3, 12, 7, 4, 3, 603000, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Dublin", "Alameda", 640000, 0.9, 8.68, 9.2, 9, 6, 2, 2, 630000, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Livermore", "Alameda", 599400, 0.9, 8.02, 9.0, 20, 12, 7, 5, 610000, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Fairfax", "Marin", 725000, 1.1, 9.66, 9.0, 2, 1, 1, 0, 770900, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Castro Valley", "Alameda", 637400, 0.9, 8.46, 9.0, 15, 10, 2, 3, 670000, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("San Ramon", "Contra Costa", 750000, 1.1, 9.91, 8.9, 15, 11, 4, 2, 712000, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Fremont", "Alameda", 656700, 1.0, 8.57, 8.8, 41, 29, 8, 8, 618000, "Mid-size City "));
        _citiesAboveApiCutoff.add(new Bpsv("Clayton", "Contra Costa", 770000, 1.1, 10.00, 8.8, 2, 1, 1, 0, 828400, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Palo Alto", "Santa Clara", 773600, 1.1, 9.94, 8.7, 16, 11, 4, 3, 1073300, "Mid-size City "));
        _citiesAboveApiCutoff.add(new Bpsv("Pleasanton", "Alameda", 774900, 1.1, 9.95, 8.7, 15, 9, 3, 3, 820000, "Mid-size City "));
        _citiesAboveApiCutoff.add(new Bpsv("Milpitas", "Santa Clara", 605000, 0.9, 7.71, 8.6, 14, 9, 3, 3, 655500, "Mid-size City "));
        _citiesAboveApiCutoff.add(new Bpsv("Foster City", "San Mateo", 783300, 1.2, 9.81, 8.5, 4, 3, 1, 0, 840800, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Petaluma", "Sonoma", 607200, 0.9, 7.51, 8.3, 33, 24, 6, 6, 634000, "Mid-size City"));
        _citiesAboveApiCutoff.add(new Bpsv("Piedmont", "Alameda", 814000, 1.2, 10.00, 8.3, 6, 3, 1, 2, 1471300, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Moraga", "Contra Costa", 820000, 1.2, 10.00, 8.2, 5, 3, 1, 1, 821200, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Novato", "Marin", 712600, 1.1, 8.54, 8.1, 17, 10, 5, 5, 685000, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Alameda", "Alameda", 658200, 1.0, 7.77, 8.0, 21, 12, 5, 6, 675200, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("San Anselmo", "Marin", 875000, 1.3, 10.00, 7.7, 3, 2, 0, 1, 985900, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Sebastopol", "Sonoma", 687000, 1.0, 7.82, 7.7, 14, 9, 7, 3, 817600, "Urban Fringe of Mid-size City "));
        _citiesAboveApiCutoff.add(new Bpsv("Kensington", "Alameda", 890450, 1.3, 10.00, 7.6, 1, 1, 0, 0, 920600, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Brisbane", "San Mateo", 673500, 1.0, 7.50, 7.5, 2, 1, 1, 0, 741200, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Glen Ellen", "Sonoma", 630000, 0.9, 7.00, 7.5, 1, 1, 0, 0, 893900, "Rural "));
        _citiesAboveApiCutoff.add(new Bpsv("Pacifica", "San Mateo", 685000, 1.0, 7.57, 7.5, 9, 6, 5, 2, 671000, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Mountain View", "Santa Clara", 718000, 1.1, 7.89, 7.4, 12, 8, 2, 2, 664800, "Mid-size City "));
        _citiesAboveApiCutoff.add(new Bpsv("Cupertino", "Santa Clara", 901000, 1.3, 9.88, 7.4, 15, 9, 3, 3, 924000, "Mid-size City, Urban Fringe of Large City"));
        _citiesAboveApiCutoff.add(new Bpsv("Corte Madera", "Marin", 917300, 1.4, 10.00, 7.4, 1, 1, 0, 0, 957200, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("San Carlos", "San Mateo", 900000, 1.3, 9.61, 7.2, 7, 5, 3, 0, 826000, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Sunnyvale", "Santa Clara", 705300, 1.0, 7.45, 7.1, 20, 13, 5, 2, 660000, "Mid-size City "));
        _citiesAboveApiCutoff.add(new Bpsv("Yountville", "Napa", 859000, 1.3, 9.00, 7.1, 1, 1, 0, 0, 807300, "Urban Fringe of Mid-size City "));
        _citiesAboveApiCutoff.add(new Bpsv("Millbrae", "San Mateo", 900000, 1.3, 9.27, 7.0, 5, 3, 1, 1, 1076300, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Danville", "Contra Costa", 984700, 1.5, 10.00, 6.9, 13, 7, 3, 3, 960000, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Mill Valley", "Marin", 992500, 1.5, 10.00, 6.8, 7, 5, 1, 1, 918000, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Belmont", "San Mateo", 890000, 1.3, 8.97, 6.8, 6, 4, 1, 1, 855000, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Lafayette", "Contra Costa", 997000, 1.5, 10.00, 6.8, 6, 4, 1, 1, 1197800, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("San Rafael", "Marin", 770600, 1.1, 7.25, 6.4, 17, 11, 4, 5, 685800, "Mid-size City, Urban Fringe of Large City"));
        _citiesAboveApiCutoff.add(new Bpsv("Los Gatos", "Santa Clara", 1065800, 1.6, 9.72, 6.2, 10, 6, 3, 1, 972500, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("San Mateo", "San Mateo", 778500, 1.2, 7.06, 6.1, 19, 13, 3, 3, 747500, "Mid-size City, Urban Fringe of Large City"));
        _citiesAboveApiCutoff.add(new Bpsv("Orinda", "Contra Costa", 1116500, 1.7, 10.00, 6.0, 6, 4, 1, 1, 1151700, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Half Moon Bay", "San Mateo", 806500, 1.2, 7.00, 5.9, 5, 2, 1, 2, 921500, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Larkspur", "Marin", 1185000, 1.8, 9.94, 5.7, 4, 0, 1, 3, 1259800, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Angwin", "Napa", 971000, 1.4, 8.00, 5.6, 1, 1, 1, 0, 689800, "Rural "));
        _citiesAboveApiCutoff.add(new Bpsv("Occidental", "Sonoma", 1027200, 1.5, 8.07, 5.3, 2, 1, 1, 0, 825700, "Rural "));
        _citiesAboveApiCutoff.add(new Bpsv("Bolinas", "Marin", 1063800, 1.6, 8.00, 5.1, 1, 1, 1, 0, 833800, "Rural "));
        _citiesAboveApiCutoff.add(new Bpsv("Woodside", "San Mateo", 1040000, 1.5, 7.56, 4.9, 2, 1, 1, 1, 1146900, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Alamo", "Contra Costa", 1392500, 2.1, 10.00, 4.8, 4, 3, 2, 1, 1553500, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Saratoga", "Santa Clara", 1367000, 2.0, 9.35, 4.6, 9, 6, 3, 2, 1455000, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Hillsborough", "San Mateo", 1471700, 2.2, 10.00, 4.6, 4, 3, 1, 0, 2846500, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Burlingame", "San Mateo", 1325000, 2.0, 8.91, 4.5, 7, 5, 1, 1, 1593400, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Los Altos", "Santa Clara", 1547200, 2.3, 9.71, 4.2, 10, 7, 2, 1, 1427500, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Tiburon", "Marin", 1755000, 2.6, 10.00, 3.8, 3, 2, 1, 0, 1616100, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Kentfield", "Marin", 1777000, 2.6, 10.00, 3.8, 2, 1, 1, 0, 1506200, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Stanford", "Santa Clara", 1827350, 2.7, 10.00, 3.7, 2, 2, 0, 0, 1560100, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Ross", "Marin", 2585000, 3.8, 10.00, 2.6, 1, 1, 1, 0, 1620800, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Atherton", "San Mateo", 2275000, 3.4, 7.51, 2.2, 5, 4, 1, 1, 1676500, "Urban Fringe of Large City "));
        _citiesAboveApiCutoff.add(new Bpsv("Portola Valley", "San Mateo", 3857100, 5.7, 10.00, 1.8, 2, 2, 1, 0, 1684300, "Urban Fringe of Large City "));


        _citiesBelowApiCutoff = new ArrayList();
        _citiesBelowApiCutoff.add(new Bpsv("American Canyon", "Solano", 634100, 0.9, 6.31, 6.7, 5, 3, 2, 0, 556800, "Urban Fringe of Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("Antioch", "Contra Costa", 496300, 0.7, 4.99, 6.8, 24, 16, 8, 5, 520000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Berkeley", "Alameda", 730100, 1.1, 6.37, 5.9, 16, 11, 3, 2, 730000, "Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("Brentwood", "Contra Costa", 605500, 0.9, 6.83, 7.6, 14, 8, 3, 5, 685000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Calistoga", "Napa", 615000, 0.9, 3.85, 4.2, 3, 1, 1, 2, 724000, "Urban Fringe of Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("Campbell", "Santa Clara", 645000, 1.0, 5.83, 6.1, 6, 4, 1, 1, 695000, "Urban Fringe of Large City"));
        _citiesBelowApiCutoff.add(new Bpsv("Cloverdale", "Sonoma", 499000, 0.7, 5.00, 6.8, 4, 2, 1, 2, 588900, "Urban Fringe of Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("Colma", "San Mateo", 717600, 1.1, 6.00, 5.6, 2, 1, 1, 0, 630400, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Concord", "Contra Costa", 504000, 0.7, 4.81, 6.4, 29, 16, 6, 10, 540000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Cotati", "Sonoma", 530000, 0.8, 6.00, 7.6, 1, 1, 0, 0, 640200, "Urban Fringe of Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("Crockett", "Contra Costa", 551000, 0.8, 4.38, 5.4, 3, 1, 2, 2, 553100, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Daly City", "San Mateo", 675300, 1.0, 5.72, 5.7, 20, 15, 3, 3, 670000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Dixon", "Solano", 469000, 0.7, 4.88, 7.0, 9, 5, 2, 3, 465000, "Urban Fringe of Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("East Palo Alto", "San Mateo", 616700, 0.9, 2.75, 3.0, 8, 7, 6, 0, 616400, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("El Cerrito", "Contra Costa", 616500, 0.9, 4.52, 5.0, 6, 4, 1, 1, 733000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("El Sobrante", "Contra Costa", 520000, 0.8, 4.05, 5.3, 4, 3, 1, 0, 546700, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Emeryville", "Alameda", 440000, 0.7, 3.08, 4.7, 2, 1, 1, 1, 460000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Fairfield", "Solano", 459400, 0.7, 5.71, 8.4, 29, 14, 9, 10, 465000, "Mid-size City"));
        _citiesBelowApiCutoff.add(new Bpsv("Forestville", "Sonoma", 420000, 0.6, 6.80, 10.9, 4, 1, 1, 3, 655700, "Urban Fringe of Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("Gilroy", "Santa Clara", 675000, 1.0, 4.98, 5.0, 15, 8, 4, 4, 690000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Hayward", "Alameda", 535300, 0.8, 3.61, 4.6, 39, 27, 10, 8, 545000, "Mid-size City, Urban Fringe of Large City"));
        _citiesBelowApiCutoff.add(new Bpsv("Healdsburg", "Sonoma", 575000, 0.9, 5.97, 7.0, 7, 4, 1, 2, 652900, "Urban Fringe of Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("Hercules", "Contra Costa", 480000, 0.7, 6.38, 9.0, 5, 3, 1, 1, 525000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Menlo Park", "San Mateo", 973400, 1.4, 5.40, 3.7, 8, 7, 7, 1, 1020500, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Montara", "San Mateo", 776500, 1.2, 6.00, 5.2, 1, 1, 0, 0, 868600, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Monte Rio", "Sonoma", 512600, 0.8, 5.00, 6.6, 1, 1, 1, 0, 539600, "Urban Fringe of Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("Morgan Hill", "Santa Clara", 725000, 1.1, 6.92, 6.4, 12, 8, 4, 4, 795000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Napa", "Napa", 569800, 0.8, 5.66, 6.7, 34, 24, 8, 7, 544000, "Mid-size City"));
        _citiesBelowApiCutoff.add(new Bpsv("Newark", "Alameda", 593000, 0.9, 5.20, 5.9, 16, 11, 5, 6, 588000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Oakland", "Alameda", 481700, 0.7, 3.19, 4.5, 127, 73, 40, 32, 500000, "Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Oakley", "Contra Costa", 465000, 0.7, 5.89, 8.5, 7, 4, 2, 1, 501000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Pinole", "Contra Costa", 530000, 0.8, 4.45, 5.7, 7, 4, 2, 2, 545000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Pittsburg", "Contra Costa", 435000, 0.6, 2.71, 4.2, 17, 12, 4, 4, 452500, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Redwood City", "San Mateo", 712900, 1.1, 5.17, 4.9, 20, 15, 8, 5, 710000, "Mid-size City, Urban Fringe of Large City"));
        _citiesBelowApiCutoff.add(new Bpsv("Richmond", "Contra Costa", 418400, 0.6, 2.40, 3.9, 29, 18, 3, 8, 439000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Rio Vista", "Solano", 399000, 0.6, 4.56, 7.7, 3, 1, 1, 1, 386000, "Urban Fringe of Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("Rodeo", "Contra Costa", 511000, 0.8, 3.00, 4.0, 1, 1, 0, 0, 540600, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Rohnert Park", "Sonoma", 490000, 0.7, 6.38, 8.8, 14, 8, 4, 4, 507000, "Urban Fringe of Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("San Bruno", "San Mateo", 665000, 1.0, 6.90, 7.0, 13, 9, 1, 3, 680000, "Mid-size City, Urban Fringe of Large City"));
        _citiesBelowApiCutoff.add(new Bpsv("San Francisco", "San Francisco", 779300, 1.2, 6.18, 5.4, 125, 78, 29, 30, 750000, "Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("San Jose", "Santa Clara", 622200, 0.9, 6.22, 6.8, 220, 134, 47, 51, 645000, "Large City"));
        _citiesBelowApiCutoff.add(new Bpsv("San Leandro", "Alameda", 526600, 0.8, 4.66, 6.0, 16, 10, 4, 3, 547000, "Mid-size City, Urban Fringe of Large City"));
        _citiesBelowApiCutoff.add(new Bpsv("San Lorenzo", "Alameda", 540000, 0.8, 3.87, 4.8, 10, 6, 3, 2, 555000, "Mid-size City, Urban Fringe of Large City"));
        _citiesBelowApiCutoff.add(new Bpsv("San Martin", "Santa Clara", 890000, 1.3, 4.00, 3.0, 1, 1, 0, 0, 955300, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("San Pablo", "Contra Costa", 440000, 0.7, 1.67, 2.6, 11, 8, 3, 4, 488200, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Santa Clara", "Santa Clara", 622900, 0.9, 6.43, 7.0, 20, 15, 3, 4, 675000, "Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("Santa Rosa", "Sonoma", 533500, 0.8, 6.44, 8.1, 69, 44, 20, 16, 519000, "Mid-size City"));
        _citiesBelowApiCutoff.add(new Bpsv("Sausalito", "Marin", 750000, 1.1, 4.00, 3.6, 2, 2, 1, 0, 818000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Sonoma", "Sonoma", 618000, 0.9, 5.61, 6.1, 12, 7, 5, 4, 699000, "Urban Fringe of Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("South San Francisco", "San Mateo", 685000, 1.0, 6.35, 6.3, 12, 6, 4, 3, 720000, "Mid-size City, Urban Fringe of Large City"));
        _citiesBelowApiCutoff.add(new Bpsv("St. Helena", "Napa", 844500, 1.3, 6.73, 5.4, 5, 2, 1, 2, 1047200, "Urban Fringe of Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("Suisun City", "Solano", 425000, 0.6, 5.34, 8.5, 3, 2, 1, 0, 439500, "Urban Fringe of Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("Union City", "Alameda", 617500, 0.9, 6.38, 7.0, 11, 7, 3, 1, 663000, "Urban Fringe of Large City "));
        _citiesBelowApiCutoff.add(new Bpsv("Vacaville", "Solano", 438100, 0.6, 5.93, 9.1, 21, 13, 5, 7, 440000, "Urban Fringe of Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("Vallejo", "Solano", 423200, 0.6, 3.23, 5.1, 28, 17, 8, 7, 420000, "Mid-size City "));
        _citiesBelowApiCutoff.add(new Bpsv("Windsor", "Sonoma", 589500, 0.9, 6.13, 7.0, 6, 3, 2, 2, 602000, "Urban Fringe of Mid-size City "));
    }


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ISessionFacade sc = SessionFacade.getInstance(request);


        ModelAndView modelAndView = new ModelAndView("/school/performance/schoolValues");

        List values = _citiesAboveApiCutoff;
        if (StringUtils.equals(request.getParameter(PARAM_LIST), PARAM_LIST_VALUE_BELOW_AVG_API)) {
            values = _citiesBelowApiCutoff;
            modelAndView.addObject(MODEL_SHOW_RANK, Boolean.FALSE);
        } else {
            modelAndView.addObject(MODEL_SHOW_RANK, Boolean.TRUE);
        }

        int limit = Integer.MAX_VALUE;
        if (StringUtils.isNumeric(request.getParameter(PARAM_LIMIT))) {
            limit = Integer.valueOf(request.getParameter(PARAM_LIMIT)).intValue();
        }

        modelAndView.addObject(MODEL_CITY_LIST,
                limit == Integer.MAX_VALUE ?
                        values :
                        values.subList(0, limit));
        modelAndView.addObject(MODEL_PAGE_SUBTITLE,
                limit == Integer.MAX_VALUE ?
                        "All Bay Area Cities" :
                        "Top " + limit + " Bay Area Cities");
        return modelAndView;
    }


    private static class Bpsv
            implements BestPublicSchoolValuesController.IBestPublicSchoolValue {

        private int _rank;
        private String _cityName;
        private String _countyName;
        private int _medianHomePrice;
        private double _averageApiRank;
        private int _schoolsCount;
        private int _elementarySchoolsCount;
        private int _middleSchoolsCount;
        private int _highSchoolsCount;
        private String _cityPageHref;
        private String _schoolsPageUrl;


        public Bpsv() {
        }

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
                    int population,
                    String localeDescription) {
            _rank = _citiesAboveApiCutoff.size() + 1;
            _cityName = cityName;
            _countyName = countyName;
            _medianHomePrice = medianHomePrice;
            _averageApiRank = averageApiRank;
            _schoolsCount = schoolsCount;
            _elementarySchoolsCount = elementarySchoolsCount;
            _middleSchoolsCount = middleSchoolsCount;
            _highSchoolsCount = highSchoolsCount;
            UrlBuilder builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.CA, _cityName);
            _cityPageHref = builder.asSiteRelative(null);
            builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.CA, _cityName);
            _schoolsPageUrl = builder.asSiteRelative(null);
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
    }

}
