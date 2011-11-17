package gs.web.request;

import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceHandler;

import javax.servlet.http.HttpServletRequest;

public class RequestInfo {

    public static final String REQUEST_ATTRIBUTE_NAME = "requestInfo";

    private final String _hostname;

    private Boolean _onPkSubdomain;
    private Boolean _developerWorkstation;
    private Boolean _isDevEnvironment;
    private Boolean _cobranded;
    private Boolean _productionHostname;
    private HttpServletRequest _request;
    private Device _device;
    private SitePreference _sitePreference;

    private static String[] PRODUCTION_HOSTNAMES = {"www.greatschools.org","pk.greatschools.org"};

    public RequestInfo(HttpServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Cannot create RequestInfo with null hostname");
        }
        if (request.getServerName() == null) {
            throw new IllegalArgumentException("Cannot create RequestInfo with a request that contains a null servername");
        }

        _request = request;
        _hostname = request.getServerName();

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
        String activate = _request.getParameter("activateMobileSite");
        return ((isDevEnvironment() || _hostname.indexOf("localhost.com") > -1) && "true".equalsIgnoreCase(activate));
    }

    public boolean isFromMobileDevice() {
        return _device.isMobile();
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

        if (!isCobranded()) {
            if ((targetSubdomain == null || targetSubdomain.equals(Subdomain.WWW)) && isOnPkSubdomain()) {
                newHostname = getBaseHostname();
            } else if (Subdomain.PK.equals(targetSubdomain)) {
                newHostname = getHostnameForPkSubdomain();
            } else {
                newHostname = UrlUtil.overwriteSubdomain(_hostname, targetSubdomain.toString());
            }
        }

        return newHostname;
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
            //TODO: remove check for pk.localhost.com
            if (!isProductionHostname() && !_hostname.contains("pk.localhost.com")) {
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
     * Creates a hostname as it should appear for preschool pages hosted from our pk. domain
     * @return
     */
    public String getHostnameForPkSubdomain() {
        String hostname = _hostname;

        if (!isOnPkSubdomain() && isPkSubdomainSupported()) {
            if (!isProductionHostname() && !_hostname.contains("www.localhost.com")) {
                hostname = Subdomain.PK.toString() + "." + _hostname;
            } else if (!isCobranded()) {
                hostname = _hostname.replaceFirst(Subdomain.WWW.toString() + ".", Subdomain.PK.toString() + ".");
            }
        }
        
        return hostname;
    }

    /**
     * Returns true if this host supports pk subdomain for preschools
     * @return
     */
    public boolean isPkSubdomainSupported() {
        //Developer workstations can set up a virtual host and specify it here
        return (!isDeveloperWorkstation() && !isCobranded()) || _hostname.contains(".localhost.com");
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
