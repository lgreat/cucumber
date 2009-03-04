package gs.web.content;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.data.util.NameValuePair;
import gs.data.state.State;
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
    private String _viewName;
    protected final Log _log = LogFactory.getLog(getClass());
    private List<String> _keySuffixes;
    
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        injectWorksheetName(request);
        Map<String, Object> model = new HashMap<String, Object>();
        // do grade level specific work on the model here
        try {
              populateModel(model);
        } catch (Exception e) {
            _log.error(e, e);
        }
        // populate city for Top [grade level] schools module
        SessionContext context = SessionContextUtil.getSessionContext(request);
        String userCityName = "Los Angeles";
        State userState = context.getStateOrDefault();
        if (context.getCity() != null) {
            userCityName = context.getCity().getName();
        }
        model.put("userCity", userCityName);
        model.put("userState", userState);

        return new ModelAndView(getViewName(),model);
    }

    /**
     * TODO: Improve performance.
     * Performance-wise, this method encourages multiple round trips to Google.
     * Ideally this would get every row in the spreadsheet the first time and cache it locally.
     * Then subsequent calls would pull from the local cache. For example, the elem controller
     * calls this method six times for k, 1, 2, 3, 4, and 5.
     */
    public void loadTableRowsIntoModel(Map<String, Object> model,String keySuffix) {
        ITableRow teaserCollegeRow = getTableDao().getFirstRowByKey("key", "teaserText_"+keySuffix);
        ITableRow callToActionCollegeRow = getTableDao().getFirstRowByKey("key", "callToAction_"+keySuffix);
        model.put("teaserText_"+keySuffix, teaserCollegeRow.getString("text"));
        model.put("callToAction_"+keySuffix, callToActionCollegeRow.getString("text"));
        model.put("callToAction_"+keySuffix+"Url", callToActionCollegeRow.getString("url"));
        List<ITableRow> articleLinkCollegeRows = getTableDao().getRowsByKey("key", "articleLink_"+keySuffix);
        for (ITableRow row : articleLinkCollegeRows) {
            String key = row.getString("key");
            String text = row.getString("text");
            String url = row.getString("url");
            text = StringUtils.replace(text, " ", "&nbsp;");
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
     * Extend this method to do custom processing.
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
}
