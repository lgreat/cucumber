package gs.web.search;

import gs.data.school.district.District;
import gs.data.school.LevelCode;
import gs.data.util.Address;
import gs.web.jsp.Util;
import gs.web.school.SchoolsController;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;

/**
 * This tag handler generates a table of schools.
 * This tag is used on and search/schoolsOnly.jspx.
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
        out.println("\"/>");
        out.println("<table class=\"columns\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
        out.println("<tr><td class=\"mainresultsheader\">");
        out.println("<table width=\"100%\"><tr><td><span id=\"resultsheadline\">");
        out.print("Found ");
        out.print(String.valueOf(_total));
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
                int page = ((getPage() > 0) ? (getPage() - 1) : 0);
                out.print(String.valueOf((page * PAGE_SIZE) + 1));
                out.print(" - ");
                int x = (page * PAGE_SIZE) + PAGE_SIZE;
                if (_total > x) {
                    out.print(String.valueOf(x));
                } else {
                    out.print(String.valueOf(_total));
                }
            } else {
                out.print("1 - ");
                out.print(String.valueOf(_total));
            }
            out.print(" of ");
            out.print(String.valueOf(_total));
        }

        if (!showall && (_total > PAGE_SIZE)) {

            StringBuffer hrefBuffer = new StringBuffer("/schools.page?");
            hrefBuffer.append(qString);
            hrefBuffer.append("&amp;showall=true");
            showAllHref = urlUtil.buildUrl(hrefBuffer.toString(), request);
            out.print("<a href=\"" + showAllHref + "\">");
            out.println("<span class=\"minilink\" style=\"padding-left:8px;\">Show all</span></a>");
        }

        out.println("</td></tr>");

        if (distId != null) {
            out.println("<tr><td>");
            out.print("<a href=\"");
            out.print(urlUtil.buildUrl(districtUrlBuffer.toString(), request));
            out.print("\">");
            out.println("<span class=\"minilink\">View district information</span></a>");
            out.println("</td></tr>");
        }

        StringBuffer filterBuffer = new StringBuffer();

        LevelCode levelCode = (LevelCode) getJspContext().findAttribute(SchoolsController.PARAM_LEVEL_CODE);
        if (levelCode != null) {
            for (Iterator i = levelCode.getIterator(); i.hasNext(); ) {
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

        String[] sts = (String[]) getJspContext().findAttribute("st");
        if (sts != null) {
            for (int i = 0; i < sts.length; i++) {
                String qs = "";
                if (filterBuffer.length() > 0) {
                    filterBuffer.append(" | ");
                }
                filterBuffer.append(Util.capitalize(sts[i]));
                if ("public".equals(sts[i])) {
                    qs = qString.replaceAll("\\&st=public", "");
                } else if ("private".equals(sts[i])) {
                    qs = qString.replaceAll("\\&st=private", "");
                } else if ("charter".equals(sts[i])) {
                    qs = qString.replaceAll("\\&st=charter", "");
                }

                StringBuffer urlBuffer = new StringBuffer("/schools.page?");
                urlBuffer.append(qs);
                filterBuffer.append(" (<a href=\"");
                filterBuffer.append(urlUtil.buildUrl(urlBuffer.toString(), request));

                filterBuffer.append("\">remove</a>)");
            }
        }

        // "compare all" links
        out.println("<tr><td colspan=\"2\">");
        out.print("<div id=\"comparelinks\">Compare ");
        out.print(cityOrDistrictName);
        out.print(" public schools: ");

        UrlBuilder compareBuilder = new UrlBuilder(request,
                "/cgi-bin/cs_compare/" + getState().getAbbreviationLowerCase());
        compareBuilder.setParameter("tab", "over");
        if (distId != null) {
            compareBuilder.setParameter("district", distId);
            compareBuilder.setParameter("area", "d");
            compareBuilder.setParameter("sortby", "name");
        } else {
            compareBuilder.setParameter("area", "m");
            compareBuilder.setParameter("city", cityOrDistrictName != null ? cityOrDistrictName : "");
            compareBuilder.setParameter("sortby", "distance");
        }

        compareBuilder.setParameter("level", "e");
        out.print(compareBuilder.asAHref("Elementary"));
        out.print(" | ");

        compareBuilder.setParameter("level", "m");
        out.print(compareBuilder.asAHref("Middle"));

        out.print(" | ");
        compareBuilder.setParameter("level", "h");
        out.print(compareBuilder.asAHref("High"));

        out.print("</div>");
        out.println("</td></tr>");
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
                writePageNumbers(out, new UrlBuilder(request, "/schools.page"));
            }
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
}

