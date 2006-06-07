package gs.web.search;

import gs.data.school.School;
import gs.data.search.highlight.TextHighlighter;
import gs.data.state.StateManager;
import gs.data.util.SpringUtil;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
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

                String context = result.getContext();

                out.write("<tr class=\"result_row\">");

                switch (result.getType()) {

                    case SearchResult.SCHOOL:

                        out.write("<td class=\"school\" colspan=\"3\">");
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

                        out.write("<h3><a href=\"");
                        out.write(_urlUtil.buildUrl(urlBuffer.toString(), request));
                        out.write("\">");
                        out.write(TextHighlighter.highlight(result.getHeadline(),
                                getSrcQuery(), "name"));

                        out.write("</a></h3>");
                        School school = getSchool(_stateManager.getState(result.getState()),
                                Integer.valueOf(result.getId()));
                        if (school != null) {
                            out.write("<div class=\"about\">");
                            out.write("<span class=\"st\">");
                            out.write(school.getType().getSchoolTypeName());
                            out.write("</span>");
                            String gl = school.getGradeLevels().getRangeString();
                            if (StringUtils.isNotEmpty(gl)) {
                                out.write("&nbsp;&#183;&nbsp;");
                                out.write("<span class=\"gl\">");
                                out.write(gl);
                                out.write("</span>");
                            }
                            out.write("</div>");
                            out.write("<address>");
                            out.write(school.getPhysicalAddress().toString());
                            out.write("</address>");
                            out.write("<div class=\"phone\">");
                            out.write(school.getPhone());
                            out.write("</div>");
                        }
                        out.write("</td>");

                        break;
                    case SearchResult.ARTICLE:

                        out.write("<td class=\"article\" colspan=\"3\">");

                        StringBuffer articleHrefBuffer =
                                new StringBuffer("/cgi-bin/show");
                        articleHrefBuffer.append("article/");
                        articleHrefBuffer.append(getSessionContext().getState().getAbbreviation());
                        articleHrefBuffer.append("/");
                        articleHrefBuffer.append(result.getId());

                        out.write("<h3><a href=\"");
                        out.write(_urlUtil.buildUrl(articleHrefBuffer.toString(), request));
                        out.write("\">");
                        out.write(TextHighlighter.highlight(result.getHeadline(),
                                getSrcQuery(), "name"));

                        out.write("</a></h3>");

                        if (context != null) {
                            out.write("<p>");
                            out.write(TextHighlighter.highlight(context,
                                    getSrcQuery(), "address"));
                            out.write("</p>");
                        }
                        out.write("</td>");

                        break;
                    case SearchResult.TERM:

                        out.write("<td class=\"glossary\" colspan=\"3\">");
                        StringBuffer termBuffer = new StringBuffer("/cgi-bin/glossary_single/");
                        termBuffer.append(getStateOrDefault().getAbbreviationLowerCase());
                        termBuffer.append("/?id=");
                        termBuffer.append(result.getId());

                        out.write("<h3><a href=\"");
                        out.write(_urlUtil.buildUrl(termBuffer.toString(), request));
                        out.write("\">");
                        out.write(TextHighlighter.highlight(result.getHeadline(),
                                getSrcQuery(), "name"));

                        out.write("</a></h3>");
                        if (context != null) {
                            out.write("<p>");
                            out.write(TextHighlighter.highlight(context,
                                    getSrcQuery(), "address"));
                            out.write("</p>");
                        }
                        out.write("</td>");

                        break;
                    default:
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
