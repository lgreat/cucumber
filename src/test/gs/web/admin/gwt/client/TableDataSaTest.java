package gs.web.admin.gwt.client;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

public class TableDataSaTest extends TestCase {
    public void testLoadTableData() {
        TableData tableData = new TableData(TableData.DEV_TO_STAGING);
        tableData.addDatabase("test", Arrays.asList(new String[]{"table1", "table2", "table3"}));
        tableData.addDatabase("another", Arrays.asList(new String[]{"table1", "table2"}));

        assertEquals("Unexpected source database", "dev", tableData.getDirection().getSource());
        List databases = tableData.getDatabaseNames();
        assertEquals("Unexpected number of databases", 2, databases.size());
        assertEquals("Unexpected database name", "test", databases.get(0));
        assertEquals("Unexpected database name", "another", databases.get(1));
        List testTableList = tableData.getTablesForDatabase((String) databases.get(0));
        assertEquals("Unexpected number of tables in test database", 3, testTableList.size());
        assertEquals("Unexpected first table name", "table1", testTableList.get(0));
        assertEquals("Unexpected second table name", "table2", testTableList.get(1));
        assertEquals("Unexpected third table name", "table3", testTableList.get(2));
        List anotherTableList = tableData.getTablesForDatabase((String) databases.get(1));
        assertEquals("Unexpected number of tables in another database", 2, anotherTableList.size());
    }
}
