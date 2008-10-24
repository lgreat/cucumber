package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import static gs.data.util.XMLUtil.*;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.validation.ObjectError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

/**
 * Interceptor that puts a school into current request if request parameters contain valid id (or schoolId) and state
 *
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class SchoolPageInterceptor extends HandlerInterceptorAdapter {
    protected final Log _log = LogFactory.getLog(getClass());
    private ISchoolDao _schoolDao;
    private Boolean _showXmlErrorPage = Boolean.FALSE;

    /**
     * Used when storing the school in the reqest
     */
    public static final String SCHOOL_ATTRIBUTE = "school";

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException, ServletException, ParserConfigurationException, TransformerException {
        // make sure we have a valid school
        State state = SessionContextUtil.getSessionContext(request).getState();
        if (state != null) {
            String schoolId = request.getParameter("id");
            if (StringUtils.isBlank(schoolId)) {
                schoolId = request.getParameter("schoolId");
            }
            if (StringUtils.isNotBlank(schoolId)) {
                try {
                    Integer id = new Integer(schoolId);
                    School s = _schoolDao.getSchoolById(state, id);
                    if (s.isActive()) {
                        request.setAttribute(SCHOOL_ATTRIBUTE, s);
                        return true;
                    }
                } catch (Exception e) {
                    _log.warn("Could not get a valid or active school: " +
                            schoolId + " in state: " + state, e);
                }
            }
        }

        // If we get this far we have an error
        if (_showXmlErrorPage) {
            showXmlErrorPage(response);
        } else {
            request.getRequestDispatcher("/school/error.page").include(request, response);
        }
        return false;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public void setShowXmlErrorPage(Boolean showXmlErrorPage) {
        this._showXmlErrorPage = showXmlErrorPage;
    }

    private void showXmlErrorPage(HttpServletResponse response) throws ParserConfigurationException, TransformerException, IOException {
        Document doc = getDocument("errors");
        Element errorElem = appendElement(doc, "error", "School could not be found, please make sure you specify a valid schoolId and state.");
        errorElem.setAttribute("key", "school_not_found");
        response.setContentType("application/xml");
        serializeDocument(response.getWriter(), doc);
        response.getWriter().flush();
    }
}

