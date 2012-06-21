package gs.web.test;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.state.State;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlBuilder;

/**
 * Uses spreadsheet at https://spreadsheets.google.com/pub?key=pYwV1uQwaOCJGhxtFDPHjTg
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class FirstTestScoreLandingController extends AbstractController {
    private ITableDao _tableDao;

    public static final String STATE_COLUMN = "state";
    public static final String TEST_ID_COLUMN = "tid";

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        // retrieve state from session context or default to CA
        State state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        // get first test id from spreadsheet for state
        String testId = getFirstTestIdForState(state); // can be null which is OK
        // get link based on test id
        String relativeUrl = getTestScoreLandingPagePath(state, testId, request.getParameter("s_cid"));
        // return redirect view to page
        return new ModelAndView(new RedirectView(relativeUrl));
    }

    /**
     * Looks up first row for state in the spreadsheet and returns the value as a string.
     * If no rows exist for state, returns null.
     */
    public String getFirstTestIdForState(State state) {
        ITableRow row = _tableDao.getFirstRowByKey(STATE_COLUMN, state.getAbbreviation());
        if (row != null) {
            return row.getString(TEST_ID_COLUMN);
        }
        return null;
    }

    /**
     * If id is specified, returns a link to the specified test landing page.
     * If id is not specified, returns a link to the state's R&C page.
     * In either case, if scidParam is provided it is passed through as a param.
     */
    public String getTestScoreLandingPagePath(State state, String id, String scidParam) {
        UrlBuilder urlBuilder;
        if (StringUtils.isNotBlank(id)) {
            urlBuilder = new UrlBuilder(UrlBuilder.TEST_SCORE_LANDING, state, id);
        } else {
            urlBuilder = new UrlBuilder(UrlBuilder.RESEARCH, state);
        }
        if (StringUtils.isNotBlank(scidParam)) {
            urlBuilder.addParameter("s_cid", scidParam);
        }
        return urlBuilder.asSiteRelative(null);
    }

    public ITableDao getTableDao() {
        return _tableDao;
    }

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
    }
}
