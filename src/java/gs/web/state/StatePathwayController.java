/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: StatePathwayController.java,v 1.7 2005/11/01 19:58:03 thuss Exp $
 */
package gs.web.state;

import gs.data.state.State;
import gs.web.SessionContext;
import gs.web.ISessionFacade;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class StatePathwayController extends AbstractController {

    public static final String BEAN_ID = "/stateLauncher.page";
    private static final String DEFAULT_PATHWAY_MAP_KEY = "default";

    private static final Log _log = LogFactory.getLog(StatePathwayController.class);

    private String _viewName;
    private Map _pathways;
    private ResourceBundleMessageSource _messageSource;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

        boolean hasSelectedState = false;
        ISessionFacade sessionFacade = SessionContext.getInstance(request);
        State state = null;
        state = sessionFacade.getState();
        if (state != null) {
            hasSelectedState = true;
        }

        String paramPathway = request.getParameter("p");
        String paramSearchQuery = request.getParameter("q");
        String pathwayUrl = "";
        gs.web.util.UrlUtil urlUtil = new gs.web.util.UrlUtil();

        if (_pathways.containsKey(paramPathway)) {
            pathwayUrl = (String) _pathways.get(paramPathway);
        } else {
            pathwayUrl = (String) _pathways.get(DEFAULT_PATHWAY_MAP_KEY);
        }

        if (hasSelectedState) {
            boolean isContextRelative = true;
            //redirect to the correct pathway
            Map params = new HashMap();
            if (pathwayUrl.matches(".+modperl.+|.+cgi-bin.+")) {
                pathwayUrl += "/" + state.getAbbreviation();
                isContextRelative = false;
            } else {
                params.put("state", state.getAbbreviation());
            }

            pathwayUrl = urlUtil.buildUrl(pathwayUrl, request);
            RedirectView redirectView = new RedirectView(pathwayUrl, isContextRelative);

            if (!StringUtils.isEmpty(paramSearchQuery)) {
                params.put("q", paramSearchQuery);
            }
            redirectView.setAttributesMap(params);

            return new ModelAndView(redirectView);

        } else {
            Map model = new HashMap();
            String postBackUrl = BEAN_ID + "?p=" + paramPathway;

            if (!StringUtils.isEmpty(paramSearchQuery)) {
                postBackUrl += "&q=" + paramSearchQuery;
            }
            String promo = null;

            try {
                promo = _messageSource.getMessage(paramPathway + "_promo", null, Locale.ENGLISH);
            } catch (NoSuchMessageException e) {
                promo = "";
            }

            model.put("url", urlUtil.buildUrl(postBackUrl, request));
            model.put("promotext", promo);

            return new ModelAndView(_viewName, model);
        }
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
