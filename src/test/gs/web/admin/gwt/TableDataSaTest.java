package gs.web.admin.gwt;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

import gs.web.admin.gwt.client.TableData;

public class TableDataSaTest extends TestCase {
    public void testLoadTableData() {
        TableData tableData = new TableData(TableData.DEV_TO_STAGING);
        tableData.addDatabase("test", Arrays.asList(new String[]{"table1", "table2", "table3"}));
        tableData.addDatabase("another", Arrays.asList(new String[]{"table1", "table2"}));

        assertEquals("Unexpected source database", "dev", tableData.getDirection().getSource());
        List databases = tableData.getDatabaseTables();
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

        List databases = tableData.getDatabaseTables();
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
}
