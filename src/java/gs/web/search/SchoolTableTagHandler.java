package gs.web.search;

import gs.data.school.LevelCode;
import gs.web.jsp.Util;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * This tag handler generates a table of schools.
 * This tag is used on and school/schoolsTable.jspx.
 * todo: This class is an <strong>ugly mess</strong> and badly needs to be
 * refactored.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolTableTagHandler extends ResultsTableTagHandler {

    /**
     * used by Spring
     */
    public static final String BEAN_ID = "schoolTableTagHandler";

    private static UrlUtil urlUtil = new UrlUtil();
    private List _schools = null;
    private String _srcQuery = null;
    private Boolean _showAll;
    private LevelCode _levelCode;
    private String[] _schoolType;
    private static final Log _log = LogFactory.getLog(SchoolTableTagHandler.class);

    /**
     * This is the list of schools that fills the schools table
     *
     * @param sList a <code>List</code> of <code>gs.data.School</code> objects
     */
    public void setSchools(List sList) {
        _schools = sList;
    }

    public void doTag() throws IOException {

        PageContext pc = (PageContext) getJspContext().findAttribute(PageContext.PAGECONTEXT);
        HttpServletRequest request = (HttpServletRequest) pc.getRequest();

        String qString = getSrcQuery();

        boolean showall = _showAll != null && _showAll.booleanValue();

        JspWriter out = getJspContext().getOut();

        boolean filtered = false;
        if (_levelCode != null || _schoolType != null) {
            filtered = true;
        }

        out.println("<table class=\"school_results_only\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");

        if (_schools != null && _schools.size() > 0) {
            // writes the list of schools
            try {
                getJspBody().invoke(out);
            } catch (Exception e) {
                _log.warn("could not write school list", e);
            }

            out.print("<tr class=\"last_row\"><td colspan=\"5\">");
            out.println("</td></tr></table>");
            out.println("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
            out.println("<td>");
            out.println("<input type=\"image\" name=\"compare\" onclick=\"return checkSelections();\" src=\"/res/img/button/btn_comparechecked_149x21.gif\" alt=\"Compare checked Schools\"/>");
            out.println("<input type=\"image\" name=\"save\" onclick=\"return checkSelections();\" src=\"/res/img/button/btn_savechecked2msl_173x21.gif\" alt=\"Save checked to My School List\"/>");
            out.println("</td><td class=\"results_pagenav\">");

            if (!showall) {
                UrlBuilder builder = new UrlBuilder(request, "/schools.page");
                builder.addParametersFromRequest(request);
                writePageNumbers(getPage(), request, builder, _total);
            }
            out.println("</td></tr><tr><td></td><td align=\"right\" style=\"padding-right:15px;padding-bottom:5px\">");
            if (!showall && (_total > PAGE_SIZE)) {
                out.print("<a href=\"");
                String showAllHref;
                StringBuffer hrefBuffer = new StringBuffer("/schools.page?");
                hrefBuffer.append(qString.replaceAll("&", "&amp;"));
                hrefBuffer.append("&amp;showall=true");
                showAllHref = urlUtil.buildUrl(hrefBuffer.toString(), request);
                out.print(showAllHref);
                out.print("\"><span class=\"minilink\">Show all</span></a>");
            }
            out.println("</td></tr><tr><td>");

        } else {
            if (filtered) {
                out.println("<tr><th class=\"left result_title\">Your refinement did not return any results.</th></tr>");
            } else {
                out.println("<tr><th class=\"left result_title\">No schools found</th></tr>");
            }
            out.println("<tr><td valign=\"top\" height=\"100\">");
            out.println("Please try again.");
        }
        out.println("</td></tr></table>");
    }

    public String getSrcQuery() {
        return _srcQuery;
    }

    public void setSrcQuery(String srcQuery) {
        _srcQuery = srcQuery;
    }

    public Boolean getShowAll() {
        return _showAll;
    }

    public void setShowAll(Boolean showAll) {
        _showAll = showAll;
    }

    public LevelCode getLevelCode() {
        return _levelCode;
    }

    public void setLevelCode(LevelCode levelCode) {
        _levelCode = levelCode;
    }

    public String[] getSchoolType() {
        return _schoolType;
    }

    public void setSchoolType(String[] schoolType) {
        _schoolType = schoolType;
    }
}

