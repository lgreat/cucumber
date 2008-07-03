package gs.web.admin.database;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlUtil;
import gs.data.state.StateManager;

import java.util.*;

/**
 * Controller to handle moving tables
 */
public class TableMoverController extends SimpleFormController {
    public static final String BEAN_ID = "/admin/database/tableMover.page";

    protected String _previewView;
    protected String _errorView;
    protected StateManager _stateManager;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        if (!new UrlUtil().isDevEnvironment(sessionContext.getHostName())) {
            // Only allowed to access from dev, otherwise, this error
            return new ModelAndView(_errorView);
        } else {
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
                errors.reject("tables", "You must select at least on table to move.");
            }
        } else {
            if (cmd.getTablesets().length == 0) {
                errors.reject("tablesets", "You must select at least one table set.");
            }
            if (cmd.getStates().length == 0) {
                errors.reject("states", "You must select at least one state.");
            }
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) {
        TableMoverCommand cmd = (TableMoverCommand) command;
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(getCommandName(), cmd);
        if ("move".equals(cmd.getMode())) {
            try {
                Thread.sleep(4000);
            } catch (Exception e) {
            }
            return new ModelAndView(getSuccessView(), model);
        } else {
            cmd.setTables(processTableSets(cmd.getTablesets(), cmd.getStates()));
            return new ModelAndView(getPreviewView(), model);
        }
    }

    protected String[] processTableSets(String[] tableSets, String[] states) {
        Set<String> tables = new TreeSet<String>();
        // Reduce to unique table names
        for (String tableSet : tableSets) {
            tables.addAll(Arrays.asList(tableSet.split(",")));
        }

        // Check if we're going to be processing all states
        if (Arrays.asList(states).contains("")) {
            List allStates = _stateManager.getSortedAbbreviations();
            states = (String[]) allStates.toArray(new String[allStates.size()]);
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
        return tables.toArray(new String[tables.size()]);
    }

    protected boolean isFormSubmission(HttpServletRequest request) {
        boolean submission = false;
        if (request.getParameter("mode") != null) {
            submission = true;
        }
        return submission;
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
}