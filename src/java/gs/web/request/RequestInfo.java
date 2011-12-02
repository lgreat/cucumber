package gs.web.request;

import gs.web.mobile.Device;
import gs.web.util.CookieUtil;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import net.sourceforge.wurfl.core.WURFLManager;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceHandler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class RequestInfo {

    public static final String REQUEST_ATTRIBUTE_NAME = "requestInfo";

    private String _hostname;
    private String _currentSubdomain;
    private String _requestURL;

    private WURFLManager _springWurflManager;

    private Boolean _onPkSubdomain;
    private Boolean _developerWorkstation;
    private Boolean _isDevEnvironment;
    private Boolean _cobranded;
    private Boolean _productionHostname;

    private HttpServletRequest _request;

    private Device _device;
    private SitePreference _sitePreference;

    public static final String MOBILE_SITE_ENABLED_COOKIE_NAME = "mobileSiteEnabled";

    private static String[] PRODUCTION_HOSTNAMES = {"www.greatschools.org","pk.greatschools.org","m.greatschools.org"};

    public RequestInfo() {
    }

    public RequestInfo(HttpServletRequest request) {
        _request = request;
        init();
    }

    public void init() {
        System.out.println("REQUESTINFO BEING INITIALIZED----------------");
        if (_request == null) {
            throw new IllegalArgumentException("Cannot create RequestInfo with null hostname");
        }
        if (_request.getServerName() == null) {
            throw new IllegalArgumentException("Cannot create RequestInfo with a request that contains a null servername");
        }

        _hostname = _request.getServerName();
        _currentSubdomain = UrlUtil.findLowestSubdomain(_hostname);
        _requestURL = UrlUtil.getRequestURL(_request);

        //set up state of HostnameInfo
        _developerWorkstation = UrlUtil.isDeveloperWorkstation(_hostname);
        _isDevEnvironment = UrlUtil.isDevEnvironment(_hostname);
        _onPkSubdomain = _hostname.contains(Subdomain.PK.toString() + ".");
        _productionHostname = org.apache.commons.lang.StringUtils.indexOfAny(_hostname, PRODUCTION_HOSTNAMES) > -1;

        Object sitePreferenceObj = _request.getAttribute(SitePreferenceHandler.CURRENT_SITE_PREFERENCE_ATTRIBUTE);
        if (sitePreferenceObj != null) {
            _sitePreference = (SitePreference) sitePreferenceObj;
        } else {
            _sitePreference = null;
        }
        _request.setAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME, this);
    }

    /******************************************************************************/
    /* Support for mobile site                                                    */
    /******************************************************************************/
    public Device getDevice() {
        if (_device == null) {
            if (_springWurflManager != null) {
                _device = new Device(_springWurflManager.getDeviceForRequest(_request));
            }
        }
        return _device;
    }

    public boolean isMobileSiteEnabled() {
        Cookie cookie = CookieUtil.getCookie(_request, MOBILE_SITE_ENABLED_COOKIE_NAME);
        return (isDevEnvironment() && cookie != null && Boolean.TRUE.equals(Boolean.valueOf(cookie.getValue())));
    }

    public boolean isFromMobileDevice() {
        if (getDevice() == null) return false;
        return getDevice().isMobileDevice();
    }

    /**
     * Gets URL for normal version if on the mobile site, otherwise gets URL for mobile version
     * @return
     */
    public String getSitePreferenceUrlForAlternateSite() {
        String newUrl = getRequestURL();

        if (isDeveloperWorkstation()) {
            if (_sitePreference == SitePreference.MOBILE) {
                newUrl = UrlUtil.putQueryParamIntoUrl(newUrl, "site_preference", SitePreference.NORMAL.toString().toLowerCase());
            } else {
                newUrl = UrlUtil.putQueryParamIntoUrl(newUrl, "site_preference", SitePreference.MOBILE.toString().toLowerCase());
            }
        } else {
            if (isOnMobileSite()) {
                newUrl = getFullUrlAtNewSubdomain(Subdomain.WWW);
                newUrl = UrlUtil.putQueryParamIntoUrl(newUrl, "site_preference", SitePreference.NORMAL.toString().toLowerCase());
            } else {
                newUrl = getFullUrlAtNewSubdomain(Subdomain.MOBILE);
                newUrl = UrlUtil.putQueryParamIntoUrl(newUrl, "site_preference", SitePreference.MOBILE.toString().toLowerCase());
            }
        }

        return newUrl;
    }

    public boolean isOnMobileSite() {
        return _hostname.startsWith(Subdomain.MOBILE.toString() + ".");
    }

    public boolean shouldRenderMobileView() {
        return isMobileSiteEnabled()
            && (isFromMobileDevice() || _sitePreference == SitePreference.MOBILE)
            && _sitePreference != SitePreference.NORMAL;
    }

    /******************************************************************************/
    /* Methods for working with hostnames                                         */
    /******************************************************************************/

    /**
     * Generates a hostname for the target Subdomain. Output depends on what the current request's hostname is.
     * @return generated hostname, otherwise returns current hostname current request is cobranded or is on an
     * unrecognized subdomain, returns current hostname;
     */
    public String getHostnameForTargetSubdomain(Subdomain targetSubdomain) {
        String newHostname = _hostname;

        if (targetSubdomain == null) {
            return _hostname;
        }

        if (!isCobranded() && !_hostname.startsWith(String.valueOf(targetSubdomain) + ".")) {
            if ((targetSubdomain == null || targetSubdomain.equals(Subdomain.WWW))) {
                if (!isProductionHostname()) {
                    //on some servers we just need to remove pk and not replace it with www
                    newHostname = _hostname.replaceFirst(_currentSubdomain + ".", "");
                } else {
                    newHostname = _hostname.replaceFirst(_currentSubdomain + ".", Subdomain.WWW.toString() + ".");
                }
            } else {
                if (isSubdomainSupported(targetSubdomain)) {
                    if (!isProductionHostname()) {
                        newHostname = targetSubdomain.toString() + "." + _hostname;
                    } else {
                        newHostname = _hostname.replaceFirst(_currentSubdomain + ".", targetSubdomain.toString() + ".");
                    }
                }
            }
        }

        return newHostname;
    }

    public boolean isSubdomainSupported(Subdomain targetSubdomain) {
        boolean supported = false;
        switch (targetSubdomain) {
            case PK:
                supported = isPkSubdomainSupported();
                break;
            case MOBILE:
                supported = isMobileSiteEnabled();
                break;
            default:
                supported = true;
        }
        return supported;
    }

    /**
     * The needed base host name (the host where we want relative URLs to direct to)
     * e.g. www.greatschools.org
     * might be different than the hostname if the current hostname contains the pk subdomain: GS-12127
     * @return
     */
    public String getBaseHostname() {

        String baseHostname = _hostname;

        if (isOnPkSubdomain()) {
            if (!isProductionHostname()) {
                //on some servers we just need to remove pk and not replace it with www
                baseHostname = _hostname.replaceFirst(Subdomain.PK.toString() + ".", "");
            } else {
                baseHostname = _hostname.replaceFirst(Subdomain.PK.toString() + ".", Subdomain.WWW.toString() + ".");
            }
        }

        return baseHostname;
    }

    /******************************************************************************/
    /* Support for pk subdomain                                                   */
    /******************************************************************************/

    /**
     * Returns true if this host supports pk subdomain for preschools
     * @return
     */
    public boolean isPkSubdomainSupported() {
        //Developer workstations can set up a virtual host and specify it here
        return (!isDeveloperWorkstation() && !isCobranded());
    }

    /******************************************************************************/
    /* URL generation for the request                                             */
    /******************************************************************************/

    /**
     * @return The full protocol, hostname, and port. e.g.  http://www.greatschools.org
     */
    public String getBaseHost() {
        return _request.getScheme() + "://" + getBaseHostname() + ((_request.getServerPort() != 80) ? ":" + _request.getServerPort() : "");
    }

    public String getRequestURL() {
        return _requestURL;
        }

    public String getFullUrlAtBaseHost() {
        String fullUrl = getBaseHost();
        fullUrl += _request.getContextPath() + _request.getServletPath();
        if (_request.getQueryString() != null) {
            fullUrl += "?" + _request.getQueryString();
        }
        return fullUrl;
    }

    public String getFullUrlAtNewSubdomain(Subdomain subdomain) {
        String newHostname = getHostnameForTargetSubdomain(subdomain);
        String fullUrl = _request.getScheme() + "://" + newHostname + ((_request.getServerPort() != 80) ? ":" + _request.getServerPort() : "");
        fullUrl += _request.getContextPath() + _request.getServletPath();
        if (_request.getQueryString() != null) {
            fullUrl += "?" + _request.getQueryString();
        }
        return fullUrl;
    }

    /******************************************************************************/
    /* Accessors                                                                  */
    /******************************************************************************/

    public String getHostname() {
        return _hostname;
    }

    public boolean isProductionHostname() {
        return _productionHostname;
    }

    /**
     * Find out if the hostname matches list of possible developer workstations
     */
    public boolean isDeveloperWorkstation() {
        return _developerWorkstation;
    }
    
    public boolean isDevEnvironment() {
        return _isDevEnvironment;
    }

    public boolean isOnPkSubdomain() {
        return _onPkSubdomain;
    }

    public boolean isCobranded() {
        if (_cobranded == null) {
            String cobrand = UrlUtil.cobrandFromUrl(_hostname);
            if (cobrand != null) {
                _cobranded = Boolean.valueOf(true);
    }

            // if _cobranded is still null and we're not sure yet:
            if (_cobranded == null) {
                SessionContext sessionContext = SessionContextUtil.getSessionContext(_request);
                if (sessionContext == null) {
                    throw new IllegalStateException("SessionContext should not be null if caller is trying to find out about cobrands");
                }
                _cobranded = sessionContext.isCobranded();
            }
        }

        return Boolean.valueOf(_cobranded);
    }

    public SitePreference getSitePreference() {
        return _sitePreference;
    }

    public HttpServletRequest getRequest() {
        return _request;
    }

    public void setRequest(HttpServletRequest request) {
        _request = request;
}

    public WURFLManager getSpringWurflManager() {
        return _springWurflManager;
    }

    public void setSpringWurflManager(WURFLManager springWurflManager) {
        _springWurflManager = springWurflManager;
    }
}
