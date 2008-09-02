/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolsController.java,v 1.62 2008/09/02 03:42:32 thuss Exp $
 */

package gs.web.school;

import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.school.LevelCode;
import gs.data.school.ISchoolDao;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.search.SearchCommand;
import gs.data.search.Searcher;
import gs.data.search.Indexer;
import gs.data.search.SchoolComparatorFactory;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.search.ResultsPager;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.RedirectView301;
import gs.web.util.PageHelper;
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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
        SessionContext context = SessionContextUtil.getSessionContext(request);
        State state = context.getState();
        Map<String, Object> model = new HashMap<String, Object>();

        CityBrowseFields cityBrowseFields = null;

        boolean isDistrictBrowse = SchoolsController.isDistrictBrowseRequest(request);

        if (!isDistrictBrowse) {
            if (SchoolsController.isOldStyleCityBrowseRequest(request)) {
                String uri = SchoolsController.createNewCityBrowseURI(request);
                String queryString = SchoolsController.createNewCityBrowseQueryString(request);
                String redirectUrl = uri + (!StringUtils.isBlank(queryString) ? "?" + queryString : "");
                return new ModelAndView(new RedirectView301(redirectUrl));
            }

            if (!SchoolsController.isRequestURIWithTrailingSlash(request)) {
                String uri = SchoolsController.createURIWithTrailingSlash(request);
                String queryString = request.getQueryString();
                String redirectUrl = uri + (!StringUtils.isBlank(queryString) ? "?" + queryString : "");
                return new ModelAndView(new RedirectView(redirectUrl));
            }

            if (!SchoolsController.isRequestURIWithTrailingSchoolsLabel(request)) {
                String uri = SchoolsController.createURIWithTrailingSchoolsLabel(request);
                String queryString = request.getQueryString();
                String redirectUrl = uri + (!StringUtils.isBlank(queryString) ? "?" + queryString : "");
                return new ModelAndView(new RedirectView(redirectUrl));
            }

            if (!SchoolsController.isValidNewStyleCityBrowseRequest(request)) {
                _log.warn("Malformed city browse url: " + request.getRequestURI());
                model.put("showSearchControl", Boolean.TRUE);
                model.put("title", "City not found");

                return new ModelAndView("status/error", model);
            }

            cityBrowseFields = SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
        }

        LevelCode levelCode = null;
        if (isDistrictBrowse) {
            final String[] paramLevelCode = request.getParameterValues(PARAM_LEVEL_CODE);
            if (paramLevelCode != null) {
                levelCode = LevelCode.createLevelCode(paramLevelCode);
            }
        } else {
            levelCode = cityBrowseFields.getLevelCode();
        }

        if (levelCode != null) {
            model.put(MODEL_LEVEL_CODE, levelCode);
        }

        String[] paramSchoolType;
        if (isDistrictBrowse) {
            paramSchoolType = request.getParameterValues(PARAM_SCHOOL_TYPE);
        } else {
            paramSchoolType = cityBrowseFields.getSchoolType();
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

        String cityName;
        if (isDistrictBrowse) {
            cityName = request.getParameter(PARAM_CITY);
        } else {
            cityName = cityBrowseFields.getCityName();
        }

        boolean cityBrowse = false;
        if (cityName != null) {
            cityBrowse = true;
            model.put(MODEL_CITY_BROWSE_URI_ROOT, createNewCityBrowseURIRoot(state, cityName));
            model.put(MODEL_CITY_BROWSE_URI_LEVEL_LABEL, createNewCityBrowseURILevelLabel(levelCode));
            model.put(MODEL_CITY_BROWSE_URI, createNewCityBrowseURI(state, cityName, cityBrowseFields.getSchoolTypeSet(), levelCode));
            searchCommand.setCity(cityName);
            searchCommand.setQ(cityName);
            model.put(MODEL_ALL_LEVEL_CODES, _schoolDao.getLevelCodeInCity(cityName, state));
        } else {
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
                    _log.warn(state + ": District Id " + districtId + " not found.");
                    BindException errors = new BindException(searchCommand, "searchCommand");
                    errors.reject("error_no_district", "District was not found.");

                    model.put("errors", errors);
                    model.put("showSearchControl", Boolean.TRUE);
                    model.put("title", "District not found");

                    return new ModelAndView("status/error", model);
                }
            }
        }

        // Get the city from us_geo.city
        if (cityName != null) {
            City city = _geoDao.findCity(state, cityName);
            if (city != null) {
                model.put(MODEL_CITY_ID, city.getId());
                PageHelper.setCityIdCookie(request, response, city);
                cityName = WordUtils.capitalize(city.getDisplayName());
                cityName = WordUtils.capitalize(cityName, new char[]{'-'});
                String displayName = cityName;
                if (displayName.equals("New York")) {
                    displayName += " City";
                } else if (State.DC.equals(state) &&
                        displayName.equals("Washington")) {
                    displayName += ", DC";
                }
                model.put(MODEL_CITY_NAME, cityName);
                model.put(MODEL_CITY_DISPLAY_NAME, displayName);
                if (cityBrowse) model.put(MODEL_HEADING1, calcCitySchoolsTitle(displayName, levelCode, paramSchoolType));                    
            }
        }

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
            _log.warn("Hits object is null for SearchCommand: " + searchCommand);
        }

        return new ModelAndView(getViewName(), model);
    }


    public static boolean isDistrictBrowseRequest(HttpServletRequest request) {
        if (request.getRequestURI() == null) {
            throw new IllegalArgumentException("Request must have request URI");
        }
        return request.getRequestURI().contains("/schools.page") && request.getParameter(PARAM_DISTRICT) != null;

    }

    public static boolean isRequestURIWithTrailingSlash(HttpServletRequest request) {
        if (request.getRequestURI() == null) {
            throw new IllegalArgumentException("Request must have request URI");
        }
        return request.getRequestURI().endsWith("/");
    }

    public static boolean isRequestURIWithTrailingSchoolsLabel(HttpServletRequest request) {
        if (request.getRequestURI() == null) {
            throw new IllegalArgumentException("Request must have request URI");
        }
        String uri = request.getRequestURI();
        return uri.endsWith("/" + LEVEL_LABEL_SCHOOLS + "/") ||
                uri.endsWith("/" + LEVEL_LABEL_PRESCHOOLS + "/") ||
                uri.endsWith("/" + LEVEL_LABEL_ELEMENTARY_SCHOOLS + "/") ||
                uri.endsWith("/" + LEVEL_LABEL_MIDDLE_SCHOOLS + "/") ||
                uri.endsWith("/" + LEVEL_LABEL_HIGH_SCHOOLS + "/");
    }

    public static String createURIWithTrailingSlash(HttpServletRequest request) {
        if (request.getRequestURI() == null) {
            throw new IllegalArgumentException("Request must have request URI");
        }
        return
                request.getRequestURI() +
                        (SchoolsController.isRequestURIWithTrailingSlash(request) ? "" : "/");
    }

    public static String createURIWithTrailingSchoolsLabel(HttpServletRequest request) {
        if (request.getRequestURI() == null) {
            throw new IllegalArgumentException("Request must have request URI");
        }
        return
                request.getRequestURI() +
                        (SchoolsController.isRequestURIWithTrailingSlash(request) ? "" : "/") +
                        (SchoolsController.isRequestURIWithTrailingSchoolsLabel(request) ? "" : "schools/");
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

    static class CityBrowseFields {
        private String _cityName = null;
        private LevelCode _levelCode = null;
        private String[] _schoolType = null;
        private Set<SchoolType> _schoolTypeSet = null;

        public String getCityName() {
            return _cityName;
        }

        public void setCityName(String cityName) {
            _cityName = cityName;
        }

        public LevelCode getLevelCode() {
            return _levelCode;
        }

        public void setLevelCode(LevelCode levelCode) {
            _levelCode = levelCode;
        }

        public String[] getSchoolType() {
            return _schoolType;
        }

        public void setSchoolType(String[] schoolType) {
            _schoolType = schoolType;
        }

        public Set<SchoolType> getSchoolTypeSet() {
            return _schoolTypeSet;
        }

        public void setSchoolTypeSet(Set<SchoolType> schoolTypeSet) {
            _schoolTypeSet = schoolTypeSet;
        }
    }

    public static CityBrowseFields getFieldsFromNewStyleCityBrowseRequest(HttpServletRequest request) {
        if (!SchoolsController.isValidNewStyleCityBrowseRequest(request)) {
            throw new IllegalArgumentException("Request uri must be valid new-style city browse request");
        }

        CityBrowseFields fields = new CityBrowseFields();

        Pattern basicStructurePattern = Pattern.compile("/(.*?)/(.*?)/(.*?)/(.*)");
        Matcher basicStructureMatcher = basicStructurePattern.matcher(request.getRequestURI());
        boolean basicStructureMatched = basicStructureMatcher.find();
        boolean filteredBySchoolType = true;
        String city = null;
        String schoolType = null;
        String level = null;

        if (basicStructureMatched) {
            city = basicStructureMatcher.group(2);
            if (basicStructureMatcher.group(4).length() == 0) {
                filteredBySchoolType = false;
                level = basicStructureMatcher.group(3);
            } else {
                schoolType = basicStructureMatcher.group(3);
                level = basicStructureMatcher.group(4).replaceAll("/", "");
            }
        }

        // school type
        String[] paramSchoolType = null;
        Set<SchoolType> schoolTypeSet = new HashSet<SchoolType>();
        if (filteredBySchoolType) {
            List<String> schoolTypes = new ArrayList<String>();
            if (schoolType.contains(SchoolType.PUBLIC.getSchoolTypeName())) {
                schoolTypes.add(SchoolType.PUBLIC.getSchoolTypeName());
                schoolTypeSet.add(SchoolType.PUBLIC);
            }
            if (schoolType.contains(SchoolType.PRIVATE.getSchoolTypeName())) {
                schoolTypes.add(SchoolType.PRIVATE.getSchoolTypeName());
                schoolTypeSet.add(SchoolType.PRIVATE);
            }
            if (schoolType.contains(SchoolType.CHARTER.getSchoolTypeName())) {
                schoolTypes.add(SchoolType.CHARTER.getSchoolTypeName());
                schoolTypeSet.add(SchoolType.CHARTER);
            }

            if (schoolTypes.size() > 0) {
                paramSchoolType = schoolTypes.toArray(new String[schoolTypes.size()]);
            }
        }

        LevelCode levelCode = null;
        if (LEVEL_LABEL_PRESCHOOLS.equals(level)) {
            levelCode = LevelCode.PRESCHOOL;
        } else if (LEVEL_LABEL_ELEMENTARY_SCHOOLS.equals(level)) {
            levelCode = LevelCode.ELEMENTARY;
        } else if (LEVEL_LABEL_MIDDLE_SCHOOLS.equals(level)) {
            levelCode = LevelCode.MIDDLE;
        } else if (LEVEL_LABEL_HIGH_SCHOOLS.equals(level)) {
            levelCode = LevelCode.HIGH;
        }

        fields.setCityName(city.replaceAll("-", " ").replaceAll("_", "-"));
        fields.setSchoolType(paramSchoolType);
        fields.setSchoolTypeSet(schoolTypeSet);
        fields.setLevelCode(levelCode);

        return fields;
    }

    /**
     * This does not validate existence of city or state, but check whether or not the
     * url is structured to contain those fields and those fields are non-blank
     *
     * @param request
     * @return
     */
    public static boolean isValidNewStyleCityBrowseRequest(HttpServletRequest request) {
        if (request.getRequestURI() == null) {
            throw new IllegalArgumentException("Request must have request URI");
        }

        Pattern basicStructurePattern = Pattern.compile("/(.*?)/(.*?)/(.*?)/(.*)");
        Matcher basicStructureMatcher = basicStructurePattern.matcher(request.getRequestURI());
        boolean basicStructureMatched = basicStructureMatcher.find();
        boolean filteredBySchoolType = true;
        String state = null;
        String city = null;
        String schoolType = null;
        String level = null;

        if (basicStructureMatched) {
            state = basicStructureMatcher.group(1);
            city = basicStructureMatcher.group(2);
            if (basicStructureMatcher.group(4).length() == 0) {
                filteredBySchoolType = false;
                level = basicStructureMatcher.group(3);
            } else {
                schoolType = basicStructureMatcher.group(3);
                level = basicStructureMatcher.group(4);
            }
        }

        // empty fields
        if (StringUtils.isBlank(state) || StringUtils.isBlank(city) || StringUtils.isBlank(level)) {
            return false;
        }

        // level
        StringBuilder levelPatternSB = new StringBuilder(
                "(" + LEVEL_LABEL_SCHOOLS + "|" + LEVEL_LABEL_PRESCHOOLS + "|" + LEVEL_LABEL_ELEMENTARY_SCHOOLS +
                        "|" + LEVEL_LABEL_MIDDLE_SCHOOLS + "|" + LEVEL_LABEL_HIGH_SCHOOLS + ")");
        if (filteredBySchoolType) {
            levelPatternSB.append("/");
        }
        Pattern levelPattern = Pattern.compile(levelPatternSB.toString());
        Matcher levelMatcher = levelPattern.matcher(level);
        boolean levelMatched = levelMatcher.find();
        if (!levelMatched) {
            return false;
        }

        // school type
        if (filteredBySchoolType) {
            Pattern schoolTypePattern = Pattern.compile("(public|private|charter|public-private|public-charter|private-charter)");
            Matcher schoolTypeMatcher = schoolTypePattern.matcher(schoolType);
            return schoolTypeMatcher.find();
        }

        return true;
    }

    public static String createNewCityBrowseURIRoot(State state, String cityName) {
        if (state == null || StringUtils.isBlank(cityName)) {
            throw new IllegalArgumentException("Must specify state and city");
        }
        String stateNameForUrl = state.getLongName().toLowerCase().replaceAll(" ", "-");
        String cityNameForUrl = cityName.toLowerCase().replaceAll("-", "_").replaceAll(" ", "-");

        return "/" + stateNameForUrl + "/" + cityNameForUrl + "/";
    }

    public static String createNewCityBrowseURISchoolTypeLabel(Set<SchoolType> schoolTypes) {
        if (schoolTypes == null || (schoolTypes.size() > 3)) {
            throw new IllegalArgumentException("Must specify a set of no more than 3 school types");
        }

        StringBuilder label = new StringBuilder();

        SchoolType firstType = null;
        SchoolType secondType = null;
        if (schoolTypes.size() == 1) {
            firstType = schoolTypes.toArray(new SchoolType[schoolTypes.size()])[0];
        } else if (schoolTypes.size() == 2) {
            if (schoolTypes.contains(SchoolType.PUBLIC)) {
                firstType = SchoolType.PUBLIC;
            } else if (schoolTypes.contains(SchoolType.PRIVATE)) {
                firstType = SchoolType.PRIVATE;
            } else if (schoolTypes.contains(SchoolType.CHARTER)) {
                firstType = SchoolType.CHARTER;
            }

            Set otherSchoolTypes = new HashSet<SchoolType>(schoolTypes);
            otherSchoolTypes.remove(firstType);

            if (otherSchoolTypes.contains(SchoolType.PUBLIC)) {
                // this should never happen
                secondType = SchoolType.PUBLIC;
            } else if (otherSchoolTypes.contains(SchoolType.PRIVATE)) {
                secondType = SchoolType.PRIVATE;
            } else if (otherSchoolTypes.contains(SchoolType.CHARTER)) {
                secondType = SchoolType.CHARTER;
            }
        }

        if (firstType != null) {
            label.append(firstType.getSchoolTypeName());
        }
        if (secondType != null) {
            label.append("-");
            label.append(secondType.getSchoolTypeName());
        }

        return label.toString();
    }

    public static String createNewCityBrowseURI(State state, String cityName, Set<SchoolType> schoolTypes, LevelCode levelCode) {
        if (state == null || StringUtils.isBlank(cityName) || (schoolTypes != null && schoolTypes.size() > 3)) {
            throw new IllegalArgumentException("Must specify state, city, level code, and a set of no more than 3 school types");
        }

        StringBuilder url = new StringBuilder(createNewCityBrowseURIRoot(state, cityName));

        String schoolTypeLabel = createNewCityBrowseURISchoolTypeLabel(schoolTypes);
        if (!StringUtils.isBlank(schoolTypeLabel)) {
            url.append(schoolTypeLabel);
            url.append("/");
        }

        url.append(createNewCityBrowseURILevelLabel(levelCode));
        url.append("/");

        return url.toString();
    }

    public static String createNewCityBrowseURILevelLabel(LevelCode levelCode) {
        if (LevelCode.PRESCHOOL.equals(levelCode)) {
            return LEVEL_LABEL_PRESCHOOLS;
        } else if (LevelCode.ELEMENTARY.equals(levelCode)) {
            return LEVEL_LABEL_ELEMENTARY_SCHOOLS;
        } else if (LevelCode.MIDDLE.equals(levelCode)) {
            return LEVEL_LABEL_MIDDLE_SCHOOLS;
        } else if (LevelCode.HIGH.equals(levelCode)) {
            return LEVEL_LABEL_HIGH_SCHOOLS;
        } else {
            // all others not supported
            return LEVEL_LABEL_SCHOOLS;
        }
    }

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

        return createNewCityBrowseURI(SessionContextUtil.getSessionContext(request).getState(),
                request.getParameter(PARAM_CITY), schoolTypes, levelCode);
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
            sb.append("s");
        } else {
            sb.append(" Schools");
        }
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
            cityWithModifier.append(cityDisplayName).append(" ").append(modifier);
            sb.append("View and map all ").append(cityWithModifier).
                    append("schools. Plus, compare or save ").
                    append(modifier).append("schools.");
        } else {
            sb.append("View and map all ").append(modifier).
                    append("schools in the ").append(districtDisplayName).
                    append(". Plus, compare or save ").append(modifier).
                    append("schools in this district.");
        }

        return sb.toString();
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
