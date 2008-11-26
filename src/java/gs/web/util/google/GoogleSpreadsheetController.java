package gs.web.util.google;

import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author thuss
 */
public class GoogleSpreadsheetController extends AbstractController {

    private ITableDao _tableDao;
    private String _viewName;
    protected static final String MODEL_ROWS = "rows";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, List<ITableRow>> model = new HashMap<String, List<ITableRow>>();
        model.put(MODEL_ROWS, _tableDao.getAllRows());
        return new ModelAndView(_viewName, model);
    }

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
    }

    public void setViewName(String viewName) {
        this._viewName = viewName;
    }
}
