package gs.web.state;

import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.SpringUtil;
import org.apache.commons.lang.ObjectUtils;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.Iterator;

/**
 * This tag handler produces a drop-down &lt;select&gt; element, where the
 * states are submitted using their uppercase abbreviation under the "state"
 * form property.
 * Defaults to using the fifty
 * state abbreviations in alphabetical order of the abbreviations, not
 * the long state name.  When usingLongNames == true, then the names are listed
 * in the alphabetical order of the long names.
 * The tag accept all optional parameter to allow a non-state selection.  This
 * appears with the option name="state" value="all".
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class StateSelectorTagHandler extends SimpleTagSupport {

    private boolean _allowNoState = false;
    private boolean _usingLongNames = false;
    private String _styleClass = null;
    private String _onChange = null;
    private State _state;

    private static final StateManager _stateManager;

    static {
        _stateManager = (StateManager) SpringUtil.getApplicationContext().getBean(StateManager.BEAN_ID);
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
     * @param styleClass - css class to use for the &lt;select&gt; element.
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
    public void setUsingLongNames(boolean useLong) {
        _usingLongNames = useLong;
    }


    /**
     * The initial state chosen in the select. Null is the "all" selection,
     * if there is one; otherwise it will default to CA.
     *
     * @param state optional initial state
     */
    public void setState(State state) {
        _state = state;
    }

    public void doTag() throws IOException {

        JspWriter out = getJspContext().getOut();
        out.print("<select name=\"state\"");
        if (_styleClass != null) {
            out.print(" class=\"" + _styleClass + "\"");
        }
        if (_onChange != null) {
            out.print(" onchange=\"" + _onChange + "\"");
        }
        out.println(">");

        if (_allowNoState) {
            out.println("<option value=\"all\"");
            if (_state == null) {
                out.print(" selected='selected' ");
            }
            out.println(">State</option>");
        }

        Iterator iterator;
        if (_usingLongNames) {
            iterator= _stateManager.getIterator();
        } else {
            iterator = _stateManager.getListByAbbreviations().iterator();
        }
        while (iterator.hasNext()) {
            State state = (State) iterator.next();

            out.print("<option value=\"");
            out.print(state.getAbbreviation());
            out.print("\" ");

            if (ObjectUtils.equals(_state, state)) {
                out.print(" selected='selected' ");
            }
            out.print(">");

            if (_usingLongNames) {
                out.print(state.getLongName());
            } else {
                out.print(state.getAbbreviation());
            }
            out.println("</option>");
        }
        out.println("</select>");
    }
}
