/*
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: PageHelper.java,v 1.112 2012/10/25 23:36:22 yfan Exp $
 */

package gs.web.util;

import gs.data.community.User;
import gs.data.geo.City;
import gs.web.ads.AdPosition;
import gs.web.community.registration.AuthenticationManager;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 */
public class PageHelper {

    private Map<String,String> _XMLNSes;

    public static final Map pageIds = new HashMap<String, String>() {{
        put("HOME", "0");
        put("RESEARCH_COMPARE", "1");
        put("LIBRARY", "2");
        put("ABOUT_US", "4");
        put("CONTACT_US", "5");
        put("NEWSLETTERS", "6");
        put("SEASONAL", "7");
        put("COLLEGE", "8");
        put("COUNTDOWN_COLLEGE", "9");
        put("COMMUNITY_LANDING", "10");
        put("PRESCHOOL", "11");
        put("ELEMENTARY_SCHOOL", "12");
        put("MIDDLE_SCHOOL", "13");
        put("HIGH_SCHOOL", "14");
        put("SPECIAL_NEEDS", "15");
        put("TUTORING", "16");
        put("MEDIA", "17");
        put("HEALTHY_KIDS", "18");
        put("EDUCATION_TOPICS", "19");
        put("SCHOOL_CHOICE", "20");
        put("COLLEGE_PREP", "21");
        put("BACK_TO_SCHOOL", "22");
        put("TOP_NAV_PROMO", "23");
        put("ACADEMICS_AND_ACTIVITIES", "24");
        put("LEARNING_DISABILITIES", "25");
        put("IMPROVE_YOUR_SCHOOL", "26");
        put("HEALTH_AND_DEVELOPMENT", "27");
        put("GREAT_GIFTS", "28");
        put("TEST_PREP", "29");
        put("EDUCATIONAL_TOYS", "30");
        put("GIFT_GUIDE", "31");
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

    public static void hideBelowNavAds(HttpServletRequest request) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.setShowingBelowNavAds(false);
        } else {
            _log.error("No PageHelper object available.");
        }
    }

    public static void hideAllAds(HttpServletRequest request) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.setHideAds(true);
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
     * Adds the referenced code file to the list of files to be included. The sitemsh decorator
     * is responsible for retrieving these and including them.
     *
     * @param request The HTTP request
     * @param javascriptSrc exact url to be included
     * @param head Boolean to control whether the JS is included in the HEAD or later
     */
    public static void addJavascriptSource(HttpServletRequest request, String javascriptSrc, boolean head) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.addJavascriptSource(javascriptSrc, head);
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
        PageHelper.addJavascriptSource(request, javascriptSrc, true);
    }

    /**
     * Adds the referenced css file to the list of files to be included at the top of the file. The sitemsh decorator is
     * responsible for retrieving these and including them.
     *
     * If media is specified for the last time the same cssSrc is specified
     *
     * If cssSrc contains screen, then the media type will be media="screen"
     * If cssSrc contains print, then the media type will be media="print"
     * Else no media attribute will appear
     *
     * @param cssSrc exact url to be included
     * @param media if this is not null, use media="[specified string]", overriding the logic above.
     */
    public static void addExternalCss(HttpServletRequest request, String cssSrc, String media) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.addCssSource(cssSrc, media);
        } else {
            _log.error("No PageHelper object available.");
        }
    }

    public static void addNthGraderHover(HttpServletRequest request) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.setShowingNthGraderHover(true);
        } else {
            _log.error("No PageHelper object available.");
        }
    }

    protected void setShowingNthGraderHover(boolean b) {
        _showingNthGraderHover = b;
    }

    public boolean isShowingNthGraderHover() {
        return _showingNthGraderHover;
    }

    public static final String REQUEST_ATTRIBUTE_NAME = "pageHelper";

    private boolean _showingHeader = true;
    private boolean _showingLeaderboard = true;
    private boolean _showingFooter = true;
    private boolean _showingFooterAd = true;

    public boolean isAdsHidden() {
        return _hideAds;
    }

    public void setHideAds(boolean _hideAds) {
        this._hideAds = _hideAds;
    }

    private boolean _showingBelowNavAds = true;
    private boolean _betaPage = false;
    private String _pageName = "";
    private boolean _includeQualaroo;

    private Properties _versionProperties ;

    private static final Log _log = LogFactory.getLog(PageHelper.class);
    private String _onload = "";
    private String _onunload = "";
    private boolean _showingNthGraderHover = false;


    private boolean _hideAds = false;


    private Set<String> _javascriptFileSources; // Unique set of files to include on page
    private List<JavaScriptInclude> _javascriptFiles;   // Insertion order is important so we'll use a list
    private Set<String> _cssFiles;          //Insertion order is important so we'll used LinkedHashSet
    private Map<String,String> _cssMediaMap; // map of css src to which media to use
    private Map<String,String> _metaProperties; //meta tags with the property attribute. Used for Facebook's implementation of OpenGraph API

    //ad positions that appear on current page
    private Set<AdPosition> _adPositions = new HashSet<AdPosition>();
    // ad positions on the current page which should not support GPT ghost text hiding
    private Set<AdPosition> _adPositionsWithDisabledGptGhostTextHiding = new HashSet<AdPosition>();
    // ad positions on the current page which should have their companion sizes omitted in the defineSlot call
    private Set<AdPosition> _adPositionsWithOmittedCompanionSizes = new HashSet<AdPosition>();
    // ad positions and associated tabs for which if condition should wrap around defineSlot() and display() ad calls
    private Map<AdPosition,String> _adPositionsWithIfConditionOnAdCalls = new HashMap<AdPosition,String>();
    // ad positions on the current page which had to be defined early using ad:enableAdPosition
    private Set<AdPosition> _adPositionsDefinedEarly = new HashSet<AdPosition>();
    /** ad keywords for current page.  Stored in a MultiMap so that multiple values can be associated with a given
     * key.  This functionality is allowed by the Google API and is required for at least one of our use cases.  This
     * MultiMap has Strings for keys and Collections of Strings for values.
     */
    private MultiMap _adKeywords = new MultiHashMap();

    /**
     * Hint for use with Google AdSense. Currently just one possible value; expand to support multiple entries if needed.
     */
    private String _adSenseHint = null;

    private boolean includeFacebookInit;

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

    public boolean hasAdKeyword(String name) {
        return _adKeywords.containsKey(name);
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

    public boolean hasAdKeywordWithValue(String key, String value) {
        Collection values = (Collection)_adKeywords.get(key);
        if (values != null) {
            return values.contains(value);
        }
        return false;
    }

    /**
     * Helper routine that returns OAS ad server formatted keyword value pairs
     * @return Empty string or
     */
    public String getOASKeywords() {
        StringBuilder buffer = new StringBuilder(_adKeywords.size()*12);
        for (Iterator it = new TreeMap(_adKeywords).keySet().iterator(); it.hasNext();) {
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

    /**                                                                                    is
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

    /**
     * @param ad Ad position that should have disabled GPT ghost text hiding
     */
    public void addAdPositionWithDisabledGptGhostTextHiding(AdPosition ad) {
        if (null == ad) {
            throw new IllegalArgumentException("Ad cannot be null");
        }
        _adPositionsWithDisabledGptGhostTextHiding.add(ad);
    }

    /**
     * @return Returns a non-null list of ad positions for which GPT ghost text hiding should be disabled
     */
    public Set<AdPosition> getAdPositionsWithDisabledGptGhostTextHiding() {
        return _adPositionsWithDisabledGptGhostTextHiding;
    }

    /**
     * @param ad Ad position that should have companion sizes omitted in the defineSlot call
     */
    public void addAdPositionWithOmittedCompanionSizes(AdPosition ad) {
        if (null == ad) {
            throw new IllegalArgumentException("Ad cannot be null");
        }
        _adPositionsWithOmittedCompanionSizes.add(ad);
    }

    /**
     * @return Returns a non-null list of ad positions for which companion sizes should be omitted in the defineSlot call
     */
    public Set<AdPosition> getAdPositionsWithOmittedCompanionSizes() {
        return _adPositionsWithOmittedCompanionSizes;
    }

    /**
     * @param ad Ad position that should have an if condition on the defineSlot() and display() ad calls
     */
    public void addAdPositionWithIfConditionOnAdCalls(AdPosition ad, String associatedTab) {
        if (ad == null || associatedTab == null) {
            throw new IllegalArgumentException("Ad cannot be null");
        }
        _adPositionsWithIfConditionOnAdCalls.put(ad, associatedTab);
    }

    /**
     * @return Returns a non-null list of ad positions for which there should be an if condition on the defineSlot() and display() ad calls
     */
    public Map<AdPosition,String> getAdPositionsWithIfConditionOnAdCalls() {
        return _adPositionsWithIfConditionOnAdCalls;
    }

    /**
     * @param ad Ad position that had to be defined early, using ad:enableAdPosition
     */
    public void addAdPositionDefinedEarly(AdPosition ad) {
        if (null == ad) {
            throw new IllegalArgumentException("Ad cannot be null");
        }
        _adPositionsDefinedEarly.add(ad);
    }

    /**
     * @return Returns a non-null list of ad positions for which had to be defined early, using ad:enableAdPosition
     */
    public Set<AdPosition> getAdPositionsDefinedEarly() {
        return _adPositionsDefinedEarly;
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
        setStateAdKeyword();
    }

    public void setStateAdKeyword() {
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

    /**
     * There's a footer ad at the bottom of the page, above the nav elements and SEO stuff. It's currently a google ad.
     * Do we show it?
     */
    public boolean isShowingBelowNavAds() {
        return _showingBelowNavAds;
    }

    public void setShowingBelowNavAds(boolean showingBelowNavAds) {
        _showingBelowNavAds = showingBelowNavAds;
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

    private boolean isDallasNewsCobrand() {
        // don't need to share this function externally.
        boolean sDallasNewsCobrand = false;
        if (_sessionContext.getCobrand() != null &&
                (_sessionContext.getCobrand().matches("dallasnews"))) {
            sDallasNewsCobrand = true;
        }
        return sDallasNewsCobrand;
    }

    public boolean isShowingHomepageBoxAd() {
        return (!isAdFree() && !isAdServedByCobrand()) ||
                (isAdServedByCobrand() && isDallasNewsCobrand());
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
        return   _hideAds || !_sessionContext.isAdvertisingOnline() ||
                (_sessionContext.getCobrand() != null &&
                 _sessionContext.getCobrand().matches("mcguire|framed|vreo|e-agent|homegain|envirian|connectingneighbors|test")
                ) ||
                _sessionContext.isCrawler() || _sessionContext.isIntegrationTest();
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
                _sessionContext.getCobrand().matches("yahoo|yahooed|family|encarta|arkansasonline|ocregister|dallasnews|momshomeroom|connpost|greenwichtime|newstimes|stamfordadvocate");
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

    public static void addXmlns(HttpServletRequest request, String name, String href) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.addXmlns(name, href);
        } else {
            _log.error("No PageHelper object available.");
        }
    }

    private void addXmlns(String name, String href) {
        if (href != null && name != null) {
            if (_XMLNSes == null) {
                _XMLNSes = new HashMap<String,String>();
            }
            _XMLNSes.put(name, href);
        }
    }

    public String getXmlnsList() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : _XMLNSes.entrySet()) {
            try {
                if (entry.getKey() != null && entry.getValue() != null) {
                    sb.append("xmlns:").append(entry.getKey());
                    sb.append("=\"");
                    sb.append(UrlUtil.urlEncode(entry.getValue()));
                    sb.append("\" ");
                }
            } catch (Exception e) {
                _log.debug("Couldn't encode url " + entry.getValue(), e);
            }
        }
        return sb.toString();
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

    private void addJavascriptSource(String src, boolean head) {
        if (_javascriptFileSources == null) {
            _javascriptFileSources = new HashSet<String>();
        }
        if (_javascriptFiles == null) {
            _javascriptFiles = new LinkedList<JavaScriptInclude>();
        }

        if (!_javascriptFileSources.contains(src)) {
            if(!src.contains("http")){
                if(src.contains("?")){
                    src = src.substring(0, src.indexOf('?'));
                }
                String cacheBustingParam;
                String sha1 = getVersionProperties().getProperty("gsweb.sha");
                if (StringUtils.length(sha1) > 5) {
                    cacheBustingParam = "?v=" + StringUtils.left(sha1, 5);
                } else {
                    cacheBustingParam = "?v=" + getVersionProperties().getProperty("gsweb.version");
                }
                src = src + cacheBustingParam;
            }

            if (!_javascriptFileSources.contains(src)){
                _javascriptFileSources.add(src);
                _javascriptFiles.add(new JavaScriptInclude(src, head));
            }
        }
    }

    private void addJavascriptSource(String src) {
        addJavascriptSource(src, true);
    }

    /**
     * Class for javascript include scripts.
     */
    private class JavaScriptInclude {
        /** Source of the script used in the src attribute */
        private String src;
        /** Should the javascript be included in the HEAD or not */
        private boolean head = true;

        public JavaScriptInclude (String src, boolean head) {
            this.src = src;
            this.head = head;
        }
        public JavaScriptInclude (String src) {
            this.src = src;
        }

        public String getSrc() {
            return src;
        }

        public boolean isHead() {
            return head;
        }
    }

    private void addCssSource(String src, String media) {
        if (_cssFiles == null) {
            _cssFiles = new LinkedHashSet<String>();
            _cssMediaMap = new HashMap<String,String>();
        }
        if (!_cssFiles.contains(src)) {
            if(!(src.indexOf("http") >-1)){
                if((src.indexOf('?') > -1)){
                    src = src.substring(0,src.indexOf('?'));
                }
                String cacheBustingParam;
                String sha1 = getVersionProperties().getProperty("gsweb.sha");
                if (StringUtils.length(sha1) > 5) {
                    cacheBustingParam = "?v=" + StringUtils.left(sha1, 5);
                } else {
                    cacheBustingParam = "?v=" + getVersionProperties().getProperty("gsweb.version");
                }
                src = src + cacheBustingParam;
            }
            _cssFiles.add(src);
            // this means that the last media specified will be used
            // if media isn't specified for the last time this file shows up, media will not be applied
            // as overriding behavior over using the file name
            // if media is specified, file name will not be used to determine media
            _cssMediaMap.put(src, media);
        }
    }

    public boolean isDevEnvironment() {
        return _urlUtil.isDevEnvironment(_sessionContext.getHostName());
    }

    public boolean isStagingServer() {
        return _urlUtil.isStagingServer(_sessionContext.getHostName());
    }

    public boolean isCloneServer() {
        return _urlUtil.isCloneServer(_sessionContext.getHostName());
    }

    public boolean isAdminServer() {
        return _urlUtil.isAdminServer(_sessionContext.getHostName());
    }

    /**
     * Returns true if this server shouldn't be crawled by search engines.
     *
     */
    public boolean isNoCrawlServer() {
        return isStagingServer()
                || isAdminServer()
                /*
                 modified to work for mitchtest it also is including
                */

                || UrlUtil.isDeveloperWorkstation(_sessionContext.getHostName())
                || UrlUtil.isQAServer(_sessionContext.getHostName());
    }

    public String getHeadCssElements() {
        StringBuilder sb = new StringBuilder();
        if (_cssFiles != null && _cssMediaMap != null) {
            for (String _cssFile : _cssFiles) {
                String src = _cssFile;
                String media = _cssMediaMap.get(src);

                src = StringUtils.replace(src, "&", "&amp;");
                if (media == null) {
                    media = "";
                    if (StringUtils.containsIgnoreCase(src, "screen")) {
                        media = " media=\"screen\"";
                    } else if (StringUtils.containsIgnoreCase(src, "print")) {
                        media = " media=\"print\"";
                    }
                } else {
                    media = " media=\"" + media + "\"";
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

    public String getHeadElements() {
        return getHeadElements(true);
    }

    public String getHeadElementsWithoutCss() {
        return getHeadElements(false);
    }

    /**
     * Examines all the javascript and css includes and dumps out the appropriate header code. If no code is necessary,
     * and empty string is returned.
     *
     * @param includeCss True to include css tags, false to skip them
     * @return non-null String.
     */
    public String getHeadElements(boolean includeCss) {
        if (_javascriptFiles != null || _cssFiles != null || _cssMediaMap != null) {
            StringBuilder sb = new StringBuilder();
            if (includeCss) {
                sb.append(getHeadCssElements());
            }

            outputScriptTags(sb, true);

            if (_metaProperties != null) {
                //sb.append("<!-- facebook ignores comments");
                for (Map.Entry entry : _metaProperties.entrySet()) {
                    sb.append("<meta property=\"");
                    sb.append(entry.getKey());
                    sb.append("\" content=\"");
                    sb.append(entry.getValue());
                    sb.append("\" />");
                }
                //sb.append("-->");
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * Examines all the javascript includes and dumps out the ones that did not want to be in the head.
     *
     * @return non-null String.
     */
    public String getBottomJavaScript() {
        StringBuilder sb = new StringBuilder();
        outputScriptTags(sb, false);
        return sb.toString();
    }

    private void outputScriptTags(StringBuilder sb, boolean head) {
        if (_javascriptFiles != null) {
            for (JavaScriptInclude _javascriptFile : _javascriptFiles) {
                if (_javascriptFile.isHead() == head) {
                    String src = _javascriptFile.getSrc();
                    src = StringUtils.replace(src, "&", "&amp;");
                    sb.append("<script type=\"text/javascript\" src=\"").append(src).append("\"></script>");
                }
            }
        }
    }

    /**
     * Static method to set a user's member cookie
     */
    public static void setMemberCookie(HttpServletRequest request, HttpServletResponse response, User user) {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        SessionContextUtil util = context.getSessionContextUtil();
        util.changeUser(request, response, user);
    }

    public static void setCityIdCookie(HttpServletRequest request, HttpServletResponse response, Integer cityId) {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        SessionContextUtil util = context.getSessionContextUtil();
        util.changeCity(context, request, response, cityId);
    }

    public static void setCityIdCookie(HttpServletRequest request, HttpServletResponse response, City city) {
        setCityIdCookie(request, response, city.getId());
    }

    public static void setPathway(HttpServletRequest request, HttpServletResponse response, String pathway) {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        SessionContextUtil util = context.getSessionContextUtil();
        util.changePathway(context, response, (String) pageIds.get(pathway));
    }

    public static void setHubCookiesForNavBar(HttpServletRequest request, HttpServletResponse response, String state,String city) {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        SessionContextUtil util = context.getSessionContextUtil();
        util.setHubStateCookie(response, request, state);
        util.setHubCityCookie(response, request, city);
    }

    public static void clearHubCookiesForNavBar(final HttpServletRequest request, final HttpServletResponse response) {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        SessionContextUtil util = context.getSessionContextUtil();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for( Cookie cookie : cookies) {
                if( "hubCity".equals(cookie.getName()) || "hubState".equals(cookie.getName())) {
                    cookie.setValue(null);
                }
            }
        }
        util.clearHubCityCookie(response);
        util.clearHubStateCookie(response);
    }

    public static void setHubUserCookie(HttpServletRequest request, HttpServletResponse response) {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        SessionContextUtil util = context.getSessionContextUtil();
        util.setIsHubUserCookie(response, request);

    }
    public static void clearHubUserCookie(HttpServletRequest request, HttpServletResponse response) {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        SessionContextUtil util = context.getSessionContextUtil();
        util.clearIsHubUserCookie(response);

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
            return CookieUtil.hasCookie(request, cookieName);
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

    public static void addMetaProperty(HttpServletRequest request, String type, String content) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.addMetaProperty(type,content);
        } else {
            _log.error("No PageHelper object available.");
        }
    }

    public void addMetaProperty(String type, String content) {
        if (_metaProperties == null) {
            _metaProperties = new HashMap<String,String>();
        }
        _metaProperties.put(type, content);
    }

    public Map<String,String> getMetaProperties() {
        return _metaProperties;
    }

    public static void setIncludeQualaroo(HttpServletRequest request, boolean includeQualaroo) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.setIncludeQualaroo(includeQualaroo);
        } else {
            _log.error("No PageHelper object available, can't include Qualaroo JS");
        }
    }

    public void setIncludeQualaroo(boolean includeQualaroo) {
        _includeQualaroo = includeQualaroo;
    }


    public boolean isIncludeQualaroo() {
        return _includeQualaroo;
    }

    public void setIncludeFacebookInit(boolean includeFacebookInit) {
        this.includeFacebookInit = includeFacebookInit;
    }

    public static void setIncludeFacebookInit(HttpServletRequest request, boolean includeFacebookInit) {
        PageHelper pageHelper = getInstance(request);
        if (pageHelper != null) {
            pageHelper.setIncludeFacebookInit(includeFacebookInit);
        } else {
            _log.error("No PageHelper object available, can't include Facebook init JS");
        }
    }

    public boolean isIncludeFacebookInit() {
        return includeFacebookInit;
    }

    public void setGptSingleRequestMode(boolean value) {
        _sessionContext.setGptSingleRequestMode(value);
    }

    public boolean isOnSchoolProfile() {
        // the logic for setting this template value is in SchoolProfileController - just search for "template"
        return hasAdKeywordWithValue("template","SchoolProf");
    }

    public static String getOptimizelyUrl() {
        return System.getProperty("optimizely_tag");
    }

    public String getOptimizelyTag() {
        return System.getProperty("optimizely_tag");
    }
}