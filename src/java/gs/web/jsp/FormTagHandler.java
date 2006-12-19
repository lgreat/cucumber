package gs.web.jsp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Write out an XHTML form tag
 *
 * @author Todd Huss <mailto:thuss@greatschools.net>
 */
public class FormTagHandler extends TagSupport {

    /**
     * The url of the action.
     */
    private String _action;

    /**
     * The method, get or post (lowercase please).
     */
    private String _method;

    /**
     * Javascript to run on submit, if any.
     */
    private String _onsubmit;

    /**
     * The CSS id, if any.
     */
    private String _id;

    /**
     * The target window name, if any.
     */
    private String _target;

    /**
     * The CSS class, if any.
     */
    private String _styleClass;

    public int doStartTag() throws JspException {
        gs.web.util.UrlUtil urlUtil = new gs.web.util.UrlUtil();
        _action = urlUtil.buildUrl(_action, (HttpServletRequest) pageContext.getRequest());

        StringBuffer xhtml = new StringBuffer();
        xhtml.append("<form accept-charset=\"UTF-8\"");
        if (_id != null) xhtml.append(" id=\"").append(_id).append("\"");
        if (_action != null) xhtml.append(" action=\"").append(_action).append("\"");
        if (_method != null) xhtml.append(" method=\"").append(_method).append("\"");
        if (_styleClass != null) xhtml.append(" class=\"").append(_styleClass).append("\"");
        if (_onsubmit != null) xhtml.append(" onsubmit=\"").append(_onsubmit).append("\"");
        if (_target != null) xhtml.append(" target=\"").append(_target).append("\"");
        xhtml.append(">");
        try {
            pageContext.getOut().print(xhtml);
        } catch (IOException e) {
            throw new JspException(e);
        }
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().print("</form>");
        } catch (IOException e) {
            throw new JspException(e);
        }
        return EVAL_PAGE;
    }

    public void setAction(String action) {
        this._action = action;
    }

    public void setMethod(String method) {
        this._method = method;
    }

    public void setOnsubmit(String onsubmit) {
        this._onsubmit = onsubmit;
    }

    public void setId(String id) {
        this._id = id;
    }

    public void setTarget(String target) {
        this._target = target;
    }

    public void setStyleClass(String styleClass) {
        _styleClass = styleClass;
    }
}
