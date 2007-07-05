/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolTableHeaderTagHandler.java,v 1.26 2007/07/05 16:53:45 thuss Exp $
 */

package gs.web.search;

import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
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
import java.util.Iterator;

/**
 * This tag handler generates a table of schools.
 * This tag is used on and school/schoolsTable.jspx.
 * todo: This class is an <strong>ugly mess</strong> and badly needs to be
 * refactored.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolTableHeaderTagHandler extends ResultsTableTagHandler {

    public static final String BEAN_ID = "schoolTableHeaderTagHandler";

    private static UrlUtil urlUtil = new UrlUtil();
    private String _srcQuery = null;
    private Boolean _showAll;
    private String _cityName;
    private String _cityDisplayName;
    private Integer _districtId;
    private LevelCode _levelCode;
    private String[] _schoolType;

    private static final Log _log = LogFactory.getLog(SchoolTableHeaderTagHandler.class);

    public void doTag() throws IOException {
        PageContext pc = (PageContext) getJspContext().findAttribute(PageContext.PAGECONTEXT);
        HttpServletRequest request = (HttpServletRequest) pc.getRequest();

        boolean showall = _showAll != null && _showAll.booleanValue();

        District district = null;
        if (_districtId != null && _districtId.intValue() != 0) {
            district = getDistrictDao().findDistrictById(getState(), _districtId);
        }

        if (district != null) {
            printDistrictHeader(district, showall, request);
        } else {
            printCityHeader(request);
        }
        request.setAttribute("pagingInfo", getPagingInfo(showall, request));
    }

    private void printDistrictHeader(District district, boolean showall, HttpServletRequest request) throws IOException {
        JspWriter out = getJspContext().getOut();

        out.println("<table width=\"100%\"><tr><td>");

        out.print("<h1>");
        out.print("Schools in ");
        out.print(district.getName());
        out.print("</h1>");

        out.print("</td><td align=\"right\" style=\"padding-right:15px;white-space:nowrap\">");

        out.println("</td></tr>");
        out.println("</table>");

        out.println("<table width=\"100%\">");

        out.println("<tr><td>");
        UrlBuilder districtProfile = new UrlBuilder(district, UrlBuilder.DISTRICT_PROFILE);
        out.print(districtProfile.asAHref(request, "<span class=\"minilink\">View district information</span>"));
        out.println("</td></tr>");

        // "compare all" links
        out.println("<tr><td colspan=\"2\">");
        out.print("<div id=\"comparelinks\">Compare ");
        out.print("<h1 style=\"display:inline;font-size:inherited; color: inherited\">");
        out.print(district.getName());
        out.print(" public schools</h1>: ");

        possiblyAddLinebreak(district.getName(), out);

        UrlBuilder compareBuilder = new UrlBuilder(request,
                "/cgi-bin/cs_compare/" + getState().getAbbreviationLowerCase());
        compareBuilder.setParameter("tab", "over");

        boolean showElementary = false, showMiddle = false, showHigh = false;
        compareBuilder.setParameter("district", district.getId().toString());
        compareBuilder.setParameter("area", "d");
        compareBuilder.setParameter("sortby", "name");
        String distId = String.valueOf(district.getId());
        if (getSchoolDao().countSchoolsInDistrict(getState(),
                SchoolType.PUBLIC, LevelCode.ELEMENTARY, distId) > 0) {
            showElementary = true;
        }
        if (getSchoolDao().countSchoolsInDistrict(getState(),
                SchoolType.PUBLIC, LevelCode.MIDDLE, distId) > 0) {
            showMiddle = true;
        }
        if (getSchoolDao().countSchoolsInDistrict(getState(),
                SchoolType.PUBLIC, LevelCode.HIGH, distId) > 0) {
            showHigh = true;
        }

        StringBuffer sb = calcCompareLinks(compareBuilder, request, showElementary, showMiddle, showHigh);
        String links = sb.toString();
        out.print(links.replaceFirst("\\|.$", ""));

        out.print("</div>");
        out.println("</td></tr>");
        // end "compare all" links

        // start filter row

        StringBuffer filterBuffer = createFilterBuffer(getSrcQuery(), request);
        printTableClose(out, filterBuffer);
    }

    private void printTableClose(JspWriter out, StringBuffer buffer) throws IOException {
        out.println("<tr><td id=\"filters\" colspan=\"2\">");
        if (buffer != null && buffer.length() > 0) {
            out.print("Filtered: ");
            out.println(buffer.toString());
        } else {
            out.print("To further narrow your list, use the filters on the left.");
        }
        out.println("</td></tr></table>");
    }

    private void printCityHeader(HttpServletRequest request) throws IOException {
        JspWriter out = getJspContext().getOut();

        out.println("<table width=\"100%\"><tr><td>");

        out.print("<h1>");
        out.print(SchoolsController.calcCitySchoolsTitle(_cityDisplayName, _levelCode, _schoolType));
        out.print("</h1>");

        out.print("</td><td align=\"right\" style=\"padding-right:15px;white-space:nowrap\">");

        out.println("</td></tr>");
        out.println("</table>");

        out.println("<table width=\"100%\">");

        out.println("<tr><td>");
        UrlBuilder cityPage = new UrlBuilder(UrlBuilder.CITY_PAGE, getState(), _cityName);
        out.print(cityPage.asAHref(request, "<span class=\"minilink\">View city information</span>"));
        out.println("</td></tr>");

        // "compare all" links
        out.println("<tr><td colspan=\"2\">");
        out.print("<div id=\"comparelinks\">Compare ");
        out.print("<h1 style=\"display:inline;font-size:inherited; color: inherited\">");
        out.print(_cityDisplayName);
        out.print(" schools</h1>: ");

        possiblyAddLinebreak(_cityDisplayName, out);

        UrlBuilder compareBuilder = new UrlBuilder(request,
                "/cgi-bin/cs_compare/" + getState().getAbbreviationLowerCase());
        compareBuilder.setParameter("tab", "over");

        boolean showElementary = false, showMiddle = false, showHigh = false;
        compareBuilder.setParameter("area", "m");
        compareBuilder.setParameter("city", _cityName);
        compareBuilder.setParameter("sortby", "distance");
        if (getSchoolDao().countSchools(getState(),
                null, LevelCode.ELEMENTARY, _cityName) > 0) {
            showElementary = true;
        }
        if (getSchoolDao().countSchools(getState(),
                null, LevelCode.MIDDLE, _cityName) > 0) {
            showMiddle = true;
        }
        if (getSchoolDao().countSchools(getState(),
                null, LevelCode.HIGH, _cityName) > 0) {
            showHigh = true;
        }

        StringBuffer sb = calcCompareLinks(compareBuilder, request, showElementary, showMiddle, showHigh);
        String links = sb.toString();
        out.print(links.replaceFirst("\\|.$", ""));

        out.print("</div>");
        out.println("</td></tr>");
        // end "compare all" links

        // start filter row

        StringBuffer filterBuffer = createFilterBuffer(getSrcQuery(), request);
        printTableClose(out, filterBuffer);
    }

    /**
     * If the provided string is longer than 12 characters write a "<br/>" tag to
     * the output stream.
     * @param s a String
     * @param out a JspWriter - the "output stream"
     * @throws IOException .
     */
    void possiblyAddLinebreak(String s, JspWriter out) throws IOException {
        if (StringUtils.isNotBlank(s)) {
            if (s.length() > 12) {
                out.println("<br/><span class=\"compareSpacer\"></span>");
            }
        }
    }

    private String getPagingInfo(boolean showall, HttpServletRequest request) throws IOException {
        StringBuffer buffer = new StringBuffer();
        if (_total > 0) {
            if (!showall) {
                int page = ((getPage() > 0) ? (getPage() - 1) : 0);
                buffer.append(String.valueOf((page * PAGE_SIZE) + 1));
                buffer.append(" - ");
                int x = (page * PAGE_SIZE) + PAGE_SIZE;
                if (_total > x) {
                    buffer.append(String.valueOf(x));
                } else {
                    buffer.append(String.valueOf(_total));
                }
            } else {
                buffer.append("1 - ");
                buffer.append(String.valueOf(_total));
            }
            buffer.append(" of ");
            buffer.append(String.valueOf(_total));
        }

        String showAllHref = "";
        if (!showall && (_total > PAGE_SIZE)) {
            StringBuffer hrefBuffer = new StringBuffer("/schools.page?");
            hrefBuffer.append(getSrcQuery());
            hrefBuffer.append("&amp;showall=true");
            showAllHref = SchoolTableHeaderTagHandler.urlUtil.buildUrl(hrefBuffer.toString(), request);
            buffer.append("<a href=\"" + showAllHref + "\">");
            buffer.append("<span class=\"minilink\" style=\"padding-left:8px;\">Show all</span></a>");
        }
        return buffer.toString();
    }

    private StringBuffer calcCompareLinks(UrlBuilder compareBuilder, HttpServletRequest request, boolean showElementary, boolean showMiddle, boolean showHigh) {
        StringBuffer sb = new StringBuffer();
        if (showElementary) {
            compareBuilder.setParameter("level", "e");
            sb.append(compareBuilder.asAHref(request, "Elementary"));
            sb.append(" | ");
        }

        if (showMiddle) {
            compareBuilder.setParameter("level", "m");
            sb.append(compareBuilder.asAHref(request, "Middle"));
            sb.append(" | ");
        }

        if (showHigh) {
            compareBuilder.setParameter("level", "h");
            sb.append(compareBuilder.asAHref(request, "High"));
        }
        return sb;
    }

    StringBuffer createFilterBuffer(String qString, HttpServletRequest request) {
        StringBuffer filterBuffer = new StringBuffer();

        if (_levelCode != null) {
            for (Iterator i = _levelCode.getIterator(); i.hasNext();) {
                LevelCode.Level level = (LevelCode.Level) i.next();
                if (filterBuffer.length() > 0) {
                    filterBuffer.append(" | ");
                }
                filterBuffer.append(Util.capitalize(level.getLongName()));
                filterBuffer.append(" (<a href=\"");
                String qs = qString.replaceAll("\\&(amp;)?lc=" + level.getName(), "");
                filterBuffer.append(SchoolTableHeaderTagHandler.urlUtil.buildUrl("/schools.page?" + qs, request));
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
                    qs = qString.replaceAll("\\&(amp;)?st=public", "");
                } else if ("private".equals(_schoolType[i])) {
                    qs = qString.replaceAll("\\&(amp;)?st=private", "");
                } else if ("charter".equals(_schoolType[i])) {
                    qs = qString.replaceAll("\\&(amp;)?st=charter", "");
                }

                StringBuffer urlBuffer = new StringBuffer("/schools.page?");
                urlBuffer.append(qs);
                filterBuffer.append(" (<a href=\"");
                filterBuffer.append(SchoolTableHeaderTagHandler.urlUtil.buildUrl(urlBuffer.toString(), request));

                filterBuffer.append("\">remove</a>)");
            }
        }
        return filterBuffer;
    }

    public String getSrcQuery() {
        return _srcQuery;
    }

    public void setSrcQuery(String srcQuery) {
        if (srcQuery.indexOf("&amp;") < 0) {
            srcQuery = srcQuery.replaceAll("&", "&amp;");
        }
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

    public String getCityDisplayName() {
        return _cityDisplayName;
    }

    public void setCityDisplayName(String cityDisplayName) {
        _cityDisplayName = cityDisplayName;
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
