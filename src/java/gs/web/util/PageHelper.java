/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PageHelper.java,v 1.63 2008/09/08 22:18:12 yfan Exp $
 */

package gs.web.util;

import gs.data.community.User;
import gs.data.geo.City;
import gs.web.ads.AdPosition;
import gs.web.community.registration.AuthenticationManager;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.MultiHashMap;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.net.URLEncoder;

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

    /**
     * Get the city name to use for the footer Yahoo Real Estate link.  Not every page has a city associated with it,
     * so those that do should set this to specify what it is if they require the YRE link in the footer.
     *
     * @return Yahoo Real Estate city name for link in footer
     */
    public String getYahooRealEstateCity() {
        return _yahooRealEstateCity;
    }

    /**
     * Get the city name to use for the footer Yahoo Real Estate link, but escape it so that it can be used in an href.  
     *
     * @return Yahoo Real Estate city name for link in footer
     */
    public String getYahooRealEstateCityEscaped() {
        String escapedCity = _yahooRealEstateCity.replace(' ', '_');
        try {
            escapedCity = URLEncoder.encode(escapedCity, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            // Do nothing, just return what we have   
        }

        return escapedCity;
    }

    /**
     * Set the city name to use for the footer Yahoo Real Estate link.  Not every page has a city associated with it,
     * so those that do should use this method to specify what it is if they require the YRE link in the footer.
     *
     * @param request Request object for static invocation
     * @param yahooRealEstateCity City name to use to link to Yahoo Real Estate in the footer
     */
    public static void setYahooRealEstateCity(HttpServletRequest request, String yahooRealEstateCity) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper._yahooRealEstateCity = yahooRealEstateCity;
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
    private String _pageName = "";
    private String _yahooRealEstateCity = "";

    private Properties _versionProperties ;

    private static final Log _log = LogFactory.getLog(PageHelper.class);
    private String _onload = "";
    private String _onunload = "";


    private Set<String> _javascriptFiles;   //Insertion order is important so we'll used LinkedHashSet
    private Set<String> _cssFiles;          //Insertion order is important so we'll used LinkedHashSet

    //ad positions that appear on current page
    private Set<AdPosition> _adPositions = new HashSet<AdPosition>();
    /** ad keywords for current page.  Stored in a MultiMap so that multiple values can be associated with a given
     * key.  This functionality is allowed by the Google API and is required for at least one of our use cases.  This
     * MultiMap has Strings for keys and Collections of Strings for values.
     */
    private MultiMap _adKeywords = new MultiHashMap();

    /**
     * Hint for use with Google AdSense. Currently just one possible value; expand to support multiple entries if needed.
     */
    private String _adSenseHint = null;

    /**
     * Add a keyword/value pair for current page to be passed on to ad server.  This method use Map semantics on
     * the underlying MultiMap -- that is it will overwrite an existing value.  These semantics were left intact
     * via this method because much client code was already expecting it this way.
     * @param name name of keyword
     * @param value value of keyword
     */
    public void addAdKeyword(String name, String value) {
        addAdKeyword(name, value, false);
    }

    /**
     * Add a keyword/value pair for current page to be passed on to ad server.  This method uses MultiMap semantics
     * and will therefore allow setting two or more values for the same key.
     * @param name name of keyword
     * @param value value of keyword
     */
    public void addAdKeywordMulti(String name, String value) {
        addAdKeyword(name, value, true);
    }

    /**
     * Add a keyword/value pair for current page to be passed on to ad server.  This mehtod allows either Map or
     * MultiMap semantics depending on the value of the boolean parameter.
     * @param name name of keyword
     * @param value value of keyword
     * @param allowMultipleValuesForKey whether to allow the multimap behavior or use default map behavior
     */
    public void addAdKeyword(String name, String value, boolean allowMultipleValuesForKey) {
        if (null == name || null == value) {
            throw new IllegalArgumentException("Name value pair cannot be null");
        }
        value = value.replaceAll("[^\\p{Alnum}]", "");
        if (value.length() > 10) {
            value = value.substring(0, 10);
        }
        if (!allowMultipleValuesForKey && _adKeywords.containsKey(name)) {
            _adKeywords.remove(name);
        }
        _adKeywords.put(name,value);
    }

    /**
     * Add a hint for use by Google AdSense. Will be repeated on page for each ad position. See GS-7089.
     * @param hint the hint string for Google
     */
    public void addAdSenseHint(String hint) {
        _adSenseHint = hint;
    }

    public String getAdSenseHint() {
        return _adSenseHint;
    }

    /**
     * Get a multimap of the ad keyword/value pairs
     * @return An empty map or map containing the keyword value pair if they exist
     */
    public MultiMap getAdKeywords() {
        return _adKeywords;
    }

    /**
     * Convenience method to obtain the first key for an ad keyword in the multimap.  This allows Map semantics
     * on the MultiMap when you only want the first value of a key.
     * @param key Key string to get the ad keyword value of
     * @return The keyword value, or the first one if multiple were set
     */
    public String getAdKeywordValue(String key) {
        Collection values = (Collection)_adKeywords.get(key);
        String value = null;

        if (values != null) {
            Iterator i = values.iterator();
            if (i.hasNext()) {
                value = (String)i.next();
            }
        }

        return value;
    }

    /**
     * Helper routine that returns OAS ad server formatted keyword value pairs
     * @return Empty string or
     */
    public String getOASKeywords() {
        StringBuffer buffer = new StringBuffer(_adKeywords.size()*12);

        for (Iterator it = _adKeywords.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            Collection values = (Collection)_adKeywords.get(key);
            for (Object obj : values) {
                String value = (String)obj;
                if ("state".equals(key)) {
                    value = value.toLowerCase();
                }

                buffer.append(key).append("=").append(value.replaceAll(" ","+"));
                if (it.hasNext()) {
                    buffer.append("&");
                }
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
            addAdKeyword("state", _sessionContext.getState().getAbbreviation());
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

    public String getPageName() {
        return _pageName;
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
     * because of a cobrand agreement or advertising is manually turned off. We also don't serve ads to crawlers to
     * conserve bandwidth.
     *
     * @return true if it's ad free
     */
    public boolean isAdFree() {
        return !_sessionContext.isAdvertisingOnline() || isFramed() || _sessionContext.isCrawler();
    }

    /**
     * Content served through ads slots is only removed if advertising is disabled sitewide, or if the visitor
     * is a crawler. Framed cobrands ARE served this type of content, but not actual ads.
     *
     * @return true if content served through ads shouldn't be displayed
     */
    public boolean isAdContentFree() {
        return !_sessionContext.isAdvertisingOnline() || _sessionContext.isCrawler();
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
                _sessionContext.getCobrand().matches("yahoo|yahooed|family|encarta|arkansasonline");
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

    private void setPageName(String pageName) {
        _pageName = pageName;
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
            if(!(src.indexOf("http") >-1)){
                if((src.indexOf('?') > -1)){
                    src = src.substring(0,src.indexOf('?'));
                }
                src = src + "?v=" + getVersionProperties().getProperty("gsweb.version");
            }
            _javascriptFiles.add(src);
        }
    }

    private void addCssSource(String src) {
        if (_cssFiles == null) {
            _cssFiles = new LinkedHashSet<String>();
        }
        if (!_cssFiles.contains(src)) {
            if(!(src.indexOf("http") >-1)){
                if((src.indexOf('?') > -1)){
                    src = src.substring(0,src.indexOf('?'));
                }
                src = src + "?v=" + getVersionProperties().getProperty("gsweb.version");
            }
            _cssFiles.add(src);
        }
    }

    public boolean isDevEnvironment() {
        return _urlUtil.isDevEnvironment(_sessionContext.getHostName());
    }

    public boolean isStagingServer() {
        return _urlUtil.isStagingServer(_sessionContext.getHostName());
    }

    public boolean isAdminServer() {
        return _urlUtil.isAdminServer(_sessionContext.getHostName());
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

    public static void setCityIdCookie(HttpServletRequest request, HttpServletResponse response, City city) {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        SessionContextUtil util = context.getSessionContextUtil();
        util.changeCity(context, request, response, city);
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

    public static void setPageName(HttpServletRequest request, String pageName) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.setPageName(pageName);
        } else {
            _log.error("No PageHelper object available.");
        }
    }

    /**
     * Sets a user's member cookie and authentication information. Call when a user needs to be
     * logged in to Community, or their email address has changed. Defaults to session duration cookie.
     *
     * @param request Http request
     * @param response Http response
     * @param user used to initialize the cookie, should not be null
     * @throws java.security.NoSuchAlgorithmException On error with md5 algorithm
     */
    public static void setMemberAuthorized(HttpServletRequest request, HttpServletResponse response, User user) throws NoSuchAlgorithmException {
        setMemberAuthorized(request, response, user, false);
    }

    /**
     * Sets a user's member cookie and authentication information. Call when a user needs to be
     * logged in to Community, or their email address has changed.
     *
     * @param request Http request
     * @param response Http response
     * @param user used to initialize the cookie, should not be null
     * @param rememberMe true to use long-lived cookie, false to use session cookie
     * @throws java.security.NoSuchAlgorithmException On error with md5 algorithm
     */
    public static void setMemberAuthorized(HttpServletRequest request, HttpServletResponse response, User user, boolean rememberMe) throws NoSuchAlgorithmException {
        String hash = AuthenticationManager.generateCookieValue(user);

        setMemberCookie(request, response, user);
        SessionContext context = SessionContextUtil.getSessionContext(request);
        SessionContextUtil util = context.getSessionContextUtil();
        util.changeAuthorization(request, response, user, hash, rememberMe);
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
     * Verifies that the storedHash is valid for the user. This method will always return false
     * if the user is inactive, even if the hash would be valid.
     * @param user User to verify authorization for
     * @param storedHash Hash string
     * @return True if hash is valid for given user
     * @throws NoSuchAlgorithmException On error with md5 algorithm
     */
    public static boolean isMemberAuthorized(User user, String storedHash) throws NoSuchAlgorithmException {
        // save time by exiting early
        if (user == null || StringUtils.isEmpty(storedHash)) {
            return false;
        } else if (user.getUserProfile() != null && !user.getUserProfile().isActive()) {
            return false;
        }
        String realHash = AuthenticationManager.generateCookieValue(user);

        return storedHash != null && realHash.equals(storedHash);
    }

    public Properties getVersionProperties() {
        if(_versionProperties == null){
            ApplicationContext ac = _sessionContext.getApplicationContext();
            _versionProperties = (Properties) ac.getBean("versionProperties");
        }
        return _versionProperties;
    }

    public void setVersionProperties(Properties versionProperties) {
        _versionProperties = versionProperties;
    }

    public static void logout(HttpServletRequest request, HttpServletResponse response) {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        SessionContextUtil util = context.getSessionContextUtil();
        util.clearUserCookies(response);
        context.setUser(null);
    }
}
