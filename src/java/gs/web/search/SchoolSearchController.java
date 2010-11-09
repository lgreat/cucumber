package gs.web.search;

import gs.data.json.JSONObject;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


public class SchoolSearchController extends AbstractCommandController implements IDirectoryStructureUrlController {

    private ISchoolDao _schoolDao;

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

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException e) throws Exception {

        if (e.hasErrors()) {
            //TODO: handle errors
        }

        String format = String.valueOf(request.getParameter("format"));
        
        SchoolSearchCommand schoolSearchCommand = (SchoolSearchCommand) command;

        State state = getStateManager().getState(schoolSearchCommand.getState());

        Map<FieldConstraint,String> fieldConstraints = getFieldConstraints(schoolSearchCommand, request);

        List<FieldFilter> filters = new ArrayList<FieldFilter>();
        String[] schoolSearchTypes = schoolSearchCommand.getSchoolTypes();
        if (schoolSearchTypes != null && schoolSearchTypes.length > 0) {
            filters.addAll(getSchoolTypeFilters(schoolSearchTypes));
        }
        String[] schoolGradeLevels = schoolSearchCommand.getGradeLevels();
        if (schoolGradeLevels != null && schoolGradeLevels.length > 0) {
            filters.addAll(getGradeLevelFilters(schoolGradeLevels));
        }

        FieldSort sort = this.getChosenSort(schoolSearchCommand);

        List<ISchoolSearchResult> searchResults = getSchoolSearchService().search(schoolSearchCommand.getSearchString(), fieldConstraints, filters.toArray(new FieldFilter[0]), sort);
        //List<ICitySearchResult> citySearchResults = getCitySearchService().search(schoolSearchCommand.getSearchString(), state);
        //List<IDistrictSearchResult> districtSearchResults = getDistrictSearchService().search(schoolSearchCommand.getSearchString(), state);

        PageHelper.setHasSearchedCookie(request, response);


        //TODO: write city and district lists to model
        Map<String,Object> model = new HashMap<String,Object>();
        model.put(MODEL_SCHOOL_TYPE, StringUtils.join(schoolSearchCommand.getSchoolTypes()));
        model.put(MODEL_LEVEL_CODE, StringUtils.join(schoolSearchCommand.getGradeLevels()));
        model.put(MODEL_SORT, schoolSearchCommand.getSortBy());

        addPagingDataToModel(schoolSearchCommand, searchResults, model);

        if ("json".equals(format)) {
            //TODO: find better way to generate JSON
            List<Map<String,Object>> searchResultsJson = new ArrayList<Map<String,Object>>();
            for (ISchoolSearchResult result : searchResults) {
                searchResultsJson.add(result.toMap());
            }
            model.put(MODEL_SCHOOL_SEARCH_RESULTS, searchResultsJson);
            return successJSON(response, model);
        } else {
            model.put(MODEL_SCHOOL_SEARCH_RESULTS, searchResults);
        }
        return new ModelAndView("/search/schoolSearchResults", model);
    }

    protected void addPagingDataToModel(SchoolSearchCommand schoolSearchCommand, List<ISchoolSearchResult> searchResults, Map<String,Object> model) {
        int start = schoolSearchCommand.getStart();
        Integer pageSize = schoolSearchCommand.getPageSize();
        int totalResults = searchResults.size();

        //TODO: perform validation to only allow no paging when results are a certain size
        if (pageSize > 0) {
            int currentPage = (int) Math.floor(start / pageSize);
            int numberOfPages = (int) Math.ceil(totalResults / pageSize);
            model.put(MODEL_CURRENT_PAGE, currentPage);
            model.put(MODEL_TOTAL_PAGES, numberOfPages);
            model.put(MODEL_USE_PAGING, Boolean.valueOf(true));
        } else {
            model.put(MODEL_USE_PAGING, Boolean.valueOf(false));
        }

        model.put(MODEL_START, schoolSearchCommand.getStart());
    }

    protected Map<FieldConstraint,String> getFieldConstraints(SchoolSearchCommand schoolSearchCommand, HttpServletRequest request) {
        Map<FieldConstraint,String> fieldConstraints = new HashMap<FieldConstraint,String>();
        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);

        if (schoolSearchCommand.getState() != null) {
            fieldConstraints.put(FieldConstraint.STATE, schoolSearchCommand.getState());
        } else if (fields.getState() != null) {
            fieldConstraints.put(FieldConstraint.STATE, fields.getState().getAbbreviationLowerCase());
        }

        String city = fields.getCityName();
        if (!StringUtils.isBlank(city)) {
            fieldConstraints.put(FieldConstraint.CITY, city);
        }

        String districtName = fields.getDistrictName();
        //TODO: handle district name constraint?

        String districtId = request.getParameter("districtId");
        if (!StringUtils.isBlank(districtId)) {
            fieldConstraints.put(FieldConstraint.DISTRICT_ID, districtId);
        }

        return fieldConstraints;
    }

    protected FieldSort getChosenSort(SchoolSearchCommand schoolSearchCommand) {
        //TODO: should be able to do this stuff using spring property editors
        String sortBy = schoolSearchCommand.getSortBy();
        FieldSort fieldSort = null;
        if (sortBy != null) {
            fieldSort = FieldSort.valueOf(sortBy);
        }
        return fieldSort;
    }

    protected List<FieldFilter> getSchoolTypeFilters(String[] schoolTypeStrings) {
        //TODO: should be able to do this stuff using spring property editors

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
        //TODO: should be able to do this stuff using spring property editors
        List<FieldFilter> filters = new ArrayList<FieldFilter>();

        if (gradeLevelStrings != null) {
            for (String levelCode : gradeLevelStrings) {
                LevelCode.Level level = LevelCode.Level.getLevelCode(levelCode);
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

    protected ModelAndView successJSON(HttpServletResponse response, Map<String,Object> model) throws IOException {
        HashMap<Object,Object> responseValues = new HashMap<Object,Object>();
        
        Set<Map.Entry<String,Object>> entries = model.entrySet();
        for (Map.Entry<String,Object> entry : entries) {
            responseValues.put(entry.getKey(), entry.getValue());
        }

        responseValues.put("status", "true");
        response.setContentType("text/x-json");
        if (responseValues != null && responseValues.size() > 0) {
            String jsonString = new JSONObject(responseValues).toString();
            response.getWriter().print(jsonString);
        }
        response.getWriter().flush();
        return null;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
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
        //TODO: write correct condition for when to delegate to this controller;
        
        boolean schoolsController = false;
        boolean cityController = false;
        boolean districtController = false;
        // level code is optional
        //copied from SchoolsController
        if (fields.hasState() && fields.hasCityName() && fields.hasSchoolTypes() && fields.hasSchoolsLabel()
                && !fields.hasSchoolName()) {
            schoolsController = true;
        }

        if (fields.hasState() && fields.hasCityName() && fields.hasDistrictName() && fields.hasSchoolsLabel()) {
            schoolsController =  true;
        }

        //copied from CityController
        cityController = fields.hasState() && fields.hasCityName() && !fields.hasDistrictName() && !fields.hasLevelCode() && !fields.hasSchoolName();

        //copied from DistrictController
        districtController = fields.hasState() && fields.hasCityName() && fields.hasDistrictName();

        return schoolsController || cityController || districtController;

    }
}
