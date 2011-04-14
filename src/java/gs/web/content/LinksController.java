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
import gs.data.util.google.GoogleSpreadsheetDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This class fetches links from a Google Spreadsheet for rendering in a list to be shown on a microsite page.
 * @author <a href="yfan@greatschools.org">Young Fan</a>
 */
public class LinksController extends AbstractController {
    public static final String BEAN_ID = "/content/links.module";
    protected final Log _log = LogFactory.getLog(getClass());

    private ITableDao _tableDao;

    private String devWorksheetName;
    private String stagingWorksheetName;
    private String productionWorksheetName;

    public static final String VIEW_NAME = "/content/links";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_LAYOUT = "layout";
    public static final String TYPE_ALL = "all";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_RANDOM = "random";
    public static final String TYPE_FIRST = "first";
    public static final String LAYOUT_BASIC = "basic";
    public static final String LAYOUT_IMAGE = "image";
    public static final String LAYOUT_Q_AND_A = "q_and_a";
    public static final String LAYOUT_LIST = "list";
    public static final String MODEL_ANCHOR = "modelAnchor";
    public static final String MODEL_ANCHOR_LIST = "modelAnchorList";
    public static final String MODEL_LAYOUT = "modelLayout";
    public static final String SPREADSHEET_PAGE = "page";
    public static final String SPREADSHEET_TEXT = "text";
    public static final String SPREADSHEET_URL = "url";
    public static final String SPREADSHEET_BEFORE = "before";
    public static final String SPREADSHEET_AFTER = "after";

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
        castDao.getSpreadsheetInfo().setWorksheetName(getWorksheet(request));
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

    protected boolean isValidRequest(HttpServletRequest request) {
        String page = request.getParameter(PARAM_PAGE);
        String type = request.getParameter(PARAM_TYPE);
        String layout = request.getParameter(PARAM_LAYOUT);

        if (page == null || type == null || layout == null) {
            return false;
        }

        if (!TYPE_FIRST.equals(type)
                && !TYPE_ALL.equals(type)
                && !TYPE_RANDOM.equals(type)
                && !TYPE_IMAGE.equals(type)) {
            return false;
        }

        if (!LAYOUT_BASIC.equals(layout)
                && !LAYOUT_Q_AND_A.equals(layout)
                && !LAYOUT_LIST.equals(layout)
                && !LAYOUT_IMAGE.equals(layout)) {
            return false;
        }

        return true;
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        if (!isValidRequest(request)) {
            return null;
        }

        String page = request.getParameter(PARAM_PAGE);
        String type = request.getParameter(PARAM_TYPE);
        String layout = request.getParameter(PARAM_LAYOUT);

        injectWorksheetName(request);

        // Note: GoogleSpreadsheetDao returns an empty list of ITableRow if no results found.

        ModelAndView modelAndView = new ModelAndView(VIEW_NAME);

        modelAndView.addObject("page", page);

        if (TYPE_FIRST.equals(type) || TYPE_IMAGE.equals(type)) {
            ITableRow row = _tableDao.getFirstRowByKey(SPREADSHEET_PAGE, page);
            if (row != null) {
                Anchor anchor = new Anchor(row.getString(SPREADSHEET_URL), row.getString(SPREADSHEET_TEXT));
                anchor.setBefore(row.getString(SPREADSHEET_BEFORE));
                anchor.setAfter(row.getString(SPREADSHEET_AFTER));
                modelAndView.addObject(MODEL_ANCHOR, anchor);
            }
        } else if (TYPE_RANDOM.equals(type)) {
            ITableRow row = _tableDao.getRandomRowByKey(SPREADSHEET_PAGE, page);
            if (row != null) {
                Anchor anchor = new Anchor(row.getString(SPREADSHEET_URL), row.getString(SPREADSHEET_TEXT));
                anchor.setBefore(row.getString(SPREADSHEET_BEFORE));
                anchor.setAfter(row.getString(SPREADSHEET_AFTER));
                modelAndView.addObject(MODEL_ANCHOR, anchor);
            }
        } else if (TYPE_ALL.equals(type)) {
            List<ITableRow> rows = _tableDao.getRowsByKey(SPREADSHEET_PAGE, page);
            if (rows != null) {
                AnchorListModel anchorListModel = new AnchorListModel();
                for (ITableRow row : rows) {
                    Anchor anchor = new Anchor(row.getString(SPREADSHEET_URL), row.getString(SPREADSHEET_TEXT));
                    anchor.setBefore(row.getString(SPREADSHEET_BEFORE));
                    anchor.setAfter(row.getString(SPREADSHEET_AFTER));
                    anchorListModel.add(anchor);
                }
                modelAndView.addObject(MODEL_ANCHOR_LIST, anchorListModel);
            }
        }

        modelAndView.addObject(MODEL_LAYOUT, layout);

        return modelAndView;
    }
}