package gs.web;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import gs.data.dao.hibernate.ThreadLocalTransactionManager;

/**
 * BaseTestCase for classes in gs.web to subclass for easy access to Spring IoC
 */
public class BaseTestCase extends TestCase {
    protected final Log _log = LogFactory.getLog(getClass());
    private static ApplicationContext _sApplicationContext = null;


    public BaseTestCase() {
    }

    public ApplicationContext getApplicationContext() {
        if (_sApplicationContext == null) {
            String[] paths = {"gs/data/applicationContext-data.xml",
                              "gs/data/dao/hibernate/applicationContext-hibernate.xml",
                              "gs/data/school/performance/applicationContext-performance.xml",
                              "applicationContext.xml",
                              "modules-servlet.xml",
                              "pages-servlet.xml"
            };
            _sApplicationContext = new ClassPathXmlApplicationContext(paths);
        }

        return _sApplicationContext;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        ThreadLocalTransactionManager.commitOrRollback();
    }
}
