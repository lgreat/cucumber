package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
//import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.HashMap;

import gs.web.util.UrlUtil;
import gs.web.util.google.IGoogleSpreadsheetDao;
import gs.web.util.google.SpreadsheetRow;
import gs.web.util.google.GoogleSpreadsheetFactory;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CommunityQuestionPromoController extends AbstractController {
    public static final String BEAN_ID = "/community/communityQuestionPromo.module";
    public static final String WORKSHEET_PRIMARY_ID_COL = "code";
    public static final String DEFAULT_CODE = "school/rating.page";
    public static final String MODEL_QUESTION_TEXT = "questionText";
    public static final String MODEL_QUESTION_LINK = "questionLink";
    public static final String MODEL_USERNAME = "username";
    public static final String MODEL_USER_ID = "userId";
    public static final String CACHE_CLEAR_PARAM = "clear";

//    private static final Logger _log = Logger.getLogger(CommunityQuestionPromoController.class);

    private String _viewName;
    private GoogleSpreadsheetFactory _googleSpreadsheetFactory;
    private IGoogleSpreadsheetDao _googleSpreadsheetDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // this could be spring configured, except that it varies depending on what hostname this request
        // is running off of
        getGoogleSpreadsheetFactory().setWorksheetName(getWorksheet(request));
        if (!StringUtils.isBlank(request.getParameter(CACHE_CLEAR_PARAM))) {
            getGoogleSpreadsheetDao().clearCache();
        }
        Map<String, Object> model = new HashMap<String, Object>();
        loadSpreadsheetDataIntoModel(model, getCode(request));
        return new ModelAndView(_viewName, model);
    }

    protected void loadSpreadsheetDataIntoModel(Map<String, Object> model, String code) {
        SpreadsheetRow row = getGoogleSpreadsheetDao().getFirstRowByKey
                (WORKSHEET_PRIMARY_ID_COL, code);

        if (row != null) {
            model.put(MODEL_QUESTION_TEXT, row.getCell("text"));
            model.put(MODEL_QUESTION_LINK, row.getCell("link"));
            model.put(MODEL_USERNAME, row.getCell("username"));
            model.put(MODEL_USER_ID, row.getCell("memberid"));
        }
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
     * Allows the code defining which row in the worksheet to use to be overridden by the request.
     */
    public String getCode(HttpServletRequest request) {
        String code = request.getParameter(WORKSHEET_PRIMARY_ID_COL);
        if (StringUtils.isBlank(code)) {
            code =  DEFAULT_CODE;
        }
        return code;
    }

    public GoogleSpreadsheetFactory getGoogleSpreadsheetFactory() {
        return _googleSpreadsheetFactory;
    }

    public void setGoogleSpreadsheetFactory(GoogleSpreadsheetFactory googleSpreadsheetFactory) {
        _googleSpreadsheetFactory = googleSpreadsheetFactory;
    }

    public IGoogleSpreadsheetDao getGoogleSpreadsheetDao() {
        if (_googleSpreadsheetDao == null) {
            _googleSpreadsheetDao = getGoogleSpreadsheetFactory().getGoogleSpreadsheetDao();
        }
        return _googleSpreadsheetDao;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public String getViewName() {
        return _viewName;
    }
}
