/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AdTagHandler.java,v 1.10 2007/05/22 22:03:34 dlee Exp $
 */
package gs.web.ads;

import gs.web.jsp.AbstractDeferredContentTagHandler;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

/**
 * Write an ad
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class AdTagHandler extends AbstractDeferredContentTagHandler {

    private String _position;
    private AdPosition _adPosition;

    private static final Log _log = LogFactory.getLog(AdTagManager.class);
    private static final String JS_METHOD_NAME_24_7 = "OAS_AD";
    private static final String JS_METHOD_NAME_GAM = "GA_googleFillSlot";

    public static final String REQUEST_ATTRIBUTE_SLOT_PREFIX_NAME = "gamSlotPrefix";

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

        StringBuffer buffer = new StringBuffer();

        //TODO remove once we start serving ads in google ad server
        String style = (_adPosition.isGAMPosition()) ? " style=\"display:none;\"" : "";

        buffer.append("<div id=\"")
                .append(getAdId())
                .append("\" class=\"")
                .append(getAdId()).append(" ")
                .append("ad").append(" ")
                .append("noprint")
                .append("\"")
                .append(style)
                .append(">");

        if (pageHelper.isAdServedByCobrand()) {
            AdTagManager adManager = AdTagManager.getInstance();
            String customAdTag = adManager.getAdTag(sc.getCobrand(), _adPosition);
            buffer.append(customAdTag);
        } else {
            //make sure position has not been previously used on this page.
            Set <AdPosition> adPositions = pageHelper.getAdPositions();
            if (adPositions.contains(_adPosition)) {
                throw new IllegalArgumentException("Ad Position already defined: " + _adPosition);
            } else {
                pageHelper.addAdPosition(_adPosition);
            }

            String jsMethodName;
            String slotName = _adPosition.getName();

            if (_adPosition.isGAMPosition()) {
                jsMethodName = JS_METHOD_NAME_GAM;
                String slotPrefix = (String) request.getAttribute(REQUEST_ATTRIBUTE_SLOT_PREFIX_NAME);
                if (StringUtils.isNotBlank(slotPrefix)) {
                    slotName = slotPrefix + slotName;
                }
            } else {
                jsMethodName = JS_METHOD_NAME_24_7;
            }
            String adCode = "<script type=\"text/javascript\">"+ jsMethodName +"('" + slotName + "');</script>";
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

    /**
     * Google Ad manager tags are not deferred.
     * @return true is ad tag is deferred.  False otherwise.
     */
    public boolean isDeferred() {
        return !_adPosition.isGAMPosition();
    }
}
