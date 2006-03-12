package gs.web.search;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Builds the model backing the schoolFilters module.
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolFiltersController extends AbstractController {

    /**
     * This method builds two lists of html markup - one to populate the
     * gradelevel unordered list and the other to populate the school type
     * unordered list.
      * @param request
     * @param response
     * @return
     * @throws Exception
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        ModelAndView mAndV = new ModelAndView("/search/schoolFilters");
        List levels = new ArrayList();

        String[] gls = (String[])request.getParameterValues("gl");
        List gradeLevels = null;
        if (gls != null) gradeLevels = Arrays.asList(gls);

        String qString = request.getQueryString();
        //remove page numbers from the url
        qString = qString.replaceAll("&p=\\p{Digit}[\\p{Digit}]?", "");

        // Build the list for grade levels: e, m, h

        // Elementary
        StringBuffer buffer = new StringBuffer();
        if (gradeLevels != null && gradeLevels.contains("elementary")) {
            buffer.append("Elementary (<a href=\"/search/search.page?");
            buffer.append(qString.replaceAll("\\&gl=elementary",""));
            buffer.append("\">remove</a>)");
        } else {
            buffer.append("<a href=\"/search/search.page?");
            buffer.append(qString);
            buffer.append("&gl=elementary\">Elementary</a>");
        }
        levels.add(buffer.toString());

        buffer = new StringBuffer();
        if (gradeLevels != null && gradeLevels.contains("middle")) {
            buffer.append("Middle (<a href=\"/search/search.page?");
            buffer.append(qString.replaceAll("\\&gl=middle",""));
            buffer.append("\">remove</a>)");
        } else {
            buffer.append("<a href=\"/search/search.page?");
            buffer.append(qString);
            buffer.append("&gl=middle\">Middle</a>");
        }
        levels.add(buffer.toString());

        // High
        buffer = new StringBuffer();
        if (gradeLevels != null && gradeLevels.contains("high")) {
            buffer.append("High (<a href=\"/search/search.page?");
            buffer.append(qString.replaceAll("\\&gl=high",""));
            buffer.append("\">remove</a>)");
        } else {
            buffer.append("<a href=\"/search/search.page?");
            buffer.append(qString);
            buffer.append("&gl=high\">High</a>");
        }
        levels.add(buffer.toString());

        mAndV.getModel().put("levels", levels);

         // Build the list for school types: public, private, charter
        List types = new ArrayList();
        String[] sts = (String[])request.getParameterValues("st");
        List schoolTypes = null;
        if (sts != null) schoolTypes = Arrays.asList(sts);

        // Public
        buffer = new StringBuffer();
        if (schoolTypes != null && schoolTypes.contains("public")) {
            buffer.append("Public (<a href=\"/search/search.page?");
            buffer.append(qString.replaceAll("\\&st=public",""));
            buffer.append("\">remove</a>)");
        } else {
            buffer.append("<a href=\"/search/search.page?");
            buffer.append(qString);
            buffer.append("&st=public\">Public</a>");
        }
        types.add(buffer.toString());

        // Charter
        buffer = new StringBuffer();
        if (schoolTypes != null && schoolTypes.contains("charter")) {
            buffer.append("Charter (<a href=\"/search/search.page?");
            buffer.append(qString.replaceAll("\\&st=charter",""));
            buffer.append("\">remove</a>)");
        } else {
            buffer.append("<a href=\"/search/search.page?");
            buffer.append(qString);
            buffer.append("&st=charter\">Charter</a>");
        }
        types.add(buffer.toString());

        // Private
        buffer = new StringBuffer();
        if (schoolTypes != null && schoolTypes.contains("private")) {
            buffer.append("Private (<a href=\"/search/search.page?");
            buffer.append(qString.replaceAll("\\&st=private",""));
            buffer.append("\">remove</a>)");
        } else {
            buffer.append("<a href=\"/search/search.page?");
            buffer.append(qString);
            buffer.append("&st=private\">Private</a>");
        }
        types.add(buffer.toString());

        mAndV.getModel().put("types", types);

        return mAndV;
    }
}
