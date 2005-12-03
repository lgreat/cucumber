/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: MockSessionFacade.java,v 1.1 2005/12/03 00:35:59 apeterson Exp $
 */

package gs.web.util;

import gs.web.ISessionFacade;
import gs.data.util.SpringUtil;
import gs.data.community.User;
import gs.data.state.State;
import org.springframework.context.ApplicationContext;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class MockSessionFacade implements ISessionFacade {
    private State _state;

    public ApplicationContext getApplicationContext() {
        return SpringUtil.getApplicationContext();
    }

    public User getUser() {
        return null;
    }

    public State getState() {
        return _state;
    }

    public State getStateOrDefault() {
        return _state == null ? State.CA : _state;
    }

    private String _pathway;

    public String getPathway() {
        return _pathway;
    }

    public void setPathway(String pathway) {
        _pathway = pathway;
    }

    private String _cobrand;


    public String getCobrand() {
        return _cobrand;
    }

    public void setCobrand(String cobrand) {
        _cobrand = cobrand;
    }

    public String getHostName() {
        return "www.greatschools.net";
    }

    public boolean isCobranded() {
        return false;
    }

    public boolean isYahooCobrand() {
        return false;
    }

    public String getSecureHostName() {
        return "secure.greatschools.net";
    }

    public void setState(State state) {
        _state = state;
    }
}
