package gs.web.school;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import gs.web.SessionContext;
import gs.data.state.State;

import java.util.Enumeration;

/**
 * This controller handles requests for "compare checked schools" and
 * "add checked to my schools list" actions.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CompareSchoolsController extends AbstractController {

    public static final String BEAN_ID = "/compareSchools.page";
    //private static final Logger _log = Logger.getLogger(CompareSchoolsController.class);


    public ModelAndView handleRequestInternal(HttpServletRequest request,
                                              HttpServletResponse response)
            throws Exception {

        SessionContext sc = SessionContext.getInstance(request);

        StringBuffer urlBuffer = new StringBuffer(50);
        if (sc != null) {
            urlBuffer.append("http://");
            urlBuffer.append(sc.getHostName());
        }
        urlBuffer.append("/cgi-bin/");

        // The input submit buttons are images that are labeled "compare" and
        // "save".  The parameters included in the request include the location
        // of the click on the image.  This is why we include the ".x"

        String idString = "/?ids=";
        String idDelimiter = ",";

        String p1 = request.getParameter("compare.x");
        if (p1 != null) {
            urlBuffer.append("msl_compare/");
        } else {
            urlBuffer.append("msl_confirm/");
            idString="/?add_ids=";
            idDelimiter = "&add_ids=";
        }

        SessionContext sessionContext = SessionContext.getInstance(request);
        State currentState = null;
        if (sessionContext != null) {
            currentState = sessionContext.getStateOrDefault();
        } else {
            currentState = State.CA; // default state
        }

        urlBuffer.append (currentState.getAbbreviationLowerCase());
        urlBuffer.append(idString);

        String[] schoolIds = request.getParameterValues("sc");
        if (schoolIds != null) {
            for (int i = 0; i < schoolIds.length; i++) {
                if (p1 != null) {
                    urlBuffer.append(schoolIds[i]);
                } else {
                    urlBuffer.append(schoolIds[i].substring(2));
                }
                if (i < schoolIds.length - 1) {
                    urlBuffer.append(idDelimiter);
                }
            }
        }

        //_log.debug("redirect url: " + urlBuffer.toString());
        View redirectView = new RedirectView(urlBuffer.toString());
        return new ModelAndView(redirectView);
    }
}
