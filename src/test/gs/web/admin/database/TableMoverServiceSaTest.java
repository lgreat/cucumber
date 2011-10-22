package gs.web.admin.database;

import gs.web.BaseTestCase;
import gs.data.state.State;
import gs.data.state.StateManager;
import org.springframework.jdbc.core.JdbcOperations;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.ArrayUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.io.IOException;

import static org.easymock.classextension.EasyMock.*;

public class TableMoverServiceSaTest extends BaseTestCase {
    private TableMoverService _tableMoverService;
    private JdbcOperations _jdbcMock;
    private HttpClient _httpClientMock;

    protected void setUp() throws Exception {
        _tableMoverService = new TableMoverService();
        _jdbcMock = createMock(JdbcOperations.class);
        _tableMoverService.setJdbcContext(_jdbcMock);
        _httpClientMock = createMock(HttpClient.class);
        _tableMoverService.setHttpClient(_httpClientMock);
        _tableMoverService.setStateManager(new StateManager());
    }

    public void testGetTables() {
        List<Map<String, Object>> resultSet = new ArrayList<Map<String, Object>>();
        resultSet.add(new HashMap<String, Object>() {
            {
                put(TableMoverService.DATABASE_COLUMN, "gs_schooldb");
                put(TableMoverService.TABLE_COLUMN, "table1");
            }
        });
        resultSet.add(new HashMap<String, Object>() {
            {
                put(TableMoverService.DATABASE_COLUMN, "gs_schooldb");
                put(TableMoverService.TABLE_COLUMN, "table2");
            }
        });
        resultSet.add(new HashMap<String, Object>() {
            {
                put(TableMoverService.DATABASE_COLUMN, "_az");
                put(TableMoverService.TABLE_COLUMN, "az_table1");
            }
        });
        resultSet.add(new HashMap<String, Object>() {
            {
                put(TableMoverService.DATABASE_COLUMN, "_az");
                put(TableMoverService.TABLE_COLUMN, "az_table2");
            }
        });

        expect(_jdbcMock.queryForList(TableMoverService.TABLE_LIST_QUERY)).andReturn(resultSet);
        replay(_jdbcMock);

        TableMoverServiceData tableData = _tableMoverService.getTables(TableMoverServiceData.DEV_TO_STAGING);

        assertEquals("Expected 2 databases", 2, tableData.getDatabasesAndTables().size());
        assertEquals("Unexpected direction", TableMoverServiceData.DEV_TO_STAGING, tableData.getDirection());

        verify(_jdbcMock);
    }

    public void testGenerateCopyCommand() {
        TableMoverServiceData.DatabaseDirection direction = TableMoverServiceData.DEV_TO_STAGING;
        String table1 = "gs_schooldb.table1";
        String table2 = "gs_schooldb.table2";
        String table3 = "_az.aztable1";
        String copyCommand = _tableMoverService.generateCopyCommand(direction, Arrays.asList(table1, table2, table3));
        assertTrue("Expected copy command", copyCommand.startsWith(TableMoverService.COPY_TABLES_COMMAND));
        assertTrue("Expected dev as from database", copyCommand.matches(".* --fromhost " + direction.getSource() + " .*"));
        assertTrue("Expected staging as to database", copyCommand.matches(".* --tohost " + direction.getTarget() + " .*"));
        String expectedTableList = table1 + "," + table2 + "," + table3;
        assertTrue("Unexpected table list", copyCommand.matches(".* --tablelist " + expectedTableList + " .*"));
    }

    public void testGenerateBackupCommand() {
        TableMoverServiceData.DatabaseDirection direction = TableMoverServiceData.DEV_TO_STAGING;
        String table1 = "gs_schooldb.table1";
        String table2 = "gs_schooldb.table2";
        String table3 = "_az.aztable1";
        String copyCommand = _tableMoverService.generateBackupCommand(direction,
                Arrays.asList(table1, table2, table3));
        assertTrue("Expected copy command", copyCommand.startsWith(TableMoverService.COPY_TABLES_COMMAND));
        assertTrue("Expected staging as from database", copyCommand.matches(".* --fromhost " + direction.getTarget() + " .*"));
        assertTrue("Expected outdir to be set", copyCommand.matches(".* " + TableMoverService.OUTDIR_FLAG_FOR_BACKUP_COMMAND + " .*"));
        String expectedTableList = table1 + "," + table2 + "," + table3;
        assertTrue("Unexpected table list", copyCommand.matches(".* --tablelist " + expectedTableList + " .*"));
    }

    public void testGenerateProductionToDevWikiText() {
        TableMoverServiceData.DatabaseDirection direction = TableMoverServiceData.PRODUCTION_TO_DEV;
        final StateManager sm = new StateManager();
        final List<State> allStates = sm.getListByAbbreviations();
        final List<State> mostStates = new ArrayList<State>(sm.getListByAbbreviations());
        mostStates.remove(State.CA);
        mostStates.remove(State.WY);
        List<String> tables = new ArrayList<String>() {
            {
                add("gs_schooldb.table1");
                add("gs_schooldb.table2");
                add("_az.aztable1");
                addAll(generateTableListWithStates(allStates, "table3"));
                addAll(generateTableListWithStates(mostStates, "table4"));
            }
        };
        String wikiText = _tableMoverService.generateWikiText(direction, tables, null, null, null);
        String expectedWikiText = "|Who/Jira|Db|Table|live -> dev|dev -> staging|staging -> live|Notes|\n" +
                "| ??/GS-???? | _az | aztable1 | done |  |  |  |\n" +
                "| ??/GS-???? | all -ca -wy | table4 | done |  |  |  |\n" +
                "| ??/GS-???? | all | table3 | done |  |  |  |\n" +
                "| ??/GS-???? | gs_schooldb | table1 | done |  |  |  |\n" +
                "| ??/GS-???? | gs_schooldb | table2 | done |  |  |  |\n";

        assertEquals("Unexpected wiki text for production to dev", expectedWikiText, wikiText);
    }

    private List<String> generateTableListWithStates(List<State> states, String tableName) {
        List<String> tables = new ArrayList<String>();
        for (State state : states) {
            tables.add("_" + state.getAbbreviationLowerCase() + "." + tableName);
        }
        return tables;
    }

    public void testGeneratedWikiTextWithNoStateSpecificTables() {
        TableMoverServiceData.DatabaseDirection direction = TableMoverServiceData.PRODUCTION_TO_DEV;
        List<String> tables = Arrays.asList("gs_schooldb.table1");
        String wikiText = _tableMoverService.generateWikiText(direction, tables, null, null, null);
        String expectedWikiText = "|Who/Jira|Db|Table|live -> dev|dev -> staging|staging -> live|Notes|\n" +
                "| ??/GS-???? | gs_schooldb | table1 | done |  |  |  |\n";

        assertEquals("Unexpected wiki text for production to dev", expectedWikiText, wikiText);
    }

    public void testGenerateDevToStagingWikiText() {
        TableMoverServiceData.DatabaseDirection direction = TableMoverServiceData.DEV_TO_STAGING;
        final StateManager sm = new StateManager();
        final List<State> allStates = sm.getListByAbbreviations();
        final List<State> mostStates = new ArrayList<State>(sm.getListByAbbreviations());
        mostStates.remove(State.CA);
        mostStates.remove(State.WY);
        mostStates.remove(State.OR);
        final List<State> aFewStates = new ArrayList<State>(sm.getListByAbbreviations().subList(3, 7));
        List<String> tables = new ArrayList<String>() {
            {
                add("_ak.table1");
                add("_ak.table2");
                add("_az.aztable1");
                addAll(generateTableListWithStates(allStates, "table3"));
                addAll(generateTableListWithStates(mostStates, "table4"));
                addAll(generateTableListWithStates(aFewStates, "table5"));
            }
        };
        String wikiText = _tableMoverService.generateWikiText(direction, tables, "TH", "GS-1234", "Note123");
        String expectedWikiText = "|Who/Jira|Db|Table|live -> dev|dev -> staging|staging -> live|Notes|\n" +
                "| TH/GS-1234 | _ak | table1 | done | done |  | Note123 |\n" +
                "| TH/GS-1234 | _ak | table2 | done | done |  | Note123 |\n" +
                "| TH/GS-1234 | _az | aztable1 | done | done |  | Note123 |\n" +
                "| TH/GS-1234 | _az | table5 | done | done |  | Note123 |\n" +
                "| TH/GS-1234 | _ca | table5 | done | done |  | Note123 |\n" +
                "| TH/GS-1234 | _co | table5 | done | done |  | Note123 |\n" +
                "| TH/GS-1234 | _ct | table5 | done | done |  | Note123 |\n" +
                "| TH/GS-1234 | all -ca -or -wy | table4 | done | done |  | Note123 |\n" +
                "| TH/GS-1234 | all | table3 | done | done |  | Note123 |\n";
        assertEquals("Unexpected wiki text for dev to staging", expectedWikiText, wikiText);
    }

    public void testParseCommandOutputWithError() {
        String errors = "Skipping table database.table1... not found in ditto.\n" +
                "Skipping table database.table2... not found in ditto.\n";

        String expectedErrorText = TableMoverService.TABLE_COPY_FAILURE_HEADER +
                "\t&#160;&#160;&#160;database.table1... not found in ditto." + TableMoverService.LINE_BREAK +
                "\t&#160;&#160;&#160;database.table2... not found in ditto." + TableMoverService.LINE_BREAK;

        assertEquals("Unexpected error text", expectedErrorText, _tableMoverService.parseCommandOutput(errors));
    }

    public void testParseCommandOutputWithoutError() {
        String errors = "Copying database.table1\n" +
                "Copying database.table2\n";

        assertNull("Expected no error text", _tableMoverService.parseCommandOutput(errors));
    }

    public void testParseEmptyNullCommandOutput() {
        String errors = null;
        assertNull("Expected no error text for null output", _tableMoverService.parseCommandOutput(errors));

        errors = "";
        assertNull("Expected no error text for empty output", _tableMoverService.parseCommandOutput(errors));
    }

    public void testGetHttpClient() {
        assertNotNull("Expected http client to be initialized", _tableMoverService.getHttpClient());
    }

    public void testTablesFoundInTablesToMove() throws IOException {
        List<String> tableList = Arrays.asList("database1.table1", "database2.table2");
        setUpWikiRequest(tableList, null);

        List tablesFound = _tableMoverService.tablesFoundInTablesToMove(tableList);
        assertEquals("Expected to find 2 tables from list in the page", 2, tablesFound.size());
        assertTrue("Expected to find database1.table1 in TableToMove", tablesFound.contains("database1.table1"));
        assertTrue("Expected to find database2.table2 in TableToMove", tablesFound.contains("database2.table2"));

        verify(_httpClientMock);
    }

    public void testTablesAlreadyCopiedToDev() throws IOException {
        final List<String> tableList = Arrays.asList("database1.table1", "database2.table2");

        GetMethod oneTableNotDoneRequest = new GetMethod(TableMoverService.TABLES_TO_MOVE_URL) {
            public String getResponseBodyAsString() throws IOException {
                return generateTableToMovePage(tableList, new String[]{"done", ""});
            }
        };
        _tableMoverService.setRequest(oneTableNotDoneRequest);

        expect(_httpClientMock.executeMethod(oneTableNotDoneRequest)).andReturn(HttpStatus.SC_OK);
        expectLastCall().times(4);
        replay(_httpClientMock);

        assertEquals("No result expected when table found and marked done", 0,
                _tableMoverService.tablesNotYetCopiedToDev(Arrays.asList("database1.table1")).size());
        assertEquals("Expected database2.table2 in list, as it's not marked done", "database2.table2",
                _tableMoverService.tablesNotYetCopiedToDev(Arrays.asList("database2.table2")).get(0));
        assertEquals("Expected database1.table2 in list, as it's not on wiki page", "database1.table2",
                _tableMoverService.tablesNotYetCopiedToDev(Arrays.asList("database1.table2")).get(0));
        List errorList = _tableMoverService.tablesNotYetCopiedToDev(Arrays.asList("database1.table1", "database2.table2"));
        assertEquals("Expected one error, as first table is marked done", 1, errorList.size());
        assertEquals("Expected database2.table2 in list, as it's not marked done", "database2.table2", errorList.get(0));
        verify(_httpClientMock);
    }

    public void testGetDatabaseTableMatcher() {
        String page = generateTableToMovePage(Arrays.asList("database1.table1", "database2.census_data_school_file", "all -database3 -database4.table2"), null);
        assertTrue("Expected to match database1.table1",
                Pattern.compile(_tableMoverService.getDatabaseTableMatcher("database1", "table1")).matcher(page).find());
        assertFalse("Shouldn't match database1.table3",
                Pattern.compile(_tableMoverService.getDatabaseTableMatcher("database1", "table3")).matcher(page).find());
        assertTrue("Expected to match database2.census_data_school_file",
                Pattern.compile(_tableMoverService.getDatabaseTableMatcher("database2", "census_data_school_file")).matcher(page).find());
        assertFalse("Shouldn't match partial database name",
                Pattern.compile(_tableMoverService.getDatabaseTableMatcher("database", "census_data_school_file")).matcher(page).find());
        assertFalse("Shouldn't match partial table name",
                Pattern.compile(_tableMoverService.getDatabaseTableMatcher("database2", "school")).matcher(page).find());
        assertFalse("Shouldn't match database3.table2 because it's excluded with a minus",
                Pattern.compile(_tableMoverService.getDatabaseTableMatcher("database3", "table2")).matcher(page).find());
        assertFalse("Shouldn't match database4.table2 because it's excluded with a minus",
                Pattern.compile(_tableMoverService.getDatabaseTableMatcher("database4", "table2")).matcher(page).find());
        assertTrue("Should match database5.table2 because it falls into the all group",
                Pattern.compile(_tableMoverService.getDatabaseTableMatcher("database5", "table2")).matcher(page).find());
    }

    public void testGetDatabaseTableCopiedToDevMatcher() {
        String page = generateTableToMovePage(Arrays.asList("database1.table1", "database2.table2", "database3.table3",
                "database4.table4", "all -database1 -database2.table5", "all -database1 -database2.table6"),
                new String[]{"done", "N/A", "n/a", "", "done", ""});
        assertTrue("Expected to match database1.table1 marked as done",
                Pattern.compile(_tableMoverService.getDatabaseTableCopiedToDevMatcher("database1", "table1")).matcher(page).find());
        assertTrue("Expected to match database2.table2 marked as N/A",
                Pattern.compile(_tableMoverService.getDatabaseTableCopiedToDevMatcher("database2", "table2")).matcher(page).find());
        assertTrue("Expected to match database3.table3 marked as n/a",
                Pattern.compile(_tableMoverService.getDatabaseTableCopiedToDevMatcher("database3", "table3")).matcher(page).find());
        assertFalse("Shouldn't match combination not marked done",
                Pattern.compile(_tableMoverService.getDatabaseTableCopiedToDevMatcher("database4", "table4")).matcher(page).find());
        assertFalse("Shouldn't match combination not in list",
                Pattern.compile(_tableMoverService.getDatabaseTableCopiedToDevMatcher("database1", "table2")).matcher(page).find());
        assertFalse("Shouldn't match partial database name",
                Pattern.compile(_tableMoverService.getDatabaseTableCopiedToDevMatcher("base3", "table3")).matcher(page).find());
        assertFalse("Shouldn't match partial table name",
                Pattern.compile(_tableMoverService.getDatabaseTableCopiedToDevMatcher("database3", "table")).matcher(page).find());
        assertTrue("Expected to match database9.table5 falls into all group and marked done",
                Pattern.compile(_tableMoverService.getDatabaseTableCopiedToDevMatcher("database9", "table5")).matcher(page).find());
        assertFalse("Shouldn't match database1.table5 because it's minused from all group",
                Pattern.compile(_tableMoverService.getDatabaseTableCopiedToDevMatcher("database1", "table5")).matcher(page).find());
        assertFalse("Shouldn't match database9.table6 not marked done",
                Pattern.compile(_tableMoverService.getDatabaseTableCopiedToDevMatcher("database9", "table6")).matcher(page).find());
        assertFalse("Shouldn't match database1.table6 not marked done",
                Pattern.compile(_tableMoverService.getDatabaseTableCopiedToDevMatcher("database1", "table6")).matcher(page).find());
    }

    public void testCheckWikiForSelectedTablesWhenAlreadyCopied() throws IOException {
        List<String> selectedTables = Arrays.asList("database1.table1", "database2.table2");
        setUpWikiRequest(selectedTables, null);

        String status = _tableMoverService.checkWikiForSelectedTables(TableMoverServiceData.PRODUCTION_TO_DEV, selectedTables);
        assertEquals("Unexpected response when selected tables have already been copied from live -> dev",
                TableMoverService.TABLES_FOUND_IN_TABLES_TO_MOVE_ERROR + selectedTables.get(0) + TableMoverService.LINE_BREAK
                        + selectedTables.get(1) + TableMoverService.LINE_BREAK,
                status);
    }

    public void testCheckWikiForSelectedTablesWhenNotCopied() throws IOException {
        setUpWikiRequest(new ArrayList<String>(), null);

        String status = _tableMoverService.checkWikiForSelectedTables(TableMoverServiceData.PRODUCTION_TO_DEV, Arrays.asList("database1.table1", "database2.table2"));
        assertNull("No error expected when selected tables aren't found", status);
    }

    public void testCheckWikiForSelectedTablesFromLiveToDev() throws IOException {
        List<String> selectedTables = Arrays.asList("database1.table1", "database2.table2");
        setUpWikiRequest(selectedTables, new String[]{"done", "N/A"});

        String status = _tableMoverService.checkWikiForSelectedTables(TableMoverServiceData.DEV_TO_STAGING, selectedTables);
        assertNull("No error expected when selected tables have already been moved from live -> dev", status);
    }

    public void testCheckWikiForSelectedTablesWhenNotCopiedFromLiveToDev() throws IOException {
        List<String> selectedTables = Arrays.asList("database1.table1", "database2.table2");
        setUpWikiRequest(selectedTables, new String[]{"done", ""});

        String status = _tableMoverService.checkWikiForSelectedTables(TableMoverServiceData.DEV_TO_STAGING, selectedTables);
        assertEquals("Unexpected response when selected tables have already been copied from live -> dev",
                TableMoverService.TABLES_NOT_YET_MOVED_ERROR + selectedTables.get(1) + TableMoverService.LINE_BREAK,
                status);
    }

    public void testFilterDatabaseListForProductionToDev() {
        TableMoverServiceData databases = new TableMoverServiceData();
        databases.setDirection(TableMoverServiceData.PRODUCTION_TO_DEV);
        databases.addDatabase("test", Arrays.asList("test1", "test2"));
        databases.addDatabase("test2", Arrays.asList("test1", "test2"));
        databases.addDatabase("gs_schooldb", Arrays.asList("test1", "test2"));
        databases.addDatabase("us_geo", Arrays.asList("test1", "test2"));

        _tableMoverService.filterDatabases(databases);
        List databasesAndTables = databases.getDatabasesAndTables();
        assertEquals("Unexpected number of databases in filtered list", 3, databasesAndTables.size());
        assertNotNull("Expected test database to be in results", databases.getDatabase("test"));
        assertNotNull("Expected test2 database to be in results", databases.getDatabase("test2"));
        assertNotNull("Expected us_geo database to be in results", databases.getDatabase("us_geo"));
        assertNull("Expected gs_schooldb database to be filtered from results", databases.getDatabase("gs_schooldb"));
    }

    public void testFilterDatabaseListForDevToStaging() {
        HashSet<String> gs_schooldbTables = TableMoverService.DEV_TO_STAGING_WHITELIST.get("gs_schooldb");
        List<String> allGs_schooldbTables = new ArrayList<String>() {
            {
                add("test1");
                add("test2");
            }
        };
        allGs_schooldbTables.addAll(gs_schooldbTables);

        Set<String> us_geoTables = TableMoverService.DEV_TO_STAGING_WHITELIST.get("us_geo");
        List<String> allUs_geoTables = new ArrayList<String>() {
            {
                add("test1");
                add("test2");
            }
        };
        allUs_geoTables.addAll(us_geoTables);

        TableMoverServiceData databases = new TableMoverServiceData();
        databases.setDirection(TableMoverServiceData.DEV_TO_STAGING);
        databases.addDatabase("test", Arrays.asList("test1", "test2"));
        databases.addDatabase("test2", Arrays.asList("test1", "test2"));
        databases.addDatabase("gs_schooldb", allGs_schooldbTables);
        databases.addDatabase("us_geo", allUs_geoTables);

        _tableMoverService.filterDatabases(databases);
        List databasesAndTables = databases.getDatabasesAndTables();
        assertEquals("Unexpected number of databases in filtered list", 4, databasesAndTables.size());
        TableMoverServiceData.DatabaseTables gs_schooldb = databases.getDatabase("gs_schooldb");
        assertEquals("Unexpected number of tables in gsschool_db", gs_schooldbTables.size(), gs_schooldb.getTables().size());
        TableMoverServiceData.DatabaseTables us_geo = databases.getDatabase("us_geo");
        assertEquals("Unexpected number of tables in us_geo", us_geoTables.size(), us_geo.getTables().size());
    }

    public void testDatabaseFiltering() {
        String[] tables = {};
        List<String> tablesFilteredOut = new ArrayList<String>();
        String[] resultTables = _tableMoverService.filter(TableMoverServiceData.DEV_TO_STAGING, tables, tablesFilteredOut);
        assertEquals(0, resultTables.length);
        assertEquals(0, tablesFilteredOut.size());

        // Try the whitelist for dev to staging
        tables = new String[]{"gs_schooldb.configuration", "gs_schooldb.foo", "_ca.foo"};
        tablesFilteredOut.clear();
        resultTables = _tableMoverService.filter(TableMoverServiceData.DEV_TO_STAGING, tables, tablesFilteredOut);
        assertEquals(2, resultTables.length);
        assertTrue("Table is in the white list", ArrayUtils.contains(resultTables, "gs_schooldb.configuration"));
        assertFalse("Table is not in the white list", ArrayUtils.contains(resultTables, "gs_schooldb.foo"));
        assertTrue("Database not managed by the white list", ArrayUtils.contains(resultTables, "_ca.foo"));
        assertEquals(1, tablesFilteredOut.size());
        assertTrue(tablesFilteredOut.contains("gs_schooldb.foo"));
    }

    private void setUpWikiRequest(final List<String> selectedTables, final String[] liveToDevStatus) throws IOException {
        GetMethod tablesFoundRequest = new GetMethod(TableMoverService.TABLES_TO_MOVE_URL) {
            public String getResponseBodyAsString() throws IOException {
                return generateTableToMovePage(selectedTables, liveToDevStatus);
            }
        };
        _tableMoverService.setRequest(tablesFoundRequest);

        expect(_httpClientMock.executeMethod(tablesFoundRequest)).andReturn(HttpStatus.SC_OK);
        replay(_httpClientMock);
    }

    private String generateTableToMovePage(List<String> tables, String[] liveToDev) {
        StringBuffer tableBuffer = new StringBuffer();
        int count = 0;
        for (Iterator<String> iterator = tables.iterator(); iterator.hasNext(); count++) {
            String databaseAndTable = iterator.next();
            String[] databaseTable = databaseAndTable.split("\\.");
            tableBuffer.append("<tr><td> DR/<a href=\"http://jira.greatschools.org:8080/browse/GS-3033\" TARGET=\"_blank\">GS-3033</a> </td><td> ");
            tableBuffer.append(databaseTable[0]);
            tableBuffer.append("  </td><td> <span style='background : #FFFFCE;'><font color=\"#0000FF\">");
            tableBuffer.append(databaseTable[1]);
            tableBuffer.append("</font></span><a href=\"/bin/edit/Greatschools/");
            tableBuffer.append(databaseTable[1]);
            tableBuffer.append("?topicparent=Greatschools.TableToMove\">?</a>  </td><td>  ");
            if (liveToDev != null) {
                tableBuffer.append(liveToDev[count]);
            }
            tableBuffer.append("</td><td>  done  </td><td>  &#160;  </td><td> DSTP  </td></tr>\n\n");
        }

        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\"> \n" +
                "<head>\n" +
                " <link href=\"/pub/TWiki/GnuSkin/twikiblue.css\" rel=\"stylesheet\" type=\"text/css\">\n" +
                " <title> Wiki . Greatschools . TableToMove   </title>\n" +
                " <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />\n" +
                "  \n" +
                " <base href=\"http://wiki.greatschools.org/bin/view/Greatschools/TableToMove\" />\n" +
                "</head>\n" +
                "\n" +
                "<body bgcolor=\"white\" text=\"black\" link=\"blue\" alink=\"aqua\" vlink=\"purple\">\n" +
                tableBuffer.toString() +
                "</table>\n" +
                "</body>\n" +
                "</html>";
    }

}
