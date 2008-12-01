package gs.web.school.review;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import gs.data.state.StateManager;
import gs.data.state.State;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Young Fan
 */
public class SubmitParentFeedbackController extends SimpleFormController {
    private static final Logger _log = Logger.getLogger(SubmitParentFeedbackController.class);
    
    /** Spring config id */
    public static final String BEAN_ID = "/school/parentReviews/submit.page";

    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;

    private String _title;
    private String _levelCodes;
    private String _omniturePageName;
    private String _omnitureHierarchy;

    protected ModelAndView processFormSubmission(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 Object cmdObject,
                                                 BindException errors) {
        SubmitParentFeedbackCommand command = (SubmitParentFeedbackCommand) cmdObject;

        SessionContext context = SessionContextUtil.getSessionContext(request);
        State state = context.getState();

        School school = _schoolDao.getSchoolById(state, Integer.parseInt(command.getSid()));

        UrlBuilder builder = new UrlBuilder(school, UrlBuilder.SCHOOL_TAKE_SURVEY);
        builder.setParameter("level","p");
        View view = new RedirectView(builder.asSiteRelative(request));
        return new ModelAndView(view);
    }

    protected Map referenceData(HttpServletRequest request, Object cmd, Errors errors) throws Exception {
        Map<String, Object> refData = new HashMap<String, Object>();

        refData.put("title", _title);
        refData.put("levelCodes", _levelCodes);
        refData.put("pageName", _omniturePageName);
        refData.put("hier1", _omnitureHierarchy);

        SessionContext context = SessionContextUtil.getSessionContext(request);
        State state = context.getState();

        if (state != null) {
            refData.put("cities", _geoDao.findCitiesByState(state));
        }
        return refData;
    }

    public void setTitle(String title) {
        _title = title;
    }

    public void setLevelCodes(String levelCodes) {
        _levelCodes = levelCodes;
    }

    public void setOmniturePageName(String omniturePageName) {
        _omniturePageName = omniturePageName;
    }

    public void setOmnitureHierarchy(String omnitureHierarchy) {
        _omnitureHierarchy = omnitureHierarchy;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}
