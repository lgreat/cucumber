package gs.web.jsp.link;

import gs.data.school.Grade;
import gs.data.school.Grades;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

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
    private boolean _useGradeName = false;
    private boolean _usePreK = false;
    private String _gradeName = null;
    private String _noGradeLabel = "--"; // default
    private String _name = "grade"; // default
    private boolean _useAlternateNames = false;
    private boolean _useUngraded = false;
    private String _ungradedLabel = "ungraded"; // default

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

    /**
     * The label for the ungraded option. Ignored if {@link #setUseUngraded(boolean)} not called.
     * @param ungradedLabel
     */
    public void setUngradedLabel(String ungradedLabel) {
        _ungradedLabel = ungradedLabel;
    }

    public boolean isUseGradeName() {
        return _useGradeName;
    }

    public void setUseGradeName(boolean useGradeName) {
        _useGradeName = useGradeName;
    }

    public String getGradeName() {
        return _gradeName;
    }

    public void setGradeName(String gradeName) {
        _gradeName = gradeName;
    }

    public boolean isUsePreK() {
        return _usePreK;
    }

    public void setUsePreK(boolean usePreK) {
        _usePreK = usePreK;
    }

    /**
     * When set to true, the selector will show "ungraded" as the last option.
     * When set to false, then the "ungraded" is not shown.
     *
     * @param useUngraded - defaults to false;
     */
    public void setUseUngraded(boolean useUngraded) {
        _useUngraded = useUngraded;
    }



    public void doTag() throws IOException {

        if (_useGradeName && !StringUtils.isBlank(_gradeName)) {
            _grade = Grade.getGradeLevel(_gradeName);
        }

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

        Grade lowestGrade = Grade.KINDERGARTEN;
        if (_usePreK) {
            lowestGrade = Grade.PRESCHOOL;
        }

        Grades grades = Grades.createGrades(lowestGrade, Grade.G_12); // limit to K-12?
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

        if (_useUngraded) {
            out.print("<option value=\"");
            out.print(Grade.UNGRADED.getName());
            out.print("\"");

            if (ObjectUtils.equals(_grade, Grade.UNGRADED)) {
                out.print(" selected=\"selected\"");
            }
            out.print(">");

            if (!StringUtils.isBlank(_ungradedLabel)) {
                out.print(_ungradedLabel);
            } else {
                out.print(Grade.UNGRADED.getName());
            }
            out.println("</option>");
        }


        out.println("</select>");
    }
}
