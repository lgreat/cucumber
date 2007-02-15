package gs.web.admin.gwt;

import junit.framework.TestCase;

import java.util.*;

import gs.web.admin.gwt.client.TableData;

public class TableDataSaTest extends TestCase {
    public void testLoadTableData() {
        TableData tableData = new TableData(TableData.DEV_TO_STAGING);
        tableData.addDatabase("test", Arrays.asList(new String[]{"table1", "table2", "table3"}));
        tableData.addDatabase("another", Arrays.asList(new String[]{"table1", "table2"}));

        assertEquals("Unexpected source database", "dev", tableData.getDirection().getSource());
        List databases = tableData.getDatabasesAndTables();
        assertEquals("Unexpected number of databases", 2, databases.size());

        TableData.DatabaseTables testTableList = (TableData.DatabaseTables) databases.get(0);
        assertEquals("Unexpected database name", "test", testTableList.getDatabaseName());
        assertEquals("Unexpected number of tables in test database", 3, testTableList.getTables().size());
        assertEquals("Unexpected first table name", "table1", testTableList.getTables().get(0));
        assertEquals("Unexpected second table name", "table2", testTableList.getTables().get(1));
        assertEquals("Unexpected third table name", "table3", testTableList.getTables().get(2));

        TableData.DatabaseTables anotherTableList = (TableData.DatabaseTables) databases.get(1);
        assertEquals("Unexpected database name", "another", anotherTableList.getDatabaseName());
        assertEquals("Unexpected number of tables in another database", 2, anotherTableList.getTables().size());
    }

    public void testLoadTablesIncrementally() {
        TableData tableData = new TableData(TableData.DEV_TO_STAGING);
        tableData.addDatabaseAndTable("test", "table1");
        tableData.addDatabaseAndTable("test", "table2");
        tableData.addDatabaseAndTable("test", "table3");
        tableData.addDatabaseAndTable("another", "table1");
        tableData.addDatabaseAndTable("another", "table2");

        List databases = tableData.getDatabasesAndTables();
        assertEquals("Unexpected number of databases", 2, databases.size());

        TableData.DatabaseTables testTableList = (TableData.DatabaseTables) databases.get(0);
        assertEquals("Unexpected database name", "test", testTableList.getDatabaseName());
        assertEquals("Unexpected number of tables in test database", 3, testTableList.getTables().size());
        assertEquals("Unexpected first table name", "table1", testTableList.getTables().get(0));
        assertEquals("Unexpected second table name", "table2", testTableList.getTables().get(1));
        assertEquals("Unexpected third table name", "table3", testTableList.getTables().get(2));

        TableData.DatabaseTables anotherTableList = (TableData.DatabaseTables) databases.get(1);
        assertEquals("Unexpected database name", "another", anotherTableList.getDatabaseName());
        assertEquals("Unexpected number of tables in another database", 2, anotherTableList.getTables().size());
    }

    public void testDatabaseDirectionEquals() {
        TableData.DatabaseDirection first = new TableData.DatabaseDirection("a", "b", "c");
        TableData.DatabaseDirection second = new TableData.DatabaseDirection("a", "b", "c");

        assertEquals("directions with the same field values should be equal", first, second);

        TableData.DatabaseDirection third = new TableData.DatabaseDirection("a", "b", "cc");

        assertTrue("directions with different field values should not be equal", !first.equals(third));
    }

    public void testGetDatabase() {
        TableData tableData = new TableData(TableData.DEV_TO_STAGING);
        tableData.addDatabaseAndTable("test", "table1");
        tableData.addDatabaseAndTable("test", "table2");
        tableData.addDatabaseAndTable("test", "table3");
        tableData.addDatabaseAndTable("another", "table1");
        tableData.addDatabaseAndTable("another", "table2");

        TableData.DatabaseTables testDatabase = tableData.getDatabase("test");
        assertEquals("Unexpected database name", "test", testDatabase.getDatabaseName());
        TableData.DatabaseTables anotherDatabase = tableData.getDatabase("another");
        assertEquals("Unexpected database name", "another", anotherDatabase.getDatabaseName());
    }

    public void testFilterDatabases() {
        TableData tableData = new TableData(TableData.PRODUCTION_TO_DEV);
        tableData.addDatabaseAndTable("test", "table1");
        tableData.addDatabaseAndTable("test", "table2");
        tableData.addDatabaseAndTable("test", "table3");
        tableData.addDatabaseAndTable("another", "table1");
        tableData.addDatabaseAndTable("another", "table2");

        tableData.filterDatabases(Arrays.asList(new String[]{"test"}));
        assertNull("Expected test database to have been removed", tableData.getDatabase("test"));

        List databasesAndTables = tableData.getDatabasesAndTables();
        for (Iterator iterator = databasesAndTables.iterator(); iterator.hasNext();) {
            TableData.DatabaseTables databaseTables = (TableData.DatabaseTables) iterator.next();
            if ("test".equals(databaseTables.getDatabaseName())) {
                fail("Expected test database to have been removed from database list");
            }
        }
    }

    public void testFilterTables() {
        TableData tableData = new TableData(TableData.DEV_TO_STAGING);
        tableData.addDatabaseAndTable("test", "table1");
        tableData.addDatabaseAndTable("test", "table2");
        tableData.addDatabaseAndTable("test", "table3");
        tableData.addDatabaseAndTable("another", "table1");
        tableData.addDatabaseAndTable("another", "table2");

        Map tablesToKeep = new HashMap();
        tablesToKeep.put("test", new HashSet() {{add("table1");add("table3");}});
        tablesToKeep.put("another", new HashSet() {{add("table2");}});

        tableData.filterTables(tablesToKeep);

        List testDatabaseTables = tableData.getDatabase("test").getTables();
        assertEquals("Unexpected number of tables in test database", 2, testDatabaseTables.size());
        assertFalse("Expected table2 to have been removed from test", testDatabaseTables.contains("table2"));
        List anotherDatabaseTables = tableData.getDatabase("another").getTables();
        assertEquals("Unexpected number of tables in another database", 1, anotherDatabaseTables.size());
        assertFalse("Expected table2 to have been removed from another", anotherDatabaseTables.contains("table1"));
        assertEquals("Unexpected table in another database", "table2", anotherDatabaseTables.get(0));

        List databasesAndTables = tableData.getDatabasesAndTables();
        for (Iterator iterator = databasesAndTables.iterator(); iterator.hasNext();) {
            TableData.DatabaseTables databaseTables = (TableData.DatabaseTables) iterator.next();
            if ("test".equals(databaseTables.getDatabaseName())) {
                assertEquals("Unexpected number of tables in test database", 2, databaseTables.getTables().size());
            }
            if ("another".equals(databaseTables.getDatabaseName())) {
                assertEquals("Unexpected number of tables in another database", 1, databaseTables.getTables().size());
            }
        }
    }
}
