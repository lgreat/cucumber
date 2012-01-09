package gs.web.admin;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.*;
import gs.data.security.IRoleDao;
import gs.data.security.Role;
import gs.data.util.DigestUtil;
import gs.web.admin.EspModerationCommand;
import gs.web.community.registration.EmailVerificationEmail;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Controller
@RequestMapping("/admin/espModerationForm.page")
public class EspModerationController implements ReadWriteAnnotationController {
    public static final String FORM_VIEW = "admin/espModerationForm";
    protected final Log _log = LogFactory.getLog(getClass());

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private IUserDao _userDao;

    @Autowired
    private ISchoolDao _schoolDao;

    @Autowired
    private IRoleDao _roleDao;

    private EmailVerificationEmail _emailVerificationEmail;

    @RequestMapping(method = RequestMethod.GET)
    public String showForm(ModelMap modelMap) {
        EspModerationCommand command = new EspModerationCommand();
        modelMap.addAttribute("espModerationCommand", command);
        populateModelWithMemberships(modelMap);
        return FORM_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String updateEspMembership(@ModelAttribute("espModerationCommand") EspModerationCommand command,
                                      BindingResult result,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {

        for (String idAndIndex : command.getEspMembershipIds()) {
            Long id = new Long(idAndIndex.substring(0, idAndIndex.indexOf("-")));
            int index = new Integer(idAndIndex.substring(idAndIndex.indexOf("-") + 1, idAndIndex.length())) - 1;

            EspMembership membership = getEspMembershipDao().findEspMembershipById(id, false);
            if (membership != null) {

                User user = membership.getUser();
                if (user != null) {

                    if ("approve".equals(command.getModeratorAction())) {

                        membership.setStatus(EspMembershipStatus.APPROVED);
                        membership.setActive(true);
                        Role role = _roleDao.findRoleByKey(Role.ESP_MEMBER);
                        if(!user.hasRole(Role.ESP_MEMBER)){
                            user.addRole(role);
                        }
                        getUserDao().updateUser(user);
                        sendESPVerificationEmail(request, user);

                    } else if ("reject".equals(command.getModeratorAction())) {
                        membership.setStatus(EspMembershipStatus.REJECTED);
                        sendGSVerificationEmail(request, user);
                    }

                    if (StringUtils.isNotBlank(command.getNote().get(index))) {
                        membership.setNote(command.getNote().get(index));
                    }
                    membership.setUpdated(new Date());
                    getEspMembershipDao().updateEspMembership(membership);
                }
            }
        }

        return "redirect:" + FORM_VIEW;
    }

    private void populateModelWithMemberships(ModelMap modelMap) {
        List<EspMembership> memberships = getEspMembershipDao().findAllEspMemberships(false);
        List<EspMembership> membershipsToProcess = new ArrayList<EspMembership>();
        List<EspMembership> approvedMemberships = new ArrayList<EspMembership>();
        List<EspMembership> rejectedMemberships = new ArrayList<EspMembership>();

        for (EspMembership membership : memberships) {
            long schoolId = membership.getSchoolId();
            School school = getSchoolDao().getSchoolById(membership.getState(), (int) schoolId);
            membership.setSchool(school);

            if (membership.getStatus().equals(EspMembershipStatus.PROCESSING)) {
                membershipsToProcess.add(membership);
            }
        }

        modelMap.put("membershipsToProcess", membershipsToProcess);
        modelMap.put("approvedMemberships", approvedMemberships);
        modelMap.put("rejectedMemberships", rejectedMemberships);
    }

    private void sendESPVerificationEmail(HttpServletRequest request, User user) {
        try {
            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            Date now = new Date();
            String nowAsString = String.valueOf(now.getTime());
            hash = DigestUtil.hashString(hash + nowAsString);

            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION_VALIDATION,
                    null,
                    hash + user.getId());
            builder.addParameter("date", nowAsString);
            //TODO change this to ESP form.
            String redirect = "school/esp/form.page";
            builder.addParameter("redirect", redirect);

            String verificationLink = builder.asAbsoluteAnchor(request, builder.asFullUrl(request)).asATag();
            //TODO send ET email with verificationLink as param.
            System.out.println("--verificationLink--------------" + verificationLink);
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

    public EmailVerificationEmail getEmailVerificationEmail() {
        return _emailVerificationEmail;
    }

    public void setEmailVerificationEmail(EmailVerificationEmail emailVerificationEmail) {
        _emailVerificationEmail = emailVerificationEmail;
    }


}