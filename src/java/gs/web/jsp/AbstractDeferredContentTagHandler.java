package gs.web.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 */
// yfan: As of 5/5/2011, this class no longer supports deferred content.
//       I did this to simplify embedding ad code in a Java string, outside the context of a jsp. (needed for GS-11664)
//       GAM ads don't support deferred content anyway, so i went ahead and completed a to-do that said to get rid of it
//       I'm keeping the class name the same because cvs doesn't recognize file name changes, and I want people to be
//       able to see the diff.
public abstract class AbstractDeferredContentTagHandler extends SimpleTagSupport {

    public void doTag() throws JspException, IOException {
        try {
            writeOutput(getTagOutput());
        } catch (Exception e) {
            throw new JspException(e);
        }
    }

    private StringBuffer getTagOutput() throws IOException, JspException {
        StringBuffer xhtml = new StringBuffer();
        xhtml.append("<div id=\"")
                .append(getId())
                .append("\">")
                .append(getContent())
                .append("</div>");
        return xhtml;
    }

    /**
     * Made this a separate method to make this class easier to test
     */
    protected void writeOutput(StringBuffer xhtml) throws IOException {
        getJspContext().getOut().println(xhtml);
    }

    /**
     *
     * @return unique id for this deferred tag
     */
    public abstract String getId();

    /**
     * @return Content to defer
     */
    public abstract String getContent() throws IOException, JspException;
}