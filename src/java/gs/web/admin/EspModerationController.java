package gs.web.admin;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.school.EspMembership;
import gs.data.school.EspMembershipStatus;
import gs.data.school.EspResponse;
import gs.data.school.IEspMembershipDao;
import gs.data.school.IEspResponseDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.security.IRoleDao;
import gs.data.security.Role;
import gs.data.util.DigestUtil;
import gs.web.community.registration.EmailVerificationEmail;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/admin/espModerationForm.page")
public class EspModerationController implements ReadWriteAnnotationController {
    
    /**
     * Dedicated data struct for each moderation row.
     */
    public static class ModerationRow {
        private EspMembership _membership;
        private String _contactName;
        private String _contactEmail;
        private boolean _isDisabledUserReRequestingAccess;
        
        public ModerationRow(EspMembership _membership) {
            super();
            this._membership = _membership;
        }

        public EspMembership getMembership() {
            return _membership;
        }

        public String getContactName() {
            return _contactName;
        }

        public void setContactName(String contactName) {
            this._contactName = contactName;
        }

        public String getContactEmail() {
            return _contactEmail;
        }

        public void setContactEmail(String contactEmail) {
            this._contactEmail = contactEmail;
        }

        public boolean isDisabledUserReRequestingAccess() {
            return _isDisabledUserReRequestingAccess;
        }

        public void setDisabledUserReRequestingAccess(boolean isDisabledUserReRequestingAccess) {
            this._isDisabledUserReRequestingAccess = isDisabledUserReRequestingAccess;
        }
        
        public boolean isEmailMatch() {
            if(_contactEmail == null || _membership.getUser() == null) return false;
            return _contactEmail.equals(_membership.getUser().getEmail());
        }
        
        public String getAbsoluteWebUrl() {
            String webUrl = _membership.getWebUrl();
            if (StringUtils.isNotEmpty(webUrl) && !StringUtils.startsWithIgnoreCase("http://", webUrl)) {
                return "http://" + webUrl;
            }
            return webUrl;
        }
    }
    
    public static final String VIEW = "admin/espModerationForm";
    protected final Log _log = LogFactory.getLog(getClass());

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private IUserDao _userDao;

    @Autowired
    private ISchoolDao _schoolDao;

    @Autowired
    private IRoleDao _roleDao;
    
    private IEspResponseDao _espResponseDao;

    private EmailVerificationEmail _emailVerificationEmail;

    private ExactTargetAPI _exactTargetAPI;

    @RequestMapping(method = RequestMethod.GET)
    public String showForm(ModelMap modelMap) {
        EspModerationCommand command = new EspModerationCommand();
        modelMap.addAttribute("espModerationCommand", command);
        populateModelWithMemberships(modelMap);
        return VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String updateEspMembership(@ModelAttribute("espModerationCommand") EspModerationCommand command,
                                      BindingResult result,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {

        //The user has to check the check boxes in order to approve or reject.Hence we iterate over the checked check boxes for those "approve" or "reject" actions.
        //The user does not have to check the check boxes for the update action.Hence we loop over all the notes for the "update" action.
        if (("approve".equals(command.getModeratorAction()) || "reject".equals(command.getModeratorAction()) && command.getEspMembershipIds() != null && !command.getEspMembershipIds().isEmpty())) {

            //The checkbox has a key of membership id.
            for (Integer membershipId : command.getEspMembershipIds()) {

                //get the membership row.
                EspMembership membership = getEspMembershipDao().findEspMembershipById(membershipId, false);
                if (membership != null) {

                    User user = membership.getUser();
                    if (user != null) {

                        if ("approve".equals(command.getModeratorAction())) {
                            membership.setStatus(EspMembershipStatus.APPROVED);
                            membership.setActive(true);
                            Role role = _roleDao.findRoleByKey(Role.ESP_MEMBER);
                            if (!user.hasRole(Role.ESP_MEMBER)) {
                                user.addRole(role);
                            }
                            getUserDao().updateUser(user);
                            sendESPVerificationEmail(request, user);

                        } else if ("reject".equals(command.getModeratorAction())) {
                            membership.setStatus(EspMembershipStatus.REJECTED);
                            if (!user.isEmailValidated()) {
                                sendGSVerificationEmail(request, user);
                            }
                        }

                        //In case a note was added while approving or rejecting.
                        //Notes is the Map of membership id to string.
                        if (command.getNotes() != null && !command.getNotes().isEmpty() && command.getNotes().get(membership.getId()) != null) {
                            membership.setNote(command.getNotes().get(membership.getId()));
                        }
                        membership.setUpdated(new Date());
                        getEspMembershipDao().updateEspMembership(membership);
                    }
                }
            }
        } else if ("update".equals(command.getModeratorAction()) && command.getNotes() != null && !command.getNotes().isEmpty()) {
            //Notes is the Map of membership id to string.
            for (int membershipId : command.getNotes().keySet()) {
                if (command.getNotes().get(membershipId) != null) {
                    //get the membership row.
                    EspMembership membership = getEspMembershipDao().findEspMembershipById(membershipId, false);

                    if (membership != null) {
                        membership.setNote(command.getNotes().get(membership.getId()));
                        membership.setUpdated(new Date());
                        getEspMembershipDao().updateEspMembership(membership);
                    }
                }
            }
        }

        return "redirect:" + "/admin/espModerationForm.page";
    }

    private void populateModelWithMemberships(ModelMap modelMap) {
        List<EspMembership> memberships = getEspMembershipDao().findAllEspMemberships(false);
        List<ModerationRow> membershipsToProcess = new ArrayList<ModerationRow>();
        List<EspMembership> approvedMemberships = new ArrayList<EspMembership>();
        List<EspMembership> rejectedMemberships = new ArrayList<EspMembership>();

        for (EspMembership membership : memberships) {
            long schoolId = membership.getSchoolId();
            School school = getSchoolDao().getSchoolById(membership.getState(), (int) schoolId);
            membership.setSchool(school);

            if (membership.getStatus().equals(EspMembershipStatus.PROCESSING)) {
                ModerationRow mrow = new ModerationRow(membership);
                membershipsToProcess.add(mrow);
            
                // contact name and email
                HashSet<String> espResponseKeys = new HashSet<String>();
                espResponseKeys.add("old_contact_name");
                espResponseKeys.add("old_contact_email");
                List<EspResponse> list = _espResponseDao.getResponsesByKeys(school, espResponseKeys, true);
                if(list != null) {
                    for(EspResponse r : list) {
                        if("old_contact_name".equals(r.getKey())) mrow.setContactName(r.getValue());
                        if("old_contact_email".equals(r.getKey())) mrow.setContactEmail(r.getValue());
                    }
                }
                
                // is disabled user re-requesting access?
                for(EspMembership rejected : rejectedMemberships) {
                    try {
                        long membershipSchoolId = membership.getSchoolId() == null ? -1 : membership.getSchoolId().longValue();
                        long rejectedSchoolId = rejected.getSchoolId() == null ? -1 : rejected.getSchoolId().longValue();
                        long membershipUserId = membership.getUser().getId().longValue();
                        long rejectedUserId = rejected.getUser().getId().longValue();
                        if(membershipSchoolId == rejectedSchoolId && membershipUserId == rejectedUserId) {
                            mrow.setDisabledUserReRequestingAccess(true);
                            break;
                        }
                    }
                    catch(NullPointerException e) {
                        continue;
                    }
                }
            }

        }
        
        modelMap.put("membershipsToProcess", membershipsToProcess);
        modelMap.put("approvedMemberships", approvedMemberships);
        modelMap.put("rejectedMemberships", rejectedMemberships);
    }

    protected void sendESPVerificationEmail(HttpServletRequest request, User user) {
        try {
            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            Date now = new Date();
            String nowAsString = String.valueOf(now.getTime());
            hash = DigestUtil.hashString(hash + nowAsString);
            String redirect = new UrlBuilder(UrlBuilder.ESP_DASHBOARD).toString();

            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.REGISTRATION_VALIDATION, null, hash + user.getId());
            urlBuilder.addParameter("date", nowAsString);
            urlBuilder.addParameter("redirect", redirect);

            StringBuffer espVerificationUrl = new StringBuffer("<a href=\"");
            espVerificationUrl.append(urlBuilder.asFullUrl(request));
            espVerificationUrl.append("\">Click here to verify</a>");

            Map<String, String> emailAttributes = new HashMap<String, String>();
            emailAttributes.put("HTML__espVerificationUrl", espVerificationUrl.toString());

            getExactTargetAPI().sendTriggeredEmail("ESP-verification", user, emailAttributes);

        } catch (Exception e) {
            _log.error("Error sending verification email message: " + e, e);
        }
    }

    private void sendGSVerificationEmail(HttpServletRequest request, User user) {
        try {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.HOME);
            String redirectUrl = urlBuilder.asFullUrl(request);
            Map<String, String> otherParams = new HashMap<String, String>();
            getEmailVerificationEmail().sendVerificationEmail(request, user, redirectUrl, otherParams);

        } catch (Exception e) {
            _log.error("Error sending verification email message: " + e, e);
        }

    }

    public IEspMembershipDao getEspMembershipDao() {
        return _espMembershipDao;
    }

    public void setEspMembershipDao(IEspMembershipDao espMembershipDao) {
        _espMembershipDao = espMembershipDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IRoleDao getRoleDao() {
        return _roleDao;
    }

    public void setRoleDao(IRoleDao roleDao) {
        _roleDao = roleDao;
    }
    
    public IEspResponseDao getEspResponseDao() {
        return _espResponseDao;
    }
    
    public void setEspResponseDao(IEspResponseDao espResponseDao) {
        this._espResponseDao = espResponseDao;
    }

    public EmailVerificationEmail getEmailVerificationEmail() {
        return _emailVerificationEmail;
    }

    public void setEmailVerificationEmail(EmailVerificationEmail emailVerificationEmail) {
        _emailVerificationEmail = emailVerificationEmail;
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
    }

}