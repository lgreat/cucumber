package gs.web.util;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletRequest;

import gs.web.util.context.SessionContextInterceptor;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;

import java.util.Date;

/**
 * The read-write intercepter examines a controller to see if it implements the read-write
 * controller interface. If it does NOT it sets the ThreadLocalTransactionManager to read-only
 * so that all database connections will be load balanced across the read-only replicated
 * databases.
 *
 *  @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class ReadWriteInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object controller) throws Exception {
        // Unless it's a read-write controller, default to the read-only databases
        if (!(controller instanceof ReadWriteController)) {
            ThreadLocalTransactionManager.setReadOnly();
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
