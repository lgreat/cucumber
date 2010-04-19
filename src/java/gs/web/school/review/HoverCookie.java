package gs.web.school.review;

import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SubCookie;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Samson Sprouse <mailto:ssprouse@greatschools.org>
 */
public class HoverCookie extends SubCookie {
    public HoverCookie(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Override
    protected CookieGenerator getCookieGenerator() {
        return SessionContextUtil
                .getSessionContext(getRequest())
                .getSessionContextUtil()
                .getHoverCookieGenerator();
    }
}