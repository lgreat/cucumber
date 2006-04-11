package gs.web.search;

import gs.web.jsp.BaseTagHandler;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * This is a base class for tag handlers that display search results.  Various
 * common methods are collected here.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public abstract class ResultsTableTagHandler extends BaseTagHandler {

    protected int _total = 0;
    protected int PAGE_SIZE = 10;
    protected boolean _debug = false;
    private String _sortColumn = null;
    private boolean _reverse = false;
    private List _results;

    public void setResults(List results) {
        _results = results;
    }

    public List getResults() {
        return _results;
    }

    public void setSortColumn(String sort) {
        _sortColumn = sort;
    }

    public String getSortColumn() {
        return _sortColumn;
    }

    public void setTotal(int total) {
        _total = total;
    }

    /**
     * Guaranteed to return a number > 0.
     *
     * @return an int
     */
    public int getPage() {
        int page = 1;
        try {
            String p = (String) getJspContext().findAttribute("p");
            if (!StringUtils.isBlank(p)) {
                int pageAttribute = Integer.parseInt(p);
                if (pageAttribute > 1) {
                    page = pageAttribute;
                }
            }
        } catch (Exception e) {/* ignore */}
        return page;
    }

    /**
     * Returns the value of the "q" parameter or an empty string if
     * there is no "q" param in the request.
     *
     * @return a non-null <code>String</code>
     */
    public String getSrcQuery() {
        String qs = "";
        String qParam = (String) getJspContext().findAttribute("q");
        if (StringUtils.isNotBlank(qParam)) {
            qs = qParam;
        }
        return qs;
    }

    public void setReverse(String reverse) {
        if ("t".equals(reverse)) {
            _reverse = true;
        } else {
            _reverse = false;
        }
    }

    protected boolean sortReverse() {
        return _reverse;
    }

    public void setDebug(boolean db) {
        _debug = db;
    }

    /**
     * Draws the page numbers.
     *
     * @param currentPage
     * @param builder the page that should be send to, for example "/search/search.page"
     * @param totalItems
     * @throws IOException
     */
    protected void writePageNumbers(final int currentPage, HttpServletRequest request, UrlBuilder builder, final int totalItems) throws IOException {

        JspWriter out = getJspContext().getOut();

        if (totalItems > PAGE_SIZE) {

            int start = (currentPage < 10) ? 1 : (currentPage - 5);
            int end = (totalItems / PAGE_SIZE) + ((totalItems % PAGE_SIZE) > 0 ? 1 : 0);

            if (currentPage > 1) {
                builder.setParameter("p", String.valueOf(currentPage - 1));
                out.print(builder.asAHref(request, "&lt;&#160;Previous", "pad"));
                out.println();
            }

            for (int i = start; i < Math.min(end + 1, end + 10); i++) {

                if (i == currentPage) {
                    out.print("<span class=\"active pad\">");
                    out.print(String.valueOf(i));
                    out.print("</span>");
                } else {
                    builder.setParameter("p", String.valueOf(i));
                    out.print(builder.asAHref(request, String.valueOf(i), "pad"));
                    out.println();
                }
            }

            if (currentPage < end) {
                builder.setParameter("p", String.valueOf(currentPage + 1));
                out.print(builder.asAHref(request, "Next &#160;&gt;", "pad"));
                out.println();
            }
        }
    }
}
