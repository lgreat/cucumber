package gs.web.survey;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.survey.ISurveyDao;
import gs.web.school.SchoolPageInterceptor;
import gs.web.school.SchoolProfileHeaderHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SchoolLevelController extends SimpleFormController {

    private ISurveyDao _surveyDao;
    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;

    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException bindException) throws Exception {
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        Set<LevelCode.Level> levelCodes = school.getLevelCode().getIndividualLevelCodes();

        boolean oneLevel = false;
        LevelCode.Level lev = null;
        Map<String, Object> data = null;

        if (levelCodes.size() == 1) {
            oneLevel = true;
            lev = (LevelCode.Level)levelCodes.toArray()[0];
        } else {
            String successView = getSuccessView();
            if (successView != null && successView.indexOf("results") != -1) {
                data = getResultsPageAttributes(school, levelCodes);
                int numLevelsWithSurveys = (Integer)data.get("numLevelsWithSurveys");
                if (numLevelsWithSurveys == 1) {
                    oneLevel = true;
                    lev = (LevelCode.Level)data.get("lastLevelWithSurveys");
                }
            } else {
                data = getFormPageAttributes();
            }
        }

        if (oneLevel) {
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("level", lev.getName());
            model.put("id", school.getId());
            model.put("state", school.getStateAbbreviation().getAbbreviation());
            return new ModelAndView(getSuccessView(), model);
        } else {
            _schoolProfileHeaderHelper.updateModel(school, data);
            return super.showForm(request, response, bindException, data);
        }
    }

    protected ModelAndView onSubmit(Object command, BindException bindException) throws Exception {
        SchoolLevelCommand levelCommand = (SchoolLevelCommand) command;
        ModelAndView modelAndView = new ModelAndView(getSuccessView());
        modelAndView.getModel().put("level", levelCommand.getLevel().getName());
        modelAndView.getModel().put("id", levelCommand.getSchool().getId());
        modelAndView.getModel().put("state", levelCommand.getSchool().getStateAbbreviation().getAbbreviation());
        return modelAndView;
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        SchoolLevelCommand command = new SchoolLevelCommand();
        command.setSchool(school);
        return command;
    }

    protected void onBind(HttpServletRequest request, Object command, BindException errors) throws Exception {
        String level = request.getParameter("level");
        if (StringUtils.isEmpty(level)) {
            errors.rejectValue("level", null, "Please select a level.");
        } else {
            SchoolLevelCommand levelCommand = (SchoolLevelCommand) command;
            levelCommand.setLevel(level);
        }
    }

    private Map<String, Object> getResultsPageAttributes(School school, Set<LevelCode.Level> levelCodes) {
        Map<String, Object> data = new HashMap<String,Object>();
        data.put("pagename", "Parent Survey Display: Multilevel select");
        data.put("titleText", "Survey Results for");
        data.put("headerText", "Read what parents shared about their experiences at this school.");
        data.put("introText", "Surveys have been completed for the following grade levels at this school. Please select the school level that interests you.");

        int numLevelsWithSurveys = 0;
        LevelCode.Level lastLevelWithSurveys = null;
        Map<LevelCode.Level,Integer> map = new HashMap<LevelCode.Level,Integer>();
        for (LevelCode.Level level : levelCodes) {
            int numSurveysForLevel = getSurveyDao().getNumSurveysTaken(school, level, null);
            map.put(level, numSurveysForLevel);
            if (numSurveysForLevel > 0) {
                numLevelsWithSurveys++;
                lastLevelWithSurveys = level;
            }
        }
        data.put("numSurveysPerLevel", map);
        data.put("numLevelsWithSurveys", numLevelsWithSurveys);
        data.put("lastLevelWithSurveys", lastLevelWithSurveys);

        return data;
    }

    private Map<String, Object> getFormPageAttributes() {
        Map<String, Object> data = new HashMap<String,Object>();
        data.put("pagename", "Parent Survey: School Level Select Page");
        data.put("titleText", "Complete a Parent Survey for");
        data.put("headerText", "Spread the word about your school's special characteristics by filling out this survey.");
        data.put("introText", "Please select the highest level your child attended at this school so we can provide you with the appropriate survey questions:");
        return data;
    }

    public ISurveyDao getSurveyDao() {
        return _surveyDao;
    }

    public void setSurveyDao(ISurveyDao surveyDao) {
        _surveyDao = surveyDao;
    }

    public SchoolProfileHeaderHelper getSchoolProfileHeaderHelper() {
        return _schoolProfileHeaderHelper;
    }

    public void setSchoolProfileHeaderHelper(SchoolProfileHeaderHelper schoolProfileHeaderHelper) {
        _schoolProfileHeaderHelper = schoolProfileHeaderHelper;
    }
}
