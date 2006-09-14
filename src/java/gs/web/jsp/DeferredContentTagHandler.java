package gs.web.jsp;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Take the enclosed XHTML, put a div marker in it's place, and put the content in a variable.
 * The content (e.g. ads) can then be written out at the end of the page and then moved to the
 * correct location with javascript.
 *
 * @author Todd Huss <mailto:thuss@greatschools.net>
 */
public class DeferredContentTagHandler extends SimpleTagSupport {

    /**
     * The required CSS id
     */
    private String _id;

    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext) getJspContext();
        JspWriter out = pageContext.getOut();

        // Append the body
        StringWriter bodyWriter = new StringWriter();
        getJspBody().invoke(bodyWriter);
        StringBuffer defer = new StringBuffer();
        String deferredContent = (String) pageContext.getAttribute("deferredContent", PageContext.REQUEST_SCOPE);
        if (deferredContent != null) {
            defer.append(deferredContent);
        }
        defer.append("<div id=\"defer-").append(_id).append("\">").append(bodyWriter).append("</div>");
        pageContext.setAttribute("deferredContent", defer.toString(), PageContext.REQUEST_SCOPE);

        // Write out the place holder
        StringBuffer xhtml = new StringBuffer();
        try {
            xhtml.append("<div id=\"").append(_id).append("\"></div>");
            out.println(xhtml);
        } catch (Exception e) {
            throw new JspException(e);
        }
    }

    public void setId(String id) {
        _id = id;
    }

}
