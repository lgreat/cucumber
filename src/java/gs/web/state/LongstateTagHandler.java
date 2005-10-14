package gs.web.state;

import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.jsp.BaseTagHandler;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * This tag replaces the $LONGSTATE text from a string and replaces it with
 * the current state or appriate text.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class LongstateTagHandler extends BaseTagHandler {

    private String _text;

    public void setText(String text) {
        _text = text;
    }

    public void doTag() throws IOException {

        String stateString = " your state ";

        ISessionFacade sc = getSessionContext();
        if (sc != null) {
            State s = sc.getState();
            if (s != null) {
                stateString = s.getLongName();
            }
        }

        JspWriter out = getJspContext().getOut();
        out.print(_text.replaceAll("\\$LONGSTATE", stateString));
    }
}
