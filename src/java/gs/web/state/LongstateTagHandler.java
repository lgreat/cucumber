package gs.web.state;

import gs.web.SessionContext;
import gs.data.state.State;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

/**
 * This tag replaces the $LONGSTATE text from a string and replaces it with
 * the current state or appriate text.
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class LongstateTagHandler extends SimpleTagSupport {

    private String _text;

    public void setText(String text) {
        _text = text;
    }

    public void doTag() throws IOException {

        JspContext jspContext = getJspContext();
        String stateString = " your state ";

        if (jspContext != null) {
            SessionContext sc = (SessionContext) jspContext.getAttribute(SessionContext.SESSION_ATTRIBUTE_NAME, PageContext.SESSION_SCOPE);
            if (sc != null) {
                State s = sc.getState();
                if (s != null) {
                    stateString = s.getLongName();
                }
            }
        }

        //String outString = _text.replace('$', ' ');
        //outString = outString.replaceAll("LONGSTATE", stateString);
        JspWriter out = getJspContext().getOut();
        out.print(_text.replaceAll("\\$LONGSTATE", stateString));
        //out.print(outString);
    }
}
