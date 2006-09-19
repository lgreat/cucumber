/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AdTagHandler.java,v 1.1 2006/09/19 23:31:10 dlee Exp $
 */
package gs.web.ads;

import gs.web.util.PageHelper;
import gs.web.util.context.ISessionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * Write an ad
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class AdTagHandler extends SimpleTagSupport {

    private String _position;
    private AdPosition _adPosition;

    public void doTag() throws IOException {
        ISessionContext sc = (ISessionContext) getJspContext().findAttribute(ISessionContext.REQUEST_ATTRIBUTE_NAME);
        PageContext pageContext = (PageContext) getJspContext();
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        PageHelper pageHelper = new PageHelper(sc, request);
        StringBuffer buffer = new StringBuffer();

        if (!pageHelper.isAdFree()) {
            String adPosition = _adPosition.getName();

            if (pageHelper.isAdFree()) {
                AdTagManager adManager = AdTagManager.getInstance();
                String customAdTag = adManager.getAdTag(sc.getCobrand(), _adPosition);
                buffer.append(customAdTag);
            } else {
                if (null != request.getAttribute(adPosition)) {
                    throw new IllegalArgumentException("Ad Position already defined: " + adPosition);
                } else {
                    request.setAttribute(adPosition, Boolean.TRUE);
                }

                buffer.append("<script type=\"text/javascript\">OAS_AD('")
                        .append(_adPosition.getName())
                        .append("');</script>");
            }
        }
        getJspContext().getOut().print(buffer.toString());
    }

    public String getPosition() {
        return _position;
    }

    public void setPosition(String position) {
        _position = position;
        _adPosition = AdPosition.getAdPosition(_position);
    }
}
