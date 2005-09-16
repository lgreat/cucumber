package gs.web.util;

import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Enumeration;
import java.beans.Introspector;
import java.sql.DriverManager;
import java.sql.Driver;

/**
 * This class is intended to cleanup JDBC and Log4j when a webapp is redeployed
 * so that all the memory can be freed based upon suggestions here:
 *
 * http://opensource2.atlassian.com/confluence/spring/pages/viewpage.action?pageId=2669
 */
public class UndeployCleanupListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent event) {
    }

    public void contextDestroyed(ServletContextEvent event) {
        try {
            // LogFactory.release(Thread.currentThread().getContextClassLoader());
            LogFactory.releaseAll();
            Introspector.flushCaches();
            for (Enumeration e = DriverManager.getDrivers(); e.hasMoreElements();) {
                Driver driver = (Driver) e.nextElement();
                if (driver.getClass().getClassLoader() == getClass().getClassLoader()) {
                    DriverManager.deregisterDriver(driver);
                }
            }
        } catch (Throwable e) {
            System.err.println("Failed to cleanup ClassLoader for webapp");
            e.printStackTrace();
        }
    }
}
