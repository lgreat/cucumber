package gs.web.admin.gwt.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import gs.data.dao.hibernate.ThreadLocalHibernateDataSource;
import gs.data.state.StateManager;
import gs.web.admin.gwt.client.ServiceException;
import gs.web.admin.gwt.client.TableCopyService;
import gs.web.admin.gwt.client.TableData;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.collections.ListUtils;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TableCopyServiceImpl extends RemoteServiceServlet implements TableCopyService {
    transient private static final Log _log = LogFactory.getLog(TableCopyServiceImpl.class);
    transient private JdbcOperations _jdbcContext;
    transient private SessionFactory _sessionFactory;
    transient private StateManager _stateManager;
    transient private HttpClient _httpClient = new HttpClient();
    transient private GetMethod _request = new GetMethod(TABLES_TO_MOVE_URL);

    public static final String DATABASE_COLUMN = "table_schema";
    public static final String TABLE_COLUMN = "table_name";
    public static final String TABLE_LIST_QUERY = "select " + DATABASE_COLUMN + ", " + TABLE_COLUMN + " from information_schema.tables " +
            "where table_schema not in ('information_schema', 'mysql') " +
            "order by table_schema, table_name;";
    public static final String COPY_TABLES_COMMAND = "/usr2/sites/main.dev/scripts/sysadmin/database/dumpcopy --yes ";
    public static final String OUTDIR_FLAG_FOR_BACKUP_COMMAND = "--outdir /var/gsbackups/tablecopy";
    public static final String TABLE_COPY_FAILURE_HEADER = "The following table(s) failed to copy:" + LINE_BREAK;

    transient public static final List<String> PRODUCTION_TO_DEV_BLACKLIST = new ArrayList<String>() {
        {
            add("gs_schooldb");
            add("us_geo");
        }
    };
    transient public static final Map<String, HashSet<String>> DEV_TO_STAGING_WHITELIST = new HashMap<String, HashSet<String>>() {
        {
            put("gs_schooldb", new HashSet<String>() {
                {
                    add("census_data_set_file");
                    add("census_data_type");
                    add("census_group_data_type");
                    add("configuration");
                    add("operator");
                    add("DataFile");
                    add("DataLoad");
                    add("DataSource");
                    add("TestDataSetFile");
                    add("TestDataSubject");
                    add("TestDataType");
                    add("TestProficiencyBand");
                    add("TestProficiencyBandGroup");
                    add("test");
                }
            });
            put("us_geo", new HashSet<String>() {
                {
                    add("city");
                }
            });
        }
    };

    public TableData getTables(TableData.DatabaseDirection direction) {
        _log.info("Retrieving tables");
        long start = System.currentTimeMillis();
        TableData databases = new TableData();
        databases.setDirection(direction);
        JdbcOperations jdbcOperations = getJdbcContext();

        long startQuery = System.currentTimeMillis();
        List results = jdbcOperations.queryForList(TABLE_LIST_QUERY);
        long endQuery = System.currentTimeMillis();

        for (Object result1 : results) {
            Map result = (Map) result1;
            String database = (String) result.get(DATABASE_COLUMN);
            String table = (String) result.get(TABLE_COLUMN);
            databases.addDatabaseAndTable(database, table);
        }

        long endPopulate = System.currentTimeMillis();
        filterDatabases(databases);
        long stop = System.currentTimeMillis();

        _log.info("Took " + (stop - start) + " milliseconds to retrieve tables");
        _log.info("Took " + (endQuery - startQuery) + " milliseconds to execute query");
        _log.info("Took " + (endPopulate - endQuery) + " milliseconds to process result set");
        _log.info("Took " + (stop - endPopulate) + " milliseconds to filter tables");
        return databases;
    }

    public String copyTables(TableData.DatabaseDirection direction, String[] tableList, boolean overrideWarnings) throws ServiceException {
        return copyTables(direction, tableList, overrideWarnings, null, null, null);
    }

    public String copyTables(TableData.DatabaseDirection direction, String[] tableList, boolean overrideWarnings, String initials, String jira, String notes) throws ServiceException {
        _log.info("Copying tables");
        long start = System.currentTimeMillis();

        // check status of tables on wiki
        if (!overrideWarnings) {
            try {
                _log.debug("Checking wiki");
                String copyStatus = checkWikiForSelectedTables(direction, Arrays.asList(tableList));
                if (copyStatus != null) {
                    throw new ServiceException(copyStatus);
                }
            } catch (IOException e) {
                _log.error("Error getting wiki status", e);
                throw new ServiceException("Error getting wiki status: " + e.getMessage());
            }
        } else {
            _log.info("Overriding warnings");
        }

        // execute copy command
        String backupCommand = generateBackupCommand(direction, Arrays.asList(tableList));
        String copyCommand = generateCopyCommand(direction, Arrays.asList(tableList));
        try {
            String backupOutput = executeCommand(backupCommand);
            _log.info("Backup command output: " + backupOutput);
            String errorText = parseCommandOutput(backupOutput);
            if (errorText != null) {
                _log.error("Error backing up tables with dumpcopy: " + errorText);
                throw new ServiceException("Error backing up tables: " + errorText);
            }

            String copyOutput = executeCommand(copyCommand);
            _log.info("Copy command output: " + copyOutput);
            errorText = parseCommandOutput(backupOutput);
            if (errorText != null) {
                _log.error("Error copying tables with dumpcopy: " + errorText);
                throw new ServiceException("Error copying tables: " + errorText);
            }
        } catch (IOException e) {
            _log.error("Error executing dumpcopy", e);
            throw new ServiceException("Error copying tables: " + e.getMessage());
        }

        long stop = System.currentTimeMillis();
        _log.info("Took " + (stop - start) + " milliseconds to copy tables");
        return generateWikiText(direction, Arrays.asList(tableList), initials, jira, notes);
    }

    private String executeCommand(String copyCommand) throws IOException {
        BufferedReader reader = null;
        try {
            _log.info("Executing command: " + copyCommand);
            Process process = Runtime.getRuntime().exec(copyCommand);
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            return buffer.toString();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // Do nothing
            }
        }
    }

    public String generateCopyCommand(TableData.DatabaseDirection direction, List<String> tables) {
        StringBuffer command = new StringBuffer();
        command.append(COPY_TABLES_COMMAND);
        command.append(" --fromhost ").append(direction.getSource()).append(" ");
        command.append(" --tohost ").append(direction.getTarget()).append(" ");
        StringBuffer tableList = new StringBuffer(" --tablelist ");
        for (Iterator<String> iterator = tables.iterator(); iterator.hasNext();) {
            String databaseAndTable = iterator.next();
            tableList.append(databaseAndTable);
            if (iterator.hasNext()) {
                tableList.append(",");
            }
        }
        tableList.append(" ");
        command.append(tableList);
        return command.toString();
    }

    public String generateBackupCommand(TableData.DatabaseDirection direction, List<String> tables) {
        StringBuffer command = new StringBuffer();
        command.append(COPY_TABLES_COMMAND);
        command.append(" --fromhost ").append(direction.getTarget()).append(" ");
        command.append(OUTDIR_FLAG_FOR_BACKUP_COMMAND);
        StringBuffer tableList = new StringBuffer(" --tablelist ");
        for (Iterator<String> iterator = tables.iterator(); iterator.hasNext();) {
            String databaseAndTable = iterator.next();
            tableList.append(databaseAndTable);
            if (iterator.hasNext()) {
                tableList.append(",");
            }
        }
        tableList.append(" ");
        command.append(tableList);
        return command.toString();
    }

    public String generateWikiText(TableData.DatabaseDirection direction, List<String> databasesAndTables, String initials, String jira, String notes) {
        if (StringUtils.isEmpty(initials)) initials = "??";
        if (StringUtils.isEmpty(jira)) jira = "GS-????";
        if (StringUtils.isEmpty(notes)) notes = "";
        StringBuffer wikiText = new StringBuffer("|Who/Jira|Db|Table|live -> dev|dev -> staging|staging -> live|Notes|\n");
        for (String databasesAndTable : condenseTableList(databasesAndTables)) {
            String[] split = databasesAndTable.split("\\.");
            wikiText.append("| ").append(initials).append("/").append(jira).append(" | ");
            wikiText.append(split[0]);
            wikiText.append(" | ");
            wikiText.append(split[1]);
            wikiText.append(" | ");
            wikiText.append("done");
            wikiText.append(" | ");
            if (direction.equals(TableData.DEV_TO_STAGING)) {
                wikiText.append("done");
            }
            wikiText.append(" | ");
            wikiText.append(" | ").append(notes);
            wikiText.append(" |\n");
        }
        return wikiText.toString();
    }

    private List<String> condenseTableList(List<String> tables) {
        final int MIN_NUMBER_OF_STATES = 40;
        List<String> condensed = new ArrayList<String>();

        // Do first pass to build tableToDatabaseMap
        Map<String, List<String>> tableToDatabaseMap = new HashMap<String, List<String>>();
        for (String table : tables) {
            if (table.startsWith("_")) {
                String state = table.split("\\.")[0].substring(1).toUpperCase(); // e.g. _ca becomes CA
                String tableSuffix = table.split("\\.")[1]; // e.g. census_school_value
                if (tableToDatabaseMap.get(tableSuffix) == null)
                    tableToDatabaseMap.put(tableSuffix, new ArrayList<String>());
                tableToDatabaseMap.get(tableSuffix).add(state);
            } else {
                condensed.add(table);
            }
        }

        // Do second pass to condense tables with MIN_NUMBER_OF_STATES or greater
        for (String tableSuffix : tableToDatabaseMap.keySet()) {
            List<String> states = tableToDatabaseMap.get(tableSuffix);
            if (states.size() >= MIN_NUMBER_OF_STATES) {
                // To condense we have to invert the list of states
                StringBuffer condensedStates = new StringBuffer("all");
                for (Object negativeState : ListUtils.subtract(_stateManager.getSortedAbbreviations(), states))
                    condensedStates.append(" -").append(((String) negativeState).toLowerCase());
                condensedStates.append(".").append(tableSuffix);
                condensed.add(condensedStates.toString());
            } else {
                for (String state : states) condensed.add("_" + state.toLowerCase() + "." + tableSuffix);
            }
        }
        Collections.sort(condensed);
        return condensed;
    }

    public String parseCommandOutput(String output) {
        String error = null;
        if (output != null) {
            Pattern pattern = Pattern.compile("Skipping table\\s+(.*)\\.(.*)");
            Matcher matcher = pattern.matcher(output);
            if (matcher.find()) {
                StringBuffer errorBuffer = new StringBuffer(TABLE_COPY_FAILURE_HEADER);
                do {
                    errorBuffer.append("\t&#160;&#160;&#160;").append(matcher.group(1)).append(".").append(matcher.group(2)).append(LINE_BREAK);
                } while (matcher.find());
                error = errorBuffer.toString();
            }
        }
        return error;
    }

    public String getDatabaseTableMatcher(String database, String table) {
        // match database and table surrounded by tags, only on the same line, followed by at least 4 table cells
        return "(?m)^.*>\\s*(" + database + "|all|all(\\s+-(?!" + database + ")\\S+)+)\\s*<.*>\\s*" + table + "\\s*<(.*/td>\\s*<td){4}.*$";
    }

    public String getDatabaseTableCopiedToDevMatcher(String database, String table) {
        // match database and table surrounded by tags, only on the same line, with "done" followed by at least 3 table cells
        return "(?m)^.*>\\s*(" + database + "|all|all(\\s+-(?!" + database + ")\\S+)+)\\s*<.*>\\s*" + table + "\\s*<.*>\\s*(done|n/a|N/A)\\s*<(.*/td>\\s*<td){3}.*$";
    }

    public List<String> tablesFoundInTablesToMove(List<String> databaseTablePairs) throws IOException {
        _httpClient.executeMethod(_request);

        List<String> found = new ArrayList<String>();

        for (String databaseTablePair : databaseTablePairs) {
            String[] databaseAndTable = databaseTablePair.split("\\.");
            // String.matches() doesn't seem to work with multiline searches
            Pattern pattern = Pattern.compile(getDatabaseTableMatcher(databaseAndTable[0], databaseAndTable[1]));
            String pageBody = _request.getResponseBodyAsString();
            Matcher matcher = pattern.matcher(pageBody);
            if (matcher.find()) {
                found.add(databaseTablePair);
            }
        }
        return found;
    }

    public List<String> tablesNotYetCopiedToDev(List<String> databaseTablePairs) throws IOException {
        _httpClient.executeMethod(_request);

        List<String> notFound = new ArrayList<String>();

        for (String databaseTable : databaseTablePairs) {
            String[] databaseAndTable = databaseTable.split("\\.");
            // String.matches() doesn't seem to work with multiline searches
            Pattern pattern = Pattern.compile(getDatabaseTableCopiedToDevMatcher(databaseAndTable[0], databaseAndTable[1]));
            String pageBody = _request.getResponseBodyAsString();
            Matcher matcher = pattern.matcher(pageBody);
            if (!matcher.find()) {
                notFound.add(databaseTable);
            }
        }

        return notFound;
    }

    public String checkWikiForSelectedTables(TableData.DatabaseDirection direction, List<String> selectedTables) throws IOException {
        if (TableData.PRODUCTION_TO_DEV.equals(direction)) {
            List<String> tablesAlreadyMoved = tablesFoundInTablesToMove(selectedTables);
            return generateWarningOutput(tablesAlreadyMoved, TABLES_FOUND_IN_TABLES_TO_MOVE_ERROR);
        } else if (TableData.DEV_TO_STAGING.equals(direction)) {
            List<String> tablesNotYetMoved = tablesNotYetCopiedToDev(selectedTables);
            return generateWarningOutput(tablesNotYetMoved, TABLES_NOT_YET_MOVED_ERROR);
        }
        return null;
    }

    /**
     * Filter the databases and tables, based on the logic in GS-3018
     *
     * @param databases
     * @return
     */
    public void filterDatabases(TableData databases) {
        if (TableData.PRODUCTION_TO_DEV.equals(databases.getDirection())) {
            databases.filterDatabases(PRODUCTION_TO_DEV_BLACKLIST);
        } else if (TableData.DEV_TO_STAGING.equals(databases.getDirection())) {
            databases.filterTables(DEV_TO_STAGING_WHITELIST);
        }
    }

    /**
     * Use black and white lists to filter out tables that should be skipped
     *
     * @param direction
     * @param tables
     * @param tablesFilteredOut is a list where filtered tables will be put
     * @return Filtered table list
     */
    public String[] filter(TableData.DatabaseDirection direction, String[] tables, List<String> tablesFilteredOut) {
        ArrayList<String> tablesToKeep = new ArrayList<String>();
        for (String table : tables) {
            if (TableData.PRODUCTION_TO_DEV.equals(direction)) {
                boolean match = false;
                for (String database : PRODUCTION_TO_DEV_BLACKLIST) {
                    if (table.startsWith(database)) match = true;
                }
                if (!match || table.equals("gs_schooldb.test")) {
                    tablesToKeep.add(table);
                } else {
                    tablesFilteredOut.add(table);
                }
            } else if (TableData.DEV_TO_STAGING.equals(direction)) {
                String database = table.split("\\.")[0];
                String tableSuffix = table.split("\\.")[1];
                Set<String> tableOKSet = DEV_TO_STAGING_WHITELIST.get(database);
                if (tableOKSet == null) {
                    tablesToKeep.add(table);
                } else if (tableOKSet.contains(tableSuffix)) {
                    tablesToKeep.add(table);
                } else {
                    tablesFilteredOut.add(table);
                }
            }
        }
        return tablesToKeep.toArray(new String[tablesToKeep.size()]);
    }

    private String generateWarningOutput(List<String> tables, String errorMessage) {
        if (!tables.isEmpty()) {
            StringBuffer error = new StringBuffer(errorMessage);
            for (String table1 : tables) {
                error.append(table1).append(LINE_BREAK);
            }
            return error.toString();
        }
        return null;
    }

    public void setJdbcContext(JdbcOperations context) {
        this._jdbcContext = context;
    }

    public JdbcOperations getJdbcContext() {
        if (_jdbcContext == null) {
            _jdbcContext = new JdbcTemplate(new ThreadLocalHibernateDataSource(getSessionFactory()));
        }
        return _jdbcContext;
    }

    private SessionFactory getSessionFactory() {
        return _sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }

    public void setHttpClient(HttpClient httpClient) {
        _httpClient = httpClient;
    }

    public HttpClient getHttpClient() {
        return _httpClient;
    }

    public void setRequest(GetMethod request) {
        _request = request;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}