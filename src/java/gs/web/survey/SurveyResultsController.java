package gs.web.survey;

import gs.data.school.School;
import gs.web.school.AbstractSchoolController;
import gs.web.school.SchoolPageInterceptor;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller used to deliver the survey results page. Now it redirects to the school profile overview page.
 */
public class SurveyResultsController extends AbstractController {

    public static final String BEAN_ID = "surveyResultsController";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // This controller is configured to be school-aware in pages-servlet.xml
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);

        // GS-13082 Redirect to new profile if eligible
        if (AbstractSchoolController.shouldRedirectToNewProfile(school)) {
            return AbstractSchoolController.getRedirectToNewProfileModelAndView(school, request, AbstractSchoolController
                    .NewProfileTabs.programsCulture);
        }

        UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        RedirectView301 redirectToOverview = new RedirectView301(urlBuilder.asSiteRelative(request));
        return new ModelAndView(redirectToOverview);
    }
}
