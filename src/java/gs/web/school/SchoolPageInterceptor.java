package gs.web.school;

import gs.data.school.School;
import static gs.data.util.XMLUtil.*;

import gs.web.request.RequestAttributeHelper;
import gs.web.util.UrlBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Interceptor that puts a school into current request if request parameters contain valid id (or schoolId) and state
 *
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 */
public class SchoolPageInterceptor extends HandlerInterceptorAdapter {
    protected final Log _log = LogFactory.getLog(getClass());
    private RequestAttributeHelper _requestAttributeHelper;
    private Boolean _showXmlErrorPage = Boolean.FALSE;
    private Boolean _showJsonErrorPage = Boolean.FALSE;

    /**
     * Used when storing the school in the reqest
     */
    public static final String SCHOOL_ATTRIBUTE = "school";

    /**
     * output format, defaults to HTML but can also be set to json or xml
     */
    public static final String OUTPUT_PARAMETER = "output";

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException, ServletException, ParserConfigurationException, TransformerException {
        // make sure we have a valid school
        School s = _requestAttributeHelper.getSchool(request);
        if (s != null && (s.isActive() || s.isDemoSchool())) {
            return true;
        }

        _log.warn("Could not get a valid or active school: " +
                RequestAttributeHelper.getSchoolId(request) + " in state: " + RequestAttributeHelper.getState(request));


        if (s != null) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.CITY_PAGE, s.getDatabaseState(), s.getCity());
            urlBuilder.addParameter("noSchoolAlert", "1");
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", urlBuilder.asFullUrl(request));
            return false;
        }

        // If we get this far we have an error, now determine the error format as html, json, or xml
        if ("xml".equals(request.getParameter(OUTPUT_PARAMETER))) showXmlErrorPage(response);
        else if ("json".equals(request.getParameter(OUTPUT_PARAMETER))) showJsonErrorPage(response);
        else if ("html".equals(request.getParameter(OUTPUT_PARAMETER))) showHtmlErrorPage(request, response);
        else if (_showXmlErrorPage) showXmlErrorPage(response);
        else if (_showJsonErrorPage) showJsonErrorPage(response);
        else showHtmlErrorPage(request, response);
        return false;
    }

    public RequestAttributeHelper getRequestAttributeHelper() {
        return _requestAttributeHelper;
    }

    public void setRequestAttributeHelper(RequestAttributeHelper requestAttributeHelper) {
        _requestAttributeHelper = requestAttributeHelper;
    }

    public void setShowXmlErrorPage(Boolean showXmlErrorPage) {
        this._showXmlErrorPage = showXmlErrorPage;
    }

    public void setShowJsonErrorPage(Boolean showJsonErrorPage) {
        _showJsonErrorPage = showJsonErrorPage;
    }

    protected void showXmlErrorPage(HttpServletResponse response) throws ParserConfigurationException, TransformerException, IOException {
        Document doc = getDocument("errors");
        Element errorElem = appendElement(doc, "error", "School could not be found, please make sure you specify a valid schoolId and state.");
        errorElem.setAttribute("key", "school_not_found");
        response.setContentType("application/xml");
        serializeDocument(response.getWriter(), doc);
        response.getWriter().flush();
    }

    protected void showJsonErrorPage(HttpServletResponse response) throws IOException {
        StringBuffer buff = new StringBuffer(400);
        buff.append("{\"status\":false,\"errors\":");
        buff.append("[");
        buff.append("\"").append("School could not be found, please make sure you specify a valid schoolId and state.").append("\"");
        buff.append("]}");
        response.setContentType("text/x-json");
        response.getWriter().print(buff.toString());
        response.getWriter().flush();
    }

    protected void showHtmlErrorPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        request.getRequestDispatcher("/school/error.page").include(request, response);
    }
}

