/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ISessionChanger.java,v 1.1 2005/10/14 23:21:26 apeterson Exp $
 */

package gs.web;

import javax.servlet.http.HttpServletRequest;

/**
 * Define techniques to modify the user's session state.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 * @todo this doesn't seem just quite right yet.
 */
public interface ISessionChanger {

    // user can change state by passing a parameter on the command line
    String STATE_PARAM = "state";

    // user can change the cobrand by passing a parameter on the command line
    String COBRAND_PARAM = "cobrand";

    // user can change hosts by passing a parameter on the command line
    String HOST_PARAM = "host";

    /**
     * Called at the beginning of the request. Allows this class to
     * do common operations for all pages.
     */
    void updateFromParams(HttpServletRequest httpServletRequest);
}
