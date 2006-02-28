package gs.web.path;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.UrlUtil;
import gs.web.SessionContext;
import gs.data.state.State;

/**
 * This class handles the form submission from compareEntry.jspx.
 * There is no validation of parameters: when a user hits the submit
 * button, the controller should alway redirect to the compare schools page.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CompareEntryController extends AbstractController {

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        String level = request.getParameter("level");
        String type = request.getParameter("type");
        String stateString = request.getParameter("state");
        if (StringUtils.isBlank(stateString)) {
            State state = SessionContext.getInstance(request).getStateOrDefault();
            stateString = state.getAbbreviationLowerCase();
        }

        StringBuffer urlBuffer = new StringBuffer(50);
        urlBuffer.append("/cgi-bin/cs_where?state=");
        urlBuffer.append(stateString);
        urlBuffer.append("&");
        urlBuffer.append((level != null) ? level : "elementary");
        urlBuffer.append("=true#");
        urlBuffer.append((type != null) ? type : "");
        UrlUtil uu = new UrlUtil();
        View view = new RedirectView(uu.buildUrl(urlBuffer.toString(), request));
        return new ModelAndView(view);
    }
}
