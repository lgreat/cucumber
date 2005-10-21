/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionFacade.java,v 1.3 2005/10/21 01:45:06 apeterson Exp $
 */

package gs.web;

import javax.servlet.http.HttpServletRequest;

/**
 * Place to hang the factory method.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 * @see SessionContextInterceptor guarantees that this object is available in the request.
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
