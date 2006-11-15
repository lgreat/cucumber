package gs.web.status;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.data.admin.batch.IBatchLogDao;
import gs.data.dao.hibernate.ThreadLocalHibernateDataSource;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.hibernate.SessionFactory;

import java.util.Date;
import java.util.List;

/**
 * @author thuss
 */
public class BatchLogControllerTest extends BaseControllerTestCase {

    private BatchLogController _controller;
    private IBatchLogDao _batchLogDao;

    public void testHandleRequest() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        ModelAndView mv = _controller.handleRequestInternal(request, getResponse());
        assertNotNull(mv);
        List batchLogs = (List) mv.getModel().get(BatchLogController.MODEL_BATCH_LOGS);
        assertNotNull(batchLogs);
        assertEquals(2, batchLogs.size());
        assertEquals(Boolean.TRUE, mv.getModel().get(BatchLogController.MODEL_OVERVIEW));
        
        request.setParameter(BatchLogController.PARAM_NAME, "gs.batch.SecondBatchJob");
        mv = _controller.handleRequestInternal(request, getResponse());
        batchLogs = (List) mv.getModel().get(BatchLogController.MODEL_BATCH_LOGS);
        assertEquals(1, batchLogs.size());
        assertEquals(null, mv.getModel().get(BatchLogController.MODEL_OVERVIEW));
    }

    protected void setUp() throws Exception {
        super.setUp();
        _controller = (BatchLogController) getApplicationContext().getBean(BatchLogController.BEAN_ID);
        _batchLogDao = (IBatchLogDao) getApplicationContext().getBean(IBatchLogDao.BEAN_ID);
        String name = "gs.batch.FirstBatchJob";
        _batchLogDao.logRun(name, "Updated 25 records", new Date(), new Date(), true);
        _batchLogDao.logRun(name, "Updated 25 records", new Date(), new Date(), true);
        _batchLogDao.logRun("gs.batch.SecondBatchJob", "Updated 5 records", new Date(), new Date(), true);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        SessionFactory sessionFactory = (SessionFactory) getApplicationContext().getBean("sessionFactory");
        JdbcTemplate jdbc = new JdbcTemplate(new ThreadLocalHibernateDataSource(sessionFactory));
        jdbc.execute("delete from batch_log");
    }

}
