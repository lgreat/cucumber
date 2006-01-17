/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: UrlUtil.java,v 1.13 2006/01/17 20:49:45 apeterson Exp $
 */

package gs.web.util;

import gs.data.state.State;
import gs.data.content.Article;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Wrapping and URL munging tools.
 * These are distinct from the GSData ones, which are general utilities. These have to do
 * with the specific GSWeb environment, especially the session facade.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public final class UrlUtil {
    private static final Log _log = LogFactory.getLog(UrlUtil.class);

    /**
     * Given a hostname, extracts the cobrand, or what looks like
     * a cobrand from it. Returns null for no cobrand.
     */
    public String cobrandFromUrl(String hostName) {
        String cobrandName = null;
        final boolean isCobrand = !hostName.startsWith("www")
                && !hostName.startsWith("secure")
                && !hostName.startsWith("staging")
                && !hostName.startsWith("dev")
                && !hostName.startsWith("localhost")
                && !hostName.startsWith("main.dev")
                && !hostName.startsWith("dlee.dev")
                && !hostName.startsWith("apeterson.dev")
                && !hostName.startsWith("thuss.dev")
                && !hostName.startsWith("chriskimm.dev")
                && !hostName.equals("127.0.0.1")
                && !hostName.equals("apeterson.office.greatschools.net")
                && hostName.indexOf('.') != -1;
        if (isCobrand) {
            cobrandName = hostName.substring(0, hostName.indexOf("."));
            // Need special cases for greatschools.cobrand.com like babycenter
            if (hostName.startsWith("greatschools.")) {
                int firstDot = hostName.indexOf(".");
                int lastDot = hostName.lastIndexOf(".");
                if (lastDot > firstDot) {
                    cobrandName = hostName.substring(firstDot + 1, lastDot);
                }
            }
        }
        return cobrandName;
    }

    public String buildPerlHostName(String hostName, String cobrand) {
        if (StringUtils.contains(hostName, "localhost")) {
            String dev = "dev.greatschools.net";
            return (cobrand == null) ? dev : cobrand + "." + dev;

            // Else if it's the main website but with the cobrand parameter passed
            // then we return the full cobrand URL
        } else if (cobrand != null &&
                (hostName.startsWith("www") ||
                        hostName.startsWith("staging") ||
                        hostName.startsWith("dev"))) {
            // dev.greatschools.net?cobrand=sfgate -> sfgate.dev.greatschools.net
            String hn = cobrand + "." + hostName;
            // azcentral.www.greatschools.net -> azcentral.greatschools.net
            return hn.replaceFirst(".www.", ".");
        } else if (hostName.startsWith("secure")) {
            // Secure pages link back to www.greatschools.net if we didn't come from a cobrand
            return "www.greatschools.net";
        } else {
            return hostName;
        }

    }

    /**
     * Examines the dest URL and returns a URL that will work from the given
     * src location. It attempts to deliver the smallest possible URL
     * that will work.
     *
     * @param dest         the site-relative link to the destination page or resource
     * @param isDestSecure should the dest page be access via https?
     * @param src          the current location. Should be a Java page.
     * @param orighost     null or cobrand.gs.net when on secure.gs.net
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    public String buildHref(String dest, boolean isDestSecure, String src, String orighost) {


        if (src == null) {
            _log.warn("Unable to interpret current page 'null' as URL");
            return dest; // no logic to do, but not a good case
        }

        _log.debug("dest=" + dest + " isDestSecure?" + isDestSecure + " src=" + src);

        boolean destIsPerl = smellsLikePerl(dest);
        try {
            URL sourceUrl = new URL(src);
            if (isDeveloperWorkstation(sourceUrl.getHost())) {
                if (destIsPerl) {
                    return "http://dev.greatschools.net" + dest;
                } else {
                    return dest;
                }
            } else {
                if ("https".equals(sourceUrl.getProtocol())) {
                    String host = sourceUrl.getHost();
                    if (orighost != null) {
                        host = orighost;
                    }
                    if (destIsPerl) {
                        return "http://" + host + dest;
                    } else {
                        if (isDestSecure) {
                            return dest;
                        } else {
                            return "http://" + host + dest;
                        }
                    }
                }
                // Anywhere but a developer workstation and we should be able to use the relative
                // link.
                return dest;
            }

        } catch (MalformedURLException e) {
            _log.warn("Unable to interpret current page as URL", e);
            return dest;
        }
    }


    /**
     * Returns true if it looks like the given resource needs to be sought from
     * the perl site. This is useful in building out URLs during development. Since
     * a programmers dev site is hosted on a different machine, this is usful to know
     * if a non-relative URL must be used.
     */
    public boolean smellsLikePerl(String partialUrl) {
        if ((partialUrl.indexOf("res/") == -1 && partialUrl.indexOf(".page") == -1)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isDeveloperWorkstation(String hostName) {
        return hostName.indexOf("localhost") > -1;
    }


    /**
     * Generates a url from the given requested resource. Common bottleneck for URL
     * building.
     * This routine can grow as we decide to do more or less with URLs.
     * It does substitution of $VARIABLES, per Perl world. The current variables
     * supported are:
     * <ul>
     * <li>$STATE
     * </ul>
     * This variable is taken from the request attribute named of the same name,
     * and if not found there, it uses the ISessionFacade available from the
     * request.
     * It will add the appropriate server name if this is a link to a perl page.
     * This is most helpful in the dev environment, but this bottleneck allows us
     * to use the same code for test and deploy environments.
     * It does not guarantee to build the smallest possible URL, but it attempts
     * to do so.
     */
    public String buildUrl(final String ref, HttpServletRequest request) {
        // If the URL has a STATE string in it (or more than one), replace it with the
        // user's state.

        gs.data.util.NetworkUtil networkUtil = new gs.data.util.NetworkUtil();

        String href = ref;

        // If the application is deployed under say /gs-web instead of /
        if (href.startsWith("/") && request.getContextPath().length() > 1 &&
                !smellsLikePerl(href)) {
            href = request.getContextPath() + href;
        }

        gs.web.ISessionFacade context = gs.web.SessionFacade.getInstance(request);
        if (href.indexOf("STATE") != -1) {
            // Allow a request attribute to override the session facade.
            if (request.getAttribute("STATE") != null &&
                    request.getAttribute("STATE") instanceof State) {
                State s = (State) request.getAttribute("STATE");
                String sa = s.getAbbreviation();
                href = href.replaceAll("\\$STATE", sa);
            } else {
                gs.data.state.State s = context.getStateOrDefault();
                String sa = s.getAbbreviation();
                href = href.replaceAll("\\$STATE", sa);
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
                (ref.indexOf("subscribe.page") > -1 || ref.indexOf("thankyou.page") > -1)) {
                secureDest = true;
        }

        return buildHref(href, secureDest, src, context!=null?context.getHostName():null);
    }

    /**
     * Converts a "vpage" to a url that can be used on the site.
     * This method can be expanded and configured to work with more
     * URLs as we grow this concept.
     * URLs that don't look like VPAGE URLs are returned as is.
     *
     * @param url url or "vpage". A vpage starts with "vpage:"
     * @return a url
     */
    public String vpageToUrl(String url) {
        if (url.startsWith("vpage:")) {
            String vpage = url.substring(6);
            if (StringUtils.equals("content.seasonal", vpage)) {
                return "/cgi-bin/site/january_parent_tips.cgi/$STATE";
                // NOTE: was "/cgi-bin/site/parent_tips.cgi/$STATE"
            } else if (StringUtils.equals("path1", vpage)) {
                return "/modperl/go/$STATE";
            } else if (StringUtils.equals("path2", vpage)) {
                return "/path/mySchool.page?state=$STATE";
            }
            throw new IllegalArgumentException("Unknown vpage indicated: " + vpage);
        } else {
            return url;
        }
    }

    public String getArticleLink(State s, Article article, boolean featured) {
        // Calculate page to use
        String page;
        if (s.isSubscriptionState() && article.isInsider()) {
            page = "showpartarticle";
        } else {
            if (featured) {
                page = "showarticlefeature";
            } else {
                page = "showarticle";
            }
        }

        // Calculate link
        String link = "/cgi-bin/" +
                page +
                "/" +
                s.getAbbreviationLowerCase() +
                "/" +
                article.getId();
        link = buildHref(link, false, null, null);
        return link;
    }


}