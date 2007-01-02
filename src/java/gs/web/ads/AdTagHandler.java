/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AdTagHandler.java,v 1.6 2007/01/02 20:09:17 cpickslay Exp $
 */
package gs.web.ads;

import gs.web.jsp.AbstractDeferredContentTagHandler;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Write an ad
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class AdTagHandler extends AbstractDeferredContentTagHandler {

    private String _position;
    private AdPosition _adPosition;
    private static final Log _log = LogFactory.getLog(AdTagManager.class);


    public String getId() {
        return _adPosition.getName();
    }

    private String getAdId() {
        return "ad" + getId();
    }

    public String getDeferredContent() throws IOException, JspException {
        SessionContext sc = (SessionContext) getJspContext().findAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);
        PageContext pageContext = (PageContext) getJspContext();
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);

        if (pageHelper.isAdFree()) {
            return ""; //early exit
        }

        String adPosition = _adPosition.getName();
        StringBuffer buffer = new StringBuffer();
        buffer.append("<div id=\"")
                .append(getAdId())
                .append("\" class=\"")
                .append(getAdId()).append(" ")
                .append("ad").append(" ")
                .append("noprint")
                .append("\">");

        if (pageHelper.isAdServedByCobrand()) {
            AdTagManager adManager = AdTagManager.getInstance();
            String customAdTag = adManager.getAdTag(sc.getCobrand(), _adPosition);
            buffer.append(customAdTag);
        } else {
            if (null != request.getAttribute(adPosition)) {
                throw new IllegalArgumentException("Ad Position already defined: " + adPosition);
            } else {
                request.setAttribute(adPosition, Boolean.TRUE);
            }

            String adCode = "<script type=\"text/javascript\">OAS_AD('" + _adPosition.getName() + "');</script>";
            JspFragment body = getJspBody();

            if (null != body) {
                StringWriter bodyWriter = new StringWriter();
                body.invoke(bodyWriter);
                StringBuffer adBuffer = bodyWriter.getBuffer();

                if (adBuffer.indexOf("$AD") == -1) {
                    throw new IllegalStateException("Missing variable $AD in body content.");
                } else {
                    buffer.append(adBuffer.toString().replaceAll("\\$AD", adCode));    
                }
            } else {
                buffer.append(adCode);
            }
        }

        buffer.append("</div>");

        return buffer.toString();
    }

    public String getPosition() {
        return _position;
    }

    public void setPosition(String position) {
        _position = position;
        _adPosition = AdPosition.getAdPosition(_position);
    }
}
