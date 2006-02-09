package gs.web.search;

import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import gs.data.school.School;
import gs.web.util.UrlUtil;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class MainResultsTag extends ResultsTableTagHandler {

    private List _results;
    private String _constraint = null;
    private UrlUtil _urlUtil;

    public MainResultsTag() {
        super();
        _urlUtil = new UrlUtil();
    }

    public void setResults(List results) {
        _results = results;
    }

    public void setConstraint(String c) {
        _constraint = c;
    }

    public String getConstraint() {
        return _constraint;
    }

    public void doTag() throws IOException {

        JspWriter out = getJspContext().getOut();
        PageContext pc = (PageContext) getJspContext().findAttribute(PageContext.PAGECONTEXT);
        HttpServletRequest request = null;
        if (pc != null) {
            request = (HttpServletRequest) pc.getRequest();
        }

        out.print("<table width=\"100%\">");
        out.println("<tr>");
        out.println("<td colspan=\"2\">");
        if (_results != null && _total > 0) {
            out.println("<table id=\"mainresults\">");
            for (int i = 0; i < _results.size(); i++) {
                SearchResult result = (SearchResult) _results.get(i);
                out.println("<tr class=\"result_row\">");

                out.print("<td class=\"headline\" colspan=\"3\">");
                switch (result.getType()) {
                    case SearchResult.SCHOOL:
                        StringBuffer urlBuffer =
                                new StringBuffer("/modperl/browse_school/");
                        urlBuffer.append(result.getState());
                        urlBuffer.append("/");
                        urlBuffer.append(result.getId());

                        out.print("<a href=\"");
                        out.print(_urlUtil.buildUrl(urlBuffer.toString(), request));
                        out.println("\">");

                        break;
                    case SearchResult.ARTICLE:

                        StringBuffer articleHrefBuffer =
                                new StringBuffer("/cgi-bin/show");
                        if (result.isInsider()) {
                            articleHrefBuffer.append("part");
                        }
                        articleHrefBuffer.append("article/");
                        articleHrefBuffer.append(getSessionContext().getState().getAbbreviation());
                        articleHrefBuffer.append("/");
                        articleHrefBuffer.append(result.getId());

                        out.print("<a href=\"");
                        out.print(_urlUtil.buildUrl(articleHrefBuffer.toString(), request));
                        out.print("\">");

                        break;
                    case SearchResult.TERM:
                        StringBuffer termBuffer = new StringBuffer("/cgi-bin/glossary_single/");
                        termBuffer.append(getStateOrDefault().getAbbreviationLowerCase());
                        termBuffer.append("/?id=");
                        termBuffer.append(result.getId());

                        out.print("<a href=\"");
                        out.print(_urlUtil.buildUrl(termBuffer.toString(), request));
                        out.print("\">");

                        break;
                    default:
                }

                out.print(result.getHeadline());
                out.println("</a>");
                out.println("</td></tr>");

                out.println("<tr class=\"contextrow\">");
                if (result.getType() == SearchResult.SCHOOL) {
                    out.println("<td>");
                    School school = getSchool(result);
                    if (school != null) {
                        out.println(school.getPhysicalAddress().toString());
                        out.print("&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;");
                        out.println(school.getType().getSchoolTypeName());
                        String gl = school.getGradeLevels().getRangeString();
                        if (StringUtils.isNotEmpty(gl)) {
                            out.print("&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;");
                            out.println(gl);
                        }
                    }
                    out.println("</td>");
                } else {
                    String context = result.getContext();
                    if (context != null) {
                        out.println("<td colspan=\"3\">");
                        out.print("<span class=\"context\">");
                        out.print(context);
                        out.println("</span>");
                        out.println("</td>");
                    }
                }

                out.println("</tr>");
                if (_debug) {
                    out.print("<tr><td><pre class=\"explanation\">");
                    out.print(result.getExplanation());
                    out.println("</pre></td></tr>");
                }
            }
            out.println("</table>");
        } else {
            getJspBody().getJspContext().setAttribute("noresults", "true");
        }

        out.println("</td></tr><tr><td class=\"results_pagenav\">");
        writePageNumbers(out);
        out.println("</td></tr></table>");

        try {
            getJspBody().invoke(out);
        } catch (JspException e) {
            throw new IOException(e.getMessage());
        }
    }
}
