package gs.web.util;

import gs.web.util.context.SessionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Per GS-3281 This code 301 redirects crawlers from cobrands onto our main website
 * to avoid duplicate content and insure our main site gets the inbound link credit
 *
 * @author thuss
 */
public class SeoCobrandRedirectInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        SessionContext sessionContext = (SessionContext) request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);
        if (sessionContext.isCobranded() && sessionContext.isCrawler() && "GET".equals(request.getMethod())) {
            // We have to special case Yahoo because as part of our contract Yahoo can crawl their cobrand
            if (!("yahooed".equals(sessionContext.getCobrand()) && request.getHeader("User-Agent").indexOf("Slurp") > -1)) {
                // Build the new URL
                StringBuffer url = new StringBuffer("http://www.greatschools.net").append(request.getRequestURI());
                if (request.getQueryString() != null) url.append("?").append(request.getQueryString());
                // Do the redirect
                response.setStatus(301);
                String redirect = response.encodeRedirectURL(url.toString());
                response.sendRedirect(redirect);
                return false;
            }
        }
        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView mv) throws Exception {
        // do nothing
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {
        // do nothing
    }

}
