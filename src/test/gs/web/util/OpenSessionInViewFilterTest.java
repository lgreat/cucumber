package gs.web.util;

import gs.web.BaseControllerTestCase;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class OpenSessionInViewFilterTest extends BaseControllerTestCase {

    public void testOsivFilter() throws IOException, ServletException {
        OpenSessionInViewFilter osiv = new OpenSessionInViewFilter();
        ServletRequest request = getRequest();
        ServletResponse response = getResponse();
        MockFilterChain chain = new MockFilterChain();

        // Test transaction management
        ThreadLocalTransactionManager.setReadOnly();
        osiv.doFilter(request, response, chain);
        // Make sure the osiv executes the transaction commit so it should
        // no longer be read-only
        assert(!ThreadLocalTransactionManager.isReadOnly());

        // This time with an exception
        ThreadLocalTransactionManager.setReadOnly();
        chain.setException(new ServletException());
        try {
            osiv.doFilter(request, response, chain);
            fail("OSIV filter should have thrown an exception");
        } catch (Exception e) {
            // Do nothing
        }
        // Make sure the osiv executes the transaction commit so it should
        // no longer be read-only
        assert(!ThreadLocalTransactionManager.isReadOnly());

    }

}
