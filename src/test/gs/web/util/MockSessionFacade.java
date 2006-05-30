/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: MockSessionFacade.java,v 1.6 2006/05/30 18:43:50 chriskimm Exp $
 */

package gs.web.util;

import gs.data.community.User;
import gs.data.state.State;
import gs.data.util.SpringUtil;
import gs.web.ISessionFacade;
import org.springframework.context.ApplicationContext;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class MockSessionFacade implements ISessionFacade {
    private State _state;
    private boolean _paidSubscriber;
    private boolean _advertisingOnline = true;
    private String _hostName = "www.greatschools.net";

    public ApplicationContext getApplicationContext() {
        return SpringUtil.getApplicationContext();
    }

    public User getUser() {
        return null;
    }

    public boolean isPaidSubscriber() {
        return _paidSubscriber;
    }

    public void setPaidSubscriber(boolean paidSubscriber) {
        _paidSubscriber = paidSubscriber;
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
        return _hostName;
    }

    public void setHostName(String hostName) {
        _hostName = hostName;
    }

    public boolean isCobranded() {
        return false;
    }

    public boolean isAdvertisingOnline() {
        return _advertisingOnline;
    }

    public void setAdvertisingOnline(boolean online) {
        _advertisingOnline = online;
    }

    public boolean isYahooCobrand() {
        return false;
    }

    public boolean isFamilyCobrand() {
        return false;
    }

    public String getSecureHostName() {
        return "secure.greatschools.net";
    }

    public boolean isBetaPage() {
        return false;
    }

    public void setState(State state) {
        _state = state;
    }
}
