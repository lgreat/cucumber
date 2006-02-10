/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContext.java,v 1.25 2006/02/10 02:10:29 thuss Exp $
 */
package gs.web;

import gs.data.community.ISubscriptionDao;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.state.State;
import gs.data.admin.IPropertyDao;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.Date;

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
    private ISubscriptionDao _subscriptionDao;
    private IPropertyDao _propertyDao;

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

    public boolean isPaidSubscriber() {
        if (_user == null) {
            return false;
        }

        return _subscriptionDao.isUserSubscribed(_user, SubscriptionProduct.ONE_YEAR_SUB, new Date());
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

    public boolean isCobranded() {
        return _cobrand != null;
    }

    /**
     * We only turn advertising off when our ad serving company has an outage
     * @return true if the ad server is working
     */
    public boolean isAdvertisingOnline() {
        return "true".equals(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_ID, "true"));
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

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public IPropertyDao getPropertyDao() {
        return _propertyDao;
    }

    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
    }
}
