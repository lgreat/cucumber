package gs.web.search;

import gs.web.jsp.BaseTagHandler;
import gs.data.state.State;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

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
    private static final Logger _log =
            Logger.getLogger(ResultsTableTagHandler.class);

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

    protected void writePageNumbers(JspWriter out) throws IOException {

        if (_total > 0) {

            /*
            out.print("Result : ");
            int p = ((_page <= 1) ? 0 : (_page-1) * PAGE_SIZE);
            out.print(p+1);
            out.print(" - ");

            if ((p + PAGE_SIZE) <= _total) {
                out.print(p + PAGE_SIZE);
            } else {
                out.print(_total);
            }
            out.print(" of ");
            out.print(_total);
            out.println();
            */

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
                hrefBuffer.append(_queryString);
                State s = getState();
                if (s != null) {
                    hrefBuffer.append("&state=");
                    hrefBuffer.append(s.getAbbreviationLowerCase());
                }
                hrefBuffer.append("&amp;c=");
                hrefBuffer.append(getConstraint());
                hrefBuffer.append("&amp;sort=");
                hrefBuffer.append(_sortColumn);
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
