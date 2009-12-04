package gs.web.jsp;

import gs.data.school.Grade;
import gs.data.school.Grades;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This tag handler produces a drop-down &lt;select&gt; element
 *
 * @author greatschools.org>
 */
public abstract class SelectorTagHandler extends SimpleTagSupport {

    private boolean _useNoResponse = false;
    private String _styleClass = null;
    private String _styleId = null;
    private String _onChange = null;
    private String _noResponseLabel = "--"; // default
    private String _name = null;
    private String _selectedValue = null;
    private String _tabIndex = null;

    /**
     * When set to true, the selector will show "--" as the default option.
     * When set to false, then the "--" is not shown and
     * the option selection is set to the current grade.
     *
     * @param noResponse - defaults to false;
     */
    public void setUseNoResponse(boolean noResponse) {
        _useNoResponse = noResponse;
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
     * Sets the value to be used as the id for this element.  Defaults to null
     * @param styleId a String id.
     */
    public void setStyleId(String styleId) {
        if (styleId != null) {
            _styleId = styleId;
        }
    }

    /**
     * This option allows you to set an onChange handler on the dropdown
     *
     * @param onChange - the onChange text to put on the web page
     */
    public void setOnChange(String onChange) {
        _onChange = onChange;
    }


    /**
     * The label for the "no response" option. Ignored if {@link #setUseNoResponse(boolean)} not
     * called.
     */
    public void setNoResponseLabel(String noResponseLabel) {
        _noResponseLabel = noResponseLabel;
    }

    /**
     * The selected option value. Defaults to null.
     * @param selectedValue
     */
    public void setSelectedValue(String selectedValue) {
        _selectedValue = selectedValue;
    }

    /**
     * Returns the selected option value. Defaults to null.
     * @return
     */
    public String getSelectedValue() {
        return _selectedValue;
    }

    public String getTabIndex() {
        return _tabIndex;
    }

    public void setTabIndex(String tabIndex) {
        _tabIndex = tabIndex;
    }

    public void doTag() throws IOException {

        JspWriter out = getJspContext().getOut();
        out.print("<select ");
        if (_styleId != null) {
            out.print(" id=\"" + _styleId + "\"");
        }
        if (_name != null) {
            out.print(" name=\"" + _name + "\"");
        }
        if (_styleClass != null) {
            out.print(" class=\"" + _styleClass + "\"");
        }
        if (_onChange != null) {
            out.print(" onchange=\"" + _onChange + "\"");
        }
        if (_tabIndex != null) {
            out.print(" tabindex=\"" + _tabIndex + "\"");
        }
        out.println(">");

        if (_useNoResponse) {
            out.print("<option value=\"\"");
            out.println(">" + _noResponseLabel +"</option>");
        }

        String[] values = getOptionValues();
        String[] displayNames = getOptionDisplayNames();

        if (values == null || displayNames == null || values.length != displayNames.length) {
            // TODO-6868 FIXME
        }

        for (int i = 0; i < values.length; i++) {
            out.print("<option value=\"");
            out.print(values[i]);
            out.print("\"");

            if (StringUtils.equals(values[i], getSelectedValue())) {
                out.print(" selected=\"selected\"");
            }

            out.print(">");
            out.print(displayNames[i]);
            out.print("</option>");
        }

        out.println("</select>");
    }

    public abstract String[] getOptionValues();
    public abstract String[] getOptionDisplayNames();
}
