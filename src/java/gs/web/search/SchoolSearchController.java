package gs.web.search;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.json.JSONObject;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.Address;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


public class SchoolSearchController extends AbstractCommandController implements IDirectoryStructureUrlController {

    private IDistrictDao _districtDao;

    private IGeoDao _geoDao;

    private SchoolSearchService _schoolSearchService;

    private CitySearchService _citySearchService;

    private DistrictSearchService _districtSearchService;

    private StateManager _stateManager;

    public static final String MODEL_SCHOOL_TYPE = "schoolType";
    public static final String MODEL_LEVEL_CODE = "levelCode";
    public static final String MODEL_SORT = "sort";
    public static final String MODEL_SCHOOL_SEARCH_RESULTS = "schoolSearchResults";
    public static final String MODEL_START = "start";
    public static final String MODEL_PAGE_SIZE = "pageSize";
    public static final String MODEL_TOTAL_RESULTS = "totalResults";
    public static final String MODEL_TOTAL_PAGES = "totalPages";
    public static final String MODEL_CURRENT_PAGE = "currentPage";
    public static final String MODEL_USE_PAGING = "usePaging";
    public static final String MODEL_CITY_ID = "cityId";

    // TODO: Omniture tracking
    // TODO: rel="canonical"
    // GS-10036
    // schoolResults.jspx: (SearchController)
    //    <c:if test="${not empty relCanonical}">
    //        <link rel="canonical" href="${relCanonical}"/>
    //    </c:if>
    // SearchController:
    // lines 336-340, 419-426
    // GS-10144, GS-10400
    // schoolsTable.jspx: (SchoolsController)
    // lines 385-414

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException e) throws Exception {

        if (e.hasErrors()) {
            //TODO: handle errors
        }

        SchoolSearchCommand schoolSearchCommand = (SchoolSearchCommand) command;

        State state = getStateManager().getState(schoolSearchCommand.getState());

        Map<FieldConstraint,String> fieldConstraints = getFieldConstraints(schoolSearchCommand, request);

        List<FilterGroup> filterGroups = new ArrayList<FilterGroup>();
        String[] schoolSearchTypes = schoolSearchCommand.getSchoolTypes();
        if (schoolSearchCommand.hasSchoolTypes()) {
            FilterGroup filterGroup = new FilterGroup();
            filterGroup.setFieldFilters(getSchoolTypeFilters(schoolSearchTypes).toArray(new FieldFilter[0]));
            filterGroups.add(filterGroup);
        }
        String[] schoolGradeLevels = schoolSearchCommand.getGradeLevels();
        if (schoolSearchCommand.hasGradeLevels()) {
            FilterGroup filterGroup = new FilterGroup();
            filterGroup.setFieldFilters(getGradeLevelFilters(schoolGradeLevels).toArray(new FieldFilter[0]));
            filterGroups.add(filterGroup);
        }

        FieldSort sort = this.getChosenSort(schoolSearchCommand);

        SearchResultsPage<ISchoolSearchResult> searchResultsPage = getSchoolSearchService().search(
                schoolSearchCommand.getSearchString(),
                fieldConstraints,
                filterGroups,
                sort,
                schoolSearchCommand.getStart(),
                schoolSearchCommand.getPageSize()
        );//TODO: finish paging
        
        //List<ICitySearchResult> citySearchResults = getCitySearchService().search(schoolSearchCommand.getSearchString(), state);
        //List<IDistrictSearchResult> districtSearchResults = getDistrictSearchService().search(schoolSearchCommand.getSearchString(), state);

        PageHelper.setHasSearchedCookie(request, response);

        //TODO: write city and district lists to model
        Map<String,Object> model = new HashMap<String,Object>();
        model.put(MODEL_SCHOOL_TYPE, StringUtils.join(schoolSearchCommand.getSchoolTypes()));
        model.put(MODEL_LEVEL_CODE, StringUtils.join(schoolSearchCommand.getGradeLevels()));
        model.put(MODEL_SORT, schoolSearchCommand.getSortBy());

        addPagingDataToModel(schoolSearchCommand.getStart(), schoolSearchCommand.getPageSize(), searchResultsPage.getTotalResults(), model); //TODO: fix
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        addGamAttributes(request, response, pageHelper, fieldConstraints, filterGroups, schoolSearchCommand.getSearchString(), searchResultsPage.getSearchResults());

        // city id needed for local community module
        SessionContext context = SessionContextUtil.getSessionContext(request);
        if (context.getCityId() != null) {
            model.put(MODEL_CITY_ID, context.getCityId());
        }

        model.put(MODEL_SCHOOL_SEARCH_RESULTS, searchResultsPage.getSearchResults());
        model.put(MODEL_TOTAL_RESULTS, searchResultsPage.getTotalResults());

        if (schoolSearchCommand.isJsonFormat()) {
            response.setContentType("application/json");
            ObjectMapper mapper = new ObjectMapper();
            AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
            // make deserializer use JAXB annotations (only)
            mapper.getDeserializationConfig().setAnnotationIntrospector(introspector);
            // make serializer use JAXB annotations (only)
            mapper.getSerializationConfig().setAnnotationIntrospector(introspector);
            mapper.writeValue(response.getWriter(), model);
            return null;
        } else {
            return new ModelAndView("/search/schoolSearchResults", model);
        }

    }

    /**
     * Calculates paging info and adds it to model. Paging is zero-based. First search result has start=0
     * 
     * @param start
     * @param pageSize
     * @param totalResults
     * @param model
     */
    protected void addPagingDataToModel(int start, Integer pageSize, int totalResults, Map<String,Object> model) {

        //TODO: perform validation to only allow no paging when results are a certain size
        if (pageSize > 0) {
            int currentPage = (int) Math.ceil((start+1) / pageSize.floatValue());
            int numberOfPages = (int) Math.ceil(totalResults / pageSize.floatValue());
            model.put(MODEL_CURRENT_PAGE, currentPage);
            model.put(MODEL_TOTAL_PAGES, numberOfPages);
            model.put(MODEL_USE_PAGING, Boolean.valueOf(true));
        } else {
            model.put(MODEL_USE_PAGING, Boolean.valueOf(false));
        }

        model.put(MODEL_START, start);
        model.put(MODEL_PAGE_SIZE, pageSize);
    }

    protected void addGamAttributes(HttpServletRequest request, HttpServletResponse response, PageHelper pageHelper, Map<FieldConstraint,String> constraints, List<FilterGroup> filterGroups, String searchString, List<ISchoolSearchResult> schoolResults) {
        if (pageHelper == null || constraints == null || filterGroups == null || schoolResults == null) {
            // search string can be null
            throw new IllegalArgumentException("PageHelper, constraints, filters, and school results must not be null");
        }

        Set<FieldFilter> filtersSet = new HashSet<FieldFilter>();
        for (FilterGroup filterGroup : filterGroups) {
            filtersSet.addAll(Arrays.asList(filterGroup.getFieldFilters()));
        }

        // GS-10003 - school type
        for (FieldFilter.SchoolTypeFilter schoolTypeFilter : FieldFilter.SchoolTypeFilter.values()) {
            if (filtersSet.contains(schoolTypeFilter)) {
                pageHelper.addAdKeywordMulti("type", schoolTypeFilter.toString().toLowerCase());
            }
        }

        // GS-6875 - level
        Set<FieldFilter.GradeLevelFilter> gradeLevelFilters = new HashSet<FieldFilter.GradeLevelFilter>();
        for (FieldFilter.GradeLevelFilter gradeLevelFilter : FieldFilter.GradeLevelFilter.values()) {
            if (filtersSet.contains(gradeLevelFilter)) {
                gradeLevelFilters.add(gradeLevelFilter);
            }
        }
        if (gradeLevelFilters.size() == 0) {
            gradeLevelFilters.addAll(Arrays.asList(FieldFilter.GradeLevelFilter.values()));
        }
        for (FieldFilter.GradeLevelFilter gradeLevelFilter : gradeLevelFilters) {
            pageHelper.addAdKeywordMulti("level", String.valueOf(gradeLevelFilter.name().toLowerCase().charAt(0)));
        }

        // GS-10157 - district browse
        State state = null;
        Integer districtId = null;
        try {
            state = State.fromString(constraints.get(FieldConstraint.STATE));
            districtId = Integer.parseInt(constraints.get(FieldConstraint.DISTRICT_ID));
        } catch (IllegalArgumentException e) {
            // nothing to do, invalid state or district ID
        }

        if (state != null && districtId != null) {
            District district = _districtDao.findDistrictById(state, districtId);
            if (district != null) {
                pageHelper.addAdKeyword("district_id", constraints.get(FieldConstraint.DISTRICT_ID));
                pageHelper.addAdKeyword("district_name", district.getName());
            }
        }

        // GS-10448 - search results
        if (schoolResults != null) {
            Set<String> cityNames = new HashSet<String>();
            for (ISchoolSearchResult schoolResult : schoolResults) {
                Address address = schoolResult.getAddress();
                if (address != null) {
                    String cityName = address.getCity();
                    if (StringUtils.isNotBlank(cityName)) {
                        cityNames.add(cityName);
                    }
                }
            }
            for (String cityName : cityNames) {
                pageHelper.addAdKeywordMulti("city", cityName);
            }
        }

        // GS-5786 - city browse, GS-7809 - adsense hints for realtor.com, GS-6971 - city id cookie
        String cityName = constraints.get(FieldConstraint.CITY);
        if (StringUtils.isNotBlank(cityName) && state != null) {
            City city = _geoDao.findCity(state, cityName);
            if (city != null) {
                // GS-5786 - city browse
                cityName = WordUtils.capitalize(city.getName());
                cityName = WordUtils.capitalize(cityName, new char[]{'-'});
                pageHelper.addAdKeywordMulti("city", cityName);

                // GS-7809 - adsense hints for realtor.com
                StringBuilder adSenseHint = new StringBuilder();
                adSenseHint.append(cityName.toLowerCase());
                adSenseHint.append(" ");
                adSenseHint.append(state.getLongName().toLowerCase());
                adSenseHint.append(" real estate house homes for sale");
                pageHelper.addAdSenseHint(adSenseHint.toString());

                // GS-6971 - city id cookie
                PageHelper.setCityIdCookie(request, response, city);
            }
        }

        // GS-10642 - query, GS-9323 zip code
        if (StringUtils.isNotBlank(searchString)) {
            // GS-10642 - query
            // also consider hyphens to be token separators
            String queryString = searchString.replaceAll("-"," ");
            String[] tokens = StringUtils.split(queryString);
            List<String> tokenList = Arrays.asList(tokens);

            Set<String> terms = new HashSet<String>(tokenList);
            for (String term : terms) {
                pageHelper.addAdKeywordMulti("query", term);
            }

            // GS-9323 zip code
            if (searchString.trim().matches("^\\d{5}$")) {
                pageHelper.addAdKeyword("zipcode", searchString.trim());
            }
        }
    }

    protected Map<FieldConstraint,String> getFieldConstraints(SchoolSearchCommand schoolSearchCommand, HttpServletRequest request) {
        Map<FieldConstraint,String> fieldConstraints = new HashMap<FieldConstraint,String>();
        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);

        State state = null;
        if (schoolSearchCommand.getState() != null) {
            try {
                state = State.fromString(schoolSearchCommand.getState());
            } catch (IllegalArgumentException e) {
                // invalid state
            }
        } else if (fields.getState() != null) {
            state = fields.getState();
            fieldConstraints.put(FieldConstraint.STATE, fields.getState().getAbbreviationLowerCase());
        }
        if (state != null) {
            fieldConstraints.put(FieldConstraint.STATE, state.getAbbreviationLowerCase());
        }

        if (fields != null) {
            String city = fields.getCityName();
            if (!StringUtils.isBlank(city)) {
                fieldConstraints.put(FieldConstraint.CITY, city);
            }

            String districtName = fields.getDistrictName();
            if (!StringUtils.isBlank(districtName) && state != null && !StringUtils.isBlank(city)) {
                District district = getDistrictDao().findDistrictByNameAndCity(state, districtName, city);
                if (district != null) {
                    fieldConstraints.put(FieldConstraint.DISTRICT_ID, String.valueOf(district.getId()));
                }
            }
        }

        return fieldConstraints;
    }

    protected FieldSort getChosenSort(SchoolSearchCommand schoolSearchCommand) {
        String sortBy = schoolSearchCommand.getSortBy();
        FieldSort fieldSort = null;
        if (sortBy != null) {
            fieldSort = FieldSort.valueOf(sortBy);
        }
        return fieldSort;
    }

    protected List<FieldFilter> getSchoolTypeFilters(String[] schoolTypeStrings) {
        List<FieldFilter> filters = new ArrayList<FieldFilter>();

        if (schoolTypeStrings != null) {
            for (String schoolTypeString : schoolTypeStrings) {
                SchoolType schoolType = SchoolType.getSchoolType(schoolTypeString);
                if (schoolType != null) {
                    FieldFilter filter = getSchoolTypeFilter(schoolType);
                    if (filter != null) {
                        filters.add(filter);
                    }
                }
            }
        }

        return filters;
    }

    protected List<FieldFilter> getGradeLevelFilters(String[] gradeLevelStrings) {
        List<FieldFilter> filters = new ArrayList<FieldFilter>();

        if (gradeLevelStrings != null) {
            for (String levelCode : gradeLevelStrings) {
                LevelCode.Level level = LevelCode.Level.getLevelCode(levelCode);
                if (level != null) {
                    FieldFilter filter = getGradeLevelFilter(level);
                    if (filter != null) {
                        filters.add(filter);
                    }
                }
            }
        }

        return filters;
    }

    protected FieldFilter getSchoolTypeFilter(SchoolType schoolType) {
        if (schoolType == null) {
            throw new IllegalArgumentException("Cannot get filter for null SchoolType");
        }
        FieldFilter filter = FieldFilter.SchoolTypeFilter.valueOf(StringUtils.upperCase(schoolType.getName()));
        return filter;
    }
    protected FieldFilter getGradeLevelFilter(LevelCode.Level level) {
        if (level == null) {
            throw new IllegalArgumentException("Cannot get filter for null LevelCode");
        }
        FieldFilter filter = FieldFilter.GradeLevelFilter.valueOf(StringUtils.upperCase(level.getLongName()));
        return filter;
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

    public SchoolSearchService getSchoolSearchService() {
        return _schoolSearchService;
    }

    public void setSchoolSearchService(SchoolSearchService schoolSearchService) {
        _schoolSearchService = schoolSearchService;
    }

    public CitySearchService getCitySearchService() {
        return _citySearchService;
    }

    public void setCitySearchService(CitySearchService citySearchService) {
        _citySearchService = citySearchService;
    }

    public DistrictSearchService getDistrictSearchService() {
        return _districtSearchService;
    }

    public void setDistrictSearchService(DistrictSearchService districtSearchService) {
        _districtSearchService = districtSearchService;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public boolean shouldHandleRequest(DirectoryStructureUrlFields fields) {
        boolean schoolsController = false;
        boolean cityController = false;
        boolean districtController = false;
        // level code is optional

        //city browse
        if (fields.hasState() && fields.hasCityName() && fields.hasSchoolsLabel()
                && !fields.hasSchoolName()) {
            schoolsController = true;
        }

        //district browse
        if (fields.hasState() && fields.hasCityName() && fields.hasDistrictName() && fields.hasSchoolsLabel()) {
            schoolsController =  true;
        }
        
        return schoolsController || cityController || districtController;

    }
}
