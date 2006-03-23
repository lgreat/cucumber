package gs.web.search;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gs.data.school.LevelCode;

/**
 * Builds the model backing the schoolFilters module.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolFiltersController extends AbstractController {

    public static final String PARAM_SCHOOL_TYPES = "st";
    public static final String PARAM_LEVEL_CODE = "lc";
    private static final String PARAM_DEST_PAGE = "dest";


    /**
     * This method builds two lists of html markup - one to populate the
     * gradelevel unordered list and the other to populate the school type
     * unordered list.
     *
     * @throws Exception
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        List levels = new ArrayList();

        String[] gls = request.getParameterValues(PARAM_LEVEL_CODE);
        List gradeLevels = null;
        if (gls != null) gradeLevels = Arrays.asList(gls);

        String qString = request.getQueryString();

        //remove page numbers from the url
        qString = qString.replaceAll("&p=\\p{Digit}[\\p{Digit}]?", "");

        String destPage = request.getParameter(PARAM_DEST_PAGE);
        if (StringUtils.isEmpty(destPage)) {
            destPage = "/schools.page";
        } else {
            //remove page numbers from the url
            qString = qString.replaceAll("&"+PARAM_DEST_PAGE+"=[^&]+", "");
        }
        destPage = request.getContextPath() + destPage;

        // Build the list for grade levels: e, m, h
        levels.add(createLevelCodeHtml(destPage, LevelCode.Level.ELEMENTARY_LEVEL, "Elementary", gradeLevels != null && gradeLevels.contains("e"), qString));
        levels.add(createLevelCodeHtml(destPage, LevelCode.Level.MIDDLE_LEVEL, "Middle", gradeLevels != null && gradeLevels.contains("m"), qString));
        levels.add(createLevelCodeHtml(destPage, LevelCode.Level.HIGH_LEVEL, "High", gradeLevels != null && gradeLevels.contains("h"), qString));


        // Build the list for school types: public, private, charter
        List types = new ArrayList();
        String[] sts = request.getParameterValues(PARAM_SCHOOL_TYPES);
        List schoolTypes = null;
        if (sts != null) schoolTypes = Arrays.asList(sts);

        types.add(createSchoolTypeHtml(destPage, "public", schoolTypes != null && schoolTypes.contains("public"), qString));
        types.add(createSchoolTypeHtml(destPage, "charter", schoolTypes != null && schoolTypes.contains("charter"), qString));
        types.add(createSchoolTypeHtml(destPage, "private", schoolTypes != null && schoolTypes.contains("private"), qString));

        ModelAndView modelAndView = new ModelAndView("/search/schoolFilters");
        modelAndView.getModel().put("levels", levels);
        modelAndView.getModel().put("types", types);

        return modelAndView;
    }

    private String createSchoolTypeHtml(String page, String schoolType, boolean hasSchoolType, String qString) {
        final String schoolTypeLabel = StringUtils.capitalize(schoolType);
        StringBuffer buffer;
        buffer = new StringBuffer();
        if (hasSchoolType) {
            buffer.append(schoolTypeLabel + " (<a href=\"" + page + "?");
            buffer.append(qString.replaceAll("\\&st=" + schoolType, ""));
            buffer.append("\">remove</a>)");
        } else {
            buffer.append("<a href=\"" + page + "?");
            buffer.append(qString);
            buffer.append("&st="+schoolType +"\">"+schoolTypeLabel +"</a>");
        }
        final String s = buffer.toString();
        return s;
    }

    private String createLevelCodeHtml(String page, LevelCode.Level level, String label, boolean hasLevel, String qString) {
        StringBuffer buffer = new StringBuffer();
        if (hasLevel) {
            buffer.append(label + " (<a href=\"" + page + "?");
            buffer.append(qString.replaceAll("\\&lc=" + level.getName(), ""));
            buffer.append("\">remove</a>)");
        } else {
            buffer.append("<a href=\"" + page + "?");
            buffer.append(qString);
            buffer.append("&lc=" + level.getName() + "\">" + label + "</a>");
        }
        final String s = buffer.toString();
        return s;
    }
}
