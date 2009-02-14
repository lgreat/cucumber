package gs.web.api.admin;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import java.io.IOException;

import gs.web.util.UrlBuilder;

/**
 * This class intercepts all requests to annotation-based controllers and checks
 * to see if the request matches a password-pretected path.  If so, then the
 * request is redirected to a login page.
 *
 * @author chriskimm@greatschools.net
 */
public class AuthInterceptor extends HandlerInterceptorAdapter {

    public static final String API_ADMIN_COOKIE = "api_admin";

    protected final Log _log = LogFactory.getLog(getClass());

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (authorized(request)) {
            return true;
        } else {
            String url = request.getRequestURL().toString();
            String query = request.getQueryString();
            StringBuilder sb = new StringBuilder();
            sb.append(url).append("?").append(query);
            UrlBuilder loginUrl = new UrlBuilder(UrlBuilder.API_ADMIN_LOGIN, null, sb.toString());
            String targetView = loginUrl.asFullUrl(request);
            response.sendRedirect(targetView);
            return false;
        }
    }

    boolean authorized(HttpServletRequest request) {
        boolean authorized = false;
        String uri = request.getRequestURI();
        if (uri != null && uri.contains("api/admin/")) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie: cookies) {
                    if (API_ADMIN_COOKIE.equals(cookie.getName())) {
                        authorized = true;
                    }
                }
            }
        } else {
            authorized = true;
        }
        return authorized;
    }
}
