package gs.web.search;

import gs.data.community.FavoriteSchool;
import gs.data.community.User;
import gs.data.community.local.ILocalBoardDao;
import gs.data.community.local.LocalBoard;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.seo.SeoUtil;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.Address;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


public class SchoolSearchController extends AbstractCommandController implements IDirectoryStructureUrlController {

    private IDistrictDao _districtDao;

    private IGeoDao _geoDao;

    private SchoolSearchService _schoolSearchService;

    private SchoolSearchService _looseSchoolSearchService;

    private CitySearchService _citySearchService;

    private DistrictSearchService _districtSearchService;

    private ILocalBoardDao _localBoardDao;

    private StateManager _stateManager;

    private static final Logger _log = Logger.getLogger(SchoolSearchController.class);
    public static final String BEAN_ID = "/search/search.page";

    public static final String MODEL_SCHOOL_TYPE = "schoolType";
    public static final String MODEL_LEVEL_CODE = "levelCode";
    public static final String MODEL_SORT = "sort";
    public static final String MODEL_SEARCH_STRING = "searchString";

    public static final String MODEL_SCHOOL_SEARCH_RESULTS = "schoolSearchResults";
    public static final String MODEL_CITY_SEARCH_RESULTS = "citySearchResults";
    public static final String MODEL_DISTRICT_SEARCH_RESULTS = "districtSearchResults";

    public static final String MODEL_START = "start";
    public static final String MODEL_PAGE_SIZE = "pageSize";
    public static final String MODEL_TOTAL_RESULTS = "totalResults";
    public static final String MODEL_TOTAL_PAGES = "totalPages";
    public static final String MODEL_CURRENT_PAGE = "currentPage";
    public static final String MODEL_USE_PAGING = "usePaging";

    public static final String MODEL_CITY_ID = "cityId";
    public static final String MODEL_MSL_SCHOOLS = "mslSchools";

    public static final String MODEL_LOCAL_BOARD_ID = "localBoardId";
    public static final String MODEL_LOCAL_CITY_NAME = "localCityName";

    public static final String MODEL_CITY = "city";
    public static final String MODEL_DISTRICT = "district";

    public static final String MODEL_TITLE = "title";
    public static final String MODEL_META_DESCRIPTION = "metaDescription";
    public static final String MODEL_META_KEYWORDS = "metaKeywords";

    public static final String MODEL_REL_CANONICAL = "relCanonical";

    public static final String MODEL_OMNITURE_PAGE_NAME = "omniturePageName";
    public static final String MODEL_OMNITURE_HIERARCHY = "omnitureHierarchy";
    public static final String MODEL_OMNITURE_QUERY = "omnitureQuery";
    public static final String MODEL_OMNITURE_SCHOOL_TYPE = "omnitureSchoolType";
    public static final String MODEL_OMNITURE_SCHOOL_LEVEL = "omnitureSchoolLevel";
    public static final String MODEL_OMNITURE_SORT_SELECTION = "omnitureSortSelection";
    public static final String MODEL_OMNITURE_RESULTS_PER_PAGE = "omnitureResultsPerPage";

    public static final String MODEL_IS_CITY_BROWSE = "isCityBrowse";
    public static final String MODEL_IS_DISTRICT_BROWSE = "isDistrictBrowse";
    public static final String MODEL_IS_SEARCH = "isSearch";

    public static final String MODEL_IS_NEARBY_SEARCH = "isNearbySearch";
    public static final String MODEL_NEARBY_SEARCH_TITLE_PREFIX = "nearbySearchTitlePrefix";
    public static final String MODEL_NEARBY_SEARCH_ZIP_CODE = "nearbySearchZipCode";

    public static final String MODEL_STATE = "state";

    public static final String MODEL_DID_YOU_MEAN = "didYouMean";

    public static final int MAX_PAGE_SIZE = 100;

    protected static final String VIEW_NOT_FOUND = "/status/error404";

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException e) throws Exception {

        SchoolSearchCommand schoolSearchCommand = (SchoolSearchCommand) command;

        FilterFactory filterFactory = new FilterFactory();

        boolean foundDidYouMeanSuggestions = false;

        if (e.hasErrors()) {
            handleErrors(e, schoolSearchCommand);
        }

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("schoolSearchCommand", schoolSearchCommand);
        List<FilterGroup> filterGroups = new ArrayList<FilterGroup>();
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);

        if (user != null) {
            Set<FavoriteSchool> mslSchools = user.getFavoriteSchools();
            model.put(MODEL_MSL_SCHOOLS, mslSchools);
        }

        SchoolSearchCommandWithFields commandAndFields = new SchoolSearchCommandWithFields(schoolSearchCommand, fields);
        String[] schoolSearchTypes = commandAndFields.getSchoolTypes();
        commandAndFields.setDistrictDao(getDistrictDao());
        commandAndFields.setGeoDao(getGeoDao());
        State state = commandAndFields.getState();
        boolean isCityBrowse = commandAndFields.isCityBrowse();
        boolean isDistrictBrowse = commandAndFields.isDistrictBrowse();
        boolean isSearch = !isCityBrowse && !isDistrictBrowse;

        Map nearbySearchInfo = null;
        if (schoolSearchCommand.isNearbySearch()) {
            nearbySearchInfo = (Map)request.getAttribute("nearbySearchInfo");
            if (nearbySearchInfo != null && nearbySearchInfo.get("state") != null && nearbySearchInfo.get("state") instanceof State) {
                state = (State)nearbySearchInfo.get("state");
                if (state != null) {
                    sessionContext.getSessionContextUtil().updateState(sessionContext, request, response, state);
                }
            }
        }

        City city = null;
        District district = null;

        if (isCityBrowse || isDistrictBrowse) {
            city = commandAndFields.getCityFromUrl();
            model.put(MODEL_CITY, city);
            if (city == null) {
                return redirectTo404(response);
            }
        }
        if (isDistrictBrowse) {
            district = commandAndFields.getDistrict();
            model.put(MODEL_DISTRICT, district);
            if (district == null) {
                return redirectTo404(response);
            }
        }
        model.put(MODEL_IS_CITY_BROWSE, isCityBrowse);
        model.put(MODEL_IS_DISTRICT_BROWSE, isDistrictBrowse);
        model.put(MODEL_IS_SEARCH, isSearch);
        model.put(MODEL_IS_NEARBY_SEARCH, schoolSearchCommand.isNearbySearch());

        if (schoolSearchCommand.isNearbySearch()) {
            String nearbySearchTitlePrefix = "Schools";
            if (schoolSearchCommand.hasGradeLevels()) {
                String[] gradeLevels = schoolSearchCommand.getGradeLevels();
                if (gradeLevels.length == 1) {
                    LevelCode levelCode = LevelCode.createLevelCode(gradeLevels[0]);
                    if (levelCode != null) {
                        nearbySearchTitlePrefix = levelCode.getLowestLevel().getLongName() + " schools";
                    }
                }
            }
            model.put(MODEL_NEARBY_SEARCH_TITLE_PREFIX, nearbySearchTitlePrefix);
        }

        //if user did not enter search term (and this is not a nearby search), redirect to state browse
        if (!schoolSearchCommand.isNearbySearch() && isSearch && StringUtils.isBlank(schoolSearchCommand.getSearchString())) {
            return stateBrowseRedirect(request, sessionContext);
        }

        // if district browse *and* lc parameter was specified, 301-redirect to use directory-structure schools label instead of lc parameter
        String lc = request.getParameter("lc");
        if (commandAndFields.isDistrictBrowse() && StringUtils.isNotBlank(lc) && !schoolSearchCommand.isAjaxRequest()) {
            LevelCode levelCode = LevelCode.createLevelCode(lc);
            UrlBuilder urlBuilder = new UrlBuilder(district, levelCode, UrlBuilder.SCHOOLS_IN_DISTRICT);
            return new ModelAndView(new RedirectView301(urlBuilder.asSiteRelative(request)));
        }

        Map<FieldConstraint,String> fieldConstraints = getFieldConstraints(state, city, district);

        //If we have school types, create a filter group for it
        if (commandAndFields.hasSchoolTypes()) {
            FilterGroup filterGroup = filterFactory.createFilterGroup(FieldFilter.SchoolTypeFilter.class,commandAndFields.getSchoolTypes());
            filterGroups.add(filterGroup);
        }

        //If we have level code(s), create a filter group for it
        LevelCode levelCode = commandAndFields.getLevelCode();
        if (levelCode != null) {
            FilterGroup filterGroup = new FilterGroup();
            FieldFilter[] filters = getGradeLevelFilters(levelCode).toArray(new FieldFilter[0]);
            if (filters != null && filters.length > 0) {
                filterGroup.setFieldFilters(filters);
                filterGroups.add(filterGroup);
            }
        }

        //Create a filter group for the Affiliation filters (currently Religious or Nonsectarian)
        if (commandAndFields.hasAffiliations()) {
            FilterGroup affiliationGroup = filterFactory.createFilterGroup(FieldFilter.AffiliationFilter.class, commandAndFields.getAffiliations());
            filterGroups.add(affiliationGroup);
        }

        if (!StringUtils.isEmpty(commandAndFields.getStudentTeacherRatio())) {
            FilterGroup studentTeacherRatioGroup = filterFactory.createFilterGroup(FieldFilter.StudentTeacherRatio.class, new String[] {commandAndFields.getStudentTeacherRatio()});
            filterGroups.add(studentTeacherRatioGroup);
        }

        if (!StringUtils.isEmpty(commandAndFields.getSchoolSize())) {
            FilterGroup schoolSizeGroup = filterFactory.createFilterGroup(FieldFilter.SchoolSize.class, new String[] {commandAndFields.getSchoolSize()});
            filterGroups.add(schoolSizeGroup);
        }

        FieldSort sort = schoolSearchCommand.getSortBy() == null ? ((isCityBrowse || isDistrictBrowse) && !"true".equals(request.getParameter("sortChanged")) ? FieldSort.GS_RATING_DESCENDING : null) : FieldSort.valueOf(schoolSearchCommand.getSortBy());
        if (sort != null) {
            schoolSearchCommand.setSortBy(sort.name());
        } else {
            schoolSearchCommand.setSortBy(null);
        }
        model.put(MODEL_SORT, schoolSearchCommand.getSortBy());

        SearchResultsPage<ISchoolSearchResult> searchResultsPage = new SearchResultsPage(0, new ArrayList<ISchoolSearchResult>());
        if (!schoolSearchCommand.isAjaxRequest() || (schoolSearchCommand.hasSchoolTypes() && schoolSearchCommand.hasGradeLevels())) {
            try {
                SchoolSearchService service = getSchoolSearchService();
                if (schoolSearchCommand.getSearchType() == SchoolSearchType.LOOSE) {
                    service = getLooseSchoolSearchService();
                }

                searchResultsPage = service.search(
                        schoolSearchCommand.getSearchString(),
                        fieldConstraints,
                        filterGroups,
                        sort,
                        schoolSearchCommand.getLat(),
                        schoolSearchCommand.getLon(),
                        schoolSearchCommand.getDistanceAsFloat(),
                        schoolSearchCommand.getStart(),
                        schoolSearchCommand.getPageSize()
                );

                if (searchResultsPage.getTotalResults() == 0 && searchResultsPage.getSpellCheckResponse() != null &&
                    !schoolSearchCommand.isNearbySearch()) {
                    String didYouMean = getSearchSuggestion(schoolSearchCommand.getSearchString(), searchResultsPage.getSpellCheckResponse());

                    if (didYouMean != null) {
                        SearchResultsPage<ISchoolSearchResult> didYouMeanResultsPage = service.search(
                                didYouMean,
                                fieldConstraints,
                                filterGroups,
                                sort,
                                schoolSearchCommand.getLat(),
                                schoolSearchCommand.getLon(),
                                schoolSearchCommand.getDistanceAsFloat(),
                                schoolSearchCommand.getStart(),
                                schoolSearchCommand.getPageSize()
                        );

                        if(didYouMeanResultsPage != null && didYouMeanResultsPage.getTotalResults() > 0) {
                            model.put(MODEL_DID_YOU_MEAN, didYouMean);
                        }
                    }
                }
            } catch (SearchException ex) {
                _log.debug("something when wrong when attempting to use SchoolSearchService. Eating exception", e);
            }
        }

        //update command's start value once we know how many results there are, since command generates page #
        if (schoolSearchCommand.getStart() >= searchResultsPage.getTotalResults()) {
            schoolSearchCommand.setStart(0);
        }

        List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();
        List<IDistrictSearchResult> districtSearchResults = new ArrayList<IDistrictSearchResult>();
        if (schoolSearchCommand.getSearchString() != null) {
            try {
                Map<IFieldConstraint,String> cityConstraints = new HashMap<IFieldConstraint,String>();
                cityConstraints.put(CitySearchFieldConstraints.STATE, state.getAbbreviationLowerCase());
                SearchResultsPage<ICitySearchResult> cityPage  = getCitySearchService().search(schoolSearchCommand.getSearchString(), cityConstraints, null, null, 0, 33);
                citySearchResults = cityPage.getSearchResults();
            } catch (SearchException ex) {
                _log.debug("something when wrong when attempting to use CitySearchService. Eating exception", e);
            }
            model.put(MODEL_CITY_SEARCH_RESULTS, citySearchResults);

            try {
                Map<IFieldConstraint,String> districtConstraints = new HashMap<IFieldConstraint,String>();
                districtConstraints.put(DistrictSearchFieldConstraints.STATE, state.getAbbreviationLowerCase());
                SearchResultsPage<IDistrictSearchResult> districtPage  = getDistrictSearchService().search(schoolSearchCommand.getSearchString(), districtConstraints, null, null, 0, 11);
                districtSearchResults = districtPage.getSearchResults();
            } catch (SearchException ex) {
                _log.debug("something when wrong when attempting to use DistrictSearchService. Eating exception", e);
            }
            model.put(MODEL_DISTRICT_SEARCH_RESULTS, districtSearchResults);
        }

        PageHelper.setHasSearchedCookie(request, response);

        model.put(MODEL_SCHOOL_TYPE, StringUtils.join(commandAndFields.getSchoolTypes()));
        if (levelCode != null) {
            model.put(MODEL_LEVEL_CODE, levelCode.getCommaSeparatedString());
        }

        addPagingDataToModel(schoolSearchCommand.getStart(), schoolSearchCommand.getPageSize(), schoolSearchCommand.getCurrentPage(), searchResultsPage.getTotalResults(), model); //TODO: fix
        addGamAttributes(request, response, pageHelper, fieldConstraints, filterGroups, schoolSearchCommand.getSearchString(), searchResultsPage.getSearchResults(), city, district);

        City localCity = (city != null ? city : commandAndFields.getCityFromSearchString());
        if (localCity != null) {
            LocalBoard localBoard = _localBoardDao.findByCityId(localCity.getId());
            if (localBoard != null) {
                model.put(MODEL_LOCAL_BOARD_ID, localBoard.getBoardId());
                model.put(MODEL_LOCAL_CITY_NAME, localCity.getName());
            }
        }

        model.put(MODEL_SEARCH_STRING, schoolSearchCommand.getSearchString());
        model.put(MODEL_SCHOOL_SEARCH_RESULTS, searchResultsPage.getSearchResults());
        model.put(MODEL_TOTAL_RESULTS, searchResultsPage.getTotalResults());
        model.put(MODEL_REL_CANONICAL,  getRelCanonical(request, state, citySearchResults, city, district,
                                     filterGroups, levelCode, schoolSearchCommand.getSearchString()));

        if (commandAndFields.isCityBrowse()) {
            model.putAll(new CityMetaDataHelper().getMetaData(commandAndFields));
        } else if (commandAndFields.isDistrictBrowse()) {
            model.putAll(new DistrictMetaDataHelper().getMetaData(commandAndFields));
        } else if (schoolSearchCommand.isNearbySearch()) {
            model.putAll(new NearbyMetaDataHelper().getMetaData(nearbySearchInfo));
        } else {
            model.putAll(new MetaDataHelper().getMetaData(commandAndFields));
        }

        model.put(MODEL_STATE, state);

        if (schoolSearchCommand.isNearbySearch()) {
            model.put(MODEL_NEARBY_SEARCH_ZIP_CODE, nearbySearchInfo.get("zipCode"));
        }

        model.put(MODEL_OMNITURE_PAGE_NAME,
                getOmniturePageName(request, schoolSearchCommand.getCurrentPage(), searchResultsPage.getTotalResults(),
                        isCityBrowse, isDistrictBrowse,
                        citySearchResults, districtSearchResults, foundDidYouMeanSuggestions)
        );
        model.put(MODEL_OMNITURE_HIERARCHY,
                getOmnitureHierarchy(schoolSearchCommand.getCurrentPage(), searchResultsPage.getTotalResults(),
                        isCityBrowse, isDistrictBrowse,
                        citySearchResults, districtSearchResults)
        );

        String omnitureQuery = isSearch? getOmnitureQuery(schoolSearchCommand.getSearchString()) : null;
        model.put(MODEL_OMNITURE_QUERY, omnitureQuery);
        model.put(MODEL_OMNITURE_SCHOOL_TYPE, getOmnitureSchoolType(schoolSearchTypes));
        model.put(MODEL_OMNITURE_SCHOOL_LEVEL, getOmnitureSchoolLevel(levelCode));
        model.put(MODEL_OMNITURE_SORT_SELECTION, getOmnitureSortSelection(sort));
        model.put(MODEL_OMNITURE_RESULTS_PER_PAGE, getOmnitureResultsPerPage(schoolSearchCommand.getPageSize(), searchResultsPage.getTotalResults()));

        if (schoolSearchCommand.isAjaxRequest()) {
            if (searchResultsPage.getTotalResults() == 0) {
                return new ModelAndView("/search/schoolSearchNoResultsTable", model);
            } else {
                return new ModelAndView("/search/schoolSearchResultsTable", model);
            }
        } else {
            if (searchResultsPage.getTotalResults() == 0) {
                return new ModelAndView("/search/schoolSearchNoResults", model);
            } else {
                return new ModelAndView("/search/schoolSearchResults", model);
            }
        }
    }

    public String getSearchSuggestion(String searchString, SpellCheckResponse spellCheckResponse) {
        String suggestedSearch = searchString;

        Map<String,SpellCheckResponse.Suggestion> suggestionMap = spellCheckResponse.getSuggestionMap();

        String[] tokens = StringUtils.splitPreserveAllTokens(suggestedSearch);

        if (tokens != null) {
            for (int i = 0; i < tokens.length; i++) {
                if (suggestionMap.containsKey(tokens[i])) {
                    tokens[i] = suggestionMap.get(tokens[i]).getAlternatives().get(0);
                } else if (suggestionMap.containsKey(StringUtils.lowerCase(tokens[i]))) {
                    tokens[i] = suggestionMap.get(StringUtils.lowerCase(tokens[i])).getAlternatives().get(0);
                }
            }
        }

        String result = StringUtils.join(tokens, ' ');

        if (searchString.equalsIgnoreCase(result)) {
            result = null;
        }

        return result;
    }

    protected class MetaDataHelper {
        public Map<String,Object> getMetaData(SchoolSearchCommandWithFields commandWithFields) {
            String searchString = commandWithFields.getSearchString();
            Map<String,Object> model = new HashMap<String,Object>();
            model.put(MODEL_TITLE, getTitle(searchString));
            return model;
        }
    }

    protected class NearbyMetaDataHelper {
        public Map<String,Object> getMetaData(Map nearbySearchInfo) {
            Map<String,Object> model = new HashMap<String,Object>();

            if (nearbySearchInfo != null) {
                Object zipCodeObj = nearbySearchInfo.get("zipCode");
                if (zipCodeObj != null && zipCodeObj instanceof String) {
                    model.put(MODEL_TITLE, getTitle((String) zipCodeObj));
                }
            }

            return model;
        }
    }

    protected class DistrictMetaDataHelper {
        public Map<String,Object> getMetaData(SchoolSearchCommandWithFields commandWithFields) {
            District district = commandWithFields.getDistrict();
            String[] schoolSearchTypes = commandWithFields.getSchoolTypes();
            LevelCode levelCode = commandWithFields.getLevelCode();
            Map<String,Object> model = new HashMap<String,Object>();
            model.put(MODEL_TITLE, getTitle(district, levelCode, schoolSearchTypes));
            model.put(MODEL_META_DESCRIPTION, getMetaDescription(district));
            model.put(MODEL_META_KEYWORDS, getMetaKeywords(district));
            return model;
        }
    }

    protected class CityMetaDataHelper {
        public Map<String,Object> getMetaData(SchoolSearchCommandWithFields commandWithFields) {
            City city = commandWithFields.getCityFromUrl();
            String[] schoolSearchTypes = commandWithFields.getSchoolTypes();
            LevelCode levelCode = commandWithFields.getLevelCode();

            Map<String,Object> model = new HashMap<String,Object>();
            model.put(MODEL_TITLE, getTitle(city.getDisplayName(), city.getState(), levelCode, schoolSearchTypes));
            model.put(MODEL_META_DESCRIPTION, getMetaDescription(city, levelCode, schoolSearchTypes));
            return model;
        }
    }

    protected State getState(HttpServletRequest request, SchoolSearchCommand schoolSearchCommand, DirectoryStructureUrlFields fields) {
        State state;
        if (schoolSearchCommand.getState() != null) {
            try {
                state = State.fromString(schoolSearchCommand.getState());
            } catch (IllegalArgumentException iae) {
                // invalid state, use default
                state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
            }
        } else if (fields != null && fields.getState() != null) {
            state = fields.getState();
        } else {
            state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        }
        return state;
    }

    private ModelAndView stateBrowseRedirect(HttpServletRequest request, SessionContext sessionContext) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.RESEARCH, sessionContext.getState());
        final String url = builder.asSiteRelative(request);
        final RedirectView view = new RedirectView(url, false);
        return new ModelAndView(view);
    }

    protected void handleErrors(BindException e, SchoolSearchCommand schoolSearchCommand) {
        if (e.hasFieldErrors("pageSize")) {
            schoolSearchCommand.setPageSize(SchoolSearchCommand.DEFAULT_PAGE_SIZE);
        }

        if (e.hasFieldErrors("start")) {
            schoolSearchCommand.setStart(0);
        }
    }

    public ModelAndView redirectTo404(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return new ModelAndView("redirect:" + VIEW_NOT_FOUND);
    }

    class InvalidDistrictException extends Exception {
        public InvalidDistrictException(String message) {
            super(message);
        }
    }
    class InvalidCityException extends Exception {
        public InvalidCityException(String message) {
            super(message);
        }
    }

    //-------------------------------------------------------------------------
    // omniture
    //-------------------------------------------------------------------------

    protected static String getOmniturePageName(HttpServletRequest request, int currentPage, int totalResults,
                                                boolean isCityBrowse, boolean isDistrictBrowse,
                                                List<ICitySearchResult> citySearchResults, List<IDistrictSearchResult> districtSearchResults, boolean foundDidYouMeanSuggestions) {
        String pageName = "";

        String paramMap = request.getParameter("map");
        boolean hasCityResults = (citySearchResults != null && citySearchResults.size() > 0);
        boolean hasDistrictResults = (districtSearchResults != null && districtSearchResults.size() > 0);

        if (isCityBrowse) {
            pageName = "schools:city:" + currentPage + ("1".equals(paramMap) ? ":map" : "");
        } else if (isDistrictBrowse) {
            pageName = "schools:district:" + currentPage + ("1".equals(paramMap) ? ":map" : "");
        } else if (totalResults > 0) {
            pageName = "School Search:Page" + currentPage;
        } else {
            String pageNamePartTwo = null;
            if (hasCityResults) {
                if (hasDistrictResults) {
                    pageNamePartTwo = "City and District only";
                } else {
                    pageNamePartTwo = "City only";
                }
            } else {
                if (hasDistrictResults) {
                    pageNamePartTwo = "District Only";
                } else {
                    if (foundDidYouMeanSuggestions) {
                        pageNamePartTwo = "noresults_Didyoumean";
                    } else {
                        pageNamePartTwo = "noresults";
                    }
                }
            }
            pageName = "School Search:" + pageNamePartTwo;
        }
        return pageName;
    }

    protected static String getOmnitureHierarchy(int currentPage, int totalResults,
                                                boolean isCityBrowse, boolean isDistrictBrowse,
                                                List<ICitySearchResult> citySearchResults, List<IDistrictSearchResult> districtSearchResults) {
        String hierarchy = "";

        boolean hasCityResults = (citySearchResults != null && citySearchResults.size() > 0);
        boolean hasDistrictResults = (districtSearchResults != null && districtSearchResults.size() > 0);

        if (isCityBrowse) {
            hierarchy = "Search,Schools,City," + (totalResults > 0 ? currentPage : "noresults");
        } else if (isDistrictBrowse) {
            hierarchy = "Search,Schools,District," + (totalResults > 0 ? currentPage : "noresults");
        } else if (totalResults > 0) {
            hierarchy = "Search,School Search," + currentPage;
        } else {
            String hierarchyPartTwo;
            if (hasCityResults) {
                if (hasDistrictResults) {
                    hierarchyPartTwo = "City and District only";
                } else {
                    hierarchyPartTwo = "City only";
                }
            } else {
                if (hasDistrictResults) {
                    hierarchyPartTwo = "District Only";
                } else {
                    hierarchyPartTwo = "Pagenoresults";
                }
            }
            hierarchy = "Search,School Search," + hierarchyPartTwo;
        }
        return hierarchy;
    }

    protected static String getOmnitureQuery(String searchString) {
        if (StringUtils.isBlank(searchString)) {
            return "[blank]";
        } else {
            return StringEscapeUtils.escapeXml(searchString.toLowerCase());
        }
    }

    // this presumes all schoolSearchTypes passed in are valid SchoolTypes.
    protected static String getOmnitureSchoolType(String[] schoolSearchTypes) {
        // currently, there's no url that will take you to a page with all school type filters unchecked,
        // for which we should be logging "nothing checked" in Omniture;
        // that's why the code here doesn't ever return it. it can be implemented if we ever add such a url
        if (schoolSearchTypes == null || schoolSearchTypes.length == 3) {
            return null;
        } else {
            return StringUtils.join(schoolSearchTypes, ",");
        }
    }

    protected static String getOmnitureSchoolLevel(LevelCode levelCode) {
        // currently, there's no url that will take you to a page with all level code filters unchecked,
        // for which we should be logging "nothing checked" in Omniture;
        // that's why the code here doesn't ever return it. it can be implemented if we ever add such a url
        if (levelCode != null && !levelCode.equals(LevelCode.ALL_LEVELS)) {
            return levelCode.getCommaSeparatedString();
        }
        return null;
    }

    protected static String getOmnitureSortSelection(FieldSort sort) {
        if (sort == null) {
            return null;
        } else {
            if (sort.name().startsWith("SCHOOL_NAME")) {
                return "School name";
            } else if (sort.name().startsWith("GS_RATING")) {
                return "GS Rating";
            } else if (sort.name().startsWith("PARENT_RATING")) {
                return "Parent Rating";
            } else if (sort.name().startsWith("DISTANCE")) {
                return "Distance";
            } else {
                return null;
            }
        }
    }

    protected static String getOmnitureResultsPerPage(int pageSize, int totalResults) {
        String resultsPerPage = "";
        // ignore pageSize = 25 per GS-11563
        switch (pageSize) {
            case 50:
                resultsPerPage = "50";
                break;
            case 100:
                if (totalResults <= 100) {
                    resultsPerPage = "All";
                } else {
                    resultsPerPage = "100";
                }
                break;
        }
        return resultsPerPage;
    }

    //-------------------------------------------------------------------------
    // title, meta description, meta keywords
    //-------------------------------------------------------------------------

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

    public static String getTitle(String cityDisplayName, State cityState, LevelCode levelCode, String[] schoolType) {
        StringBuffer sb = new StringBuffer();
        sb.append(cityDisplayName);
        if (schoolType != null && (schoolType.length == 1 || schoolType.length == 2)) {
            for (int x=0; x < schoolType.length; x++) {
                if (x == 1) {
                    sb.append(" and");
                }
                if ("private".equals(schoolType[x])) {
                    sb.append(" Private");
                } else if ("charter".equals(schoolType[x])) {
                    sb.append(" Public Charter");
                } else {
                    sb.append(" Public");
                }
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
            sb.append("s and Daycare Centers");
        } else {
            sb.append(" Schools");
        }
        sb.append(" - ").append(cityDisplayName).append(", ").append(cityState.getAbbreviation());
        sb.append(" | GreatSchools");
        return sb.toString();
    }

    protected static String getTitle(boolean isCityBrowse, boolean isDistrictBrowse, City city, District district, LevelCode levelCode, String[] schoolTypes, String searchString) {
        if (isCityBrowse) {
            return getTitle(city.getDisplayName(), city.getState(), levelCode, schoolTypes);
        } else if (isDistrictBrowse) {
            return getTitle(district, levelCode, schoolTypes);
        } else {
            return getTitle(searchString);
        }
    }
    protected static String getTitle(District district, LevelCode levelCode, String[] schoolTypes) {
        return SeoUtil.generatePageTitle(district, levelCode, schoolTypes);
    }
    protected static String getTitle(String searchString) {
        String title;
        if (StringUtils.isNotBlank(searchString)) {
            title = "GreatSchools.org Search: " + StringEscapeUtils.escapeHtml(searchString);
        } else {
            title = "GreatSchools.org Search";
        }
        return title;
    }

    protected static String getMetaDescription(boolean isCityBrowse, boolean isDistrictBrowse, City city, District district, LevelCode levelCode, String[] schoolTypes) {
        if (isCityBrowse) {
            return calcMetaDesc(null, city.getDisplayName(), city.getState(), levelCode, schoolTypes);
        } else if (isDistrictBrowse) {
            return SeoUtil.generateMetaDescription(district);
        } else {
            return null;
        }
    }
    protected static String getMetaDescription(City city, LevelCode levelCode, String[] schoolTypes) {
        return calcMetaDesc(null, city.getDisplayName(), city.getState(), levelCode, schoolTypes);
    }
    protected static String getMetaDescription(District district) {
        return SeoUtil.generateMetaDescription(district);
    }

    protected static String getMetaKeywords(District district) {
        if (district != null) {
            return SeoUtil.generateMetaKeywords(district);
        }
        return null;
    }

    //-------------------------------------------------------------------------
    // pagination
    //-------------------------------------------------------------------------

    /**
     * Calculates paging info and adds it to model. Paging is zero-based. First search result has start=0
     *
     * @param start
     * @param pageSize
     * @param totalResults
     * @param model
     */
    protected void addPagingDataToModel(int start, Integer pageSize, int currentPage, int totalResults, Map<String,Object> model) {

        //TODO: perform validation to only allow no paging when results are a certain size
        if (pageSize > 0) {
            int numberOfPages = (int) Math.ceil(totalResults / pageSize.floatValue());
            model.put(MODEL_CURRENT_PAGE, currentPage);
            model.put(MODEL_TOTAL_PAGES, numberOfPages);
            model.put(MODEL_USE_PAGING, Boolean.valueOf(true));
        } else {
            model.put(MODEL_USE_PAGING, Boolean.valueOf(false));
        }

        model.put(MODEL_START, start < totalResults? start : 0);
        model.put(MODEL_PAGE_SIZE, pageSize);
    }

    //-------------------------------------------------------------------------
    // GAM
    //-------------------------------------------------------------------------

    protected void addGamAttributes(HttpServletRequest request, HttpServletResponse response, PageHelper pageHelper,
                                    Map<FieldConstraint,String> constraints, List<FilterGroup> filterGroups, String searchString,
                                    List<ISchoolSearchResult> schoolResults,
                                    City city, District district) {
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

        if (district != null) {
            pageHelper.addAdKeyword("district_id", constraints.get(FieldConstraint.DISTRICT_ID));
            pageHelper.addAdKeyword("district_name", district.getName());
        }

        // GS-10448 - search results
        if (StringUtils.isNotBlank(searchString) && schoolResults != null) {
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

        // GS-11511 - nearby search by zip code
        if (request.getAttribute("nearbySearchInfo") != null && request.getAttribute("nearbySearchInfo") instanceof Map) {
            Map nearbySearchInfo = (Map) request.getAttribute("nearbySearchInfo");
            Object nearbyZipCode = nearbySearchInfo.get("zipCode");
            Object nearbyState = nearbySearchInfo.get("state");
            Object nearbyCity = nearbySearchInfo.get("city");

            if (nearbyZipCode != null && nearbyZipCode instanceof String) {
                pageHelper.addAdKeyword("zipcode", (String)nearbyZipCode);
            }
            if (nearbyState != null && nearbyState instanceof State) {
                // this overrides the state GAM attribute
                pageHelper.addAdKeyword("state", ((State)nearbyState).getAbbreviation());
            }
            if (nearbyCity != null && nearbyCity instanceof String) {
                pageHelper.addAdKeywordMulti("city", (String)nearbyCity);
            }
        }
    }

    //-------------------------------------------------------------------------
    // rel canonical
    //-------------------------------------------------------------------------

    protected String getRelCanonical(HttpServletRequest request, State state, List<ICitySearchResult> citySearchResults, City city, District district,
                                     List<FilterGroup> filterGroups, LevelCode levelCode, String searchString) {
        if (request == null || state == null) {
            throw new IllegalArgumentException("Request and state must not be null");
        }
        String url = null;

        if (StringUtils.isNotBlank(searchString) && state != null) {
            // GS-10036 - search pages
            // search string that matches city, e.g. q=alameda&state=CA
            if (citySearchResults != null) {
                for (ICitySearchResult cityResult : citySearchResults) {
                    try {
                        if (StringUtils.equalsIgnoreCase(searchString, cityResult.getCity())
                                && StringUtils.equalsIgnoreCase(state.getAbbreviation(), cityResult.getState().toString())) {
                            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.CITY_PAGE, state, searchString);
                            url = urlBuilder.asFullUrl(request);
                        }
                    } catch (Exception e) {
                        _log.warn("Error determining city URL for canonical: " + e, e);
                    }
                }
            }
            // if no valid city is discernible, the result should be canonicalized to the appropriate state home page
            if (url == null) {
                try {
                    UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.RESEARCH, state);
                    url = urlBuilder.asFullUrl(request);
                } catch (Exception e) {
                    _log.warn("Error determining state URL for canonical: " + e, e);
                }
            }
        } else {
            // GS-10144, GS-10400 - browse pages

            Set<FieldFilter> filtersSet = new HashSet<FieldFilter>();
            if (filterGroups != null) {
                for (FilterGroup filterGroup : filterGroups) {
                    filtersSet.addAll(Arrays.asList(filterGroup.getFieldFilters()));
                }
            }

            if (district != null) {
                // district browse
                UrlBuilder urlBuilder = new UrlBuilder(district, UrlBuilder.SCHOOLS_IN_DISTRICT);
                url = urlBuilder.asFullUrl(request) + (levelCode != null ? "?lc=" + levelCode : "");
            } else if (city != null) {
                // city browse
                boolean publicSelected = filtersSet.contains(FieldFilter.SchoolTypeFilter.PUBLIC);
                boolean charterSelected = filtersSet.contains(FieldFilter.SchoolTypeFilter.CHARTER);
                boolean privateSelected = filtersSet.contains(FieldFilter.SchoolTypeFilter.PRIVATE);

                HashSet<SchoolType> schoolTypeSet = new HashSet<SchoolType>(1);
                if (publicSelected && !charterSelected && !privateSelected) {
                    schoolTypeSet.add(SchoolType.PUBLIC);
                } else if (!publicSelected && charterSelected && !privateSelected) {
                    schoolTypeSet.add(SchoolType.CHARTER);
                } else if (!publicSelected && !charterSelected && privateSelected) {
                    schoolTypeSet.add(SchoolType.PRIVATE);
                }

                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY,
                        state,
                        city.getName(),
                        schoolTypeSet, levelCode);
                url = urlBuilder.asFullUrl(request);
            }
        }

        return url;
    }

    //-------------------------------------------------------------------------
    // helper methods for filters, constraints
    //-------------------------------------------------------------------------

    protected Map<FieldConstraint,String> getFieldConstraints(State state, City city, District district) {
        Map<FieldConstraint,String> fieldConstraints = new HashMap<FieldConstraint,String>();

        if (state != null) {
            fieldConstraints.put(FieldConstraint.STATE, state.getAbbreviationLowerCase());
        }

        // GS-11129 if constraining by district, don't also constrain by city
        // or else we'll omit schools in the district that aren't in the same city as the distict
        if (district != null) {
            fieldConstraints.put(FieldConstraint.DISTRICT_ID, String.valueOf(district.getId()));
        } else if (city != null) {
            fieldConstraints.put(FieldConstraint.CITY, city.getName());
        }

        return fieldConstraints;
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

    protected List<FieldFilter> getGradeLevelFilters(LevelCode levelCode) {
        List<FieldFilter> filters = new ArrayList<FieldFilter>();

        if (levelCode != null) {
            for (LevelCode.Level level : levelCode.getIndividualLevelCodes()) {
                FieldFilter filter = getGradeLevelFilter(level);
                if (filter != null) {
                    filters.add(filter);
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

    //-------------------------------------------------------------------------
    // spring-injected accessors
    //-------------------------------------------------------------------------

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

    public SchoolSearchService getLooseSchoolSearchService() {
        return _looseSchoolSearchService;
    }

    public void setLooseSchoolSearchService(SchoolSearchService looseSchoolSearchService) {
        _looseSchoolSearchService = looseSchoolSearchService;
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

    public ILocalBoardDao getLocalBoardDao() {
        return _localBoardDao;
    }

    public void setLocalBoardDao(ILocalBoardDao localBoardDao) {
        _localBoardDao = localBoardDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    //-------------------------------------------------------------------------
    // required to implement interface IDirectoryStructureUrlController
    //-------------------------------------------------------------------------

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


/*


public static boolean isOldStyleCityBrowseRequest(HttpServletRequest request) {
        if (request.getRequestURI() == null) {
            throw new IllegalArgumentException("Request must have request URI");
        }

        // state is in SessionContext already
        return (!isDistrictBrowseRequest(request) &&
                request.getRequestURI().contains("/schools.page") &&
                request.getParameter(PARAM_CITY) != null);
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

        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY,
            SessionContextUtil.getSessionContext(request).getState(), request.getParameter(PARAM_CITY), schoolTypes, levelCode);
        return urlBuilder.asSiteRelative(request);
    }


*/