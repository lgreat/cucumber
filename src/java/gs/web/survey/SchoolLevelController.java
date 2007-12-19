package gs.web.survey;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.web.school.SchoolPageInterceptor;
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

    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException bindException) throws Exception {
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        Set<LevelCode.Level> levelCodes = school.getLevelCode().getIndividualLevelCodes();

        if (levelCodes.size() < 2) {
            ModelAndView modelAndView = new ModelAndView(getSuccessView());
            LevelCode.Level lev = (LevelCode.Level )levelCodes.toArray()[0];
            modelAndView.getModel().put("level", lev.getName()); 
            modelAndView.getModel().put("id", school.getId());
            modelAndView.getModel().put("state", school.getStateAbbreviation().getAbbreviation());
            return modelAndView;
        } else {
            Map<String, String> data = null;
            String successView = getSuccessView();
            if (successView != null && successView.indexOf("results") != -1) {
                data = getResultsPageAttributes();
            } else {
                data = getFormPageAttributes();
            }
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

    private Map<String, String> getResultsPageAttributes() {
        Map<String, String> data = new HashMap<String,String>();
        data.put("titleText", "Survey Results for");
        data.put("headerText", "Read what parents shared about their experiences at this school.");
        data.put("introText", "Please select the school level that interests you.");
        return data;
    }

    private Map<String, String> getFormPageAttributes() {
        Map<String, String> data = new HashMap<String,String>();
        data.put("titleText", "Complete a Parent Survey for");
        data.put("headerText", "Spread the word about your school's special characteristics by filling out this survey.");
        data.put("introText", "Please select the highest level your child attended at this school so we can provide you with the appropriate survey questions:");
        return data;
    }
}
