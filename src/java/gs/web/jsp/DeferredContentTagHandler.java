package gs.web.jsp;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Take the enclosed XHTML, put a div marker in it's place, and put the content in a variable.
 * The content (e.g. ads) can then be written out at the end of the page and then moved to the
 * correct location with javascript.
 *
 * @author Todd Huss <mailto:thuss@greatschools.net>
 */
public class DeferredContentTagHandler extends AbstractDeferredContentTagHandler {

    /**
     * The required CSS id
     */
    private String _id;

    public boolean isDeferred() {
        return true;
    }

    public String getId() {
        return _id;
    }

    public String getDeferredContent() throws IOException, JspException {
        // Append the body
        StringWriter writer = new StringWriter();
        getJspBody().invoke(writer);
        return writer.toString();
    }

    public void setId(String id) {
        _id = id;
    }

}
