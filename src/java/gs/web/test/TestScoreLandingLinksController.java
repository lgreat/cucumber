package gs.web.test;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.util.table.ITableDao;
import gs.web.util.UrlBuilder;
import gs.data.util.table.ITableRow;
import gs.web.util.list.Anchor;
import gs.data.state.State;
import gs.data.state.StateManager;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Dave Roy droy@greatschools.org
 */
public class TestScoreLandingLinksController extends AbstractController {
    public static final String SHOW_FINDER_PARAM = "showFinder";
    public static final String STATE_PARAM = "state";
    public static final String SKIP_TID_PARAM = "skip_tid";
    public static final String WORKSHEET_PRIMARY_ID_COL = "state";

    protected static final String _anchorStyleClass = "testScoreLandingLink";
    protected static final String MODEL_LANDING_LINKS_KEY = "testLandingLinks";
    protected static final String TABLE_DATA_TYPE_ID_COL = "tid";
    protected static final String TABLE_SHORT_NAME_COL = "shortname";
    protected static final String TABLE_LONG_NAME_COL = "longname";

    private String _viewName;
    private StateManager _stateManager;

    private ITableDao _tableDao;

    private Logger _log = Logger.getLogger(TestScoreLandingLinksController.class);

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            loadSpreadsheetDataIntoModel(model, httpServletRequest);
        } catch (Exception e) {
            _log.error("Exception in landing links controller: ", e);
        }
        return new ModelAndView(_viewName, model);
    }

    /**
     * Access the spreadsheet and pull out the data to create anchors in the model.
     * @param model Model to pass to view
     * @param request HTTP request object
     */
    protected void loadSpreadsheetDataIntoModel(Map<String, Object> model, HttpServletRequest request) {
        List<Anchor> links = new ArrayList<Anchor>();

        String stateParam = request.getParameter(STATE_PARAM);
        if (stateParam != null) {
            State state = _stateManager.getState(stateParam);
            if (state != null) {
                if (!StringUtils.equals(request.getParameter(SHOW_FINDER_PARAM), "false")) {
                    links.add(makeFindSchoolsLink(state));
                }

                String skip_tid = request.getParameter(SKIP_TID_PARAM);
                if (skip_tid == null) {
                    skip_tid = "";
                }
                List<ITableRow> rows = getSpreadsheetRows(state);
                if (rows != null && !rows.isEmpty()) {
                    for (ITableRow row : rows) {
                        Anchor anchor = makeLandingLink(state, row, skip_tid);
                        if (anchor != null) {
                            links.add(anchor);
                        }
                    }
                }
            }
        }

        model.put(MODEL_LANDING_LINKS_KEY, links);
    }

    /**
     * Get the rows for the current state
     * @param state The state to get rows for
     * @return List of rows
     */
    protected List<ITableRow> getSpreadsheetRows(State state) {
        String stateAbbrev = state.getAbbreviation();
        return getTableDao().getRowsByKey(WORKSHEET_PRIMARY_ID_COL, stateAbbrev);
    }

    /**
     * Create an anchor for a specific test landing link
     * @param state The state to make a link for
     * @param row Row of data containing details on the test
     * @param skip_tid The test id of a test to ignore, if you are already on that tests page, for example
     * @return Anchor containing link to test landing page
     */
    protected Anchor makeLandingLink(State state, ITableRow row, String skip_tid) {
        Anchor result = null;
        
        String datatype_id_str = row.getString(TABLE_DATA_TYPE_ID_COL);
        if (!StringUtils.isBlank(datatype_id_str) && !skip_tid.equals(datatype_id_str)) {
            String shortname = row.getString(TABLE_SHORT_NAME_COL);
            if (!StringUtils.isBlank(shortname) ) {
                result = makeLandingLink(state, shortname, datatype_id_str);
            } else {
                String longname = row.getString(TABLE_LONG_NAME_COL);
                if (!StringUtils.isBlank(longname)) {
                    result = makeLandingLink(state, longname, datatype_id_str);
                }
            }
        }

        return result;
    }

    /**
     * Create an anchor for a specific test landing link
     * @param state The state to make a link for
     * @param testName The name of thest being linked to
     * @param datatype_id The datatype id of the test being linked to
     * @return Anchor containing the link to test landing page
     */
    protected Anchor makeLandingLink(State state, String testName, String datatype_id) {
        String testNameLower = testName.toLowerCase();
        if (!(testNameLower.endsWith(" ratings") || testNameLower.endsWith(" grades") ||
                testNameLower.endsWith(" profiles") || testNameLower.endsWith(" scores"))) {
            testName += " results";
        }
        String visibleText = state.getLongName() + " " + testName;
        UrlBuilder url_builder = new UrlBuilder(UrlBuilder.TEST_SCORE_LANDING, state, datatype_id);
        return new Anchor(url_builder.toString(), visibleText, _anchorStyleClass);
    }

    /**
     * Create an anchor to find more schools in the state
     * @param state The state to make a finder link for
     * @return Anchor to find more schools
     */
    protected Anchor makeFindSchoolsLink(State state) {
        String visibleText = "Find " + state.getLongName() + " schools";
        UrlBuilder url_builder = new UrlBuilder(UrlBuilder.RESEARCH, state);
        return new Anchor(url_builder.toString(), visibleText, _anchorStyleClass);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public ITableDao getTableDao() {
        return _tableDao;
    }

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}
