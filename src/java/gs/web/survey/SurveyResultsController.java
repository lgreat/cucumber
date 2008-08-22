package gs.web.survey;

import gs.data.survey.ISurveyDao;
import gs.data.survey.SurveyResults;
import gs.data.school.School;
import gs.data.school.ISchoolDao;
import gs.web.school.SchoolPageInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller delivers the survey results page.  Required query params:
 * <ul>
 * <li>id (School id></li>
 * <li>state (2-letter abbrev)</li>
 * <li>level (Survey level code)</li>
 * </ul>
 *
 * @author chriskimm@greatschools.net
 */
public class SurveyResultsController extends AbstractController {

    public static final String BEAN_ID = "surveyResultsController";

    private ISurveyDao _surveyDao;
    private ISchoolDao _schoolDao;

    private static final String VIEW_NAME = "survey/results";
    private static final String MODEL_NAME = "results";
    private static final String LEVEL_PARAM = "level";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // This controller is configured to be school-aware in pages-servlet.xml
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);

        String level = request.getParameter(LEVEL_PARAM);
        SurveyResults results = getSurveyDao().getSurveyResultsForSchool(level, school);
        ModelAndView mAndV = new ModelAndView(VIEW_NAME);
        mAndV.getModel().put(MODEL_NAME, results);
        return mAndV;
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
}
