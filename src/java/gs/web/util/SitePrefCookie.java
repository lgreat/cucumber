package gs.web.util;

import gs.web.util.context.SubCookie;
import gs.web.util.context.SessionContextUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.CookieGenerator;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SitePrefCookie extends SubCookie {
    public SitePrefCookie(HttpServletRequest request, HttpServletResponse response){
        super(request, response);
    }

    protected CookieGenerator getCookieGenerator() {
        return SessionContextUtil
                .getSessionContext(getRequest())
                .getSessionContextUtil()
                .getSitePrefCookieGenerator();
    }
}
