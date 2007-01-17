package gs.web.admin.gwt.server;

import gs.web.BaseTestCase;
import gs.web.admin.gwt.client.TableData;
import gs.data.dao.hibernate.ThreadLocalHibernateDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.JdbcOperations;
import org.hibernate.SessionFactory;
import org.easymock.MockControl;

import java.util.*;

public class TableCopyServiceSaTest extends BaseTestCase {
    private SessionFactory _sessionFactory;
    private TableCopyServiceImpl tableCopyService;
    private MockControl jdbcMock;
    private JdbcOperations context;

    protected void setUp() throws Exception {
        ApplicationContext ctx = getApplicationContext();
        _sessionFactory = (SessionFactory) ctx.getBean("sessionFactory");

        tableCopyService = new TableCopyServiceImpl();
        jdbcMock = MockControl.createControl(JdbcOperations.class);
        context = (JdbcOperations) jdbcMock.getMock();
        tableCopyService.setJdbcContext(context);
    }

    public void testLookupTables() {
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

        TableData tableData = tableCopyService.lookupTables(TableData.DEV_TO_STAGING);

        assertEquals("Expected 2 databases", 2, tableData.getDatabaseTables().size());

        jdbcMock.verify();
    }

    // TODO: how do I get connection to other database?
//    public void testQuery() {
//        JdbcTemplate jdbc = new JdbcTemplate(new ThreadLocalHibernateDataSource(_sessionFactory));
//        List results = jdbc.queryForList(TableCopyServiceImpl.TABLE_LIST_QUERY);
//
//        for (Iterator iterator = results.iterator(); iterator.hasNext();) {
//            Map result = (Map) iterator.next();
//            for (Iterator iterator1 = result.keySet().iterator(); iterator1.hasNext();) {
//                String column = (String) iterator1.next();
//                String value = (String) result.get(column);
//                System.out.println(column + ": " + value);
//            }
//        }
//    }
}
