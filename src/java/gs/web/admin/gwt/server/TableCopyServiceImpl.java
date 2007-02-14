package gs.web.admin.gwt.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import gs.web.admin.gwt.client.TableCopyService;
import gs.web.admin.gwt.client.TableData;
import gs.web.admin.gwt.client.ServiceException;
import gs.data.dao.hibernate.ThreadLocalHibernateDataSource;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.hibernate.SessionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;


public class TableCopyServiceImpl extends RemoteServiceServlet implements TableCopyService {
    private static final Log _log = LogFactory.getLog(TableCopyServiceImpl.class);
    private JdbcOperations _jdbcContext;
    private SessionFactory _sessionFactory;
    private HttpClient _httpClient = new HttpClient();
    private GetMethod _request = new GetMethod(TABLES_TO_MOVE_URL);

    public static final String DATABASE_COLUMN = "table_schema";
    public static final String TABLE_COLUMN = "table_name";
    public static final String TABLE_LIST_QUERY = "select " + DATABASE_COLUMN + ", " + TABLE_COLUMN + " from information_schema.tables " +
            "where table_schema not in ('information_schema', 'mysql') " +
            "order by table_schema, table_name;";
    public static final String COPY_TABLES_COMMAND = "/usr2/sites/main.dev/scripts/sysadmin/database/dumpcopy --yes ";
    public static final String TABLE_COPY_FAILURE_HEADER = "The following table(s) failed to copy:\n<br>";
    public static final String TABLES_TO_MOVE_URL = "http://wiki.greatschools.net/bin/view/Greatschools/TableToMove";
    public static final String TABLES_FOUND_IN_TABLES_TO_MOVE_ERROR = "The following tables have already been copied.\n" +
            "Please check http://wiki.greatschools.net/bin/view/Greatschools/TableToMove before proceeding\n";
    public static final String TABLES_NOT_YET_MOVED_ERROR = "The following tables have not yet been copied from live -> dev.\n" +
            "Please check http://wiki.greatschools.net/bin/view/Greatschools/TableToMove before proceeding\n";

    public TableData getTables(TableData.DatabaseDirection direction) {
//        return populateTestData();
        _log.info("Retrieving tables");
        long start = System.currentTimeMillis();
        TableData databases = new TableData();
        databases.setDirection(direction);
        JdbcOperations jdbcOperations = getJdbcContext();
        List results = jdbcOperations.queryForList(TABLE_LIST_QUERY);

        for (Iterator iterator = results.iterator(); iterator.hasNext();) {
            Map result = (Map) iterator.next();
            String database = (String) result.get(DATABASE_COLUMN);
            String table = (String) result.get(TABLE_COLUMN);
            databases.addDatabaseAndTable(database, table);
        }
        long stop = System.currentTimeMillis();
        _log.info("Took " + (stop-start) + " milliseconds to retrieve tables");
        return databases;
    }

    public String copyTables(TableData.DatabaseDirection direction, String[] tableList) throws ServiceException {
//        return "success!";
        _log.info("Copying tables");
        long start = System.currentTimeMillis();

        // check status of tables on wiki
        try {
            String copyStatus = checkWikiForSelectedTables(direction, Arrays.asList(tableList));
            if (copyStatus != null) {
                ServiceException exception = new ServiceException(copyStatus);
                throw exception;
            }
        } catch (IOException e) {
            _log.error("Error getting wiki status", e);
            throw new ServiceException("Error getting wiki status: " + e.getMessage());
        }

        // execute copy command
        String copyCommand = generateCopyCommand(direction, Arrays.asList(tableList));
        String copyOutput = null;
        try {
            copyOutput = executeCopyCommand(copyCommand);
            _log.info("Copy command output: " + copyOutput);
        } catch (IOException e) {
            _log.error("Error executing dumpcopy", e);
            throw new ServiceException("Error copying tables: " + e.getMessage());
        }

        // process comand output
        String errorText = parseCommandOutput(copyOutput);
        if (errorText != null) {
            _log.error("Error executing dumpcopy: " + errorText);
            ServiceException exception = new ServiceException("Error copying tables: " + errorText);
            throw exception;
        }

        long stop = System.currentTimeMillis();
        _log.info("Took " + (stop - start) + " milliseconds to copy tables");
        return generateWikiText(direction, Arrays.asList(tableList));
    }

    private String executeCopyCommand(String copyCommand) throws IOException {
        BufferedReader reader = null;
        try {
            _log.info("Executing command: " + copyCommand);
            Process process = Runtime.getRuntime().exec(copyCommand);
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuffer buffer = new StringBuffer();
            String line;
            while((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            return buffer.toString();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {}
        }
    }

    public String generateCopyCommand(TableData.DatabaseDirection devToStaging, List databasesAndTables) {
        StringBuffer command = new StringBuffer();
        command.append(COPY_TABLES_COMMAND);
        command.append(" --fromhost " + devToStaging.getSource() + " ");
        command.append(" --tohost " + devToStaging.getTarget() + " ");
        StringBuffer tables = new StringBuffer(" --tablelist ");
        for (Iterator iterator = databasesAndTables.iterator(); iterator.hasNext();) {
            String databaseAndTable = (String) iterator.next();
            tables.append(databaseAndTable);
            if (iterator.hasNext()) {
                tables.append(",");
            }
        }
        tables.append(" ");
        command.append(tables);
        return command.toString();
    }

    public String generateWikiText(TableData.DatabaseDirection direction, List databasesAndTables) {
        StringBuffer wikiText = new StringBuffer("|Who/Jira|Db|Table|live -> dev|dev -> staging|staging -> live|Notes|\n");
        for (Iterator iterator = databasesAndTables.iterator(); iterator.hasNext();) {
            String databaseAndTable = (String) iterator.next();
            String[] split = databaseAndTable.split("\\.");
            wikiText.append("| ??/GS-???? | ");
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
            wikiText.append(" | ");
            wikiText.append(" |\n");
        }
        return wikiText.toString();
    }

    public String parseCommandOutput(String output) {
        String error = null;
        if (output != null) {
            Pattern pattern = Pattern.compile("Skipping table\\s+(.*)\\.(.*)");
            Matcher matcher = pattern.matcher(output);
            if (matcher.find()) {
                StringBuffer errorBuffer = new StringBuffer(TABLE_COPY_FAILURE_HEADER);
                do {
                    errorBuffer.append("\t&nbsp;&nbsp;&nbsp;").append(matcher.group(1)).append(".").append(matcher.group(2)).append("<br>\n");
                } while (matcher.find());
                error = errorBuffer.toString();
            }
        }
        return error;
    }

    public String getDatabaseTableMatcher(String database, String table) {
        // match database and table in separate cells, only on the same line
        return "(?m)^.*" + database + ".*</td>\\s*<td.*" + table + ".*$";
    }

    public String getDatabaseTableCopiedToDevMatcher(String database, String table) {
        // match database and table in separate cells, only on the same line, with "done" followed by at least 3 table cells
        return "(?m)^.*" + database + ".*</td>\\s*<td.*?" + table + ".*?</td>\\s*<td.*?done(.*</td>\\s*<td){3}.*$";
    }

    public List tablesFoundInTablesToMove(List databaseTablePairs) throws IOException {
        _httpClient.executeMethod(_request);

        List found = new ArrayList();

        for (Iterator iterator = databaseTablePairs.iterator(); iterator.hasNext();) {
            String databaseTable = (String) iterator.next();
            String[] databaseAndTable = databaseTable.split("\\.");
            // String.matches() doesn't seem to work with multiline searches
            Pattern pattern = Pattern.compile(getDatabaseTableMatcher(databaseAndTable[0], databaseAndTable[1]));
            String pageBody = _request.getResponseBodyAsString();
            Matcher matcher = pattern.matcher(pageBody);
            if (matcher.find()) {
                found.add(databaseTable);
            }
        }
        return found;
    }

    public List tablesNotYetCopiedToDev(List databaseTablePairs) throws IOException {
        _httpClient.executeMethod(_request);

        List notFound = new ArrayList();

        for (Iterator iterator = databaseTablePairs.iterator(); iterator.hasNext();) {
            String databaseTable = (String) iterator.next();
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

    public String checkWikiForSelectedTables(TableData.DatabaseDirection direction, List selectedTables) throws IOException {
        if (TableData.PRODUCTION_TO_DEV.equals(direction)) {
            List tablesAlreadyMoved = tablesFoundInTablesToMove(selectedTables);
            return generateWarningOutput(tablesAlreadyMoved, TABLES_FOUND_IN_TABLES_TO_MOVE_ERROR);
        } else if (TableData.DEV_TO_STAGING.equals(direction)) {
            List tablesNotYetMoved = tablesNotYetCopiedToDev(selectedTables);
            return generateWarningOutput(tablesNotYetMoved, TABLES_NOT_YET_MOVED_ERROR);
        }
        return null;
    }

    private String generateWarningOutput(List tables, String errorMessage) {
        if (!tables.isEmpty()) {
            StringBuffer error = new StringBuffer(errorMessage);
            for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
                String table = (String) iterator.next();
                error.append(table).append("\n");
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

    private TableData populateTestData() {
        List gsList = new ArrayList();
        List azList = new ArrayList();
        TableData tableData = new TableData();
        gsList.add("table1");
        gsList.add("table2");
        azList.add("az_table1");
        azList.add("az_table2");
        tableData.addDatabase("gs_schooldb", gsList);
        tableData.addDatabase("_az", azList);
        tableData.setDirection(TableData.DEV_TO_STAGING);
        return tableData;
    }
}