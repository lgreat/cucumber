package gs.web.survey;

import gs.data.survey.ISurveyDao;
import gs.data.survey.SurveyResults;
import gs.data.school.School;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.web.request.RequestInfo;
import gs.web.school.SchoolPageInterceptor;
import gs.web.school.SchoolProfileHeaderHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This controller delivers the survey results page.  Required query params:
 * <ul>
 * <li>id (School id></li>
 * <li>state (2-letter abbrev)</li>
 * <li>level (Survey level code)</li>
 * </ul>
 *
 * @author chriskimm@greatschools.org
 */
public class SurveyResultsController extends AbstractController {

    public static final String BEAN_ID = "surveyResultsController";

    private ISurveyDao _surveyDao;
    private ISchoolDao _schoolDao;
    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;

    private static final String VIEW_NAME = "survey/results";
    private static final String MODEL_NAME = "results";
    private static final String MODEL_LEVEL = "level";
    private static final String LEVEL_PARAM = "level";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // This controller is configured to be school-aware in pages-servlet.xml
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);

        // Preschool profile pages should be hosted from pk.greatschools.org (GS-12127). Redirect if needed
        ModelAndView preschoolRedirectMAndV = getPreschoolRedirectViewIfNeeded(request, school);

        String level = request.getParameter(LEVEL_PARAM);
        String levelString = null;
        Set<LevelCode.Level> levelCodes = school.getLevelCode().getIndividualLevelCodes();
        if (levelCodes.size() > 1) {
            LevelCode.Level lev = LevelCode.Level.getLevelCode(level);
            if (LevelCode.Level.PRESCHOOL_LEVEL.equals(lev)) {
                levelString = lev.getLongName();
            } else {
                levelString = lev.getLongName() + " school";
            }
        }

        Map<String, Object> model = new HashMap<String, Object>();

        _schoolProfileHeaderHelper.updateModel(request, response, school, model);

        SurveyResults results = getSurveyDao().getSurveyResultsForSchool(level, school);
        model.put(MODEL_NAME, results);
        model.put(MODEL_LEVEL, levelString);
        return new ModelAndView(VIEW_NAME, model);
    }

    public ModelAndView getPreschoolRedirectViewIfNeeded(HttpServletRequest request, School school) {
        ModelAndView modelAndView = null;
        if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
            RequestInfo hostnameInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);
            if (!hostnameInfo.isOnPkSubdomain() && hostnameInfo.isPkSubdomainSupported()) {
                UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SURVEY_RESULTS);
                String level = request.getParameter("level");
                StringBuffer buffer = new StringBuffer(urlBuilder.asFullUrl(request));
                if (level != null) {
                    buffer.append("&level=");
                    buffer.append(level);
                }
                modelAndView = new ModelAndView(new RedirectView301(buffer.toString()));
            }
        }
        return modelAndView;
    }

    public ISurveyDao getSurveyDao() {
        return _surveyDao;
    }

    public void setSurveyDao(ISurveyDao surveyDao) {
        _surveyDao = surveyDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public SchoolProfileHeaderHelper getSchoolProfileHeaderHelper() {
        return _schoolProfileHeaderHelper;
    }

    public void setSchoolProfileHeaderHelper(SchoolProfileHeaderHelper schoolProfileHeaderHelper) {
        _schoolProfileHeaderHelper = schoolProfileHeaderHelper;
    }
}
