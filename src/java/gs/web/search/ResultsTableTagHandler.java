package gs.web.search;

import gs.data.school.LevelCode;
import gs.data.state.State;
import gs.web.jsp.BaseTagHandler;
import gs.web.school.SchoolsController;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.standard.functions.Functions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.Iterator;
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
     * @param path the page that should be send to, for example "/search/search.page"
     * @throws IOException
     */
    protected void writePageNumbers(UrlBuilder path) throws IOException {

        String cityParam = (String) getJspContext().findAttribute("city");
        JspWriter out = getJspContext().getOut();

        String distParam = null;
        String distNameParam = null;
        if (cityParam == null) {
            distParam = (String) getJspContext().findAttribute("district");
            distNameParam = (String) getJspContext().findAttribute("distname");
        }

        if (_total > 0) {

            if (_total > PAGE_SIZE) {

                int start = (getPage() < 10) ? 1 : (getPage() - 5);
                int end = (_total / PAGE_SIZE) + ((_total % PAGE_SIZE) > 0 ? 1 : 0);
                int counter = 1;

                StringBuffer hrefBuffer = new StringBuffer(40);
                hrefBuffer.append("<a class=\"pad\" href=\"");
                hrefBuffer.append(path.asSiteRelative(null));
                hrefBuffer.append("?q=");
                hrefBuffer.append(Functions.escapeXml(getSrcQuery()));

                if (cityParam != null) {
                    hrefBuffer.append("&city=");
                    hrefBuffer.append(cityParam);
                } else if (distParam != null) {
                    hrefBuffer.append("&district=");
                    hrefBuffer.append(distParam);
                }

                if (distNameParam != null) {
                    hrefBuffer.append("&distname=");
                    hrefBuffer.append(distNameParam);
                }

                State s = getState();
                if (s != null) {
                    hrefBuffer.append("&state=");
                    hrefBuffer.append(s.getAbbreviationLowerCase());
                }

                PageContext pc = (PageContext) getJspContext().findAttribute(PageContext.PAGECONTEXT);
                if (pc != null) {
                    HttpServletRequest request = (HttpServletRequest) pc.getRequest();
                    String c = request.getParameter("type");
                    if (StringUtils.isBlank(c)) {
                        c = request.getParameter("c");
                    }
                    if (StringUtils.isNotBlank(c)) {
                        hrefBuffer.append("&amp;type=");
                        hrefBuffer.append(c);
                    }
                }


                final Object lcObject = getJspContext().findAttribute(SchoolsController.MODEL_LEVEL_CODE);
                if (lcObject != null) {
                    // This comes in as either a String[] or a LevelCode object,
                    // depending on the controller.
                    LevelCode levelCode;
                    if (lcObject instanceof String[]) {
                        levelCode = LevelCode.createLevelCode((String[]) lcObject);
                    } else {
                        levelCode = (LevelCode) lcObject;
                    }
                    for (Iterator iter = levelCode.getIterator(); iter.hasNext();) {
                        LevelCode.Level level = (LevelCode.Level) iter.next();
                        hrefBuffer.append("&lc=");
                        hrefBuffer.append(level.getName());
                    }
                }

                String[] sts = (String[]) getJspContext().findAttribute("st");
                if (sts != null) {
                    for (int i = 0; i < sts.length; i++) {
                        hrefBuffer.append("&st=");
                        hrefBuffer.append(sts[i]);
                    }
                }

                if (!StringUtils.isBlank(_sortColumn)) {
                    hrefBuffer.append("&amp;sort=");
                    hrefBuffer.append(_sortColumn);
                }

                hrefBuffer.append("&amp;p=");
                String hrefStart = hrefBuffer.toString();

                if (getPage() > 1) {
                    out.print(hrefStart);
                    out.print(String.valueOf(getPage() - 1));
                    out.print("\">");
                    out.println("&lt;&#160;Previous</a>");
                }

                for (int i = start; i < end + 1; i++) {

                    if (i == getPage()) {
                        out.print("<span class=\"active pad\">");
                    } else {
                        out.print(hrefStart);
                        out.print(String.valueOf(i));
                        out.print("\">");
                    }
                    out.print(String.valueOf(i));
                    if (i == getPage()) {
                        out.print("</span>");
                    } else {
                        out.println("</a>");
                    }
                    counter = counter + 1;
                    if (counter > 10) {
                        break;
                    }
                }

                if (getPage() < end) {
                    out.print(hrefStart);
                    out.print(String.valueOf(getPage() + 1));
                    out.print("\">");
                    out.println("Next &#160;&gt;</a>");
                }
            }
        }
    }
}
