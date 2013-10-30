/*
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: UrlUtil.java,v 1.129 2012/10/10 23:02:52 yfan Exp $
 */

package gs.web.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import gs.data.state.State;
import gs.data.util.CdnUtil;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Wrapping and URL munging tools.
 * These are distinct from the GSData ones, which are general utilities. These have to do
 * with the specific GSWeb environment, especially the session facade.
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 * @see org.springframework.web.util.WebUtils
 */
public final class UrlUtil {
    private static final Log _log = LogFactory.getLog(UrlUtil.class);

    public static String putQueryParamIntoQueryString(String queryString, String key, String value, boolean overwrite) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        Multimap<String, String> keysValues = getParamsFromQueryStringPreserveAll(queryString);

        if (overwrite) {
            keysValues.removeAll(key);
        }

        keysValues.put(key,value);

        String newQueryString = getQueryStringFromMultiMap(keysValues);

        return newQueryString;
    }

    public static String putQueryParamIntoQueryString(String queryString, String key, String value) {
        return putQueryParamIntoQueryString(queryString, key, value, true);
    }

    public static String putQueryParamIntoUrl(String url, String key, String value, boolean overwrite) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (url == null) {
            throw new IllegalArgumentException("Url cannot be null");
        }

        String[] parts = url.split("\\?");

        String base = parts[0];
        String queryString = "";
        if (parts.length > 1) {
            queryString = parts[1];
        }

        queryString = putQueryParamIntoQueryString(queryString, key, value, overwrite);

        String newUrl = base;
        if (queryString.length() > 0) {
            newUrl += "?" + queryString;
        }

        return newUrl;
    }

    public static String putQueryParamIntoUrl(String url, String key, String value) {
        return putQueryParamIntoUrl(url, key, value, true);
    }

    /**
     * Returns a map of param name to param value extracted from the provided query string.
     * The query string should not start with a question mark!
     */
    public static Map<String, String> getParamsFromQueryString(String queryString) {
        Map<String, String> params = new HashMap<String, String>();
        if (StringUtils.isNotBlank(queryString)) {
            queryString = queryString.replaceAll("&amp;", "&");
            String[] nameValuePairs = queryString.split("&");
            for (String nameValuePair: nameValuePairs) {
                String[] nameAndValue = nameValuePair.split("=");
                if (nameAndValue.length > 1) {
                    try {
                        params.put(nameAndValue[0], URLDecoder.decode(nameAndValue[1], "UTF-8"));
                    } catch (UnsupportedEncodingException uee) {
                        _log.warn("Can't decode param: " + nameValuePair);
                    }
                }
            }
        }
        return params;
    }

    /**
     * Returns a map of param name to param value extracted from the provided query string.
     * The query string should not start with a question mark! Works with query strings that contain multiple pairs
     * with the same key
     */
    public static Multimap<String, String> getParamsFromQueryStringPreserveAll(String queryString) {
        Multimap<String, String> params = TreeMultimap.create();
        if (StringUtils.isNotBlank(queryString)) {
            queryString = queryString.replaceAll("&amp;", "&");
            String[] nameValuePairs = queryString.split("&");
            for (String nameValuePair: nameValuePairs) {
                String[] nameAndValue = nameValuePair.split("=");
                if (nameAndValue.length > 1) {
                    try {
                        params.put(nameAndValue[0], URLDecoder.decode(nameAndValue[1], "UTF-8"));
                    } catch (UnsupportedEncodingException uee) {
                        _log.warn("Can't decode param: " + nameValuePair);
                    }
                }
            }
        }
        return params;
    }

    public static String removeParamsFromQueryString(String queryString, String... keys) {
        Multimap<String, String> map = getParamsFromQueryStringPreserveAll(queryString);

        Set<String> keySet = map.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String mapKey = iterator.next();
            for (String key : keys) {
                if (mapKey.equalsIgnoreCase(key)) {
                    iterator.remove();
                }
            }
        }

        return getQueryStringFromMultiMap(map);
    }

    public static String removeQueryParamsFromUrl(String url, String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (url == null) {
            throw new IllegalArgumentException("Url cannot be null");
        }

        String[] parts = url.split("\\?");

        String base = parts[0];
        String queryString = "";
        if (parts.length > 1) {
            queryString = parts[1];
        }

        queryString = removeParamsFromQueryString(queryString, key);

        String newUrl = base;
        if (queryString.length() > 0) {
            newUrl += "?" + queryString;
        }

        return newUrl;
    }

    /**
     * Creates a queryString from a map of key-value pairs. Question mark not included.
     * @param keysAndValues
     * @return
     */
    public static String getQueryStringFromMap(Map<String,String> keysAndValues) {
        StringBuffer queryString = new StringBuffer();

        for (Map.Entry<String,String> entry : keysAndValues.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key != null && value != null) {
                if (queryString.length() > 0) {
                    queryString.append("&");
                }
                try {
                    queryString.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
                } catch (UnsupportedEncodingException uee) {
                    _log.warn("Can't encode value: " + value);
                }
            }
        }

        return queryString.toString();
    }

    /**
     * Creates a queryString from a map of key-value pairs. Question mark not included.
     * @param keysAndValues
     * @return
     */
    public static String getQueryStringFromMultiMap(Multimap<String, String> keysAndValues) {
        StringBuffer queryString = new StringBuffer();

        for (Map.Entry<String,String> entry : keysAndValues.entries()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key != null && value != null) {
                if (queryString.length() > 0) {
                    queryString.append("&");
                }
                try {
                    queryString.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
                } catch (UnsupportedEncodingException uee) {
                    _log.warn("Can't encode value: " + value);
                }
            }
        }

        return queryString.toString();
    }

    /**
     * Gets the host and path portion of the URL the client used when initiating the request, before any request
     * dispatching has taken place.
     * i.e. Handles includes and forwards, which HttpServletRequest.getRequestURL no longer does due to servlet spec
     *
     * e.g. https://www.greatschools.org:443/california/?decorator=none ==> https://www.greatschools.org/california/
     *
     * @param request
     * @return
     */
    public static String getRequestHostAndPath(HttpServletRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Provided request was null");
        }

        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        // handle forwards
        String requestUri = (String)request.getAttribute("javax.servlet.forward.request_uri");

        // handle includes
        if (requestUri == null) {
            requestUri = (String)request.getAttribute("javax.servlet.include.request_uri");
        }

        // handle default case
        if (requestUri == null) {
            requestUri = request.getRequestURI();
        }

        StringBuffer buffer = buildHostAndPortString(scheme, serverName, serverPort);

        buffer.append(requestUri);

        return buffer.toString();
    }

    /**
     * Builds a host string with protocol + hostname + port number. Decides whether or not to include port number
     * based on the scheme (protocol)
     *
     * @param scheme e.g. http | https
     * @param hostname e.g. www.greatschools.org
     * @param port the port number
     * @return
     */
    public static StringBuffer buildHostAndPortString(String scheme, String hostname, int port) {
        if (StringUtils.isBlank(scheme)) {
            throw new IllegalArgumentException("Schema was null or blank. Cannot build host string.");
        }
        if (StringUtils.isBlank(hostname)) {
            throw new IllegalArgumentException("Hostname was null or blank. Cannot build host string.");
        }
        if (port < 1) {
            throw new IllegalArgumentException("Port number was invalid. Cannot build host string.");
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(scheme);
        buffer.append("://");
        buffer.append(hostname);

        //if not http:80 or https:443, then add the port number
        if (
            !(scheme.equalsIgnoreCase("http") && port == 80) &&
            !(scheme.equalsIgnoreCase("https") && port == 443)
        ) {
            buffer.append(":").append(String.valueOf(port));
        }
        return buffer;
    }
    
    public static StringBuffer buildHostAndPortString(HttpServletRequest request) {
        return buildHostAndPortString(request.getScheme(), request.getServerName(), request.getServerPort());
    }

    /**
     * Given a request and new hostname, builds the host portion of URL (minus path and query params).
     * Users the same scheme and port as the given request, but overrides with hostname with the provided newHostname
     *
     * @param request
     * @param newHostname
     * @return
     */
    public static String getHostAtNewHostname(HttpServletRequest request, String newHostname) {
        String scheme = request.getScheme();
        int serverPort = request.getServerPort();

        StringBuffer buffer = buildHostAndPortString(scheme, newHostname, serverPort);

        return buffer.toString();
    }

    /**
     * Gets the URL the client used when initiating the request, before any request dispatching has taken place.
     * i.e. Handles includes and forwards, which HttpServletRequest.getRequestURL no longer does due to servlet spec
     *
     * @param request
     * @return
     */
    public static String getRequestURL(HttpServletRequest request) {

        String url = getRequestHostAndPath(request);

        // handle forwards
        String queryString = (String) request.getAttribute("javax.servlet.forward.query_string");

        // handle includes
        if (queryString == null) {
            queryString = (String) request.getAttribute("javax.servlet.include.query_string");
        }

        // handle default case
        if (queryString == null) {
            queryString = request.getQueryString();
        }

        if (queryString != null) {
            url = url + "?" + queryString;
        }

        return url;
    }

    public static String getRequestURLAtNewHostname(HttpServletRequest request, String newHostname) {
        if (newHostname == null) {
            throw new IllegalArgumentException("new hostname cannot be null.");
        }
        String url = getRequestURL(request);
        url = url.replaceFirst(request.getServerName(), newHostname);
        return url;
    }

    /**
     * Given a hostname, extracts the "highest" subdomain if present. e.g.  abc.xyz.example.com => xyz
     * Is null-safe
     * @param hostname
     * @return subdomain if present in hostname. Otherwise null
     */
    public static String findHighestSubdomain(String hostname) {
        if (hostname == null || hostname.length() == 0) {
            return null;
        }

        String[] hostnameTokens = StringUtils.split(hostname, "\\.");

        if (hostnameTokens.length >= 3) {
            int subdomainPosition = hostnameTokens.length - 3; //subdomain is the third-to-the-last position
            return hostnameTokens[subdomainPosition];
        } else {
            return null;
        }
    }


    /**
     * Given a hostname, extracts the "lowest" subdomain if present. e.g.  abc.xyz.example.com => abc
     * Is null-safe
     * @param hostname
     * @return subdomain if present in hostname. Otherwise null
     */
    public static String findLowestSubdomain(String hostname) {
        if (hostname == null || hostname.length() == 0) {
            return null;
        }

        String[] hostnameTokens = StringUtils.split(hostname, "\\.");

        if (hostnameTokens.length >= 3) {
            return hostnameTokens[0];
        } else {
            return null;
        }
    }

    /**
     * Given a hostname, finds and overwrites the "highest" subdomain if present, and returns a new hostname.
     * If no subdomain exists, prepends the given subdomain and returns a new hostname.
     * @param hostname must contain domain and top-level domain
     * @param newSubdomain the subdomain to use as a replacement
     * @return
     */
    public static String overwriteSubdomain(String hostname, String newSubdomain) {
        return overwriteSubdomain(hostname, newSubdomain, 0);
    }

    /**
     * Given a hostname, finds and overwrites the subdomain at the given level if present, and returns a new hostname.
     * If no subdomain exists at the given level, prepends the given subdomain and returns a new hostname.
     * @param hostname must contain domain and top-level domain
     * @param level zero-based position of subdomain to replace.  e.g.  blah.dev.greatschools.org : blah = position 1
     * @return
     */
    public static String overwriteSubdomain(String hostname, String newSubdomain, int level) {
        if (newSubdomain == null) {
            throw new IllegalArgumentException("newSubdomain is null");
        }

        String[] hostnameTokens = StringUtils.split(hostname, ".");

        //subdomain is the third-to-the-last position:  www.example.com
        if (hostnameTokens.length >= (3 + level)) {
            int subdomainPosition = hostnameTokens.length - (3 + level);
            hostnameTokens[subdomainPosition] = newSubdomain;
        } else if (hostnameTokens.length == (2 + level)) {
            hostnameTokens[0] = newSubdomain + "." + hostnameTokens[0];
        } else {
            return hostname;
        }

        hostname = StringUtils.join(hostnameTokens, '.');

        return hostname;
    }

    public static final String[] COBRAND_IGNORE_URL_PREFIXES = {
        "secure",
        "res1",
        "res2",
        "res3",
        "res4",
        "res5",
        "res6",
        "res7",
        "qa.",
        "sw1-pub.",
        "sw1-pre.",
        "sw2-pub.",
        "sw2-pre.",
        "app1a",
        "app1b",
        "app1c",
        "app1d",
        "app1e",
        "app1f",
        "app2a",
        "app2b",
        "app2c",
        "app2d",
        "app2e",
        "app2f",
        "staging",
        "maddy",
        "admin",
        "clone",
        "crufty",
        "dev",
        "localhost",
        "www.localhost",
        "pk.",
        "main.dev",
        "chriskimm.dev",
        "aroy.office",
        "aroy.dev",
        "droy.dev",
        "profile.dev",
        "cpickslay.",
        "editorial.",
        "127.0.0.1",
        "192.168.",
        "172.21.1.142",
        "172.18.",
        "m.",
        "samson.",
        "mitchtest.",
        "rcox.office"
    };

    /**
     * Given a hostname, extracts the cobrand, or what looks like
     * a cobrand from it. Returns null for no cobrand.
     */
    public static String cobrandFromUrl(String hostName) {
        String cobrandName = null;

        String[] urlTokens = hostName.split("\\.");

        boolean isCobrand =
                !("www".equals(urlTokens[0]) && hostName.indexOf("greatschools") > -1)
                && !gs.data.util.string.StringUtils.startsWithAny(hostName, COBRAND_IGNORE_URL_PREFIXES)
                && !(hostName.indexOf("vpn.greatschools.org") != -1)
                && urlTokens.length >= 3;

        if (isCobrand) {
            cobrandName = urlTokens[0];
            // Need special cases for greatschools.cobrand.com like babycenter
            // or schoolrankings.nj.com (GS-11466)
            if ("greatschools".equals(urlTokens[0]) || "schoolrankings".equals(urlTokens[0])) {
                cobrandName = urlTokens[1];
            // Need special case for cobrands like www.fresno.schools.net
            } else if (hostName.startsWith("www")) {
                cobrandName = urlTokens[1];
            }
        }
        return cobrandName;
    }

    /**
     * Create the correct perl hostname, most useful in the development environment.
     */
    public String buildPerlHostName(String hostName, String cobrand) {
        if (StringUtils.contains(hostName, "localhost")) {
            String dev = "dev.greatschools.org";
            return (cobrand == null) ? dev : cobrand + "." + dev;

            // Else if it's the main website but with the cobrand parameter passed
            // then we return the full cobrand URL
        } else if (cobrand != null &&
                (hostName.startsWith("www") ||
                        hostName.startsWith("staging") ||
                        hostName.startsWith("dev"))) {
            // dev.greatschools.org?cobrand=sfgate -> sfgate.dev.greatschools.org
            String hn = cobrand + "." + hostName;
            // azcentral.www.greatschools.org -> azcentral.greatschools.org
            return hn.replaceFirst(".www.", ".");
        } else if (hostName.startsWith("secure")) {
            // Secure pages link back to www.greatschools.org if we didn't come from a cobrand
            return "www.greatschools.org";
        } else {
            return hostName;
        }

    }

    /**
     * Examines the dest URL and returns a URL that will work from the given
     * src location. It attempts to deliver the smallest possible URL
     * that will work.
     * This is a low-level function, and you may want to use {@link #buildUrl(String,javax.servlet.http.HttpServletRequest)}
     * instead.
     *
     * @param destHost     null or cobrand.gs.net when on secure.gs.net
     * @param destPath     the site-relative link to the destination page or resource
     * @param isDestSecure should the dest page be access via https?
     * @param srcUri       the current location. Should be a Java page.
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    public String buildHref(String destHost, String destPath, boolean isDestSecure, String srcUri, boolean allowCdn) {


        if (srcUri == null) {
            _log.warn("Unable to interpret current page 'null' as URL where destHost = " + destHost +
                    " and destPath = " + destPath);
            return destPath; // no logic to do, but not a good case
        }

//        _log.debug("dest=" + destPath + " isDestSecure?" + isDestSecure + " src=" + srcUri);

        try {
            URL sourceUrl = new URL(srcUri);
            if (isDeveloperWorkstation(sourceUrl.getHost())) {
                if (destPath.contains("cgi-bin") || destPath.contains("modperl")
                        || destPath.startsWith("/css/") || destPath.startsWith("/js/")) {
                    return "http://dev.greatschools.org" + destPath;
                } else {
                    return destPath;
                }
            } else {
                if ("https".equals(sourceUrl.getProtocol())) {
                    String host = sourceUrl.getHost();
                    if (destHost != null) {
                        host = destHost;
                    }
                    boolean destIsPerl = smellsLikePerl(destPath);
                    if (destIsPerl) {
                        // With Perl pulling modperl/promos is an exception because it's
                        // used for pulling images for tracking
                        if (destPath.indexOf("modperl/promos") > -1) {
                            return destPath;
                        } else {
                            return "http://" + host + destPath;
                        }
                    } else {
                        if (isDestSecure) {
                            return destPath;
                        } else {
                            return "http://" + host + destPath;
                        }
                    }
                }
                // Anywhere but a developer workstation and we should be able to use the relative
                // link.
                if (allowCdn && CdnUtil.isCdnEnabled() && destPath.startsWith("/")) {
                    destPath = CdnUtil.getCdnPrefix() + destPath;
                }
                return destPath;
            }

        } catch (MalformedURLException e) {
            _log.warn("Unable to interpret current page as URL", e);
            return destPath;
        }
    }

    public String buildUrl(final String ref, HttpServletRequest request) {
        return buildUrl(ref, request, false);
    }

    /**
     * Generates a url from the given requested resource. Common bottleneck for URL
     * building.
     * This routine can grow as we decide to do more or less with URLs.
     * It does substitution of $VARIABLES, per Perl world. The current variables
     * supported are:
     * <ul>
     * <li>$STATE - replaced with uppercase state abbreviation</li>
     * <li>$LCSTATE - replaced with lowercase state abbreviation</li>
     * </ul>
     * This variable is taken from the request attribute named of the same name,
     * and if not found there, it uses the SessionContext available from the
     * request.
     * It will add the appropriate server name if this is a link to a perl page.
     * This is most helpful in the dev environment, but this bottleneck allows us
     * to use the same code for test and deploy environments.
     * It does not guarantee to build the smallest possible URL, but it attempts
     * to do so.
     */
    public String buildUrl(final String ref, HttpServletRequest request, boolean allowCdn) {
        String href = ref;

        // Fully qualified URLs don't get any treatment here escape white space
        if (href.startsWith("http:")) {
            href = href.replaceAll("\\s+", "+");
            return href;
        }

        // If the application is deployed under say /gs-web instead of /
        if (href.startsWith("/") && request.getContextPath().length() > 1 &&
                !smellsLikePerl(href)) {
            href = request.getContextPath() + href;
        }

        // If the URL has a STATE string in it (or more than one), replace it with the
        // user's state.
        gs.web.util.context.SessionContext context = SessionContextUtil.getSessionContext(request);
        if (href.indexOf("STATE") != -1) {
            // Allow a request attribute to override the session facade.
            if (request.getAttribute("STATE") != null &&
                    request.getAttribute("STATE") instanceof State) {
                State s = (State) request.getAttribute("STATE");
                String sa = s.getAbbreviation();
                href = href.replaceAll("\\$STATE", sa);
                href = href.replaceAll("\\$LCSTATE", sa.toLowerCase());
            } else {
                gs.data.state.State s = context.getStateOrDefault();
                String sa = s.getAbbreviation();
                href = href.replaceAll("\\$STATE", sa);
                href = href.replaceAll("\\$LCSTATE", sa.toLowerCase());
            }
        }

        if (href.indexOf("RELEASE_NUMBER") != -1) {
            // Allow a request attribute to override the session facade.
            if (request.getAttribute("RELEASE_NUMBER") != null) {
                String release_number =  (String) request.getAttribute("RELEASE_NUMBER");
                href = href.replaceAll("\\$RELEASE_NUMBER", release_number);
            }
        }

        if (href.indexOf("HOST") != -1) {
            // Allow a request attribute to override the session facade.
            if (request.getAttribute("HOST") != null) {
                String h = (String) request.getAttribute("HOST");
                href = href.replaceAll("\\$HOST", h);
            } else {
                String s = context.getHostName();
                href = href.replaceAll("\\$HOST", s);
            }
        }

        final String src = request.getRequestURL().toString();

        boolean secureDest = false;
        if ("https".equals(request.getScheme()) &&
                (ref.indexOf("subscribe.page") > -1 || ref.indexOf("thankyou.page") > -1
                        || ref.endsWith(".css") || ref.endsWith(".js") || ref.endsWith(".gif")
                        || ref.endsWith(".jpg") || ref.endsWith(".jpeg"))) {
            secureDest = true;
        }

        href = href.replaceAll("\\s+", "+");

        return buildHref(context != null ? context.getHostName() : null, href, secureDest, src, allowCdn);
    }


    /**
     * Returns true if it looks like the given resource needs to be sought from
     * the perl site. This is useful in building out URLs during development. Since
     * a programmers dev site is hosted on a different machine, this is usful to know
     * if a non-relative URL must be used.
     */
    public boolean smellsLikePerl(String partialUrl) {
        return (partialUrl.indexOf("res/") == -1 && partialUrl.indexOf(".page") == -1);
    }

    /**
     * Is this code running on a developers workstation?
     */
    public static boolean isDeveloperWorkstation(String hostName) {
        return hostName.indexOf("localhost") > -1 ||
                hostName.indexOf("127.0.0.1") > -1 ||
                hostName.matches("^172\\.18\\.1.*") ||
                hostName.matches("^172\\.21\\.1.*") ||
                hostName.indexOf("samson.") != -1 ||
                hostName.indexOf("mitchtest.") != -1 ||
                hostName.indexOf("rcox.office.") != -1 ||
                (hostName.matches(".+\\.office.*") && hostName.indexOf("cpickslay.office") == -1) ||
                hostName.indexOf("vpn.greatschools.org") != -1 ||
                hostName.indexOf("macbook") > -1;
    }

    /**
     * Is this development code, either on the developers workstation
     * or on one of the development servers?
     * Is this not the live site? or a very near clone of it?
     */
    public static boolean isDevEnvironment(String hostName) {
        return hostName.indexOf("dev.") != -1 ||
                hostName.endsWith("dev") ||
                hostName.indexOf("staging") != -1 ||
                hostName.indexOf("clone") != -1 ||
                hostName.indexOf("cmsqa") != -1 ||
                hostName.indexOf("qa") != -1 ||
                hostName.equals("127.0.0.1") ||
                hostName.matches("^172\\.18\\.1.*") ||
                hostName.matches(".*carbonfive.com") ||
                isDeveloperWorkstation(hostName);
    }

    public static boolean isStagingServer(String hostName) {
        return hostName.indexOf("staging") != -1 ||
                hostName.indexOf("clone") != -1 ||
                hostName.indexOf("willow") != -1;
    }

    public static boolean isQAServer(String hostName) {
        return hostName.indexOf("cmsqa1") != -1 ||
                hostName.indexOf("cmsqa2") != -1 ||
                hostName.indexOf("qa.greatschools") != -1;
    }

    public static boolean isCloneServer(String hostName) {
        return hostName.indexOf("clone") != -1;
    }

    public static boolean isPreReleaseServer(String hostName) {
        return hostName.indexOf("rithmatic") != -1;
    }

    public static String getApiHostname(String hostName) {
        if (isCloneServer(hostName)) {
            return "api.clone.greatschools.org";
        } else if (isStagingServer(hostName)) {
            return "api.staging.greatschools.org";
        } else if (hostName.indexOf("cmsqa1") != -1) {
            return "api.cmsqa1.greatschools.org";
        } else if (hostName.indexOf("cmsqa2") != -1) {
            return "api.cmsqa2.greatschools.org";
        } else if (isDevEnvironment(hostName)) {
            return "api.dev.greatschools.org";
        }
        return "api.greatschools.org";
    }

    public static boolean isAdminServer(String hostName) {
        return hostName.indexOf("admin") != -1 ||
                hostName.indexOf("maddy") != -1;
    }

    /**
     * Returns true if the url seems like a community content creation link. These links
     * tend to be treated differently when redirects are concerned (to preserve partial
     * data)
     * @param url url to check (null-safe: returns false)
     */
    public static boolean isCommunityContentLink(String url) {
        boolean rval = false;
        // if it is non-empty
        if (!StringUtils.isEmpty(url)) {
            // if it points to a community server
            if (StringUtils.contains(url, "community.greatschools.org") ||
                    StringUtils.contains(url, "comgen1.greatschools.org") ||
                    StringUtils.contains(url, "community.clone.greatschools.org") ||
                    StringUtils.contains(url, "community.dev.greatschools.org") ||
                    StringUtils.contains(url, "community.staging.greatschools.org")) {
                // if it smells like a content creation link
                if (StringUtils.contains(url, "/advice/write")
                        || StringUtils.contains(url, "/groups/create")
                        || StringUtils.contains(url, "/groups/similar")
                        || StringUtils.contains(url, "/q-and-a/?submit=true")
                        || StringUtils.contains(url, "/q-and-a?submit=true")
                        || StringUtils.contains(url, "/report")
                        || StringUtils.contains(url, "?comment")
                        || StringUtils.contains(url, "/members/watchlist/watch")
                        || StringUtils.contains(url, "/recommend-content")
                        || StringUtils.contains(url, "/join")
                        ) {
                    rval = true;
                }
            } else if (StringUtils.contains(url, "community.gs") || StringUtils.contains(url, "discussion.gs")) {
                rval = true;
            }
        }
        return rval;
    }

    /**
     * Adds a parameter to a url String
     *
     * @param url the base url string that the parameter will be added to
     * @param  parameter the parameter with value to be added to the base url
     * @return the url with the parameter appended to it
     *
     * @example addParameter("myBaseUrl.first", "param1=29")
     *
     */
    public static String addParameter(String url, String parameter)  {

        String delimiter;
        if (url.contains("?")){
             delimiter = "&";
        } else {
            delimiter = "?";
        }

        StringBuilder buf;
        if (url.contains("#")) {
            String urlBeforeAnchor = url.substring(0, url.indexOf("#"));
            String anchor = url.substring(url.indexOf("#"));
            buf = new StringBuilder(urlBeforeAnchor);
            buf.append(delimiter);
            buf.append(parameter);
            buf.append(anchor);
        } else {
            buf = new StringBuilder(url);
            buf.append(delimiter);
            buf.append(parameter);
        }

        return buf.toString();
    }

    public static String buildArticleUrl(Integer articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("Article ID must not be null");
        }
        UrlBuilder builder = new UrlBuilder(articleId, false);
        return builder.toString();
    }


    public static String urlEncode(String input) throws Exception {
        return URLEncoder.encode(input, "UTF-8");
    }

    public static String encodeWithinQuery(String input) throws URIException {
        return URIUtil.encodeWithinQuery(input);
    }

    public static String formatUrl(String url) {
        if (!StringUtils.startsWith(url, "http://")) {
            return "http://" + url;
        }
        return url;
    }
}
