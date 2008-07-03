/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: TableMoverCommand.java,v 1.2 2008/07/03 01:01:40 thuss Exp $
 */
package gs.web.admin.database;

/**
 * Backing object for #TableMoverController
 * @author thuss
 */
public class TableMoverCommand {
    private String _target;
    private String[] _tablesets;
    private String[] _states;
    private String _jira;
    private String _initials;
    private String _notes;
    private String[] _tables;
    private String mode;

    public String getTarget() {
        return _target;
    }

    public void setTarget(String target) {
        _target = target;
    }

    public String[] getTablesets() {
        return _tablesets;
    }

    public void setTablesets(String[] tablesets) {
        _tablesets = tablesets;
    }

    public String[] getStates() {
        return _states;
    }

    public void setStates(String[] states) {
        _states = states;
    }

    public String getJira() {
        return _jira;
    }

    public void setJira(String jira) {
        _jira = jira;
    }

    public String getInitials() {
        return _initials;
    }

    public void setInitials(String initials) {
        _initials = initials;
    }

    public String getNotes() {
        return _notes;
    }

    public void setNotes(String notes) {
        _notes = notes;
    }

    public String[] getTables() {
        return _tables;
    }

    public void setTables(String[] tables) {
        _tables = tables;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}