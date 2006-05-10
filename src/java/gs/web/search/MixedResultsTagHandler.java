package gs.web.search;

import gs.data.school.School;
import gs.data.search.highlight.TextHighlighter;
import gs.data.state.StateManager;
import gs.data.util.SpringUtil;
import gs.web.util.UrlUtil;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.io.Writer;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class MixedResultsTagHandler extends ResultsTableTagHandler {

    private UrlUtil _urlUtil;
    private Writer _writer;
    private static final StateManager _stateManager;

    static {
        _stateManager = (StateManager) SpringUtil.getApplicationContext().getBean(StateManager.BEAN_ID);
    }

    public MixedResultsTagHandler() {
        super();
        _urlUtil = new UrlUtil();
    }

    public void setWriter(Writer writer) {
        _writer = writer;
    }

    public Writer getWriter() {
        if (_writer == null) {
            _writer = getJspContext().getOut();
        }
        return _writer;
    }

    public void doTag() throws IOException {

        Writer out = getWriter();
        PageContext pc = (PageContext) getJspContext().findAttribute(PageContext.PAGECONTEXT);
        HttpServletRequest request = null;
        if (pc != null) {
            request = (HttpServletRequest) pc.getRequest();
        }

        out.write("<table width=\"100%\">");
        out.write("<tr>");
        out.write("<td colspan=\"2\">");
        if (getResults() != null && _total > 0) {
            out.write("<table id=\"mainresults\">");
            for (int i = 0; i < getResults().size(); i++) {
                SearchResult result = (SearchResult) getResults().get(i);
                out.write("<tr class=\"result_row\">");

                out.write("<td class=\"headline\" colspan=\"3\">");
                switch (result.getType()) {
                    case SearchResult.SCHOOL:
                        StringBuffer urlBuffer;
                        if ("private".equals(result.getSchoolType())) {
                            urlBuffer = new StringBuffer("/cgi-bin/");
                            urlBuffer.append(result.getState());
                            urlBuffer.append("/private/");

                        } else {
                            urlBuffer =
                                    new StringBuffer("/modperl/browse_school/");
                            urlBuffer.append(result.getState().toLowerCase());
                            urlBuffer.append("/");
                        }
                        urlBuffer.append(result.getId());

                        out.write("<a href=\"");
                        out.write(_urlUtil.buildUrl(urlBuffer.toString(), request));
                        out.write("\">");

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

                        out.write("<a href=\"");
                        out.write(_urlUtil.buildUrl(articleHrefBuffer.toString(), request));
                        out.write("\">");

                        break;
                    case SearchResult.TERM:
                        StringBuffer termBuffer = new StringBuffer("/cgi-bin/glossary_single/");
                        termBuffer.append(getStateOrDefault().getAbbreviationLowerCase());
                        termBuffer.append("/?id=");
                        termBuffer.append(result.getId());

                        out.write("<a href=\"");
                        out.write(_urlUtil.buildUrl(termBuffer.toString(), request));
                        out.write("\">");

                        break;
                    default:
                }

                out.write(TextHighlighter.highlight(result.getHeadline(),
                        getSrcQuery(), "name"));

                out.write("</a>");
                out.write("</td></tr>");

                out.write("<tr class=\"contextrow\">");
                if (result.getType() == SearchResult.SCHOOL) {
                    out.write("<td>");
                    School school = getSchool(_stateManager.getState(result.getState()),
                            Integer.valueOf(result.getId()));
                    if (school != null) {
                        //out.write(school.getPhone());
                        //out.write("&nbsp;&nbsp;&nbsp;&#183;&nbsp;&nbsp;&nbsp;");
                        out.write(school.getPhysicalAddress().toString());
                        out.write("&nbsp;&nbsp;&nbsp;&#183;&nbsp;&nbsp;&nbsp;");
                        out.write(school.getType().getSchoolTypeName());
                        String gl = school.getGradeLevels().getRangeString();
                        if (StringUtils.isNotEmpty(gl)) {
                            out.write("&nbsp;&nbsp;&nbsp;&#183;&nbsp;&nbsp;&nbsp;");
                            out.write(gl);
                        }
                    }
                    out.write("</td>");
                } else {
                    String context = result.getContext();
                    if (context != null) {
                        out.write("<td colspan=\"3\">");
                        out.write("<span class=\"context\">");
                        out.write(TextHighlighter.highlight(context,
                                getSrcQuery(), "address"));
                        out.write("</span>");
                        out.write("</td>");
                    }
                }

                out.write("</tr>");
                if (_debug) {
                    out.write("<tr><td><pre class=\"explanation\">");
                    out.write(result.getExplanation());
                    out.write("</pre></td></tr>");
                }
            }
            out.write("</table>");
        } else {
            getJspBody().getJspContext().setAttribute("noresults", "true");
        }

        out.write("</td></tr><tr><td class=\"results_pagenav\">");
        UrlBuilder builder = new UrlBuilder(request, "/search/search.page");
        builder.addParametersFromRequest(request);
        writePageNumbers(getPage(), request, builder, _total);
        out.write("</td></tr></table>");

        try {
            getJspBody().invoke(out);
        } catch (JspException e) {
            throw new IOException(e.getMessage());
        }
    }
}
