/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PageHelper.java,v 1.11 2006/05/23 23:14:16 dlee Exp $
 */

package gs.web.util;

import gs.web.ISessionFacade;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class to render and decorate a JSP page correctly.
 * Provides a place to encapsulate logic about our pages to answer
 * fine-grained questions about what shows and doesn't show in a testable manner.
 * <ul>
 * <li>Replaces somewhat haphazard nature of sitemesh's meta tags.
 * <li>Replaces untyped nature of page scoped attributes.
 * <li>Provides a place to make business rule and policy decisions for JSP pages.
 * </ul>
 * <p>Usage on pages should be done via the pageHelper tag library.
 * There is an access (via static methods) that controllers can use, but this
 * has not proven useful and may be phased out.
 * <p>The sitemesh decorator has responsibility to handle some of the work
 * here.
 * Those should access the current object under the request scoped "pageHelper"
 * attribute.
 * <p>There is code in the page interceptor responsible for creating this object
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

    public static void hideFooterAd(HttpServletRequest request) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.setShowingFooterAd(false);
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


    /**
     * Adds the referenced code file to the list of files to be included
     * at the top of the file. The sitemsh decorator is responsible for
     * retrieving these and including them.
     *
     * @param javascriptSrc exact url to be included
     */
    public static void addJavascriptSource(HttpServletRequest request, String javascriptSrc) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.addJavascriptSource(javascriptSrc);
        } else {
            _log.error("No PageHelper object available.");
        }
    }


    /**
     * Adds the referenced css file to the list of files to be included
     * at the top of the file. The sitemsh decorator is responsible for
     * retrieving these and including them.
     *
     * @param cssSrc exact url to be included
     */
    public static void addExternalCss(HttpServletRequest request, String cssSrc) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.addCssSource(cssSrc);
        } else {
            _log.error("No PageHelper object available.");
        }
    }


    public static final String REQUEST_ATTRIBUTE_NAME = "pageHelper";

    private boolean _showingHeader = true;
    private boolean _showingFooter = true;
    private boolean _showingFooterAd = true;
    private boolean _advertisingOnline = true;
    private final String _cobrand;
    private final String _hostName;
    private final boolean _subscriptionState;

    private static final Log _log = LogFactory.getLog(PageHelper.class);
    private String _onload = "";
    private List _javascriptFiles;
    private List _cssFiles;

    private static UrlUtil _urlUtil = new UrlUtil();

    private static PageHelper getInstance(HttpServletRequest request) {
        return (PageHelper) request.getAttribute(REQUEST_ATTRIBUTE_NAME);
    }

    public PageHelper(ISessionFacade sessionFacade) {
        _cobrand = sessionFacade.getCobrand();
        _subscriptionState = sessionFacade.getStateOrDefault().isSubscriptionState();
        _hostName = sessionFacade.getHostName();
        _advertisingOnline = sessionFacade.isAdvertisingOnline();
    }

    public boolean isShowingBannerAd() {
        return !isAdFree() && (StringUtils.isEmpty(_cobrand) ||
                "charterschoolratings".equals(_cobrand));
    }

    public boolean isShowingLogo() {
        return _showingHeader && !isFramed();
    }

    public boolean isShowingUserInfo() {
        return _showingHeader && !isFramed();
    }


    /**
     * Is all the stuff in the footer (SE links, About us links, copyright)
     * shown?
     *
     * @todo break this into smaller pieces?
     */
    public boolean isShowingFooter() {
        return _showingFooter && !isFramed() && !isYahooCobrand();
    }

    /**
     * There's a footer ad at the bottom of the page, above the nav elements and SEO
     * stuff. It's currently a google ad. Do we show it?
     */
    public boolean isShowingFooterAd() {
        return _showingFooterAd && !isAdFree() && !isYahooCobrand() && !isFamilyCobrand();
    }

    public void setShowingFooterAd(boolean showingFooterAd) {
        _showingFooterAd = showingFooterAd;
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

    private boolean isFamilyCobrand() {
        // don't need to share this function externally.
        boolean sFamilyCobrand = false;
        if (_cobrand != null &&
                (_cobrand.matches("family"))) {
            sFamilyCobrand = true;
        }
        return sFamilyCobrand;
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
     * Determine if this site is a totally ad free site
     *
     * @return true if it's ad free
     */
    public boolean isAdFree() {
        return !_advertisingOnline || isFramed();
    }

    /**
     * Determine if this site is a framed site, in other words, no ads and no nav
     *
     * @return true if it's framed
     */
    public boolean isFramed() {
        return _cobrand != null &&
                _cobrand.matches("mcguire|framed|number1expert|vreo|e-agent");
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

    private void addJavascriptSource(String src) {
        if (_javascriptFiles == null) {
            _javascriptFiles = new ArrayList();
        }
        _javascriptFiles.add(src);
    }

    private void addCssSource(String src) {
        if (_cssFiles == null) {
            _cssFiles = new ArrayList();
        }
        _cssFiles.add(src);
    }

    public boolean isDevEnvironment() {
        return _urlUtil.isDevEnvironment(_hostName);
    }

    /**
     * Examines all the javascript and css includes and dumps out the
     * appropriate header code. If no code is necessary, and empty string
     * is returned.
     *
     * @return non-null String.
     */
    public String getHeadElements() {
        if (_javascriptFiles != null || _cssFiles != null) {
            StringBuffer sb = new StringBuffer();
            if (_javascriptFiles != null) {
                for (Iterator iter = _javascriptFiles.iterator(); iter.hasNext();) {
                    String src = (String) iter.next();
                    src = StringUtils.replace(src, "&", "&amp;");
                    sb.append("<script type=\"text/javascript\" src=\"" + src + "\"></script>");
                }
            }
            if (_cssFiles != null) {
                for (Iterator iter = _cssFiles.iterator(); iter.hasNext();) {
                    String src = (String) iter.next();
                    src = StringUtils.replace(src, "&", "&amp;");
                    sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + src + "\"></link>");
                }
            }
            return sb.toString();
        }
        return "";
    }
}
