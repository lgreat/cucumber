package gs.web.school;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.LastModified;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.school.School;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.state.State;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlBuilder;
import gs.web.util.RedirectView301;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;

import java.util.List;

/**
 * This class is intended to be the base class for School Profile pages and other pages that need
 * access to a single school.  The state and the school id are required url parameters.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public abstract class AbstractSchoolController extends WebContentGenerator implements Controller {

    private static final Logger _log = Logger.getLogger(AbstractSchoolController.class);
    private ISchoolDao _schoolDao;

    /** The name of the error view - see pages-servlet.xml */
    private String _errorViewName = "/school/error";

    /** Used when storing the school in the reqest */
    public static final String SCHOOL_ATTRIBUTE = "school";

    /** Used when storing the school id string in the reqest */
    public static final String SCHOOL_ID_ATTRIBUTE = "schoolIdStr";

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
                        // if it's a preschool, 301-redirect to the directory-structure url instead of the old-style url 
                        if (this instanceof SchoolOverviewController && LevelCode.PRESCHOOL.equals(s.getLevelCode())) {
                            UrlBuilder urlBuilder = new UrlBuilder(s, UrlBuilder.SCHOOL_PROFILE);
                            return new ModelAndView(new RedirectView301(urlBuilder.asSiteRelative(request)));
                        }
                        request.setAttribute(SCHOOL_ATTRIBUTE, s);
                        return handleRequestInternal(request, response);
                    }
                } catch (Exception e) {
                    _log.warn("Could not get a valid or active school: " +
                            schoolId + " in state: " + state, e);
                }
            } else if (this instanceof SchoolOverviewController) {
                DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);
                if (shouldHandleRequest(fields)) {
                    List<School> schools = _schoolDao.findSchoolsInCityByName(fields.getState(), fields.getCityName(), fields.getSchoolName());
                    if (schools != null && schools.size() == 1 && schools.get(0).isActive()) {
                        request.setAttribute(SCHOOL_ATTRIBUTE, schools.get(0));
                        request.setAttribute(SCHOOL_ID_ATTRIBUTE, String.valueOf(schools.get(0).getId()));

                        UrlBuilder urlBuilder = new UrlBuilder(schools.get(0), UrlBuilder.SCHOOL_PROFILE);
                        // redirect if discrepancy between expected url and actual url, e.g. due to uppercase/lowercase in school name
                        if (!request.getRequestURI().equals(urlBuilder.asSiteRelative(request))) {
                            return new ModelAndView(new RedirectView(urlBuilder.asSiteRelative(request)));
                        }

                        return handleRequestInternal(request, response);
                    }
                }
            }
        }

        // display error view.
        return new ModelAndView(getErrorViewName());
    }


    // required to implement IDirectoryStructureUrlController
    public boolean shouldHandleRequest(DirectoryStructureUrlFields fields) {
        if (fields == null) {
            return false;
        }
        return fields.hasState() && fields.hasCityName() &&
            fields.hasSchoolTypes() && fields.getSchoolTypes().isEmpty() &&
            // this line about level codes would have to be changed if non-preschools are to be supported
            fields.hasLevelCode() && fields.getLevelCode().equals(LevelCode.PRESCHOOL) &&
            fields.hasSchoolName();
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
