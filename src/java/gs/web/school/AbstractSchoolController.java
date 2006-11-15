package gs.web.school;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.LastModified;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.school.School;
import gs.data.school.ISchoolDao;
import gs.data.state.State;
import gs.web.util.context.SessionContextUtil;

/**
 * This class is intended to be the base class for School Profile pages and other pages need
 * access to a single school.  The state and the school id are required url parameters.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public abstract class AbstractSchoolController extends WebContentGenerator implements Controller {

    private static final Logger _log = Logger.getLogger(AbstractSchoolController.class);
    private ISchoolDao _schoolDao;
    protected School _school;

    /** The name of the error view - see pages-servlet.xml */
    private String _errorViewName = "/school/error";

    /**
     * This method populates the protected _school parameter with a School object if a valid,
     * active School is retrieved using the supplied "state" and "id" parameters.  If a
     * school cannot be found, the a configurable error view is returned.
     *
     * @param request Provided by the servlet context
     * @param response Provided by the servlet context
     * @return a ModelAndView type
     * @throws Exception
     */
    public final ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

        // reset the school for this request 
        _school = null;

        // delegate to WebContentGenerator for checking and preparing
		checkAndPrepare(request, response, this instanceof LastModified);

        // make sure we have a valid school
        State state = SessionContextUtil.getSessionContext(request).getState();
        if (state != null) {
            String schoolId = request.getParameter("id");
            if (StringUtils.isNotBlank(schoolId)) {
                try {
                    Integer id = new Integer(schoolId);
                    School s = _schoolDao.getSchoolById(state, id);
                    if (s.isActive()) {
                        _school = s;
                    }
                } catch (Exception e) {
                    _log.warn("Could not get a valid or active school: " +
                            schoolId + " in state: " + state, e);
                }
            }
        }

        if (_school != null) {
            return handleRequestInternal(request, response);
        } else {
            // display error view.
            return new ModelAndView(getErrorViewName());
        }
    }

    public School getSchool() {
        return _school;
    }

    /**
	 * Template method. Subclasses must implement this.
	 * The contract is the same as for <code>handleRequest</code>.
	 * @see #handleRequest
     * @param request An HttpServletRequest
     * @param response An HttpServletResponse
     * @return a ModelAndView type
     * @throws Exception if something is wacky.
	 */
	protected abstract ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
	    throws Exception;


    /**
     * @return An ISchoolDao type
     */
    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    /**
     * Spring setter
     * @param schoolDao used to get school
     */
    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public void setErrorViewName(String viewName) {
        if (StringUtils.isNotBlank(viewName)) {
            _errorViewName = viewName;
        } else {
            throw new IllegalArgumentException ("viewname cannot be blank");
        }
    }

    protected String getErrorViewName() {
        return _errorViewName;
    }
}
