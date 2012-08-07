package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolHelper;
import gs.web.FruitcakeControllerFamilyResolver;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.request.RequestAttributeHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlBuilder.VPage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.LastModified;
import org.springframework.web.servlet.support.WebContentGenerator;

/**
 * This class is intended to be the base class for School Profile pages and other pages that need
 * access to a single school.  The state and the school id are required url parameters.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public abstract class AbstractSchoolController extends WebContentGenerator implements Controller {

    public static enum NewProfileTabs {
        overview, reviews, testScores("test-scores"), ratings, demographics, teachers,
        programsCulture("programs-culture"), programsResources("programs-resources"), extracurriculars, culture;

        private String _parameterValue;
        NewProfileTabs() {
            _parameterValue = name();
        }
        NewProfileTabs(String parameterValue) {
            _parameterValue=parameterValue;
        }
        public String getParameterValue() {
            return _parameterValue;
        }
    }


    private static VPage resolveVPage(DirectoryStructureUrlFields fields) {
        if(fields.getExtraResourceIdentifier() != null) {
            switch(fields.getExtraResourceIdentifier()) {
                case ESP_DISPLAY_PAGE:
                    return UrlBuilder.SCHOOL_PROFILE_ESP_DISPLAY;
            }
        }
        // default
        return UrlBuilder.SCHOOL_PROFILE;
    }

    private static final Logger _log = Logger.getLogger(AbstractSchoolController.class);
    private ISchoolDao _schoolDao;
    RequestAttributeHelper _requestAttributeHelper;

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
        School s = _requestAttributeHelper.getSchool(request);
        if (s != null) {
            DirectoryStructureUrlFields fields = RequestAttributeHelper.getDirectoryStructureUrlFields(request);
            if (s.isActive() || s.isDemoSchool()) {
                // if it's a preschool on an old-style url, 301-redirect to the directory-structure url
                if ((fields == null || !fields.hasSchoolID())
                        && this instanceof SchoolOverview2010Controller && LevelCode.PRESCHOOL.equals(s.getLevelCode())) {
                    UrlBuilder urlBuilder = new UrlBuilder(s, UrlBuilder.SCHOOL_PROFILE);
                    return new ModelAndView(new RedirectView301(urlBuilder.asFullUrl(request)));
                }
                if (fields != null && fields.hasSchoolID() && fields.hasState() && fields.hasCityName()) {
                    VPage vpage = resolveVPage(fields);
                    assert vpage != null;
                    UrlBuilder urlBuilder = new UrlBuilder(s, vpage);
                    // 301-redirect if discrepancy between expected url and actual url, e.g. due to uppercase/lowercase in school name or change in school name
                    String ruri = request.getRequestURI(), urlb = urlBuilder.asSiteRelative(request);
                    if (!ruri.equals(urlb)) {
                        return new ModelAndView(new RedirectView301(urlBuilder.asFullUrl(request)));
                    }
                }
                request.setAttribute(SCHOOL_ID_ATTRIBUTE, String.valueOf(s.getId()));
                return handleRequestInternal(request, response);
            } else if (fields != null && fields.hasCityName()) {
                // GS-9940 Redirect requests for inactive schools to the city home
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.CITY_PAGE, s.getDatabaseState(), fields.getCityName());
                urlBuilder.addParameter("noSchoolAlert", "1");
                return new ModelAndView(new RedirectView301(urlBuilder.asSiteRelative(request)));
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

    public static boolean shouldRedirectToNewProfile(School school) {
        return school != null && SchoolHelper.isSchoolForNewProfile(school);
    }

    public static ModelAndView getRedirectToNewProfileModelAndView(School school, HttpServletRequest request, NewProfileTabs tab) {
        UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        urlBuilder.addParametersFromRequest(request);
        // filter out certain parameters we don't want passed through
        urlBuilder.removeParameter("tab");
        urlBuilder.removeParameter("state");
        urlBuilder.removeParameter("id");
        if (tab != null && tab != NewProfileTabs.overview) {
            urlBuilder.addParameter("tab", tab.getParameterValue());
        }
        return new ModelAndView(new RedirectView301(urlBuilder.asSiteRelative(request)));
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

    public RequestAttributeHelper getRequestAttributeHelper() {
        return _requestAttributeHelper;
    }

    public void setRequestAttributeHelper(RequestAttributeHelper requestAttributeHelper) {
        _requestAttributeHelper = requestAttributeHelper;
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
