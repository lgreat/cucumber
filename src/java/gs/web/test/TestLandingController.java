package gs.web.test;

import gs.data.school.SchoolType;
import gs.web.geo.StateSpecificFooterHelper;
import gs.web.util.list.Anchor;
import gs.web.util.UrlBuilder;
import gs.web.util.google.GoogleSpreadsheetDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.geo.IGeoDao;
import gs.data.content.IArticleDao;
import gs.data.content.Article;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.DateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.*;

/**
 * This is the controller for the Test Landing Page.  It gets the data for the page
 * model from a google spreadsheet by using the gdata api.  
 *
 * @author chriskimm@greatschools.org
 */
public class TestLandingController extends SimpleFormController {

    /** Spring config id */
    public static final String BEAN_ID = "/test/landing.page";

    /** Stores all of the test data */
    static Map<String, Map> _cache;

    /** Used to populate city lists */
    private IGeoDao _geoDao;

    /** Used to build article links */
    private IArticleDao _articleDao;

    /** Used to populate school list drop-down */
    private ISchoolDao _schoolDao;

    private StateManager _stateManager;

    private ITableDao _tableDao;

    private StateSpecificFooterHelper _stateSpecificFooterHelper;

    final private static Set<SchoolType> CITY_BROWSE_SCHOOL_TYPES = new HashSet<SchoolType>();
    static {
        CITY_BROWSE_SCHOOL_TYPES.add(SchoolType.PUBLIC);
        CITY_BROWSE_SCHOOL_TYPES.add(SchoolType.CHARTER);
    }

    private static final Logger _log = Logger.getLogger(TestLandingController.class);

    protected Map referenceData(HttpServletRequest request, Object cmd, Errors errors) throws Exception {
        Map<String, Object> refData = new HashMap<String, Object>();

        if (StringUtils.isNotBlank(request.getParameter("clear"))) {
            _cache = null;
        }

        String stateParam = request.getParameter("state");
        if (StringUtils.isNotBlank(stateParam)) {
            State state = getStateManager().getState(stateParam);
            refData.put("cities", _geoDao.findCitiesByState(state));
            String testIdParam = request.getParameter("tid");
            if (StringUtils.isNotBlank(testIdParam)) {
                String key = state.getAbbreviation() + testIdParam;
                Map<String, String> testData = getTestData(key);
                if (testData == null) {
                    errors.reject("Could not find test info for: " + key);
                } else {
                    refData.putAll(testData);
                    _stateSpecificFooterHelper.placePopularCitiesInModel(state, refData);
                }
            } else {
                errors.reject("test id (tid) parameter is missing");
            }
        } else {
            errors.reject("state parameter is missing");            
        }
        return refData;
    }
    
    protected Map<String, String> getTestData(String key) {
        if (_cache == null || _cache.isEmpty()) {
             _cache = new HashMap<String, Map>();
            loadCache(_cache);
        }
        return _cache.get(key);
    }

    protected ModelAndView processFormSubmission(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 Object cmdObject,
                                                 BindException errors) {

        String type = request.getParameter("type");
        String stateParam = request.getParameter("state");
        State state = _stateManager.getState(stateParam);

        View view = null;
        if ("achievement".equals(type)) {
            String sid = request.getParameter("sid");
            Integer school_id = Integer.parseInt(sid);
            School school = getSchoolDao().getSchoolById(state, school_id);
            UrlBuilder builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_TEST_SCORE);
            String url = builder.asSiteRelative(request);

            String testIdParam = request.getParameter("tid");
            if (StringUtils.isNotBlank(testIdParam)) {
                String dataKey = state.getAbbreviation() + testIdParam;
                Map<String, String> data = getTestData(dataKey);
                if (data != null) {
                    String anchor = data.get("testanchor");
                    if (StringUtils.isNotBlank(anchor)) {
                        url = url + "#" + anchor;
                    }
                }
            }
            view = new RedirectView(url);
            
        } else if ("compare".equals(type)) {
            UrlBuilder urlBuilder = getCityBrowseUrlBuilder(state, request.getParameter("city"), request.getParameter("level"));
            view = new RedirectView(urlBuilder.asSiteRelative(request));
        } 

        return new ModelAndView(view);
    }

    static UrlBuilder getCityBrowseUrlBuilder(State state, String cityName, String level) {
        if (state == null || StringUtils.isBlank(cityName) || StringUtils.isBlank(level)) {
            throw new IllegalArgumentException("Must specify state, city name, and level");
        }
        return new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, state, cityName, CITY_BROWSE_SCHOOL_TYPES, LevelCode.createLevelCode(level));
    }

    public void loadCache(Map<String, Map> cache) {
        GoogleSpreadsheetDao spreadsheetDao = (GoogleSpreadsheetDao) getTableDao();
        List<ITableRow> rows = spreadsheetDao.getAllRows();
        for (ITableRow row : rows) {
            Map<String, Object> values = new HashMap<String, Object>();
            for (Object o : row.getColumnNames()) {
                String tag = (String)o;
                if ("links".equals(tag)) {
                    values.put(tag, parseAnchorList(row.getString(tag)));
                } else if ("levels".equals(tag)) {
                    String levs = row.getString(tag);
                    values.put(tag, parseLevelCodes(levs));
                        values.put("levs", levs);
                } else if ("alertexpire".equals(tag)) {
                    String date = row.getString(tag);
                    if (StringUtils.isNotBlank(date)) {
                        DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy");
                        DateTime dt = fmt.parseDateTime(date);
                        if (dt.isBeforeNow()) {
                            values.put("alertexpired", "true");
                        }
                    }
                } else {
                    values.put(tag, row.getString(tag));
                }
            }
            String state = row.getString("state");
            String tid = row.getString("tid");
            cache.put(state+tid, values);
        }
    }

    List<LevelCode.Level> parseLevelCodes(String text) {
        List<LevelCode.Level> list = new ArrayList<LevelCode.Level>();
        if (StringUtils.isNotBlank(text)) {
            String[] levels = text.split(",");
            for (String l : levels) {
                list.add(LevelCode.Level.getLevelCode(l));
            }
        }
        return list;
    }

    List<Anchor> parseAnchorList(String text) {
        List<Anchor> list = new ArrayList<Anchor>();
        if (StringUtils.isNotBlank(text)) {
            String[] links = text.split("\\n");
            for (String s : links) {
                String[] s2 = s.split(",");
                if (s2.length > 0) {
                    if (s2[0] != null) {
                        if (s2[0].startsWith("aid:")) {
                            try {
                                int aid = Integer.parseInt(s2[0].substring(4));
                                Article article = getArticleDao().getArticleFromId(aid);
                                if (article != null) {
                                    UrlBuilder builder = new UrlBuilder(article.getId(), false);
                                    list.add(new Anchor(builder.toString(), article.getTitle(), "article"));
                                } else {
                                    _log.warn("Could not find article: " + aid);
                                }
                            } catch (NumberFormatException e) {
                                _log.warn(e);
                            }
                        } else if (s2[0].startsWith("break")) {
                            list.add(new Anchor(null,null));
                        }   else if (s2.length == 2) {
                            if (s2[0].startsWith("More")) {
                                list.add(new Anchor(s2[1], s2[0], "more"));
                            } else {
                                list.add(new Anchor(s2[1], s2[0]));
                            }

                        }
                    }
                }
            }
        }
        return list;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public IArticleDao getArticleDao() {
        return _articleDao;
    }

    public void setArticleDao(IArticleDao articleDao) {
        _articleDao = articleDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public ITableDao getTableDao() {
        return _tableDao;
    }

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
    }

    public StateSpecificFooterHelper getStateSpecificFooterHelper() {
        return _stateSpecificFooterHelper;
    }

    public void setStateSpecificFooterHelper(StateSpecificFooterHelper stateSpecificFooterHelper) {
        _stateSpecificFooterHelper = stateSpecificFooterHelper;
    }
}
