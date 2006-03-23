/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: UrlBuilder.java,v 1.2 2006/03/23 01:32:32 apeterson Exp $
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


    /**
     * Create a builder to the same page.
     *
     * @param request the current request
     */
    public UrlBuilder(HttpServletRequest request) {
        _serverName = request.getServerName();
        _serverPort = request.getServerPort();
        _contextPath = request.getContextPath();
        _path = request.getRequestURI();
    }


    /**
     * Create a link to an article
     *
     * @param featured should the "featured" url be used instead of the normal one. This is
     *                 for tracking.
     */
    public UrlBuilder(State s, Article article, boolean featured) {
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

    public void setPath(String path) {
        _path = path;
    }

    public void addParametersFromRequest(HttpServletRequest request) {
        if (_parameters == null) {
            _parameters = new HashMap(request.getParameterMap());
        } else {
            _parameters.putAll(request.getParameterMap());
        }
    }

    public void setParameter(String key, String value) {
        if (_parameters == null) {
            _parameters = new HashMap();
        }
        try {
            _parameters.put(key, new String[]{URLEncoder.encode(value, "UTF-8")});
        } catch (UnsupportedEncodingException e) {
            _parameters.put(key, new String[]{value});
            _log.warn("Unable to encode parameter");
        }
    }

    public void removeParameter(String key) {
        if (_parameters != null) {
            _parameters.remove(key);
        }
    }

    public String toString() {
        return asSiteRelativeUrl();
    }

    public String asSiteRelativeUrl() {
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
        return null;
    }

    public String asFullUrl() {
        String url = "http://" +
                _serverName +
                ((_serverPort != 80) ? ":" + _serverPort : "") +
                asSiteRelativeUrl();
        return url;
    }


}
