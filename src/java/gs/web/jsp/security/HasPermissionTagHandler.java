package gs.web.jsp.security;

import gs.web.util.context.SessionContextUtil;
import gs.web.util.PageHelper;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.JspTagException;
import javax.servlet.http.HttpServletRequest;

// http://www.java2s.com/Code/Java/JSP/Createyourowntagacustomtagbody.htm
public class HasPermissionTagHandler extends BodyTagSupport {

    private String _key;

    @Override
    public int doStartTag() throws JspTagException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        if (PageHelper.isMemberAuthorized(request) && SessionContextUtil.getSessionContext(request).getUser().hasPermission(_key)) {
            return BodyTagSupport.EVAL_BODY_INCLUDE;
        } else {
            return BodyTagSupport.SKIP_BODY;
        }
    }

    public String getKey() {
        return _key;
    }

    public void setKey(String key) {
        _key = key;
    }
}
