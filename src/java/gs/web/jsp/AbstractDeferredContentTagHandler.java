package gs.web.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public abstract class AbstractDeferredContentTagHandler extends SimpleTagSupport {

    public static final String REQUEST_ATTRIBUTE_NAME = "deferredContent";

    public void doTag() throws JspException, IOException {
        String nonDeferredContent = "";

        // Write out the place holder
        if (isDeferred()) {
            PageContext pageContext = (PageContext) getJspContext();

            StringBuffer defer = new StringBuffer();

            String deferredContent = (String) pageContext.getAttribute(REQUEST_ATTRIBUTE_NAME, PageContext.REQUEST_SCOPE);
            if (deferredContent != null) {
                defer.append(deferredContent);
            }
            defer.append("<div id=\"defer-").append(getId()).append("\">").append(getDeferredContent()).append("</div>");
            pageContext.setAttribute("deferredContent", defer.toString(), PageContext.REQUEST_SCOPE);
        } else {
            nonDeferredContent = getDeferredContent();
        }

        StringBuffer xhtml = new StringBuffer();
        try {
            xhtml.append("<div id=\"")
                    .append(getId())
                    .append("\">")
                    .append(nonDeferredContent)
                    .append("</div>");
            writeOutput(xhtml);
        } catch (Exception e) {
            throw new JspException(e);
        }
    }

    /**
     * Made this a separate method to make this class easier to test
     */
    protected void writeOutput(StringBuffer xhtml) throws IOException {
        getJspContext().getOut().println(xhtml);
    }

    /**
     * Method to be overriden for subclasses that want to change the default
     * deferral behavior.  A better option is to not use this tag handler
     * if your tag does not defer content.
     *
     * TODO: remove method once we switch over all ads to google ad manager.
     * @return true
     */
    public abstract boolean isDeferred();

    /**
     *
     * @return unique id for this deferred tag
     */
    public abstract String getId();

    /**
     * @return Content to defer
     */
    public abstract String getDeferredContent() throws IOException, JspException;
}
