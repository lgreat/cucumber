package gs.web;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * BaseTestCase for classes in gs.web to subclass for easy access to Spring IoC
 */
public class BaseTestCase extends TestCase {
    protected final Log _log = LogFactory.getLog(getClass());
    protected static final ApplicationContext _sApplicationContext;
    private ResourceBundle _resourceBundle;

    // This static block ensures that Spring's BeanFactory is only loaded
    // once for all tests. We should find a better way to do this though so that
    // gs.data.BaseTestCase and this one don't have duplicated context paths
    static {
        // the dao.type is written to the database.properties file
        // in properties.xml
        ResourceBundle db = ResourceBundle.getBundle("conf/database");
        String[] paths = {"gs/data/applicationContext-data.xml",
                          "gs/data/dao/hibernate/applicationContext-hibernate.xml",
                          "gs/data/school/performance/applicationContext-performance.xml",
                          "applicationContext.xml",
                          "modules-servlet.xml",
                          "pages-servlet.xml"
        };
        _sApplicationContext = new ClassPathXmlApplicationContext(paths);
    }

    public BaseTestCase() {
        // Since a ResourceBundle is not required for each class, just
        // do a simple check to see if one exists
        String className = this.getClass().getName();

        try {
            _resourceBundle = ResourceBundle.getBundle(className);
        } catch (final MissingResourceException mre) {
            //log.warn("No resource bundle found for: " + className);
        }
    }

    public ApplicationContext getApplicationContext() {
        return _sApplicationContext;
    }
}
