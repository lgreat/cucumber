package gs.web.search;

import gs.web.jsp.BaseTagHandler;
import gs.data.state.State;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspContext;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.taglibs.standard.functions.Functions;
import org.apache.commons.lang.StringUtils;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public abstract class ResultsTableTagHandler extends BaseTagHandler {

    private int _total = 0;
    private int _page = 0;
    protected String _queryString = null;
    protected int PAGE_SIZE = 10;
    private String _sortColumn = null;
    private boolean _reverse = false;
    private List _results;
    private String[] gradeLevels;
    private String[] schoolTypes;

    public void setResults(List results) {
        _results = results;
    }

    public List getResults() {
        return _results;
    }

    public void setTotal(int total) {
        _total = total;
    }

    public void setPage(int page) {
        _page = page;
    }

    public void setQuery(String query) {
        _queryString = query;
    }

    public String getQueryString() {
        return _queryString;
    }

    public void setSortColumn(String sort) {
        if (sort != null) {
            _sortColumn = sort;
        }
    }

    public String getSortColumn() {
        return _sortColumn;
    }

    public abstract String getConstraint();

    public void setReverse(String reverse) {
        if (reverse != null && reverse.equals("t")) {
            _reverse = true;
        }
    }

    protected boolean sortReverse() {
        return _reverse;
    }

    public void setParameters(Map parameters) {
        if (parameters != null) {
            gradeLevels = (String[])parameters.get("gl");
            schoolTypes = (String[])parameters.get("st");
        } else {
            gradeLevels = null;
            schoolTypes = null;
        }
    }

    protected void writePageNumbers(JspWriter out) throws IOException {

        JspContext jspContext = getJspContext();
        String cityParam = null;
        String distParam = null;

        if (jspContext != null) {
            cityParam = (String)jspContext.findAttribute("city");
            if (cityParam == null) {
                distParam = (String)jspContext.findAttribute("district");
            }
        }

        if (_total > 0) {

            if (_total > PAGE_SIZE) {
                out.println("Results page:&nbsp;&nbsp;");

                if (_page < 1) { // when page has not be set yet.
                    _page = 1;
                }

                int start = (_page < 10) ? 1 : (_page - 5);
                int end = (_total / PAGE_SIZE) + ((_total % PAGE_SIZE) > 0 ? 1 : 0);
                int counter = 1;

                StringBuffer hrefBuffer = new StringBuffer(40);
                hrefBuffer.append("<a class=\"pad\" href=\"/search/search.page?q=");
                hrefBuffer.append(Functions.escapeXml(_queryString));

                if (cityParam != null) {
                    hrefBuffer.append("&city=");
                    hrefBuffer.append(cityParam);
                } else if (distParam != null) {
                    hrefBuffer.append("&district=");
                    hrefBuffer.append(distParam);
                }

                State s = getState();
                if (s != null) {
                    hrefBuffer.append("&state=");
                    hrefBuffer.append(s.getAbbreviationLowerCase());
                }

                String c = getConstraint();
                if (!StringUtils.isEmpty(c)) {
                    hrefBuffer.append("&amp;c=");
                    hrefBuffer.append(c);
                }

                if (gradeLevels != null) {
                    for(int i = 0; i < gradeLevels.length; i++) {
                        hrefBuffer.append("&gl=");
                        hrefBuffer.append(gradeLevels[i]);
                    }
                }

                if (schoolTypes != null) {
                    for(int i = 0; i < schoolTypes.length; i++) {
                        hrefBuffer.append("&st=");
                        hrefBuffer.append(schoolTypes[i]);
                    }
                }

                if (!StringUtils.isEmpty(_sortColumn)) {
                    hrefBuffer.append("&amp;sort=");
                    hrefBuffer.append(_sortColumn);
                }

                hrefBuffer.append("&amp;p=");
                String hrefStart = hrefBuffer.toString();

                if (_page > 1) {
                    out.print(hrefStart);
                    out.print(_page-1);
                    out.print("\">");
                    out.println("&lt;&nbsp;Previous</a>");
                }

                for (int i = start; i < end+1; i++) {

                    if (i == _page) {
                        out.print("<span class=\"active pad\">");
                    } else {
                        out.print(hrefStart);
                        out.print(i);
                        out.print("\">");
                    }
                    out.print(i);
                    if (i == _page) {
                        out.print("</span>");
                    } else {
                        out.println("</a>");
                    }
                    counter = counter + 1;
                    if (counter > 10) {
                        break;
                    }
                }

                if (_page < end) {
                    out.print(hrefStart);
                    if (_page < 1) { // when page has not be set yet.
                        _page = 1;
                    }
                    out.print(_page+1);
                    out.print("\">");
                    out.println("Next &nbsp;&gt;</a>");
                }
            }
        }
    }
}
