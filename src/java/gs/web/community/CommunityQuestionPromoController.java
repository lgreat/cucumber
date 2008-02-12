package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.ServiceException;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import gs.web.util.UrlUtil;

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

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        loadSpreadsheetDataIntoModel(model, getWorksheetUrl(request), getCode(request));
        return new ModelAndView(_viewName, model);
    }

    protected void loadSpreadsheetDataIntoModel(Map<String, Object> model, String worksheetUrl, String code) {
        SpreadsheetService service = new SpreadsheetService("greatschools-community-question-promo");
        try {
            WorksheetEntry dataWorksheet = service.getEntry(new URL(worksheetUrl), WorksheetEntry.class);
            URL listFeedUrl = dataWorksheet.getListFeedUrl();
            ListFeed lf = service.getFeed(listFeedUrl, ListFeed.class);
            for (ListEntry entry : lf.getEntries()) {
                String entryId = entry.getCustomElements().getValue(WORKSHEET_PRIMARY_ID_COL);
                if (code.equals(entryId)) {
                    model.put(MODEL_QUESTION_TEXT, entry.getCustomElements().getValue("text"));
                    model.put(MODEL_QUESTION_LINK, entry.getCustomElements().getValue("link"));
                    model.put(MODEL_USERNAME, entry.getCustomElements().getValue("username"));
                    model.put(MODEL_USER_ID, entry.getCustomElements().getValue("memberId"));
                    break;
                }
            }
        } catch (MalformedURLException e) {
            _log.error(e);
            e.printStackTrace();
        } catch (IOException e) {
            _log.error(e);
            e.printStackTrace();
        } catch (ServiceException e) {
            _log.error(e);
            e.printStackTrace();
        }
    }

    public String getWorksheetUrl(HttpServletRequest request) {
        return WORKSHEET_PREFIX + "/" +
                getWorksheetKey(request) + "/" +
                WORKSHEET_VISIBILITY + "/" +
                WORKSHEET_PROJECTION + "/" + getWorksheet(request);
    }

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

    public String getWorksheetKey(HttpServletRequest request) {
        String key = request.getParameter("key");
        if (StringUtils.isBlank(key)) {
            key =  WORKSHEET_KEY;
        }
        return key;
    }

    public String getCode(HttpServletRequest request) {
        String code = request.getParameter(WORKSHEET_PRIMARY_ID_COL);
        if (StringUtils.isBlank(code)) {
            code =  DEFAULT_CODE;
        }
        return code;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public String getViewName() {
        return _viewName;
    }
}
