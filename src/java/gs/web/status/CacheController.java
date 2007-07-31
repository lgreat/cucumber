package gs.web.status;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Statistics;

/**
 * Displays status information about the ehcache
 *
 * @author thuss
 */
public class CacheController extends AbstractController {

    public static final String BEAN_ID = "/status/cache.page";

    public static final String MODEL_CACHES = "caches";

    public static final String MODEL_STATS = "stats";

    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Map> model = new HashMap<String, Map>();
        CacheManager manager = CacheManager.create();
        String[] cacheNames = manager.getCacheNames();
        Map<String, Cache> caches = new HashMap<String, Cache>();
        Map<String, Statistics> stats = new HashMap<String, Statistics>();
        for (String cacheName : cacheNames) {
            caches.put(cacheName, manager.getCache(cacheName));
            stats.put(cacheName, manager.getCache(cacheName).getStatistics());
        }
        model.put(MODEL_CACHES, caches);
        model.put(MODEL_STATS, stats);
        return new ModelAndView(_viewName, model);
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
