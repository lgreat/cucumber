package gs.web.admin.gwt.server;

import gs.web.BaseTestCase;
import gs.web.admin.gwt.client.TableData;
import org.springframework.jdbc.core.JdbcOperations;
import org.easymock.MockControl;

import java.util.*;
import java.io.*;

public class TableCopyServiceSaTest extends BaseTestCase {
    private TableCopyServiceImpl tableCopyService;
    private MockControl jdbcMock;
    private JdbcOperations context;

    protected void setUp() throws Exception {
        tableCopyService = new TableCopyServiceImpl();
        jdbcMock = MockControl.createControl(JdbcOperations.class);
        context = (JdbcOperations) jdbcMock.getMock();
        tableCopyService.setJdbcContext(context);
    }

    public void testGetTables() {
        List resultSet = new ArrayList();
        resultSet.add(new HashMap() {
            {
                put(TableCopyServiceImpl.DATABASE_COLUMN, "gs_schooldb");
                put(TableCopyServiceImpl.TABLE_COLUMN, "table1");
            }
        });
        resultSet.add(new HashMap() {
            {
                put(TableCopyServiceImpl.DATABASE_COLUMN, "gs_schooldb");
                put(TableCopyServiceImpl.TABLE_COLUMN, "table2");
            }
        });
        resultSet.add(new HashMap() {
            {
                put(TableCopyServiceImpl.DATABASE_COLUMN, "_az");
                put(TableCopyServiceImpl.TABLE_COLUMN, "az_table1");
            }
        });
        resultSet.add(new HashMap() {
            {
                put(TableCopyServiceImpl.DATABASE_COLUMN, "_az");
                put(TableCopyServiceImpl.TABLE_COLUMN, "az_table2");
            }
        });

        jdbcMock.expectAndReturn(context.queryForList(TableCopyServiceImpl.TABLE_LIST_QUERY), resultSet);
        jdbcMock.replay();

        TableData tableData = tableCopyService.getTables(TableData.DEV_TO_STAGING);

        assertEquals("Expected 2 databases", 2, tableData.getDatabaseTables().size());
        assertEquals("Unexpected direction", TableData.DEV_TO_STAGING, tableData.getDirection());

        jdbcMock.verify();
    }


    public void testGenerateCopyCommand() {
        TableData.DatabaseDirection direction = TableData.DEV_TO_STAGING;
        String table1 = "gs_schooldb.table1";
        String table2 = "gs_schooldb.table2";
        String table3 = "_az.aztable1";
        String copyCommand = tableCopyService.generateCopyCommand(direction,
                Arrays.asList(new String[]{table1, table2, table3}));
        System.out.println("'" + copyCommand + "'");
        assertTrue("Expected copy command", copyCommand.startsWith(TableCopyServiceImpl.COPY_TABLES_COMMAND));
        assertTrue("Expected dev as from database", copyCommand.matches(".* --fromhost " + direction.getSource() + " .*"));
        assertTrue("Expected staging as to database", copyCommand.matches(".* --tohost " + direction.getTarget() + " .*"));
        String expectedTableList = table1 + "," + table2 + "," + table3;
        assertTrue("Unexpected table list", copyCommand.matches(".* --tablelist " + expectedTableList + " .*"));
    }

    public void testGenerateProductionToDevWikiText() {
        TableData.DatabaseDirection direction = TableData.PRODUCTION_TO_DEV;
        String table1 = "gs_schooldb.table1";
        String table2 = "gs_schooldb.table2";
        String table3 = "_az.aztable1";
        String wikiText = tableCopyService.generateWikiText(direction,
                Arrays.asList(new String[]{table1, table2, table3}));

        String expectedWikiText = "|Who/Jira|Db|Table|live -> dev|dev -> staging|staging -> live|Notes|\n" +
                "| ??/GS-???? | gs_schooldb | table1 | done |  |  |  |\n" +
                "| ??/GS-???? | gs_schooldb | table2 | done |  |  |  |\n" +
                "| ??/GS-???? | _az | aztable1 | done |  |  |  |\n";

        assertEquals("Unexpected wiki text for production to dev", expectedWikiText, wikiText);
    }

    public void testGenerateDevToStagingWikiText() {
        TableData.DatabaseDirection direction = TableData.DEV_TO_STAGING;
        String table1 = "gs_schooldb.table1";
        String table2 = "gs_schooldb.table2";
        String table3 = "_az.aztable1";
        String wikiText = tableCopyService.generateWikiText(direction,
                Arrays.asList(new String[]{table1, table2, table3}));

        String expectedWikiText = "|Who/Jira|Db|Table|live -> dev|dev -> staging|staging -> live|Notes|\n" +
                "| ??/GS-???? | gs_schooldb | table1 | done | done |  |  |\n" +
                "| ??/GS-???? | gs_schooldb | table2 | done | done |  |  |\n" +
                "| ??/GS-???? | _az | aztable1 | done | done |  |  |\n";

        assertEquals("Unexpected wiki text for dev to staging", expectedWikiText, wikiText);
    }
//
//    public void testParseCommandOutput() {
//        String errors = "Skipping database.table1\n" +
//                "Skipping database.table2\n";
//
//        String expectedErrorText = "The following table(s) failed to copy:\n" +
//                "\tdatabase.table1\n" +
//                "\tdatabase.table2\n";
//
//        assertEquals("Unexpected error text", expectedErrorText, tableCopyService.parseCommandOutput(errors));
//    }
}
