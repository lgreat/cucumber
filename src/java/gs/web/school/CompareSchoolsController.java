package gs.web.school;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import gs.web.SessionContext;
import gs.data.state.State;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CompareSchoolsController extends AbstractController {

    public static final String BEAN_ID = "/compareSchools.page";
    //private static final Logger _log = Logger.getLogger(CompareSchoolsController.class);

    public ModelAndView handleRequestInternal(HttpServletRequest request,
                                              HttpServletResponse response)
            throws Exception {

        SessionContext sessionContext = SessionContext.getInstance(request);
        State currentState = null;
        if (sessionContext != null) {
            currentState = sessionContext.getState();
        }

        StringBuffer urlBuffer =
                new StringBuffer("http://www.greatschools.net/cgi-bin/msl_compare/");

        // Try to get the state from the SessionContext.  If there's not state,
        // then default to CA.
        if (currentState != null) {
            urlBuffer.append (currentState.getAbbreviationLowerCase());
        } else {
            urlBuffer.append ("ca");
        }

        urlBuffer.append("/?ids=");

        String[] schoolIds = request.getParameterValues("sc");
        if (schoolIds != null) {
            for (int i = 0; i < schoolIds.length; i++) {
                System.out.println ("schoolId: " + schoolIds[i]);
                urlBuffer.append(schoolIds[i]);
                if (i < schoolIds.length - 1) {
                    urlBuffer.append(',');
                }
            }
        }

        View redirectView = new RedirectView(urlBuffer.toString());
        return new ModelAndView(redirectView);
    }
}
