package gs.web.survey;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.web.school.SchoolPageInterceptor;
import gs.web.util.UrlBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

public class SchoolLevelController extends SimpleFormController {
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        Set<LevelCode.Level> levelCodes = school.getLevelCode().getIndividualLevelCodes();

        if (levelCodes.size() < 2) {
            String redirectUrl = new UrlBuilder(school, UrlBuilder.SCHOOL_TAKE_SURVEY).asSiteRelative(request);
            response.sendRedirect(redirectUrl);
            return null;
        } else {
            return new ModelAndView(getFormView());
        }
    }

}
