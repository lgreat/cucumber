/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PageHelper.java,v 1.3 2006/01/10 18:26:07 apeterson Exp $
 */

package gs.web.util;

import gs.web.ISessionFacade;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper class to render and decorate a JSP page correctly.
 * Provides a place to encapsulate logic about our pages to answer
 * fine-grained questions about what shows and doesn't show in a testable manner.
 * <ul>
 * <li>Replaces somewhat haphazard nature of sitemesh's meta tags.
 * <li>Replaces untyped nature of page scoped attributes.
 * <li>Provides a place to make business rule and policy decisions for JSP pages.
 * </ul>
 * <p>There are basically two usages of this. One, is on actual JSP pages (or their
 * controllers or moduels). Here, the static functions are available to set
 * information about the page.
 * <p>The other usage is the rendering code, mostly in the sitemesh decorator.
 * Those should access the current object under the request scoped "pageHelper"
 * attribute.
 * <p>There is also code in the page interceptor responsible for creating this object
 * and setting its initial values.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class PageHelper {

    public static void hideHeader(HttpServletRequest request) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.setShowingHeader(false);
        } else {
            _log.error("No PageHelper object available.");
        }
    }

    public static void hideFooter(HttpServletRequest request) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.setShowingFooter(false);
        } else {
            _log.error("No PageHelper object available.");
        }
    }

    /**
     * Adds the given code to the onload script of the body tag.
     */
    public static void addOnLoadHandler(HttpServletRequest request, String javascript) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.addOnLoadHandler(javascript);
        } else {
            _log.error("No PageHelper object available.");
        }
    }


    public static final String REQUEST_ATTRIBUTE_NAME = "pageHelper";

    private boolean _showingHeader = true;
    private boolean _showingFooter = true;
    private final String _cobrand;
    private final boolean _subscriptionState;

    private static final Log _log = LogFactory.getLog(PageHelper.class);
    private String _onload = "";

    private static PageHelper getInstance(HttpServletRequest request) {
        return (PageHelper) request.getAttribute(REQUEST_ATTRIBUTE_NAME);
    }

    public PageHelper(ISessionFacade sessionFacade) {
        _cobrand = sessionFacade.getCobrand();
        _subscriptionState = sessionFacade.getStateOrDefault().isSubscriptionState();
    }

    public boolean isShowingBannerAd() {
        return StringUtils.isEmpty(_cobrand) ||
                "charterschoolratings".equals(_cobrand);
    }

    public boolean isShowingLogo() {
        return _showingHeader && !isAdFree();
    }

    public boolean isShowingUserInfo() {
        return _showingHeader && !isAdFree();
    }


    /**
     * Is all the stuff in the footer (SE links, About us links, copyright)
     * shown?
     *
     * @todo break this into smaller pieces?
     */
    public boolean isShowingFooter() {
        return _showingFooter && !isAdFree() && !isYahooCobrand();
    }

    private boolean isYahooCobrand() {
        // don't need to share this function externally.
        boolean sYahooCobrand = false;
        if (_cobrand != null &&
                (_cobrand.matches("yahoo|yahooed"))) {
            sYahooCobrand = true;
        }
        return sYahooCobrand;
    }


    public boolean isLogoLinked() {
        return StringUtils.isEmpty(_cobrand);
    }

    /**
     * Is the user allowed to sign in here?
     */
    public boolean isSignInAvailable() {
        return _subscriptionState;
    }

    /**
     * Determine if this site should be ad free
     *
     * @return true if it's ad free
     */
    public boolean isAdFree() {
        return _cobrand != null &&
                (_cobrand.matches("mcguire|framed|number1expert|vreo"));
    }

    /**
     * A String of the onload script(s) to be included in the body tag.
     */
    public String getOnload() {
        return _onload;
    }

    private void setShowingHeader(boolean showingHeader) {
        _showingHeader = showingHeader;
    }

    private void setShowingFooter(boolean showingFooter) {
        _showingFooter = showingFooter;
    }

    private void addOnLoadHandler(String javascript) {
        if (javascript.indexOf('\"') != -1) {
            throw new IllegalArgumentException("Quotes not coded correctly.");
        }
        if (StringUtils.isNotEmpty(_onload)) {
            _onload += ";";
        }
        _onload += javascript;
    }

}
