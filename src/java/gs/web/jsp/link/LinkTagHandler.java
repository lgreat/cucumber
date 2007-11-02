/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: LinkTagHandler.java,v 1.14 2007/11/02 23:51:10 dlee Exp $
 */

package gs.web.jsp.link;

import gs.data.state.State;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Support for all GreatSchools link tags.
 * Provides support for:
 * <ol>
 * <li>a tag generation
 * <li>CSS class attribute generation
 * <li>default link text (TBD)
 * <li>var variable specification (TBD)
 * </ol>
 * Subclasses are responsible for generating the correct URL using the UrlBuilder.
 * They must implement {link #createUrlBuilder()}.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 * @see UrlBuilder
 */
public abstract class LinkTagHandler extends TagSupport {
    protected final Logger _log = Logger.getLogger(this.getClass());
    private String _styleClass;
    private String _target;
    private String _anchor;
    private String _title;
    private String _styleId;
    private String _onMouseOver;
    private String _onMouseOut;
    private String _onclick;
    private boolean _absolute = false;

    /**
     * Create a UrlBuilder object pointing to the correct page.
     */
    protected abstract UrlBuilder createUrlBuilder();

    /**
     * Text to be used if the caller doesn't explicitly include text
     * in the body of the tag.
     * This gives us a convenient way to have standard link text for
     * the page and change it centrally.
     */
    protected String getDefaultLinkText() {
        return null;
    }

    public int doStartTag() throws JspException {
        try {
            UrlBuilder builder = createUrlBuilder();
            printATagStart(builder);
        } catch (IOException e) {
            throw new JspException(e);
        }

        return EVAL_BODY_INCLUDE; //EVAL_BODY_BUFFERED;
    }

    public int doAfterBody() throws JspException {

/*
        try {
            pageContext.getOut().print("DO AFTER BODY");
        } catch (IOException e) {
            throw new JspException(e);
        }

        // If there was nothing in the body, then write out the default link
        String s = bodyContent.getString();
        if (StringUtils.isEmpty(s)) {

            try {
                pageContext.getOut().print("NO BODY CONTENT");
            } catch (IOException e) {
                throw new JspException(e);
            }

            String d = getDefaultLinkText();
            if (StringUtils.isNotEmpty(d)) {
                try {
                    pageContext.getOut().print(d);
                } catch (IOException e) {
                    throw new JspException(e);
                }
            }
        }
*/


        return super.doAfterBody();

    }

    public int doEndTag() throws JspException {

        try {
            pageContext.getOut().print("</a>");
        } catch (IOException e) {
            throw new JspException(e);
        }
        return super.doEndTag();
    }

    protected void printATagStart(UrlBuilder builder) throws IOException {
        pageContext.getOut().print("<a");

        if (StringUtils.isNotEmpty(_styleId)) {
            pageContext.getOut().print(" id=\"");
            pageContext.getOut().print(_styleId);
            pageContext.getOut().print("\"");
        }

        if (StringUtils.isNotEmpty(_styleClass)) {
            pageContext.getOut().print(" class=\"");            
            pageContext.getOut().print(_styleClass);
            pageContext.getOut().print("\"");
        }

        if (StringUtils.isNotEmpty(_target)) {
            pageContext.getOut().print(" target=\"");
            pageContext.getOut().print(_target);
            pageContext.getOut().print("\"");
        }

        if (StringUtils.isNotEmpty(_title)) {
            pageContext.getOut().print(" title=\"");
            pageContext.getOut().print(_title);
            pageContext.getOut().print("\"");
        }

        if (StringUtils.isNotEmpty(_onMouseOver)) {
            pageContext.getOut().print(" onmouseover=\"");
            pageContext.getOut().print(_onMouseOver);
            pageContext.getOut().print("\"");
        }

        if (StringUtils.isNotEmpty(_onMouseOut)) {
            pageContext.getOut().print(" onmouseout=\"");
            pageContext.getOut().print(_onMouseOut);
            pageContext.getOut().print("\"");
        }

        if (StringUtils.isNotEmpty(_onclick)) {
            pageContext.getOut().print(" onclick=\"");
            pageContext.getOut().print(_onclick);
            pageContext.getOut().print("\"");            
        }

        String href;
        if (isAbsolute()) {
            href = builder.asFullUrlXml((HttpServletRequest) pageContext.getRequest());
        } else {
            href = builder.asSiteRelativeXml((HttpServletRequest) pageContext.getRequest());
        }

        if (StringUtils.isNotEmpty(_anchor)) {
            //only set anchor if one is not found in url
            if (!StringUtils.contains(href, '#')) {
                _anchor = StringUtils.remove(_anchor, '#');
                href = href + '#' + _anchor;
            }
        }

        if (StringUtils.isNotEmpty(getId())) {
            pageContext.getOut().print(" id=\"");
            pageContext.getOut().print(getId());
            pageContext.getOut().print("\"");
        }

        pageContext.getOut().print(" href=\"");
        pageContext.getOut().print(href);
        pageContext.getOut().print("\"");
        pageContext.getOut().print(">");
    }

    protected SessionContext getSessionContext() {
        HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
        SessionContext sc = null;
        if (request != null) {
            sc = SessionContextUtil.getSessionContext(request);
        }
        return sc;
    }

    /**
     * @return The current <code>State</code> based on knowledge of location
     *         awareness in the <code>SessionConetext</code> object, or CA if there
     *         is no current location awareness.
     */
    protected State getState() {
        State state = State.CA;
        SessionContext sc = getSessionContext();
        if (sc != null) {
            state = sc.getStateOrDefault();
        }
        return state;
    }


    public String getStyleClass() {
        return _styleClass;
    }

    public void setStyleClass(String styleClass) {
        _styleClass = styleClass;
    }

    public void setTarget(String target) {
        _target = target;
    }

    /**
     * The name of the anchor on destination page.
     * Some example anchors:
     * somelink.html#anchor
     * somelink.html?a=1&b=2#anchor
     *
     * If any anchor is already specified in the href, this anchor is ignored.
     *
     * @param anchor
     */
    public void setAnchor(String anchor) {
        _anchor = anchor;
    }

    public void setTitle(String title) {
        _title = title;
    }

    public String getStyleId() {
        return _styleId;
    }

    public void setStyleId(String styleId) {
        _styleId = styleId;
    }

    public String getOnMouseOver() {
        return _onMouseOver;
    }

    public void setOnMouseOver(String onMouseOver) {
        _onMouseOver = onMouseOver;
    }

    public String getOnMouseOut() {
        return _onMouseOut;
    }

    public void setOnMouseOut(String onMouseOut) {
        _onMouseOut = onMouseOut;
    }

    public boolean isAbsolute() {
        return _absolute;
    }

    public void setAbsolute(boolean absolute) {
        _absolute = absolute;
    }

    public String getOnclick() {
        return _onclick;
    }

    public void setOnclick(String onclick) {
        _onclick = onclick;
    }
}
