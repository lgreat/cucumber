package gs.web.util;

import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

public class RedirectView301 extends RedirectView {

    public RedirectView301(String s) {
        super(s);
    }

    protected void sendRedirect(HttpServletRequest request, HttpServletResponse response, String targetUrl, boolean http10Compatible) {
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        response.setHeader("Location", response.encodeRedirectURL(targetUrl));
    }
}
