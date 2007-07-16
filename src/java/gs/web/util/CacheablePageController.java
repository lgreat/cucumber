package gs.web.util;

import org.springframework.web.servlet.mvc.Controller;

/**
 * This interface is used by the ResponseInterceptor to determine whether to
 * make this page cacheable by the proxies by setting the appropriate headers
 *
 * Note because cookies are disabled with cacheable pages it means we can't
 * reliably do AB testing on them 
 *
 * @author thuss
 */
public interface CacheablePageController extends Controller {
}
