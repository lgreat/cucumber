package gs.web.path;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.BindException;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.UrlUtil;
import gs.web.SessionContext;
import gs.data.state.State;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CompareEntryController extends SimpleFormController {

    private static Logger _log = Logger.getLogger(CompareEntryController.class);

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws ServletException {

        String level = request.getParameter("level");
        String type = request.getParameter("type");
        String stateString = request.getParameter("state");
        if (StringUtils.isBlank(stateString)) {
            State state = SessionContext.getInstance(request).getStateOrDefault();
            stateString = state.getAbbreviationLowerCase();
        }

        StringBuffer urlBuffer = new StringBuffer();
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
