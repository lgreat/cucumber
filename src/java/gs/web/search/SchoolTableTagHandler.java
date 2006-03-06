package gs.web.search;

import gs.data.school.School;
import gs.data.school.district.District;
import gs.data.search.highlight.TextHighlighter;
import gs.data.util.Address;
import gs.web.util.UrlUtil;
import gs.web.jsp.Util;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

/**
 * This tag handler generates a table of schools.
 * This tag is used on search.jspx
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolTableTagHandler extends ResultsTableTagHandler {

    /** used by Spring */
    public static final String BEAN_ID = "schoolTableTagHandler";

    private static UrlUtil urlUtil = new UrlUtil();
    private List _schools = null;
    private static final Log _log = LogFactory.getLog(SchoolTableTagHandler.class);

    /**
     * This is the list of schools that fills the schools table
     * @param sList a <code>List</code> of <code>gs.data.School</code> objects
     */
    public void setSchools(List sList) {
        _schools = sList;
    }

    public String getConstraint() {
        return "school";
    }

    public void doTag() throws IOException {

        PageContext pc = (PageContext) getJspContext().findAttribute(PageContext.PAGECONTEXT);
        String qString = "";
        boolean showall = false;
        String showAllHref = "";
        HttpServletRequest request = null;
        StringBuffer districtInfoBuffer = null;

        if (pc != null) {
            request = (HttpServletRequest) pc.getRequest();
            qString = request.getQueryString();
            showall = "true".equals(request.getParameter("showall"));
        } else {
            _log.info("PageContext is null");
        }

        JspWriter out = getJspContext().getOut();

        out.print("<form action=\"/compareSchools.page\">");
        out.print("<input type=\"hidden\" name=\"state\" value=\"");
        out.print(getStateOrDefault().getAbbreviation());
        out.println ("\"/>");
        out.println("<table class=\"columns\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
        out.println("<tr><td class=\"mainresultsheader\">");
        out.println("<table width=\"100%\"><tr><td><span id=\"resultsheadline\">");
        out.print("Found ");
        out.print(_total);
        String cityOrDistrictName = (String) getJspContext().findAttribute("city");
        String distId = null;

        String schoolString;
        if (_total != 1) {
            schoolString = " schools";
        } else {
            schoolString = " school";
        }

        StringBuffer districtUrlBuffer = new StringBuffer("/cgi-bin/");

        if (cityOrDistrictName != null) {
            out.print(" ");
            out.print(cityOrDistrictName);
            out.print(schoolString);
        } else {
            cityOrDistrictName = (String) getJspContext().findAttribute("distname");
            if (cityOrDistrictName != null) {
                out.print(schoolString);
                out.print(" in ");
                out.print(cityOrDistrictName);

                districtUrlBuffer.append(getState().getAbbreviationLowerCase());
                districtUrlBuffer.append("/district_profile/");
                distId = (String) getJspContext().findAttribute("district");
                if (StringUtils.isNotEmpty(distId)) {
                    districtUrlBuffer.append(distId);
                    District district = getDistrictDao().findDistrictById(getState(), new Integer(distId));
                    if (district != null) {
                        districtInfoBuffer = new StringBuffer(70);
                        Address address = district.getPhysicalAddress();
                        districtInfoBuffer.append(address.getStreet());
                        districtInfoBuffer.append("<br/>");
                        districtInfoBuffer.append(address.getCity());
                        districtInfoBuffer.append(", ");
                        districtInfoBuffer.append(address.getState());
                        districtInfoBuffer.append("  ");
                        districtInfoBuffer.append(address.getZip());
                        districtInfoBuffer.append("<br/>");
                        districtInfoBuffer.append(district.getCounty());
                        districtInfoBuffer.append("<br/>");
                        districtInfoBuffer.append("Phone: ");
                        districtInfoBuffer.append(district.getPhone());
                    }
                } else {
                    districtUrlBuffer.append("");
                }

                districtUrlBuffer.append("/");
                out.println("</span>");
            }
        }

        out.print("</span></td><td align=\"right\" style=\"padding-right:15px;white-space:nowrap\">");

        if (_total > 0) {
            if (!showall) {
                int page = ((_page > 0) ? (_page - 1) : 0);
                out.print((page * PAGE_SIZE) + 1);
                out.print(" - ");
                int x = (page * PAGE_SIZE) + PAGE_SIZE;
                if (_total > x) {
                    out.print(x);
                } else {
                    out.print(_total);
                }
            } else {
                out.print("1 - ");
                out.print(_total);
            }
            out.print(" of ");
            out.print(_total);
        }

        if (!showall && (_total > PAGE_SIZE)) {

            StringBuffer hrefBuffer = new StringBuffer("/search/search.page?");
            hrefBuffer.append(qString);
            hrefBuffer.append("&amp;showall=true");
            showAllHref = urlUtil.buildUrl(hrefBuffer.toString(), request);
            out.print("<a href=\"" + showAllHref + "\">");
            out.println("<span class=\"minilink\" style=\"padding-left:8px;\">Show all</span></a>");
        }

        out.println("</td></tr>");

        out.println("<tr><td>");
        out.print("<a href=\"");
        out.print(urlUtil.buildUrl(districtUrlBuffer.toString(), request));
        out.print("\">");
        out.println("<span class=\"minilink\">View district information</span></a>");
        out.println("</td></tr>");

        StringBuffer filterBuffer = new StringBuffer();

        String[] gls = (String[]) getJspContext().findAttribute("gl");
        if (gls != null) {
            for (int i = 0; i < gls.length; i++) {
                String qs = "";
                if (filterBuffer.length() > 0) {
                    filterBuffer.append(" | ");
                }
                filterBuffer.append(Util.capitalize(gls[i]));
                if ("elementary".equals(gls[i])) {
                    qs = qString.replaceAll("\\&amp;gl=elementary", "");
                } else if ("middle".equals(gls[i])) {
                    qs = qString.replaceAll("\\&amp;gl=middle", "");
                } else if ("high".equals(gls[i])) {
                    qs = qString.replaceAll("\\&amp;gl=high", "");
                }

                StringBuffer urlBuffer = new StringBuffer("/search/search.page?");
                urlBuffer.append(qs);
                filterBuffer.append(" (<a href=\"");
                filterBuffer.append(urlUtil.buildUrl(urlBuffer.toString(), request));
                filterBuffer.append("\">remove</a>)");
            }
        }

        String[] sts = (String[]) getJspContext().findAttribute("st");
        if (sts != null) {
            for (int i = 0; i < sts.length; i++) {
                String qs = "";
                if (filterBuffer.length() > 0) {
                    filterBuffer.append(" | ");
                }
                filterBuffer.append(Util.capitalize(sts[i]));
                if ("public".equals(sts[i])) {
                    qs = qString.replaceAll("\\&amp;st=public", "");
                } else if ("private".equals(sts[i])) {
                    qs = qString.replaceAll("\\&amp;st=private", "");
                } else if ("charter".equals(sts[i])) {
                    qs = qString.replaceAll("\\&amp;st=charter", "");
                }

                StringBuffer urlBuffer = new StringBuffer("/search/search.page?");
                urlBuffer.append(qs);
                filterBuffer.append(" (<a href=\"");
                filterBuffer.append(urlUtil.buildUrl(urlBuffer.toString(), request));

                filterBuffer.append("\">remove</a>)");
            }
        }

        // If we're displaying a district list, then show district info
        /*
        if (districtInfoBuffer != null) {
            out.println("<tr><td>");
            out.println("<div id=\"distinfo\">");
            out.println(districtInfoBuffer.toString());
            out.println("</div>");
            out.println("</td></tr>");
        }
        */

        // "compare all" links
        StringBuffer compareBaseBuffer = new StringBuffer();
        compareBaseBuffer.append("/cgi-bin/cs_compare/");
        compareBaseBuffer.append(getState().getAbbreviationLowerCase());
        compareBaseBuffer.append("/?area=");
        if (distId != null) {
            compareBaseBuffer.append("d");
        } else {
            compareBaseBuffer.append("m&amp;city=");
            compareBaseBuffer.append(cityOrDistrictName.replaceAll("\\s", "+"));
        }

        compareBaseBuffer.append("&amp;sortby=");
        if (distId != null) {
            compareBaseBuffer.append("name");
        } else {
            compareBaseBuffer.append("distance");
        }

        compareBaseBuffer.append("&amp;tab=over&amp;level=");

        String compareUrlBase = compareBaseBuffer.toString();
        out.println("<tr><td>");
        out.print("<div id=\"comparelinks\">Compare ");
        out.print(cityOrDistrictName);
        out.print(" public schools: ");

        out.print("<a href=\"");
        StringBuffer buffer = new StringBuffer(compareUrlBase);
        buffer.append("e");
        if (distId != null) {
            buffer.append("&amp;district=");
            buffer.append(distId);
        }
        out.print(urlUtil.buildUrl(buffer.toString(), request));
        out.print("\">Elementary</a>");
        out.print(" | ");

        out.print("<a href=\"");
        buffer = new StringBuffer(compareUrlBase);
        buffer.append("m");
        if (distId != null) {
            buffer.append("&amp;district=");
            buffer.append(distId);
        }
        out.print(urlUtil.buildUrl(buffer.toString(), request));
        out.print("\">Middle</a>");
        out.print(" | ");

        out.print("<a href=\"");
        buffer = new StringBuffer(compareUrlBase);
        buffer.append("h");
        if (distId != null) {
            buffer.append("&amp;district=");
            buffer.append(distId);
        }
        out.print(urlUtil.buildUrl(buffer.toString(), request));
        out.print("\">High</a></div>");
        out.println("<td><tr>");
        // end "compare all" links

        // start filter row
        out.println("<tr><td id=\"filters\" colspan=\"2\">");
        if (filterBuffer.length() > 0) {
            out.print("Filtered: ");
            out.println(filterBuffer.toString());
        } else {
            out.print("To further narrow your list, use the filters on the left.");
        }
        out.println("</td></tr></table>");
        out.println("</td></tr>");
        // end filter row

        out.println("<tr><td valign=\"top\">");
        out.println("<table class=\"school_results_only\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");

        if (_schools != null && _schools.size() > 0) {

            /// start control row
            out.print("<tr class=\"control_row\"><td colspan=\"5\">");
            out.println("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
            out.println("<tr><td style=\"padding-top:5px\">");
            writeButtons(out);
            out.println("</td><td class=\"results_pagenav\">");
            out.println("</td></tr></table></td></tr>");
            /// end control row

            out.println("<tr><th class=\"result_title\" width=\"1\">&nbsp;</th>");
            out.println("<th align=\"left\"   class=\"result_title\">Schools</th>");
            out.println("<th align=\"center\" class=\"result_title\">Type</th>");
            out.println("<th align=\"center\" class=\"result_title\">Grade</th>");
            out.println("<th align=\"center\" class=\"result_title\">Enrollment&nbsp;&nbsp;</th>");
            out.println("</tr>");

            for (int i = 0; i < _schools.size(); i++) {

                School school = (School)_schools.get(i);

                if (school != null) {
                    out.println("<tr class=\"result_row\">");
                    out.println("<td class=\"checkbox\" width=\"1\">");
                    out.print("<input name=\"sc\" type=\"checkbox\"  value=\"");
                    out.print(school.getDatabaseState().getAbbreviationLowerCase());
                    out.print(school.getId());
                    out.print("\" /></td>");

                    out.println("<td>");

                    StringBuffer urlBuffer = new StringBuffer("/modperl/browse_school/");
                    if ("private".equals(school.getType().getSchoolTypeName())) {
                        urlBuffer = new StringBuffer("/cgi-bin/");
                        urlBuffer.append(school.getDatabaseState().getAbbreviationLowerCase());
                        urlBuffer.append("/private/");
                    } else {
                        urlBuffer =
                                new StringBuffer("/modperl/browse_school/");
                        urlBuffer.append(school.getDatabaseState().getAbbreviationLowerCase());
                        urlBuffer.append("/");
                    }
                    urlBuffer.append(school.getId().toString());

                    out.print("<a href=\"");
                    out.print(urlUtil.buildUrl(urlBuffer.toString(), request));
                    out.println("\">");
                    out.print(TextHighlighter.highlight(school.getName(), getQueryString(), "name"));
                    out.println("</a><br/>");
                    out.print(school.getPhysicalAddress().toString());
                    out.println("<br/>");

                    District dist = school.getDistrict();
                    if (dist != null) {
                        String name = dist.getName();
                        if (name != null) {
                            out.print(name);
                        }
                    }

                    out.println("</td>");
                    out.println("<td align=\"center\">");
                    out.println(school.getType().getSchoolTypeName());
                    out.println("</td><td align=\"center\">");
                    String gradeRange = school.getGradeLevels().getRangeString();
                    out.println(StringUtils.isNotEmpty(gradeRange) ? gradeRange : "not available");
                    out.println("</td><td align=\"center\">");
                    try {
                        int enrollment = school.getEnrollment();
                        if (enrollment > -1) {
                            out.print(enrollment);
                        } else {
                            out.print("not available");
                        }
                    } catch (Exception e) {
                        _log.warn("Problem getting enrollment: " + school, e);
                    }
                    out.println("</td>");
                    out.println("</tr>");
                }

            }
            out.print("<tr class=\"last_row\"><td colspan=\"5\">");

            out.println("</td></tr></table>");
            out.println("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
            out.println("<td>");
            writeButtons(out);
            out.println("</td><td class=\"results_pagenav\">");

            if (!showall) writePageNumbers(out);
            out.println("</td><tr><td></td><td align=\"right\" style=\"padding-right:15px;padding-bottom:5px\">");
            if (!showall && (_total > PAGE_SIZE)) {
                out.print("<a href=\"");
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
        out.println("</td></tr></table>");
        out.println("</form>");
    }

    private static void writeButtons(JspWriter out) throws IOException {
        out.println("<input type=\"image\" name=\"compare\" onclick=\"return checkSelections();\" src=\"/res/img/btn_comparechecked_149x21.gif\" alt=\"Compare checked Schools\"/>");
        out.println("<input type=\"image\" name=\"save\" onclick=\"return checkSelections();\" src=\"/res/img/btn_savechecked2msl_173x21.gif\" alt=\"Save checked to My School List\"/>");
    }
}

