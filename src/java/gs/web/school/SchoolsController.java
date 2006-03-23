/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolsController.java,v 1.5 2006/03/23 18:21:38 apeterson Exp $
 */

package gs.web.school;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.Hits;
import gs.data.search.Searcher;
import gs.data.search.SearchCommand;
import gs.data.school.district.IDistrictDao;
import gs.data.school.district.District;
import gs.data.state.State;
import gs.web.search.ResultsPager;
import gs.web.SessionContext;
import gs.web.ISessionFacade;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SchoolsController extends AbstractController {

    private static Logger _log = Logger.getLogger(SchoolsController.class);

    private Searcher _searcher;
    private ResultsPager _resultsPager;
    private IDistrictDao _districtDao;

    // INPUTS
    // see SearchCommand, which Spring wires up
    public static final String PARAM_QUERY = "q";
    public static final String PARAM_PAGE = "p";
    public static final String PARAM_DISTRICT = "district";
    public static final String PARAM_DISTRICT_NAME = "distname";
    public static final String PARAM_SHOW_ALL = "showall";
    public static final String PARAM_CITY = "city";
    public static final String PARAM_LEVEL_CODE = "lc";
    public static final String PARAM_SCHOOL_TYPE = "st";

    // OUTPUT
    // request attributes
    public static final String REQ_ATTR_QUERY = "q";
    public static final String REQ_ATTR_DISTNAME = "distname";
    public static final String REQ_ATTR_PAGE = "p";
    public static final String REQ_ATTR_CITY = "city";
    public static final String REQ_ATTR_DISTRICT = "district";
    public static final String REQ_ATTR_LEVEL_CODE = "lc";
    public static final String REQ_ATTR_SCHOOL_TYPE = "st";

    // model properties: request.*
    public static final String MODEL_SCHOOLS_TOTAL = "schoolsTotal";
    public static final String MODEL_SCHOOLS = "schools";
    public static final String MODEL_PAGE_SIZE = "pageSize";
    public static final String MODEL_TOTAL = "total";


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

        ISessionFacade context = SessionContext.getInstance(request);
        State state = context.getState();

        request.setAttribute(REQ_ATTR_QUERY, request.getParameter(PARAM_QUERY));

        final String[] paramLevelCode = request.getParameterValues(PARAM_LEVEL_CODE);
        String[] levels = paramLevelCode;
        if (levels != null) {
            request.setAttribute(REQ_ATTR_LEVEL_CODE, levels);
        }

        final String[] paramSchoolType = request.getParameterValues(PARAM_SCHOOL_TYPE);
        String[] sTypes = paramSchoolType;
        if (sTypes != null) {
            request.setAttribute(REQ_ATTR_SCHOOL_TYPE, sTypes);
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
        request.setAttribute(REQ_ATTR_PAGE, Integer.toString(page));

        int pageSize = 10;
        String paramShowAll = request.getParameter(PARAM_SHOW_ALL);
        int schoolsPageSize = StringUtils.equals(paramShowAll, "true") ||
                StringUtils.equals(paramShowAll, "1") ? -1 : 10;


        SearchCommand searchCommand = new SearchCommand();
        searchCommand.setC("school");
        searchCommand.setState(state);
        searchCommand.setGl(paramLevelCode);
        searchCommand.setSt(paramSchoolType);

        String city = StringUtils.capitalize(request.getParameter(PARAM_CITY));
        if (city != null) {
            request.setAttribute(REQ_ATTR_CITY, city);
            searchCommand.setCity(city);
            searchCommand.setQ(city);

        } else {
            String districtParam = request.getParameter(PARAM_DISTRICT);
            if (districtParam != null) {

                // Look up the district name
                String districtIdStr = request.getParameter(PARAM_DISTRICT);
                request.setAttribute(REQ_ATTR_DISTRICT, districtIdStr);
                int districtId = Integer.parseInt(districtIdStr);
                District district = _districtDao.findDistrictById(state, new Integer(districtId));
                request.setAttribute(REQ_ATTR_DISTNAME, district.getName());
                searchCommand.setDistrict(districtIdStr);
                searchCommand.setQ(district.getName());
            }
        }


        // Build the results and the model
        Map model = new HashMap();
        Hits hts = _searcher.search(searchCommand);
        if (hts != null) {

            _resultsPager.load(hts, "school");

            model.put(MODEL_SCHOOLS_TOTAL, new Integer(_resultsPager.getSchoolsTotal()));
            model.put(MODEL_SCHOOLS, _resultsPager.getSchools(page, schoolsPageSize));
            model.put(MODEL_PAGE_SIZE, new Integer(pageSize));
            model.put(MODEL_TOTAL, new Integer(hts.length()));
        } else {
            _log.warn("Hits object is null for SearchCommand: " + searchCommand);
        }

        return new ModelAndView("search/schoolsOnly", "results", model);
    }


    /**
     * A setter for Spring
     */
    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }

    /**
     * A setter for Spring
     */
    public void setResultsPager(ResultsPager resultsPager) {
        _resultsPager = resultsPager;
    }

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }
}
