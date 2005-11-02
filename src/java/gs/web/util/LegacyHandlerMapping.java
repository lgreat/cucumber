/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: LegacyHandlerMapping.java,v 1.1 2005/11/02 01:10:34 apeterson Exp $
 */

package gs.web.util;

import gs.data.state.State;
import gs.data.state.StateManager;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class LegacyHandlerMapping extends AbstractUrlHandlerMapping {

    private UrlPathHelper _urlPathHelper = new UrlPathHelper();
    private StateManager _stateManager;

    public LegacyHandlerMapping() {
        _urlPathHelper.setAlwaysUseFullPath(true);
    }

    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
        String lookupPath = _urlPathHelper.getLookupPathForRequest(request);
        logger.debug("Looking up handler for [" + lookupPath + "]");

        lookupPath = lookupPath.substring(12,14);
        logger.error(lookupPath);

        State s = _stateManager.getState(lookupPath);

        request.setAttribute("state", s);
        Object handler = getApplicationContext().getBean("/path/choose.page");
        return handler;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}