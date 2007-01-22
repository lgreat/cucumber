package gs.web.admin.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.*;


public class TableCopyGWTPanel implements EntryPoint {
    private List selectedTables = new ArrayList();
    private TableData.DatabaseDirection _direction;
    VerticalPanel mainPanel = new VerticalPanel();
    VerticalPanel targetChooser = new VerticalPanel();
    HorizontalPanel tableChooser = new HorizontalPanel();
    VerticalPanel tableLister = new VerticalPanel();

    Tree tableTree = new Tree();
    ListBox tableList = new ListBox();
    Button addButton = new Button();
    Button submitButton = new Button("Copy tables");
    Button removeButton = new Button("Remove selected tables from list");
    RadioButton selectDev = new RadioButton("source", TableData.DEV_TO_STAGING.label);
    RadioButton selectProd = new RadioButton("source", TableData.PRODUCTION_TO_DEV.label);
    Label errorMessage = new Label();
    TextArea wikiText = new TextArea();
    Label wikiLabel = new Label("Success! Add (or overwrite) the following lines to http://wiki.greatschools.net/bin/view/Greatschools/TableToMove");
    Image waiting = new Image();

    public void onModuleLoad() {
        initializeLayout();

        final TableCopyServiceAsync service = TableCopyService.App.getInstance();

        selectDev.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                _direction = TableData.DEV_TO_STAGING;
                service.getTables(_direction, new TableLoadCallback());
            }
        });
        selectProd.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                _direction = TableData.PRODUCTION_TO_DEV;
                service.getTables(_direction, new TableLoadCallback());
            }
        });

        removeButton.addClickListener(new ClickListener(){
            public void onClick(Widget sender) {
                for (int i=0; i<tableList.getItemCount(); i++) {
                    if (tableList.isItemSelected(i)) {
                        selectedTables.remove(tableList.getItemText(i));
                    }
                }
                populateTableList();
            }
        });

        submitButton.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                String[] tables = new String[tableList.getItemCount()];
                for (int i = 0; i < tables.length; i++) {
                    tables[i] = tableList.getItemText(i);
                }
                waiting.setUrl("/res/img/admin/waiting_head.gif");
                tableLister.add(waiting);
                service.copyTables(_direction, tables, new CopyTablesCallback());
            }
        });

    }

    private void addDatabasesToTree(TableData tableData) {
        resetLayout();

        tableTree.addTreeListener(new TreeListener() {
            public void onTreeItemSelected(TreeItem item) {
                if (item.getChildCount() == 0) {
                    // must be a leaf
                    String databaseDotTableName = item.getParentItem().getText() + "." + item.getText();
                    if (!selectedTables.contains(databaseDotTableName)) {
                        selectedTables.add(databaseDotTableName);
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
        for (Iterator iterator = selectedTables.iterator(); iterator.hasNext();) {
            String table = (String) iterator.next();
            tableList.addItem(table);
        }
    }

    private void initializeLayout() {
        errorMessage.setVisible(false);

        tableList.setWidth("300");
        tableList.setVisibleItemCount(10);

        wikiText.setWidth("700");

        tableLister.setSpacing(5);
        
        mainPanel.add(errorMessage);

        targetChooser.add(selectDev);
        targetChooser.add(selectProd);
        mainPanel.add(targetChooser);

        mainPanel.add(tableChooser);


        RootPanel.get().add(mainPanel);
    }

    private void resetLayout() {
        tableChooser.clear();
        wikiText.setText("");
        tableLister.remove(tableList);
        tableLister.remove(submitButton);
        tableLister.remove(removeButton);
        tableLister.remove(wikiText);

        tableList.clear();
        selectedTables.clear();

        tableTree = new Tree();
        tableTree.setWidth("400");
    }

    private class TableLoadCallback implements AsyncCallback {
        public void onFailure(Throwable caught) {
            errorMessage.setText(caught.getMessage());
            errorMessage.setVisible(true);
        }

        public void onSuccess(Object result) {
            addDatabasesToTree((TableData) result);
        }
    }

    private class CopyTablesCallback implements AsyncCallback {
        public void onFailure(Throwable caught) {
            errorMessage.setText(caught.getMessage());
            errorMessage.setVisible(true);
        }

        public void onSuccess(Object result) {
            wikiText.setVisibleLines(selectedTables.size() + 3);
            wikiText.setText((String) result);
            tableLister.remove(waiting);
            tableLister.add(wikiLabel);
            tableLister.add(wikiText);
        }
    }
}
