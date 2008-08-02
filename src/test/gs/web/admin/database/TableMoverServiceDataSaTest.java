package gs.web.admin.database;

import junit.framework.TestCase;

import java.util.*;

public class TableMoverServiceDataSaTest extends TestCase {
    public void testLoadTableData() {
        TableMoverServiceData tableData = new TableMoverServiceData(TableMoverServiceData.DEV_TO_STAGING);
        tableData.addDatabase("test", Arrays.asList("table1", "table2", "table3"));
        tableData.addDatabase("another", Arrays.asList("table1", "table2"));

        assertEquals("Unexpected source database", "dev", tableData.getDirection().getSource());
        List<TableMoverServiceData.DatabaseTables> databases = tableData.getDatabasesAndTables();
        assertEquals("Unexpected number of databases", 2, databases.size());

        TableMoverServiceData.DatabaseTables testTableList = databases.get(0);
        assertEquals("Unexpected database name", "test", testTableList.getDatabaseName());
        assertEquals("Unexpected number of tables in test database", 3, testTableList.getTables().size());
        assertEquals("Unexpected first table name", "table1", testTableList.getTables().get(0));
        assertEquals("Unexpected second table name", "table2", testTableList.getTables().get(1));
        assertEquals("Unexpected third table name", "table3", testTableList.getTables().get(2));

        TableMoverServiceData.DatabaseTables anotherTableList = databases.get(1);
        assertEquals("Unexpected database name", "another", anotherTableList.getDatabaseName());
        assertEquals("Unexpected number of tables in another database", 2, anotherTableList.getTables().size());
    }

    public void testLoadTablesIncrementally() {
        TableMoverServiceData tableData = new TableMoverServiceData(TableMoverServiceData.DEV_TO_STAGING);
        tableData.addDatabaseAndTable("test", "table1");
        tableData.addDatabaseAndTable("test", "table2");
        tableData.addDatabaseAndTable("test", "table3");
        tableData.addDatabaseAndTable("another", "table1");
        tableData.addDatabaseAndTable("another", "table2");

        List<TableMoverServiceData.DatabaseTables> databases = tableData.getDatabasesAndTables();
        assertEquals("Unexpected number of databases", 2, databases.size());

        TableMoverServiceData.DatabaseTables testTableList = databases.get(0);
        assertEquals("Unexpected database name", "test", testTableList.getDatabaseName());
        assertEquals("Unexpected number of tables in test database", 3, testTableList.getTables().size());
        assertEquals("Unexpected first table name", "table1", testTableList.getTables().get(0));
        assertEquals("Unexpected second table name", "table2", testTableList.getTables().get(1));
        assertEquals("Unexpected third table name", "table3", testTableList.getTables().get(2));

        TableMoverServiceData.DatabaseTables anotherTableList = databases.get(1);
        assertEquals("Unexpected database name", "another", anotherTableList.getDatabaseName());
        assertEquals("Unexpected number of tables in another database", 2, anotherTableList.getTables().size());
    }

    public void testDatabaseDirectionEquals() {
        TableMoverServiceData.DatabaseDirection first = new TableMoverServiceData.DatabaseDirection("a", "b", "c");
        TableMoverServiceData.DatabaseDirection second = new TableMoverServiceData.DatabaseDirection("a", "b", "c");

        assertEquals("directions with the same field values should be equal", first, second);

        TableMoverServiceData.DatabaseDirection third = new TableMoverServiceData.DatabaseDirection("a", "b", "cc");

        assertTrue("directions with different field values should not be equal", !first.equals(third));
    }

    public void testGetDatabase() {
        TableMoverServiceData tableData = new TableMoverServiceData(TableMoverServiceData.DEV_TO_STAGING);
        tableData.addDatabaseAndTable("test", "table1");
        tableData.addDatabaseAndTable("test", "table2");
        tableData.addDatabaseAndTable("test", "table3");
        tableData.addDatabaseAndTable("another", "table1");
        tableData.addDatabaseAndTable("another", "table2");

        TableMoverServiceData.DatabaseTables testDatabase = tableData.getDatabase("test");
        assertEquals("Unexpected database name", "test", testDatabase.getDatabaseName());
        TableMoverServiceData.DatabaseTables anotherDatabase = tableData.getDatabase("another");
        assertEquals("Unexpected database name", "another", anotherDatabase.getDatabaseName());
    }

    public void testFilterDatabases() {
        TableMoverServiceData tableData = new TableMoverServiceData(TableMoverServiceData.PRODUCTION_TO_DEV);
        tableData.addDatabaseAndTable("test", "table1");
        tableData.addDatabaseAndTable("test", "table2");
        tableData.addDatabaseAndTable("test", "table3");
        tableData.addDatabaseAndTable("another", "table1");
        tableData.addDatabaseAndTable("another", "table2");

        tableData.filterDatabases(Arrays.asList("test"));
        assertNull("Expected test database to have been removed", tableData.getDatabase("test"));

        List<TableMoverServiceData.DatabaseTables> databasesAndTables = tableData.getDatabasesAndTables();
        for (TableMoverServiceData.DatabaseTables databasesAndTable : databasesAndTables) {
            if ("test".equals(databasesAndTable.getDatabaseName())) {
                fail("Expected test database to have been removed from database list");
            }
        }
    }

    public void testFilterTables() {
        TableMoverServiceData tableData = new TableMoverServiceData(TableMoverServiceData.DEV_TO_STAGING);
        tableData.addDatabaseAndTable("test", "table1");
        tableData.addDatabaseAndTable("test", "table2");
        tableData.addDatabaseAndTable("test", "table3");
        tableData.addDatabaseAndTable("another", "table1");
        tableData.addDatabaseAndTable("another", "table2");

        Map<String, HashSet<String>> tablesToKeep = new HashMap<String, HashSet<String>>();
        tablesToKeep.put("test", new HashSet<String>() {{add("table1");add("table3");}});
        tablesToKeep.put("another", new HashSet<String>() {{add("table2");}});

        tableData.filterTables(tablesToKeep);

        List<String> testDatabaseTables = tableData.getDatabase("test").getTables();
        assertEquals("Unexpected number of tables in test database", 2, testDatabaseTables.size());
        assertFalse("Expected table2 to have been removed from test", testDatabaseTables.contains("table2"));
        List<String> anotherDatabaseTables = tableData.getDatabase("another").getTables();
        assertEquals("Unexpected number of tables in another database", 1, anotherDatabaseTables.size());
        assertFalse("Expected table2 to have been removed from another", anotherDatabaseTables.contains("table1"));
        assertEquals("Unexpected table in another database", "table2", anotherDatabaseTables.get(0));

        List<TableMoverServiceData.DatabaseTables> databasesAndTables = tableData.getDatabasesAndTables();
        for (TableMoverServiceData.DatabaseTables databaseTables : databasesAndTables) {
            if ("test".equals(databaseTables.getDatabaseName())) {
                assertEquals("Unexpected number of tables in test database", 2, databaseTables.getTables().size());
            }
            if ("another".equals(databaseTables.getDatabaseName())) {
                assertEquals("Unexpected number of tables in another database", 1, databaseTables.getTables().size());
            }
        }
    }
}
