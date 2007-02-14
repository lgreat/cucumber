package gs.web.admin.gwt.server;

import gs.web.BaseTestCase;
import gs.web.admin.gwt.client.TableData;
import org.springframework.jdbc.core.JdbcOperations;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.util.*;
import java.util.regex.Pattern;
import java.io.IOException;

public class TableCopyServiceSaTest extends BaseTestCase {
    private TableCopyServiceImpl _tableCopyService;
    private MockControl _jdbcMock;
    private JdbcOperations _context;
    private MockControl _httpClientMock;
    private HttpClient _client;

    protected void setUp() throws Exception {
        _tableCopyService = new TableCopyServiceImpl();
        _jdbcMock = MockControl.createControl(JdbcOperations.class);
        _context = (JdbcOperations) _jdbcMock.getMock();
        _tableCopyService.setJdbcContext(_context);
        _httpClientMock = MockClassControl.createControl(HttpClient.class);
        _client = (HttpClient) _httpClientMock.getMock();
        _tableCopyService.setHttpClient(_client);
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

        _jdbcMock.expectAndReturn(_context.queryForList(TableCopyServiceImpl.TABLE_LIST_QUERY), resultSet);
        _jdbcMock.replay();

        TableData tableData = _tableCopyService.getTables(TableData.DEV_TO_STAGING);

        assertEquals("Expected 2 databases", 2, tableData.getDatabaseTables().size());
        assertEquals("Unexpected direction", TableData.DEV_TO_STAGING, tableData.getDirection());

        _jdbcMock.verify();
    }


    public void testGenerateCopyCommand() {
        TableData.DatabaseDirection direction = TableData.DEV_TO_STAGING;
        String table1 = "gs_schooldb.table1";
        String table2 = "gs_schooldb.table2";
        String table3 = "_az.aztable1";
        String copyCommand = _tableCopyService.generateCopyCommand(direction,
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
        String wikiText = _tableCopyService.generateWikiText(direction,
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
        String wikiText = _tableCopyService.generateWikiText(direction,
                Arrays.asList(new String[]{table1, table2, table3}));

        String expectedWikiText = "|Who/Jira|Db|Table|live -> dev|dev -> staging|staging -> live|Notes|\n" +
                "| ??/GS-???? | gs_schooldb | table1 | done | done |  |  |\n" +
                "| ??/GS-???? | gs_schooldb | table2 | done | done |  |  |\n" +
                "| ??/GS-???? | _az | aztable1 | done | done |  |  |\n";

        assertEquals("Unexpected wiki text for dev to staging", expectedWikiText, wikiText);
    }

    public void testParseCommandOutputWithError() {
        String errors = "Skipping table database.table1... not found in ditto.\n" +
                "Skipping table database.table2... not found in ditto.\n";

        String expectedErrorText = TableCopyServiceImpl.TABLE_COPY_FAILURE_HEADER +
                "\t&nbsp;&nbsp;&nbsp;database.table1... not found in ditto." + TableCopyServiceImpl.LINE_BREAK +
                "\t&nbsp;&nbsp;&nbsp;database.table2... not found in ditto." + TableCopyServiceImpl.LINE_BREAK;

        assertEquals("Unexpected error text", expectedErrorText, _tableCopyService.parseCommandOutput(errors));
    }

    public void testParseCommandOutputWithoutError() {
        String errors = "Copying database.table1\n" +
                "Copying database.table2\n";

        assertNull("Expected no error text", _tableCopyService.parseCommandOutput(errors));
    }

    public void testParseEmptyNullCommandOutput() {
        String errors = null;
        assertNull("Expected no error text for null output", _tableCopyService.parseCommandOutput(errors));

        errors = "";
        assertNull("Expected no error text for empty output", _tableCopyService.parseCommandOutput(errors));
    }

    public void testGetHttpClient() {
        assertNotNull("Expected http client to be initialized", _tableCopyService.getHttpClient());
    }

    public void testTablesFoundInTablesToMove() throws IOException {
        final List tableList = Arrays.asList(new String[]{"database1.table1", "database2.table2"});

        GetMethod tablesFoundRequest = new GetMethod(TableCopyServiceImpl.TABLES_TO_MOVE_URL) {
            public String getResponseBodyAsString() throws IOException {
                return generateTableToMovePage(tableList, null);
            }
        };
        _tableCopyService.setRequest(tablesFoundRequest);

        _httpClientMock.expectAndReturn(_client.executeMethod(tablesFoundRequest),
                HttpStatus.SC_OK);
        _httpClientMock.replay();

        List tablesFound = _tableCopyService.tablesFoundInTablesToMove(tableList);
        assertEquals("Expected to find 2 tables from list in the page", 2, tablesFound.size());
        assertTrue("Expected to find database1.table1 in TableToMove", tablesFound.contains("database1.table1"));
        assertTrue("Expected to find database2.table2 in TableToMove", tablesFound.contains("database2.table2"));

        _httpClientMock.verify();
    }

    public void testTablesAlreadyCopiedToDev() throws IOException {
        final List tableList = Arrays.asList(new String[]{"database1.table1", "database2.table2"});

        GetMethod oneTableNotDoneRequest = new GetMethod(TableCopyServiceImpl.TABLES_TO_MOVE_URL) {
            public String getResponseBodyAsString() throws IOException {
                return generateTableToMovePage(tableList, new boolean[]{true, false});
            }
        };
        _tableCopyService.setRequest(oneTableNotDoneRequest);

        _httpClientMock.expectAndReturn(_client.executeMethod(oneTableNotDoneRequest),
                HttpStatus.SC_OK, 4);
        _httpClientMock.replay();

        assertEquals("No result expected when table found and marked done", 0,
                _tableCopyService.tablesNotYetCopiedToDev(Arrays.asList(new String[]{"database1.table1"})).size());
        assertEquals("Expected database2.table2 in list, as it's not marked done", "database2.table2",
                _tableCopyService.tablesNotYetCopiedToDev(Arrays.asList(new String[]{"database2.table2"})).get(0));
        assertEquals("Expected database1.table2 in list, as it's not on wiki page", "database1.table2",
                _tableCopyService.tablesNotYetCopiedToDev(Arrays.asList(new String[]{"database1.table2"})).get(0));
        List errorList = _tableCopyService.tablesNotYetCopiedToDev(Arrays.asList(new String[]{"database1.table1", "database2.table2"}));
        assertEquals("Expected one error, as first table is marked done", 1, errorList.size());
        assertEquals("Expected database2.table2 in list, as it's not marked done", "database2.table2", errorList.get(0));

        _httpClientMock.verify();
    }

    public void testGetDatabaseTableMatcher() {
        String database = "xxx";
        String table = "yyy";
        assertEquals("Unexpected regular expression to match database and table on one line", "(?m)^.*xxx.*</td>\\s*<td.*yyy.*$",
                _tableCopyService.getDatabaseTableMatcher(database, table));

        String page = generateTableToMovePage(Arrays.asList(new String[]{"database1.table1", "database2.table2"}), null);
        assertTrue("Expected to match database1.table1",
                Pattern.compile(_tableCopyService.getDatabaseTableMatcher("database1", "table1")).matcher(page).find());
        assertFalse("Shouldn't match database1.table2",
                Pattern.compile(_tableCopyService.getDatabaseTableMatcher("database1", "table2")).matcher(page).find());
    }

    public void testGetDatabaseTableCopiedToDevMatcher() {
        String database = "xxx";
        String table = "yyy";
        assertEquals("Unexpected regular expression to match database and table on one line with live -> dev marked done",
                "(?m)^.*xxx.*</td>\\s*<td.*?yyy.*?</td>\\s*<td.*?done(.*</td>\\s*<td){3}.*$",
                _tableCopyService.getDatabaseTableCopiedToDevMatcher(database, table));

        String page = generateTableToMovePage(Arrays.asList(new String[]{"database1.table1", "database2.table2"}), new boolean[]{true, false});

        assertTrue("Expected to match database1.table1",
                Pattern.compile(_tableCopyService.getDatabaseTableCopiedToDevMatcher("database1", "table1")).matcher(page).find());
        assertFalse("Shouldn't match combination not marked done",
                Pattern.compile(_tableCopyService.getDatabaseTableCopiedToDevMatcher("database2", "table2")).matcher(page).find());
        assertFalse("Shouldn't match combination not in list",
                Pattern.compile(_tableCopyService.getDatabaseTableCopiedToDevMatcher("database1", "table2")).matcher(page).find());
    }

    public void testCheckWikiForSelectedTablesWhenAlreadyCopied() throws IOException {
        final List selectedTables = Arrays.asList(new String[]{"database1.table1", "database2.table2"});

        GetMethod tablesFoundRequest = new GetMethod(TableCopyServiceImpl.TABLES_TO_MOVE_URL) {
            public String getResponseBodyAsString() throws IOException {
                return generateTableToMovePage(selectedTables, null);
            }
        };
        _tableCopyService.setRequest(tablesFoundRequest);

        _httpClientMock.expectAndReturn(_client.executeMethod(tablesFoundRequest),
                HttpStatus.SC_OK);
        _httpClientMock.replay();


        String status = _tableCopyService.checkWikiForSelectedTables(TableData.PRODUCTION_TO_DEV, selectedTables);

        assertEquals("Unexpected response when selected tables have already been copied from live -> dev",
                TableCopyServiceImpl.TABLES_FOUND_IN_TABLES_TO_MOVE_ERROR + selectedTables.get(0) + TableCopyServiceImpl.LINE_BREAK
                        + selectedTables.get(1) + TableCopyServiceImpl.LINE_BREAK,
                status);
    }

    public void testCheckWikiForSelectedTablesWhenNotCopied() throws IOException {
        GetMethod tablesNotFoundRequest = new GetMethod(TableCopyServiceImpl.TABLES_TO_MOVE_URL) {
            public String getResponseBodyAsString() throws IOException {
                return generateTableToMovePage(new ArrayList(), null);
            }
        };
        _tableCopyService.setRequest(tablesNotFoundRequest);

        _httpClientMock.expectAndReturn(_client.executeMethod(tablesNotFoundRequest),
                HttpStatus.SC_OK);
        _httpClientMock.replay();


        String status = _tableCopyService.checkWikiForSelectedTables(TableData.PRODUCTION_TO_DEV, Arrays.asList(new String[]{"database1.table1", "database2.table2"}));

        assertNull("No error expected when selected tables aren't found", status);
    }

    public void testCheckWikiForSelectedTablesFromLiveToDev() throws IOException {
        final List selectedTables = Arrays.asList(new String[]{"database1.table1", "database2.table2"});

        GetMethod tablesFoundRequest = new GetMethod(TableCopyServiceImpl.TABLES_TO_MOVE_URL) {
            public String getResponseBodyAsString() throws IOException {
                return generateTableToMovePage(selectedTables, new boolean[]{true, true});
            }
        };
        _tableCopyService.setRequest(tablesFoundRequest);

        _httpClientMock.expectAndReturn(_client.executeMethod(tablesFoundRequest),
                HttpStatus.SC_OK);
        _httpClientMock.replay();


        String status = _tableCopyService.checkWikiForSelectedTables(TableData.DEV_TO_STAGING, selectedTables);

        assertNull("No error expected when selected tables have already been moved from live -> dev", status);
    }

    public void testCheckWikiForSelectedTablesWhenNotCopiedFromLiveToDev() throws IOException {
        final List selectedTables = Arrays.asList(new String[]{"database1.table1", "database2.table2"});

        GetMethod tablesFoundRequest = new GetMethod(TableCopyServiceImpl.TABLES_TO_MOVE_URL) {
            public String getResponseBodyAsString() throws IOException {
                return generateTableToMovePage(selectedTables, new boolean[]{true, false});
            }
        };
        _tableCopyService.setRequest(tablesFoundRequest);

        _httpClientMock.expectAndReturn(_client.executeMethod(tablesFoundRequest),
                HttpStatus.SC_OK);
        _httpClientMock.replay();


        String status = _tableCopyService.checkWikiForSelectedTables(TableData.DEV_TO_STAGING, selectedTables);

        assertEquals("Unexpected response when selected tables have already been copied from live -> dev",
                TableCopyServiceImpl.TABLES_NOT_YET_MOVED_ERROR + selectedTables.get(1) + TableCopyServiceImpl.LINE_BREAK,
                status);
    }


    private String generateTableToMovePage(List tables, boolean[] liveToDev) {
        StringBuffer tableBuffer = new StringBuffer();
        int count = 0;
        for (Iterator iterator = tables.iterator(); iterator.hasNext(); count++) {
            String databaseAndTable = (String) iterator.next();
            String[] databaseTable = databaseAndTable.split("\\.");
            tableBuffer.append("<tr><td> DR/<a href=\"http://jira.greatschools.net:8080/browse/GS-3033\" TARGET=\"_blank\">GS-3033</a> </td><td> ");
            tableBuffer.append(databaseTable[0]);
            tableBuffer.append("  </td><td> <span style='background : #FFFFCE;'><font color=\"#0000FF\">");
            tableBuffer.append(databaseTable[1]);
            tableBuffer.append("</font></span><a href=\"/bin/edit/Greatschools/");
            tableBuffer.append(databaseTable[1]);
            tableBuffer.append("?topicparent=Greatschools.TableToMove\">?</a>  </td><td>  ");
            if (liveToDev == null || liveToDev[count]) {
                tableBuffer.append("done");
            }
            tableBuffer.append("</td><td>  done  </td><td>  &nbsp;  </td><td> DSTP  </td></tr>\n\n");
        }

        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\"> \n" +
            "<head>\n" +
            " <link href=\"/pub/TWiki/GnuSkin/twikiblue.css\" rel=\"stylesheet\" type=\"text/css\">\n" +
            " <title> Wiki . Greatschools . TableToMove   </title>\n" +
            " <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />\n" +
            "  \n" +
            " <base href=\"http://wiki.greatschools.net/bin/view/Greatschools/TableToMove\" />\n" +
            "</head>\n" +
            "\n" +
            "<body bgcolor=\"white\" text=\"black\" link=\"blue\" alink=\"aqua\" vlink=\"purple\">\n" +
            tableBuffer.toString() +
            "</table>\n" +
            "</body>\n" +
            "</html>";
    }

}
