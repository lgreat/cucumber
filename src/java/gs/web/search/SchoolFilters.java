package gs.web.search;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolFilters extends BaseQueryTagHandler {

    public void doTag() throws IOException {

        JspWriter out = getJspContext().getOut();

        out.println("<h2 class=\"browseheader\">Filter your list</h2>");
        out.println("<ul class=\"filterlist\" id=\"schoolfilterlist\">");

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
        out.print("<li>");
        if (gradeLevels != null && gradeLevels.contains("elementary")) {
            out.print("Elementary (<a href=\"/search/search.page?");
            out.print(qString.replaceAll("\\&gl=elementary",""));
            out.print("\">remove</a>)");
        } else {
            out.print("<a href=\"/search/search.page?");
            out.print(qString);
            out.println("&gl=elementary\">Elementary</a>");
        }
        out.print("</li>");

        // Middle
        out.print("<li>");
        if (gradeLevels != null && gradeLevels.contains("middle")) {
            out.print("Middle (<a href=\"/search/search.page?");
            out.print(qString.replaceAll("\\&gl=middle",""));
            out.print("\">remove</a>)");
        } else {
            out.print("<a href=\"/search/search.page?");
            out.print(qString);
            out.println("&gl=middle\">Middle</a>");
        }
        out.print("</li>");

        // High
        out.print("<li>");
        if (gradeLevels != null && gradeLevels.contains("high")) {
            out.print("High (<a href=\"/search/search.page?");
            out.print(qString.replaceAll("\\&gl=high",""));
            out.print("\">remove</a>)");
        } else {
            out.print("<a href=\"/search/search.page?");
            out.print(qString);
            out.println("&gl=high\">High</a>");
        }
        out.print("</li></ul><br/>");

        out.println("<ul class=\"filterlist\">");


        String[] sts = (String[])getJspContext().findAttribute("st");
        List schoolTypes = null;
        if (sts != null) schoolTypes = Arrays.asList(sts);


        // Public
        out.print("<li>");
        if (schoolTypes != null && schoolTypes.contains("public")) {
            out.print("Public (<a href=\"/search/search.page?");
            out.print(qString.replaceAll("\\&st=public",""));
            out.print("\">remove</a>)");
        } else {
            out.print("<a href=\"/search/search.page?");
            out.print(qString);
            out.println("&st=public\">Public</a>");
        }
        out.print("</li>");

        // Charter
        out.print("<li>");
        if (schoolTypes != null && schoolTypes.contains("charter")) {
            out.print("Charter (<a href=\"/search/search.page?");
            out.print(qString.replaceAll("\\&st=charter",""));
            out.print("\">remove</a>)");
        } else {
            out.print("<a href=\"/search/search.page?");
            out.print(qString);
            out.println("&st=charter\">Charter</a>");
        }
        out.print("</li>");

        // Private
        out.print("<li>");
        if (schoolTypes != null && schoolTypes.contains("private")) {
            out.print("Private (<a href=\"/search/search.page?");
            out.print(qString.replaceAll("\\&st=private",""));
            out.print("\">remove</a>)");
        } else {
            out.print("<a href=\"/search/search.page?");
            out.print(qString);
            out.println("&st=private\">Private</a>");
        }
        out.print("</li></ul>");
    }
}
