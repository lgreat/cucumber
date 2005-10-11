package gs.web.search;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspContext;
import java.io.IOException;

/**
 * @author Chris Kimm <mailto:chris@seeqa.com>
 */
public class SchoolFiltersTag extends SimpleTagSupport {
    public void doTag() throws IOException {
        JspContext jspContext = getJspContext();
        String o = (String)jspContext.findAttribute("schooltype");
        String gl = (String)jspContext.findAttribute("gradelevel");

        JspWriter out = getJspContext().getOut();
        out.println("<table><tr><td>");
        out.println("<td>");

        out.println("<div class=\"checkbox\">");
        out.print("<input type=\"checkbox\" name=\"schooltype\" ");
        out.print("value=\"public\">");
        out.print(" Public");
        //out.print(o);
        out.println("</input>");
        out.println("</div>");

        out.println("<div class=\"checkbox\">");
        out.print("<input type=\"checkbox\" name=\"schooltype\" ");
        out.print(" value=\"private\">");
        out.print(" Private");
        out.println("</input>");
        out.println("</div>");

        out.println("<div class=\"checkbox\">");
        out.print("<input type=\"checkbox\" name=\"schooltype\" value=\"charter\">");
        out.print(" Charter");
        out.println("</input>");
        out.println("</div>");

        out.println("</td><td>");

        out.println("<div class=\"checkbox\">");
        out.print("<input type=\"checkbox\" name=\"gradelevel\" value=\"elementary\">");
        out.print(" Elementary");
        //out.print(gl);
        out.println("</input>");
        out.println("</div>");

        out.println("<div class=\"checkbox\">");
        out.print("<input type=\"checkbox\" name=\"gradelevel\" value=\"middle\">");
        out.print(" Middle");
        out.println("</input>");
        out.println("</div>");

        out.println("<div class=\"checkbox\">");
        out.print("<input type=\"checkbox\" name=\"gradelevel\" value=\"high\">");
        out.print(" High");
        out.println("</input>");
        out.println("</div>");

        out.println("</td></tr></table");
    }
}
