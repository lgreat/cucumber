package gs.web.request;

import gs.web.util.CookieUtil;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceHandler;
import org.springframework.mobile.device.site.SitePreferenceHandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class RequestInfo {

    public static final String REQUEST_ATTRIBUTE_NAME = "requestInfo";

    private final String _hostname;
    private final String _currentSubdomain;

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

    public RequestInfo(HttpServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Cannot create RequestInfo with null hostname");
        }
        if (request.getServerName() == null) {
            throw new IllegalArgumentException("Cannot create RequestInfo with a request that contains a null servername");
        }

        _request = request;
        _hostname = request.getServerName();
        _currentSubdomain = UrlUtil.findLowestSubdomain(_hostname);

        //set up state of HostnameInfo
        _developerWorkstation = UrlUtil.isDeveloperWorkstation(_hostname);
        _isDevEnvironment = UrlUtil.isDevEnvironment(_hostname);
        _onPkSubdomain = _hostname.contains(Subdomain.PK.toString() + ".");
        _productionHostname = org.apache.commons.lang.StringUtils.indexOfAny(_hostname, PRODUCTION_HOSTNAMES) > -1;

        String cobrand = UrlUtil.cobrandFromUrl(_hostname);
        SessionContext sessionContext = SessionContextUtil.getSessionContext(_request);
        _cobranded = cobrand != null || sessionContext.isCobranded();

        _device = DeviceUtils.getCurrentDevice(request);

        Object sitePreferenceObj = request.getAttribute(SitePreferenceHandler.CURRENT_SITE_PREFERENCE_ATTRIBUTE);
        if (sitePreferenceObj != null) {
            _sitePreference = (SitePreference) sitePreferenceObj;
        } else {
            _sitePreference = null;
        }
    }

    /******************************************************************************/
    /* Support for mobile site                                                    */
    /******************************************************************************/

    public boolean isMobileSiteEnabled() {
        Cookie cookie = CookieUtil.getCookie(_request, MOBILE_SITE_ENABLED_COOKIE_NAME);
        return (isDevEnvironment() && cookie != null && Boolean.TRUE.equals(Boolean.valueOf(cookie.getValue())));
    }

    public boolean isFromMobileDevice() {
        return _device != null && _device.isMobile();
    }

    /**
     * Gets URL for normal version if on the mobile site, otherwise gets URL for mobile version
     * @return
     */
    public String getSitePreferenceUrlForAlternateSite() {
        String newUrl;
        if (isOnMobileSite()) {
            newUrl = getFullUrlAtNewSubdomain(Subdomain.WWW);
            newUrl = UrlUtil.putQueryParamIntoUrl(newUrl, "site_preference", SitePreference.NORMAL.toString().toLowerCase());
        } else {
            newUrl = getFullUrlAtNewSubdomain(Subdomain.MOBILE);
            newUrl = UrlUtil.putQueryParamIntoUrl(newUrl, "site_preference", SitePreference.MOBILE.toString().toLowerCase());
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
        return _cobranded;
    }

    public SitePreference getSitePreference() {
        return _sitePreference;
    }
}
