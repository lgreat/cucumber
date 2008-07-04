/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: TableMoverCommand.java,v 1.3 2008/07/04 00:11:06 thuss Exp $
 */
package gs.web.admin.database;

import gs.web.admin.gwt.client.TableData;

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

    public TableData.DatabaseDirection getDirection() {
        TableData.DatabaseDirection direction = null;
        if ("dev".equals(_target)) {
            direction = TableData.PRODUCTION_TO_DEV;
        } else if ("staging".equals(_target)) {
            direction = TableData.DEV_TO_STAGING;
        }
        return direction;
    }
}