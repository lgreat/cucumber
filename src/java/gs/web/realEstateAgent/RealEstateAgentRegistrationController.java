package gs.web.realEstateAgent;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.WelcomeMessageStatus;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.realEstateAgent.AgentAccount;
import gs.data.realEstateAgent.IAgentAccountDao;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 2/6/13
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/realEstateAgent/")
public class RealEstateAgentRegistrationController implements ReadWriteAnnotationController {
    private static Logger _logger = Logger.getLogger(RealEstateAgentRegistrationController.class);

    private static final String REGISTRATION_PAGE_VIEW = "/realEstateAgent/registrationHome";

    public static final String NEW_USER_COOKIE_HASH = "newAgentRegistration12830";

    @Autowired
    private IAgentAccountDao _agentAccountDao;

    @Autowired
    private IUserDao _userDao;

    @RequestMapping(value = "registration.page", method = RequestMethod.GET)
    public String showForm (HttpServletRequest request,
                            HttpServletResponse response) {
        return REGISTRATION_PAGE_VIEW;
    }

    @RequestMapping(value = "savePersonalInfo.page", method = RequestMethod.POST)
    public void onPersonalInfoSubmit (HttpServletRequest request,
                                      HttpServletResponse response,
                                      @RequestParam(value = "firstName", required = true) String fName,
                                      @RequestParam(value = "lastName", required = true) String lName,
                                      @RequestParam(value = "email", required = true) String email,
                                      @RequestParam(value = "password", required = true) String password) {

        response.setContentType("application/json");


        email = (email != null && StringUtils.isNotBlank(email.trim())) ? email.trim() : null;
        if(email == null) {
            outputJson(response, false);
            return;
        }

        User user = _userDao.findUserFromEmailIfExists(email);

        if(user != null && user.getId() != null) {
            setUserFields(fName, lName, user);
        }
        else {
            user = new User();
            user.setEmail(email);
            user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
            setUserFields(fName, lName, user);
            _userDao.saveUser(user);
        }

        setUserPassword(password, user);
        updateUserProfile(user);
        _userDao.updateUser(user);

        setUserCookie(user, request, response);

        outputJson(response, true);
    }

    @RequestMapping(value = "saveBusinessInfo.page", method = RequestMethod.POST)
    public void onBusinessInfoSubmit (HttpServletRequest request,
                                      HttpServletResponse response,
                                      @RequestParam(value = "companyName", required = false) String companyName,
                                      @RequestParam(value = "workNumber", required = false) String workNumber,
                                      @RequestParam(value = "cellNumber", required = false) String cellNumber,
                                      @RequestParam(value = "address", required = false) String address,
                                      @RequestParam(value = "city", required = false) String city,
                                      @RequestParam(value = "state", required = false) String state,
                                      @RequestParam(value = "zip", required = false) String zip) {

        response.setContentType("application/json");

        int userId = getUserIdFromCookie(request, response);

        if(userId == -1) {
            outputJson(response, false);
            return;
        }

        User user = _userDao.findUserFromId(userId);

        if(user == null || user.getId() == null) {
            outputJson(response, false);
            return;
        }

        AgentAccount agentAccount = _agentAccountDao.findAgentAccountByUserId(user.getId());
        if (agentAccount == null) {
            agentAccount = new AgentAccount(user);
            setAgentAccountFields(agentAccount, companyName, workNumber, cellNumber, address, city, state, zip);
            _agentAccountDao.save(agentAccount);
        }
        else {
            setAgentAccountFields(agentAccount, companyName, workNumber, cellNumber, address, city, state, zip);
            _agentAccountDao.updateAgentAccount(agentAccount);
        }

        outputJson(response, true);
    }

    private void setUserFields(String fName, String lName, User user) {
        if(user != null) {
            if(StringUtils.isNotBlank(fName)) {
                user.setFirstName(fName.trim());
            }
            if(StringUtils.isNotBlank(lName)) {
                user.setLastName(lName.trim());
            }
        }
    }

    private void setUserPassword(String password, User user) {
        try {
            if (StringUtils.isNotEmpty(password) && !user.isEmailValidated()) {
                user.setPlaintextPassword(password);
            }
        }
        catch (Exception ex) {
            _logger.debug("RealEstateAgentSignUpController: Unable to set user password");
        }
    }

    private void updateUserProfile(User user) {
        UserProfile userProfile;

        if (user.getUserProfile() != null && user.getUserProfile().getId() != null) {
            userProfile = user.getUserProfile();
            generateScreenName(userProfile, user.getId());
        }
        else {
            userProfile = new UserProfile();
            generateScreenName(userProfile, user.getId());
            userProfile.setUser(user);
            user.setUserProfile(userProfile);
        }
    }

    private void setUserCookie(User user, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(String.valueOf(("newAgentRegistration12830").hashCode()), String.valueOf(user.getId()));
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            // don't set domain for developer workstations so they can still access the cookie!!
            cookie.setDomain(".greatschools.org");
        }
        response.addCookie(cookie);
    }

    private Integer getUserIdFromCookie (HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();

        for(Cookie cookie : cookies) {
            if(String.valueOf((NEW_USER_COOKIE_HASH).hashCode()).equals(cookie.getName())) {
                try {
                    response.addCookie(cookie);
                    return Integer.parseInt(cookie.getValue());
                }
                catch (NumberFormatException ex) {
                    return -1;
                }
            }
        }
        return -1;
    }

    public void setAgentAccountFields(AgentAccount agentAccount, String companyName, String workNumber, String cellNumber,
                                      String address, String city, String state, String zip) {
        if(StringUtils.isNotBlank(workNumber)) {
            agentAccount.setWorkNumber(workNumber.trim());
        }
        if(StringUtils.isNotBlank(cellNumber)) {
            agentAccount.setCellNumber(cellNumber.trim());
        }
        if(StringUtils.isNotBlank(companyName)) {
            agentAccount.setCompanyName(companyName.trim());
        }
        if(StringUtils.isNotBlank(address)) {
            agentAccount.setAddress(address.trim());
        }
        if(StringUtils.isNotBlank(city)) {
            agentAccount.setCity(city.trim());
        }
        if(StringUtils.isNotBlank(state)) {
            agentAccount.setState(state.trim());
        }
        if(StringUtils.isNotBlank(zip)) {
            agentAccount.setZip(zip.trim());
        }
    }

    private void outputJson(HttpServletResponse response, boolean isSuccess) {
        JSONObject responseJson = new JSONObject();
        try {
            responseJson.accumulate("success", isSuccess);
            responseJson.write(response.getWriter());
            response.getWriter().flush();
        }
        catch (JSONException ex) {}
        catch (IOException ex) {}
    }


    private void generateScreenName(UserProfile userProfile, Integer userId) {
        if (userProfile != null) {
            userProfile.setScreenName("user" + userId);
            userProfile.setUpdated(new Date());
        }
    }

    public IAgentAccountDao getAgentAccountDao() {
        return _agentAccountDao;
    }

    public void setAgentAccountDao(IAgentAccountDao _accountDao) {
        this._agentAccountDao = _accountDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }
}
