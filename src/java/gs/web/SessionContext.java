/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContext.java,v 1.20 2005/11/01 17:06:00 apeterson Exp $
 */
package gs.web;

import gs.data.community.User;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;

/**
 * Implementation of the ISessionFacade interface based on Java servlet
 * sessions.
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 * @see SessionContextInterceptor
 */
public class SessionContext
        extends SessionFacade
        implements ApplicationContextAware, Serializable {

    static final String BEAN_ID = "sessionContext";

    private static final long serialVersionUID = -314159265358979323L;

    private static final Log _log = LogFactory.getLog(SessionContextInterceptor.class);

    /**
     * The name of the cobrand (sfgate, azcentral, dps, etc...) or null
     */
    private String _cobrand;
    private String _hostName;
    private User _user;
    private State _state;
    private String _pathway;

    private ApplicationContext _applicationContext;

    /**
     * Created by Spring as needed.
     */
    public SessionContext() {
    }


    public ApplicationContext getApplicationContext() {
        return _applicationContext;
    }

    public User getUser() {
        return _user;
    }

    public void setUser(final User user) {
        _user = user;
    }

    public State getState() {
        return _state;
    }

    public State getStateOrDefault() {
        return _state == null ? State.CA : _state;
    }

    public void setState(final State state) {
        _state = state;
    }

    public String getCobrand() {
        return _cobrand;
    }

    public String getHostName() {
        return _hostName;
    }

    public boolean isCobrand() {
        return _cobrand != null;
    }

    public boolean isAdFree() {
        boolean sAdFree = false;
        if (_cobrand != null &&
                (_cobrand.matches("mcguire|framed|number1expert"))) {
            sAdFree = true;
        }
        return sAdFree;
    }

    public boolean isYahooCobrand() {
        boolean sYahooCobrand = false;
        if (_cobrand != null &&
                (_cobrand.matches("yahoo|yahooed"))) {
            sYahooCobrand = true;
        }
        return sYahooCobrand;
    }

    public String getSecureHostName() {
        String sHost = "secure.greatschools.net";

        if (StringUtils.contains(_hostName, "dev.greatschools.net")) {
            sHost = "secure.dev.greatschools.net";
        } else if (StringUtils.equalsIgnoreCase(_hostName, "staging.greatschools.net")) {
            sHost = "secure.staging.greatschools.net";
        }

        return sHost;
    }


    public void setHostName(final String hostName) {
        _hostName = hostName;
    }

    public void setCobrand(final String cobrand) {
        _cobrand = cobrand;
    }

    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        _applicationContext = applicationContext;
    }


    public String getPathway() {
        return _pathway;
    }

    public void setPathway(String pathway) {
        _pathway = pathway;
    }
}
