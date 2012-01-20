package gs.web.request;

import gs.web.mobile.Device;
import gs.web.mobile.MobileHelper;
import gs.web.mobile.UnknownDevice;
import gs.web.util.CookieUtil;
import gs.web.util.UrlUtil;
import net.sourceforge.wurfl.core.WURFLManager;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceHandler;
import org.springframework.mobile.device.site.SitePreferenceUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.mobile.device.site.SitePreferenceHandler.CURRENT_SITE_PREFERENCE_ATTRIBUTE;

public class RequestInfo {

    public static final String REQUEST_ATTRIBUTE_NAME = "requestInfo";

    private HttpServletRequest _request;

    private WURFLManager _springWurflManager;

    private Device _device;
    private SitePreference _sitePreference;

    public static final String MOBILE_SITE_ENABLED_COOKIE_NAME = "mobileSiteEnabled";

    private HostData _hostData;

    public RequestInfo() {
    }

    public RequestInfo(HttpServletRequest request) {
        setRequest(request);
        init();
    }

    public void init() {
        _hostData = new HostData(_request);
        if (_springWurflManager != null) {
            _device = new Device(_springWurflManager.getDeviceForRequest(_request));
        } else {
            _device = new Device(new UnknownDevice());
        }
        _request.setAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME, this);
    }
    
    public static RequestInfo getRequestInfo(HttpServletRequest request) {
        RequestInfo requestInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);
        if (requestInfo == null) {
            requestInfo = new RequestInfo(request);
        }
        return requestInfo;
    }

    /******************************************************************************/
    /* Support for mobile site                                                    */
    /******************************************************************************/

    public Device getDevice() {
        return _device;
    }

    public boolean isMobileSiteEnabled() {
        Cookie cookie = CookieUtil.getCookie(_hostData.getRequest(), MOBILE_SITE_ENABLED_COOKIE_NAME);
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
        Subdomain subdomainToSwitchTo;
        SitePreference chosenSitePreference;

        if (isOnMobileSite()) {
            subdomainToSwitchTo = Subdomain.WWW;
            chosenSitePreference = SitePreference.NORMAL;
        } else {
            subdomainToSwitchTo = Subdomain.MOBILE;
            chosenSitePreference = SitePreference.MOBILE;
        }
        String newHostname = getHostnameForTargetSubdomain(subdomainToSwitchTo);

        if (!isDeveloperWorkstation()) {
            newUrl = UrlUtil.getRequestURLAtNewHostname(_hostData.getRequest(), newHostname);
        }
        newUrl = UrlUtil.putQueryParamIntoUrl(newUrl, MobileHelper.SITE_PREFERENCE_KEY_NAME, chosenSitePreference.toString().toLowerCase());

        return newUrl;
    }

    public boolean isOnMobileSite() {
        return _hostData.getHostname().startsWith(Subdomain.MOBILE.toString() + ".");
    }

    public boolean shouldRenderMobileView() {
        return isMobileSiteEnabled()
            && (isFromMobileDevice() || getSitePreference() == SitePreference.MOBILE)
            && getSitePreference() != SitePreference.NORMAL;
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
        String newHostname = _hostData.getHostname();

        if (targetSubdomain == null) {
            return _hostData.getHostname();
        }

        if (!isCobranded() && !_hostData.getHostname().startsWith(String.valueOf(targetSubdomain) + ".")) {
            if ((targetSubdomain == null || targetSubdomain.equals(Subdomain.WWW))) {
                if (!isProductionHostname()) {
                    //on some servers we just need to remove pk and not replace it with www
                    newHostname = _hostData.getHostname().replaceFirst(_hostData.getCurrentSubdomain() + ".", "");
                } else {
                    newHostname = _hostData.getHostname().replaceFirst(_hostData.getCurrentSubdomain() + ".", Subdomain.WWW.toString() + ".");
                }
            } else {
                if (isSubdomainSupported(targetSubdomain)) {
                    if (!isProductionHostname()) {
                        newHostname = targetSubdomain.toString() + "." + _hostData.getHostname();
                    } else {
                        newHostname = _hostData.getHostname().replaceFirst(_hostData.getCurrentSubdomain() + ".", targetSubdomain.toString() + ".");
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

        String baseHostname = _hostData.getHostname();

        if (isOnPkSubdomain()) {
            if (!isProductionHostname()) {
                //on some servers we just need to remove pk and not replace it with www
                baseHostname = _hostData.getHostname().replaceFirst(Subdomain.PK.toString() + ".", "");
            } else {
                baseHostname = _hostData.getHostname().replaceFirst(Subdomain.PK.toString() + ".", Subdomain.WWW.toString() + ".");
            }
        }

        return baseHostname;
    }

    /**
     * Returns a host string (e.g. http://www.greatschools.org) at the base hostname.
     * Base hostname is the hostname where relative paths should resolve to.
     * For example, we might want relative paths on http://pk.greatschools.org to resolve to
     * http://www.greatschools.org
     *
     * @return A host string like http://www.greatschools.org or http://dev.greatschools.org:8080
     */
    public String getBaseHost() {
        String baseHostname = getBaseHostname();
        return UrlUtil.getHostAtNewHostname(_request, baseHostname);
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
    /* Delegate methods                                                           */
    /******************************************************************************/

    public String getRequestURL() {
        return _hostData.getRequestURL();
    }

    public String getHostname() {
        return _hostData.getHostname();
    }

    public boolean isProductionHostname() {
        return _hostData.isProductionHostname();
    }

    public boolean isDeveloperWorkstation() {
        return _hostData.isDeveloperWorkstation();
    }
    
    public boolean isDevEnvironment() {
        return _hostData.isDevEnvironment();
    }

    public boolean isOnPkSubdomain() {
        return _hostData.isOnPkSubdomain();
    }

    public boolean isCobranded() {
        return _hostData.isCobranded();
    }

    /******************************************************************************/
    /* Accessors                                                                  */
    /******************************************************************************/

    public SitePreference getSitePreference() {
        Cookie[] cookies = _request.getCookies();
        String site_preference = null;
        SitePreference sitePreference = null;

        site_preference = _request.getParameter("site_preference");

        if (site_preference == null && cookies != null) {
            // hack alert: the interceptor which spring-mobile provides does not execute before spring uses DeviceSpecificControllerFactory
            // to get the appropriate controllers for beans which use that factory. The factory needs to know what the site preference is
            // TODO: figure out better way to accomplish this
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("org.springframework.mobile.device.site.CookieSitePreferenceRepository.SITE_PREFERENCE")) {
                    site_preference = cookie.getValue();
                }
            }
        }

        if (SitePreference.MOBILE.toString().equalsIgnoreCase(site_preference)) {
            sitePreference = SitePreference.MOBILE;
        } else if (SitePreference.NORMAL.toString().equalsIgnoreCase(site_preference)) {
            sitePreference = SitePreference.NORMAL;
        }

        return sitePreference;
    }

    public WURFLManager getSpringWurflManager() {
        return _springWurflManager;
    }

    public void setSpringWurflManager(WURFLManager springWurflManager) {
        _springWurflManager = springWurflManager;
    }

    public HttpServletRequest getRequest() {
        return _request;
    }

    public void setRequest(HttpServletRequest request) {
        _request = request;
    }

    public HostData getHostData() {
        return _hostData;
    }
}