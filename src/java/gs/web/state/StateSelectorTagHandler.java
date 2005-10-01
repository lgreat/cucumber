package gs.web.state;

import gs.web.jsp.BaseTagHandler;
import gs.data.state.StateManager;
import gs.data.state.State;

import javax.servlet.jsp.JspWriter;
import java.util.List;
import java.io.IOException;

/**
 * This tag handler produces a drop-down with the fifty state abbreviations in
 * alphabetical order of the abbreviations, not the long state name.
 *
 * The tag accept all optional parameter to allow a non-state selection.  This
 * appears with the option name="state" value="all".
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class StateSelectorTagHandler extends BaseTagHandler {

    private boolean _allowNoState = false;
    private static StateManager _stateManager = new StateManager();
    private static List states = _stateManager.getSortedAbbreviations();

    /**
     * When set to true, this option allows the state selector will include
     * a "state" option with a value of "all".
     * @param allow - defaults to false;
     */
    public void setAllowNoState(boolean allow) {
        _allowNoState = allow;
    }

    public void doTag() throws IOException {

        JspWriter out = getJspContext().getOut();
        out.println ("<select name=\"state\">");

        if (_allowNoState) {
            out.println ("<option name=\"state\" value=\"all\">State</option>");
        }

        for (int i = 0; i< states.size(); i++) {
            out.print("<option name=\"state\" value=\"");
            String state = (String)states.get(i);
            out.print(state);
            out.print("\" ");
            State s = getStateOrDefault();
            if(s.getAbbreviation().equalsIgnoreCase(state)) {
                out.print(" selected ");
            }
            out.print(">");
            out.print(state);
            out.println("</option>");
        }
        out.println ("</option>");
        out.println ("</select>");
    }
}
