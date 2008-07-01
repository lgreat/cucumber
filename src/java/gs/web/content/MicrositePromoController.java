package gs.web.content;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.web.util.list.Anchor;
import gs.web.util.list.AnchorListModel;
import gs.web.util.UrlUtil;
import gs.web.util.google.GoogleSpreadsheetDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Random;

/**
 * This class fetches links from a Google Spreadsheet for rendering in a list to be shown on a microsite page.
 * @author <a href="yfan@greatschools.net">Young Fan</a>
 */
public class MicrositePromoController extends AbstractController {
    public static final String BEAN_ID = "/content/micrositePromo.module";
    protected final Log _log = LogFactory.getLog(getClass());

    private ITableDao _tableDao;

    private String devWorksheetName;
    private String stagingWorksheetName;
    private String productionWorksheetName;

    public static final String VIEW_NAME = "/content/micrositePromo";
    public static final String PARAM_PAGE = "page";
    public static final String MODEL_ANCHOR = "modelAnchor";    
    public static final String SPREADSHEET_PAGE = "page";
    public static final String SPREADSHEET_TEXT = "text";
    public static final String SPREADSHEET_URL = "url";

    public ITableDao getTableDao() {
        return _tableDao;
    }

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
    }

    public String getDevWorksheetName() {
        return devWorksheetName;
    }

    public void setDevWorksheetName(String devWorksheetName) {
        this.devWorksheetName = devWorksheetName;
    }

    public String getStagingWorksheetName() {
        return stagingWorksheetName;
    }

    public void setStagingWorksheetName(String stagingWorksheetName) {
        this.stagingWorksheetName = stagingWorksheetName;
    }

    public String getProductionWorksheetName() {
        return productionWorksheetName;
    }

    public void setProductionWorksheetName(String productionWorksheetName) {
        this.productionWorksheetName = productionWorksheetName;
    }

    /**
     * This could be spring configured, except that it varies depending on what hostname this request
     * is running off of
     * @see gs.web.community.CommunityQuestionPromoController
     */
    protected void injectWorksheetName(HttpServletRequest request) {
        GoogleSpreadsheetDao castDao = (GoogleSpreadsheetDao) getTableDao();
        String worksheetName = getWorksheet(request);
        String worksheetUrl = castDao.getWorksheetUrl();
        if (!worksheetUrl.endsWith(worksheetName)) {
            castDao.setWorksheetUrl(worksheetUrl + worksheetName);
        }
    }

    protected String getWorksheet(HttpServletRequest request) {
        String worksheetName;
        UrlUtil util = new UrlUtil();

        if (util.isDevEnvironment(request.getServerName()) && !util.isStagingServer(request.getServerName())) {
            worksheetName = getDevWorksheetName();
        } else if (util.isStagingServer(request.getServerName())) {
            worksheetName = getStagingWorksheetName();
        } else {
            worksheetName = getProductionWorksheetName();
        }

        return worksheetName;
    }

        /**
     * Returns a random row out of a list of rows.
     *
     * @param rows list of rows
     * @return a random row contained in rows
     */
    protected ITableRow getRandomRow(List<ITableRow> rows) {
        int count = rows.size();
        Random ran = new Random();
        int randomIndex = ran.nextInt(count);
        return rows.get(randomIndex);
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        String page = request.getParameter(PARAM_PAGE);

        if (page == null) {
            return null;
        }

        injectWorksheetName(request);

        // Note: GoogleSpreadsheetDao returns an empty list of ITableRow if no results found.

        ITableRow row = _tableDao.getRandomRowByKey(SPREADSHEET_PAGE, page);
        Anchor anchor = new Anchor(row.getString(SPREADSHEET_URL), row.getString(SPREADSHEET_TEXT));

        ModelAndView modelAndView = new ModelAndView(VIEW_NAME);
        modelAndView.addObject(MODEL_ANCHOR, anchor);
        return modelAndView;
    }
}
