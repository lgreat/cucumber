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
import java.util.Set;

public class SchoolLevelController extends SimpleFormController {
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException bindException) throws Exception {
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        Set<LevelCode.Level> levelCodes = school.getLevelCode().getIndividualLevelCodes();

        if (levelCodes.size() < 2) {
            return new ModelAndView(getSuccessView());
        } else {
            return super.showForm(request, response, bindException);
        }
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        SchoolLevelCommand command = new SchoolLevelCommand();
        command.setSchool(school);
        return command;
    }

    protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {
        String level = request.getParameter("level");
        if (StringUtils.isEmpty(level)) {
            errors.rejectValue("level", null, "Please select a level.");
        } else {
            SchoolLevelCommand levelCommand = (SchoolLevelCommand) command;
            levelCommand.setLevel(LevelCode.Level.getLevelCode(level));
        }
    }
}
