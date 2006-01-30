package gs.web.search;

import gs.data.school.School;
import gs.data.school.district.District;
import gs.data.search.highlight.TextHighlighter;
import gs.web.util.UrlUtil;

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
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolTableTagHandler extends ResultsTableTagHandler {

    public static final String BEAN_ID = "schoolTableTagHandler";
    private static UrlUtil uu = new UrlUtil();
    private List _schools = null;
    private static final Log _log = LogFactory.getLog(SchoolTableTagHandler.class);

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
        if (pc != null) {
            request = (HttpServletRequest) pc.getRequest();
            qString = request.getQueryString();
            showall = "true".equals(request.getParameter("showall"));
        }

        JspWriter out = getJspContext().getOut();

        out.println("<form action=\"/compareSchools.page\">");
        out.println("<table class=\"columns\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");

        out.println("<tr><td class=\"mainresultsheader\">");
        out.println("<table width=\"100%\"><tr><td><span id=\"resultsheadline\">");
        out.print("Found ");
        out.print(_total);
        out.print(" school");
        if (_total != 1) {
            out.print("s");
        }
        out.print(" in ");
        String city = (String) getJspContext().findAttribute("city");
        if (city != null) {
            out.print(" the city of: ");
            out.print(city);
        } else {
            String dist = (String) getJspContext().findAttribute("distname");
            if (dist != null) {
                out.print(" the district of: ");
                out.print(dist);

                StringBuffer urlBuffer = new StringBuffer("http://");
                urlBuffer.append(getHostname());
                urlBuffer.append("/cgi-bin/");
                urlBuffer.append(getState().getAbbreviationLowerCase());
                urlBuffer.append("/district_profile/");
                String distId = (String) getJspContext().findAttribute("district");
                urlBuffer.append(StringUtils.isNotEmpty(distId) ? distId : "");
                urlBuffer.append("/");
                out.print("</span><a href=\"" + urlBuffer.toString() + "\">");
                out.print("<span class=\"minilink\" style=\"padding-left:8px;\">View district profile</span></a><span>");
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
            hrefBuffer.append("&showall=true");
            showAllHref = uu.buildUrl(hrefBuffer.toString(), request);
            out.print("&nbsp;&nbsp;<a href=\"" + showAllHref + "\">");
            out.println("<span class=\"minilink\">Show all</span></a>");
        }

        out.println("</td></tr>");

        StringBuffer filterBuffer = new StringBuffer();

        String[] gls = (String[]) getJspContext().findAttribute("gl");
        if (gls != null) {
            for (int i = 0; i < gls.length; i++) {
                String qs = "";
                if (filterBuffer.length() > 0) {
                    filterBuffer.append(" | ");
                }
                filterBuffer.append(capitalize(gls[i]));
                if ("elementary".equals(gls[i])) {
                    qs = qString.replaceAll("\\&gl=elementary", "");
                } else if ("middle".equals(gls[i])) {
                    qs = qString.replaceAll("\\&gl=middle", "");
                } else if ("high".equals(gls[i])) {
                    qs = qString.replaceAll("\\&gl=high", "");
                }
                filterBuffer.append(" (<a href=\"/search/search.page?");
                filterBuffer.append(qs);
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
                filterBuffer.append(capitalize(sts[i]));
                if ("public".equals(sts[i])) {
                    qs = qString.replaceAll("\\&st=public", "");
                } else if ("private".equals(sts[i])) {
                    qs = qString.replaceAll("\\&st=private", "");
                } else if ("charter".equals(sts[i])) {
                    qs = qString.replaceAll("\\&st=charter", "");
                }
                filterBuffer.append(" (<a href=\"/search/search.page?");
                filterBuffer.append(qs);
                filterBuffer.append("\">remove</a>)");
            }
        }


        out.println("<tr><td id=\"filters\" colspan=\"2\">");
        if (filterBuffer.length() > 0) {
            out.print("Filtered: ");
            out.println(filterBuffer.toString());
        } else {
            out.print("To further narrow your list, use the filters on the left.");
        }

        out.println("</td></tr></table>");
        out.print("</td></tr><tr><td valign=\"top\">");
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

                SearchResult sResult = (SearchResult) _schools.get(i);
                School school = getSchool(sResult);

                if (school != null) {
                    out.println("<tr class=\"result_row\">");
                    out.println("<td class=\"checkbox\" width=\"1\">");
                    out.print("<input name=\"sc\" type=\"checkbox\"  value=\"");
                    out.print(school.getDatabaseState().getAbbreviationLowerCase());
                    out.print(school.getId());
                    out.print("\" /></td>");

                    out.println("<td>");

                    out.print("<a href=\"/modperl/browse_school/");
                    out.print(school.getDatabaseState().getAbbreviation());
                    out.print("/");
                    out.print(school.getId().toString());
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
        out.println("<input type=\"image\" name=\"compare\" src=\"/res/img/btn_comparechecked_149x21.gif\" alt=\"Compare checked Schools\"/>");
        out.println("<input type=\"image\" name=\"save\" src=\"/res/img/btn_savechecked2msl_173x21.gif\" alt=\"Save checked to My School List\"/>");
    }

    private static String capitalize(String s) {
        String capString = s;
        if (s != null && s.length() > 1) {
            String initial = s.substring(0, 1);
            String rest = s.substring(1, s.length());
            StringBuffer buffer = new StringBuffer(s.length());
            buffer.append(initial.toUpperCase());
            buffer.append(rest);
            capString = buffer.toString();
        }
        return capString;
    }
}

