package gs.web.admin.database;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlUtil;
import gs.web.admin.gwt.server.TableCopyServiceImpl;
import gs.web.admin.gwt.client.ServiceException;
import gs.web.admin.gwt.client.TableData;
import gs.data.state.StateManager;

import java.util.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Controller to handle moving tables
 *
 * @author thuss
 */
public class TableMoverController extends SimpleFormController {

    public static final String BEAN_ID = "/admin/database/tableMover.page";
    protected String _previewView;
    protected String _errorView;
    protected StateManager _stateManager;
    protected TableCopyServiceImpl _tableCopyService;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        if (!new UrlUtil().isDevEnvironment(sessionContext.getHostName())) {
            // Only allowed to access from dev, otherwise, this error
            return new ModelAndView(_errorView);
        } else {
            request.setAttribute("allTableSets", getAllTableSets());
            request.setAttribute("allStates", _stateManager.getListByAbbreviations());
            return super.handleRequest(request, response);
        }
    }

    protected boolean isFormChangeRequest(HttpServletRequest request, Object command) {
        TableMoverCommand cmd = (TableMoverCommand) command;
        return "edit".equals(cmd.getMode());
    }

    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) {
        TableMoverCommand cmd = (TableMoverCommand) command;
        if ("move".equals(cmd.getMode())) {
            if (cmd.getTables().length == 0) {
                errors.reject("tables", "You must select at least one table to move.");
            }
        } else {
            if (cmd.getTablesets().length == 0) {
                errors.reject("tablesets", "You must select at least one table set.");
            }
            if (cmd.getStates().length == 0) {
                errors.reject("states", "You must select at least one state.");
            }
            if (!errors.hasErrors()) {
                List<String> tablesFilteredOut = new ArrayList<String>();
                cmd.setTables(processTableSets(cmd.getTablesets(), cmd.getStates(), cmd.getDirection(), tablesFilteredOut));
                if (cmd.getTables().length == 0) {
                    errors.reject("tablesets", "No tables left to move after filtering tables against blacklist and whitelist.");
                }
            }
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws IOException, ServiceException {
        TableMoverCommand cmd = (TableMoverCommand) command;
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(getCommandName(), cmd);
        if ("move".equals(cmd.getMode())) {
            try {
                model.put("wikiText", _tableCopyService.copyTables(cmd.getDirection(), cmd.getTables(),
                        true, cmd.getInitials(), cmd.getJira(), cmd.getNotes()));
            } catch (Exception e) {
                Writer result = new StringWriter();
                e.printStackTrace(new PrintWriter(result));
                model.put("errors", e.getMessage() + "\n\n" + result.toString());
            }
            return new ModelAndView(getSuccessView(), model);
        } else {
            model.put("warnings", _tableCopyService.checkWikiForSelectedTables(cmd.getDirection(), Arrays.asList(cmd.getTables())));
            return new ModelAndView(getPreviewView(), model);
        }
    }

    /**
     * Process the table sets down to tables and then filter out tables based on black and white lists
     *
     * @param tableSets
     * @param direction
     * @param states
     * @return Array of database.tablesname strings
     */
    protected String[] processTableSets(String[] tableSets, String[] states, TableData.DatabaseDirection direction, List<String> tablesFilteredOut) {
        Set<String> tables = new TreeSet<String>();
        // Reduce to unique table names
        for (String tableSet : tableSets) {
            tables.addAll(Arrays.asList(tableSet.split(",")));
        }

        // Check if we're going to be processing all states
        if (Arrays.asList(states).contains("")) {
            List<String> allStates = _stateManager.getSortedAbbreviations();
            states = allStates.toArray(new String[allStates.size()]);
        }

        // Add states to the tables that need it        
        for (String table : new TreeSet<String>(tables)) {
            if (!table.contains(".")) {
                for (String state : states) {
                    tables.add("_" + state.toLowerCase() + "." + table);
                }
                // Remove the no-state version
                tables.remove(table);
            }
        }
        return _tableCopyService.filter(direction, tables.toArray(new String[tables.size()]), tablesFilteredOut);
    }

    protected boolean isFormSubmission(HttpServletRequest request) {
        boolean submission = false;
        if (request.getParameter("mode") != null) {
            submission = true;
        }
        return submission;
    }

    private Map<String, String> getAllTableSets() {
        // Tables without a database prefix are assumed to be state specific.
        Map<String, String> tableSets = new LinkedHashMap<String, String>();
        tableSets.put("gs_schooldb.configuation,TestDataSet,TestDataSchoolValue,city_rating,district_rating", "All ratings");
        tableSets.put("gs_schooldb.configuation,TestDataSet,TestDataSchoolValue", "School ratings");
        tableSets.put("city_rating", "City ratings");
        tableSets.put("district_rating", "District ratings");
        tableSets.put("school,district,census_data_set,census_data_school_value,census_data_district_value,pq,pq_volunteer", "Public Directory (with census)");
        tableSets.put("school,district,pq,pq_volunteer", "Public Directory (no census)");
        tableSets.put("school,census_data_set,census_data_school_value,pq,pq_volunteer", "Private Directory: Schools only (with census)");
        tableSets.put("school,pq,pq_volunteer", "Private Directory: Schools only (no census)");
        tableSets.put("gs_schooldb.census_data_set_file,gs_schooldb.DataFile,gs_schooldb.DataLoad,us_geo.city,nearby", "Directory Copy Up (with census)");
        tableSets.put("gs_schooldb.DataFile,gs_schooldb.DataLoad,us_geo.city,nearby", "Directory Copy Up (no census)");
        tableSets.put("school,pq,pq_volunteer", "Schools and ESP");
        tableSets.put("nearby", "Nearby");
        tableSets.put("pq,pq_volunteer", "ESP");
        tableSets.put("school,nearby", "School and Nearby");
        tableSets.put("school", "Schools only (no census)");
        tableSets.put("district", "Districts only (no census)");
        tableSets.put("us_geo.city", "Cities");
        tableSets.put("census_data_set,census_data_school_value,census_data_district_value", "School and District Census");
        tableSets.put("census_data_set,census_data_school_value", "School Census");
        tableSets.put("gs_schooldb.census_data_set_file,gs_schooldb.DataFile,gs_schooldb.DataLoad", "Census Copy Up");
        tableSets.put("gs_schooldb.test", "Table for testing this tool");
        return tableSets;
    }

    public void setErrorView(String errorView) {
        _errorView = errorView;
    }

    public void setPreviewView(String previewView) {
        _previewView = previewView;
    }

    public String getPreviewView() {
        return _previewView;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public void setTableCopyService(TableCopyServiceImpl tableCopyService) {
        _tableCopyService = tableCopyService;
    }
}