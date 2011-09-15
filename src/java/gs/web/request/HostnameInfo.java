package gs.web.request;

import gs.web.util.UrlUtil;

public class HostnameInfo {

    public static final String REQUEST_ATTRIBUTE_NAME = "hostnameInfo";

    private final String _hostname;

    private Boolean _onPkSubdomain;
    private Boolean _developerWorkstation;
    private Boolean _isDevEnvironment;
    private Boolean _cobranded;

    public HostnameInfo(String hostname) {
        if (hostname == null) {
            throw new IllegalArgumentException("Cannot create HostnameInfo with null hostname");
        }

        _hostname = hostname;

        //set up state of HostnameInfo
        _developerWorkstation = UrlUtil.isDeveloperWorkstation(hostname);
        _isDevEnvironment = UrlUtil.isDevEnvironment(hostname);
        _onPkSubdomain = hostname.contains(Subdomain.PK.toString() + ".");

        String cobrand = UrlUtil.cobrandFromUrl(hostname);
        _cobranded = cobrand != null;
    }

    /**
     * The needed base URL (the host where we want relative URLs to direct to)
     * might be different than the hostname if the current hostname contains the pk subdomain: GS-12127
     * @return
     */
    public String getBaseHostname() {

        String baseHostname = _hostname;

        if (isOnPkSubdomain()) {
            //TODO: remove check for pk.localhost.com
            if (isDevEnvironment() && !_hostname.contains("pk.localhost.com")) {
                //on some servers we just need to remove pk and not replace it with www
                baseHostname = _hostname.replaceFirst(Subdomain.PK.toString() + ".", "");
            } else {
                baseHostname = _hostname.replaceFirst(Subdomain.PK.toString() + ".", Subdomain.WWW.toString() + ".");
            }
        }
        
        return baseHostname;
    }

    /**
     * Creates a hostname as it should appear for preschool pages hosted from our pk. domain
     * @return
     */
    public String getHostnameForPkSubdomain() {
        String hostname = _hostname;

        if (!isOnPkSubdomain() && isPkSubdomainSupported()) {
            if (isDevEnvironment() && !_hostname.contains("www.localhost.com")) {
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

    public String getHostname() {
        return _hostname;
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

}
