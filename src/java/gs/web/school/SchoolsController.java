/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolsController.java,v 1.40 2008/02/26 17:44:51 cpickslay Exp $
 */

package gs.web.school;

import gs.data.geo.IGeoDao;
import gs.data.school.LevelCode;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.search.SearchCommand;
import gs.data.search.Searcher;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.search.ResultsPager;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.search.Hits;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SchoolsController extends AbstractController {

    private static Logger _log = Logger.getLogger(SchoolsController.class);

    private Searcher _searcher;
    private IDistrictDao _districtDao;
    private IGeoDao _geoDao;

    // INPUTS
    public static final String PARAM_PAGE = "p";
    public static final String PARAM_DISTRICT = "district";
    public static final String PARAM_DISTRICT_NAME = "distname";
    public static final String PARAM_SHOW_ALL = "showall";
    /**
     * The name of the city, if provided.
     */
    public static final String PARAM_CITY = "city";
    /**
     * Zero or more of (e,m,h).
     */
    public static final String PARAM_LEVEL_CODE = "lc";
    /**
     * Zero or more of (public,private,charter), as separate parameters.
     */
    public static final String PARAM_SCHOOL_TYPE = "st";

    public static final String PARAM_SHOW_MAP = "map";

    // OUTPUT
    // request attributes
    public static final String MODEL_PAGE = "p";
    /**
     * The name of the city, if provided.
     */
    public static final String MODEL_CITY_NAME = "cityName";
    public static final String MODEL_CITY_DISPLAY_NAME = "cityDisplayName";

    /**
     * Whether we should show all records
     */
    public static final String MODEL_SHOW_ALL = "showAll";

    /**
     * The ID of the district, if provided.
     */
    public static final String MODEL_DISTRICT = "district";
    public static final String MODEL_DISTRICT_OBJECT = "districtObject";
    public static final String MODEL_DISTNAME = "distname";
    public static final String MODEL_DIST_CITY_NAME = "distCityName";
    /**
     * An optional LevelCode object.
     */
    public static final String MODEL_LEVEL_CODE = "lc";

    /**
     * Zero or more of (public,private,charter), in a String[].
     */
    public static final String MODEL_SCHOOL_TYPE = "st";

    // model properties: request.* (as well)
    /**
     * Total number of results available for the query.
     */
    public static final String MODEL_SCHOOLS_TOTAL = "schoolsTotal";
    /**
     * Total number of results available for the query (the same).
     */
    public static final String MODEL_TOTAL = "total";
    /**
     * A List of School objects.
     */
    public static final String MODEL_SCHOOLS = "schools";
    /**
     * Requested page size. The number of items on the page is the size
     * of the schools list.
     */
    public static final String MODEL_PAGE_SIZE = "pageSize";

    /**
     * Though this method throws <code>Exception</code>, it should swallow most
     * (all?) searching errors while just logging appropriately and returning
     * no results to the user.  Search/Query/Parsing errors are meaningless to
     * most users and should be handled internally.
     *
     * @return a <code>ModelAndView</code> which contains Map containting
     *         search results and attendant parameters as the model.
     * @throws Exception
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
            throws Exception {

        SessionContext context = SessionContextUtil.getSessionContext(request);
        State state = context.getState();

        Map model = new HashMap();

        final String[] paramLevelCode = request.getParameterValues(PARAM_LEVEL_CODE);
        LevelCode levelCode = null;
        if (paramLevelCode != null) {
            levelCode = LevelCode.createLevelCode(paramLevelCode);
            model.put(MODEL_LEVEL_CODE, levelCode);
        }

        final String[] paramSchoolType = request.getParameterValues(PARAM_SCHOOL_TYPE);

        if (paramSchoolType != null) {
            model.put(MODEL_SCHOOL_TYPE, paramSchoolType);
        }

        int page = 1;
        String p = request.getParameter(PARAM_PAGE);
        if (p != null) {
            try {
                page = Integer.parseInt(p);
            } catch (Exception e) {
                // ignore this and just assume the page is 1.
            }
        }
        model.put(MODEL_PAGE, Integer.toString(page));

        int pageSize = 10;

        String paramShowAll = request.getParameter(PARAM_SHOW_ALL);
        if (context.isCrawler()) {
            pageSize = 100;
        } else if (StringUtils.equals(paramShowAll, "1") ||
                StringUtils.equals(paramShowAll, "true")) {
            pageSize = -1;
        }

        SearchCommand searchCommand = new SearchCommand();
        searchCommand.setC("school");
        searchCommand.setState(state);
        if (levelCode != null) {
            searchCommand.setLevelCode(levelCode);
        }
        searchCommand.setSt(paramSchoolType);

        String cityName = StringUtils.capitalize(request.getParameter(PARAM_CITY));
        if (cityName != null) {
            String displayName = cityName;
            if (displayName.equals("New York")) {
                displayName += " City";
            } else if (State.DC.equals(state) &&
                    displayName.equals("Washington")) {
                displayName += ", DC";
            }
            model.put(MODEL_CITY_NAME, cityName);
            model.put(MODEL_CITY_DISPLAY_NAME, displayName);
            searchCommand.setCity(cityName);
            searchCommand.setQ(cityName);

        } else {
            String districtParam = request.getParameter(PARAM_DISTRICT);
            if (districtParam != null) {

                // Look up the district name
                String districtIdStr = request.getParameter(PARAM_DISTRICT);
                model.put(MODEL_DISTRICT, districtIdStr);
                int districtId = Integer.parseInt(districtIdStr);
                District district = null;
                try {
                    district = _districtDao.findDistrictById(state, new Integer(districtId));
                    model.put(MODEL_DISTNAME, district.getName());

                    Address districtAddress = district.getPhysicalAddress();
                    if (districtAddress != null) {
                        String city = districtAddress.getCity();
                        if (city != null) {
                            model.put(MODEL_DIST_CITY_NAME, city);
                        } else {
                            model.put(MODEL_DIST_CITY_NAME, "");
                        }
                    } else {
                        model.put(MODEL_DIST_CITY_NAME, "");                        
                    }

                    model.put(MODEL_DISTRICT_OBJECT, district);
                    
                    searchCommand.setDistrict(districtIdStr);
                    // the following is not needed and breaks sometimes. See SearcherTest.
                    // searchCommand.setQ(district.getName());
                } catch (ObjectRetrievalFailureException e) {
                    _log.warn(state + ": District Id " + districtId + " not found.");
                    BindException errors = new BindException(searchCommand, "searchCommand");
                    errors.reject("error_no_district", "District was not found.");

                    model.put("errors", errors);
                    model.put("showSearchControl", Boolean.TRUE);
                    model.put("title", "District not found");

                    ModelAndView modelAndView = new ModelAndView("status/error", model);
                    return modelAndView;
                }
            }
        }

        // Build the results and the model
        Hits hts = _searcher.search(searchCommand);
        if (hts != null) {
            ResultsPager _resultsPager = new ResultsPager(hts, ResultsPager.ResultType.school);
            Map resultsModel = new HashMap();
            resultsModel.put(MODEL_SCHOOLS_TOTAL, new Integer(hts.length()));
            resultsModel.put(MODEL_SCHOOLS, _resultsPager.getResults(page, pageSize));
            resultsModel.put(MODEL_PAGE_SIZE, new Integer(pageSize));
            resultsModel.put(MODEL_TOTAL, new Integer(hts.length()));
            resultsModel.put(MODEL_SHOW_ALL, paramShowAll);
            model.put("results", resultsModel);
        } else {
            _log.warn("Hits object is null for SearchCommand: " + searchCommand);
        }

        final ModelAndView modelAndView = new ModelAndView("school/schoolsTable", model);
        return modelAndView;

    }


    /**
     * A setter for Spring
     */
    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }


    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public static String calcCitySchoolsTitle(String cityDisplayName, LevelCode levelCode, String[] schoolType) {
        StringBuffer sb = new StringBuffer();
        sb.append(cityDisplayName);
        if (schoolType != null && schoolType.length == 1) {
            if ("private".equals(schoolType[0])) {
                sb.append(" Private");
            } else if ("charter".equals(schoolType[0])) {
                sb.append(" Charter");
            } else {
                sb.append(" Public");
            }
        }
        if (levelCode != null &&
                levelCode.getCommaSeparatedString().length() == 1) {
            if (levelCode.containsLevelCode(LevelCode.Level.PRESCHOOL_LEVEL)) {
                sb.append(" Preschool");
            } else if (levelCode.containsLevelCode(LevelCode.Level.ELEMENTARY_LEVEL)) {
                sb.append(" Elementary");
            } else if (levelCode.containsLevelCode(LevelCode.Level.MIDDLE_LEVEL)) {
                sb.append(" Middle");
            } else if (levelCode.containsLevelCode(LevelCode.Level.HIGH_LEVEL)) {
                sb.append(" High");
            }
        }
        sb.append(" Schools");
        return sb.toString();
    }

    public static String calcMetaDesc(String districtDisplayName, String cityDisplayName, LevelCode levelCode, String[] schoolType) {
        StringBuffer sb = new StringBuffer();
        StringBuffer cityWithModifier = new StringBuffer();
        StringBuffer modifier = new StringBuffer();

        if (schoolType != null && schoolType.length == 1) {
            if ("private".equals(schoolType[0])) {
                modifier.append("private");
            } else if ("charter".equals(schoolType[0])) {
                modifier.append("charter");
            } else {
                modifier.append("public");
            }
            modifier.append(" ");

        }

         if (levelCode != null &&
                levelCode.getCommaSeparatedString().length() == 1) {
            if (levelCode.containsLevelCode(LevelCode.Level.PRESCHOOL_LEVEL)) {
                modifier.append("preschool");
            } else if (levelCode.containsLevelCode(LevelCode.Level.ELEMENTARY_LEVEL)) {
                modifier.append("elementary");
            } else if (levelCode.containsLevelCode(LevelCode.Level.MIDDLE_LEVEL)) {
                modifier.append("middle");
            } else if (levelCode.containsLevelCode(LevelCode.Level.HIGH_LEVEL)) {
                modifier.append("high");
            }
             modifier.append(" ");

        }



        if (districtDisplayName == null) {
            cityWithModifier.append(cityDisplayName+" "+modifier);
        sb.append("View and map all "+cityWithModifier+"schools. Plus, compare or save "+modifier+"schools.");
        }
         else if (districtDisplayName != null)
        {
           

           sb.append("View and map all "+modifier+"schools in the "+districtDisplayName+". Plus, compare or save "+modifier+"schools in this district.");
        }

        return sb.toString();
    }

}
