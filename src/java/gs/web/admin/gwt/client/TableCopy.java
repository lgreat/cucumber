package gs.web.admin.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.*;


public class TableCopy implements EntryPoint {
    private List selectedTables = new ArrayList();
    VerticalPanel mainPanel = new VerticalPanel();
    VerticalPanel targetChooser = new VerticalPanel();
    HorizontalPanel tableChooser = new HorizontalPanel();
    VerticalPanel tableLister = new VerticalPanel();

    List aa = new ArrayList() {{
        add("district");
        add("pq");
        add("pq_volunteer");
        add("private");
        add("school");
    }};
    List ak = new ArrayList() {{
        add("TestDataDistrictValue");
        add("TestDataSchoolValue");
        add("TestDataSet");
        add("TestDataStateValue");
        add("air");
        add("air_desc");
        add("...");
    }};
    List gs_schooldb = new ArrayList() {
        {
            add("DataFile");
            add("DataLoad");
            add("DataSource");
            add("TestDataBreakdown");
            add("TestDataSetFile");
            add("TestDataSubject");
            add("...");
        }
    };
    List new_database = new ArrayList() {
        {
            add("new_table");
            add("another_new_table");
        }
    };

    Map devDatabases = new HashMap() {
        {
            put("_aa", aa);
            put("_ak", ak);
            put("gs_schooldb", gs_schooldb);
            put("new_database", new_database);
        }
    };
    Map productionDatabases = new HashMap() {
        {
            put("_aa", aa);
            put("_ak", ak);
            put("gs_schooldb", gs_schooldb);
        }
    };


    Tree tableTree = new Tree();
    ListBox tableList = new ListBox();
    Button addButton = new Button();
    Button submitButton = new Button("Copy tables");
    Button removeButton = new Button("Remove selected tables from list");
    RadioButton selectDev = new RadioButton("source", TableData.DEV_TO_STAGING.label);
    RadioButton selectProd = new RadioButton("source", TableData.PRODUCTION_TO_DEV.label);
    Label errorMessage = new Label();

    public void onModuleLoad() {
        errorMessage.setVisible(false);
        mainPanel.add(errorMessage);

        targetChooser.add(selectDev);
        targetChooser.add(selectProd);
        mainPanel.add(targetChooser);

        mainPanel.add(tableChooser);

        RootPanel.get().add(mainPanel);

        final TableCopyServiceAsync service = TableCopyService.App.getInstance();
        

        selectDev.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                service.getTables(TableData.DEV_TO_STAGING, new TableLoadCallback());
//                addDatabasesToTable(selectDev.getText(), devDatabases);
            }
        });
        selectProd.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                service.getTables(TableData.PRODUCTION_TO_DEV, new TableLoadCallback());
//                addDatabasesToTable(selectProd.getText(), productionDatabases);
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

    }

    private void addDatabasesToTree(TableData tableData) {
        tableChooser.clear();
        tableLister.remove(tableList);
        tableLister.remove(submitButton);
        tableLister.remove(removeButton);

        tableTree = new Tree();
        tableTree.setWidth("400");
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

        tableList.setWidth("300");
        tableList.clear();
        selectedTables.clear();
        tableList.setVisibleItemCount(10);
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
        submitButton.setText("Copy selected tables from " + tableData.getDirection());
        tableLister.add(tableList);
        tableLister.add(removeButton);
        tableLister.add(submitButton);

        tableChooser.add(tableTree);
        tableChooser.add(tableLister);

    }

    private void addDatabasesToTable(String direction, Map databases) {
        // load from specified direction
        tableChooser.clear();
        tableLister.remove(tableList);
        tableLister.remove(submitButton);
        tableLister.remove(removeButton);

        tableTree = new Tree();
        tableTree.setWidth("400");
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

        tableList.setWidth("300");
        tableList.clear();
        selectedTables.clear();
        tableList.setVisibleItemCount(10);
        tableList.setMultipleSelect(true);
        for (Iterator iterator = databases.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            TreeItem database = new TreeItem(key);
            List list = (List) databases.get(key);
            for (Iterator tableIterator = list.iterator(); tableIterator.hasNext();) {
                String table = (String) tableIterator.next();
                TreeItem tableItem = new TreeItem(table);
                database.addItem(tableItem);
            }
            tableTree.addItem(database);
        }
        submitButton.setText("Copy selected tables from " + direction);
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

    private class TableLoadCallback implements AsyncCallback {
        public void onFailure(Throwable caught) {
            errorMessage.setText(caught.getMessage());
            errorMessage.setVisible(true);
            caught.printStackTrace();
        }

        public void onSuccess(Object result) {
            addDatabasesToTree((TableData) result);
        }
    }
}
