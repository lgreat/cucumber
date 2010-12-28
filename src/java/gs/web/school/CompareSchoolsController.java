package gs.web.school;

import gs.data.state.State;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.axis.utils.ArrayUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller handles requests for "compare checked schools" and
 * "add checked to my schools list" actions.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class CompareSchoolsController extends AbstractController {

    public static final String BEAN_ID = "/compareSchools.page";

    public ModelAndView handleRequestInternal(HttpServletRequest request,
                                              HttpServletResponse response)
            throws Exception {

        SessionContext sc = SessionContextUtil.getSessionContext(request);
        StringBuffer urlBuffer = new StringBuffer(50);
        if (sc != null) {
            urlBuffer.append("http://");
            urlBuffer.append(sc.getHostName());
        }

        // The input submit buttons are images that are labeled "compare" and
        // "save".  The parameters included in the request include the location
        // of the click on the image.  This is why we include the ".x"

        String idString = "/?ids=";
        String idDelimiter = ",";

        boolean hoverCompare = (request.getParameter("hover_compare") != null)
                || (request.getParameter("hover_compare.x") != null);

        String p1 = request.getParameter("compare.x");

        if (hoverCompare || p1 != null) {
//            urlBuffer.append("/cgi-bin/cs_compare/");
//        } else if (p1 != null) {
//            urlBuffer.append("/modperl/msl_compare/");
            // aroy: GS-10742 new compare
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.COMPARE_SCHOOLS_OVERVIEW);

            String[] schoolIds = request.getParameterValues("sc");
            if (schoolIds != null) {
                urlBuilder.setParameter("schools", StringUtils.join(schoolIds, ","));
            }
            String cpnValue = request.getParameter("cpn");
            if (cpnValue != null && cpnValue.length() > 0) {
                urlBuilder.addParameter("cpn", cpnValue);
            }

            View redirectView = new RedirectView(urlBuilder.asFullUrl(request));
            return new ModelAndView(redirectView);
        } else {
            urlBuffer.append("/cgi-bin/msl_confirm/");
            idString = "/?add_ids=";
            idDelimiter = "&add_ids=";
        }

        if (sc != null) {
            State currentState = sc.getStateOrDefault();
            urlBuffer.append(currentState.getAbbreviationLowerCase());
        } else {
            urlBuffer.append("ca");
        }

        urlBuffer.append(idString);

        String[] schoolIds = request.getParameterValues("sc");
        if (schoolIds != null) {
            for (int i = 0; i < schoolIds.length; i++) {
                if (p1 != null || hoverCompare) {
                    urlBuffer.append(schoolIds[i]);
                } else {
                    urlBuffer.append(schoolIds[i].substring(2));
                }
                if (i < schoolIds.length - 1) {
                    urlBuffer.append(idDelimiter);
                }
            }
        }

        if (hoverCompare) {
            urlBuffer.append("&level=").append(request.getParameter("levelCode"));
            urlBuffer.append("&area=s&msl=1&tab=over");
        }

        //add the omniture cpn parameter to the url
        String cpnValue = request.getParameter("cpn");
        if (cpnValue != null && cpnValue.length() > 0) {
            urlBuffer.append("&cpn=");
            urlBuffer.append(cpnValue);
        }

        View redirectView = new RedirectView(urlBuffer.toString());
        return new ModelAndView(redirectView);
    }
}
