package gs.web.admin.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.*;


/**
 * The TableCopyGWTPanel is laid out like this:
 * <verticalPanel name="mainPanel">
 *   <label name="errorMessage"/>
 *   <verticalPanel name="targetChooser">
 *     <radioButton name="selectDev"/>
 *     <radioButton name="selectProd"/>
 *   </verticalPanel>
 *   <horizontalPanel name="tableChooser">
 *     <tree name="tableTree"/>
 *     <verticalPanel name="tableLister">
 *       <listBox name="tableList"/>
 *       <button name="removeButton"/>
 *       <button name="submitButton"/>
 *       <image name="waiting"/>
 *       <html name="successText"/>
 *       <textArea name="wikiText"/>
 *     </verticalPanel>
 *   </horizontalPanel>
 * </verticalPanel>
 */
public class TableCopyGWTPanel implements EntryPoint {
    private List _selectedTables = new ArrayList();
    private TableData.DatabaseDirection _direction;

    // gui panels
    VerticalPanel mainPanel = new VerticalPanel();
    VerticalPanel targetChooser = new VerticalPanel();
    HorizontalPanel tableChooser = new HorizontalPanel();
    VerticalPanel tableLister = new VerticalPanel();

    // gui widgets
    Tree tableTree = new Tree();
    ListBox tableList = new ListBox();
    Button addButton = new Button();
    Button submitButton = new Button("Copy tables");
    Button removeButton = new Button("Remove selected tables from list");
    Button overrideButton = new Button("Warnings be damned! Copy away!");
    RadioButton selectDev = new RadioButton("source", TableData.DEV_TO_STAGING.label);
    RadioButton selectProd = new RadioButton("source", TableData.PRODUCTION_TO_DEV.label);
    HTML errorMessage = new HTML();
    TextArea wikiText = new TextArea();
    HTML successText = new HTML("Success! Add (or overwrite) the above lines (not including the header line) to <a href=\"http://wiki.greatschools.net/bin/view/Greatschools/TableToMove\" target=\"_blank\">TableToMove</a>");
    Image waiting = new Image("/res/img/admin/waiting_head.gif");

    public void onModuleLoad() {
        initializeLayout();

        final TableCopyServiceAsync service = TableCopyService.App.getInstance();

        initializeClickListeners(service);
    }

    private void initializeLayout() {
        tableList.setSize("300", "300");
        tableList.setVisibleItemCount(10);

        wikiText.setCharacterWidth(80);

        tableLister.setSpacing(5);

        errorMessage.setVisible(false);
        errorMessage.setStyleName("error");
        mainPanel.add(errorMessage);
        overrideButton.setVisible(false);
        mainPanel.add(overrideButton);

        targetChooser.add(selectDev);
        targetChooser.add(selectProd);
        mainPanel.add(targetChooser);

        mainPanel.add(tableChooser);


        RootPanel.get().add(mainPanel);
    }

    private void initializeClickListeners(final TableCopyServiceAsync service) {
        selectDev.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                _direction = TableData.DEV_TO_STAGING;
                clearInputs();
                targetChooser.add(waiting);
                service.getTables(_direction, new TableLoadCallback());
            }
        });
        
        selectProd.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                _direction = TableData.PRODUCTION_TO_DEV;
                clearInputs();
                targetChooser.add(waiting);
                service.getTables(_direction, new TableLoadCallback());
            }
        });

        removeButton.addClickListener(new ClickListener(){
            public void onClick(Widget sender) {
                for (int i=0; i<tableList.getItemCount(); i++) {
                    if (tableList.isItemSelected(i)) {
                        _selectedTables.remove(tableList.getItemText(i));
                    }
                }
                populateTableList();
            }
        });

        submitButton.addClickListener(new CopyTablesListener(service, false));

        overrideButton.addClickListener(new CopyTablesListener(service, true));
    }

    private void addDatabasesToTree(TableData tableData) {
        clearInputs();

        tableTree.addTreeListener(new TreeListener() {
            public void onTreeItemSelected(TreeItem item) {
                if (item.getChildCount() == 0) {
                    // must be a leaf
                    String databaseDotTableName = item.getParentItem().getText() + "." + item.getText();
                    if (!_selectedTables.contains(databaseDotTableName)) {
                        _selectedTables.add(databaseDotTableName);
                        populateTableList();
                    }
                }
            }

            public void onTreeItemStateChanged(TreeItem item) {}
        });

        tableList.setMultipleSelect(true);
        List databases = tableData.getDatabaseTables();
        for (Iterator iterator = databases.iterator(); iterator.hasNext();) {
            TableData.DatabaseTables databaseTables = (TableData.DatabaseTables) iterator.next();
            String databaseName = databaseTables.getDatabaseName();
            TreeItem database = new TreeItem(databaseName);
            List tables = databaseTables.getTables();
            for (Iterator tableIterator = tables.iterator(); tableIterator.hasNext();) {
                String tableName = (String) tableIterator.next();
                TreeItem tableItem = new TreeItem(tableName);
                database.addItem(tableItem);
            }
            tableTree.addItem(database);
        }
        submitButton.setText("Copy selected tables from " + tableData.getDirection().getLabel());
        tableLister.add(tableList);
        tableLister.add(removeButton);
        tableLister.add(submitButton);

        tableChooser.add(tableTree);
        tableChooser.add(tableLister);
    }

    private void populateTableList() {
        tableList.clear();
        for (Iterator iterator = _selectedTables.iterator(); iterator.hasNext();) {
            String table = (String) iterator.next();
            tableList.addItem(table);
        }
    }

    private void clearInputs() {
        // remove widgets
        tableChooser.remove(tableTree);
        tableChooser.remove(tableLister);

        tableLister.remove(tableList);
        tableLister.remove(submitButton);
        tableLister.remove(removeButton);
        tableLister.remove(wikiText);

        // reset input values
        _selectedTables.clear();

        // reset widgets
        tableList.clear();
        tableTree.clear();
        wikiText.setText("");

        tableTree = new Tree();
        tableTree.setWidth("400");
    }

    private class TableLoadCallback implements AsyncCallback {
        public void onFailure(Throwable caught) {
            targetChooser.remove(waiting);
            errorMessage.setText(caught.getMessage());
            errorMessage.setVisible(true);
        }

        public void onSuccess(Object result) {
            targetChooser.remove(waiting);
            addDatabasesToTree((TableData) result);
        }
    }

    private class CopyTablesCallback implements AsyncCallback {
        public void onFailure(Throwable caught) {
            tableLister.remove(waiting);
            String error = caught.getMessage();
            errorMessage.setHTML(error);
            errorMessage.setVisible(true);
            // todo: add button to override error message
            if (error.startsWith(TableCopyService.TABLES_FOUND_IN_TABLES_TO_MOVE_ERROR) ||
                    error.startsWith(TableCopyService.TABLES_NOT_YET_MOVED_ERROR)) {
                overrideButton.setVisible(true);
            }
        }

        public void onSuccess(Object result) {
            tableLister.remove(waiting);
            errorMessage.setText("");
            wikiText.setVisibleLines(_selectedTables.size() + 3);
            wikiText.setText((String) result);
            tableLister.remove(waiting);
            tableLister.add(successText);
            tableLister.add(wikiText);
        }
    }

    private class CopyTablesListener implements ClickListener {
        private TableCopyServiceAsync _service;
        private boolean _override;

        public CopyTablesListener(TableCopyServiceAsync service, boolean overrideWarnings) {
            _service = service;
            _override = overrideWarnings;
        }

        public void onClick(Widget sender) {
            String[] tables = new String[tableList.getItemCount()];
            for (int i = 0; i < tables.length; i++) {
                tables[i] = tableList.getItemText(i);
            }
            errorMessage.setVisible(false);
            overrideButton.setVisible(false);
            tableLister.remove(successText);
            tableLister.remove(wikiText);
            tableLister.add(waiting);
            _service.copyTables(_direction, tables, _override, new CopyTablesCallback());
//                waiting.setVisible(false);
        }
    }
}
