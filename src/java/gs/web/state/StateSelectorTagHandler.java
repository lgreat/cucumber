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
 * <p/>
 * The tag accept all optional parameter to allow a non-state selection.  This
 * appears with the option name="state" value="all".
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class StateSelectorTagHandler extends BaseTagHandler {

    private boolean _allowNoState = false;
    private boolean _useLongNames = false;
    private String _cssClass = null;
    private String _onChange = null;
    private static StateManager _stateManager = new StateManager();
    private static List states = _stateManager.getSortedAbbreviations();
    private static List statesList = StateManager.getList();

    /**
     * When set to true, this option allows the state selector will include
     * a "state" option with a value of "all".
     *
     * @param allow - defaults to false;
     */
    public void setAllowNoState(boolean allow) {
        _allowNoState = allow;
    }

    /**
     * This option allows you to set the css class of the state dropdown
     *
     * @param cssClass - css class to use
     */
    public void setCssClass(String cssClass) {
        _cssClass = cssClass;
    }

    /**
     * This option allows you to set an onChange handler on the state dropdown
     *
     * @param onChange - the onChange text to put on the web page
     */
    public void setOnChange(String onChange) {
        _onChange = onChange;
    }

    /**
     * If true states full names will be displayed.  Otherwise, abbreviations
     * are used as option values.
     */
    public void setUseLongNames(boolean useLong) {
        _useLongNames = useLong;
    }

    public void doTag() throws IOException {

        JspWriter out = getJspContext().getOut();
        out.print("<select name=\"state\"");
        if (_cssClass != null) {
            out.print(" class=\"" + _cssClass + "\"");
        }
        if (_onChange != null) {
            out.print(" onChange=\"" + _onChange + "\"");
        }
        out.println(">");

        if (_allowNoState) {
            out.println("<option name=\"state\" value=\"all\">State</option>");
        }

        if (_useLongNames) {
            for (int i = 0; i < statesList.size(); i++) {
                out.print("<option name=\"state\" value=\"");
                State state = (State) statesList.get(i);
                out.print(state.getAbbreviationLowerCase());
                out.print("\" ");
                State s = getStateOrDefault();
                if (s.equals(state)) {
                    out.print(" selected ");
                }
                out.print(">");
                out.print(state.getLongName());
                out.println("</option>");
            }
        } else {
            for (int i = 0; i < states.size(); i++) {
                out.print("<option name=\"state\" value=\"");
                String state = (String) states.get(i);
                out.print(state);
                out.print("\" ");
                State s = getStateOrDefault();
                if (s.getAbbreviation().equalsIgnoreCase(state)) {
                    out.print(" selected ");
                }
                out.print(">");
                out.print(state);
                out.println("</option>");
            }
        }
        out.println("</select>");
    }
}
