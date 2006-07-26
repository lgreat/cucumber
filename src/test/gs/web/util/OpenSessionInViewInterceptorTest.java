package gs.web.util;

import gs.web.BaseControllerTestCase;
import gs.web.status.MonitorController;
import gs.web.content.AllArticlesController;
import gs.web.search.SearchController;
import gs.web.geo.CityController;
import gs.web.community.registration.RegistrationController;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class OpenSessionInViewInterceptorTest extends BaseControllerTestCase {

    public void testOsivInterceptor() throws Exception {
        // All controllers should be treated as read-only unless they are flagged as read-write
        Controller controller = new ParameterizableViewController();
        assertReadWriteStatus(controller, true, null);
        assertReadWriteStatus(controller, true, new Exception());

        controller = new CityController();
        assertReadWriteStatus(controller, true, null);

        controller = new AllArticlesController();
        assertReadWriteStatus(controller, true, null);

        // Read-write controllers need to be explicitly flagged as such with an interface
        controller = new RegistrationController();
        assertReadWriteStatus(controller, false, null);

        controller = new MonitorController();
        assertReadWriteStatus(controller, false, null);
    }

    private void assertReadWriteStatus(Controller controller, boolean isReadOnly, Exception e) throws Exception {
        OpenSessionInViewInterceptor osiv = new OpenSessionInViewInterceptor();
        ServletRequest request = getRequest();
        ServletResponse response = getResponse();
        osiv.preHandle((HttpServletRequest) request, (HttpServletResponse) response, controller);
        assertTrue(ThreadLocalTransactionManager.isReadOnly() == isReadOnly);
        osiv.afterCompletion((HttpServletRequest) request, (HttpServletResponse) response, controller, e);
        assertFalse(ThreadLocalTransactionManager.isReadOnly());
    }
}
