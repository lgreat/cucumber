package gs.web.status;

import org.springframework.web.servlet.ModelAndView;
import gs.web.BaseControllerTestCase;

import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Statistics;

/**
 * @author thuss
 */
public class CacheControllerTest extends BaseControllerTestCase {

    protected CacheController _controller;

    public void setUp() {
        _controller = (CacheController) getApplicationContext().getBean(CacheController.BEAN_ID);
    }

    public void testHandleRequest() throws Exception {
        ModelAndView mv = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNotNull(mv.getViewName());
        
        Map caches = (Map) mv.getModel().get(CacheController.MODEL_CACHES);
        assertTrue(caches.size() > 0);
        assertTrue("Expected this map to contain Cache objects", caches.get(caches.keySet().iterator().next()) instanceof Cache);

        Map stats = (Map) mv.getModel().get(CacheController.MODEL_STATS);
        assertTrue(stats.size() > 0);
        assertTrue("Expected this map to contain Statistics objects", stats.get(stats.keySet().iterator().next()) instanceof Statistics);
    }

}
