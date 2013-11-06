package gs.web.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author thuss
 */
public class CookieUtil {

    public static void setCookie(HttpServletResponse response, String cookieName, String value, int maxAge) {
            Cookie cookie = new Cookie(cookieName, new Date().toString());
            cookie.setPath("/");
            cookie.setValue(value);
            cookie.setDomain(".greatschools.org");
            cookie.setMaxAge(maxAge);
            response.addCookie(cookie);
    }

    public static Cookie getCookie(HttpServletRequest request, String cookieName) {
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }


    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static boolean hasCookie(HttpServletRequest request, String cookieName) {
        return getCookie(request, cookieName) != null;
    }
}
