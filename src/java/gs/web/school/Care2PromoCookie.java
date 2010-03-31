package gs.web.school;

import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SubCookie;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Young Fan
 */
public class Care2PromoCookie extends SubCookie {
    public Care2PromoCookie(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Override
    protected CookieGenerator getCookieGenerator() {
        return SessionContextUtil
                .getSessionContext(getRequest())
                .getSessionContextUtil()
                .getCare2PromoCookieGenerator();
    }
}
