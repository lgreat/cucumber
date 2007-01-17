package gs.web.admin.gwt.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import gs.web.admin.gwt.client.TableCopyService;
import gs.web.admin.gwt.client.TableData;
import gs.data.dao.hibernate.ThreadLocalHibernateDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.hibernate.SessionFactory;


public class TableCopyServiceImpl extends RemoteServiceServlet implements TableCopyService {
    List gsList = new ArrayList();
    List azList = new ArrayList();
    TableData tableData = new TableData();
    private JdbcOperations _jdbcContext;
    private SessionFactory _sessionFactory;
    public static final String DATABASE_COLUMN = "table_schema";
    public static final String TABLE_COLUMN = "table_name";
    public static final String TABLE_LIST_QUERY = "select " + DATABASE_COLUMN + ", " + TABLE_COLUMN + " from information_schema.tables " +
            "where table_schema not in ('information_schema', 'mysql') " +
            "order by table_schema, table_name;";


    public TableCopyServiceImpl() {
        gsList.add("table1");
        gsList.add("table2");
        azList.add("az_table1");
        azList.add("az_table2");
        tableData.addDatabase("gs_schooldb", gsList);
        tableData.addDatabase("_az", azList);
    }

    public TableData getTables(TableData.DatabaseDirection direction) {
//        return tableData;
        return lookupTables(direction);
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

    public TableData lookupTables(TableData.DatabaseDirection direction) {
        TableData databases = new TableData();
        JdbcOperations jdbcOperations = getJdbcContext();
        List results = jdbcOperations.queryForList(TABLE_LIST_QUERY);

        for (Iterator iterator = results.iterator(); iterator.hasNext();) {
            Map result = (Map) iterator.next();
            String database = (String) result.get(DATABASE_COLUMN);
            String table = (String) result.get(TABLE_COLUMN);
            databases.addDatabaseAndTable(database, table);
        }
        return databases;
    }
}