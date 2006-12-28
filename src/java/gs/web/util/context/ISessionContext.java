/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ISessionContext.java,v 1.4 2006/12/28 21:58:32 cpickslay Exp $
 */

package gs.web.util.context;

import gs.data.community.User;
import gs.data.state.State;
import org.springframework.context.ApplicationContext;

/**
 * @deprecated This interface has no clear purpose, and is missing several important methods from SessionContext.
 *             Use SessionContext instead.
 */
public interface ISessionContext {
    String REQUEST_ATTRIBUTE_NAME = "context";

    ApplicationContext getApplicationContext();

    /**
     * Current user, if known. This does NOT guarantee that this is a subscribed
     * user. Other tests must be used to protect paid content.
     * This may be a costly operation. You should use individual fields, like email or
     * nickname, if they are sufficient.
     */
    User getUser();

    String getNickname();

    String getEmail();

    Integer getMemberId();

    int getMslCount();

    int getMssCount();

    String getUserHash();

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
     * Determine if the user is a crawler
     *
     * @return true if it's a cobrand
     */
    boolean isCrawler();

    /**
     * If our ad server company has an outage we can turn off advertising
     *
     * @return true if advertising it turned on
     */
    boolean isAdvertisingOnline();

    /**
     * Is this the yahoo cobrand?
     * yahoo cobrands are yahooed and yahoo
     */
    boolean isYahooCobrand();

    /**
     * Is this disney's family cobrand?
     * family.greatschools.net
     */
    boolean isFamilyCobrand();


}
