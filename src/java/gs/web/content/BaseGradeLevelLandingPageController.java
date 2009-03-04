package gs.web.content;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.data.util.NameValuePair;
import gs.data.state.State;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.web.util.google.GoogleSpreadsheetDao;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Shared behavior between e/m/h landing page controllers. Subclasses should extend populateModel to do
 * custom processing.
 *
 * @author aroy@greatschools.net
 * @author npatury@greatschools.net
 */
public class BaseGradeLevelLandingPageController extends AbstractController {
    private ITableDao _tableDao;
    private ISchoolDao _schoolDao;
    private IGeoDao _geoDao;
    private String _viewName;
    protected final Log _log = LogFactory.getLog(getClass());
    private List<String> _keySuffixes;
    private LevelCode _levelCode;
    
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        injectWorksheetName(request);
        Map<String, Object> model = new HashMap<String, Object>();
        // do grade level specific work on the model here
        try {
            loadTableRowsIntoModel(model);
            populateModel(model);
            model.remove("keyRowMap");
        } catch (Exception e) {
            _log.error(e, e);
        }

        SessionContext context = SessionContextUtil.getSessionContext(request);
        City userCity;
        if (context.getCity() != null) {
            userCity = context.getCity();
        } else {
            userCity = getGeoDao().findCity(State.CA, "Los Angeles");
        }
        model.put("cityObject", userCity);

        List<ISchoolDao.ITopRatedSchool> topRatedSchools =
                getSchoolDao().findTopRatedSchoolsInCity(userCity, 1, _levelCode.getLowestLevel(), 5);
        if (topRatedSchools.size() > 0) {
            model.put("topRatedSchools", topRatedSchools);
            List<School> schools = new ArrayList<School>(topRatedSchools.size());
            for (ISchoolDao.ITopRatedSchool s: topRatedSchools) {
                schools.add(s.getSchool());
            }
            model.put("topSchools", schools);
        } else {
            List schools = getSchoolDao().findSchoolsInCity(userCity.getState(), userCity.getName(), false);
            if (schools.size() > 0) {
                if (schools.size() > 10) {
                    schools = schools.subList(0, 10);
                }
                model.put("topSchools", schools);
            }
        }

        return new ModelAndView(getViewName(),model);
    }

    /**
     * Pre-load google spreadsheet into the model. This is to improve performance by making only a single
     * round trip to Google. The map is indexed by the value in the column "key"
     */
    protected void loadTableRowsIntoModel(Map<String, Object> model) {
        List<ITableRow> rows = getTableDao().getAllRows();
        Map<String, List<ITableRow>> keyRowMap = new HashMap<String, List<ITableRow>>(rows.size());
        for (ITableRow row: rows) {
            String key = row.getString("key");
            List<ITableRow> existingValues = keyRowMap.get(key);
            if (existingValues == null) {
                existingValues = new ArrayList<ITableRow>();
                keyRowMap.put(key, existingValues);
            }
            existingValues.add(row);
        }
        model.put("keyRowMap", keyRowMap);
    }

    /**
     * Convenience method to pull a row out of the map
     */
    protected ITableRow getFirstRowFromMap(Map<String, List<ITableRow>> keyRowMap, String key) {
        List<ITableRow> existingValues = keyRowMap.get(key);
        if (existingValues != null) {
            return existingValues.get(0);
        }
        return null;
    }

    /**
     * This operates off of the local cached map and never makes any round trips to Google.
     */
    public void loadTableRowsIntoModel(Map<String, Object> model, String keySuffix) {
        Map<String, List<ITableRow>> keyRowMap = (Map<String, List<ITableRow>>) model.get("keyRowMap");

        ITableRow teaserCollegeRow = getFirstRowFromMap(keyRowMap, "teaserText_"+keySuffix);
        ITableRow callToActionCollegeRow = getFirstRowFromMap(keyRowMap, "callToAction_"+keySuffix);
        model.put("teaserText_"+keySuffix, teaserCollegeRow.getString("text"));
        model.put("callToAction_"+keySuffix, callToActionCollegeRow.getString("text"));
        model.put("callToAction_"+keySuffix+"Url", callToActionCollegeRow.getString("url"));
        List<ITableRow> articleLinkCollegeRows = keyRowMap.get("articleLink_"+keySuffix);
        for (ITableRow row : articleLinkCollegeRows) {
            String key = row.getString("key");
            String text = row.getString("text");
            String url = row.getString("url");
            NameValuePair<String, String> textUrl = new NameValuePair<String, String>(text, url);
            List<NameValuePair<String, String>> textUrls = (List<NameValuePair<String, String>>) model.get(key);
            if (textUrls == null) {
                textUrls = new ArrayList<NameValuePair<String, String>>();
            }
            textUrls.add(textUrl);
            model.put(key, textUrls);
        }
    }

    protected void injectWorksheetName(HttpServletRequest request) {
        GoogleSpreadsheetDao castDao = (GoogleSpreadsheetDao) getTableDao();
        castDao.getSpreadsheetInfo().setWorksheetName(getWorksheet(request));
    }

    protected String getWorksheet(HttpServletRequest request) {
        String worksheetName;
        if (UrlUtil.isDevEnvironment(request.getServerName()) && !UrlUtil.isStagingServer(request.getServerName())) {
            worksheetName = "od6";
        } else if (UrlUtil.isStagingServer(request.getServerName())) {
            worksheetName = "od7";
        } else {
            worksheetName = "od4";
        }

        return worksheetName;
    }

    /**
     * Default behavior is to load the rows listed in keySuffixes into the model.
     * Extend this method to do custom processing. But remember to call super if you want to keep the
     * default behavior!
     */
    protected void populateModel(Map<String,Object> model) {
        if (getKeySuffixes() != null && getKeySuffixes().size() > 0) {
            for (String keySuffix: getKeySuffixes()) {
                loadTableRowsIntoModel(model, keySuffix);
            }
        }
    }
    
    public ITableDao getTableDao() {
        return _tableDao;
    }

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public List<String> getKeySuffixes() {
        return _keySuffixes;
    }

    public void setKeySuffixes(List<String> keySuffixes) {
        _keySuffixes = keySuffixes;
    }

    public LevelCode getLevelCode() {
        return _levelCode;
    }

    public void setLevelCode(LevelCode levelCode) {
        _levelCode = levelCode;
    }
}
