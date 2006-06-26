package gs.web;

import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
            String[] paths = {"applicationContext.xml",
                              "modules-servlet.xml",
                              "pages-servlet.xml"
            };
            _sApplicationContext = new ClassPathXmlApplicationContext(paths, gs.data.util.SpringUtil.getApplicationContext());
        }

        return _sApplicationContext;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        ThreadLocalTransactionManager.commitOrRollback();
    }
}
