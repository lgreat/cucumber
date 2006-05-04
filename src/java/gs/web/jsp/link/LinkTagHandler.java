/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: LinkTagHandler.java,v 1.3 2006/05/04 07:13:57 apeterson Exp $
 */

package gs.web.jsp.link;

import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionFacade;
import gs.web.util.UrlBuilder;
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

        pageContext.getOut().print(" href=\"");
        pageContext.getOut().print(builder.asSiteRelativeXml((HttpServletRequest) pageContext.getRequest()));
        pageContext.getOut().print("\"");

        pageContext.getOut().print(">");
    }

    protected ISessionFacade getSessionContext() {
        HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
        ISessionFacade sc = null;
        if (request != null) {
            sc = SessionFacade.getInstance(request);
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
        ISessionFacade sc = getSessionContext();
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

    public String getTarget() {
        return _target;
    }

    public void setTarget(String target) {
        _target = target;
    }
}
