/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PageHelper.java,v 1.43 2007/06/05 23:05:35 aroy Exp $
 */

package gs.web.util;

import gs.data.community.User;
import gs.web.ads.AdPosition;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.community.registration.AuthenticationManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Helper class to render and decorate a JSP page correctly. Provides a place to encapsulate logic about our pages to
 * answer fine-grained questions about what shows and doesn't show in a testable manner.
 * <p/>
 * <ul> <li>Replaces somewhat haphazard nature of sitemesh's meta tags. <li>Replaces untyped nature of page scoped
 * attributes. <li>Provides a place to make business rule and policy decisions for JSP pages. </ul>
 * <p/>
 * Usage on pages should be done via the pageHelper tag library. There is an access (via static methods) that
 * controllers can use, but this has not proven useful and may be phased out. <p>The sitemesh decorator has
 * responsibility to handle some of the work here. Those should access the current object under the request scoped
 * "pageHelper" attribute.
 * <p/>
 * There is code in the page interceptor responsible for creating this object and setting its initial values.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class PageHelper {
    public static final Map pageIds = new HashMap<String, String>() {{
        put("HOME", "0");
        put("RESEARCH_COMPARE", "1");
        put("LIBRARY", "2");
        put("ABOUT_US", "4");
        put("CONTACT_US", "5");
        put("NEWSLETTERS", "6");
        put("SEASONAL", "7");
        put("COUNTDOWN_COLLEGE", "9");
        put("COMMUNITY_LANDING", "10");
    }};

    private final SessionContext _sessionContext;

    public static void hideLeaderboard(HttpServletRequest request) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.setShowingLeaderboard(false);
        } else {
            _log.error("No PageHelper object available.");
        }
    }

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
     * Adds the given code to the onload script of the body tag.
     */
    public static void addOnunloadHandler(HttpServletRequest request, String javascript) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.addOnunloadHandler(javascript);
        } else {
            _log.error("No PageHelper object available.");
        }
    }

    /**
     * Adds the referenced code file to the list of files to be included at the top of the file. The sitemsh decorator
     * is responsible for retrieving these and including them.
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
     * Adds the referenced css file to the list of files to be included at the top of the file. The sitemsh decorator is
     * responsible for retrieving these and including them.
     *
     * If cssSrc contains screen, then the media type will be media="screen"
     * If cssSrc contains print, then the media type will be media="print"
     * Else no media attribute will appear
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
    private boolean _showingLeaderboard = true;
    private boolean _showingFooter = true;
    private boolean _showingFooterAd = true;
    private boolean _betaPage = false;

    private static final Log _log = LogFactory.getLog(PageHelper.class);
    private String _onload = "";
    private String _onunload = "";


    private Set<String> _javascriptFiles;   //Insertion order is important so we'll used LinkedHashSet
    private Set<String> _cssFiles;          //Insertion order is important so we'll used LinkedHashSet

    //ad positions that appear on current page
    private Set<AdPosition> _adPositions = new HashSet<AdPosition>();
    //ad keywords for current page
    private Map<String,String> _adKeywords = new HashMap<String,String>();

    /**
     * Add a keyword/value pair for current page to be passed on to ad server
     * @param name name of keyword
     * @param value value of keyword
     */
    public void addAdKeyword(String name, String value) {
        if (null == name || null == value) {
            throw new IllegalArgumentException("Name value pair cannot be null");
        }
        _adKeywords.put(name,value);
    }

    /**
     * Get a map of the ad keyword/value pairs
     * @return An empty map or map containing the keyword value pair if they exist
     */
    public Map<String,String> getAdKeywords() {
        return _adKeywords;
    }

    /**
     * Helper routine that returns OAS ad server formatted keyword value pairs
     * @return Empty string or
     */
    public String getOASKeywords() {
        StringBuffer buffer = new StringBuffer(_adKeywords.size()*12);

        for (Iterator it = _adKeywords.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            buffer.append(key).append("=").append(_adKeywords.get(key));
            if (it.hasNext()) {
                buffer.append("&");
            }
        }        
        return buffer.toString();
    }

    /**
     * @param ad Ad position that will be added on current page
     */
    public void addAdPosition(AdPosition ad) {
        if (null == ad) {
            throw new IllegalArgumentException("Ad cannot be null");
        }
        _adPositions.add(ad);
    }

    /**
     * @return Returns a non-null list of ad positions for current page
     */
    public Set<AdPosition> getAdPositions() {
        return _adPositions;
    }

    private static UrlUtil _urlUtil = new UrlUtil();

    private static PageHelper getInstance(HttpServletRequest request) {
        return (PageHelper) request.getAttribute(REQUEST_ATTRIBUTE_NAME);
    }

    public PageHelper(SessionContext sessionContext, HttpServletRequest request) {
        _sessionContext = sessionContext;

        // a simple-minded way to determine if the page is a beta-related page
        String uri = request.getRequestURI();
        if (StringUtils.isNotBlank(uri)) {
            if (uri.matches(".*/community/beta.*")) {
                _betaPage = true;
            }
        }
        if (null != _sessionContext.getState()) {
            _adKeywords.put("state", _sessionContext.getState().getAbbreviationLowerCase());    
        }
    }

    public boolean isShowingBannerAd() {
        return !isAdFree() && (StringUtils.isEmpty(_sessionContext.getCobrand()) ||
                "charterschoolratings".equals(_sessionContext.getCobrand()));
    }

    public boolean isShowingLogo() {
        return _showingHeader && !isFramed();
    }

    public boolean isShowingUserInfo() {
        return _showingHeader && !isFramed();
    }

    public boolean isBetaPage() {
        return _betaPage;
    }


    /**
     * Is all the stuff in the footer (SE links, About us links, copyright) shown?
     *
     * @todo break this into smaller pieces?
     */
    public boolean isShowingFooter() {
        return _showingFooter && !isFramed() && !isYahooCobrand();
    }

    /**
     * There's a footer ad at the bottom of the page, above the nav elements and SEO stuff. It's currently a google ad.
     * Do we show it?
     */
    public boolean isShowingFooterAd() {
        return _showingFooterAd && !isAdFree() && !isYahooCobrand() && !isFamilyCobrand();
    }

    public void setShowingFooterAd(boolean showingFooterAd) {
        _showingFooterAd = showingFooterAd;
    }

    public boolean isShowingLeaderboard() {
        return _showingLeaderboard;
    }

    public void setShowingLeaderboard(boolean showingLeaderboard) {
        _showingLeaderboard = showingLeaderboard;
    }

    private boolean isYahooCobrand() {
        // don't need to share this function externally.
        boolean sYahooCobrand = false;
        if (_sessionContext.getCobrand() != null &&
                (_sessionContext.getCobrand().matches("yahoo|yahooed"))) {
            sYahooCobrand = true;
        }
        return sYahooCobrand;
    }

    private boolean isFamilyCobrand() {
        // don't need to share this function externally.
        boolean sFamilyCobrand = false;
        if (_sessionContext.getCobrand() != null &&
                (_sessionContext.getCobrand().matches("family"))) {
            sFamilyCobrand = true;
        }
        return sFamilyCobrand;
    }

    public boolean isLogoLinked() {
        return StringUtils.isEmpty(_sessionContext.getCobrand());
    }

    /**
     * Is the user allowed to sign in here?
     */
    public boolean isSignInAvailable() {
        return false;
    }

    /**
     * Determine if this site is a totally ad free site. This will return TRUE if advertising is turned off either
     * because of a cobrand agreement or advertising is manually turned off.
     *
     * @return true if it's ad free
     */
    public boolean isAdFree() {
        return !_sessionContext.isAdvertisingOnline() || isFramed();
    }

    /**
     * Determine if this site is a framed site, in other words, no ads and no nav
     *
     * @return true if it's framed
     */
    public boolean isFramed() {
        return _sessionContext.isFramed();
    }

    public boolean isAdServedByCobrand() {
        return _sessionContext.getCobrand() != null &&
                _sessionContext.getCobrand().matches("yahoo|yahooed|family|encarta");
    }
    /**
     * A String of the onload script(s) to be included in the body tag.
     */
    public String getOnload() {
        return _onload;
    }

    /**
     * @return A String of the onunload script(s) to be included in the body tag.
     */
    public String getOnunload() {
        return _onunload;
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

        if (!StringUtils.contains(_onload, javascript)) {
            if (StringUtils.isNotEmpty(_onload)) {
                _onload += ";";
            }
            _onload += javascript;
        }
    }

    private void addOnunloadHandler(String javascript) {
        if (javascript.indexOf('\"') != -1) {
            throw new IllegalArgumentException("Quotes not coded correctly.");
        }

        if (!StringUtils.contains(_onunload, javascript)) {
            if (StringUtils.isNotEmpty(_onunload)) {
                _onunload += ";";
            }
            _onunload += javascript;
        }
    }

    private void addJavascriptSource(String src) {
        if (_javascriptFiles == null) {
            _javascriptFiles = new LinkedHashSet<String>();
        }
        if (!_javascriptFiles.contains(src)) {
            _javascriptFiles.add(src);
        }
    }

    private void addCssSource(String src) {
        if (_cssFiles == null) {
            _cssFiles = new LinkedHashSet<String>();
        }
        if (!_cssFiles.contains(src)) {
            _cssFiles.add(src);
        }
    }

    public boolean isDevEnvironment() {
        return _urlUtil.isDevEnvironment(_sessionContext.getHostName());
    }

    public boolean isStagingServer() {
        return _urlUtil.isStagingServer(_sessionContext.getHostName());
    }

    /**
     * Examines all the javascript and css includes and dumps out the appropriate header code. If no code is necessary,
     * and empty string is returned.
     *
     * @return non-null String.
     */
    public String getHeadElements() {
        if (_javascriptFiles != null || _cssFiles != null) {
            StringBuffer sb = new StringBuffer();
            if (_javascriptFiles != null) {
                for (String _javascriptFile : _javascriptFiles) {
                    String src = _javascriptFile;
                    src = StringUtils.replace(src, "&", "&amp;");
                    sb.append("<script type=\"text/javascript\" src=\"").append(src).append("\"></script>");
                }
            }
            if (_cssFiles != null) {
                for (String _cssFile : _cssFiles) {
                    String src = _cssFile;
                    src = StringUtils.replace(src, "&", "&amp;");
                    String media = "";
                    if (StringUtils.containsIgnoreCase(src, "screen")) {
                        media = " media=\"screen\"";
                    } else if (StringUtils.containsIgnoreCase(src, "print")) {
                        media = " media=\"print\"";
                    }
                    sb.append("<link rel=\"stylesheet\" type=\"text/css\"")
                            .append(media)
                            .append(" href=\"")
                            .append(src)
                            .append("\"></link>");
                }
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * Static method to set a user's member cookie
     */
    public static void setMemberCookie(HttpServletRequest request, HttpServletResponse response, User user) {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        SessionContextUtil util = context.getSessionContextUtil();
        util.changeUser(context, response, user);
    }

    public static void setPathway(HttpServletRequest request, HttpServletResponse response, String pathway) {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        SessionContextUtil util = context.getSessionContextUtil();
        util.changePathway(context, response, (String) pageIds.get(pathway));
    }

    public static void setHasSearchedCookie(HttpServletRequest request, HttpServletResponse response) {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        context.setHasSearched(true);
        SessionContextUtil util = context.getSessionContextUtil();
        util.setHasSearched(response);
    }

    /**
     * Sets a user's member cookie and authentication information. Call when a user needs to be
     * logged in to Community, or their email address has changed.
     *
     * @param request Http request
     * @param response Http response
     * @param user used to initialize the cookie, should not be null
     * @throws java.security.NoSuchAlgorithmException On error with md5 algorithm
     */
    public static void setMemberAuthorized(HttpServletRequest request, HttpServletResponse response, User user) throws NoSuchAlgorithmException {
        String hash = AuthenticationManager.generateCookieValue(user);

        setMemberCookie(request, response, user);
        SessionContext context = SessionContextUtil.getSessionContext(request);
        SessionContextUtil util = context.getSessionContextUtil();
        util.changeAuthorization(request, response, user, hash);
    }

    /**
     * This method checks only that the userHash cookie is set, it does not actually verify
     * that the hash is valid. This method should perform no expensive operations (!IMPORTANT).
     * @param request http request object
     * @return true if the session context contains a member id and a user hash string.
     */
    public static boolean isCommunityCookieSet(HttpServletRequest request) {
        if (request.getCookies() != null) {
            String cookieName = "community_" + SessionContextUtil.getServerName(request);
            for (Cookie cookie: request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method verifies that the session context contains a member id and a user hash, and
     * that the user hash is valid for that member id. This method performs database access.
     * @param request Http request
     * @return True if SessionContext contains a valid member id and hash
     */
    public static boolean isMemberAuthorized(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return false;
        }
        try {
            String cookieName = "community_" + SessionContextUtil.getServerName(request);
            String storedHash = null;
            Integer memberId = null;
            for (Cookie cookie: request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    memberId = AuthenticationManager.getUserIdFromCookieValue(cookie.getValue());
                    storedHash = AuthenticationManager.getHashFromCookieValue(cookie.getValue());
                }
            }
            SessionContext context = SessionContextUtil.getSessionContext(request);
            return memberId != null && StringUtils.isNotEmpty(storedHash) &&
                    isMemberAuthorized(context.getUser(), storedHash + memberId);
        } catch (Exception e) {
            _log.error(e);
            return false;
        }
    }

    /**
     * Verifies that the storedHash is valid for the user.
     * @param user User to verify authorization for
     * @param storedHash Hash string
     * @return True if hash is valid for given user
     * @throws NoSuchAlgorithmException On error with md5 algorithm
     */
    public static boolean isMemberAuthorized(User user, String storedHash) throws NoSuchAlgorithmException {
        // save time by exiting early
        if (user == null || StringUtils.isEmpty(storedHash)) {
            return false;
        }
        String realHash = AuthenticationManager.generateCookieValue(user);

        return storedHash != null && realHash.equals(storedHash);
    }
}
