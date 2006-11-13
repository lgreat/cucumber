package gs.web.jsp.link;

import gs.data.school.Grade;
import gs.data.school.Grades;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang.ObjectUtils;

/**
 * This tag handler produces a drop-down &lt;select&gt; element for a child's grade
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GradeSelectorTagHandler extends SimpleTagSupport {

    private boolean _useNoGrade = false;
    private String _styleClass = null;
    private String _styleId = "gradeSelector"; // default
    private String _onChange = null;
    private Grade _grade;
    private String _noGradeLabel = "--"; // default
    private String _name = "grade"; // default
    private boolean _useAlternateNames = false;

    public void setUseAlternateNames(boolean useAlternateNames) {
        _useAlternateNames = useAlternateNames;
    }

    /**
     * When set to true, the selector will show "--" as the default option.
     * When set to false, then the "--" is not shown and
     * the option selection is set to the current grade.
     *
     * @param noGrade - defaults to false;
     */
    public void setUseNoGrade(boolean noGrade) {
        _useNoGrade = noGrade;
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
     * The label for the "no grade" option. Ignored if {@link #setUseNoGrade(boolean)} not
     * called.
     */
    public void setNoGradeLabel(String noGradeLabel) {
        _noGradeLabel = noGradeLabel;
    }

    /**
     * The initial grade chosen in the select. Null is the "all" selection,
     * if there is one.
     *
     * @param grade optional initial grade
     */
    public void setGrade(Grade grade) {
        _grade = grade;
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
        out.println(">");

        if (_useNoGrade) {
            out.print("<option value=\"\"");
            out.println(">" + _noGradeLabel +"</option>");
        }

        Grades grades = Grades.createGrades(Grade.KINDERGARTEN, Grade.G_12); // limit to K-12?
        for (Iterator iter = Grade.iterator(); iter.hasNext();) {
            Grade grade = (Grade) iter.next();
            if (grades.contains(grade)) {
                out.print("<option value=\"");
                out.print(grade.getName());
                out.print("\"");

                if (ObjectUtils.equals(_grade, grade)) {
                    out.print(" selected=\"selected\"");
                }
                out.print(">");

                if (_useAlternateNames && grade == Grade.KINDERGARTEN) {
                    out.print("K");
                } else {
                    out.print(grade.getName());
                }
                out.println("</option>");
            }
        }
        out.println("</select>");
    }
}
