package gs.web.search;

import gs.data.school.LevelCode;
import gs.data.school.district.District;
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
    private String _cityName;
    private Integer _districtId;
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


        District district = null;
        if (_districtId != null && _districtId.intValue() != 0) {
            district = getDistrictDao().findDistrictById(getState(), _districtId);
        }


        JspWriter out = getJspContext().getOut();

        StringBuffer filterBuffer = new StringBuffer();
        if (_levelCode != null) {
            for (Iterator i = _levelCode.getIterator(); i.hasNext();) {
                LevelCode.Level level = (LevelCode.Level) i.next();
                String qs = "";
                if (filterBuffer.length() > 0) {
                    filterBuffer.append(" | ");
                }
                filterBuffer.append(Util.capitalize(level.getLongName()));
                qs = qString.replaceAll("\\&lc=" + level.getName(), "");
                filterBuffer.append(" (<a href=\"");
                filterBuffer.append(urlUtil.buildUrl("/schools.page?" + qs, request));
                filterBuffer.append("\">remove</a>)");
            }
        }

        if (_schoolType != null) {
            for (int i = 0; i < _schoolType.length; i++) {
                String qs = "";
                if (filterBuffer.length() > 0) {
                    filterBuffer.append(" | ");
                }
                filterBuffer.append(Util.capitalize(_schoolType[i]));
                if ("public".equals(_schoolType[i])) {
                    qs = qString.replaceAll("\\&st=public", "");
                } else if ("private".equals(_schoolType[i])) {
                    qs = qString.replaceAll("\\&st=private", "");
                } else if ("charter".equals(_schoolType[i])) {
                    qs = qString.replaceAll("\\&st=charter", "");
                }

                StringBuffer urlBuffer = new StringBuffer("/schools.page?");
                urlBuffer.append(qs);
                filterBuffer.append(" (<a href=\"");
                filterBuffer.append(urlUtil.buildUrl(urlBuffer.toString(), request));

                filterBuffer.append("\">remove</a>)");
            }
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
            out.println("<input type=\"image\" name=\"compare\" onclick=\"return checkSelections();\" src=\"/res/img/btn_comparechecked_149x21.gif\" alt=\"Compare checked Schools\"/>");
            out.println("<input type=\"image\" name=\"save\" onclick=\"return checkSelections();\" src=\"/res/img/btn_savechecked2msl_173x21.gif\" alt=\"Save checked to My School List\"/>");
            out.println("</td><td class=\"results_pagenav\">");

            if (!showall) {
                UrlBuilder builder = new UrlBuilder(request, "/schools.page");
                builder.addParametersFromRequest(request);
                writePageNumbers(getPage(), request, builder, _total);
            }
            out.println("</td><tr><td></td><td align=\"right\" style=\"padding-right:15px;padding-bottom:5px\">");
            if (!showall && (_total > PAGE_SIZE)) {
                out.print("<a href=\"");
                String showAllHref = "";
                StringBuffer hrefBuffer = new StringBuffer("/schools.page?");
                hrefBuffer.append(qString);
                hrefBuffer.append("&amp;showall=true");
                showAllHref = urlUtil.buildUrl(hrefBuffer.toString(), request);
                out.print(showAllHref);
                out.print("\"><span class=\"minilink\">Show all</span></a>");
            }
            out.println("</td></tr><td>");

        } else {
            if (filterBuffer.length() > 0) {
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

    public String getCityName() {
        return _cityName;
    }

    public void setCityName(String cityName) {
        _cityName = cityName;
    }

    public Integer getDistrictId() {
        return _districtId;
    }

    public void setDistrictId(Integer districtId) {
        _districtId = districtId;
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

