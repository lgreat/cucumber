/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionFacade.java,v 1.1 2005/10/14 23:21:26 apeterson Exp $
 */

package gs.web;

import javax.servlet.http.HttpServletRequest;

/**
 * Place to hang the factory method.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public abstract class SessionFacade implements ISessionFacade {
    /**
     * Accessor
     */
    public static ISessionFacade getInstance(HttpServletRequest request) {
        return SessionContext.getInstanceImpl(request);
    }
}
