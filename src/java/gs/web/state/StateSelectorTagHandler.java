package gs.web.state;

import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.SpringUtil;
import org.apache.commons.lang.ObjectUtils;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * This tag handler produces a drop-down &lt;select&gt; element, where the
 * states are submitted using their uppercase abbreviation under the "state"
 * form property.
 * Defaults to using the fifty
 * state abbreviations in alphabetical order of the abbreviations, not
 * the long state name.  When usingLongNames == true, then the names are listed
 * in the alphabetical order of the long names.
 * The tag accepts an optional parameter to allow a non-state selection.  This
 * appears with the option name="state" value="--".
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class StateSelectorTagHandler extends SimpleTagSupport {

    private boolean _useNoState = false;
    private boolean _usingLongNames = false;
    private String _styleClass = null;
    private String _styleId = "stateSelector"; // default
    private String _onChange = null;
    private State _state;
    private String _noStateLabel = "--";
    private String _name = "state"; // default
    private boolean _multiple = false; // default
    private int _size = 0;
    private Set _stateSet;

    private static final StateManager _stateManager;

    static {
        _stateManager = (StateManager) SpringUtil.getApplicationContext().getBean(StateManager.BEAN_ID);
    }

    public Set getStateSet() {
        return _stateSet;
    }

    public void setStateSet(Set stateSet) {
        _stateSet = stateSet;
    }

    public boolean isMultiple() {
        return _multiple;
    }

    /**
     * Sets this select to be a multiple-select
     */
    public void setMultiple(boolean multiple) {
        _multiple = multiple;
    }

    public int getSize() {
        return _size;
    }

    /**
     * When isMultiple() is true, this provides a hint to the browser about how many items to
     * display in the select box before needing scrolling
     * @param size number of rows
     */
    public void setSize(int size) {
        _size = size;
    }

    /**
     * When set to true, the selector will show "--" no matter what the
     * state value is.  When set to false, then the "--" is not shown and
     * the option selection is set to the current state.
     *
     * @param noState - defaults to false;
     */
    public void setUseNoState(boolean noState) {
        _useNoState = noState;
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
     * This option allows you to set the name of the state dropdown
     * @param name - name for the tag
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Sets the value to be used as the id for this element.  Defaults to
     * "stateSelector".
     * @param styleId a String id.
     */
    public void setStyleId(String styleId) {
        if (styleId != null) {
            _styleId = styleId;
        }
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
     * The label for the "no state" option. Ignored if {@link #setUseNoState(boolean)} not
     * called.
     */
    public void setNoStateLabel(String noStateLabel) {
        _noStateLabel = noStateLabel;
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
        out.print("<select id=\"");
        out.print(_styleId);
        out.print("\" name=\"" + _name + "\"");
        if (_styleClass != null) {
            out.print(" class=\"" + _styleClass + "\"");
        }
        if (_onChange != null) {
            out.print(" onchange=\"" + _onChange + "\"");
        }
        if (_multiple) {
            out.print(" multiple=\"multiple\"");
            if (_size > 0) {
                out.print(" size=\"" + _size + "\"");
            }
        }
        out.println(">");

        if (_useNoState) {
            out.print("<option value=\"\"");
            if (!_multiple || _stateSet == null) {
                out.print(" selected='selected' ");
            }
            out.println(">" + _noStateLabel +"</option>");
        }

        Iterator iterator;
        if (_usingLongNames) {
            iterator = _stateManager.getIterator();
        } else {
            iterator = _stateManager.getListByAbbreviations().iterator();
        }
        while (iterator.hasNext()) {
            State state = (State) iterator.next();

            out.print("<option value=\"");
            out.print(state.getAbbreviation());
            out.print("\" ");

            if (ObjectUtils.equals(_state, state) && !_useNoState && !_multiple) {
                out.print(" selected='selected' ");
            } else if (_multiple && _stateSet != null) {
                if (_stateSet.contains(state)) {
                    out.print(" selected='selected' ");
                }
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
