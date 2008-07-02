/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: TableMoverCommand.java,v 1.1 2008/07/02 01:00:10 thuss Exp $
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
}