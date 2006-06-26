/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolsController.java,v 1.17 2006/06/26 21:26:17 apeterson Exp $
 */

package gs.web.school;

import gs.data.geo.IGeoDao;
import gs.data.school.LevelCode;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.search.SearchCommand;
import gs.data.search.Searcher;
import gs.data.state.State;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.web.ISessionContext;
import gs.web.SessionContextUtil;
import gs.web.search.ResultsPager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.search.Hits;
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
     * The ID of the district, if provided.
     */
    public static final String MODEL_DISTRICT = "district";
    public static final String MODEL_DISTNAME = "distname";
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
        ThreadLocalTransactionManager.setReadOnly();

        ISessionContext context = SessionContextUtil.getSessionContext(request);
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
        int schoolsPageSize = StringUtils.equals(paramShowAll, "true") ||
                StringUtils.equals(paramShowAll, "1") ? -1 : 10;


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
                District district = _districtDao.findDistrictById(state, new Integer(districtId));
                model.put(MODEL_DISTNAME, district.getName());
                searchCommand.setDistrict(districtIdStr);
                // the following is not needed and breaks sometimes. See SearcherTest.
                // searchCommand.setQ(district.getName());
            }
        }

        // Build the results and the model
        Hits hts = _searcher.search(searchCommand);
        if (hts != null) {
            ResultsPager _resultsPager = new ResultsPager(hts, "school");
            Map resultsModel = new HashMap();
            resultsModel.put(MODEL_SCHOOLS_TOTAL, new Integer(_resultsPager.getSchoolsTotal()));
            resultsModel.put(MODEL_SCHOOLS, _resultsPager.getSchools(page, schoolsPageSize));
            resultsModel.put(MODEL_PAGE_SIZE, new Integer(pageSize));
            resultsModel.put(MODEL_TOTAL, new Integer(hts.length()));
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
}
