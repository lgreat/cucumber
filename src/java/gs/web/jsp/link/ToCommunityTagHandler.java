/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.jsp.link;

import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.ISessionContext;
import gs.web.util.UrlBuilder;
import gs.web.community.registration.AuthenticationManager;
import gs.web.jsp.BaseTagHandler;
import gs.data.community.User;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;

/**
 * Provides support for linking to arbitrary URLs on the community vendor site. This gets called
 * with the targetLink set, and a URL is constructed that goes to the community vendor site,
 * contains authentication information for the current user, and instructs the vendor site
 * to redirect the user to the relative URL targetLink on their site after authentication.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ToCommunityTagHandler extends BaseTagHandler {
    private static AuthenticationManager _authenticationManager;
    private String _targetLink;
    private String _styleClass;
    private String _target;

    public void doTag() throws IOException {
        JspWriter out = getJspContext().getOut();
        out.print("<a");

        if (!StringUtils.isEmpty(_styleClass)) {
            out.print(" class=\"" + _styleClass + "\"");
        }

        if (!StringUtils.isEmpty(_target)) {
            out.print(" target=\"" + _target + "\"");
        }

        PageContext pageContext = (PageContext) getJspContext().findAttribute(PageContext.PAGECONTEXT);
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        User user = SessionContextUtil.getSessionContext(request).getUser();
        String href = "";
        if (user != null) {
            // send user to community vendor site
            try {
                href = constructUrl(_targetLink, user, getAuthenticationManager());
            } catch (NoSuchAlgorithmException e) {
                _log.error(e);
            }
        } else {
            // what? No user?? Then send them to login with a redirect to the community vendor site
            UrlBuilder builder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, _targetLink);
            href = builder.asFullUrl(request);
        }

        out.print(" href=\"");
        out.print(href);
        out.print("\"");
        out.print(">");

        JspFragment jspBody = getJspBody();
        if (jspBody != null && StringUtils.isNotEmpty(jspBody.toString())) {
            try {
                jspBody.invoke(out);
            } catch (JspException e) {
                _log.error(e);
            }
        }

        out.print("</a>");
    }

    protected AuthenticationManager getAuthenticationManager() {
        if (_authenticationManager == null) {
            ISessionContext sc = getSessionContext();
            ApplicationContext applicationContext = sc.getApplicationContext();
            _authenticationManager = (AuthenticationManager) applicationContext.getBean(AuthenticationManager.BEAN_ID);
        }
       return _authenticationManager;
    }

    // For use by test classes
    protected void setAuthenticationManager(AuthenticationManager authenticationManager) {
        _authenticationManager = authenticationManager;
    }

    /**
     * Constructs a URL to the community vendor site that provides authentication for the given
     * user and redirects them to targetLink.
     * @param targetLink ultimate destination on the community vendor site
     * @param user current user
     * @param authenticationManager this bean is required to generate the authentication information
     * @return a fully qualified URL to the community vendor site
     * @throws NoSuchAlgorithmException
     */
    public static String constructUrl(String targetLink, User user, AuthenticationManager authenticationManager) throws NoSuchAlgorithmException {
        AuthenticationManager.AuthInfo authInfo = authenticationManager.generateAuthInfo(user);
        String hashParam = authenticationManager.getParameterValue(authInfo);
        return AuthenticationManager.WEBCROSSING_FORWARD_URL + hashParam + targetLink;
    }

    public String getTargetLink() {
        return _targetLink;
    }

    public void setTargetLink(String targetLink) {
        _targetLink = targetLink;
    }

    public String getStyleClass() {
        return _styleClass;
    }

    public void setStyleClass(String styleClass) {
        _styleClass = styleClass;
    }

    public String getTarget() {
        return _target;
    }

    public void setTarget(String target) {
        _target = target;
    }
}