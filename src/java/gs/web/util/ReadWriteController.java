package gs.web.util;

import org.springframework.web.servlet.mvc.Controller;

/**
 * This interface is used by the OpenSessionInViewInterceptor to determine whether to give
 * the controller the read-write databases or the read-only databases
 *
 * @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public interface ReadWriteController extends Controller {
}
