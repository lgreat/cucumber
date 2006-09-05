package gs.web.jsp.link;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.*;

import gs.data.community.UserProfile;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class InterestsListTagHandler extends SimpleTagSupport {
    protected final Log _log = LogFactory.getLog(getClass());

    private String _styleClass = "interests"; // default
    private List _interestChoices;

    public String getStyleClass() {
        return _styleClass;
    }

    public void setStyleClass(String styleClass) {
        _styleClass = styleClass;
    }

    public List getInterestChoices() {
        if (_interestChoices == null) {
            _interestChoices = new ArrayList();
        }
        return _interestChoices;
    }

    public void setInterestChoices(List interestChoices) {
        _interestChoices = interestChoices;
    }

    public void doTag() throws IOException {
        JspWriter out = getJspContext().getOut();
        Iterator keys = UserProfile.getInterestsMap().keySet().iterator();

        while (keys.hasNext()) {
            String code = String.valueOf(keys.next());
            String value = String.valueOf(UserProfile.getInterestsMap().get(code));

            out.print("<input class=\"");
            out.print(getStyleClass());
            out.print("\" id=\"");
            out.print(code);
            out.print("\" name=\"");
            out.print(code);
            out.print("\" type=\"checkbox\"");
            if (getInterestChoices().contains(code)) {
                out.print(" checked=\"checked\"");
            }
            out.println("/>");
            out.print("<label for=\"");
            out.print(code);
            out.print("\" class=\"");
            out.print(getStyleClass());
            out.print("\">");
            out.print(value);
            out.println("</label>");
            out.println("<br style=\"clear: left;\"/>");
        }
    }
}
