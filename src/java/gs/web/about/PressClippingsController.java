package gs.web.about;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.data.util.google.GoogleSpreadsheetDao;
import gs.web.util.UrlUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="yfan@greatschools.org">Young Fan</a>
 */
public class PressClippingsController extends AbstractController {
    public static final String BEAN_ID = "/about/pressClippings.module";
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String VIEW_NAME = "/about/pressClippings";
    public static final String MODEL_PRESS_CLIPPINGS = "modelPressClippings";
    public static final String PARAM_FIRST = "first";
    public static final String SPREADSHEET_TEXT = "text";
    public static final String SPREADSHEET_URL = "url";
    public static final String SPREADSHEET_SOURCE = "source";
    public static final String SPREADSHEET_DATE = "date";

    private ITableDao _tableDao;
    private String _internalWorksheetName;
    private String _productionWorksheetName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        injectWorksheetName(request);

        boolean includeAll = true;
        int numToInclude = 0;
        if (request.getParameter(PARAM_FIRST) != null) {
            numToInclude = Integer.parseInt(request.getParameter(PARAM_FIRST));
            includeAll = false;
        }

        // Note: GoogleSpreadsheetDao returns an empty list of ITableRow if no results found.

        List<PressClipping> clippings = new ArrayList<PressClipping>();

        ModelAndView modelAndView = new ModelAndView(VIEW_NAME);
        List<ITableRow> rows = _tableDao.getAllRows();
        int count = 0;
        for (ITableRow row : rows) {
            count++;

            if (includeAll || count <= numToInclude) {
                PressClipping clipping = new PressClipping();
                clipping.setText(row.getString(SPREADSHEET_TEXT));
                clipping.setUrl(row.getString(SPREADSHEET_URL));
                clipping.setSource(row.getString(SPREADSHEET_SOURCE));
                clipping.setDate(row.getString(SPREADSHEET_DATE));
                clippings.add(clipping);
            }
        }
        modelAndView.addObject(MODEL_PRESS_CLIPPINGS, clippings);

        return modelAndView;
    }

    protected String getWorksheet(HttpServletRequest request) {
        String worksheetName;
        UrlUtil util = new UrlUtil();

        if (util.isDevEnvironment(request.getServerName())) {
            worksheetName = getInternalWorksheetName();
        } else {
            worksheetName = getProductionWorksheetName();
        }

        return worksheetName;
    }

    /**
     * This could be spring configured, except that it varies depending on what hostname this request
     * is running off of
     * @see gs.web.community.CommunityQuestionPromoController
     */
    protected void injectWorksheetName(HttpServletRequest request) {
        GoogleSpreadsheetDao castDao = (GoogleSpreadsheetDao) getTableDao();
        castDao.getSpreadsheetInfo().setWorksheetName(getWorksheet(request));
    }

    public ITableDao getTableDao() {
        return _tableDao;
    }

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
    }

    public String getInternalWorksheetName() {
        return _internalWorksheetName;
    }

    public void setInternalWorksheetName(String internalWorksheetName) {
        _internalWorksheetName = internalWorksheetName;
    }

    public String getProductionWorksheetName() {
        return _productionWorksheetName;
    }

    public void setProductionWorksheetName(String productionWorksheetName) {
        _productionWorksheetName = productionWorksheetName;
    }
}
