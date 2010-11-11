package gs.web.compare;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.util.PageHelper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public abstract class AbstractCompareSchoolController extends AbstractController {
    private static final Logger _log = Logger.getLogger(AbstractCompareSchoolController.class);
    public static final String PARAM_SCHOOLS = "schools";
    public static final String PARAM_PAGE = "p";
    public static final String MODEL_SCHOOLS_STRING = "schoolsString";
    public static final String MODEL_SCHOOLS = "schools";
    public static final String MODEL_START_INDEX = "startIndex";
    public static final String MODEL_END_INDEX = "endIndex";
    public static final String MODEL_TOTAL_SCHOOLS = "totalSchools";
    public static final String MODEL_TAB = "tab";
    public static final String MODEL_PAGE_NUMBER = "page";
    public static final String MODEL_PAGE_SIZE = "pageSize";
    public static final int DEFAULT_PAGE_SIZE = 4;
    public static final int MIN_SCHOOLS = 2;
    public static final int MAX_SCHOOLS = 8;

    private ISchoolDao _schoolDao;
    private IRatingsConfigDao _ratingsConfigDao;
    private TestManager _testManager;
    private String _errorView = "/compare/error";
    private int _pageSize = DEFAULT_PAGE_SIZE;

    @Override
    protected final ModelAndView handleRequestInternal(HttpServletRequest request,
                                                       HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();
        List<ComparedSchoolBaseStruct> schools = getSchools(request, model);
        if (schools == null) {
            return getErrorResponse("invalid school string");
        }
        model.put(MODEL_SCHOOLS_STRING, request.getParameter(PARAM_SCHOOLS));
        try {
            handleCompareRequest(request, response, schools, model);
            model.put(MODEL_SCHOOLS, schools);
        } catch (Exception e) {
            _log.error(e, e);
            return getErrorResponse("unknown exception");
        }
        return new ModelAndView(getSuccessView(), model);
    }

    /** Insert whatever data needed by the view for the implementing class. */
    protected abstract void handleCompareRequest(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 List<ComparedSchoolBaseStruct> schools,
                                                 Map<String, Object> model) throws IOException;
    /** Return the appropriate success view for the implementing class. */
    public abstract String getSuccessView();
    /** Return an instance of the struct appropriate for the implementing class. */
    protected abstract ComparedSchoolBaseStruct getStruct();

    protected ModelAndView getErrorResponse(String details) {
        Map<String, String> errorModel = new HashMap<String, String>(1);
        errorModel.put("details", details);
        return new ModelAndView(_errorView, errorModel);
    }

    protected String[] paginateSchools(HttpServletRequest request, String[] schoolsArray,
                                       Map<String, Object> model) {
        model.put(MODEL_TOTAL_SCHOOLS, schoolsArray.length);
        model.put(MODEL_START_INDEX, 1);
        model.put(MODEL_END_INDEX, getPageSize());
        model.put(MODEL_PAGE_NUMBER, 1);
        model.put(MODEL_PAGE_SIZE, getPageSize());
        if (schoolsArray.length <= getPageSize()) {
            model.put(MODEL_END_INDEX, schoolsArray.length);
            return schoolsArray;
        }
        String pageNumber = request.getParameter(PARAM_PAGE);
        if (StringUtils.isBlank(pageNumber) || StringUtils.equals("1", pageNumber)
                || !StringUtils.isNumeric(pageNumber)) {
            return (String[]) ArrayUtils.subarray(schoolsArray, 0, getPageSize());
        }
        int startIndex, endIndex;
        try {
            int nPageNumber = Integer.parseInt(pageNumber);
            model.put(MODEL_PAGE_NUMBER, nPageNumber);
            endIndex = getPageSize() * nPageNumber;
            if (endIndex > schoolsArray.length) {
                endIndex = schoolsArray.length;
            }
            startIndex = endIndex - getPageSize();
            model.put(MODEL_START_INDEX, startIndex+1);
            model.put(MODEL_END_INDEX, endIndex);
            return (String[]) ArrayUtils.subarray(schoolsArray, startIndex, endIndex);
        } catch (NumberFormatException nfe) {
            _log.warn("Invalid page number: " + pageNumber);
            return (String[]) ArrayUtils.subarray(schoolsArray, 0, getPageSize());
        }
    }

    protected boolean validateSchools(String[] schools) {
        if (schools.length < MIN_SCHOOLS) {
            _log.error("Compare schools with fewer than " + MIN_SCHOOLS +
                    " schools. String used: \"" + StringUtils.join(schools, ",") + "\"");
            return false;
        } else if (schools.length > MAX_SCHOOLS) {
            _log.error("Compare schools with more than " + MAX_SCHOOLS +
                    " schools. String used: \"" + StringUtils.join(schools, ",") + "\"");
            return false;
        }

        State theState = null;
        Set<Integer> idSet = new HashSet<Integer>(8);
        for (String school: schools) {
            if (StringUtils.length(school) < 3) {
                _log.error("Compare schools String invalid: \"" + school + "\"");
                return false;
            }
            try {
                State state = State.fromString(school.substring(0, 2));
                int id = Integer.parseInt(school.substring(2));
                if (theState == null) {
                    theState = state;
                } else if (!theState.equals(state)) {
                    _log.error("Differing states in compare schools string: " + StringUtils.join(schools, ","));
                    return false;
                }
                if (idSet.contains(id)) {
                    _log.error("Duplicate school in compare schools string: " + StringUtils.join(schools, ","));
                    return false;
                } else {
                    idSet.add(id);
                }
            } catch (IllegalArgumentException iae) {
                _log.error("Compare schools String invalid: \"" + school + "\"");
                return false;
            }
        }
        return true;
    }

    protected List<ComparedSchoolBaseStruct> getSchools(HttpServletRequest request, Map<String, Object> model) {
        String schoolsParamValue = request.getParameter(PARAM_SCHOOLS);
        if (StringUtils.isBlank(schoolsParamValue)) {
            _log.error("Compare schools string empty");
            return null;
        }
        String[] splitSchools = schoolsParamValue.split(",");
        if (!validateSchools(splitSchools)) {
            return null;
        }

        splitSchools = paginateSchools(request, splitSchools, model);
        List<ComparedSchoolBaseStruct> rval = new ArrayList<ComparedSchoolBaseStruct>(splitSchools.length);
        for (String splitSchool: splitSchools) {
            try {
                State state = State.fromString(splitSchool.substring(0, 2));
                int id = Integer.parseInt(splitSchool.substring(2));
                School school = _schoolDao.getSchoolById(state, id);
                if (school == null) {
                    _log.error("Compare schools School not found. School " + splitSchool +
                        " from \"" + schoolsParamValue + "\"");
                    return null;
                }
                ComparedSchoolBaseStruct struct = getStruct();
                struct.setSchool(school);
                rval.add(struct);
            } catch (ObjectRetrievalFailureException orfe) {
                _log.error("Compare schools School not found. School " + splitSchool +
                        " from \"" + schoolsParamValue + "\"");
                return null;
            }
        }
        return rval;
    }

    // Helper methods

    protected void handleGSRating(HttpServletRequest request, List<ComparedSchoolBaseStruct> schools) throws IOException {
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        boolean isFromCache = true;
        if (pageHelper != null && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer()) {
            isFromCache = false;
        }

        State state = schools.get(0).getSchool().getDatabaseState();
        IRatingsConfig ratingsConfig = _ratingsConfigDao.restoreRatingsConfig(state, isFromCache);

        if (null != ratingsConfig) {
            for (ComparedSchoolBaseStruct baseStruct: schools) {
                handleGSRating(baseStruct, ratingsConfig);
            }
        }
    }

    protected void handleGSRating(ComparedSchoolBaseStruct struct, IRatingsConfig ratingsConfig) {
        SchoolTestValue schoolTestValue =
                _testManager.getOverallRating(struct.getSchool(), ratingsConfig.getYear());
        if (null != schoolTestValue && null != schoolTestValue.getValueInteger()) {
            struct.setGsRating(schoolTestValue.getValueInteger());
        }
    }


    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IRatingsConfigDao getRatingsConfigDao() {
        return _ratingsConfigDao;
    }

    public void setRatingsConfigDao(IRatingsConfigDao ratingsConfigDao) {
        _ratingsConfigDao = ratingsConfigDao;
    }

    public TestManager getTestManager() {
        return _testManager;
    }

    public void setTestManager(TestManager testManager) {
        _testManager = testManager;
    }

    public String getErrorView() {
        return _errorView;
    }

    public void setErrorView(String errorView) {
        _errorView = errorView;
    }

    public int getPageSize() {
        return _pageSize;
    }

    public void setPageSize(int pageSize) {
        _pageSize = pageSize;
    }
}
