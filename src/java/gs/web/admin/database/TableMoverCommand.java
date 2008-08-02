/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: TableMoverCommand.java,v 1.7 2008/08/02 22:43:06 thuss Exp $
 */
package gs.web.admin.database;

import gs.web.admin.database.TableMoverServiceData;

/**
 * Backing object for #TableMoverController
 *
 * @author thuss
 */
public class TableMoverCommand {
    private String[] _states = new String[0];
    private String[] _tablesets = new String[0];
    private String[] _tables = new String[0];
    private String[] _tablesFilteredOut = new String[0];
    private String _target;
    private String _jira;
    private String _initials;
    private String _notes;
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

    public TableMoverServiceData.DatabaseDirection getDirection() {
        TableMoverServiceData.DatabaseDirection direction = null;
        if ("dev".equals(_target)) {
            direction = TableMoverServiceData.PRODUCTION_TO_DEV;
        } else if ("staging".equals(_target)) {
            direction = TableMoverServiceData.DEV_TO_STAGING;
        }
        return direction;
    }

    public String[] getTablesFilteredOut() {
        return _tablesFilteredOut;
    }

    public void setTablesFilteredOut(String[] tablesFilteredOut) {
        _tablesFilteredOut = tablesFilteredOut;
    }
}