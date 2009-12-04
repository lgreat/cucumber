package gs.web.util;

import gs.web.BaseControllerTestCase;
import gs.web.api.admin.AccountController;
import gs.web.status.MonitorController;
import gs.web.content.AllArticlesController;
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
 * @author <a href="mailto:thuss@greatschools.org">Todd Huss</a>
 */
public class ReadWriteInterceptorTest extends BaseControllerTestCase {

    public void testReadWriteInterceptor() throws Exception {
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

        Object annotationController = new AccountController();
        assertReadWriteStatus(annotationController, false, null);
    }

    private void assertReadWriteStatus(Object controller, boolean isReadOnly, Exception e) throws Exception {
        ReadWriteInterceptor osiv = new ReadWriteInterceptor();
        ServletRequest request = getRequest();
        ServletResponse response = getResponse();
        osiv.preHandle((HttpServletRequest) request, (HttpServletResponse) response, controller);
        assertTrue(ThreadLocalTransactionManager.isReadOnly() == isReadOnly);
        osiv.postHandle((HttpServletRequest) request, (HttpServletResponse) response, controller, null);
        osiv.afterCompletion((HttpServletRequest) request, (HttpServletResponse) response, controller, e);
        ThreadLocalTransactionManager.commitOrRollback();
    }
}
