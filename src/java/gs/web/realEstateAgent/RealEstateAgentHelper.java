package gs.web.realEstateAgent;

import gs.data.community.User;
import gs.data.realEstateAgent.AgentAccount;
import gs.data.realEstateAgent.IAgentAccountDao;
import gs.data.state.State;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 3/19/13
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */
@Component("realEstateAgentHelper")
public class RealEstateAgentHelper {

    protected static final String NEW_USER_COOKIE_HASH = "newAgentRegistration";

    public static final String REGISTRATION_PAGE_VIEW = "/realEstateAgent/registrationHome";

    public static final String CREATE_REPORT_PAGE_VIEW = "/realEstateAgent/createReport";

    @Autowired
    private IAgentAccountDao _agentAccountDao;

    public User getUserFromSessionContext(HttpServletRequest request) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        return sessionContext.getUser();
    }

    public boolean hasAgentAccountFromSessionContext(HttpServletRequest request) {
        User user = getUserFromSessionContext(request);
        if(user != null && user.getId() != null) {
            return hasAgentAccountFromUserId(user.getId());
        }
        return false;
    }

    public boolean hasAgentAccountFromRegistrationCookie(HttpServletRequest request) {
        Integer userId = getUserIdFromCookie(request);
        return hasAgentAccountFromUserId(userId);
    }

    public boolean hasAgentAccount(HttpServletRequest request) {
        return (hasAgentAccountFromSessionContext(request) || hasAgentAccountFromRegistrationCookie(request));
    }

    public boolean hasAgentAccountFromUserId(Integer userId) {
        if(userId != null) {
            if (getAgentAccountDao().findAgentAccountByUserId(userId) !=  null) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public AgentAccount getAgentAccount(HttpServletRequest request) {
        Integer userId = getUserId(request);

        if(userId != null) {
            return getAgentAccountDao().findAgentAccountByUserId(userId);
        }

        return null;
    }

    public void setUserCookie(User user, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(String.valueOf((NEW_USER_COOKIE_HASH).hashCode()), String.valueOf(user.getId()));
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            // don't set domain for developer workstations so they can still access the cookie!!
            cookie.setDomain(".greatschools.org");
        }
        response.addCookie(cookie);
    }

    public Integer getUserIdFromCookie (HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(String.valueOf((NEW_USER_COOKIE_HASH).hashCode()).equals(cookie.getName())) {
                    try {
                        return Integer.parseInt(cookie.getValue());
                    }
                    catch (NumberFormatException ex) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public Integer getUserId (HttpServletRequest request) {
        User user = getUserFromSessionContext(request);

        Integer userIdFromCookie = getUserIdFromCookie(request);

        if(userIdFromCookie != null) {
            return userIdFromCookie;
        }
        else if (user != null && user.getId() != null){
            return user.getId();
        }

        return null;
    }

    public String getViewForUser (ModelMap modelMap, HttpServletRequest request, Integer userId, String view) {
        AgentAccount agentAccount = getAgentAccount(request);
        if(agentAccount != null && agentAccount.getState() != null) {
            if(CREATE_REPORT_PAGE_VIEW.equals(view)) {
                modelMap.putAll(getCompanyInfoFields(agentAccount));
            }
            return view;
        }

        return "redirect:" + getRealEstateSchoolGuidesUrl(request);
    }

    public Map<String, Object> getCompanyInfoFields(AgentAccount agentAccount) {
        Map<String, Object> savedFields = new HashMap<String, Object>();

        savedFields.put("companyName", agentAccount.getCompanyName());
        savedFields.put("workNumber", agentAccount.getWorkNumber());
        savedFields.put("cellNumber", agentAccount.getCellNumber());
        savedFields.put("address", agentAccount.getAddress());
        savedFields.put("city", agentAccount.getCity());
        savedFields.put("state", State.fromString(agentAccount.getState()));
        savedFields.put("zip", agentAccount.getZip());

        return savedFields;
    }

    public String getRealEstateSchoolGuidesUrl(HttpServletRequest request) {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.REAL_ESTATE_SCHOOL_GUIDES);
        return urlBuilder.asSiteRelative(request);
    }

    public String getRealEstateCreateGuideUrl (HttpServletRequest request) {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.REAL_ESTATE_CREATE_GUIDE);
        return urlBuilder.asSiteRelative(request);
    }

    /* useful for debugging */
    public boolean skipUserValidation(HttpServletRequest request) {
        if("true".equals(request.getParameter("skipUserCheck"))) {
            return true;
        }
        return false;
    }

    public IAgentAccountDao getAgentAccountDao() {
        return _agentAccountDao;
    }

    public void setAgentAccountDao(IAgentAccountDao _accountDao) {
        this._agentAccountDao = _accountDao;
    }
}
