/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolsController.java,v 1.1 2006/03/22 00:55:22 apeterson Exp $
 */

package gs.web.school;

import org.springframework.web.servlet.mvc.AbstractFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.Hits;
import gs.data.search.SpellCheckSearcher;
import gs.data.search.Searcher;
import gs.data.search.SearchCommand;
import gs.web.search.ResultsPager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.naming.OperationNotSupportedException;
import java.util.Map;
import java.util.HashMap;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SchoolsController extends AbstractFormController {

    public static final String BEAN_ID = "/search/search.page";

    private static Logger _log = Logger.getLogger(SchoolsController.class);

    private Searcher _searcher;
    private ResultsPager _resultsPager;

    // INPUTS
    // see SearchCommand, which Spring wires up
    public static final String PARAM_QUERY = "q";
    public static final String PARAM_PAGE = "p";
    public static final String PARAM_DISTRICT = "district";
    public static final String PARAM_DISTRICT_NAME = "distname";
    public static final String PARAM_SHOW_ALL = "showall";
    public static final String PARAM_CITY = "city";
    public static final String PARAM_LEVEL_CODE = "gl";
    public static final String PARAM_SCHOOL_TYPE = "st";

    // OUTPUT
    // request attributes
    public static final String REQ_ATTR_QUERY = "q";
    public static final String REQ_ATTR_DISTNAME = "distname";
    public static final String REQ_ATTR_PAGE = "p";
    public static final String REQ_ATTR_CITY = "city";
    public static final String REQ_ATTR_DISTRICT = "district";
    public static final String REQ_ATTR_LEVEL_CODE = "gl";
    public static final String REQ_ATTR_SCHOOL_TYPE = "st";

    // model properties: request.*
    public static final String MODEL_SCHOOLS_TOTAL = "schoolsTotal";
    public static final String MODEL_SCHOOLS = "schools";
    public static final String MODEL_PAGE_SIZE = "pageSize";
    public static final String MODEL_MAIN_RESULTS = "mainResults";
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
    public ModelAndView processFormSubmission(
            HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
            throws Exception {


        final SearchCommand searchCommand = (SearchCommand) command;

        request.setAttribute(REQ_ATTR_QUERY, request.getParameter(PARAM_QUERY));

        if (searchCommand.getCity() != null) {
            request.setAttribute(REQ_ATTR_CITY, searchCommand.getCity());
        } else if (searchCommand.getDistrict() != null) {
            request.setAttribute(REQ_ATTR_DISTRICT, searchCommand.getDistrict());
            request.setAttribute(REQ_ATTR_DISTNAME, request.getParameter(PARAM_DISTRICT_NAME));
        }

        String[] levels = request.getParameterValues(PARAM_LEVEL_CODE);
        if (levels != null) {
            request.setAttribute(REQ_ATTR_LEVEL_CODE, levels);
        }

        String[] sTypes = request.getParameterValues(PARAM_SCHOOL_TYPE);
        if (sTypes != null) {
            request.setAttribute(REQ_ATTR_SCHOOL_TYPE, sTypes);
        }

        int page = 1;
        String p = request.getParameter(PARAM_PAGE);
        if (p != null) {
            try {
                request.setAttribute(REQ_ATTR_PAGE, p);
                page = Integer.parseInt(p);
            } catch (Exception e) {
                // ignore this and just assume the page is 1.
            }
        }

        int pageSize = 10;
        String paramShowAll = request.getParameter(PARAM_SHOW_ALL);
        int schoolsPageSize = StringUtils.equals(paramShowAll, "true") ? -1 : 10;

        // Build the results and the model
        Map model = new HashMap();

        Hits hts = _searcher.search(searchCommand);
        if (hts != null) {

            _resultsPager.load(hts, searchCommand.getType());

            model.put(MODEL_SCHOOLS_TOTAL, new Integer(_resultsPager.getSchoolsTotal()));
            model.put(MODEL_SCHOOLS, _resultsPager.getSchools(page, schoolsPageSize));
            model.put(MODEL_PAGE_SIZE, new Integer(pageSize));
            model.put(MODEL_MAIN_RESULTS, _resultsPager.getResults(page, pageSize));
            model.put(MODEL_TOTAL, new Integer(hts.length()));
        } else {
            _log.warn("Hits object is null for SearchCommand: " + searchCommand);
        }

        return new ModelAndView("search/schoolsOnly", "results", model);
    }

    public boolean isFormSubmission(HttpServletRequest request) {
        return true;
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new SearchCommand();
    }

    public ModelAndView showForm(HttpServletRequest request,
                                 HttpServletResponse response, BindException errors)
            throws Exception {
        throw new OperationNotSupportedException();
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

}
