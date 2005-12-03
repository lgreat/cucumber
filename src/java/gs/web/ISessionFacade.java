/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ISessionFacade.java,v 1.5 2005/12/03 00:35:59 apeterson Exp $
 */

package gs.web;

import gs.data.community.User;
import gs.data.state.State;
import org.springframework.context.ApplicationContext;

/**
 * The purpose is to hold common "global" properties for a user throughout their
 * session. It's a facade over the regular session, provide type safety and
 * whatever integrity guarantees we need to add. This class is wired to always
 * be available to your page (via Spring), so you don't have to defensively check for null.
 * Additionally, we can enforce rules like "the user's current geographic state is available",
 * and not mess with checks to make sure values are in the session. See {@link #getStateOrDefault()} for
 * an example of this.
 * Finally, this class gets called at the beginning of each request, and can
 * perform global operations like changing the user's state, host or cobrand.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public interface ISessionFacade {

    ApplicationContext getApplicationContext();

    /**
     * Current user, if known. This does NOT guarantee that this is a subscribed
     * user. Other tests must be used to protect paid content.
     */
    User getUser();

    /**
     * Current state (of the U.S.).
     */
    State getState();

    /**
     * Guaranteed non-null state.
     */
    State getStateOrDefault();

    /**
     * A pathway of "1", "2" or "3", or null for no pathway.
     */
    String getPathway();

    void setPathway(String pathway);

    String getCobrand();

    String getHostName();

    /**
     * Determine if this is our main website or a cobrand
     *
     * @return true if it's a cobrand
     */
    boolean isCobranded();

    /**
     * Is this the yahoo cobrand?
     * yahoo cobrands are yahooed and yahoo
     */
    boolean isYahooCobrand();

    String getSecureHostName();
}
