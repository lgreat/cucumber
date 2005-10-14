/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ISessionFacade.java,v 1.1 2005/10/14 23:21:26 apeterson Exp $
 */

package gs.web;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.content.IArticleDao;
import gs.data.school.ISchoolDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import org.springframework.context.ApplicationContext;

/**
 * The purpose is to hold common "global" properties for a user throughout their
 * session. It's a facade over the regular session, provide type safety and
 * whatever integrity guarantees we need to add. This class is wired to always
 * be available to your page (via Spring), so you don't have to defensively check for null.
 * Additionally, we can enforce rules like "the user's current geographic state is available",
 * and not mess with checks to make sure values are in the session. See {@link #getStateOrDefault()} for
 * an example of this.
 * <p />
 * Finally, this class gets called at the beginning of each request, and can
 * perform global operations like changing the user's state, host or cobrand.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public interface ISessionFacade {
    ApplicationContext getApplicationContext();

    User getUser();

    State getState();

    State getStateOrDefault();

    String getCobrand();

    String getHostName();

    /**
     * Determine if this is our main website or a cobrand
     *
     * @return true if it's a cobrand
     */
    boolean isCobrand();

    /**
     * Determine if this site should be ad free
     *
     * @return true if it's ad free
     */
    boolean isAdFree();

    /**
     * Is this the yahoo cobrand?
     * yahoo cobrands are yahooed and yahoo
     */
    boolean isYahooCobrand();

    String getSecureHostName();

    IUserDao getUserDao();

    StateManager getStateManager();

    IArticleDao getArticleDao();

    ISchoolDao getSchoolDao();
}
