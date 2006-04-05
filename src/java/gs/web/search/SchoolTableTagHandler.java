package gs.web.search;

import gs.data.school.LevelCode;
import gs.data.school.district.District;
import gs.web.jsp.Util;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
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
    private String _queryString = null;
    private Boolean _showAll;
    private String _cityName;
    private Integer _districtId;
    private LevelCode _levelCode ;
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

        String qString = getQueryString();

        boolean showall = _showAll != null && _showAll.booleanValue();


        District district = null;
        if (_districtId != null && _districtId.intValue() != 0) {
            district = getDistrictDao().findDistrictById(getState(), _districtId);
        }


        JspWriter out = getJspContext().getOut();

        out.println("<table width=\"100%\"><tr><td>");


        out.print("<h1 class=\"resultsheadline\">");
        String cityOrDistrictName = "";
        if (StringUtils.isNotEmpty(_cityName)) {
            cityOrDistrictName = _cityName;
            out.print(cityOrDistrictName);
            out.print((_total != 1) ? " schools" : "school");
        } else if (district != null) {
            cityOrDistrictName = district.getName();
            out.print((_total != 1) ? "Schools" : "School");
            out.print(" in ");
            out.print(district.getName());
        }
        out.print("</h1>");

        out.print("</td><td align=\"right\" style=\"padding-right:15px;white-space:nowrap\">");

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

        String showAllHref = "";
        if (!showall && (_total > PAGE_SIZE)) {
            StringBuffer hrefBuffer = new StringBuffer("/schools.page?");
            hrefBuffer.append(qString);
            hrefBuffer.append("&amp;showall=true");
            showAllHref = urlUtil.buildUrl(hrefBuffer.toString(), request);
            out.print("<a href=\"" + showAllHref + "\">");
            out.println("<span class=\"minilink\" style=\"padding-left:8px;\">Show all</span></a>");
        }

        out.println("</td></tr>");

        out.println("<tr><td>");
        if (district != null) {
            UrlBuilder districtProfile = new UrlBuilder(district, UrlBuilder.DISTRICT_PROFILE);
            out.print(districtProfile.asAHref(request, "<span class=\"minilink\">View district information</span>"));
        } else if (StringUtils.isNotEmpty(_cityName)) {
            UrlBuilder cityPage = new UrlBuilder(UrlBuilder.CITY_PAGE, getState(), _cityName);
            out.print(cityPage.asAHref(request, "<span class=\"minilink\">View city information</span>"));
        }
        out.println("</td></tr>");

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

        // "compare all" links
        out.println("<tr><td colspan=\"2\">");
        out.print("<div id=\"comparelinks\">Compare ");
        out.print("<h1 style=\"display:inline;\">");
        out.print(cityOrDistrictName);
        out.print(" public schools</h1>: ");

        UrlBuilder compareBuilder = new UrlBuilder(request,
                "/cgi-bin/cs_compare/" + getState().getAbbreviationLowerCase());
        compareBuilder.setParameter("tab", "over");
        if (district != null) {
            compareBuilder.setParameter("district", district.getId().toString());
            compareBuilder.setParameter("area", "d");
            compareBuilder.setParameter("sortby", "name");
        } else {
            compareBuilder.setParameter("area", "m");
            compareBuilder.setParameter("city", cityOrDistrictName != null ? cityOrDistrictName : "");
            compareBuilder.setParameter("sortby", "distance");
        }

        compareBuilder.setParameter("level", "e");
        out.print(compareBuilder.asAHref(request, "Elementary"));
        out.print(" | ");

        compareBuilder.setParameter("level", "m");
        out.print(compareBuilder.asAHref(request, "Middle"));

        out.print(" | ");
        compareBuilder.setParameter("level", "h");
        out.print(compareBuilder.asAHref(request, "High"));

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
                writePageNumbers(new UrlBuilder(request, "/schools.page"));
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
    }

    public String getQueryString() {
        return _queryString;
    }

    public void setQueryString(String queryString) {
        _queryString = queryString;
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

