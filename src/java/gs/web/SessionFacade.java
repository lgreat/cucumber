/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionFacade.java,v 1.2 2005/10/21 00:16:01 apeterson Exp $
 */

package gs.web;

import javax.servlet.http.HttpServletRequest;

/**
 * Place to hang the factory method.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public abstract class SessionFacade implements ISessionFacade {

    public static final String REQUEST_ATTRIBUTE_NAME = "context";

    /**
     * Accessor
     */
    public static ISessionFacade getInstance(HttpServletRequest request) {
        return (ISessionFacade) request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);
    }
}
