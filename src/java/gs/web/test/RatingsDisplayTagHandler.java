/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RatingsDisplayTagHandler.java,v 1.4 2006/09/08 17:59:35 apeterson Exp $
 */
package gs.web.test;

import gs.data.test.rating.IRatingsDisplay;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.List;

/**
 * Generate the ratings table
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class RatingsDisplayTagHandler extends SimpleTagSupport {

    List _columns;
    List _rowGroups;

    public void doTag() throws IOException {
        PageContext pageContext = (PageContext) getJspContext().findAttribute(PageContext.PAGECONTEXT);
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        StringBuffer buffer = new StringBuffer();

        int totalColumns = _columns.size() + 1 + 1; //all students + combined

        //print table start and header complete
        buffer.append("<table id=\"rating\">")
                .append("<thead>")
                .append("<tr class=\"heading\">");

        buffer.append("<th scope=\"col\">").append("All Students").append("</th>");

        for (int i = 0; i < _columns.size(); i++) {
            String columnLabel = (String) _columns.get(i);
            buffer.append("<th scope=\"col\">").append(columnLabel).append("</th>");
        }
        buffer.append("<th scope=\"col\">").append("Combined").append("</th>");
        buffer.append("</tr>").append("</thead>");

        for (int i = 0; i < _rowGroups.size(); i++) {
            IRatingsDisplay.IRowGroup rowgroup = (IRatingsDisplay.IRowGroup) _rowGroups.get(i);
            int rows = rowgroup.getNumRows();

            buffer.append("<tbody>")
                    .append("<tr><td colspan=\"")
                    .append(totalColumns)
                    .append("\" scope=\"rowgroup\" class=\"rowgroup\">")
                    .append(rowgroup.getLabel())
                    .append("</td></tr>");

            for (int j = 0; j < rows; j++) {
                IRatingsDisplay.IRowGroup.IRow row = rowgroup.getRow(j);
                buffer.append("<tr>")
                        .append("<td scope=\"row\" class=\"row\">")
                        .append(row.getLabel())
                        .append("</td>");

                for (int k = 0; k < _columns.size(); k++) {
                    Integer rating = row.getRating(k);
                    buffer.append("<td>")
                            .append(rating != null ? rating : "n/a")
                            .append("</td>");
                }

                buffer.append("<td>Combined score</td>")
                        .append("</tr>");
            }
            buffer.append("</tbody>");
        }
        buffer.append("</table>");
        pageContext.getOut().print(buffer.toString());
    }

    public List getColumns() {
        return _columns;
    }

    public void setColumns(List columns) {
        _columns = columns;
    }

    public List getRowGroups() {
        return _rowGroups;
    }

    public void setRowGroups(List rowGroups) {
        _rowGroups = rowGroups;
    }
}
