package gs.web.jsp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.net.URLEncoder;

/**
 * Populate a variable with the absolute path portion, plus query args, of the current request URL as seen
 * in the browser.  This has been tested in top level jspx, and modules.  It is possible that it will be incorrect in
 * an included jsp or some other scenario.
 *
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class RequestUtilTagHandler extends TagSupport {
    private static final Log _log = LogFactory.getLog(RequestUtilTagHandler.class);
    private String           _var;
    private boolean          _urlEncode = false;

    public int doStartTag() throws JspException {
        if (_var != null && _var.length() > 0) {
            HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
            String currentAbsolutePath = currentAbsolutePath(request);
            if (currentAbsolutePath != null && currentAbsolutePath.length() > 0) {
                if (_urlEncode) {
                    try {
                        currentAbsolutePath = URLEncoder.encode(currentAbsolutePath, "UTF-8");
                    } catch (java.io.UnsupportedEncodingException uee) {
                        // do nothing, return unencoded string as is
                        _log.warn("RequestUtilTagHandler: UnsupportedEncodingException 'UTF-8'");
                    }
                }

                pageContext.setAttribute(_var, currentAbsolutePath, PageContext.PAGE_SCOPE);
            }
        }

        return SKIP_BODY;
    }

    private String currentAbsolutePath(HttpServletRequest request) {
        StringBuffer uri = new StringBuffer((String)request.getAttribute("javax.servlet.forward.request_uri"));
        String query = (String)request.getAttribute("javax.servlet.forward.query_string");
        if (query != null && query.length() > 0) {
            uri.append("?").append(query);
        }

        return uri.toString();
    }

    public String getVar() {
        return _var;
    }

    public void setVar(String var) {
        _var = var;
    }

    public boolean isUrlEncode() {
        return _urlEncode;
    }

    public void setUrlEncode(boolean urlEncode) {
        _urlEncode = urlEncode;
    }
}