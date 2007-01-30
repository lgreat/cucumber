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


public class TableCopyServiceImpl extends RemoteServiceServlet implements TableCopyService {
    private static final Log _log = LogFactory.getLog(TableCopyServiceImpl.class);
    private JdbcOperations _jdbcContext;
    private SessionFactory _sessionFactory;
    public static final String DATABASE_COLUMN = "table_schema";
    public static final String TABLE_COLUMN = "table_name";
    public static final String TABLE_LIST_QUERY = "select " + DATABASE_COLUMN + ", " + TABLE_COLUMN + " from information_schema.tables " +
            "where table_schema not in ('information_schema', 'mysql') " +
            "order by table_schema, table_name;";
    public static final String COPY_TABLES_COMMAND = "/usr2/sites/main.dev/scripts/sysadmin/database/dumpcopy --yes ";
    static final String TABLE_COPY_FAILURE_HEADER = "The following table(s) failed to copy:\n<br>";

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

    public TableData getTables(TableData.DatabaseDirection direction) {
        _log.info("Retrieving tables");
        long start = System.currentTimeMillis();
//        return populateTestData();
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
        String copyCommand = generateCopyCommand(direction, Arrays.asList(tableList));
        String copyOutput = null;
        try {
            copyOutput = executeCopyCommand(copyCommand);
        } catch (IOException e) {
            _log.error("Error executing dumpcopy: " + e.getMessage());
            throw new ServiceException("Error copying tables: " + e.getMessage());
        }
        String errorText = parseCommandOutput(copyOutput);
        if (errorText != null) {
            _log.error("Error executing dumpcopy: " + errorText);
            ServiceException exception = new ServiceException("Error copying tables: " + errorText);
            throw exception;
        }
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