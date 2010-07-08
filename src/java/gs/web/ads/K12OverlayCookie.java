package gs.web.ads;

import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SubCookie;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class K12OverlayCookie extends SubCookie {
    public K12OverlayCookie(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Override
    protected CookieGenerator getCookieGenerator() {
        return SessionContextUtil
                .getSessionContext(getRequest())
                .getSessionContextUtil()
                .getK12OverlayCookieGenerator();
    }
}
