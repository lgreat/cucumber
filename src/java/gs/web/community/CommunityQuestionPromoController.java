package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.HashMap;

import gs.web.util.UrlUtil;
import gs.web.util.google.ICachedGoogleSpreadsheetDao;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CommunityQuestionPromoController extends AbstractController {
    public static final String BEAN_ID = "/community/communityQuestionPromo.module";
    public static final String WORKSHEET_PREFIX = "http://spreadsheets.google.com/feeds/worksheets";
    public static final String WORKSHEET_KEY = "pmY-74KD4CbXrSKtrPdEnSg";
    public static final String WORKSHEET_VISIBILITY = "public";
    public static final String WORKSHEET_PROJECTION = "values";
    public static final String WORKSHEET_PRIMARY_ID_COL = "code";
    public static final String DEFAULT_CODE = "school/rating.page";
    public static final String MODEL_QUESTION_TEXT = "questionText";
    public static final String MODEL_QUESTION_LINK = "questionLink";
    public static final String MODEL_USERNAME = "username";
    public static final String MODEL_USER_ID = "userId";
    
    private static final Logger _log = Logger.getLogger(CommunityQuestionPromoController.class);

    private String _viewName;
    private ICachedGoogleSpreadsheetDao _cachedGoogleSpreadsheetDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        loadSpreadsheetDataIntoModel(model, getWorksheetUrl(request), getCode(request));
        return new ModelAndView(_viewName, model);
    }

    protected void loadSpreadsheetDataIntoModel(Map<String, Object> model, String worksheetUrl, String code) {
        Map<String, String> dataMap = getCachedGoogleSpreadsheetDao().getDataFromRow(worksheetUrl,
                WORKSHEET_PRIMARY_ID_COL, code, ICachedGoogleSpreadsheetDao.HOUR);

        if (dataMap != null) {
            model.put(MODEL_QUESTION_TEXT, dataMap.get("text"));
            model.put(MODEL_QUESTION_LINK, dataMap.get("link"));
            model.put(MODEL_USERNAME, dataMap.get("username"));
            model.put(MODEL_USER_ID, dataMap.get("memberid"));
        }
    }

    public String getWorksheetUrl(HttpServletRequest request) {
        return WORKSHEET_PREFIX + "/" +
                getWorksheetKey(request) + "/" +
                WORKSHEET_VISIBILITY + "/" +
                WORKSHEET_PROJECTION + "/" + getWorksheet(request);
    }

    /**
     * Allows the worksheet to be overridden by the request. If not, it returns od6 (first worksheet)
     * for dev and developer boxes, and od4 for staging, production, all else
     */
    public String getWorksheet(HttpServletRequest request) {
        String worksheet = request.getParameter("worksheet");
        if (StringUtils.isBlank(worksheet)) {
            UrlUtil util = new UrlUtil();
            if (util.isDevEnvironment(request.getServerName()) && !util.isStagingServer(request.getServerName())) {
                worksheet = "od6"; // od6 is always the first worksheet
            } else {
                worksheet = "od4";
            }
        }
        return worksheet;
    }

    /**
     * Allows the Google Spreadsheets key (that determines which spreadsheet to use) to be overridden
     * by the request
     */
    public String getWorksheetKey(HttpServletRequest request) {
        String key = request.getParameter("key");
        if (StringUtils.isBlank(key)) {
            key =  WORKSHEET_KEY;
        }
        return key;
    }

    /**
     * Allows the code defining which row in the worksheet to use to be overridden by the request.
     */
    public String getCode(HttpServletRequest request) {
        String code = request.getParameter(WORKSHEET_PRIMARY_ID_COL);
        if (StringUtils.isBlank(code)) {
            code =  DEFAULT_CODE;
        }
        return code;
    }

    public ICachedGoogleSpreadsheetDao getCachedGoogleSpreadsheetDao() {
        return _cachedGoogleSpreadsheetDao;
    }

    public void setCachedGoogleSpreadsheetDao(ICachedGoogleSpreadsheetDao cachedGoogleSpreadsheetDao) {
        _cachedGoogleSpreadsheetDao = cachedGoogleSpreadsheetDao;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public String getViewName() {
        return _viewName;
    }
}
