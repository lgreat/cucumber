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
                StringBuffer newUrl = new StringBuffer("http://www.greatschools.net");
                String uri = request.getRequestURI();

                // Handle URL's rewritten behind the scenes by Apache
                if ("/school/overview.page".equals(uri)) {
                    newUrl.append("/modperl/browse_school/").append(request.getParameter("state")).append("/").append(request.getParameter("id"));
                } else if ("/school/research.page".equals(uri)) {
                    newUrl.append("/modperl/go/").append(request.getParameter("state"));
                } else {
                    // Otherwise use the same URI and request parameters
                    newUrl.append(uri);
                    if (request.getQueryString() != null) newUrl.append("?").append(request.getQueryString());
                }
                
                // Do the redirect
                response.setStatus(301);
                String redirect = response.encodeRedirectURL(newUrl.toString());
                response.sendRedirect(redirect);
                return false;
            }
        }
        return true;
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
