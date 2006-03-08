package gs.web.search;

import gs.web.jsp.BaseTagHandler;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * This tag renders the filter links that are displayed on search.page for
 * lists of schools.  This tag supports filtering by school type (pub, pri, cha)
 * and gradelevel (e,m,h).
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolFilters extends BaseTagHandler {

    public void doTag() throws IOException {

        JspWriter out = getJspContext().getOut();

        out.println("<h2 id=\"filterheader\">Filter your list</h2>");
        out.println("<ul class=\"filterlist\">");

        // list header
        out.println("<li class=\"filtertype\">School Level</li>");

        String[] gls = (String[])getJspContext().findAttribute("gl");
        List gradeLevels = null;
        if (gls != null) gradeLevels = Arrays.asList(gls);

        String qString = "";

        PageContext pc = (PageContext)getJspContext().findAttribute(PageContext.PAGECONTEXT);
        if (pc != null) {
            HttpServletRequest request = (HttpServletRequest)pc.getRequest();
            qString = request.getQueryString();
            //remove page numbers from the url
            qString = qString.replaceAll("&p=\\p{Digit}[\\p{Digit}]?", "");
        }

        // Elementary
        out.print("<li class=\"filter\">");
        if (gradeLevels != null && gradeLevels.contains("elementary")) {
            out.print("Elementary (<a href=\"/search/search.page?");
            out.print(qString.replaceAll("\\&gl=elementary",""));
            out.print("\">remove</a>)");
        } else {
            out.print("<a href=\"/search/search.page?");
            out.print(qString);
            out.print("&gl=elementary\">Elementary</a>");
        }
        out.println("</li>");

        // Middle
        out.print("<li class=\"filter\">");
        if (gradeLevels != null && gradeLevels.contains("middle")) {
            out.print("Middle (<a href=\"/search/search.page?");
            out.print(qString.replaceAll("\\&gl=middle",""));
            out.print("\">remove</a>)");
        } else {
            out.print("<a href=\"/search/search.page?");
            out.print(qString);
            out.print("&gl=middle\">Middle</a>");
        }
        out.println("</li>");

        // High
        out.print("<li class=\"filter\">");
        if (gradeLevels != null && gradeLevels.contains("high")) {
            out.print("High (<a href=\"/search/search.page?");
            out.print(qString.replaceAll("\\&gl=high",""));
            out.print("\">remove</a>)");
        } else {
            out.print("<a href=\"/search/search.page?");
            out.print(qString);
            out.print("&gl=high\">High</a>");
        }
        out.println("</li></ul>");

        out.println("<ul class=\"filterlist\">");

        // list header
        out.println("<li class=\"filtertype\">School Type</li>");

        String[] sts = (String[])getJspContext().findAttribute("st");
        List schoolTypes = null;
        if (sts != null) schoolTypes = Arrays.asList(sts);


        // Public
        out.print("<li class=\"filter\">");
        if (schoolTypes != null && schoolTypes.contains("public")) {
            out.print("Public (<a href=\"/search/search.page?");
            out.print(qString.replaceAll("\\&st=public",""));
            out.print("\">remove</a>)");
        } else {
            out.print("<a href=\"/search/search.page?");
            out.print(qString);
            out.print("&st=public\">Public</a>");
        }
        out.println("</li>");

        // Charter
        out.print("<li class=\"filter\">");
        if (schoolTypes != null && schoolTypes.contains("charter")) {
            out.print("Charter (<a href=\"/search/search.page?");
            out.print(qString.replaceAll("\\&st=charter",""));
            out.print("\">remove</a>)");
        } else {
            out.print("<a href=\"/search/search.page?");
            out.print(qString);
            out.print("&st=charter\">Charter</a>");
        }
        out.println("</li>");

        // Private
        out.print("<li class=\"filter\">");
        if (schoolTypes != null && schoolTypes.contains("private")) {
            out.print("Private (<a href=\"/search/search.page?");
            out.print(qString.replaceAll("\\&st=private",""));
            out.print("\">remove</a>)");
        } else {
            out.print("<a href=\"/search/search.page?");
            out.print(qString);
            out.print("&st=private\">Private</a>");
        }
        out.println("</li></ul>");
    }
}
