/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: UrlBuilder.java,v 1.6 2006/03/24 01:17:58 apeterson Exp $
 */

package gs.web.util;

import gs.data.content.Article;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Provides a builder utility for our URLs.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 * @see UrlUtil
 */
public class UrlBuilder {

    private static final Log _log = LogFactory.getLog(UrlBuilder.class);

    private String _serverName;
    private int _serverPort = 80;
    private String _contextPath;

    /**
     * Path relative to the host/context.
     */
    private String _path;
    private Map _parameters;
    private boolean _perlPage = false;
    private static UrlUtil _urlUtil = new UrlUtil();


    /**
     * Create a builder to the given site page.
     *
     * @param request             the current request
     * @param contextRelativePath the requested page. Null asks for the same page, but it may or
     *                            may not work. It would be great if you could always generate the link to the
     *                            current page, but unfortunately this isn't available at all points.
     *                            Tomcat, or possibly spring, has decorated the request so that it
     *                            doesn't point to what the user really asked for. If you're in the midst of processing a page, it now points to the
     *                            Jsp page that is being shown, not the user's request. It does seem to work in the controller, though.
     *                            I solved this before by grabbing it earlier on in the servlet processing
     *                            and stashing it away for later retrieval.
     */
    public UrlBuilder(HttpServletRequest request, String contextRelativePath) {
        _serverName = request.getServerName();
        _serverPort = request.getServerPort();
        _contextPath = request.getContextPath();
        _path = contextRelativePath;
        _perlPage = _urlUtil.smellsLikePerl(_path);
        if (contextRelativePath == null) {
            _path = request.getRequestURI();
            _path = StringUtils.removeStart(_path, _contextPath);
        } else {
            _path = contextRelativePath;
        }
        // _log.error("PathInfo="+request.getPathInfo()); // yields null
        // _log.error("PathTranslated="+request.getPathTranslated()); // yields null
        // _log.error("ServletPath="+request.getServletPath()); // yields "/WEB-INF/page/search/schoolsOnly.jspx"
        // _log.error("requestURI="+request.getRequestURI()); // yields "/gs-web/WEB-INF/page/search/schoolsOnly.jspx"
        // _log.error("requestURL="+request.getRequestURL()); // yields "http://apeterson.office.greatschools.net:8080/gs-web/WEB-INF/page/search/schoolsOnly.jspx"
    }


    /**
     * Create a link to an article
     *
     * @param featured should the "featured" url be used instead of the normal one. This is
     */
    public UrlBuilder(Article article, State s, boolean featured) {
        _perlPage = true;
        _contextPath = "";

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
        // TH: Commented this out because buildHref is noop with a null request
        // link = buildHref(null, link, false, null);
        _path = "/cgi-bin/" +
                page +
                "/" +
                s.getAbbreviationLowerCase() +
                "/" +
                article.getId();
    }

    /**
     * Set the path to the page.
     *
     * @param path context-relative path
     */
    public void setPath(String path) {
        _path = path;
        _perlPage = _urlUtil.smellsLikePerl(path);
    }

    /**
     * Takes all the parameters in the given requests and adds them to the URL.
     * If some parameters already exist, they will be replaced completely.
     */
    public void addParametersFromRequest(HttpServletRequest request) {
        if (_parameters == null) {
            _parameters = new HashMap(request.getParameterMap());
        } else {
            _parameters.putAll(request.getParameterMap());
        }
    }

    /**
     * Replaces the given parameter.
     *
     * @param value previously encoded value. Spaces should be represented by "+" signs,
     *              and "=" and "&" should be encoded, along with other extended characters.
     */
    public void setParameterNoEncoding(String key, String value) {
        if (_parameters == null) {
            _parameters = new HashMap();
        }
        _parameters.put(key, new String[]{value});
    }

    /**
     * Replaces the given parameter.
     *
     * @param value unencoded values. Spaces, ampersands, equal signs, etc. will be replaced.
     */
    public void setParameter(String key, String value) {
        try {
            value = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            _log.warn("Unable to encode parameter");
        }
        setParameterNoEncoding(key, value);
    }

    /**
     * Take away the parameter.
     */
    public void removeParameter(String key) {
        if (_parameters != null) {
            _parameters.remove(key);
        }
    }

    public String toString() {
        return asSiteRelative();
    }

    /**
     * Provides a site-relative path to the page, including the context path if needed.
     */
    public String asSiteRelative() {
        StringBuffer sb = new StringBuffer();
        if (!_perlPage && StringUtils.isNotEmpty(_contextPath)) {
            sb.append(_contextPath);
        }
        sb.append(_path);
        if (_parameters != null && _parameters.size() > 0) {
            sb.append("?");
            List keys = new ArrayList(_parameters.keySet());
            Collections.sort(keys);
            for (Iterator iter = keys.iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                String[] values = (String[]) _parameters.get(key);
                for (int i = 0; i < values.length; i++) {
                    sb.append(key);
                    sb.append("=" + values[i]);
                    if (i < values.length && iter.hasNext()) {
                        sb.append("&");
                    }
                }
            }
        }

        return sb.toString();
    }

    public Anchor asAnchor(String label) {
        return new Anchor(asSiteRelative(), label);
    }

    /**
     * Provides a full URL to the page. Generally not needed, but occassionally necessary.
     *
     * @see #asSiteRelative()
     */
    public String asFullUrl() {
        String url = "http://" +
                _serverName +
                ((_serverPort != 80) ? ":" + _serverPort : "") +
                asSiteRelative();
        return url;
    }


    /**
     * Provides a site-relative link wrapped in an a tag.
     */
    public String asAHref(String label) {
        return "<a href=\"" + asSiteRelative() + "\">" + label + "</a>";
    }
}
