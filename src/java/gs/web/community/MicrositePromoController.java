package gs.web.community;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.web.util.list.Anchor;
import gs.web.util.list.AnchorListModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This class fetches links from a Google Spreadsheet for rendering in a list to be shown on a microsite page.
 * @author <a href="yfan@greatschools.net">Young Fan</a>
 */
public class MicrositePromoController extends AbstractController {
    public static final String BEAN_ID = "/community/micrositePromo.module";
    protected final Log _log = LogFactory.getLog(getClass());

    private ITableDao _tableDao;
    public static final String VIEW_NAME = "/community/micrositePromo";
    public static final String PARAM_PAGE = "page";
    public static final String MODEL_ANCHOR_LIST = "modelAnchorList";    
    private static final String SPREADSHEET_PAGE = "page";
    private static final String SPREADSHEET_TEXT = "text";
    private static final String SPREADSHEET_URL = "url";

    public ITableDao getTableDao() {
        return _tableDao;
    }

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        String page = request.getParameter(PARAM_PAGE);

        if (page == null) {
            return null;
        }

        // Note: GoogleSpreadsheetDao returns an empty list of ITableRow if no results found.

        List<ITableRow> rows = _tableDao.getRowsByKey(SPREADSHEET_PAGE, page);
        AnchorListModel anchorListModel = new AnchorListModel();
        for (ITableRow row : rows) {
            Anchor anchor = new Anchor(row.getString(SPREADSHEET_URL), row.getString(SPREADSHEET_TEXT));
            anchorListModel.add(anchor);
        }

        ModelAndView modelAndView = new ModelAndView(VIEW_NAME);
        modelAndView.addObject(MODEL_ANCHOR_LIST, anchorListModel);
        return modelAndView;
    }
}
