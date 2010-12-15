package gs.web.util;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;

/**
 * Interceptor to set http cache headers
 *
 * @author thuss
 */
public class HttpCacheInterceptor implements HandlerInterceptor {
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_PRAGMA = "Pragma";
    public static final String HEADER_EXPIRES = "Expires";
    public static final String CACHE_CONTROL_MAX_AGE_PROPERTY = "cachetime";
    public static final int CACHE_CONTROL_MAX_AGE_DEFAULT = (60 * 60 * 24 * 7);
    public static final int EXPIRE_AT_END_OF_SESSION = -1;
    public static final int EXPIRE_NOW = 0;

    public static int getCacheControlMaxAge() {
        String cacheTime = System.getProperty(CACHE_CONTROL_MAX_AGE_PROPERTY);
        if (cacheTime != null) {
            try {
                int cacheControlMaxAge = Integer.parseInt(cacheTime);
                if (cacheControlMaxAge < 0) {
                    cacheControlMaxAge = 0;
                }
                return cacheControlMaxAge;
            } catch (NumberFormatException e) {
                return CACHE_CONTROL_MAX_AGE_DEFAULT;
            }
        } else {
            return CACHE_CONTROL_MAX_AGE_DEFAULT;
        }
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        return true;
    }

    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse response, Object controller, ModelAndView modelAndView) throws Exception {
        // If the controller has already set Cache-Control headers do nothing
        if (!response.containsHeader("Cache-Control")) {
            if (controller instanceof CacheablePageController) {
                setCacheHeaders(response);
            } else {
                setNoCacheHeaders(response);
            }
        }
    }

    public void setNoCacheHeaders(HttpServletResponse response) {
        response.setHeader(HEADER_CACHE_CONTROL, "no-cache");
        response.setHeader(HEADER_PRAGMA, "no-cache");
        response.setDateHeader(HEADER_EXPIRES, EXPIRE_NOW);
    }

    /**
     * This method is public so it can be used by HttpCacheFilter (a servlet filter)
     */
    public void setCacheHeaders(HttpServletResponse response) {
        response.setHeader(HEADER_CACHE_CONTROL, "public, max-age=" + getCacheControlMaxAge());
        response.setDateHeader(HEADER_EXPIRES, new Date().getTime() + 1000 * getCacheControlMaxAge());
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        //do nothing
    }
}
