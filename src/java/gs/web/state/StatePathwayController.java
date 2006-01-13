/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: StatePathwayController.java,v 1.20 2006/01/13 01:16:54 apeterson Exp $
 */
package gs.web.state;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class StatePathwayController extends AbstractController {

    public static final String BEAN_ID = "/stateLauncher.page";
    static final String DEFAULT_PATHWAY_MAP_KEY = "default";
    public static final String SEARCH_SINGLE = "search_single";
    public static final String SEARCH_DOUBLE = "search_double";

    private static final Log _log = LogFactory.getLog(StatePathwayController.class);

    private String _viewName;
    private Map _pathways;
    private ResourceBundleMessageSource _messageSource;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {


        boolean hasSelectedState = true;

        String state = request.getParameter("state");
        if (StringUtils.isEmpty(state)) {
            hasSelectedState = false;
            state = "";
        }

        String paramPathway = request.getParameter("p");
        String redirectUrl = "";
        gs.web.util.UrlUtil urlUtil = new gs.web.util.UrlUtil();

        if (_pathways.containsKey(paramPathway)) {
            redirectUrl = (String) _pathways.get(paramPathway);
        } else if (request.getParameter("url") != null) {
            redirectUrl = request.getParameter("url");
            paramPathway = DEFAULT_PATHWAY_MAP_KEY;
        } else {
            redirectUrl = (String) _pathways.get(DEFAULT_PATHWAY_MAP_KEY);
            paramPathway = DEFAULT_PATHWAY_MAP_KEY;
        }

        if (urlUtil.smellsLikePerl(redirectUrl)) {
            redirectUrl += "/";
        } else {
            redirectUrl += "?state=";
        }

        String extraParam;

        if (paramPathway.equals(SEARCH_SINGLE)) {
            extraParam = buildSingleSearchParam(request);
        } else if (paramPathway.equals(SEARCH_DOUBLE)) {
            extraParam = buildDoubleSearchParam(request);
        } else {
            extraParam = "";
        }

        if (hasSelectedState) {
            redirectUrl += state + extraParam;
            redirectUrl = urlUtil.buildUrl(redirectUrl, request);
            httpServletResponse.sendRedirect(redirectUrl);

            return new ModelAndView();

        } else {
            Map model = new HashMap();
            String promo = null;

            try {
                promo = _messageSource.getMessage(paramPathway + "_promo", null, Locale.ENGLISH);
            } catch (Exception e) {
                promo = "";
            }

            model.put("url", urlUtil.buildUrl(redirectUrl, request));
            model.put("extraParams", extraParam);
            model.put("promotext", promo);

            return new ModelAndView(_viewName, model);
        }
    }

    private String buildSingleSearchParam (final HttpServletRequest request) throws Exception {
        String appendParam = "";

        String paramQuery = request.getParameter("q");
        if (!StringUtils.isEmpty(paramQuery)) {
            appendParam += "&q=" + URLEncoder.encode(paramQuery, "UTF-8");
        }

        String paramSt = request.getParameter("st");
        if (!StringUtils.isEmpty(paramSt)) {
            appendParam += "&st=" + URLEncoder.encode(paramSt, "UTF-8");
        }

        String paramC = request.getParameter("c");
        if (!StringUtils.isEmpty(paramC)) {
            appendParam += "&c=" + URLEncoder.encode(paramC, "UTF-8");
        }

        return appendParam;
    }

    private String buildDoubleSearchParam (final HttpServletRequest request) throws Exception {

        String paramName = request.getParameter("field1");
        String paramCity = request.getParameter("field2");

        String appendParam = "?selector=by_school";

        if (!StringUtils.isEmpty(paramName)) {
            appendParam += "&field1=" + URLEncoder.encode(paramName, "UTF-8");
        }

        if (!StringUtils.isEmpty(paramCity)) {
            appendParam += "&field2=" + URLEncoder.encode(paramCity, "UTF-8");
        }

        return appendParam;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public Map getPathways() {
        return _pathways;
    }

    public void setPathways(Map pathways) {
        _pathways = pathways;
    }

    public ResourceBundleMessageSource getMessageSource() {
        return _messageSource;
    }

    public void setMessageSource(ResourceBundleMessageSource messageSource) {
        _messageSource = messageSource;
    }
}
