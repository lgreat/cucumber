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
import gs.data.school.LevelCode;
import gs.data.state.State;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlBuilder;
import gs.web.util.RedirectView301;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;

/**
 * This class is intended to be the base class for School Profile pages and other pages that need
 * access to a single school.  The state and the school id are required url parameters.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
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
                        if (this instanceof SchoolOverview2010Controller && LevelCode.PRESCHOOL.equals(s.getLevelCode())) {
                            UrlBuilder urlBuilder = new UrlBuilder(s, UrlBuilder.SCHOOL_PROFILE);
                            return new ModelAndView(new RedirectView301(urlBuilder.asFullUrl(request)));
                        }
                        request.setAttribute(SCHOOL_ATTRIBUTE, s);
                        return handleRequestInternal(request, response);
                    }
                } catch (Exception e) {
                    _log.warn("Could not get a valid or active school: " +
                            schoolId + " in state: " + state, e);
                }
            } else if (this instanceof SchoolOverview2010Controller) {
                DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);
                //if (shouldHandleRequest(fields)) {
                    try {
                        Integer id = new Integer(fields.getSchoolID());
                        School s = _schoolDao.getSchoolById(state, id);
                        if (s.isActive()) {
                            UrlBuilder urlBuilder = new UrlBuilder(s, UrlBuilder.SCHOOL_PROFILE);
                            // 301-redirect if discrepancy between expected url and actual url, e.g. due to uppercase/lowercase in school name or change in school name
                            if (!request.getRequestURI().equals(urlBuilder.asSiteRelative(request))) {
                                return new ModelAndView(new RedirectView301(urlBuilder.asFullUrl(request)));
                            }

                            request.setAttribute(SCHOOL_ATTRIBUTE, s);
                            request.setAttribute(SCHOOL_ID_ATTRIBUTE, fields.getSchoolID());
                            return handleRequestInternal(request, response);
                        } else {
                            // GS-9940 Redirect requests for inactive schools to the city home
                            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.CITY_PAGE, s.getDatabaseState(), fields.getCityName());
                            urlBuilder.addParameter("noSchoolAlert", "1");
                            return new ModelAndView(new RedirectView301(urlBuilder.asSiteRelative(request)));
                        }
                    } catch (Exception e) {
                        _log.warn("Could not get a valid or active school: " +
                                fields.getSchoolID() + " in state: " + state, e);
                        // GS-9940 Redirect requests for inactive schools to the city home
                        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.CITY_PAGE, state, fields.getCityName());
                        urlBuilder.addParameter("noSchoolAlert", "1");
                        return new ModelAndView(new RedirectView301(urlBuilder.asSiteRelative(request)));
                    }
                //}
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
            // Check for the PK version or the public/private version
            ((fields.hasLevelCode() && fields.getLevelCode().equals(LevelCode.PRESCHOOL) &&
                fields.hasSchoolName() && fields.hasSchoolID()) ||
             (!fields.hasLevelCode() && fields.hasSchoolName() && fields.hasSchoolID()));
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
