package gs.web.util;

import gs.web.util.context.SessionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletRequest;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.HandlerInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

/**
 * Per GS-3281 This code 301 redirects crawlers from cobrands onto our main website
 * to avoid duplicate content and insure our main site gets the inbound link credit
 *
 * @author thuss
 */
public class SeoCobrandRedirectInterceptor implements HandlerInterceptor {

    private static final Log _log = LogFactory.getLog(SeoCobrandRedirectInterceptor.class);

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        SessionContext sessionContext = (SessionContext) request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);

        String uri = request.getRequestURI();
        if (sessionContext.isCobranded() && sessionContext.isCrawler() && "GET".equals(request.getMethod()) && !"/robots.txt".equals(uri)) {
            // We have to special case Yahoo because as part of our contract Yahoo can crawl their cobrand
            if (!("yahooed".equals(sessionContext.getCobrand()) && request.getHeader("User-Agent").indexOf("Slurp") > -1)) {
                StringBuffer newUrl = new StringBuffer("http://www.greatschools.net");

                // Handle URL's rewritten behind the scenes by Apache
                if ("/index.page".equals(uri)) {
                    newUrl.append("/");
                } else if ("/school/overview.page".equals(uri)) {
                    newUrl.append("/modperl/browse_school/").append(request.getParameter("state")).append("/").append(request.getParameter("id"));
                } else if ("/school/research.page".equals(uri)) {
                    newUrl.append("/modperl/go/").append(request.getParameter("state"));
                } else {
                    // Otherwise use the same URI and request parameters
                    newUrl.append(uri);
                    if (request.getQueryString() != null) newUrl.append("?").append(request.getQueryString());
                }

                // Do the redirect
                String redirectUrl = newUrl.toString();
                String redirect = response.encodeRedirectURL(redirectUrl);
                response.setStatus(301);
                response.setHeader("Location", redirect);
                response.setHeader("Connection", "close");
                logRedirect(request, redirectUrl);
                return false;
            }
        }
        return true;
    }

    private void logRedirect(ServletRequest r, String redirectUrl) {
        StringBuffer url = null;
        String userAgent = null;
        String remoteIp = null;
        String referrer = null;
        if (r instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) r;
            // Reconstruct the URL
            url = request.getRequestURL();
            if (request.getQueryString() != null) url.append("?").append(request.getQueryString());
            userAgent = request.getHeader("User-Agent");
            remoteIp = request.getRemoteAddr();
            referrer = request.getHeader("Referer");
        }
        _log.warn(new Date().toString() + " REDIRECT TO: " + redirectUrl + " REQUEST: " + url + " REFERRER: " + referrer +
                " REMOTEIP: " + remoteIp + " USER-AGENT: " + userAgent);
    }

    public void postHandle
            (HttpServletRequest
                    request, HttpServletResponse
                    response, Object
                    o, ModelAndView
                    mv) throws Exception {
        // do nothing
    }

    public void afterCompletion
            (HttpServletRequest
                    request, HttpServletResponse
                    response, Object
                    o, Exception
                    e) throws Exception {
        // do nothing
    }

}
