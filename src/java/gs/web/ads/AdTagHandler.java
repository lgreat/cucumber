/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: AdTagHandler.java,v 1.53 2012/07/12 16:31:53 yfan Exp $
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
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public class AdTagHandler extends AbstractDeferredContentTagHandler {

    private String _position;
    private AdPosition _adPosition;
    private boolean _alwaysShow = false;
    private boolean _showOnPrintView = false;

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

    public String getContent(HttpServletRequest request, SessionContext sc, JspFragment body) throws JspException, IOException {
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);

        if (pageHelper.isAdContentFree() || (!_alwaysShow && pageHelper.isAdFree())) {
            return ""; //early exit
        }

        StringBuffer buffer = new StringBuffer();

        buffer.append("<div id=\"")
                .append(getAdId())
                .append("\" class=\"")
                .append(getAdId()).append(" ")
                .append("ad");
        if (!isShowOnPrintView()) {
            buffer.append(" noprint");
        }
        buffer.append("\"")
                .append(">");

        if (!isAlwaysShow() && pageHelper.isAdServedByCobrand()) {
            AdTagManager adManager = AdTagManager.getInstance();
            String customAdTag = adManager.getAdTag(sc.getCobrand(), _adPosition);
            if (StringUtils.isEmpty(customAdTag)) {
                return "";
            } else {
                buffer.append(customAdTag);
            }
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
                if (!"Global_NavPromo_970x30".equals(slotName) &&
                    !"Custom_Peelback_Ad".equals(slotName) &&
                    !"Custom_Welcome_Ad".equals(slotName) &&
                    StringUtils.isNotBlank(slotPrefix)) {
                    slotName = slotPrefix + slotName ;
                }
            } else {
                jsMethodName = JS_METHOD_NAME_24_7;
            }
            String adCode = "<script type=\"text/javascript\">"+ jsMethodName +"('" + slotName + "');</script>";

            // TODO - currently only works from a JSP; if needed, refactor to allow extracting for non-JSP situations
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

    public String getGptContent(HttpServletRequest request, SessionContext sc, JspFragment body) throws JspException, IOException {
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);

        // exit early if mobile ad and mobile ads are offline
        if (isMobileAdAndOffline(sc)){
            return "";
        }
        // exit early if not mobile and page is ad free
        else if (!isMobileAd() && (pageHelper.isAdContentFree() || (!_alwaysShow && pageHelper.isAdFree()))){
            return "";
        }

        StringBuffer buffer = new StringBuffer();

        buffer.append("<div id=\"")
                .append(getAdId())
                .append("\" class=\"")
                .append(getAdId()).append(" ")
                .append("ad");
        if (!isShowOnPrintView()) {
            buffer.append(" noprint");
        }
        buffer.append("\"")
                .append(">");

        if (!isAlwaysShow() && pageHelper.isAdServedByCobrand()) {
            AdTagManager adManager = AdTagManager.getInstance();
            String customAdTag = adManager.getAdTag(sc.getCobrand(), _adPosition);
            if (StringUtils.isEmpty(customAdTag)) {
                return "";
            } else {
                buffer.append(customAdTag);
            }
        } else {
            //make sure position has not been previously used on this page.
            // GPT, unlike GAM tags, allows the same ad slot to be used multiple times on the same page,
            // but let's keep the same logic for backwards compatibility
            Set <AdPosition> adPositions = pageHelper.getAdPositions();
            if (adPositions.contains(_adPosition)) {
                throw new IllegalArgumentException("Ad Position already defined: " + _adPosition);
            } else {
                pageHelper.addAdPosition(_adPosition);
            }

            String slotName = _adPosition.getName();

            if (_adPosition.isGAMPosition()) {
                String slotPrefix = (String) request.getAttribute(REQUEST_ATTRIBUTE_SLOT_PREFIX_NAME);
                if (!"Global_NavPromo_970x30".equals(slotName) &&
                        !"Custom_Peelback_Ad".equals(slotName) &&
                        !"Custom_Welcome_Ad".equals(slotName) &&
                        StringUtils.isNotBlank(slotPrefix)) {
                    slotName = slotPrefix + slotName ;
                }
            }
            StringBuilder adCodeBuffer = new StringBuilder();

            boolean disabledGptGhostTextHiding = pageHelper.getAdPositionsWithDisabledGptGhostTextHiding().contains(_adPosition);

            if (disabledGptGhostTextHiding) {
                adCodeBuffer.append("<div id=\"gpt").append(getId()).append("\">");;
            }
            adCodeBuffer.append("<script type=\"text/javascript\">");

            if (isAsyncMode(sc)) {
                adCodeBuffer.append("googletag.cmd.push(function() {");
            }

            String divContainingAd = (disabledGptGhostTextHiding ? "gpt" + getId() : getAdId());

            adCodeBuffer.append("googletag.display(\"").append(divContainingAd).append("\");");
            if (isAsyncMode(sc)) {
                adCodeBuffer.append("});");
            }
            adCodeBuffer.append("</script>");
            if (disabledGptGhostTextHiding) {
                adCodeBuffer.append("</div>");
            }

            // TODO - currently only works from a JSP; if needed, refactor to allow extracting for non-JSP situations
            if (null != body) {
                StringWriter bodyWriter = new StringWriter();
                body.invoke(bodyWriter);
                StringBuffer adBuffer = bodyWriter.getBuffer();

                if (adBuffer.indexOf("$AD") == -1) {
                    throw new IllegalStateException("Missing variable $AD in body content.");
                } else {
                    buffer.append(adBuffer.toString().replaceAll("\\$AD", adCodeBuffer.toString()));
                }
            } else {
                buffer.append(adCodeBuffer.toString());
            }
        }
        buffer.append("</div>");
        return buffer.toString();
    }

    public String getContent() throws IOException, JspException {
        SessionContext sc = (SessionContext) getJspContext().findAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);
        PageContext pageContext = (PageContext) getJspContext();
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        // if this is a mobile ad, we will always
        // want to display GPT tag regardless of
        // if GPT is enabled for the desktop
        if ( isMobileAd() ){
            return getGptContent(request, sc, getJspBody());
        }
        // if GPT is enabled on the desktop,
        // show the GPT tag
        else if (sc != null && sc.isGptEnabled()) {
            return getGptContent(request, sc, getJspBody());
        }
        // fallback to old tags
        else {
            return getContent(request, sc, getJspBody());
        }
    }

    public boolean isMobileAd(){
        return (_adPosition!=null && _adPosition.isMobileSpecific());
    }

    /**
     * Return true if the ad position is mobile
     * but advertising for mobile devices is offline
     * @param sc
     * @return
     */
    public boolean isMobileAdAndOffline(SessionContext sc){
        return (isMobileAd() && sc != null && !sc.isAdvertisingOnMobileOnline());
    }

    /**
     * Return true if mobile device and mobile in async mode
     * or not mobile device and gpt is enabled and gpt is in async mode
     * @param sc
     * @return
     */
    public boolean isAsyncMode(SessionContext sc) {
        if (isMobileAd() && sc!=null && sc.isGptAsynchronousModeOnMobileEnabled()){
            return true;
        }
        else if (!isMobileAd() && sc!=null && sc.isGptEnabled() && sc.isGptAsynchronousModeEnabled()){
            return true;
        }
        return false;
    }

    public String getPosition() {
        return _position;
    }

    public void setPosition(String position) {
        _position = position;
        _adPosition = AdPosition.getAdPosition(_position);
    }

    public boolean isAlwaysShow() {
        return _alwaysShow;
    }

    public void setAlwaysShow(boolean alwaysShow) {
        this._alwaysShow = alwaysShow;
    }

    public boolean isShowOnPrintView() {
        return _showOnPrintView;
    }

    public void setShowOnPrintView(boolean showOnPrintView) {
        _showOnPrintView = showOnPrintView;
    }
}