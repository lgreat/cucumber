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
public class HttpCacheHeaderInterceptor implements HandlerInterceptor {
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_PRAGMA = "Pragma";
    public static final String HEADER_EXPIRES = "Expires";
    public static final int EXPIRE_AT_END_OF_SESSION = -1;
    public static final int EXPIRE_NOW = 0;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        return true;
    }

    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse response, Object controller, ModelAndView modelAndView) throws Exception {
        // If the controller has already set some Cache-Control headers
        if (!response.containsHeader("Cache-Control")) {
            if (controller instanceof CacheablePageController) {
                response.setHeader(HEADER_CACHE_CONTROL, "public; max-age: 600");
                response.setHeader(HEADER_PRAGMA, "");
                response.setDateHeader(HEADER_EXPIRES, new Date().getTime() + 600000);
            } else {
                response.setHeader(HEADER_CACHE_CONTROL, "no-cache");
                response.setHeader(HEADER_PRAGMA, "no-cache");
                response.setDateHeader(HEADER_EXPIRES, 0);
            }
        }
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        //do nothing
    }
}
