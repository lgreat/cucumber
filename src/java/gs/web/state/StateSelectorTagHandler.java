package gs.web.state;

import gs.web.jsp.BaseTagHandler;
import gs.data.state.StateManager;
import gs.data.state.State;
import gs.data.util.SpringUtil;

import javax.servlet.jsp.JspWriter;
import java.util.List;
import java.io.IOException;

/**
 * This tag handler produces a drop-down which defaults to using the fifty
 * state abbreviations in alphabetical order of the abbreviations, not
 * the long state name.  When useLongNames == true, then the names are listed
 * in the alphabetical order of the long names.
 * <p/>
 * The tag accept all optional parameter to allow a non-state selection.  This
 * appears with the option name="state" value="all".
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class StateSelectorTagHandler extends BaseTagHandler {

    private boolean _allowNoState = false;
    private boolean _useLongNames = false;
    private String _styleClass = null;
    private String _onChange = null;

    private static final StateManager _stateManager;
    private static final List _stateAbbreviations;
    private static final List _statesList;

    static {
        _stateManager = (StateManager) SpringUtil.getApplicationContext().getBean(StateManager.BEAN_ID);
        _stateAbbreviations = _stateManager.getSortedAbbreviations();
        _statesList = StateManager.getList();
    }


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
     * @param styleClass - css class to use
     */
    public void setStyleClass(String styleClass) {
        _styleClass = styleClass;
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
        if (_styleClass != null) {
            out.print(" class=\"" + _styleClass + "\"");
        }
        if (_onChange != null) {
            out.print(" onChange=\"" + _onChange + "\"");
        }
        out.println(">");

        if (_allowNoState) {
            out.println("<option value=\"all\">State</option>");
        }

        State currentState = getStateOrDefault();

        if (_useLongNames) {
            for (int i = 0; i < _statesList.size(); i++) {
                out.print("<option value=\"");
                State state = (State) _statesList.get(i);
                out.print(state.getAbbreviationLowerCase());
                out.print("\" ");
                if (currentState.equals(state)) {
                    out.print(" selected='selected' ");
                }
                out.print(">");
                out.print(state.getLongName());
                out.println("</option>");
            }
        } else {
            for (int i = 0; i < _stateAbbreviations.size(); i++) {
                out.print("<option value=\"");
                String state = (String) _stateAbbreviations.get(i);
                out.print(state);
                out.print("\" ");
                if (currentState.getAbbreviation().equalsIgnoreCase(state)) {
                    out.print(" selected='selected' ");
                }
                out.print(">");
                out.print(state);
                out.println("</option>");
            }
        }
        out.println("</select>");
    }
}
