package gs.web.request;

import gs.web.mobile.Device;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class HostData {

    private final HttpServletRequest _request;
    private final String _requestURL;

    // hostname / subdomain info
    private final String _hostname;
    private final String _currentSubdomain;
    private final Boolean _onPkSubdomain;

    // environment info
    private final Boolean _developerWorkstation;
    private final Boolean _isDevEnvironment;
    private final Boolean _isNonQaDevEnvironment;
    private final Boolean _productionHostname;

    private final Boolean _cobranded;

    private static String[] PRODUCTION_HOSTNAMES = {"www.greatschools.org","pk.greatschools.org","m.greatschools.org","api.greatschools.org"};

    public static Subdomain[] CONTENT_SUBDOMAINS = {Subdomain.WWW, Subdomain.PK, Subdomain.MOBILE};

    public HostData(HttpServletRequest request) {
        _hostname = request.getServerName();
        _currentSubdomain = UrlUtil.findLowestSubdomain(request.getServerName());
        _requestURL = UrlUtil.getRequestURL(request);

        _developerWorkstation = UrlUtil.isDeveloperWorkstation(request.getServerName());
        _isDevEnvironment = UrlUtil.isDevEnvironment(request.getServerName());
        _isNonQaDevEnvironment = UrlUtil.isDevEnvironment(request.getServerName()) && !UrlUtil.isQAServer(request.getServerName());

        _onPkSubdomain = request.getServerName().contains(Subdomain.PK.toString() + ".");
        _productionHostname = StringUtils.indexOfAny(request.getServerName(), PRODUCTION_HOSTNAMES) > -1;

        // Determine if this is a cobrand (copied from SessionContextUtil)
        String cobrand = null;
        String paramCobrand = request.getParameter(SessionContextUtil.COBRAND_PARAM);
        if (StringUtils.isNotEmpty(paramCobrand)) {
            if (!paramCobrand.equalsIgnoreCase("www")) {
                cobrand = paramCobrand;
            }
        } else {
            cobrand = UrlUtil.cobrandFromUrl(request.getServerName());
        }
        _cobranded = cobrand != null;

        _request = request;
    }

    public String getHostname() {
        return _hostname;
    }

    public String getCurrentSubdomain() {
        return _currentSubdomain;
    }

    public String getRequestURL() {
        return _requestURL;
    }

    public boolean isOnPkSubdomain() {
        return _onPkSubdomain;
    }

    public boolean isDeveloperWorkstation() {
        return _developerWorkstation;
    }

    public boolean isDevEnvironment() {
        return _isDevEnvironment;
    }

    public boolean isNonQaDevEnvironment() {
        return _isNonQaDevEnvironment;
    }

    public boolean isCobranded() {
        return _cobranded;
    }

    public boolean isProductionHostname() {
        return _productionHostname;
    }

    public HttpServletRequest getRequest() {
        return _request;
    }

}