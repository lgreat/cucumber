package gs.web.jsp.link;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import gs.web.community.registration.RegistrationFollowUpController;
import gs.web.state.StateSelectorTagHandler;
import gs.data.state.State;
import gs.data.community.Subscription;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class PreviousSchoolsListTagHandler extends SimpleTagSupport {
//    protected final Log _log = LogFactory.getLog(getClass());
//
//    private String _stateStyleClass = "state"; // default
//    private String _schoolStyleClass = "schoolname"; // default
//    private State _defaultState = State.CA;
//    private List _schoolValues;
//    private List _schoolNames;
//
//    public String getStateStyleClass() {
//        return _stateStyleClass;
//    }
//
//    public void setStateStyleClass(String stateStyleClass) {
//        _stateStyleClass = stateStyleClass;
//    }
//
//    public String getSchoolStyleClass() {
//        return _schoolStyleClass;
//    }
//
//    public void setSchoolStyleClass(String schoolStyleClass) {
//        _schoolStyleClass = schoolStyleClass;
//    }
//
//    public List getSchoolValues() {
//        if (_schoolValues == null) {
//            _schoolValues = new ArrayList();
//        }
//        return _schoolValues;
//    }
//
//    public void setSchoolValues(List schoolValues) {
//        _schoolValues = schoolValues;
//    }
//
//    public List getSchoolNames() {
//        if (_schoolNames == null) {
//            _schoolNames = new ArrayList();
//        }
//        return _schoolNames;
//    }
//
//    public void setSchoolNames(List schoolNames) {
//        _schoolNames = schoolNames;
//    }
//
//    public State getDefaultState() {
//        return _defaultState;
//    }
//
//    public void setDefaultState(State defaultState) {
//        _defaultState = defaultState;
//    }
//
//    public void doTag() throws IOException {
//        JspWriter out = getJspContext().getOut();
//
//        BindException errors = (BindException)
//                getJspContext().findAttribute("org.springframework.validation.BindException.followUpCmd");
//
//        out.print("<label class=\"");
//        out.print(_schoolStyleClass);
//        out.println("\">School</label>");
//        out.print("<label class=\"");
//        out.print(_stateStyleClass);
//        out.println("\">State</label>");
//        out.println("<br style=\"clear: left;\"/>");
//
//        for (int x=1; x <= RegistrationFollowUpController.NUMBER_PREVIOUS_SCHOOLS; x++) {
//            String schoolVal = "";
//            String schoolIdVal = "";
//            State stateVal = _defaultState;
//            if (getSchoolValues().size() >= x) {
//                Subscription sub = (Subscription) getSchoolValues().get(x-1);
//                schoolIdVal = String.valueOf(sub.getSchoolId());
//                stateVal = sub.getState();
//            }
//            if (getSchoolNames().size() >= x) {
//                schoolVal = String.valueOf(getSchoolNames().get(x-1));
//            }
//            if (errors != null) {
//                FieldError error = errors.getFieldError("previousSchoolNames[" + (x-1) + "]");
//                if (error != null) {
//                    out.print("<div class=\"error\">");
//                    out.print(error.getDefaultMessage());
//                    out.println("</div>");
//                }
//            }
//
//            out.print("<input class=\"");
//            out.print(_schoolStyleClass);
//            out.print("\" id=\"previousSchool" + x + "\" name=\"previousSchool" + x + "\" ");
//            out.println("type=\"text\" value=\"" + schoolVal + "\"/>");
//            out.print("<input type=\"hidden\" name=\"previousSchoolId" + x + "\" ");
//            out.print("id=\"previousSchoolId" + x + "\" ");
//            out.println("value=\"" + schoolIdVal + "\"/>");
//
//            StateSelectorTagHandler ss = new StateSelectorTagHandler();
//            ss.setJspContext(getJspContext());
//            ss.setStyleId("previousState" + x);
//            ss.setName("previousState" + x);
//            ss.setState(stateVal);
//            ss.doTag();
//
//            out.println("<br style=\"clear: left;\"/>");
//
//        }
//    }
}
