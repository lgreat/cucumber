package gs.web.util;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;

/**
 * @see <a href="http://wiki.greatschools.org/bin/view/Greatschools/DatabaseConnectionManagement">Wiki Documentation</a>
 * 
 * The read-write intercepter examines a controller to see if it implements the read-write
 * controller interface. If it does NOT it sets the ThreadLocalTransactionManager to read-only
 * so that all database connections will be load balanced across the read-only replicated
 * databases.
 *
 *  @author <a href="mailto:thuss@greatschools.org">Todd Huss</a>
 */
public class ReadWriteInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object controller) throws Exception {
        // Unless it's a read-write controller, default to the read-only databases
        if (!(controller instanceof ReadWriteController) && !(controller instanceof ReadWriteAnnotationController)) {
            ThreadLocalTransactionManager.setReadOnly();
        } else {
            // Make sure we're not inheriting any read only connections
            ThreadLocalTransactionManager.commitOrRollback();
        }
        return true;
    }

    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object controller, ModelAndView modelAndView) throws Exception {
        // Do nothing
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object controller, Exception e) throws Exception {
        // Do nothing
    }
}
