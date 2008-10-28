/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolsController.java,v 1.77 2008/10/28 17:36:17 aroy Exp $
 */

package gs.web.school;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.search.Indexer;
import gs.data.search.SchoolComparatorFactory;
import gs.data.search.SearchCommand;
import gs.data.search.Searcher;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.search.ResultsPager;
import gs.web.util.*;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.path.DirectoryStructureUrlFields;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SchoolsController extends AbstractController implements IDirectoryStructureUrlController {
    public static final String BEAN_ID = "/schools.page";

    private static Logger _log = Logger.getLogger(SchoolsController.class);

    private Searcher _searcher;
    private IDistrictDao _districtDao;
    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private String _viewName;

    // INPUTS
    public static final String PARAM_PAGE = "p";
    public static final String PARAM_DISTRICT = "district";
    public static final String PARAM_DISTRICT_NAME = "distname";
    public static final String PARAM_SHOW_ALL = "showall";
    public static final String PARAM_RESULTS_PER_PAGE = "pageSize";
    public static final String PARAM_SORT_COLUMN = "sortColumn";
    public static final String PARAM_SORT_DIRECTION = "sortDirection";

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

    // OUTPUT
    // request attributes
    public static final String MODEL_PAGE = "p";
    /**
     * The name of the city, if provided.
     */
    public static final String MODEL_CITY_NAME = "cityName";
    public static final String MODEL_CITY_DISPLAY_NAME = "cityDisplayName";

    /**
     * The city id from the us_geo.city table for fetching feeds from community
     */
    public static final String MODEL_CITY_ID = "cityId";

    /**
     * Page heading
     */
    public static final String MODEL_HEADING1 = "heading1";

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

    public static final String MODEL_ALL_LEVEL_CODES = "allLevelCodes";

    public static final String MODEL_CITY_BROWSE_URI_ROOT = "cityBrowseUriRoot";
    public static final String MODEL_CITY_BROWSE_URI_LEVEL_LABEL = "cityBrowseUriLevelLabel";
    public static final String MODEL_CITY_BROWSE_URI = "cityBrowseUri";

    public static final String MODEL_IS_CITY_BROWSE = "isCityBrowse";
    public static final String MODEL_IS_DISTRICT_BROWSE = "isDistrictBrowse";

    public static final String LEVEL_LABEL_PRESCHOOLS = "preschools";
    public static final String LEVEL_LABEL_ELEMENTARY_SCHOOLS = "elementary-schools";
    public static final String LEVEL_LABEL_MIDDLE_SCHOOLS = "middle-schools";
    public static final String LEVEL_LABEL_HIGH_SCHOOLS = "high-schools";
    public static final String LEVEL_LABEL_SCHOOLS = "schools";

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
        Map<String, Object> model = new HashMap<String, Object>();

        SessionContext context = SessionContextUtil.getSessionContext(request);
        State state = context.getState();

        if (state == null) {
            BadRequestLogger.logBadRequest(_log, request, "Missing state in city/district browse request.");
            model.put("showSearchControl", Boolean.TRUE);
            model.put("title", "State not found");
            return new ModelAndView("status/error", model);            
        }

        boolean isDistrictBrowse = SchoolsController.isDistrictBrowseRequest(request);
        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);

        if (!isDistrictBrowse) {
            if (SchoolsController.isOldStyleCityBrowseRequest(request)) {
                String uri = SchoolsController.createNewCityBrowseURI(request);
                String queryString = SchoolsController.createNewCityBrowseQueryString(request);
                String redirectUrl = uri + (!StringUtils.isBlank(queryString) ? "?" + queryString : "");
                return new ModelAndView(new RedirectView301(redirectUrl));
            } else if (!shouldHandleRequest(fields)) {
                throw new IllegalStateException("Should not be in SchoolsController for city browse if expected fields not populated.");
            }
        }

        LevelCode levelCode = null;
        if (isDistrictBrowse) {
            final String[] paramLevelCode = request.getParameterValues(PARAM_LEVEL_CODE);
            if (paramLevelCode != null) {
                levelCode = LevelCode.createLevelCode(paramLevelCode);
                if (levelCode.hasMultipleLevelCodes() || levelCode.hasNoLevelCodes()) {
                    // remove any "lc" parameters
                    String queryString = request.getQueryString().replaceAll("&?lc=[^&]*","");
                    String redirectUrl = request.getRequestURI() + "?" + queryString;
                    return new ModelAndView(new RedirectView(redirectUrl));
                }
            }
        } else {
            levelCode = fields.getLevelCode();
        }

        if (levelCode != null) {
            model.put(MODEL_LEVEL_CODE, levelCode);
        }

        String[] paramSchoolType;
        if (isDistrictBrowse) {
            paramSchoolType = request.getParameterValues(PARAM_SCHOOL_TYPE);
        } else {
            paramSchoolType = fields.getSchoolTypesParams();
        }

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
        try {
            Integer paramPageSize = new Integer(request.getParameter(PARAM_RESULTS_PER_PAGE));
            if (paramPageSize > 1) {
                pageSize = paramPageSize;
            }
        } catch (Exception ex) {
            // do nothing
        }

        String paramShowAll = request.getParameter(PARAM_SHOW_ALL);
        if (StringUtils.equals(paramShowAll, "1") ||
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

        String cityName = null;
        if (isDistrictBrowse) {
            String districtParam = request.getParameter(PARAM_DISTRICT);
            if (districtParam != null) {
                // Look up the district name
                String districtIdStr = request.getParameter(PARAM_DISTRICT);
                model.put(MODEL_DISTRICT, districtIdStr);
                int districtId = Integer.parseInt(districtIdStr);
                District district;
                try {
                    district = _districtDao.findDistrictById(state, districtId);
                    model.put(MODEL_DISTNAME, district.getName());

                    model.put(MODEL_HEADING1, "Schools in " + district.getName());

                    Address districtAddress = district.getPhysicalAddress();
                    if (districtAddress != null) {
                        cityName = districtAddress.getCity();
                        if (cityName != null) {
                            model.put(MODEL_DIST_CITY_NAME, cityName);
                        } else {
                            model.put(MODEL_DIST_CITY_NAME, "");
                        }
                    } else {
                        model.put(MODEL_DIST_CITY_NAME, "");
                    }
                    model.put(MODEL_DISTRICT_OBJECT, district);
                    model.put(MODEL_ALL_LEVEL_CODES, _schoolDao.getLevelCodeInDistrict(districtId, state));
                    searchCommand.setDistrict(districtIdStr);
                } catch (ObjectRetrievalFailureException e) {
                    BadRequestLogger.logBadRequest(_log, request, state + ": District Id " + districtId + " not found.", e);
                    BindException errors = new BindException(searchCommand, "searchCommand");
                    errors.reject("error_no_district", "District was not found.");

                    model.put("errors", errors);
                    model.put("showSearchControl", Boolean.TRUE);
                    model.put("title", "District not found");

                    return new ModelAndView("status/error", model);
                }
            }
        } else {
            cityName = fields.getCityName();
            model.put(MODEL_CITY_BROWSE_URI_ROOT, DirectoryStructureUrlFactory.createNewCityBrowseURIRoot(state, cityName));
            model.put(MODEL_CITY_BROWSE_URI_LEVEL_LABEL, DirectoryStructureUrlFactory.createNewCityBrowseURILevelLabel(levelCode));
            model.put(MODEL_CITY_BROWSE_URI, DirectoryStructureUrlFactory.createNewCityBrowseURI(state, cityName, fields.getSchoolTypes(), levelCode));
            searchCommand.setCity(cityName);
            searchCommand.setQ(cityName);
            model.put(MODEL_ALL_LEVEL_CODES, _schoolDao.getLevelCodeInCity(cityName, state));        
        }

        // Get the city from us_geo.city
        if (cityName != null) {
            City city = _geoDao.findCity(state, cityName);
            if (city != null) {
                model.put(MODEL_CITY_ID, city.getId());
                PageHelper.setCityIdCookie(request, response, city);
                cityName = WordUtils.capitalize(city.getDisplayName());
                cityName = WordUtils.capitalize(cityName, new char[]{'-'});
                if (cityName.equals("Washington, DC")) {
                    // GS-7153 - unfortunately, City.getDisplayName() appends ", DC", which
                    // causes problems on the city browse page
                    cityName = "Washington";
                }
                String displayName = cityName;
                if (displayName.equals("New York")) {
                    displayName += " City";
                } else if (State.DC.equals(state) &&
                        displayName.equals("Washington")) {
                    displayName += ", DC";
                }
                model.put(MODEL_CITY_NAME, cityName);
                model.put(MODEL_CITY_DISPLAY_NAME, displayName);
                if (!isDistrictBrowse) {
                    if (levelCode != null &&
                        levelCode.getCommaSeparatedString().length() == 1 &&
                        levelCode.containsLevelCode(LevelCode.Level.PRESCHOOL_LEVEL)) {
                        // Added for GS-7375, heading1 for preschool filter shouldn't use the same
                        // text as the title
                        model.put(MODEL_HEADING1, displayName + " Preschools");
                    } else {
                        model.put(MODEL_HEADING1, calcCitySchoolsTitle(displayName, levelCode, paramSchoolType));
                    }
                }
            } else {
                BadRequestLogger.logBadRequest(_log, request, "City not found in state in city/district browse request.");
                model.put("showSearchControl", Boolean.TRUE);
                model.put("title", "City not found in state");
                return new ModelAndView("status/error", model);
            }
        }

        model.put(MODEL_IS_CITY_BROWSE, !isDistrictBrowse);
        model.put(MODEL_IS_DISTRICT_BROWSE, isDistrictBrowse);

        // Build the results and the model
        String sortColumn = request.getParameter(PARAM_SORT_COLUMN);
        String sortDirection = request.getParameter(PARAM_SORT_DIRECTION);
        if (sortColumn == null) {
            sortColumn = "schoolResultsHeader";
            sortDirection = "asc";
        }
        if (sortDirection == null) {
            if (sortColumn.equals("schoolResultsHeader")) {
                sortDirection = "asc";
            } else {
                sortDirection = "desc";
            }
        }

        Sort sort = createSort(searchCommand, sortColumn, sortDirection);
        searchCommand.setSort(sort);

        Hits hts = _searcher.search(searchCommand);
        if (hts != null) {
            Comparator comparator = SchoolComparatorFactory.createComparator(sortColumn, sortDirection);

            ResultsPager _resultsPager;
            if (comparator != null) {
                // sort the hits using the comparator
                _resultsPager = new ResultsPager(hts, ResultsPager.ResultType.school, comparator);
            } else {
                _resultsPager = new ResultsPager(hts, ResultsPager.ResultType.school);
            }

            Map<String, Object> resultsModel = new HashMap<String, Object>();
            resultsModel.put(MODEL_SCHOOLS_TOTAL, hts.length());
            resultsModel.put(MODEL_SCHOOLS, _resultsPager.getResults(page, pageSize));
            resultsModel.put(MODEL_PAGE_SIZE, pageSize);
            resultsModel.put(MODEL_TOTAL, hts.length());
            resultsModel.put(MODEL_SHOW_ALL, paramShowAll);
            resultsModel.put(PARAM_SORT_COLUMN, sortColumn);
            resultsModel.put(PARAM_SORT_DIRECTION, sortDirection);
            model.put("results", resultsModel);
        } else {
            BadRequestLogger.logBadRequest(_log, request, "Hits object is null for SearchCommand: " + searchCommand);
        }

        return new ModelAndView(getViewName(), model);
    }

    public static boolean isDistrictBrowseRequest(HttpServletRequest request) {
        if (request.getRequestURI() == null) {
            throw new IllegalArgumentException("Request must have request URI");
        }
        return request.getRequestURI().contains("/schools.page") && request.getParameter(PARAM_DISTRICT) != null;
    }

    public static boolean isOldStyleCityBrowseRequest(HttpServletRequest request) {
        if (request.getRequestURI() == null) {
            throw new IllegalArgumentException("Request must have request URI");
        }

        // state is in SessionContext already
        return (!isDistrictBrowseRequest(request) &&
                request.getRequestURI().contains("/schools.page") &&
                request.getParameter(PARAM_CITY) != null);
    }

    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================    

    public static String createNewCityBrowseURI(HttpServletRequest request) {
        // school type(s)
        Set<SchoolType> schoolTypes = new HashSet<SchoolType>();
        final String[] paramSchoolType = request.getParameterValues(PARAM_SCHOOL_TYPE);
        if (paramSchoolType != null) {
            for (String schoolType : paramSchoolType) {
                if (SchoolType.PUBLIC.getSchoolTypeName().equals(schoolType)) {
                    schoolTypes.add(SchoolType.PUBLIC);
                } else if (SchoolType.PRIVATE.getSchoolTypeName().equals(schoolType)) {
                    schoolTypes.add(SchoolType.PRIVATE);
                } else if (SchoolType.CHARTER.getSchoolTypeName().equals(schoolType)) {
                    schoolTypes.add(SchoolType.CHARTER);
                }
            }
        }

        // level code
        LevelCode levelCode = null;
        final String[] paramLevelCode = request.getParameterValues(PARAM_LEVEL_CODE);
        if (paramLevelCode != null) {
            levelCode = LevelCode.createLevelCode(paramLevelCode);
        }

        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY,
            SessionContextUtil.getSessionContext(request).getState(), request.getParameter(PARAM_CITY), schoolTypes, levelCode);
        return urlBuilder.asSiteRelative(request);
    }

    public static String createNewCityBrowseQueryString(HttpServletRequest request) {
        StringBuilder queryString = new StringBuilder();
        String page = request.getParameter(PARAM_PAGE);
        String resultsPerPage = request.getParameter(PARAM_RESULTS_PER_PAGE);
        String showAll = request.getParameter(PARAM_SHOW_ALL);
        String sortColumn = request.getParameter(PARAM_SORT_COLUMN);
        String sortDirection = request.getParameter(PARAM_SORT_DIRECTION);

        if (page != null) {
            queryString.append(PARAM_PAGE + "=").append(page);
        }
        if (resultsPerPage != null) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(PARAM_RESULTS_PER_PAGE + "=").append(resultsPerPage);
        }
        if (showAll != null) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(PARAM_SHOW_ALL + "=").append(showAll);
        }
        if (sortColumn != null) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(PARAM_SORT_COLUMN + "=").append(sortColumn);
        }
        if (sortDirection != null) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(PARAM_SORT_DIRECTION + "=").append(sortDirection);
        }

        return queryString.toString();
    }

    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================

    protected Sort createSort(SearchCommand searchCommand, String sortColumn, String sortDirection) {

        _log.debug("SearchController.createSort");

        if (!searchCommand.isSchoolsOnly()) {
            return null;
        }

        Sort result = null;

        _log.debug("createSort: sortColumn: " + sortColumn);
        _log.debug("createSort: sortDirection: " + sortDirection);

        if (sortColumn == null || sortColumn.equals("schoolResultsHeader")) {
            boolean descending = false;  // default is ascending order
            if (sortDirection != null && sortDirection.equals("desc")) {
                descending = true;
            }
            result = new Sort(new SortField(Indexer.SORTABLE_NAME, SortField.STRING, descending));

            _log.debug("createSort: SortField: " + Indexer.SORTABLE_NAME + ", " + SortField.STRING + ", " + descending);
        }
        // default is School, ascending

        return result;
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
        if (levelCode != null &&
                levelCode.getCommaSeparatedString().length() == 1 &&
                levelCode.containsLevelCode(LevelCode.Level.PRESCHOOL_LEVEL)) {
            sb.append("s and Daycare Centers - GreatSchools");
        } else {
            sb.append(" Schools");
        }
        return sb.toString();
    }

    /**
     * Kept in case any other usages exist. Use other method with State instead.
     */
    public static String calcMetaDesc(String districtDisplayName, String cityDisplayName,
                                      LevelCode levelCode, String[] schoolType) {
        return calcMetaDesc(districtDisplayName, cityDisplayName, null, levelCode, schoolType);
    }

    public static String calcMetaDesc(String districtDisplayName, String cityDisplayName,
                                      State state, LevelCode levelCode, String[] schoolType) {
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
            cityWithModifier.append(cityDisplayName).append(" ").append(modifier);
            // for preschools, do a special SEO meta description
            if (levelCode != null &&
                levelCode.getCommaSeparatedString().length() == 1 &&
                    levelCode.containsLevelCode(LevelCode.Level.PRESCHOOL_LEVEL)) {
                sb.append("Find the best preschools in ").append(cityDisplayName);
                if (state != null) {
                    // pretty sure State can never be null here, but why take a chance?
                    sb.append(", ").append(state.getLongName()).append(" (").
                            append(state.getAbbreviation()).
                            append(")");
                }
                sb.append(" - view preschool ratings, reviews and map locations.");
            } else {
                sb.append("View and map all ").append(cityWithModifier).
                        append("schools. Plus, compare or save ").
                        append(modifier).append("schools.");
            }
        } else {
            sb.append("View and map all ").append(modifier).
                    append("schools in the ").append(districtDisplayName).
                    append(". Plus, compare or save ").append(modifier).
                    append("schools in this district.");
        }

        return sb.toString();
    }

    // required to implement IDirectoryStructureUrlController
    public boolean shouldHandleRequest(DirectoryStructureUrlFields fields) {
        if (fields == null) {
            return false;
        }
        // level code is optional
        return fields.hasState() && fields.hasCityName() && fields.hasSchoolTypes() && fields.hasSchoolsLabel() && !fields.hasSchoolName();
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}
