/*
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: UrlUtil.java,v 1.109 2011/06/23 01:50:51 ssprouse Exp $
 */

package gs.web.util;

import gs.data.state.State;
import gs.data.util.CdnUtil;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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

    public static String putQueryParamIntoQueryString(String queryString, String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        Map<String,String> keysValues = getParamsFromQueryString(queryString);

        keysValues.put(key,value);

        String newQueryString = getQueryStringFromMap(keysValues);

        return newQueryString;
    }

    public static String putQueryParamIntoUrl(String url, String key, String value) {
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

        queryString = putQueryParamIntoQueryString(queryString, key, value);

        String newUrl = base + "?" + queryString;

        return newUrl;
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
                try {
                    params.put(nameAndValue[0], URLDecoder.decode(nameAndValue[1], "UTF-8"));
                } catch (UnsupportedEncodingException uee) {
                    _log.warn("Can't decode param: " + nameValuePair);
                }
            }
        }
        return params;
    }

    public static String removeParamsFromQueryString(String queryString, String... keys) {
        Map<String,String> map = getParamsFromQueryString(queryString);

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

        return getQueryStringFromMap(map);
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
     * Given a hostname, extracts the cobrand, or what looks like
     * a cobrand from it. Returns null for no cobrand.
     */
    public String cobrandFromUrl(String hostName) {
        String cobrandName = null;
        boolean isCobrand = !(hostName.startsWith("www") && hostName.indexOf("greatschools") > -1)
                && !hostName.startsWith("secure")
                && !hostName.startsWith("res1")
                && !hostName.startsWith("res2")
                && !hostName.startsWith("res3")
                && !hostName.startsWith("res4")
                && !hostName.startsWith("res5")
                && !hostName.startsWith("res6")
                && !hostName.startsWith("res7")
                && !hostName.startsWith("qa.")
                && !hostName.startsWith("sw1-pub.")
                && !hostName.startsWith("sw1-pre.")
                && !hostName.startsWith("app1a")
                && !hostName.startsWith("app1b")
                && !hostName.startsWith("app1c")
                && !hostName.startsWith("app1d")
                && !hostName.startsWith("app2a")
                && !hostName.startsWith("app2b")
                && !hostName.startsWith("app2c")
                && !hostName.startsWith("app2d")
                && !hostName.startsWith("staging")
                && !hostName.startsWith("maddy")
                && !hostName.startsWith("admin")
                && !hostName.startsWith("clone")
                && !hostName.startsWith("crufty")
                && !hostName.startsWith("dev")
                && !hostName.startsWith("localhost")
                && !hostName.startsWith("main.dev")
                && !hostName.startsWith("chriskimm.dev")
                && !hostName.startsWith("aroy.office")
                && !hostName.startsWith("aroy.dev")
                && !hostName.startsWith("droy.dev")
                && !hostName.startsWith("profile.dev")
                && !hostName.startsWith("cpickslay.")
                && !hostName.startsWith("editorial.")
                && !hostName.startsWith("mwong.dev")
                && !(hostName.indexOf("vpn.greatschools.org") != -1)
                && !hostName.equals("127.0.0.1")
                && !hostName.startsWith("192.168.")
                && !hostName.startsWith("172.21.1.142")
                && !hostName.startsWith("172.18.")
                && hostName.indexOf('.') != -1;
        if (isCobrand) {
            cobrandName = hostName.substring(0, hostName.indexOf("."));
            // Need special cases for greatschools.cobrand.com like babycenter
            // or schoolrankings.nj.com (GS-11466)
            if (hostName.startsWith("greatschools.") || hostName.startsWith("schoolrankings.")) {
                int firstDot = hostName.indexOf(".");
                int lastDot = hostName.lastIndexOf(".");
                if (lastDot > firstDot) {
                    cobrandName = hostName.substring(firstDot + 1, lastDot);
                }
            // Need special case for cobrands like www.fresno.schools.net
            } else if (hostName.startsWith("www")) {
                int firstDot = hostName.indexOf(".");
                if (firstDot > -1) {
                    int secondDot = hostName.indexOf(".", firstDot+1);
                    if (secondDot > firstDot) {
                        cobrandName = hostName.substring(firstDot + 1, secondDot);
                    }
                }
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
}