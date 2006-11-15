package gs.web.status;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.admin.batch.IBatchLogDao;

import java.util.Map;
import java.util.HashMap;

/**
 * Displays status information about batch jobs that have run
 *
 * @author thuss
 */
public class BatchLogController extends AbstractController {

    public static final String BEAN_ID = "/status/batch.page";

    public static final String MODEL_BATCH_LOGS = "batchlogs";

    public static final String MODEL_OVERVIEW = "overview";

    public static final String PARAM_NAME = "name";

    private String _viewName;

    private IBatchLogDao _batchLogDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map model = new HashMap();
        String nameParam = request.getParameter(PARAM_NAME);

        // Look in the URL if they aren't in parameters
        if (StringUtils.isEmpty(nameParam)) {
            model.put(MODEL_BATCH_LOGS, _batchLogDao.findMostRecent());
            model.put(MODEL_OVERVIEW, Boolean.TRUE);
        } else {
            model.put(MODEL_BATCH_LOGS, _batchLogDao.findByName(nameParam));
        }
        return new ModelAndView(_viewName, model);
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public void setBatchLogDao(IBatchLogDao batchLogDao) {
        _batchLogDao = batchLogDao;
    }
}
